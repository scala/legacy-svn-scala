import sbt._
import Keys._


trait LayeredBuild extends Build {
  // All the dependencies to make layered builds..
  def fjbg : Project
  def jline : Project
  // TODO - This probably needs to be defined here as well, since it has Scala source code *and* java source code...
  def msil : Project
  def forkjoin : Project

  import Build._

  def compileInputsTask =
    (dependencyClasspath, sources, compilers, classDirectory, compileIncSetup, streams, sourceDirectory) map {
    (cp, srcs, cs, classes, incSetup, s, sd) =>
			val classpath = classes +: data(cp)
      // TODO - Ask mark to use default parameters so named arguments work here..
      Compiler.inputs(classpath,                                      // Classpath
                      sortFiles(srcs),                                // Sources
                      classes,                                        // Output Directory
                      Seq[String]("-sourcepath", sd.getAbsolutePath), // TODO - Allow Scalac Options
                      Seq[String]("-target", "1.5"),                  // TODO - Allow Javac Options
                      100,                                            // Max Errors
                      CompileOrder.JavaThenScala)(                    // Compilation Order
                      cs, incSetup, s.log)
  }
  // TODO - Figure out WTF to do here...
  def compileIncSetupTask =
		(dependencyClasspath, cacheDirectory, skip in compile, definesClass) map { (cp, cacheDir, skip, definesC) =>
			Compiler.IncSetup(Defaults.analysisMap(cp), definesC, skip, cacheDir / "compile")
		}
  def compileTask = (compileInputs, streams) map { (i,s) => Compiler(i,s.log) }
  def compilersSetting = compilers <<= (scalaInstance, appConfiguration, streams, classpathOptions, javaHome) map {
    (si, app, s, co, jh) => Compiler.compilers(si, co, jh)(app, s.log)
  }
  def sortFiles(files : Seq[File]) = files sortWith { (lhs, rhs) =>
    (lhs.getAbsolutePath, rhs.getAbsolutePath) match {
      case (lhs, rhs) if (lhs endsWith "package.scala") && (rhs endsWith "package.scala") => lhs < rhs
      case (lhs, rhs) if lhs endsWith "package.scala" => true
      case (lhs, rhs) if rhs endsWith "package.scala" => false
      case (lhs, rhs) => lhs < rhs
    }    
  }
  val layerProjectName = SettingKey[String]("layer-project-name", "The name of the project for layered building.")
  def paths(layer : String): Seq[Setting[_]] = Seq(
		baseDirectory <<= thisProject(_.base),
		target <<= (baseDirectory, layerProjectName) apply { (bd, name) => bd / "target" / layer / name },
		defaultExcludes in GlobalScope :== (".*"  - ".") || HiddenFileFilter,
		historyPath <<= target(t => Some(t / ".history")),
    // TODO - move this to src/library src/compiler etc.
		sourceDirectory <<= (baseDirectory, layerProjectName) { _ / "src" / _ },
		sourceFilter in GlobalScope :== ("*.java" | "*.scala"),
		cacheDirectory <<= target / "cache",
                classDirectory <<= target(_ / "classes")
	)
  // Simple tasks, like clean.
  lazy val projectTasks: Seq[Setting[_]] = Seq(
		cleanFiles <<= Seq(target).join,
		cleanKeepFiles <<= historyPath(_.toList),
		clean <<= (cleanFiles, cleanKeepFiles) map Defaults.doClean)

  def layeredProjectSettings(layer : String): Seq[Setting[_]] = paths(layer) ++ projectTasks ++ Seq[Setting[_]](
    compileInputs <<= compileInputsTask,
    compileIncSetup <<= compileIncSetupTask,
    unmanagedSourceDirectories <<= Seq(sourceDirectory).join,
    sources <<= Defaults.collectFiles(unmanagedSourceDirectories, sourceFilter, defaultExcludes in GlobalScope),
    compilersSetting,
    classpathOptions in GlobalScope :== ClasspathOptions.boot,
    compile <<= compileTask
  )

  // Creates a 'layer' of a Scala build using a different instance of Scala.
  def makeLayer(layer: String, scalaLibraryPath : File, scalaCompilerPath : File) : (Project, Project) = {
    val library = Project(layer + "-library", file("."), settings = layeredProjectSettings(layer) ++
      Seq(name := "scala-library",
          layerProjectName := "library",
          version := layer,
          // TODO - use depends on.
          dependencyClasspath <<= (classDirectory in forkjoin in Compile) map { 
            (fj) => Seq(fj) map Attributed.blank },
          scalaInstance <<= appConfiguration map { app =>
            val launcher = app.provider.scalaProvider.launcher
            // TODO - Pass in FJBG
            // TODO - Explicitly version?
            ScalaInstance(
              scalaLibraryPath,
              scalaCompilerPath,
              launcher,
              file("lib/fjbg.jar"))
          }
      )
    ) aggregate(forkjoin)

    // Define the compiler
    val compiler = Project(layer + "-compiler", file("."), settings = layeredProjectSettings(layer) ++
    Seq(name := "scala-compiler",
        layerProjectName := "compiler",
        version := layer,
        // TODO - Use depends on *and* SBT's magic dependency mechanisms...
        dependencyClasspath <<= (classDirectory in forkjoin in Compile,
                                 classDirectory in library in Compile,
                                 classDirectory in fjbg in Compile,
                                 classDirectory in jline in Compile,
                                 classDirectory in msil in Compile) map {
          (fj, lib, fjbg, jline, msil) =>
            Seq(fj, lib, fjbg, jline, msil, file("lib/ant/ant.jar")) map Attributed.blank
        },
        scalaInstance <<= (appConfiguration, classDirectory in library) map { (app, lib) =>
		      val launcher = app.provider.scalaProvider.launcher
          // TODO - explicitly pull FJBG from somewhere...
          ScalaInstance(
            scalaLibraryPath,
            scalaCompilerPath,
            launcher,
            file("lib/fjbg.jar"))
        }
      )
    ) aggregate(fjbg, jline, library, msil)

    // Return the generated projects.
    (library, compiler)
  }
}
