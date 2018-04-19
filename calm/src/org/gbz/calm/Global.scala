package org.gbz.calm

import java.text.SimpleDateFormat

import akka.actor.ActorSystem
import akka.stream.{ActorMaterializer, ActorMaterializerSettings, Supervision}
import ammonite.ops.pwd

import scala.concurrent.{ExecutionContext, ExecutionContextExecutor}
import org.gbz.Extensions._
import org.json4s._
import org.json4s.jackson.JsonMethods._
import net.ruippeixotog.scalascraper.browser.JsoupBrowser

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

  val logs = pwd/'data/'logs
}
