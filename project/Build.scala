import sbt._
import Keys._

object ScalaBuild extends Build {
	lazy val projects  = Seq(root, compQuick, libQuick)
	lazy val root      = Project("scala", file(".")) aggregate(compQuick)
	lazy val compQuick = compiler(libQuick, "quick")
	lazy val libQuick  = library("quick") settings (
	  classpathOptions := ClasspathOptions(autoBoot = true, bootLibrary = true, compiler = false, extra = false)
	)

	def compiler(lib: ProjectReference, stage: String): Project =
		Project("compiler-" + stage, file(".")) dependsOn(lib) aggregate(lib) settings(compilerSettings(stage) : _*)

	def library(stage: String): Project =
		Project("library-" + stage, file(".")) settings(librarySettings(stage) : _*)

	def commonSettings(stage: String, component: String): Seq[Setting[_]] = Seq(
		target <<= target(_ / stage / component),
		crossPaths := false,
		src(component),
		classpathOptions := ClasspathOptions.manual,
		sourceDirectory in Compile <<= baseDirectory(_ / "src"),
		version := "2.10.1-SNAPSHOT",
		scalaVersion := "2.9.0"
	)
	def librarySettings(stage: String) = commonSettings(stage, "library") ++ Seq(
		unmanaged(lib => Seq(lib / "fjbg.jar", lib / "jline.jar"))
	)
	def compilerSettings(stage: String) = commonSettings(stage, "compiler") ++ Seq(
		unmanaged(lib => Seq(lib / "fjbg.jar", lib / "jline.jar", lib /"ant" / "ant.jar", lib / "msil.jar")),
		mainClass in (Compile,run) := Some("scala.tools.nsc.MainGenericRunner")
	)

	def src(name: String) =
		scalaSource in Compile <<= sourceDirectory(_ / name)

	def unmanaged(files: File => Seq[File]) =
		unmanagedClasspath in Compile <<= unmanagedBase map { base => files(base) map Attributed.blank }
}
