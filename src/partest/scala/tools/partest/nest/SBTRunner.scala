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
                                jvmFiles: Option[Array[File]] = None,
                                posFiles: Option[Array[File]] = None,
                                negFiles: Option[Array[File]] = None)

  def main(args: Array[String]): Unit = {
    def parseArgs(args: Seq[String], data: CommandLineOptions): CommandLineOptions = args match {
      case Seq("-cp", cp, rest @ _*) => parseArgs(rest, data.copy(classpath=Some(cp)))
      case Seq("-run", runFiles, rest @ _*) => parseArgs(rest, data.copy(runFiles=Some(runFiles.split(",").map(new File(_)))))
      case Seq("-jvm", runFiles, rest @ _*) => parseArgs(rest, data.copy(jvmFiles=Some(runFiles.split(",").map(new File(_)))))
      case Seq("-pos", runFiles, rest @ _*) => parseArgs(rest, data.copy(posFiles=Some(runFiles.split(",").map(new File(_)))))
      case Seq("-neg", runFiles, rest @ _*) => parseArgs(rest, data.copy(negFiles=Some(runFiles.split(",").map(new File(_)))))
      case Seq() => data
      case x =>        
        sys.error("Unknown command line options: " + x)
    }
    val config = parseArgs(args, CommandLineOptions())
    fileManager.CLASSPATH = config.classpath getOrElse error("No classpath set")
    // Find scala library jar file...
    val lib: Option[String] = (fileManager.CLASSPATH split File.pathSeparator filter (_ matches ".*scala-library.*\\.jar")).headOption
    fileManager.LATEST_LIB = lib getOrElse error("No scala-library found!")
    // Now run and report...
    val runs = Map("run" -> config.runFiles,
                   "jvm" -> config.jvmFiles,
                   "pos" -> config.posFiles,
                   "neg" -> config.posFiles).filter(_._2.isDefined).mapValues(_.get)
    // This next bit uses java maps...
    import collection.JavaConversions._
    val failures = for { 
     (testType, files) <- runs
     (path, result) <- reflectiveRunTestsForFiles(files,testType)
     if result == 1 || result == 2
     val resultName = (if(result == 1) " [FAILED]" else " [TIMEOUT]")
    } yield path + resultName
    
    // Re-list all failures so we can go figure out what went wrong.
    failures foreach System.err.println
    if(!failures.isEmpty) sys.exit(1)
  }
}

