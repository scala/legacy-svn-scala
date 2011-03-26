import org.scalacheck._
import org.scalacheck.Prop._

import java.net.URLClassLoader

object XMLUtil {
  import scala.xml._

  def stripGroup(seq: Node): Node = {
    seq match {
      case group: Group => {
        <div class="group">{ group.nodes.map(stripGroup _) }</div>
      }
      case e: Elem => {
        val child = e.child.map(stripGroup _)
        Elem(e.prefix, e.label, e.attributes, e.scope, child : _*)
      }
      case _ => seq
    }
  }
}

object Test extends Properties("HtmlFactory") {
  import scala.tools.nsc.doc.{DocFactory, Settings}
  import scala.tools.nsc.doc.model.IndexModelFactory
  import scala.tools.nsc.doc.html.HtmlFactory

  def getClasspath = {
    // these things can be tricky
    // this test previously relied on the assumption that the current thread's classloader is an url classloader and contains all the classpaths
    // does partest actually guarantee this? to quote Leonard Nimoy: The answer, of course, is no.
    // this test _will_ fail again some time in the future.
    val paths = Thread.currentThread.getContextClassLoader.asInstanceOf[URLClassLoader].getURLs.map(_.getPath)
    val morepaths = Thread.currentThread.getContextClassLoader.getParent.asInstanceOf[URLClassLoader].getURLs.map(_.getPath)
    (paths ++ morepaths).mkString(java.io.File.pathSeparator)
  }

  def createFactory = {
    val settings = new Settings({Console.err.println(_)})
    settings.classpath.value = getClasspath

    val reporter = new scala.tools.nsc.reporters.ConsoleReporter(settings)
    new DocFactory(reporter, settings)
  }

  def createTemplates(basename: String) = {
    val result = scala.collection.mutable.Map[String, scala.xml.NodeSeq]()

    createFactory.makeUniverse(List("test/scaladoc/resources/"+basename)) match {
      case Some(universe) => {
        val index = IndexModelFactory.makeIndex(universe)
        (new HtmlFactory(universe, index)).writeTemplates((page) => {
          result += (page.absoluteLinkTo(page.path) -> page.body)
        })
      }
      case _ => ;
    }

    result
  }

  def shortComments(root: scala.xml.Node) =
    XMLUtil.stripGroup(root).descendant.flatMap {
      case e: scala.xml.Elem => {
        if (e.attribute("class").toString.contains("shortcomment")) {
          Some(e)
        } else {
          None
        }
      }
      case _ => None
    }

  property("Trac #3790") = {
    createTemplates("Trac3790.scala")("Trac3790.html") match {
      case node: scala.xml.Node => {
        val comments = shortComments(node)

        comments.exists { _.toString.contains(">A lazy String\n</p>") } &&
          comments.exists { _.toString.contains(">A non-lazy String\n</p>") }
      }
      case _ => false
    }
  }

  property("Trac #4306") = {
    val files = createTemplates("Trac4306.scala")
    files("com/example/trac4306/foo/package$$Bar.html") != None
  }

  property("Trac #4366") = {
    createTemplates("Trac4366.scala")("Trac4366.html") match {
      case node: scala.xml.Node => {
        shortComments(node).exists { n => {
          val str = n.toString
          str.contains("<code>foo</code>") && str.contains("</strong>")
        } }
      }
      case _ => false
    }
  }

  property("Trac #4358") = {
    createTemplates("Trac4358.scala")("EasyMockSugar.html") match {
      case node: scala.xml.Node =>
        ! shortComments(node).exists {
          _.toString.contains("<em>i.</em>")
        }
      case _ => false
    }
  }

  property("Trac #4180") = {
    createTemplates("Trac4180.scala")("Test.html") != None
  }
}
