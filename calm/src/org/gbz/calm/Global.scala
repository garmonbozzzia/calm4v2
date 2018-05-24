package org.gbz.calm

import java.text.SimpleDateFormat

import akka.actor.ActorSystem
import akka.stream.{ActorMaterializer, ActorMaterializerSettings, Supervision}
import ammonite.ops.pwd
import net.ruippeixotog.scalascraper.browser.JsoupBrowser
import org.gbz.Extensions._
import org.json4s._

import scala.concurrent.{ExecutionContext, ExecutionContextExecutor}


/* Created on 19.04.18 */
object Global {
  implicit val system = ActorSystem()

  val decider: Supervision.Decider = x => Supervision.Resume.traceWith(_ => x).traceWith(_ => x.getStackTrace.mkString("\n"))
  implicit val materializer = ActorMaterializer(
    ActorMaterializerSettings(system).withSupervisionStrategy(decider))

  implicit val ec: ExecutionContextExecutor = ExecutionContext.global
  val browser = JsoupBrowser()
  implicit val formats = DefaultFormats

  val timezoneDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss Z")
  val dateFormat = new SimpleDateFormat("yyyy-MM-dd")

  implicit val logs = pwd/'data/'logs/"log.txt"


  import scala.reflect._
  import scala.reflect.runtime.universe._

  def fromMap[T: TypeTag: ClassTag](m: Map[String,_]): T = {
    val rm = runtimeMirror(classTag[T].runtimeClass.getClassLoader)
    val classTest = typeOf[T].typeSymbol.asClass
    val classMirror = rm.reflectClass(classTest)
    val constructor = typeOf[T].decl(termNames.CONSTRUCTOR).asMethod
    val constructorMirror = classMirror.reflectConstructor(constructor)
    val constructorArgs = constructor.paramLists.flatten.map( (param: Symbol) => {
      val paramName = param.name.toString
      if(param.typeSignature <:< typeOf[Option[Any]])
        m.get(paramName)
      else
        m.getOrElse(paramName, throw new IllegalArgumentException("Map is missing required parameter named " + paramName))
    })

    constructorMirror(constructorArgs:_*).asInstanceOf[T]
  }

  def toImmutable[A](elements: Iterable[A]) =
    new scala.collection.immutable.Iterable[A] {
      override def iterator: Iterator[A] = elements.toIterator
    }

  object CalmStates extends Enumeration {
    val NewPendingForConfirmation = Value("NewPendingForConfirmation ")
    val PendingForConfirmation = Value("PendingForConfirmation ")
    val Confirmed = Value("Confirmed ")
    val RequestedReconfirm = Value("RequestedReconfirm ")
    val Reconfirmed = Value("Reconfirmed ")
    val Arrived = Value("Arrived ")
    val Left = Value("Left ")
    val Completed = Value("Completed ")
    val NewPendingForWaitlist = Value("NewPendingForWaitlist ")
    val PendingForWaitlist = Value("PendingForWaitlist ")
    val ConfirmableWaitlist = Value("ConfirmableWaitlist ")
    val WaitListReqReconfirm = Value("WaitListReqReconfirm ")
    val WaitListReconfirmed = Value("WaitListReconfirmed ")
    val NewApplication = Value("NewApplication ")
    val NewNoVacancy = Value("NewNoVacancy ")
    val NoVacancy = Value("NoVacancy ")
    val NoShow = Value("NoShow ")
    val Cancelled = Value("Cancelled ")
    val Discontinued = Value("Discontinued ")
    val Refused = Value("Refused ")
  }

  //CalmStatuses.values

  val calmStates = Seq(
    "NewPendingForConfirmation",
    "PendingForConfirmation",
    "Confirmed",
    "RequestedReconfirm",
    "Reconfirmed",
    "Arrived",
    "Left",
    "Completed",
    "NewPendingForWaitlist",
    "PendingForWaitlist",
    "ConfirmableWaitlist",
    "WaitListReqReconfirm",
    "WaitListReconfirmed",
    "NewApplication",
    "NewNoVacancy",
    "NoVacancy",
    "NoShow",
    "Cancelled",
    "Discontinued",
    "Refused"
  )
}
