/* NSC -- new scala compiler
 * Copyright 2005-2011 LAMP/EPFL
 * @author  Martin Odersky
 */

package scala.tools.nsc
package symtab

import ast.{Trees, TreePrinters, DocComments}

import util._

abstract class SymbolTable extends reflect.generic.Universe
                              with Names
                              with Symbols
                              with Types
                              with Scopes
                              with Caches
                              with Definitions
                              with reflect.generic.Constants
                              with BaseTypeSeqs
                              with InfoTransformers
                              with StdNames
                              with AnnotationInfos
                              with AnnotationCheckers
                              with Trees
                              with TreePrinters
                              with Positions
                              with DocComments
                              with TypeDebugging
{
  def settings: Settings
  def rootLoader: LazyType
  def log(msg: => AnyRef)
  def abort(msg: String) = throw new Error(msg)
  def abort() = throw new Error()

  /** Are we compiling for Java SE ? */
  def forJVM: Boolean

  /** Are we compiling for .NET ? */
  def forMSIL: Boolean
  
  /** A period is an ordinal number for a phase in a run.
   *  Phases in later runs have higher periods than phases in earlier runs.
   *  Later phases have higher periods than earlier phases in the same run.
   */
  type Period = Int
  final val NoPeriod = 0

  /** An ordinal number for compiler runs. First run has number 1. */
  type RunId = Int
  final val NoRunId = 0

  private var ph: Phase = NoPhase
  private var per = NoPeriod

  final def phase: Phase = ph

  final def phase_=(p: Phase) {
    //System.out.println("setting phase to " + p)
    assert((p ne null) && p != NoPhase)
    ph = p
    per = (currentRunId << 8) + p.id
  }

  /** The current compiler run identifier. */
  def currentRunId: RunId

  /** The run identifier of the given period */
  final def runId(period: Period): RunId = period >> 8

  /** The phase identifier of the given period */
  final def phaseId(period: Period): Phase#Id = period & 0xFF

  /** The period at the start of run that includes `period` */
  final def startRun(period: Period): Period = period & 0xFFFFFF00

  /** The current period */
  final def currentPeriod: Period = {
    //assert(per == (currentRunId << 8) + phase.id)
    per
  }

  /** The phase associated with given period */
  final def phaseOf(period: Period): Phase = phaseWithId(phaseId(period))

  final def period(rid: RunId, pid: Phase#Id): Period = 
    (currentRunId << 8) + pid

  /** Perform given operation at given phase */
  final def atPhase[T](ph: Phase)(op: => T): T = {
    // Eugene: insert same thread assertion here
    val current = phase
    phase = ph
    try op
    finally phase = current
  }
  final def afterPhase[T](ph: Phase)(op: => T): T =
    atPhase(ph.next)(op)
  
  /** Break into repl debugger if assertion is true */
  // def breakIf(assertion: => Boolean, args: Any*): Unit =
  //   if (assertion)
  //     ILoop.break(args.toList)

  /** The set of all installed infotransformers */
  var infoTransformers = new InfoTransformer {
    val pid = NoPhase.id
    val changesBaseClasses = true
    def transform(sym: Symbol, tpe: Type): Type = tpe
  }

  /** The phase which has given index as identifier */
  val phaseWithId: Array[Phase]
}
