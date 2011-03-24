/* NSC -- new Scala compiler
 * Copyright 2006-2011 LAMP/EPFL
 * @author  Lex Spoon
 */

package scala.tools.nsc

import java.io.IOException
import java.net.URL
import scala.tools.util.PathResolver

import io.{ File }
import util.{ ClassPath, ScalaClassLoader }
import Properties.{ versionString, copyrightString }
import interpreter.{ ILoop }
import GenericRunnerCommand._

/** An object that runs Scala code.  It has three possible
  * sources for the code to run: pre-compiled code, a script file,
  * or interactive entry.
  */
object MainGenericRunner {
  def errorFn(ex: Throwable): Boolean = {
    ex.printStackTrace()
    false
  }
  def errorFn(str: String): Boolean = {
    Console println str
    false
  }
  
  def main(args: Array[String]) {
    if (!process(args))
      sys.exit(1)
  }

  def process(args: Array[String]): Boolean = {
    val command = new GenericRunnerCommand(args.toList, (x: String) => errorFn(x))
    import command.{ settings, howToRun, thingToRun }
    def sampleCompiler = new Global(settings)   // def so its not created unless needed
    
    if (!command.ok)                      return errorFn("\n" + command.shortUsageMsg)
    else if (settings.version.value)      return errorFn("Scala code runner %s -- %s".format(versionString, copyrightString))
    else if (command.shouldStopWithInfo)  return errorFn(command getInfoMessage sampleCompiler)

    def isE   = !settings.execute.isDefault
    def dashe = settings.execute.value

    def isI   = !settings.loadfiles.isDefault
    def dashi = settings.loadfiles.value
    
    def combinedCode  = {
      val files   = if (isI) dashi map (file => File(file).slurp()) else Nil
      val str     = if (isE) List(dashe) else Nil
      
      files ++ str mkString "\n\n"
    }
    
    def runTarget(): Either[Throwable, Boolean] = howToRun match {
      case AsObject =>
        ObjectRunner.runAndCatch(settings.classpathURLs, thingToRun, command.arguments)
      case AsScript =>
        ScriptRunner.runScriptAndCatch(settings, thingToRun, command.arguments)
      case AsJar    =>
        ObjectRunner.runAndCatch(
          File(thingToRun).toURL +: settings.classpathURLs,
          new io.Jar(thingToRun).mainClass getOrElse sys.error("Cannot find main class for jar: " + thingToRun),
          command.arguments
        )
      case _  =>
        // We start the repl when no arguments are given.
        Right(new ILoop process settings)
    }
    
    /** If -e and -i were both given, we want to execute the -e code after the
     *  -i files have been included, so they are read into strings and prepended to
     *  the code given in -e.  The -i option is documented to only make sense
     *  interactively so this is a pretty reasonable assumption.
     *
     *  This all needs a rewrite though.
     */
    if (isE) {
      ScriptRunner.runCommand(settings, combinedCode, thingToRun +: command.arguments)
    }
    else runTarget() match {
      case Left(ex) => errorFn(ex)
      case Right(b) => b
    }      
  }
}
