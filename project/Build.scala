import sbt._
import Keys._

object ScalaBuild extends Build {
  // lazy val projects  = Seq(root, compQuick, libQuick)
  lazy val root      = Project("scala", file(".")) // TODO - aggregate on, say... quick


  // These are setting overrides for most artifacts in the Scala build file.
  def settingOverrides: Seq[Setting[_]] = Seq(
                             publishArtifact in packageBin := false,
                             publishArtifact in packageSrc := false,
                             javaSource in Compile <<= (baseDirectory, name) apply (_ / "src" / _),
                             target <<= (baseDirectory, name) apply (_ / "target" / _),
                             (classDirectory in Compile) <<= target(_ / "classes"),
                             javacOptions ++= Seq("-target", "1.5"),
                             scalaSource in Compile <<= (baseDirectory, name) apply (_ / "src" / _),
                             autoScalaLibrary := false,
                             unmanagedJars := Seq()
                            )
  // TODO - Figure out a way to uniquely determine a version to assign to Scala builds...
  def currentUniqueRevision = "0.1"

  // --------------------------------------------------------------
  //  Libraries used by Scalac that change infrequently
  //  (or hopefully so).
  // --------------------------------------------------------------

  // Jline nested project.   Compile this sucker once and be done.
  lazy val jline = Project("jline", file("src/jline"))
  // Fast Java Bytecode Generator (nested in every scala-compiler.jar)
  lazy val fjbg = Project("fjbg", file(".")) settings(settingOverrides : _*)

  // Forkjoin backport
  lazy val forkjoin = Project("forkjoin", file(".")) settings(settingOverrides : _*)

  // MSIL code generator
  // TODO - This probably needs to compile against quick, but Sabbus
  // had it building against locker, so we'll do worse and build
  // build against STARR for now.
  lazy val msil = Project("msil", file(".")) settings((settingOverrides ++ Seq(
                            defaultExcludes ~= (_ || "tests"),
                            javacOptions ++= Seq("-source", "1.4"),
                            target := file("target/msil")                          
                          )): _*)


  // --------------------------------------------------------------
  //  The magic kingdom.
  //  Layered compilation of Scala.
  //   Stable Reference -> Locker ('Lockable' dev version) -> Quick -> Strap (Binary compatibility testing)
  // --------------------------------------------------------------

  // Need a report on this...
  // TODO - Resolve STARR from a repo..
  def STARR = scalaInstance <<= appConfiguration map { app =>
    val launcher = app.provider.scalaProvider.launcher
    ScalaInstance(
      file("build/locker/classes/library"),
      file("build/locker/classes/compiler"),
      launcher,
      file("lib/fjbg.jar"))
  }

  // Locker is a lockable Scala compiler that can be built of 'current' source to perform rapid development.
  lazy val (lockerLib, lockerComp) = makeLayer("locker", STARR)
  lazy val locker = Project("locker", file(".")) aggregate(lockerLib, lockerComp)

  // Quick is the general purpose project layer for the Scala compiler.
  lazy val (quickLib, quickComp) = makeLayer("quick", makeScalaReference("locker", lockerLib, lockerComp, fjbg))
  lazy val quick = Project("quick", file(".")) aggregate(quickLib, quickComp)




  def makeScalaReference(layer : String, library: Project, compiler: Project, fjbg: Project) =
     scalaInstance <<= (appConfiguration,
                        (exportedProducts in library in Compile),
                        (exportedProducts in compiler in Compile),
                        (exportedProducts in fjbg in Compile)) map {
    (app, lib: Classpath, comp: Classpath, fjbg : Classpath) =>
    val launcher = app.provider.scalaProvider.launcher
    // TODO - Figure out a better way here, or bug Mark.
    if (lib.length != 1 || comp.length != 1) {
      error("Cannot build a ScalaReference with more than one classpath element")
    }
    ScalaInstance(
      layer + "-" + currentUniqueRevision,
      lib.head.data,
      comp.head.data,
      launcher,
      (fjbg.map(_.data):_*))
  }

  def makeLayer(layer: String, referenceScala: Setting[Task[ScalaInstance]]) : (Project, Project) = {
    // TODO - Make version number for library...
    val library = Project(layer + "-library", file("."))  settings( (settingOverrides ++
      Seq(version := layer,
          // TODO - use depends on.
          unmanagedClasspath in Compile <<= (exportedProducts in forkjoin in Compile).identity,
          scalaSource in Compile <<= (baseDirectory) apply (_ / "src" / "library"),
          referenceScala
      )) :_*)

    // Define the compiler
    val compiler = Project(layer + "-compiler", file(".")) settings((settingOverrides ++
    Seq(version := layer,
        scalaSource in Compile <<= (baseDirectory) apply (_ / "src" / "compiler"),
        // TODO - Use depends on *and* SBT's magic dependency mechanisms...
        unmanagedClasspath in Compile <<= (
                                 exportedProducts in forkjoin in Compile,
                                 exportedProducts in library in Compile,
                                 exportedProducts in fjbg in Compile,
                                 exportedProducts in jline in Compile,
                                 exportedProducts in msil in Compile) map {
          (fj, lib, fjbg, jline, msil) => fj ++ lib ++ fjbg ++ jline ++ msil
        },
        libraryDependencies += "org.apache.ant" % "ant" % "1.8.2",
        referenceScala
      )
    ):_*)

    // Return the generated projects.
    (library, compiler)
  }
}
