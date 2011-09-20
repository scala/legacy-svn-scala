resolvers += Resolver.url(
  "Typesafe nightlies", 
  url("https://typesafe.artifactoryonline.com/typesafe/ivy-snapshots/")
)(Resolver.ivyStylePatterns)

resolvers ++= Seq(
  "junit interface repo" at "https://repository.jboss.org/nexus/content/repositories/scala-tools-releases",
  ScalaToolsSnapshots
)

organization := "org.scala-lang"

version := "2.10.0-SNAPSHOT"

scalaVersion := "2.10.0-SNAPSHOT"


// logLevel := Level.Debug

pomExtra := <xml:group>
  <inceptionYear>2002</inceptionYear>
    <licenses>
      <license>
        <name>BSD-like</name>
        <url>http://www.scala-lang.org/downloads/license.html</url>
      </license>
    </licenses>
    <scm>
      <connection>scm:svn:http://lampsvn.epfl.ch/svn-repos/scala/scala/trunk</connection>
    </scm>
    <issueManagement>
      <system>jira</system>
      <url>http://issues.scala-lang.org</url>
    </issueManagement>
  </xml:group>


//onLoad in Global <<= (onLoad in Global) ?? { (s: State) => println("Base onload"); s }
onLoad in Global := { (state: State) =>
  //println("--------------------------------")
  //println("Fixing up scalacheck references.")
  //println("--------------------------------")
  // TODO -fix up scalacheck
  val extracted = Project.extract(state)
  import extracted._
  def f(s: Setting[_]): Setting[_] = s.key.key match {
    case scalaInstance.key =>
      val projectScope = s.key.scope.project
      if(projectScope.isSelect && projectScope.asInstanceOf[Select[ProjectReference]].s == scalacheck) {
        println("Rewriting setting for: " + s)
        // TODO - Do we need to modify this to be in scope of scalacheck? most likely...
        quickScalaInstance
      } else s
    case onLoad.key =>
      // Remove onloads now that we've fixed everything!
      s.asInstanceOf[Setting[State => State]].mapInit((_,_) => identity _)
    case _ => s
   }
   val transformed = session.mergeSettings map ( s => f(s) )
   val newStructure = Load.reapply(transformed, structure)
   Project.setProject(session, newStructure, state)
}
