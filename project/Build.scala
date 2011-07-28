import sbt._
import Keys._

object ScalaBuild extends LayeredBuild {
  // lazy val projects  = Seq(root, compQuick, libQuick)
  lazy val root      = Project("scala", file(".")) aggregate(lockerLib)

  // --------------------------------------------------------------
  //  Libraries used by Scalac that change infrequently
  //  (or hopefully so).
  // --------------------------------------------------------------


  // Jline nested project.   Compile this sucker once and be done.
  override lazy val jline = Project("jline", file("src/jline"))
  // Fast Java Bytecode Generator (nested in every scala-compiler.jar)
  override lazy val fjbg = Project("fjbg", file("."),
                          settings = Defaults.defaultSettings ++ Seq(
                            javaSource in Compile := file("src/fjbg"),
                            javacOptions ++= Seq("-target", "1.5"),
                            target := file("target/fjbg"),
                            (classDirectory in Compile) <<= target(_ / "classes")))

  // Forkjoin backport
  override lazy val forkjoin = Project("forkjoin", file("."),
                          settings = Defaults.defaultSettings ++ Seq(
                            javaSource in Compile := file("src/forkjoin"),
                            javacOptions ++= Seq("-target", "1.5"),  
                            target := file("target/forkjoin"),                          
                            (classDirectory in Compile) <<= target(_ / "classes")))

  // MSIL code generator
  // TODO - This probably needs to compile against quick, but Sabbus
  // had it building against locker, so we'll do worse and build
  // build against STARR for now.
  lazy val msil = Project("msil", file("."),
                          settings = Defaults.defaultSettings ++ Seq(
                            javaSource in Compile := file("src/msil"),
                            scalaSource in Compile := file("src/msil"),
                            defaultExcludes ~= (_ || "tests"),
                            javacOptions ++= Seq("-target", "1.5",
                                                 "-source", "1.4"),
                            target := file("target/msil"),                          
                            (classDirectory in Compile) <<= target(_ / "classes")))


  // --------------------------------------------------------------
  //  The magic kingdom.
  //  Layered compilation of Scala.
  //   Stable Reference -> Locker ('Lockable' dev version) -> Quick -> Strap (Binary compatibility testing)
  // --------------------------------------------------------------

  lazy val (lockerLib, lockerComp) = makeLayer("locker", file("build/locker/classes/library"), file("build/locker/classes/compiler"))

/*
    lazy val lockerLib = Project("locker-library", file("."), settings = layeredProjectSettings("locker") ++
    Seq(name := "scala-library",
        layerProjectName := "library",
        version := "locker",
        // TODO - use depends on.
        dependencyClasspath <<= (classDirectory in forkjoin in Compile) map { 
          (fj) => Seq(fj) map Attributed.blank },
        scalaInstance <<= appConfiguration map { app =>
          val launcher = app.provider.scalaProvider.launcher
          // TODO - This is STARR, reference STARR using dependency resolution
          // TODO - Explicitly version STARR
          ScalaInstance(
            file("build/locker/classes/library"),
            file("build/locker/classes/compiler"),
            launcher,
            file("lib/fjbg.jar"))
        }
    )
  ) aggregate(forkjoin)
  lazy val lockerComp = Project("locker-compiler", file("."), settings = layeredProjectSettings("locker") ++
    Seq(name := "scala-compiler",
        layerProjectName := "compiler",
        version := "locker",
        // TODO - Use depends on *and* SBT's magic dependency mechanisms...
        dependencyClasspath <<= (classDirectory in forkjoin in Compile,
                                 classDirectory in lockerLib in Compile,
                                 classDirectory in fjbg in Compile,
                                 classDirectory in jline in Compile,
                                 classDirectory in msil in Compile) map {
          (fj, lib, fjbg, jline, msil) =>
            Seq(fj, lib, fjbg, jline, msil, file("lib/ant/ant.jar")) map Attributed.blank
        },
        scalaInstance <<= (appConfiguration, classDirectory in lockerLib) map { (app, lib) =>
		      val launcher = app.provider.scalaProvider.launcher
          // TODO - Figure out dependency resoltuion to look up STARR and only use compiler *not* library...
          ScalaInstance(
            file("build/locker/classes/library"),
            file("build/locker/classes/compiler"),
            launcher,
            file("lib/fjbg.jar"))
        }
    )
  ) aggregate(fjbg, jline, lockerLib)
  */
}
