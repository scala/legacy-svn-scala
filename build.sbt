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
