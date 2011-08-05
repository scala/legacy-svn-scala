package scala.tools.partest
package nest

import java.io.File
import scala.tools.nsc.io.{ Directory }


object SBTRunner extends DirectRunner {
  
  val fileManager = new FileManager {
    var JAVACMD: String        = "java"
    var JAVAC_CMD: String      = "javac"
    var CLASSPATH: String      = _
    var LATEST_LIB: String     = _
    val testRootPath: String   = PathSettings.testRoot.path
    val testRootDir: Directory = PathSettings.testRoot
  }
  
  def reflectiveRunTestsForFiles(kindFiles: Array[File], kind: String):java.util.HashMap[String,Int] = {
    def convert(scalaM:scala.collection.immutable.Map[String,Int]):java.util.HashMap[String,Int] = {
      val javaM = new java.util.HashMap[String,Int]()
      for(elem <- scalaM) yield {javaM.put(elem._1,elem._2)}
      javaM
    }

    def failedOnlyIfRequired(files:List[File]):List[File]={
      if (fileManager.failed) files filter (x => fileManager.logFileExists(x, kind)) else files 
    }
    convert(runTestsForFiles(failedOnlyIfRequired(kindFiles.toList), kind))    
  }

  case class CommandLineOptions(classpath: Option[String] = None,
                                runFiles: Option[Array[File]] = None,
                                jvmFiles:Option[Array[File]] = None) 
  
  def reportResults(results: java.util.HashMap[String,Int]):Unit = {
    import collection.JavaConversions._
    val failed: Iterable[String] = results collect {
          case (path, 1)    => path + " [FAILED]"
          case (path, 2)    => path + " [TIMOUT]"
        }
    // TODO - better reporting
    failed foreach System.err.println
  }
  def runAndReportResults(testType: String)(files: Array[File]): Unit = {
    reportResults(reflectiveRunTestsForFiles(files, testType))
  }

  def main(args: Array[String]): Unit = {
    Console.println("This would run partest, but not right now...")
    def parseArgs(args: Seq[String], data: CommandLineOptions): CommandLineOptions = args match {
      case Seq("-cp", cp, rest @ _*) => parseArgs(rest, data.copy(classpath=Some(cp)))
      case Seq("-run", runFiles, rest @ _*) => parseArgs(rest, data.copy(runFiles=Some(runFiles.split(",").map(new File(_)))))
      case Seq() => data
      case x =>        
        sys.error("Unknown command line options: " + x)
    }
    val config = parseArgs(args, CommandLineOptions())
    println("Found config: " + config)
    fileManager.CLASSPATH = config.classpath getOrElse error("No classpath set")
    // Find scala library jar file...
    val lib: Option[String] = (fileManager.CLASSPATH split File.pathSeparator filter (_ matches ".*scala-library.*\\.jar")).headOption
    fileManager.LATEST_LIB = lib getOrElse error("No scala-library found!")
    println("Found lib := " + fileManager.LATEST_LIB)
    // Now run and report...
    config.runFiles foreach runAndReportResults("run")
    config.jvmFiles foreach runAndReportResults("jvm")
  }
}

