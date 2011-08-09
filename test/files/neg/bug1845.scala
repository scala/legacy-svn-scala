import scala.util.parsing.combinator.syntactical.TokenParsers
import scala.util.parsing.combinator.lexical.StdLexical
import scala.util.parsing.combinator.token._

class MyTokenParsers extends TokenParsers {
  import lexical._
  type Tokens = StdTokens
  type Elem = lexical.Token
  val lexical = new StdLexical
}
