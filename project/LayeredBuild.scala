import sbt._
import Keys._


object LayeredBuild {
  import Build._

  def compileInputsTask =
		(dependencyClasspath, sources, compilers, classDirectory, compileIncSetup, streams) map {
		(cp, srcs, cs, classes, incSetup, s) =>
			val classpath = classes +: data(cp)
      // TODO - Ask mark to use default parameters so named arguments work here..
			Compiler.inputs(classpath,                               // Classpath
                      srcs,                                    // Sources
                      classes,                                 // Output Directory
                      Seq[String]("-optimise"),                // Scalac Options
                      Seq[String](),                           // Javac Options
                      100,                                     // Max Errors
                      CompileOrder.Mixed)(                     // Compilation Order
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
  val layerProjectName = SettingKey[String]("layer-project-name", "The name of the project for layered building.")
  def paths(layer : String): Seq[Setting[_]] = Seq(
		baseDirectory <<= thisProject(_.base),
		target <<= baseDirectory / ("target/" + layer),
		defaultExcludes in GlobalScope :== (".*"  - ".") || HiddenFileFilter,
		historyPath <<= target(t => Some(t / ".history")),
    // TODO - move this to src/library src/compiler etc.
		sourceDirectory <<= (baseDirectory, layerProjectName) { _ / "src" / _ },
		sourceFilter in GlobalScope :== ("*.java" | "*.scala"),
		cacheDirectory <<= target / "cache",
    classDirectory <<= (target, name){(t,n) => t / n / "classes"}
	)
  def layeredProjectSettings(layer : String): Seq[Setting[_]] = paths(layer) ++ Seq[Setting[_]](
    compileInputs <<= compileInputsTask,
    compileIncSetup <<= compileIncSetupTask,
    unmanagedSourceDirectories <<= Seq(sourceDirectory).join,
    sources <<= Defaults.collectFiles(unmanagedSourceDirectories, sourceFilter, defaultExcludes in GlobalScope),
    compilersSetting,
    classpathOptions in GlobalScope :== ClasspathOptions.boot,
    compile <<= compileTask
  )
}