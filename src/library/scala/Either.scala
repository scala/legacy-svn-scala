/*                     __                                               *\
**     ________ ___   / /  ___     Scala API                            **
**    / __/ __// _ | / /  / _ |    (c) 2003-2011, LAMP/EPFL             **
**  __\ \/ /__/ __ |/ /__/ __ |    http://scala-lang.org/               **
** /____/\___/_/ |_/____/_/ | |                                         **
**                          |/                                          **
\*                                                                      */



package scala

/** Represents a value of one of two possible types (a disjoint union.)
 *  Instances of Either are either an instance of [[scala.Left]] or [[scala.Right]].
 * 
 *  A common use of Either is as an alternative to [[scala.Option]] for dealing
 *  with possible missing values.  In this usage, [[scala.None]] is replaced
 *  with a [[scala.Left]] which can contain useful information.
 *  [[scala.Right]] takes the place of [[scala.Some]].  Convention dictates
 *  that Left is used for failure and Right is used for success.
 *
 *  For example, you could use ``Either[Exception, Int]`` to safely parse a String:
 * 
 *  {{{
 *  val maybeAnInt = Console.readLine("Please enter your favorite integer: ")
 *  val result: Either[Exception, Int] =
 *    try {
 *      Right(result.toInt)
 *    } catch {
 *      case ex => Left(ex)
 *    }
 *  println(
 *    result.fold(
 *      i => "So your favorite number is " + i + ", eh?",
 *      ex => "That's not a number! It caused " + ex
 *    )
 *  )
 *  }}}
 *
 *  You can also use pattern matching to extract values from `Either`: {{{
 *  result match {
 *    case Left(i) =>
 *      "So your favorite number is " + i + ", eh?"
 *    case Right(i) =>
 *      "That's not a number! It caused " + ex
 *  }
 *  }}}
 *
 *  For use with `for` comprehensions, an `Either` must be projected into
 *  either its Left or Right value.  A projection this way has `map`
 *  defined to produce a modified copy of the original Either if the projection
 *  corresponds to the proper either type: a left projection of a Right returns
 *  the original value unmodified.
 *
 *  {{{
 *  val l: Either[String, Int] = Left("scala")
 *  val r: Either[String, Int] = Right(12)
 *  l.left.map(_.size): Either[Int, Int] // Left(5)
 *  r.left.map(_.size): Either[Int, Int] // Right(12)
 *  l.right.map(_.toDouble): Either[String, Double] // Left("scala")
 *  r.right.map(_.toDouble): Either[String, Double] // Right(12d)
 *  }}}
 *  
 *  Of course, this works with for-comprehensions as well: {{{
 *  for (s <- l.left) yield s.size // Left(5)
 *  }}}
 *  
 *  Projections define `flatMap` similarly, allowing "chaining" in
 *  for-comprehensions with multiple scrutinees: {{{
 *  val l: Either[String, Int] = Left("scala")
 *  val r: Either[String, Int] = Right(12)
 *  l.left.flatMap(_ => r): Either[String, Int]   // Right(12)
 *  l.right.flatMap(_ => r): Either[String, Int]  // Left("scala")
 *  r.left.flatMap(_ => l): Either[String, Int]   // Right(12)
 *  r.right.flatMap(_ => l): Either[String, Int]  // Left("scala")
 *  }}}
 * 
 *  This also works with for-comprehensions: {{{
 *  for (s <- l.left; t <- r.left) yield i // Right(12)
 *  }}}
 *
 *  @author <a href="mailto:research@workingmouse.com">Tony Morris</a>, Workingmouse
 *  @version 1.0, 11/10/2008
 *  @since 2.7
 */
sealed abstract class Either[+A, +B] {
  /**
   * Projects this `Either` as a `Left`.
   * @see [[scala.Either.LeftProjection]]
   */
  def left = Either.LeftProjection(this)

  /**
   * Projects this `Either` as a `Right`.
   * @see [[scala.Either.LeftProjection]]
   */
  def right = Either.RightProjection(this)

  /**
   * Applies `fa` if this is a `Left` or `fb` if this is a `Right`.
   *
   * For example:
   *
   * {{{
   * val result: Either[Exception, Value] = possiblyFailingOperation()
   * log(result.fold(
   *   ex => "Operation failed with " + ex,
   *   v => "Operation produced value: " + v
   * ))
   * }}}
   *
   * @param fa the function to apply if this is a `Left`
   * @param fb the function to apply if this is a `Right`
   * @return the results of applying the function
   */
  def fold[X](fa: A => X, fb: B => X) = this match {
    case Left(a) => fa(a)
    case Right(b) => fb(b)
  }

  /**
   * If this is a `Left`, then return the left value in `Right` or vice versa.
   * 
   * {{{
   * val l: Either[String, Int] = Left("left")
   * l.swap: Either[Int, String] // Right("left")
   * }}}
   */
  def swap = this match {
    case Left(a) => Right(a)
    case Right(b) => Left(b)
  }

  /**
   * Joins an `Either` through `Right`.
   *
   * This method requires that the right side of this Either is itself an
   * Either type - that is, this must be some type like: {{{
   * Either[Either[A, B], C]
   * }}}
   *
   * If this instance is a Right[Either[A, B]] then the contained Either[A,B]
   * will be returned, otherwise this value will be returned directly.
   *
   * {{{
   * Right[Either[Int, String], String](Right(12)).joinRight: Either[Int, String] // Right(12)
   * Right[Either[Int, String], String](Left("scala")).joinRight: Either[Int, String] // Left("scala")
   * Left[Either[Int, String], String]("haskell).joinRight: Either[Int, String] = // Left("haskell")
   * }}}
   *
   * This method, and `joinLeft`, are analogous to `Option#flatten`
   */
  def joinRight[A1 >: A, B1 >: B, C](implicit ev: B1 <:< Either[A1, C]): Either[A1, C] = this match {
    case Left(a)  => Left(a)
    case Right(b) => b
  }

  /**
   * Joins an `Either` through `Left`.
   *
   * This method requires that the left side of this Either is itself an
   * Either type - that is, this must be some type like: {{{
   * Either[A, Either[B, C]]
   * }}}
   *
   * If this instance is a Left[Either[A, B]] then the contained Either[A,B]
   * will be returned, otherwise this value will be returned directly.
   *
   * {{{
   * Left[String, Either[String, Int]](Right("scala")).joinLeft: Either[Int, String] // Right("scala")
   * Left[String, Either[String, Int]](Left(12)).joinLeft: Either[Int, String] // Left(12)
   * Right[String, Either[String, Int]]("haskell).joinLeft: Either[Int, String] = // Left("haskell")
   * }}}
   *
   * This method, and `joinRight`, are analogous to `Option#flatten`
   */
  def joinLeft[A1 >: A, B1 >: B, C](implicit ev: A1 <:< Either[C, B1]): Either[C, B1] = this match {
    case Left(a)  => a
    case Right(b) => Right(b)
  }

  /**
   * Returns `true` if this is a `Left`, `false` otherwise.
   * 
   * {{{
   * Left('a).isLeft // true
   * Right('a).isLeft // false
   * }}}
   */
  def isLeft: Boolean

  /**
   * Returns `true` if this is a `Right`, `false` otherwise.
   * 
   * {{{
   * Left('a).isRight // false
   * Right('a).isRight // true
   * }}}
   */
  def isRight: Boolean
}

/**
 * The left side of the disjoint union, as opposed to the [[scala.Right]] side.
 *
 * @author <a href="mailto:research@workingmouse.com">Tony Morris</a>, Workingmouse
 * @version 1.0, 11/10/2008
 * @see [[scala.Either]]
 */
final case class Left[+A, +B](a: A) extends Either[A, B] { 
  def isLeft = true
  def isRight = false
}

/**
 * The right side of the disjoint union, as opposed to the [[scala.Left]] side.
 *
 * @author <a href="mailto:research@workingmouse.com">Tony Morris</a>, Workingmouse
 * @version 1.0, 11/10/2008 
 * @see [[scala.Either]]
 */ 
final case class Right[+A, +B](b: B) extends Either[A, B] {
  def isLeft = false
  def isRight = true
}

object Either {
  class MergeableEither[A](x: Either[A, A]) {
    def merge: A = x match {
      case Left(a)  => a
      case Right(a) => a
    }
  }

  /**
   * Allows use of a ``merge`` method to extract values from Either instances
   * regardless of whether they are Left or Right.
   *
   * {{{
   * val l = Left(List(1)): Either[List[Int], Vector[Int]]
   * val r = Right(Vector(1)): Either[List[Int], Vector[Int]]
   * l.merge: Seq[Int] // List(1)
   * r.merge: Seq[Int] // Vector(1)
   * }}}
   */
  implicit def either2mergeable[A](x: Either[A, A]): MergeableEither[A] = new MergeableEither(x)

  /**
   * Projects an `Either` into a `Left`.
   *
   * This allows for-comprehensions over Either instances - for example {{{
   * for (s <- Left("scala").left) yield s.length // Left(5)
   * }}}
   *
   * Continuing the analogy with [[scala.Option]], a LeftProjection declares
   * that Left should be analogous to Some in some code.
   *
   * {{{
   * // using Option:
   * def interactWithDB(x: Query): Option[Result] =
   *   try {
   *     Some(getResultFromDatabase(x))
   *   } catch {
   *     case ex => None
   *   }
   *
   * val report = 
   *   for (r <- interactWithDB(someQuery)) yield generateReport(r)
   * if (report.isDefined)
   *   send(report)
   * else
   *   log("report not generated, not sure why...")
   * }}}
   *
   * {{{
  * // using Either
   * def interactWithDB(x: Query): Either[Result, Exception] =
   *   try {
   *     Left(getResultFromDatabase(x))
   *   } catch {
   *     case ex => Right(ex)
   *   }
   *
   * val report = 
   *   for (r <- interactWithDB(someQuery).left) yield generateReport(r)
   * if (report.isLeft)
   *   send(report)
   * else
   *   log("report not generated, reason was " + report.right.get)
   * }}}
   *
   * @author <a href="mailto:research@workingmouse.com">Tony Morris</a>, Workingmouse
   * @version 1.0, 11/10/2008
   */
  final case class LeftProjection[+A, +B](e: Either[A, B]) {
    /**
     * Returns the value from this `Left` or throws `Predef.NoSuchElementException`
     * if this is a `Right`.
     *
     * {{{
     * Left(12).left.get // 12
     * Right(12).left.get // NoSuchElementException
     * }}}
     *
     * @throws Predef.NoSuchElementException if the projection is [[scala.Right]]
     */
    def get = e match {
      case Left(a) => a
      case Right(_) =>  throw new NoSuchElementException("Either.left.value on Right")
    }

    /**
     * Executes the given side-effect if this is a `Left`.
     *
     * {{{
     * Left(12).left.foreach(x => println(x))  // prints "12"
     * Right(12).left.foreach(x => println(x)) // doesn't print
     * }}}
     * @param e The side-effect to execute.
     */
    def foreach[U](f: A => U) = e match {
      case Left(a) => f(a)
      case Right(_) => {}
    }

    /**
     * Returns the value from this `Left` or the given argument if this is a
     * `Right`.
     * 
     * {{{
     * Left(12).left.getOrElse(17)  // 12
     * Right(12).left.getOrElse(17) // 17
     * }}}
     *
     */
    def getOrElse[AA >: A](or: => AA) = e match {
      case Left(a) => a
      case Right(_) => or
    }

    /**
     * Returns `true` if `Right` or returns the result of the application of
     * the given function to the `Left` value.
     * 
     * {{{
     * Left(12).left.forall(_ > 10)  // true
     * Left(7).left.forall(_ > 10)   // false
     * Right(12).left.forall(_ > 10) // true
     * }}}
     *
     */
    def forall(f: A => Boolean) = e match {
      case Left(a) => f(a)
      case Right(_) => true
    }

    /**
     * Returns `false` if `Right` or returns the result of the application of
     * the given function to the `Left` value.
     * 
     * {{{
     * Left(12).left.exists(_ > 10)  // true
     * Left(7).left.exists(_ > 10)   // false
     * Right(12).left.exists(_ > 10) // false
     * }}}
     *
     */
    def exists(f: A => Boolean) = e match {
      case Left(a) => f(a)
      case Right(_) => false
    }

    /**
     * Binds the given function across `Left`.
     *
     * {{{
     * Left(12).left.flatMap(x => Left("scala")) // Left("scala")
     * Right(12).left.flatMap(x => Left("scala") // Right(12)
     * }}}
     * @param The function to bind across `Left`.
     */
    def flatMap[BB >: B, X](f: A => Either[X, BB]) = e match {
      case Left(a) => f(a)
      case Right(b) => Right(b)
    }

    /**
     * Maps the function argument through `Left`.
     *
     * {{{
     * Left(12).left.map(_ + 2) // Left(14)
     * Right[Int, Int](12).left.map(_ + 2) // Right(12)
     * }}}
     */
    def map[X](f: A => X) = e match {
      case Left(a) => Left(f(a))
      case Right(b) => Right(b)
    }

    /**
     * Returns `None` if this is a `Right` or if the given predicate
     * `p` does not hold for the left value, otherwise, returns a `Left`.
     *
     * {{{
     * Left(12).left.filter(_ > 10)  // Some(Left(12))
     * Left(7).left.filter(_ > 10)   // None
     * Right(12).left.filter(_ > 10) // None
     * }}}
     */
    def filter[Y](p: A => Boolean): Option[Either[A, Y]] = e match {
      case Left(a) => if(p(a)) Some(Left(a)) else None
      case Right(b) => None
    }

    /**
     * Returns a `Seq` containing the `Left` value if it exists or an empty
     * `Seq` if this is a `Right`.
     *
     * {{{
     * Left(12).left.toSeq // Seq(12)
     * Right(12).left.toSeq // Seq()
     * }}}
     */
    def toSeq = e match {
      case Left(a) => Seq(a)
      case Right(_) => Seq.empty
    }

    /**
     * Returns a `Some` containing the `Left` value if it exists or a
     * `None` if this is a `Right`.
     * 
     * {{{
     * Left(12).left.toOption // Some(12)
     * Right(12).left.toOption // None
     * }}}
     */
    def toOption = e match {
      case Left(a) => Some(a)
      case Right(_) => None
    }
  }

  /**
   * Projects an `Either` into a `Right`.
   *
   * This allows for-comprehensions over Either instances - for example {{{
   * for (s <- Right("scala").Right) yield s.length // Left(5)
   * }}}
   *
   * Continuing the analogy with [[scala.Option]], a RightProjection declares
   * that Right should be analogous to Some in some code.
   *
   * {{{
   * // using Option:
   * def interactWithDB(x: Query): Option[Result] =
   *   try {
   *     Some(getResultFromDatabase(x))
   *   } catch {
   *     case ex => None
   *   }
   *
   * val report = 
   *   for (r <- interactWithDB(someQuery)) yield generateReport(r)
   * if (report.isDefined)
   *   send(report)
   * else
   *   log("report not generated, not sure why...")
   * }}}
   *
   * {{{
   * // using Either
   * def interactWithDB(x: Query): Either[Exception, Result] =
   *   try {
   *     Right(getResultFromDatabase(x))
   *   } catch {
   *     case ex => Right(ex)
   *   }
   *
   * val report = 
   *   for (r <- interactWithDB(someQuery).right) yield generateReport(r)
   * if (report.isRight)
   *   send(report)
   * else
   *   log("report not generated, reason was " + report.right.get)
   * }}}
   *
   * @author <a href="mailto:research@workingmouse.com">Tony Morris</a>, Workingmouse
   * @version 1.0, 11/10/2008
   */
  final case class RightProjection[+A, +B](e: Either[A, B]) {
    /**
     * Returns the value from this `Right` or throws 
     * `Predef.NoSuchElementException` if this is a `Left`.
     *
     * {{{
     * Right(12).right.get // 12
     * Left(12).right.get // NoSuchElementException
     * }}}
     *
     * @throws Predef.NoSuchElementException if the projection is `Left`.
     */
    def get = e match {
      case Left(_) =>  throw new NoSuchElementException("Either.right.value on Left")
      case Right(a) => a
    }

    /**
     * Executes the given side-effect if this is a `Right`.
     *
     * {{{
     * Right(12).right.foreach(x => println(x)) // prints "12"
     * Left(12).right.foreach(x => println(x))  // doesn't print
     * }}}
     * @param e The side-effect to execute.
     */
    def foreach[U](f: B => U) = e match {
      case Left(_) => {}
      case Right(b) => f(b)
    }

    /**
     * Returns the value from this `Right` or the given argument if this is a
     * `Left`.
     *
     * {{{
     * Right(12).right.getOrElse(17) // 12
     * Left(12).right.getOrElse(17)  // 17
     * }}}
     */
    def getOrElse[BB >: B](or: => BB) = e match {
      case Left(_) => or
      case Right(b) => b
    }

    /**
     * Returns `true` if `Left` or returns the result of the application of
     * the given function to the `Right` value.
     *
     * {{{
     * Right(12).right.forall(_ > 10) // true
     * Right(7).right.forall(_ > 10)  // false
     * Left(12).right.forall(_ > 10)  // true
     * }}}
     */
    def forall(f: B => Boolean) = e match {
      case Left(_) => true
      case Right(b) => f(b)
    }

    /**
     * Returns `false` if `Left` or returns the result of the application of
     * the given function to the `Right` value.
     *
     * {{{
     * Right(12).right.exists(_ > 10)  // true
     * Right(7).right.exists(_ > 10)   // false
     * Left(12).right.exists(_ > 10)   // false
     * }}}
     */
    def exists(f: B => Boolean) = e match {
      case Left(_) => false
      case Right(b) => f(b)
    }

    /**
     * Binds the given function across `Right`.
     *
     * @param The function to bind across `Right`.
     */
    def flatMap[AA >: A, Y](f: B => Either[AA, Y]) = e match {
      case Left(a) => Left(a)
      case Right(b) => f(b)
    }

    /**
     * Maps the function argument through `Right`.
     *
     * {{{
     * Right(12).right.flatMap(x => Right("scala") // Right("scala")
     * Left(12).right.flatMap(x => Left("scala"))  // Left(12)
     * }}}
     */
    def map[Y](f: B => Y) = e match {
      case Left(a) => Left(a)
      case Right(b) => Right(f(b))
    }

    /** Returns `None` if this is a `Left` or if the
     *  given predicate `p` does not hold for the right value,
     *  otherwise, returns a `Right`.
     * 
     * {{{
     * Right(12).right.filter(_ > 10) // Some(Right(12))
     * Right(7).right.filter(_ > 10)  // None
     * Left(12).right.filter(_ > 10)  // None
     * }}}
     */
    def filter[X](p: B => Boolean): Option[Either[X, B]] = e match {
      case Left(_) => None
      case Right(b) => if(p(b)) Some(Right(b)) else None
    }

    /** Returns a `Seq` containing the `Right` value if
     *  it exists or an empty `Seq` if this is a `Left`.
     *
     * {{{
     * Right(12).right.toSeq // Seq(12)
     * Left(12).right.toSeq // Seq()
     * }}}
     */
    def toSeq = e match {
      case Left(_) => Seq.empty
      case Right(b) => Seq(b)
    }

    /** Returns a `Some` containing the `Right` value
     *  if it exists or a `None` if this is a `Left`.
     *
     * {{{
     * Right(12).right.toOption // Some(12)
     * Left(12).right.toOption // None
     * }}}
     */
    def toOption = e match {
      case Left(_) => None
      case Right(b) => Some(b)
    }
  }

  /** If the condition satisfies, return the given `A` in `Left`,
   *  otherwise, return the given `B` in `Right`.
   *
   * {{{
   * val userInput: String = ...
   * Either.cond(
   *   userInput.forall(_.isDigit) && userInput.size == 10,
   *   "The input (%s) does not look like a phone number".format(userInput),
   *   PhoneNumber(userInput)
   * }}}
   */
  def cond[A, B](test: Boolean, right: => B, left: => A): Either[A, B] = 
    if (test) Right(right) else Left(left)
}
