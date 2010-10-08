/* NEST (New Scala Test)
 * Copyright 2007-2010 LAMP/EPFL
 * @author Philipp Haller
 */

// $Id$

package scala.tools.partest
package nest

import java.io.{File, PrintStream, FileOutputStream, BufferedReader,
                InputStreamReader, StringWriter, PrintWriter}
import java.util.StringTokenizer
import scala.util.Properties.{ setProp }
import scala.tools.nsc.util.ScalaClassLoader
import scala.tools.nsc.io.Directory

import scala.actors.Actor._
import scala.actors.TIMEOUT

trait DirectRunner {

  def fileManager: FileManager
  
  import PartestDefaults.numActors

  if (isPartestDebug)
    scala.actors.Debug.level = 3
  
  if (PartestDefaults.poolSize.isEmpty) {
    scala.actors.Debug.info("actors.corePoolSize not defined")
    setProp("actors.corePoolSize", "16")
  }
  
  def runTestsForFiles(kindFiles: List[File], kind: String): scala.collection.immutable.Map[String, Int] = {    
    val len = kindFiles.length
    val (testsEach, lastFrag) = (len/numActors, len%numActors)
    val last = numActors-1
    val consFM = new ConsoleFileManager
    import consFM.{ latestCompFile, latestLibFile, latestPartestFile }
    val scalacheckURL = PathSettings.scalaCheck.toURL
    val scalaCheckParentClassLoader = ScalaClassLoader.fromURLs(List(scalacheckURL, latestCompFile.toURI.toURL, latestLibFile.toURI.toURL, latestPartestFile.toURI.toURL))
    val workers = for (i <- List.range(0, numActors)) yield {
      val toTest = kindFiles.slice(i*testsEach, (i+1)*testsEach)
      val worker = new Worker(fileManager, scalaCheckParentClassLoader)
      worker.start()
      if (i == last)
        worker ! RunTests(kind, (kindFiles splitAt (last*testsEach))._2)
      else
        worker ! RunTests(kind, toTest)
      worker
    }

    var logsToDelete: List[File] = List()
    var outdirsToDelete: List[File] = List()
    var results = new scala.collection.immutable.HashMap[String, Int]
    workers foreach { w =>
      receiveWithin(3600 * 1000) {
        case Results(res, logs, outdirs) =>
          logsToDelete :::= logs filter (_.toDelete)
          outdirsToDelete :::= outdirs
          results ++= res
        case TIMEOUT =>
          // add at least one failure
          NestUI.verbose("worker timed out; adding failed test")
          results += ("worker timed out; adding failed test" -> 2)
      }
    }
    
    if (isPartestDebug)
      fileManager.showTestTimings()

    if (!isPartestDebug) {
      for (x <- logsToDelete ::: outdirsToDelete) {
        NestUI.verbose("deleting "+x)
        Directory(x).deleteRecursively()
      }
    }
    
    results
  }

}
