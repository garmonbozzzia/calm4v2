import utest._
object ExtensionsTest extends TestSuite {
  val tests = Tests {
    import org.gbz.Extensions._
    * - {
      def f1(n: Int) = n.toString
      def f2: Int => String = _.toString
      def g1(n: Int, m: Int): Int = n + m
      def g2: (Int,Int) => Int = _ + _
//      assert(f2 ! 3 == "3")
      compileError("f1 ! 3")
      compileError("f1 ! 3")
//      assert(f1 _ ! 3 == "3")
      assert((f1 _)(3) == "3")
      compileError("(f1 _) 3" )
      assert(List(1,2,3).zipWith(List(4,5,6,7))(_ + _) == List(5,7,9))
      assert((2,"123").reduce(_ + _.toInt) == 125)
      //assert(1 == 2)
      //assert(List( true, true))
      //List( 1 -> 2, 2 -> 2).map(_.reduce( (x,y) => assert(f1(x) == y).trace))
    }
    'Timer - Timer(Thread.sleep(1000))(_.trace)(_ => "Done".trace)
  }
}
