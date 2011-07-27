import sbt._
import Keys._

object ScalaBuild extends Build {
  // lazy val projects  = Seq(root, compQuick, libQuick)
  lazy val root      = Project("scala", file(".")) aggregate(lockerComp)

  // --------------------------------------------------------------
  //  Libraries used by Scalac that change infrequently
  //  (or hopefully so).
  // --------------------------------------------------------------


  // Jline nested project.   Compile this sucker once and be done.
  lazy val jline = Project("jline", file("src/jline"))
  // Fast Java Bytecode Generator (nested in every scala-compiler.jar)
  lazy val fjbg = Project("fjbg", file("src/fjbg"),
                          settings = Defaults.defaultSettings ++ Seq(
                            javaSource in Compile := file("src/fjbg"),
                            javacOptions ++= Seq("-target", "1.5")))

  // MSIL code generator
  // TODO - This probably needs to compile against quick, but Sabbus
  // had it building against locker, so we'll do worse and build
  // build against STARR for now.
  lazy val msil = Project("msil", file("src/msil"),
                          settings = Defaults.defaultSettings ++ Seq(
                            javaSource in Compile := file("src/msil"),
                            scalaSource in Compile := file("src/msil"),
                            defaultExcludes ~= (_ || "tests"),
                            javacOptions ++= Seq("-target", "1.5",
                                                 "-source", "1.4")))

  import LayeredBuild._

  // --------------------------------------------------------------
  //  The magic kingdom.
  //  Layered compilation of Scala.
  //   Stable Reference -> Locker ('Lockable' dev version) -> Quick -> Strap (Binary compatibility testing)
  // --------------------------------------------------------------

    lazy val lockerLib = Project("locker-library", file("."), settings = layeredProjectSettings("quick") ++
    Seq(name := "scala-library",
        layerProjectName := "library",
        version := "quick",
        dependencyClasspath := Seq(),
        scalaInstance <<= appConfiguration map { app =>
		      val launcher = app.provider.scalaProvider.launcher
          // TODO - This is STARR, reference STARR using dependency resolution
          // TODO - Explicitly version STARR
          ScalaInstance(
            file("lib/scala-library.jar"),
            file("lib/scala-compiler.jar"),
            launcher,
            file("lib/fjbg.jar"))
        }
    )
  )
  lazy val lockerComp = Project("locker-compiler", file("."), settings = layeredProjectSettings("quick") ++
    Seq(name := "scala-compiler",
        layerProjectName := "compiler",
        version := "locker",
        dependencyClasspath := Seq(),
        scalaInstance <<= (appConfiguration, classDirectory in lockerLib) map { (app, lib) =>
		      val launcher = app.provider.scalaProvider.launcher
          // TODO - Figure out dependency resoltuion to look up STARR and only use compiler *not* library...
          ScalaInstance(
            lib,
            file("lib/scala-compiler.jar"),
            launcher,
            file("lib/fjbg.jar"))
        }
    )
  ) dependsOn(lockerLib)
}
