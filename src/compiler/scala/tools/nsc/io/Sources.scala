package scala.tools.nsc
package io

import util.ClassPath
import java.util.concurrent.{ Future, ConcurrentHashMap, ExecutionException }
import java.util.zip.ZipException
import Jar.{ isJarOrZip, locateByClass }
import collection.JavaConverters._
import Properties.{ envOrElse, propOrElse }

class Sources(val path: String) {
  val expandedPath        = ClassPath.join(ClassPath expandPath path: _*)
  val cache               = new ConcurrentHashMap[String, List[Fileish]]
  def allNames            = cache.keys.asScala.toList.sorted
  def apply(name: String) = get(name)
  def size                = cache.asScala.values map (_.length) sum

  private var debug = false
  private def dbg(msg: => Any) = if (debug) Console println msg
  private val partitioned = ClassPath toPaths expandedPath partition (_.isDirectory)

  val dirs   = partitioned._1 map (_.toDirectory)
  val jars   = partitioned._2 filter isJarOrZip map (_.toFile)
  val (isDone, force) = {
    val f1  = spawn(calculateDirs())
    val f2  = spawn(calculateJars())    
    val fn1 = () => { f1.isDone() && f2.isDone() }
    val fn2 = () => { f1.get() ; f2.get() ; () }

    (fn1, fn2)
  }
  
  private def catchZip(body: => Unit): Unit = {
    try body
    catch { case x: ZipException => dbg("Caught: " + x) }
  }

  private def calculateDirs() =
    dirs foreach { d => dbg(d) ; catchZip(addSources(d.deepFiles map (x => Fileish(x)))) }

  private def calculateJars() = 
    jars foreach { j => dbg(j) ; catchZip(addSources(new Jar(j).fileishIterator)) }
  
  private def addSources(fs: TraversableOnce[Fileish]) =
    fs foreach { f => if (f.isSourceFile) add(f.name, f) }

  private def get(key: String): List[Fileish] =
    if (cache containsKey key) cache.get(key) else Nil

  private def add(key: String, value: Fileish) = {
    if (cache containsKey key) cache.replace(key, value :: cache.get(key))
    else cache.put(key, List(value))
  }
  override def toString = "Sources(%d dirs, %d jars, %d sources)".format(
    dirs.size, jars.size, cache.asScala.values map (_.length) sum
  )
}

trait LowPrioritySourcesImplicits {
  self: Sources.type =>

  implicit def fallbackSources: Sources = defaultSources
}

object Sources extends LowPrioritySourcesImplicits {
  // Examples of what libraryJar might be, each of which we'd like to find
  // the source files automatically:
  //
  // /scala/trunk/build/pack/lib/scala-library.jar
  // /scala/trunk/build/quick/classes/library
  // /scala/inst/scala-2.9.0.r24213-b20110206233447/lib/scala-library.jar
  private def libraryJar = locateByClass(classOf[ScalaObject]) map (_.toAbsolute.path)
  private def autoSourcePaths: List[String] = libraryJar.toList flatMap { lib =>
    val markers = List("build/pack/lib", "build/quick/classes", "scala-library.jar")
    markers filter (lib contains _) flatMap { m =>
      val dir = Path(lib take lib.indexOf(m)) / "src"
      
      if (dir.exists) ClassPath.expandDir(dir.path)
      else Nil
    }
  }
  
  val sourcePathEnv   = envOrElse("SOURCEPATH", "")
  val defaultSources  = apply(autoSourcePaths :+ sourcePathEnv: _*)
    
  def apply(paths: String*): Sources = new Sources(ClassPath.join(paths: _*))
}
