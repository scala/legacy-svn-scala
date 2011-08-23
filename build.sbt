// resolvers += ScalaToolsSnapshots
resolvers += "junit interface repo" at "https://repository.jboss.org/nexus/content/repositories/scala-tools-releases"

version := "2.10.0-SNAPSHOT"

// TODO - Should this be the same as the version we're building?
scalaVersion := "2.9.1.RC3"

organization := "org.scala-lang"

ivyScala ~= { (is: Option[IvyScala]) => is.map(_.copy(checkExplicit = false) ) }
