import akka.http.scaladsl.model.Uri
import calm.solid.Sandbox.mainDesign
import calm.solid._
import org.gbz.Tag.@@
import utest._
import scala.collection.immutable.{Seq => ISeq}


import scala.concurrent.Future


object Stash extends TestSuite with Designs {
  override def tests = Tests {
    'WebClient - {
      import CalmUri._
      val a: WebClient[CalmUri, CalmHeaders] = mainDesign
        //          .bind[WebClient]
        .newSession.build[WebClient[CalmUri, CalmHeaders]]
    }

    'NewWc - {
      implicit val keyInt: Key[Int] = ???
      implicit val vldStr: Validator[String] = ???
      implicit val cuInt: CalmUri[Int] = ???
      implicit val chStr: CalmHeaders[String] = ???

      implicit def wcIntStr[A: CalmUri, B: CalmHeaders]: WebClientA[A, B] = WebClientA.calm4[A, B]

      val a: Future[String @@ String] = wcIntStr.get(10)
      //WebClientA.calm4[Int,String]
    }
    'SandBox - {
      //Implicits
      //      implicit val key: Key[Int] = _ => pwd
      implicit val vld: Validator[String] = ???
      implicit val cu: CalmUri[Int] = _ => Uri./
      implicit val ch: CalmHeaders[String] = CalmHeaders.pure(ISeq.empty)
      //      val key: Key[Int] = _ => pwd
      //      val key2: Validator[String] = ???
      //      implicit val common: Common[Int,String] = common2
      //      implicit val cmn: Common[Int,String] = UriHeader[Int, String]()
    }

    'WebClient - {
      import CalmUri._
      val a: WebClient[CalmUri, CalmHeaders] = mainDesign
        //          .bind[WebClient]
        .newSession.build[WebClient[CalmUri, CalmHeaders]]
    }

    'NewWc - {
      implicit val keyInt: Key[Int] = ???
      implicit val vldStr: Validator[String] = ???
      implicit val cuInt: CalmUri[Int] = ???
      implicit val chStr: CalmHeaders[String] = ???
      implicit def wcIntStr[A:CalmUri,B:CalmHeaders]: WebClientA[A,B] = WebClientA.calm4[A,B]
      val a: Future[String @@ String] = wcIntStr.get(10)
      //WebClientA.calm4[Int,String]
    }
  }
}
