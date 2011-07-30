import sbt._
import Keys._

object ScalaBuild extends Build {
  lazy val root = Project("scala", file(".")) // TODO - aggregate on, say... quick


  // These are setting overrides for most artifacts in the Scala build file.
  def settingOverrides: Seq[Setting[_]] = Seq(
                             publishArtifact in packageDoc := false,
                             publishArtifact in packageSrc := false,
                             javaSource in Compile <<= (baseDirectory, name) apply (_ / "src" / _),
                             target <<= (baseDirectory, name) apply (_ / "target" / _),
                             (classDirectory in Compile) <<= target(_ / "classes"),
                             javacOptions ++= Seq("-target", "1.5"),
                             scalaSource in Compile <<= (baseDirectory, name) apply (_ / "src" / _),
                             autoScalaLibrary := false,
                             unmanagedJars := Seq(),
                             unmanagedBase <<= baseDirectory(_ / "useless" / "directory" / "name")
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
  // TODO - This probably needs to compile in the layers, but Sabbus
  // had it building against locker, so we'll do worse and build
  // build against STARR for now.
  lazy val msilSettings = settingOverrides ++ Seq(
                            defaultExcludes ~= (_ || "tests"),
                            javacOptions ++= Seq("-source", "1.4"),
                            STARR                         
                          )
  lazy val msil = Project("msil", file(".")) settings(msilSettings: _*)


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

  // Reference to quick scala instance.
  def quickScalaInstance = makeScalaReference("quick", quickLib, quickComp, fjbg)
  def quickScalaLibraryDependency = unmanagedClasspath in Compile <++= (exportedProducts in quickLib in Compile).identity
  def quickScalaCompilerDependency = unmanagedClasspath in Compile <++= (exportedProducts in quickComp in Compile).identity


  // --------------------------------------------------------------
  //  Helper methods for layered compilation.
  // --------------------------------------------------------------
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
      (fjbg.files:_*))
  }
  
  // Creates a "layer" of Scala compilation.  That is, this will build the next version of Scala from a previous version.
  // Returns the library project and compiler project from the next layer.
  // Note:  The library and compiler are not *complete* in the sense that they are missing things like "actors" and "fjbg".
  def makeLayer(layer: String, referenceScala: Setting[Task[ScalaInstance]]) : (Project, Project) = {
    // TODO - Make version number for library...
    val library = Project(layer + "-library", file("."))  settings( (settingOverrides ++
      Seq(version := layer,
          // TODO - use depends on.
          unmanagedClasspath in Compile <<= (exportedProducts in forkjoin in Compile).identity,
          scalaSource in Compile <<= (baseDirectory) apply (_ / "src" / "library"),
          // TODO - Allow other scalac option settings.
          scalacOptions in Compile <<= (scalaSource in Compile) map (src => Seq("-sourcepath", src.getAbsolutePath)),
          referenceScala
      )) :_*)

    // Define the compiler
    val compiler = Project(layer + "-compiler", file(".")) settings((settingOverrides ++
    Seq(version := layer,
        scalaSource in Compile <<= (baseDirectory) apply (_ / "src" / "compiler"),
        // TODO - Use depends on *and* SBT's magic dependency mechanisms...
        unmanagedClasspath in Compile <<= Seq(forkjoin, library, fjbg, jline, msil).map(exportedProducts in Compile in _).join.map(_.map(_.flatten)),
        libraryDependencies += "org.apache.ant" % "ant" % "1.8.2",
        referenceScala
      )
    ):_*)

    // Return the generated projects.
    (library, compiler)
  }

  // --------------------------------------------------------------
  //  Projects dependent on layered compilation (quick)
  // --------------------------------------------------------------
  // TODO - in sabbus, these all use locker to build...
  lazy val dependentProjectSettings = settingOverrides ++ Seq(quickScalaInstance, quickScalaLibraryDependency)
  lazy val actors = Project("actors", file(".")) settings(dependentProjectSettings:_*) dependsOn(forkjoin)
  lazy val dbc = Project("dbc", file(".")) settings(dependentProjectSettings:_*)
  lazy val swing = Project("swing", file(".")) settings(dependentProjectSettings:_*)
  lazy val scalacheck = Project("scalacheck", file(".")) settings(dependentProjectSettings:_*)
  // Things that compile against the compiler.
  lazy val compilerDependentProjectSettings = dependentProjectSettings ++ Seq(quickScalaCompilerDependency)
  lazy val scalap = Project("scalap", file(".")) settings(compilerDependentProjectSettings:_*)
  lazy val partestSettings = compilerDependentProjectSettings ++ Seq(libraryDependencies += "org.apache.ant" % "ant" % "1.8.2")
  lazy val partest = Project("partest", file(".")) settings(partestSettings:_*)  dependsOn(actors,forkjoin,scalap)
  // TODO - generate scala properties file...

  // --------------------------------------------------------------
  //  Continuations plugin + library
  // --------------------------------------------------------------
  lazy val continuationsPluginSettings = compilerDependentProjectSettings ++ Seq(
    scalaSource in Compile <<= baseDirectory(_ / "src/continuations/plugin/"),
    resourceDirectory in Compile <<= baseDirectory(_ / "src/continuations/plugin/"),
    exportJars := true
  )
  lazy val continuationsPlugin = Project("continuations-plugin", file(".")) settings(continuationsPluginSettings:_*)
  lazy val continuationsLibrarySettings = dependentProjectSettings ++ Seq(
    scalaSource in Compile <<= baseDirectory(_ / "src/continuations/library/"),
    scalacOptions in Compile <++= (exportedProducts in Compile in continuationsPlugin) map { 
     case Seq(cpDir) => Seq("-Xplugin-require:continuations", "-P:continuations:enable", "-Xplugin:"+cpDir.data.getAbsolutePath)
    }
  )
  lazy val continuationsLibrary = Project("continuations-library", file(".")) settings(continuationsLibrarySettings:_*)
}
