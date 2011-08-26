import sbt._
import Keys._

object Release {

  // TODO - move more of the dist project over here...

  /** This generates a  properties file, if it does not already exist, with the maximum lastmodified timestamp
    * of any source file. */
  def generatePropertiesFile(name: String)(baseDirectory: File, version: String, dir: File): Seq[File] = {
    val target = dir / name
    // TODO - Regenerate on triggers, like recompilation or something...
    // TODO - also think about pulling git last-commit for this...
    if (!target.exists) {
      val ts = getLastModified(baseDirectory)
      val formatter = new java.text.SimpleDateFormat("yyyyMMdd'T'HHmmss")
      formatter.setTimeZone(java.util.TimeZone.getTimeZone("GMT"))
      val fullVersion = version + "." + formatter.format(new java.util.Date(ts))
      makePropertiesFile(target, fullVersion)
    }
    target :: Nil
  }
  
  // This creates the *.properties file used to determine the current version of scala at runtime.  TODO - move these somewhere utility like.
  def makePropertiesFile(f: File, version: String): Unit =
    IO.write(f, "version.number = "+version+"\ncopyright.string = Copyright 2002-2011, LAMP/EPFL")

  def makeFullVersionString(baseDirectory: File, baseVersion: String) = baseVersion+"."+getLastModified(baseDirectory)

  // TODO - Something that doesn't take so long...
  def allSourceFiles(baseDirectory: File) = (baseDirectory / "src") ** ("*.scala" | "*.java" )

  def getLastModified(baseDirectory: File) =
    allSourceFiles(baseDirectory).get.map(_.lastModified).max
  
}
