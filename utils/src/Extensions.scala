package org.gbz

object Extensions {
//  implicit class FastParseW[T](val parser: Parser[T]) extends AnyVal {
//    def fastParse(data: String): Option[T] = parser.parse(data) match {
//      case Parsed.Success(x, _) => Some(x)
//      case x => None.traceWith(_ => s"$x\n$data\n")
//    }
//  }ddd

  implicit class ZipExt[A](val obj: Iterable[A]) extends AnyVal {
    def zipWith[B,C](that: Iterable[B])(f: (A, B) => C): Iterable[C] = obj.zip(that).map(_.reduce(f))
    //def zip[A1 >: A, B, That](that: GenIterable[B])(implicit bf: CanBuildFrom[Repr, (A1, B), That]): That = {
  }

  implicit class Tuple2Ext[A,B](val obj: Tuple2[A,B]) extends AnyVal {
    def reduce[C](f: (A,B) => C) = f(obj._1, obj._2)
  }

  implicit class TransformImplicit[T](val obj: T) extends AnyVal {
    def <|[B](f: T => B): T = {f(obj); obj}
    def |?>(condition: Boolean)(transform: T => T): T =
      if(condition) transform(obj) else obj
    def |?>(condition: T => Boolean)(transform: T => T): T =
      if(condition(obj)) transform(obj) else obj
    def |?!>(condition: Boolean)(transform: T => T): T =
      if(!condition) transform(obj) else obj
    def |?!>(condition: T => Boolean)(transform: T => T): T =
      if(!condition(obj)) transform(obj) else obj
  }

  implicit class Traceable[A] (val obj: A) extends AnyVal {
    def traceWith[B](f: A => B ): A = { println(f(obj)); obj}
    def trace[U](u: => U): A = traceWith(_ => u)
    def trace: A = trace[A](obj)
  }

  def assertEq[T](y: T): T => Unit = x => assert(x == y)
  implicit class BackArrowAssert[T](val left: T) extends AnyVal {
    //def <== (right: Any) = {assert(left == right); left}
    def <== (right: Any): T = left <| assertEq(right)
  }

//  val printer = pprint.copy( additionalHandlers = {case x:String => pprint.Tree.Literal(x.toString)},
//    defaultHeight = 1000, defaultWidth = 140)
  // val log = Logging(CalmImplicits.system, this)

  implicit class Mapable (val cc: Any) extends AnyVal {
    def ccToMap: Map[String, Any] =
      (Map[String, Any]() /: cc.getClass.getDeclaredFields) {
        (a, f) =>
          f.setAccessible(true)
          a + (f.getName -> f.get(cc))
      }
  }
}
