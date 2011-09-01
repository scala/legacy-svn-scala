// resolvers += ScalaToolsSnapshots
resolvers += "junit interface repo" at "https://repository.jboss.org/nexus/content/repositories/scala-tools-releases"

resolvers += Resolver.url("Typesafe nightlies", url("https://typesafe.artifactoryonline.com/typesafe/ivy-snapshots/"))(Resolver.ivyStylePatterns)

version := "2.10.0-SNAPSHOT"

// TODO - Should this be the same as the version we're building?
//scalaVersion := "2.9.1.RC3"

organization := "org.scala-lang"

ivyScala ~= { (is: Option[IvyScala]) => is.map(_.copy(checkExplicit = false) ) }

pomIncludeRepository := { _ => false }

publishMavenStyle := true

makePomConfiguration <<= makePomConfiguration apply (_.copy(configurations = Some(Seq(Compile, Default))))

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
