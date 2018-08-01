package org.gbz

import akka.actor.ActorSystem
import akka.stream.{ActorMaterializer, ActorMaterializerSettings, Supervision}
import org.gbz.utils.log.Log._

import scala.collection.immutable
import scala.concurrent.ExecutionContextExecutor

object Global {
  implicit val system: ActorSystem = ActorSystem()

  val decider: Supervision.Decider = x => Supervision.Resume.traceWith(_ => x).traceWith(_ => x.getStackTrace.mkString("\n"))
  implicit val materializer: ActorMaterializer = ActorMaterializer(
    ActorMaterializerSettings(system).withSupervisionStrategy(decider))

  implicit val ec: ExecutionContextExecutor = system.dispatcher

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

  def toImmutable[A](elements: Iterable[A]): immutable.Iterable[A] =
    new scala.collection.immutable.Iterable[A] {
      override def iterator: Iterator[A] = elements.toIterator
    }
}
