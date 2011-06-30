/* NSC -- new Scala compiler
 * Copyright 2005-2011 LAMP/EPFL
 * @author  Martin Odersky
 */

package scala.tools.nsc
package ast

import scala.collection.mutable.ListBuffer
import symtab.Flags._
import symtab.SymbolTable

/** XXX to resolve: TreeGen only assumes global is a SymbolTable, but
 *  TreeDSL at the moment expects a Global.  Can we get by with SymbolTable?
 */
abstract class TreeGen extends reflect.internal.TreeGen {
  val global: Global

  import global._
  import definitions._
  
  def mkCheckInit(tree: Tree): Tree = {
    val tpe =
      if (tree.tpe != null || !tree.hasSymbol) tree.tpe
      else tree.symbol.tpe

    if (!global.phase.erasedTypes && settings.warnSelectNullable.value && 
        tpe <:< NotNullClass.tpe && !tpe.isNotNull)
      mkRuntimeCall(nme.checkInitialized, List(tree))
    else
      tree
  }

  // wrap the given expression in a SoftReference so it can be gc-ed
  def mkSoftRef(expr: Tree): Tree = New(TypeTree(SoftReferenceClass.tpe), List(List(expr)))

  def mkCached(cvar: Symbol, expr: Tree): Tree = {
    val cvarRef = mkUnattributedRef(cvar)
    Block(
      List(
        If(Apply(Select(cvarRef, nme.eq), List(Literal(Constant(null)))),
           Assign(cvarRef, expr),
           EmptyTree)),
      cvarRef
    )
  }

  // Builds a tree of the form "{ lhs = rhs ; lhs  }"
  def mkAssignAndReturn(lhs: Symbol, rhs: Tree): Tree = {
    val lhsRef = mkUnattributedRef(lhs)
    Block(Assign(lhsRef, rhs) :: Nil, lhsRef)
  }

  def mkModuleVarDef(accessor: Symbol) = {
    val mval = (
      accessor.owner.newVariable(accessor.pos.focus, nme.moduleVarName(accessor.name))
      setInfo accessor.tpe.finalResultType
      setFlag (MODULEVAR)
    )
    
    mval.addAnnotation(AnnotationInfo(VolatileAttr.tpe, Nil, Nil))
    if (mval.owner.isClass) {
      mval setFlag (PRIVATE | LOCAL | SYNTHETIC)
      mval.owner.info.decls.enter(mval)
    }
    ValDef(mval)
  }
  
  // def m: T = { if (m$ eq null) m$ = new m$class(...) m$ }
  // where (...) are eventual outer accessors
  def mkCachedModuleAccessDef(accessor: Symbol, mvar: Symbol) =
    DefDef(accessor, mkCached(mvar, newModule(accessor, mvar.tpe)))

  def mkModuleAccessDef(accessor: Symbol, msym: Symbol) =
    DefDef(accessor, Select(This(msym.owner), msym))

  def newModule(accessor: Symbol, tpe: Type) =
    New(TypeTree(tpe), 
        List(for (pt <- tpe.typeSymbol.primaryConstructor.info.paramTypes) 
             yield This(accessor.owner.enclClass)))

  // def m: T;
  def mkModuleAccessDcl(accessor: Symbol) = 
    DefDef(accessor setFlag lateDEFERRED, EmptyTree)

  def mkRuntimeCall(meth: Name, args: List[Tree]): Tree =
    mkRuntimeCall(meth, Nil, args)

  def mkRuntimeCall(meth: Name, targs: List[Type], args: List[Tree]): Tree =
    mkMethodCall(ScalaRunTimeModule, meth, targs, args)
  
  def mkSysErrorCall(message: String): Tree =
    mkMethodCall(Sys_error, List(Literal(message)))

  /** A creator for a call to a scala.reflect.Manifest or ClassManifest factory method.
   * 
   *  @param    full          full or partial manifest (target will be Manifest or ClassManifest)
   *  @param    constructor   name of the factory method (e.g. "classType")
   *  @param    tparg         the type argument
   *  @param    args          value arguments
   *  @return   the tree
   */  
  def mkManifestFactoryCall(full: Boolean, constructor: String, tparg: Type, args: List[Tree]): Tree =
    mkMethodCall(
      if (full) FullManifestModule else PartialManifestModule,
      constructor,
      List(tparg),
      args
    )

  /** Make a synchronized block on 'monitor'. */
  def mkSynchronized(monitor: Tree, body: Tree): Tree =     
    Apply(Select(monitor, Object_synchronized), List(body))

  def wildcardStar(tree: Tree) =
    atPos(tree.pos) { Typed(tree, Ident(tpnme.WILDCARD_STAR)) }

  def paramToArg(vparam: Symbol) = {
    val arg = Ident(vparam)
    if (isRepeatedParamType(vparam.tpe)) wildcardStar(arg)
    else arg
  }

  def paramToArg(vparam: ValDef) = {
    val arg = Ident(vparam.name)
    if (treeInfo.isRepeatedParamType(vparam.tpt)) wildcardStar(arg)
    else arg
  }

  /** Make forwarder to method `target`, passing all parameters in `params` */
  def mkForwarder(target: Tree, vparamss: List[List[Symbol]]) =
    (target /: vparamss)((fn, vparams) => Apply(fn, vparams map paramToArg))

  /** Applies a wrapArray call to an array, making it a WrappedArray.
   *  Don't let a reference type parameter be inferred, in case it's a singleton:
   *  apply the element type directly.
   */
  def mkWrapArray(tree: Tree, elemtp: Type) = {
    val sym = elemtp.typeSymbol
    val meth: Name =
      if (isValueClass(sym)) "wrap"+sym.name+"Array"
      else if ((elemtp <:< AnyRefClass.tpe) && !isPhantomClass(sym)) "wrapRefArray"
      else "genericWrapArray"

    mkMethodCall(
      PredefModule,
      meth,
      if (isValueClass(sym)) Nil else List(elemtp),
      List(tree)
    )
  }
  
  /** Generate a cast for tree Tree representing Array with
   *  elem type elemtp to expected type pt.
   */
  def mkCastArray(tree: Tree, elemtp: Type, pt: Type) =
    if (elemtp.typeSymbol == AnyClass && isValueClass(tree.tpe.typeArgs.head.typeSymbol))
      mkCast(mkRuntimeCall("toObjectArray", List(tree)), pt)
    else
      mkCast(tree, pt)
  
  /** Translate names in Select/Ident nodes to type names.
   */
  def convertToTypeName(tree: Tree): Option[RefTree] = tree match {
    case Select(qual, name) => Some(Select(qual, name.toTypeName))
    case Ident(name)        => Some(Ident(name.toTypeName))
    case _                  => None
  }

  /** Try to convert Select(qual, name) to a SelectFromTypeTree.
   */
  def convertToSelectFromType(qual: Tree, origName: Name) = convertToTypeName(qual) match {
    case Some(qual1)  => SelectFromTypeTree(qual1 setPos qual.pos, origName.toTypeName)
    case _            => EmptyTree
  }

  /** Used in situations where you need to access value of an expression several times
   */
  def evalOnce(expr: Tree, owner: Symbol, unit: CompilationUnit)(within: (() => Tree) => Tree): Tree = {
    var used = false
    if (treeInfo.isPureExpr(expr)) {
      within(() => if (used) expr.duplicate else { used = true; expr })
    } else {
      val temp = owner.newValue(expr.pos.makeTransparent, unit.freshTermName("ev$"))
        .setFlag(SYNTHETIC).setInfo(expr.tpe)
      val containing = within(() => Ident(temp) setPos temp.pos.focus setType expr.tpe)
      ensureNonOverlapping(containing, List(expr))
      Block(List(ValDef(temp, expr)), containing) setPos (containing.pos union expr.pos)
    }
  }

  def evalOnceAll(exprs: List[Tree], owner: Symbol, unit: CompilationUnit)(within: (List[() => Tree]) => Tree): Tree = {
    val vdefs = new ListBuffer[ValDef]
    val exprs1 = new ListBuffer[() => Tree]
    val used = new Array[Boolean](exprs.length)
    var i = 0
    for (expr <- exprs) {
      if (treeInfo.isPureExpr(expr)) {
        exprs1 += {
          val idx = i
          () => if (used(idx)) expr.duplicate else { used(idx) = true; expr }
        }
      } else {
        val temp = owner.newValue(expr.pos.makeTransparent, unit.freshTermName("ev$"))
          .setFlag(SYNTHETIC).setInfo(expr.tpe)
        vdefs += ValDef(temp, expr)
        exprs1 += (() => Ident(temp) setPos temp.pos.focus setType expr.tpe)
      }
      i += 1
    }
    val prefix = vdefs.toList
    val containing = within(exprs1.toList)
    ensureNonOverlapping(containing, exprs)
    if (prefix.isEmpty) containing
    else Block(prefix, containing) setPos (prefix.head.pos union containing.pos)
  }

  /** Return a double-checked locking idiom around the syncBody tree. It guards with `cond` and
   *  synchronizez on `clazz.this`. Additional statements can be included after initialization,
   *  (outside the synchronized block).
   *
   *  The idiom works only if the condition is using a volatile field.
   *  @see http://www.cs.umd.edu/~pugh/java/memoryModel/DoubleCheckedLocking.html
   */
  def mkDoubleCheckedLocking(clazz: Symbol, cond: Tree, syncBody: List[Tree], stats: List[Tree]): Tree =
    mkDoubleCheckedLocking(mkAttributedThis(clazz), cond, syncBody, stats)
  
  def mkDoubleCheckedLocking(attrThis: Tree, cond: Tree, syncBody: List[Tree], stats: List[Tree]): Tree = {
    If(cond,
       Block(
         mkSynchronized(
           attrThis,
           If(cond, Block(syncBody: _*), EmptyTree)) ::
         stats: _*),
       EmptyTree)
  }
}
