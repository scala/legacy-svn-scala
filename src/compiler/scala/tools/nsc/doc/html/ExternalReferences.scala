package scala.tools.nsc.doc.html


object ExternalReferences {

  val mappings: Seq[TemplateToExternalReference] = Seq(JDKLinks, ApacheCommonsLinks)

  def getLink(entity: String): Option[String] = mappings.find(_.isDefinedAt(entity)).map(_.apply(entity))

  object JDKLinks extends JavaDocBasedExternalReference {
    val supportedPrefixes = Seq("java.", "javax.", "org.ietf.jgss", "org.omg.", "org.w3c.dom.", "org.xml.sax.", "com.sun.", "sun.", "sunw.")
    val addressPrefix = "http://download.oracle.com/javase/7/docs/api/"
  }

  object ApacheCommonsLinks extends JavaDocBasedExternalReference {
    lazy val supportedPrefixes = Seq(prefix)
    private val prefix = "org.apache.commons."
    val addressPrefix = "http://commons.apache.org/"
    override def apply(entity: String) = {
      var packageName = entity.substring(entity.indexOf(prefix) + prefix.length, entity.length)
      packageName = packageName.substring(0, packageName.indexOf('.'))
      addressPrefix + packageName + "/api-release/" +  entity.replace('.', '/') + ".html"
    }
  }

  trait JavaDocBasedExternalReference extends TemplateToExternalReference {
    val addressPrefix: String
    def apply(entity: String) = addressPrefix + entity.replace('.', '/') + ".html"
  }

  trait TemplateToExternalReference extends PartialFunction[String, String] {
    val supportedPrefixes: Seq[String]
    override def isDefinedAt(entity: String) = supportedPrefixes.exists(entity.startsWith)
  }
}