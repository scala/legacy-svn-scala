import sbt._

import Build._
import Keys._
import Project.Initialize
import complete._

/** This object */
object partest {

  /** The key for the run-partest task that exists in Scala's test suite. */
  lazy val runPartest = TaskKey[Unit]("run-partest", "Runs the partest test suite against the quick.")
  lazy val runPartestSingle = InputKey[Unit]("run-partest-single", "Runs a single partest test against quick.")
  lazy val partestRunner = TaskKey[PartestRunner]("partest-runner", "Creates a runner that can run partest suites")
  lazy val partestTestRuns = TaskKey[Map[String, Seq[File]]]("partest-tests", "Creates a map of test-type to a sequence of the test files/directoryies to test.")
  lazy val partestTestRunsForCompletion = TaskKey[(Set[String], Set[String])]("partest-tests-completion")

  lazy val partestTaskSettings: Seq[Setting[_]] = Seq(
    partestRunner <<= partestRunnerTask(fullClasspath in Runtime),
    partestTestRuns <<= partestTestRunTaskDefault(baseDirectory),
    runPartest <<= runPartestTask(partestRunner, partestTestRuns, scalacOptions in Test),
    partestTestRunsForCompletion <<= partestTestRuns map convertTestsForAutoComplete,
    runPartestSingle <<= runSingleTestTask(partestRunner, partestTestRuns, scalacOptions in Test)
  )

  // What's fun here is that we want "*.scala" files *and* directories in the base directory...
  def partestResources(base: File, testType: String): PathFinder = testType match {
    case "res" => base ** "*.res"
    case "buildmanager" => base * "*"
    // TODO - Only allow directories that have "*.scala" children...
    case _ => base * "*" filter { f => !f.getName.endsWith(".obj") && (f.isDirectory || f.getName.endsWith(".scala")) }
  }
  lazy val partestTestTypes = Seq("run", "jvm", "pos", "neg", "buildmanager", "res", "shootout", "scalap", "specialized", "presentation")
  // TODO - Figure out how to specify only a subset of resources...
  def partestTestRunTaskDefault(baseDirectory: ScopedSetting[File]): Project.Initialize[Task[Map[String, Seq[File]]]] =
     (baseDirectory) map { dir =>
       partestTestTypes map {
         testType => 
           val testDir = dir / "test"
           testType -> partestResources(testDir / "files" / testType, testType).get           
       } toMap
     }
  // TODO - Split partest task into Configurations and build a Task for each Configuration.
  // *then* mix all of them together for run-testsuite or something clever like this.
  def runPartestTask(runner: ScopedTask[PartestRunner], testRuns: ScopedTask[Map[String,Seq[File]]], scalacOptions: ScopedTask[Seq[String]]): Initialize[Task[Unit]] =
    (runner, testRuns, scalacOptions, streams) map {     
      (runner, runs, scalaOpts, s) =>
        runPartestImpl(runner, runs, scalaOpts, s)
    }
  private def runPartestImpl(runner: PartestRunner, runs: Map[String,Seq[File]], scalacOptions: Seq[String], s: TaskStreams): Unit = {
    val testArgs = runs withFilter (!_._2.isEmpty) flatMap { case (testType, files) =>
          Seq("-"+testType, files.mkString(","))
        }
        val extraArgs = scalacOptions flatMap (opt => Seq("-scalacoption", opt))
        import collection.JavaConverters._
        val results: collection.mutable.Map[String,Int] = runner.run((testArgs ++ extraArgs).toArray).asScala
        // TODO - save results
        val failures = for {
          (path, result) <- results
          if result == 1 || result == 2
          val resultName = (if(result == 1) " [FAILED]" else " [TIMEOUT]")
        } yield path + resultName
        if (!failures.isEmpty) {
          failures.foreach(m => s.log.error(m))
          error("Test Failures! ("+failures.size+" of "+results.size+")")
        } else {
          s.log.info(""+results.size+" tests passed.")
        }
  }

  def convertTestsForAutoComplete(tests: Map[String, Seq[File]]): (Set[String], Set[String]) = {
    (tests.keys.toSet, (for { 
      (testType, files) <- tests.toSet
      file <- files
    } yield cleanFileName(file)).toSet )
  }

  /** Takes a test file, as sent ot Partest, and cleans it up for auto-complete */
  def cleanFileName(file: File): String = {
    // TODO - Something intelligent here
    import scala.util.matching.Regex
    val TestPattern = new Regex(".*/test/(.*)")    
    file.getCanonicalPath match {
       case TestPattern(n) => n
       case _ => file.getName
    }
  }

  def runSingleTestParser: (State, Map[String,Set[String]]) => Parser[(String,String)] = {
    import DefaultParsers._
    (state, tests) =>
      (Space ~> token(NotSpace examples partestTestTypes.toSet)) flatMap { testType =>
        // TODO - Figure out how to use partestTestRuns Key so this works with continuations...
        lazy val files = partestResources(new File(".") / "test" / "files" / testType, testType).get.map(_.getPath).toSet
        (Space ~> token(NotSpace examples tests.get(testType).getOrElse(files)) map { test => (testType, test) })
      }
  }

  def runSingleTestTask(runner: ScopedTask[PartestRunner], tests: ScopedTask[Map[String, Seq[File]]], scalacOptions: ScopedTask[Seq[String]]) : Initialize[InputTask[Unit]] = {
    import sbinary.DefaultProtocol._
    InputTask(TaskData(partestTestRunsForCompletion)(runSingleTestParser)(Map())) { result =>
        (runner, result, scalacOptions, streams) map { (r, test, o, s) => runPartestImpl(r, Map(test._1 -> Seq(new File(test._2))), o, s) }
    }
  }  
  def partestRunnerTask(classpath: ScopedTask[Classpath]): Project.Initialize[Task[PartestRunner]] =
     (classpath) map { (cp) => 
       new PartestRunner(Build.data(cp)) 
     }
}

class PartestRunner(classpath: Seq[File]) {
    // Classloader that does *not* have this as parent, for differing Scala version.
    lazy val classLoader = new java.net.URLClassLoader(classpath.map(_.toURI.toURL).toArray, null)
    lazy val (mainClass, mainMethod) = try {
       val c = classLoader.loadClass("scala.tools.partest.nest.SBTRunner")
       val m = c.getMethod("mainReflect", classOf[Array[String]])
       (c,m)
    }
    lazy val classPathArgs = Seq("-cp", classpath.map(_.getAbsoluteFile).mkString(java.io.File.pathSeparator))
    def run(args: Array[String]): java.util.Map[String,Int] = try {
      val allArgs = (classPathArgs ++ args).toArray
      mainMethod.invoke(null, allArgs).asInstanceOf[java.util.Map[String,Int]]
    } catch {
      case e => 
        //error("Could not run Partest: " + e)
        throw e 
    }
}  
