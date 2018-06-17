package org.gbz.calm

import akka.http.scaladsl.model.Uri
import akka.http.scaladsl.model.Uri.{Path, Query}
import org.gbz.ExtUtils._

import scala.collection.immutable
import scala.language.implicitConversions

/**
  * Created by yuri on 26.08.17.
  */
object CalmUri {
  type Id = Int
  private val ks = Seq("[data]", "[name]", "[searchable]", "[orderable]", "[search][value]", "[search][regex]")
  private val vs = Seq("", true, true, "", false).map(_.toString)

  private def columnParams(n: Int): immutable.Seq[(String, String)] = for{
    i <- (0 to n).toList
    (x,y) <- ks.zip(i.toString +: vs)
  } yield s"columns[$i]$x" -> y

  val host = Uri("https://calm.dhamma.org")
  implicit def seq2query(seq: Seq[(String, String)]): Uri.Query = Uri.Query(seq.toMap)
  implicit def string2Path(str: String): Path = Path(str)

  def searchUri(s: String): Uri = host.withPath("/en/course_applications/search").withQuery(Query("typeahead" -> s))
  def messageUri(mId: Int, aId: Id): Uri = host.withPath(s"/en/course_applications/$aId/messages/$mId")
  def noteUri(nId: Int, aId: Id): Uri = host.withPath(s"/en/course_applications/$aId/notes/$nId")
  def messageOrNoteUri( msgId: Int, aId: Int, mType: String): Uri = mType match {
    case "m" => messageUri(msgId, aId)
    case "n" => noteUri(msgId, aId)
  }

  def applicationUri(appId: Id, courseId: Id): Uri =
    host.withPath(s"/en/courses/$courseId/course_applications/$appId/edit")

  def reflistUri(appId: Id): Uri = host
    .withPath(s"/en/course_application/$appId/course_application_load_rl")
    .withQuery(columnParams(9) ++ Seq(
      "order[0][column]" -> "0",
      "order[0][dir]" -> "asc",
      "start" -> "0",
      "length" -> "100",
      "search[value]" -> "",
      "search[regex]" -> "false"
    ))

  def courseUri(id: Int): Uri = host.withPath(s"/en/courses/$id/course_applications")

  def inboxUri(start: Int = 0): Uri =
    host.withPath("/en/course_applications/inbox").withQuery(columnParams(8) ++ Seq(
      "draw" -> "1",
      "order[0][column]" -> "1",
      "order[0][dir]" -> "asc",
      "start" -> start.toString,
      "length" -> "100",
      "search[value]" -> "",
      "search[regex]" -> "false",
      "user_custom_search[filterOnMyApplicationsOnly]" -> "false",
      "user_custom_search[length]" -> "100",
      "user_custom_search[start]" -> "0"
    )
    )

  def coursesUri(startDate: String = "2010-8-01"): Uri =
    host.withPath("/en/courses").withQuery(columnParams(10) ++ Seq (
      "order[0][column]" -> "0",
      "order[0][dir]" -> "asc",
      "start" -> "0",
      "length" -> "500",
      "search[value]" -> "",
      "search[regex]" -> "false",
      "user_custom_search[length]" -> "100",
      "user_custom_search[start]" -> "0",
      "user_custom_search[operator_start_date]" -> "gte_date",
      //  "user_custom_search[criterion_start_date]" -> startDate,
      "user_custom_search[operator_course_type_id]" -> "eq",
      "user_custom_search[filterOnMyCoursesOnly]" -> "false",
      "user_custom_search[defaultCurrentDate]" -> "true",
      "user_custom_search[context]" -> "all_courses"
    )).trace

  def conversationUri(appId: Id): Uri = host.withPath(s"/en/course_applications/$appId/conversation_datatable")
    .withQuery( columnParams(8) ++ Seq(
      "order[0][column]" -> "0",
      "order[0][dir]" -> "asc",
      "start" -> "0",
      "length" -> "-1",
      "search[value]" -> "",
      "search[regex]" -> "false"
    )
    )
}