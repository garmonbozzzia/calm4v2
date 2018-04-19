import utest._
import org.gbz.Extensions._

object HelloTests extends TestSuite{
  val tests = Tests{
    // 'SignIn - {
    //   for{
    //     auth <- Authentication.cookie.trace
    //     result <- Http().singleRequest(Get("https://calm.dhamma.org/en/courses").addHeader(auth))
    //   } yield result.trace
    // }
    'UtilsUsing - {
      "Hello".trace
    }
  }
}