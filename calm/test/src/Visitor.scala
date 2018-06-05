import utest._

/* Created on 02.06.18 */
object Visitor extends TestSuite{
  abstract class Json
  case class Str(value: String) extends Json
  case class Num(value: Int) extends Json
  case class Dict(pairs: (String, Json)*) extends Json

  val a = ???
  val b = ???
  val tree = Dict(
    "hello" -> Dict(
      "i am" -> Dict("cow" -> Num(1)),
      "you are" -> Dict("cow" -> Num(2))
    ),
    "world" -> Num(31337),
    "bye" -> Str("314")
  )

  abstract class Visitor[T] {
    def visitStr(value: String): T
    def visitInt(value: Int): T
    def visitDict(): DictVisitor[T]
  }

  abstract class DictVisitor[T]{
    def visitKey(key: String): Unit
    def visitValue(): Visitor[T]
    def visitValue(value: T): Unit
    def done(): T
  }

  def dispatch[T](input: Json, visitor: Visitor[T]): T = {
    input match {
      case Str(s) => visitor.visitStr(s)
      case Num(n) => visitor.visitInt(n)
      case Dict(kvs @ _*) =>
        val dv = visitor.visitDict()
        for ((k,v) <- kvs){
          dv.visitKey(k)
          dispatch(v, dv.visitValue())
        }
        dv.done()
    }
  }

  class StringifyVisitor extends Visitor[String] {

    override def visitStr(value: String) = s"\"$value\""

    override def visitInt(value: Int) = value.toString

    override def visitDict() = new StringifyDictVisitor
  }

  class StringifyDictVisitor extends DictVisitor[String]{

    val tokens = collection.mutable.Buffer("{")

    override def visitKey(key: String): Unit = {
      if (tokens.length > 1) tokens.append(",")
      tokens.append(s"\"$key\":")
    }

    override def visitValue() = new StringifyVisitor

    override def visitValue(value: String): Unit = tokens.append(value)

    override def done() = {
      tokens.append("}")
      tokens.mkString
    }
  }
  // {"hello":{"i am":{"cow":1},"you are":{"cow":2}},"world":31337,"bye":"314"}
  override def tests = Tests{
    * - {
      println(dispatch(tree, new StringifyVisitor))
    }
  }
}
