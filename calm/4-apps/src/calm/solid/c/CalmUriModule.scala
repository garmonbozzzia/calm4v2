package calm.solid

import akka.http.scaladsl.model.Uri
import akka.http.scaladsl.model.Uri.{Path, Query}
import scala.collection.immutable
import scala.language.implicitConversions
import org.gbz.Tag._

import calm.solid.CalmUriModule._

object CalmUriModule {
  val ks = Seq("[data]", "[name]", "[searchable]", "[orderable]", "[search][value]", "[search][regex]")
  val vs = Seq("", true, true, "", false).map(_.toString)

  def columnParams(n: Int): immutable.Seq[(String, String)] = for{
      i <- (0 to n).toList
      (x,y) <- ks.zip(i.toString +: vs)
    } yield s"columns[$i]$x" -> y

  val host: Uri = Uri("https://calm.dhamma.org")
  implicit def seq2query(seq: Seq[(String, String)]): Uri.Query = Uri.Query(seq.toMap)
  implicit def string2Path(str: String): Path = Path(str)
}

trait CalmUriModule {
  this: CoreModule with CalmEntitiesModule =>

  implicit val searchUri: CalmUri[String@@SearchTag] = x =>
    host.withPath("/en/course_applications/search")
      .withQuery(Query("typeahead" -> x))

  implicit val appUri: CalmUri[(AppId,CourseId)] = x =>
    host.withPath(s"/en/courses/${x._2}/course_applications/${x._1}/edit")

  implicit val reflistUri: CalmUri[Int@@ReflistTag] = appId => host
    .withPath(s"/en/course_application/$appId/course_application_load_rl")
    .withQuery(columnParams(9) ++ Seq(
      "order[0][column]" -> "0",
      "order[0][dir]" -> "asc",
      "start" -> "0",
      "length" -> "100",
      "search[value]" -> "",
      "search[regex]" -> "false"
    ))

  implicit val courseUri: CalmUri[CourseId] = id =>
    host.withPath(s"/en/courses/$id/course_applications")

  implicit val inboxUri: CalmUri[Int@@InboxTag] = start =>
    host.withPath("/en/course_applications/inbox")
      .withQuery(columnParams(8) ++ Seq(
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

  implicit val coursesUri: CalmUri[CourseListRequest] = x =>
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
    ))

  implicit val messageUri: CalmUri[MessageRequest] = x =>
    host.withPath(s"/en/course_applications/${x._2}/messages/${x._1}")

  implicit val noteUri: CalmUri[NoteRequest] = x =>
    host.withPath(s"/en/course_applications/${x._2}/notes/${x._1}")

  implicit val conversationUri: CalmUri[ConversationRequest] = appId =>
    host.withPath(s"/en/course_applications/$appId/conversation_datatable")
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
