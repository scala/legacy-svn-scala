/* NSC -- new Scala compiler
 * Copyright 2005-2011 LAMP/EPFL
 * @author  Martin Odersky
 */

package scala.tools.nsc
package interpreter

import Predef.{ println => _, _ }
import util.{ Set => _, _ }
import scala.collection.{ mutable, immutable }
import scala.sys.BooleanProp
import Exceptional.unwrap
import ScalaClassLoader.URLClassLoader
import symtab.Flags
import io.VirtualDirectory
import scala.tools.nsc.io.AbstractFile
import reporters._
import symtab.Flags
import scala.reflect.internal.Names
import scala.tools.util.PathResolver
import scala.tools.nsc.util.{ ScalaClassLoader, Exceptional }
import ScalaClassLoader.URLClassLoader
import Exceptional.unwrap
import scala.collection.{ mutable, immutable }
import scala.PartialFunction.{ cond, condOpt }
import scala.util.control.Exception.{ ultimately }
import scala.reflect.NameTransformer
import IMain._

/** An interpreter for Scala code.
 *  
 *  The main public entry points are compile(), interpret(), and bind().
 *  The compile() method loads a complete Scala file.  The interpret() method
 *  executes one line of Scala code at the request of the user.  The bind()
 *  method binds an object to a variable that can then be used by later
 *  interpreted code.
 *  
 *  The overall approach is based on compiling the requested code and then
 *  using a Java classloader and Java reflection to run the code
 *  and access its results.
 *  
 *  In more detail, a single compiler instance is used
 *  to accumulate all successfully compiled or interpreted Scala code.  To
 *  "interpret" a line of code, the compiler generates a fresh object that
 *  includes the line of code and which has public member(s) to export
 *  all variables defined by that code.  To extract the result of an
 *  interpreted line to show the user, a second "result object" is created
 *  which imports the variables exported by the above object and then
 *  exports members called "$eval" and "$print". To accomodate user expressions
 *  that read from variables or methods defined in previous statements, "import"
 *  statements are used.
 *  
 *  This interpreter shares the strengths and weaknesses of using the
 *  full compiler-to-Java.  The main strength is that interpreted code
 *  behaves exactly as does compiled code, including running at full speed.
 *  The main weakness is that redefining classes and methods is not handled
 *  properly, because rebinding at the Java level is technically difficult.
 *
 *  @author Moez A. Abdel-Gawad
 *  @author Lex Spoon
 */
class IMain(val settings: Settings, protected val out: JPrintWriter) extends Imports {
  imain =>
  
  /** construct an interpreter that reports to Console */
  def this(settings: Settings) = this(settings, new NewLinePrintWriter(new ConsoleWriter, true))
  def this() = this(new Settings())

  lazy val repllog: Logger = new Logger {
    val out: JPrintWriter = imain.out
    val isInfo: Boolean  = BooleanProp keyExists "scala.repl.info"
    val isDebug: Boolean = BooleanProp keyExists "scala.repl.debug"
    val isTrace: Boolean = BooleanProp keyExists "scala.repl.trace"
  }
  lazy val formatting: Formatting = new Formatting {
    val prompt = Properties.shellPromptString
  }
  lazy val reporter: ConsoleReporter = new ReplReporter(this)

  import formatting._
  import reporter.{ printMessage, withoutTruncating }

  private[nsc] var printResults: Boolean = true   // whether to print result lines
  private[nsc] var totalSilence: Boolean = false  // whether to print anything
  
  /** directory to save .class files to */
  val virtualDirectory = new VirtualDirectory("(memory)", None) {
    private def pp(root: io.AbstractFile, indentLevel: Int) {
      val spaces = "    " * indentLevel
      out.println(spaces + root.name)
      if (root.isDirectory)
        root.toList sortBy (_.name) foreach (x => pp(x, indentLevel + 1))
    }
    // print the contents hierarchically
    def show() = pp(this, 0)
  }

  // This exists mostly because using the reporter too early leads to deadlock.
  private def echo(msg: String) { Console println msg }

  /** We're going to go to some trouble to initialize the compiler asynchronously.
   *  It's critical that nothing call into it until it's been initialized or we will
   *  run into unrecoverable issues, but the perceived repl startup time goes
   *  through the roof if we wait for it.  So we initialize it with a future and
   *  use a lazy val to ensure that any attempt to use the compiler object waits
   *  on the future.
   */
  private val _compiler: Global = newCompiler(settings, reporter)
  private var _initializeComplete = false
  private def _initSources = List(new BatchSourceFile("<init>", "class $repl_$init { }"))
  private def _initialize() = {
    try {
      new _compiler.Run() compileSources _initSources
      _initializeComplete = true
      true
    }
    catch AbstractOrMissingHandler()
  }
  
  // set up initialization future
  private var _isInitialized: () => Boolean = null
  // argument is a thunk to execute after init is done
  def initialize(postInitSignal: => Unit): Unit = synchronized {
    if (_isInitialized == null)
      _isInitialized = scala.concurrent.ops future {
        val result = _initialize()
        postInitSignal
        result
      }
  }
  def initializeSynchronous(): Unit = {
    if (!isInitializeComplete) {
      _initialize()
      assert(global != null, global)
    }
  }
  def isInitializeComplete = _initializeComplete

  /** the public, go through the future compiler */
  lazy val global: Global = {
    if (isInitializeComplete) _compiler
    else {
      // If init hasn't been called yet you're on your own.
      if (_isInitialized == null) {
        repldbg("Warning: compiler accessed before init set up.  Assuming no postInit code.")
        initialize(())
      }
      // blocks until it is ; false means catastrophic failure
      if (_isInitialized()) _compiler
      else null
    }
  }
  @deprecated("Use `global` for access to the compiler instance.", "2.9.0")
  lazy val compiler: global.type = global

  import global._
  import definitions.{ ScalaPackage, JavaLangPackage, PredefModule, RootClass }

  private implicit def privateTreeOps(t: Tree): List[Tree] = {
    (new Traversable[Tree] {
      def foreach[U](f: Tree => U): Unit = t foreach { x => f(x) ; () }
    }).toList
  }
  
  // TODO: If we try to make naming a lazy val, we run into big time
  // scalac unhappiness with what look like cycles.  It has not been easy to
  // reduce, but name resolution clearly takes different paths.
  object naming extends {
    val global: imain.global.type = imain.global    
  } with Naming {
    // make sure we don't overwrite their unwisely named res3 etc.
    override def freshUserVarName(): String = {
      val name = super.freshUserVarName()
      if (definedNameMap contains name) freshUserVarName()
      else name
    }
    def isInternalVarName(name: Name): Boolean = isInternalVarName("" + name)
  }
  import naming._

  // object dossiers extends {
  //   val intp: imain.type = imain
  // } with Dossiers { }
  // import dossiers._
  
  lazy val memberHandlers = new {
    val intp: imain.type = imain
  } with MemberHandlers
  import memberHandlers._
  
  def atPickler[T](op: => T): T = atPhase(currentRun.picklerPhase)(op)
  def afterTyper[T](op: => T): T = atPhase(currentRun.typerPhase.next)(op)

  /** Temporarily be quiet */
  def beQuietDuring[T](body: => T): T = {
    val saved = printResults
    printResults = false
    try body
    finally printResults = saved
  }
  def beSilentDuring[T](operation: => T): T = {
    val saved = totalSilence
    totalSilence = true
    try operation
    finally totalSilence = saved
  }
  
  def quietRun[T](code: String) = beQuietDuring(interpret(code))
  
  private def logAndDiscard[T](label: String, alt: => T): PartialFunction[Throwable, T] = {
    case t => repldbg(label + ": " + t) ; alt
  }

  /** whether to bind the lastException variable */
  private var bindExceptions = true
  /** takes AnyRef because it may be binding a Throwable or an Exceptional */
  private def withLastExceptionLock[T](body: => T): T = {
    assert(bindExceptions, "withLastExceptionLock called incorrectly.")
    bindExceptions = false

    try     beQuietDuring(body)
    catch   logAndDiscard("bindLastException", null.asInstanceOf[T])
    finally bindExceptions = true
  }
  
  /** A string representing code to be wrapped around all lines. */
  private var _executionWrapper: String = ""
  def executionWrapper = _executionWrapper
  def setExecutionWrapper(code: String) = _executionWrapper = code
  def clearExecutionWrapper() = _executionWrapper = ""
  
  lazy val lineManager = createLineManager()

  /** interpreter settings */
  lazy val isettings = new ISettings(this)

  /** Create a line manager.  Overridable.  */
  protected def createLineManager(): Line.Manager = new Line.Manager

  /** Instantiate a compiler.  Overridable. */
  protected def newCompiler(settings: Settings, reporter: Reporter) = {
    settings.outputDirs setSingleOutput virtualDirectory
    settings.exposeEmptyPackage.value = true

    new Global(settings, reporter)
  }
  
  /** Parent classloader.  Overridable. */
  protected def parentClassLoader: ClassLoader =
    settings.explicitParentLoader.getOrElse( this.getClass.getClassLoader() )
  
  /** the compiler's classpath, as URL's */
  lazy val compilerClasspath = global.classPath.asURLs

  /* A single class loader is used for all commands interpreted by this Interpreter.
     It would also be possible to create a new class loader for each command
     to interpret.  The advantages of the current approach are:

       - Expressions are only evaluated one time.  This is especially
         significant for I/O, e.g. "val x = Console.readLine"

     The main disadvantage is:

       - Objects, classes, and methods cannot be rebound.  Instead, definitions
         shadow the old ones, and old code objects refer to the old
         definitions.
  */
  private var _classLoader: AbstractFileClassLoader = null
  def resetClassLoader() = _classLoader = makeClassLoader()
  def classLoader: AbstractFileClassLoader = {
    if (_classLoader == null)
      resetClassLoader()
    
    _classLoader
  }
  private def makeClassLoader(): AbstractFileClassLoader = {
    val parent =
      if (parentClassLoader == null)  ScalaClassLoader fromURLs compilerClasspath
      else                            new URLClassLoader(compilerClasspath, parentClassLoader)

    new AbstractFileClassLoader(virtualDirectory, parent) {
      /** Overridden here to try translating a simple name to the generated
       *  class name if the original attempt fails.  This method is used by
       *  getResourceAsStream as well as findClass.
       */
      override protected def findAbstractFile(name: String): AbstractFile = {
        super.findAbstractFile(name) match {
          // deadlocks on startup if we try to translate names too early
          case null if isInitializeComplete => generatedName(name) map (x => super.findAbstractFile(x)) orNull
          case file                         => file
        }
      }
    }
  }

  def getInterpreterClassLoader() = classLoader

  // Set the current Java "context" class loader to this interpreter's class loader
  def setContextClassLoader() = classLoader.setAsContext()

  /** Given a simple repl-defined name, returns the real name of
   *  the class representing it, e.g. for "Bippy" it may return
   *
   *    $line19.$read$$iw$$iw$$iw$$iw$$iw$$iw$$iw$$iw$Bippy
   */
  def generatedName(simpleName: String): Option[String] = {
    if (simpleName endsWith "$") optFlatName(simpleName.init) map (_ + "$")
    else optFlatName(simpleName)
  }
  def flatName(id: String)    = optFlatName(id) getOrElse id
  def optFlatName(id: String) = requestForIdent(id) map (_ fullFlatName id)

  def allDefinedNames = definedNameMap.keys.toList sortBy (_.toString)
  def pathToType(id: String): String = pathToName(newTypeName(id))
  def pathToTerm(id: String): String = pathToName(newTermName(id))
  def pathToName(name: Name): String = {
    if (definedNameMap contains name)
      definedNameMap(name) fullPath name
    else name.toString
  }

  /** Most recent tree handled which wasn't wholly synthetic. */
  private def mostRecentlyHandledTree: Option[Tree] = {
    prevRequests.reverse foreach { req =>
      req.handlers.reverse foreach {
        case x: MemberDefHandler if x.definesValue && !isInternalVarName(x.name)  => return Some(x.member)
        case _ => ()
      }
    }
    None
  }
  
  /** Stubs for work in progress. */
  def handleTypeRedefinition(name: TypeName, old: Request, req: Request) = {
    for (t1 <- old.simpleNameOfType(name) ; t2 <- req.simpleNameOfType(name)) {
      repldbg("Redefining type '%s'\n  %s -> %s".format(name, t1, t2))
    }
  }

  def handleTermRedefinition(name: TermName, old: Request, req: Request) = {
    for (t1 <- old.compilerTypeOf get name ; t2 <- req.compilerTypeOf get name) {
      // Printing the types here has a tendency to cause assertion errors, like
      //   assertion failed: fatal: <refinement> has owner value x, but a class owner is required
      // so DBG is by-name now to keep it in the family.  (It also traps the assertion error,
      // but we don't want to unnecessarily risk hosing the compiler's internal state.)
      repldbg("Redefining term '%s'\n  %s -> %s".format(name, t1, t2))
    }
  }
  def recordRequest(req: Request) {
    if (req == null || referencedNameMap == null)
      return

    prevRequests += req
    req.referencedNames foreach (x => referencedNameMap(x) = req)
    
    // warning about serially defining companions.  It'd be easy
    // enough to just redefine them together but that may not always
    // be what people want so I'm waiting until I can do it better.
    for {
      name   <- req.definedNames filterNot (x => req.definedNames contains x.companionName)
      oldReq <- definedNameMap get name.companionName
      newSym <- req.definedSymbols get name
      oldSym <- oldReq.definedSymbols get name.companionName
    } {
      replwarn("warning: previously defined %s is not a companion to %s.".format(
        stripString("" + oldSym), stripString("" + newSym)))
      replwarn("Companions must be defined together; you may wish to use :paste mode for this.")
    }
    
    // Updating the defined name map
    req.definedNames foreach { name =>
      if (definedNameMap contains name) {
        if (name.isTypeName) handleTypeRedefinition(name.toTypeName, definedNameMap(name), req)
        else handleTermRedefinition(name.toTermName, definedNameMap(name), req)
      }
      definedNameMap(name) = req
    }
  }
  
  private[nsc] def replwarn(msg: => String): Unit =
    if (!settings.nowarnings.value)
      printMessage(msg)
  
  def isParseable(line: String): Boolean = {
    beSilentDuring {
      try parse(line) match {
        case Some(xs) => xs.nonEmpty  // parses as-is
        case None     => true         // incomplete
      }
      catch { case x: Exception =>    // crashed the compiler
        replwarn("Exception in isParseable(\"" + line + "\"): " + x)
        false
      }
    }
  }

  /** Compile an nsc SourceFile.  Returns true if there are
   *  no compilation errors, or false otherwise.
   */
  def compileSources(sources: SourceFile*): Boolean = {
    reporter.reset()
    new Run() compileSources sources.toList
    !reporter.hasErrors
  }

  /** Compile a string.  Returns true if there are no
   *  compilation errors, or false otherwise.
   */
  def compileString(code: String): Boolean =
    compileSources(new BatchSourceFile("<script>", code))

  /** Build a request from the user. `trees` is `line` after being parsed.
   */
  private def buildRequest(line: String, trees: List[Tree]): Request = new Request(line, trees)
  
  // rewriting "5 // foo" to "val x = { 5 // foo }" creates broken code because
  // the close brace is commented out.  Strip single-line comments.
  // ... but for error message output reasons this is not used, and rather than
  // enclosing in braces it is constructed like "val x =\n5 // foo".
  private def removeComments(line: String): String = {
    showCodeIfDebugging(line) // as we're about to lose our // show
    line.lines map (s => s indexOf "//" match {
      case -1   => s
      case idx  => s take idx
    }) mkString "\n"
  }
  private def safePos(t: Tree, alt: Int): Int =
    try t.pos.startOrPoint
    catch { case _: UnsupportedOperationException => alt }
    
  // Given an expression like 10 * 10 * 10 we receive the parent tree positioned
  // at a '*'.  So look at each subtree and find the earliest of all positions.
  private def earliestPosition(tree: Tree): Int = {
    var pos = Int.MaxValue
    tree foreach { t =>
      pos = math.min(pos, safePos(t, Int.MaxValue))
    }
    pos
  }

  private def requestFromLine(line: String, synthetic: Boolean): Either[IR.Result, Request] = {
    val content = indentCode(line)
    val trees = parse(content) match {
      case None         => return Left(IR.Incomplete)
      case Some(Nil)    => return Left(IR.Error) // parse error or empty input
      case Some(trees)  => trees
    }
    repltrace(
      trees map { t => 
        t map { t0 => t0.getClass + " at " + safePos(t0, -1) + "\n" }
      } mkString
    )
    // If the last tree is a bare expression, pinpoint where it begins using the
    // AST node position and snap the line off there.  Rewrite the code embodied
    // by the last tree as a ValDef instead, so we can access the value.
    trees.last match {
      case _:Assign                        => // we don't want to include assignments
      case _:TermTree | _:Ident | _:Select => // ... but do want other unnamed terms.
        val varName  = if (synthetic) freshInternalVarName() else freshUserVarName()
        val rewrittenLine = (
          // In theory this would come out the same without the 1-specific test, but
          // it's a cushion against any more sneaky parse-tree position vs. code mismatches:
          // this way such issues will only arise on multiple-statement repl input lines,
          // which most people don't use.
          if (trees.size == 1) "val " + varName + " =\n" + content
          else {
            // The position of the last tree
            val lastpos0 = earliestPosition(trees.last)
            // Oh boy, the parser throws away parens so "(2+2)" is mispositioned.
            // So until we can fix the parser we'll have to go trawling.
            val adjustment = ((content take lastpos0).reverse takeWhile { ch =>
              ch.isWhitespace || ch == '(' || ch == ')'
            }).length
            val lastpos = lastpos0 - adjustment

            // the source code split at the laboriously determined position.
            val (l1, l2) = content splitAt lastpos
            val prefix   = if (l1.trim == "") "" else l1 + ";\n"
            // Note to self: val source needs to have this precise structure so that
            // error messages print the user-submitted part without the "val res0 = " part.
            val combined   = prefix + "val " + varName + " =\n" + l2

            repldbg(List(
              "    line" -> line,
              " content" -> content,
              "     was" -> l2,
              "combined" -> combined) map {
                case (label, s) => label + ": '" + s + "'"
              } mkString "\n"
            )
            combined
          }
        )
        // Rewriting    "foo ; bar ; 123"
        // to           "foo ; bar ; val resXX = 123"
        requestFromLine(rewrittenLine, synthetic) match {
          case Right(req) => return Right(req withOriginalLine line)
          case x          => return x
        }
      case _ => 
    }
    Right(buildRequest(line, trees))
  }
  
  def typeCleanser(sym: Symbol, memberName: Name): Type = {
    // the types are all =>T; remove the =>
    val tp1 = afterTyper(sym.info.nonPrivateDecl(memberName).tpe match {
      case NullaryMethodType(tp) => tp
      case tp                    => tp
    })
    // normalize non-public types so we don't see protected aliases like Self
    afterTyper(tp1 match {
      case TypeRef(_, sym, _) if !sym.isPublic  => tp1.normalize
      case tp                                   => tp
    })
  }

  /**
   *  Interpret one line of input. All feedback, including parse errors
   *  and evaluation results, are printed via the supplied compiler's 
   *  reporter. Values defined are available for future interpreted strings.
   *  
   *  The return value is whether the line was interpreter successfully,
   *  e.g. that there were no parse errors.
   */
  def interpret(line: String): IR.Result = interpret(line, false)
  def interpret(line: String, synthetic: Boolean): IR.Result = {
    def loadAndRunReq(req: Request) = {
      val (result, succeeded) = req.loadAndRun
      /** To our displeasure, ConsoleReporter offers only printMessage,
       *  which tacks a newline on the end.  Since that breaks all the
       *  output checking, we have to take one off to balance.
       */
      def show() = {
        if (result == "") ()
        else printMessage(result stripSuffix "\n")
      }

      if (succeeded) {
        if (printResults)
          show()
        else if (isReplDebug) // show quiet-mode activity
          printMessage(result.trim.lines map ("[quiet] " + _) mkString "\n")
        
        // Book-keeping.  Have to record synthetic requests too,
        // as they may have been issued for information, e.g. :type
        recordRequest(req)
        IR.Success
      }
      else {
        // don't truncate stack traces
        withoutTruncating(show())
        IR.Error
      }
    }
    
    if (global == null) IR.Error
    else requestFromLine(line, synthetic) match {
      case Left(result) => result
      case Right(req)   => 
        // null indicates a disallowed statement type; otherwise compile and
        // fail if false (implying e.g. a type error)
        if (req == null || !req.compile) IR.Error
        else loadAndRunReq(req)
    }
  }

  /** Bind a specified name to a specified value.  The name may
   *  later be used by expressions passed to interpret.
   *
   *  @param name      the variable name to bind
   *  @param boundType the type of the variable, as a string
   *  @param value     the object value to bind to it
   *  @return          an indication of whether the binding succeeded
   */
  def bind(name: String, boundType: String, value: Any): IR.Result = {
    val bindRep = new ReadEvalPrint()
    val run = bindRep.compile("""
        |object %s {
        |  var value: %s = _
        |  def set(x: Any) = value = x.asInstanceOf[%s]
        |}
      """.stripMargin.format(bindRep.evalName, boundType, boundType)
      )
    bindRep.callOpt("set", value) match {
      case Some(_)  => interpret("val %s = %s.value".format(name, bindRep.evalPath))
      case _        => repldbg("Set failed in bind(%s, %s, %s)".format(name, boundType, value)) ; IR.Error
    }
  }
  def rebind(p: NamedParam): IR.Result = {
    val name     = p.name
    val oldType  = typeOfTerm(name) getOrElse { return IR.Error }
    val newType  = p.tpe
    val tempName = freshInternalVarName()

    quietRun("val %s = %s".format(tempName, name))
    quietRun("val %s = %s.asInstanceOf[%s]".format(name, tempName, newType))
  }
  def quietImport(ids: String*): IR.Result = beQuietDuring(addImports(ids: _*))
  def addImports(ids: String*): IR.Result = 
    if (ids.isEmpty) IR.Success
    else interpret("import " + ids.mkString(", "))

  def quietBind(p: NamedParam): IR.Result                  = beQuietDuring(bind(p))
  def bind(p: NamedParam): IR.Result                       = bind(p.name, p.tpe, p.value)
  def bind[T: Manifest](name: String, value: T): IR.Result = bind((name, value))
  def bindValue(x: Any): IR.Result                         = bindValue(freshUserVarName(), x)
  def bindValue(name: String, x: Any): IR.Result           = bind(name, TypeStrings.fromValue(x), x)

  /** Reset this interpreter, forgetting all user-specified requests. */
  def reset() {
    virtualDirectory.clear()
    resetClassLoader()
    resetAllCreators()
    prevRequests.clear()
  }

  /** This instance is no longer needed, so release any resources
   *  it is using.  The reporter's output gets flushed.
   */
  def close() {
    reporter.flush()
  }
  
  /** Here is where we:
   * 
   *  1) Read some source code, and put it in the "read" object.
   *  2) Evaluate the read object, and put the result in the "eval" object.
   *  3) Create a String for human consumption, and put it in the "print" object.
   *
   *  Read! Eval! Print! Some of that not yet centralized here.
   */
  class ReadEvalPrint(lineId: Int) {
    def this() = this(freshLineId())

    val packageName = sessionNames.line + lineId
    val readName    = sessionNames.read
    val evalName    = sessionNames.eval
    val printName   = sessionNames.print
    
    class LineExceptional(ex: Throwable) extends Exceptional(ex) {
      private def showReplInternal = isettings.showInternalStackTraces

      override def spanFn(frame: JavaStackFrame) =
        if (showReplInternal) super.spanFn(frame)
        else !(frame.className startsWith evalPath)

      override def contextPrelude = super.contextPrelude + (
        if (showReplInternal) ""
        else "/* The repl internal portion of the stack trace is elided. */\n"
      )
    }
    def bindError(t: Throwable) = {
      if (!bindExceptions) // avoid looping if already binding
        throw t
      
      val unwrapped = unwrap(t)
      withLastExceptionLock {
        if (opt.richExes) {
          val ex = new LineExceptional(unwrapped)
          bind[Exceptional]("lastException", ex)
          ex.contextHead + "\n(access lastException for the full trace)"
        }
        else {
          bind[Throwable]("lastException", unwrapped)
          util.stackTraceString(unwrapped)
        }
      }
    }

    // TODO: split it out into a package object and a regular
    // object and we can do that much less wrapping.
    def packageDecl = "package " + packageName
    
    def pathTo(name: String)   = packageName + "." + name
    def packaged(code: String) = packageDecl + "\n\n" + code

    def readPath  = pathTo(readName)
    def evalPath  = pathTo(evalName)
    def printPath = pathTo(printName)
    
    def call(name: String, args: Any*): AnyRef = 
      evalMethod(name).invoke(evalClass, args.map(_.asInstanceOf[AnyRef]): _*)
    
    def callOpt(name: String, args: Any*): Option[AnyRef] =
      try Some(call(name, args: _*))
      catch { case ex: Exception => bindError(ex) ; None }
    
    private def load(s: String): Class[_] =
      (classLoader tryToInitializeClass s) getOrElse sys.error("Failed to load expected class: '" + s + "'")

    lazy val evalClass = load(evalPath)
    lazy val evalValue = callOpt(evalName)

    def compile(source: String): Boolean = compileAndSaveRun("<console>", source)
    def lineAfterTyper[T](op: => T): T = {
      assert(lastRun != null, "Internal error: trying to use atPhase, but Run is null." + this)
      atPhase(lastRun.typerPhase.next)(op)
    }
    
    /** The innermost object inside the wrapper, found by
      * following accessPath into the outer one.
      */
    def resolvePathToSymbol(accessPath: String): Symbol = {
      val readRoot  = definitions.getModule(readPath)   // the outermost wrapper
      (accessPath split '.').foldLeft(readRoot) { (sym, name) =>
        if (name == "") sym else
        lineAfterTyper(sym.info member newTermName(name))
      }
    }
    private var lastRun: Run = _
    private def evalMethod(name: String) = evalClass.getMethods filter (_.getName == name) match {
      case Array(method) => method
      case xs            => sys.error("Internal error: eval object " + evalClass + ", " + xs.mkString("\n", "\n", ""))
    }
    private def compileAndSaveRun(label: String, code: String) = {
      showCodeIfDebugging(code)
      reporter.reset()
      lastRun = new Run()
      lastRun.compileSources(List(new BatchSourceFile(label, packaged(code))))
      !reporter.hasErrors
    }
  }

  /** One line of code submitted by the user for interpretation */
  // private 
  class Request(val line: String, val trees: List[Tree]) {
    val lineRep     = new ReadEvalPrint()
    import lineRep.lineAfterTyper
    
    private var _originalLine: String = null
    def withOriginalLine(s: String): this.type = { _originalLine = s ; this }
    def originalLine = if (_originalLine == null) line else _originalLine

    /** handlers for each tree in this request */
    val handlers: List[MemberHandler] = trees map (memberHandlers chooseHandler _)

    /** all (public) names defined by these statements */
    val definedNames = handlers flatMap (_.definedNames)

    /** list of names used by this expression */
    val referencedNames: List[Name] = handlers flatMap (_.referencedNames)

    /** def and val names */
    def termNames = handlers flatMap (_.definesTerm)
    def typeNames = handlers flatMap (_.definesType)
    def definedOrImported = handlers flatMap (_.definedOrImported)

    /** Code to import bound names from previous lines - accessPath is code to
      * append to objectName to access anything bound by request.
      */
    val ComputedImports(importsPreamble, importsTrailer, accessPath) =
      importsCode(referencedNames.toSet)

    /** Code to access a variable with the specified name */
    def fullPath(vname: String) = (
      lineRep.readPath + accessPath + ".`%s`".format(vname)
    )
    /** Same as fullpath, but after it has been flattened, so:
     *  $line5.$iw.$iw.$iw.Bippy      // fullPath
     *  $line5.$iw$$iw$$iw$Bippy      // fullFlatName
     */
    def fullFlatName(name: String) =
      lineRep.readPath + accessPath.replace('.', '$') + "$" + name

    /** The unmangled symbol name, but supplemented with line info. */
    def disambiguated(name: Name): String = name + " (in " + lineRep + ")"

    /** Code to access a variable with the specified name */
    def fullPath(vname: Name): String = fullPath(vname.toString)

    /** the line of code to compute */
    def toCompute = line

    /** generate the source code for the object that computes this request */
    private object ObjectSourceCode extends CodeAssembler[MemberHandler] {
      val preamble = """
        |object %s {
        |%s%s
      """.stripMargin.format(lineRep.readName, importsPreamble, indentCode(toCompute))
      val postamble = importsTrailer + "\n}"
      val generate = (m: MemberHandler) => m extraCodeToEvaluate Request.this
    }
    
    private object ResultObjectSourceCode extends CodeAssembler[MemberHandler] {
      /** We only want to generate this code when the result
       *  is a value which can be referred to as-is.
       */      
      val evalResult =
        if (!handlers.last.definesValue) ""
        else handlers.last.definesTerm match {
          case Some(vname) if typeOf contains vname =>
            """
            |lazy val $result = {
            |  %s
            |  %s
            |}""".stripMargin.format(lineRep.printName, fullPath(vname))
          case _  => ""
        }
      // first line evaluates object to make sure constructor is run
      // initial "" so later code can uniformly be: + etc
      val preamble = """
      |object %s {
      |  %s
      |  val %s: String = %s {
      |    %s
      |    (""
      """.stripMargin.format(
        lineRep.evalName, evalResult, lineRep.printName,
        executionWrapper, lineRep.readName + accessPath
      )
      
      val postamble = """
      |    )
      |  }
      |}
      """.stripMargin
      val generate = (m: MemberHandler) => m resultExtractionCode Request.this
    }

    // get it
    def getEvalTyped[T] : Option[T] = getEval map (_.asInstanceOf[T])
    def getEval: Option[AnyRef] = {
      // ensure it has been compiled
      compile
      // try to load it and call the value method      
      lineRep.evalValue filterNot (_ == null)
    }

    /** Compile the object file.  Returns whether the compilation succeeded.
     *  If all goes well, the "types" map is computed. */
    lazy val compile: Boolean = {
      // error counting is wrong, hence interpreter may overlook failure - so we reset
      reporter.reset()

      // compile the object containing the user's code
      lineRep.compile(ObjectSourceCode(handlers)) && {
        // extract and remember types 
        typeOf
        typesOfDefinedTerms

        // compile the result-extraction object
        beSilentDuring {
          lineRep compile ResultObjectSourceCode(handlers)
        }
      }
    }

    lazy val resultSymbol = lineRep.resolvePathToSymbol(accessPath)
    def applyToResultMember[T](name: Name, f: Symbol => T) = lineAfterTyper(f(resultSymbol.info.nonPrivateDecl(name)))

    /* typeOf lookup with encoding */
    def lookupTypeOf(name: Name) = typeOf.getOrElse(name, typeOf(global.encode(name.toString)))
    def simpleNameOfType(name: TypeName) = (compilerTypeOf get name) map (_.typeSymbol.simpleName)
    
    private def typeMap[T](f: Type => T): Map[Name, T] =
      termNames ++ typeNames map (x => x -> f(typeCleanser(resultSymbol, x))) toMap

    /** Types of variables defined by this request. */
    lazy val compilerTypeOf = typeMap[Type](x => x)
    /** String representations of same. */
    lazy val typeOf         = typeMap[String](tp => afterTyper(tp.toString))
    
    // lazy val definedTypes: Map[Name, Type] = {
    //   typeNames map (x => x -> afterTyper(resultSymbol.info.nonPrivateDecl(x).tpe)) toMap
    // }
    lazy val definedSymbols: Map[Name, Symbol] = (
      termNames.map(x => x -> applyToResultMember(x, x => x)) ++
      typeNames.map(x => x -> compilerTypeOf.get(x).map(_.typeSymbol).getOrElse(NoSymbol))
    ).toMap

    lazy val typesOfDefinedTerms: Map[Name, Type] =
      termNames map (x => x -> applyToResultMember(x, _.tpe)) toMap

    /** load and run the code using reflection */
    def loadAndRun: (String, Boolean) = {
      import interpreter.Line._

      try {
        val execution = lineManager.set(originalLine)(lineRep call sessionNames.print)
        execution.await()
        
        execution.state match {
          case Done       => ("" + execution.get(), true)
          case Threw      => (lineRep.bindError(execution.caught()), false)
          case Cancelled  => ("Execution interrupted by signal.\n", false)
          case Running    => ("Execution still running! Seems impossible.", false)
        }
      }
      finally lineManager.clear()
    }

    override def toString = "Request(line=%s, %s trees)".format(line, trees.size)
  }

  /** Returns the name of the most recent interpreter result.
   *  Mostly this exists so you can conveniently invoke methods on
   *  the previous result.
   */
  def mostRecentVar: String =
    if (mostRecentlyHandledTree.isEmpty) ""
    else "" + (mostRecentlyHandledTree.get match {
      case x: ValOrDefDef           => x.name
      case Assign(Ident(name), _)   => name
      case ModuleDef(_, name, _)    => name
      case _                        => naming.mostRecentVar
    })
  
  def requestForName(name: Name): Option[Request] = {
    assert(definedNameMap != null, "definedNameMap is null")
    definedNameMap get name
  }

  def requestForIdent(line: String): Option[Request] =
    requestForName(newTermName(line)) orElse requestForName(newTypeName(line))
  
  def requestHistoryForName(name: Name): List[Request] =
    prevRequests.toList.reverse filter (_.definedNames contains name)
    
  def safeClass(name: String): Option[Symbol] = {
    try Some(definitions.getClass(newTypeName(name)))
    catch { case _: MissingRequirementError => None }
  }
  def safeModule(name: String): Option[Symbol] = {
    try Some(definitions.getModule(newTermName(name)))
    catch { case _: MissingRequirementError => None }
  }

  def definitionForName(name: Name): Option[MemberHandler] =
    requestForName(name) flatMap { req =>
      req.handlers find (_.definedNames contains name)
    }
  
  def valueOfTerm(id: String): Option[AnyRef] =
    requestForIdent(id) flatMap (_.getEval)

  def classOfTerm(id: String): Option[JClass] =
    valueOfTerm(id) map (_.getClass)    

  def typeOfTerm(id: String): Option[Type] = newTermName(id) match {
    case nme.ROOTPKG  => Some(definitions.RootClass.tpe)
    case name         => requestForName(name) flatMap (_.compilerTypeOf get name)
  }
  def symbolOfTerm(id: String): Symbol =
    requestForIdent(id) flatMap (_.definedSymbols get newTermName(id)) getOrElse NoSymbol

  def runtimeClassAndTypeOfTerm(id: String): Option[(JClass, Type)] = {
    for {
      clazz <- classOfTerm(id)
      tpe <- runtimeTypeOfTerm(id)
      nonAnon <- clazz.supers find (!_.isScalaAnonymous)
    } yield {
      (nonAnon, tpe)
    }
  }
  
  def runtimeTypeOfTerm(id: String): Option[Type] = {
    for {
      tpe <- typeOfTerm(id)
      clazz <- classOfTerm(id)
      val staticSym = tpe.typeSymbol
      runtimeSym <- safeClass(clazz.getName)
      if runtimeSym != staticSym
      if runtimeSym isSubClass staticSym
    } yield {
      runtimeSym.info
    }
  }
  
  private object exprTyper extends { val repl: IMain.this.type = imain } with ExprTyper { }
  def parse(line: String): Option[List[Tree]] = exprTyper.parse(line)
  def typeOfExpression(expr: String, silent: Boolean = true): Option[Type] = {
    exprTyper.typeOfExpression(expr, silent)
  }
    
  protected def onlyTerms(xs: List[Name]) = xs collect { case x: TermName => x }
  protected def onlyTypes(xs: List[Name]) = xs collect { case x: TypeName => x }
    
  def definedTerms   = onlyTerms(allDefinedNames) filterNot isInternalVarName
  def definedTypes   = onlyTypes(allDefinedNames)
  def definedSymbols = prevRequests.toSet flatMap ((x: Request) => x.definedSymbols.values)
  
  /** the previous requests this interpreter has processed */
  private lazy val prevRequests      = mutable.ListBuffer[Request]()
  private lazy val referencedNameMap = mutable.Map[Name, Request]()
  private lazy val definedNameMap    = mutable.Map[Name, Request]()
  protected def prevRequestList      = prevRequests.toList
  private def allHandlers            = prevRequestList flatMap (_.handlers)
  def allSeenTypes                   = prevRequestList flatMap (_.typeOf.values.toList) distinct
  def allImplicits                   = allHandlers filter (_.definesImplicit) flatMap (_.definedNames)
  def importHandlers                 = allHandlers collect { case x: ImportHandler => x }
  
  def visibleTermNames: List[Name] = definedTerms ++ importedTerms distinct

  /** Another entry point for tab-completion, ids in scope */
  def unqualifiedIds = visibleTermNames map (_.toString) filterNot (_ contains "$") sorted
  
  /** Parse the ScalaSig to find type aliases */
  def aliasForType(path: String) = ByteCode.aliasForType(path)
  
  def withoutUnwrapping(op: => Unit): Unit = {
    val saved = isettings.unwrapStrings
    isettings.unwrapStrings = false
    try op
    finally isettings.unwrapStrings = saved
  }
  
  def symbolDefString(sym: Symbol) = {
    TypeStrings.quieter(
      afterTyper(sym.defString),
      sym.owner.name + ".this.",
      sym.owner.fullName + "."
    )
  }
  
  def showCodeIfDebugging(code: String) {
    /** Secret bookcase entrance for repl debuggers: end the line
     *  with "// show" and see what's going on.
     */
    if (repllog.isTrace || (code.lines exists (_.trim endsWith "// show"))) {
      echo(code)
      parse(code) foreach (ts => ts foreach (t => withoutUnwrapping(repldbg(asCompactString(t)))))
    }
  }
  // debugging
  def debugging[T](msg: String)(res: T) = {
    repldbg(msg + " " + res)
    res
  }
}

/** Utility methods for the Interpreter. */
object IMain {
  // The two name forms this is catching are the two sides of this assignment:
  //
  // $line3.$read.$iw.$iw.Bippy = 
  //   $line3.$read$$iw$$iw$Bippy@4a6a00ca
  private def removeLineWrapper(s: String) = s.replaceAll("""\$line\d+[./]\$(read|eval|print)[$.]""", "")
  private def removeIWPackages(s: String)  = s.replaceAll("""\$(iw|read|eval|print)[$.]""", "")
  def stripString(s: String)               = removeIWPackages(removeLineWrapper(s))
  
  trait CodeAssembler[T] {
    def preamble: String
    def generate: T => String
    def postamble: String

    def apply(contributors: List[T]): String = stringFromWriter { code =>
      code println preamble
      contributors map generate foreach (code println _)
      code println postamble
    }
  }
  
  trait StrippingWriter {
    def isStripping: Boolean
    def stripImpl(str: String): String
    def strip(str: String): String = if (isStripping) stripImpl(str) else str
  }
  trait TruncatingWriter {
    def maxStringLength: Int
    def isTruncating: Boolean
    def truncate(str: String): String = {
      if (isTruncating && str.length > maxStringLength)
        (str take maxStringLength - 3) + "..."
      else str
    }
  }
  abstract class StrippingTruncatingWriter(out: JPrintWriter)
          extends JPrintWriter(out)
             with StrippingWriter
             with TruncatingWriter {
    self =>
 
    def clean(str: String): String = truncate(strip(str))
    override def write(str: String) = super.write(clean(str))
  }
  class ReplStrippingWriter(intp: IMain) extends StrippingTruncatingWriter(intp.out) {
    import intp._
    def maxStringLength    = isettings.maxPrintString
    def isStripping        = isettings.unwrapStrings
    def isTruncating       = reporter.truncationOK

    def stripImpl(str: String): String = naming.unmangle(str)
  }
}
