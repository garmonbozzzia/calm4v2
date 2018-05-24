/* Created on 18.05.18 */
import com.karasiq.bootstrap.Bootstrap.default._
import org.scalajs.dom
import org.scalajs.dom.raw.MouseEvent
import org.scalajs.dom.{document, window}
import rx._

import scala.language.postfixOps
import scala.scalajs.js
import scala.scalajs.js.Dynamic.{global => g, newInstance => jsnew}
import scalaTags.all._
//import
import upickle.default.{ReadWriter => RW, macroRW}
import upickle.default._

//{"familyName":"Тимонова"
// "cId":"4053"
// "givenName":"Галина"
// "email":"Galtimon@gmail.com"
// "state":"Completed"
// "role":"N"
// "pregnant":"false"
// "dismissed":""
// "aId":"250575"
// "phoneMobile":"89039066583"
// "nSat":"0"
// "enrolled":"18-02-06 08:08"
// "age":"71"
// "displayId":"D8pbVj"
// "receivedAt":"02/06 08:08"
// "phoneHome":"3832259938"
// "birth_date":"1947-02-08 1947-02-08"
// "gender":"F"
// "nServe":"0"}

case class NewApp(givenName: String,
                  familyName: String,
                  email: String,
                  @upickle.key("birth_date") birthDate: String,
                  phoneMobile: String,
                  phoneHome: String,
                  receivedAt: String)
object NewApp{
  implicit def rw: RW[NewApp] = macroRW
}

object BootstrapTestApp {
  def main(args: Array[String]): Unit = {
//    fff
    ggg
  }

  def ggg = {
    jQuery( () => {
      val node = div(`class`:= "container")(`class` := "sm-2")(p("Hello")).render
      node.onclick = ( (e:MouseEvent) => println(""))

      val echo = "ws://localhost:8080/course"
      val socket = new dom.WebSocket(echo)
      val listGroupNode = ul(`class`:="list-group").render

      val data = Var(Seq[NewApp]())

      //jQuery.apply("li")
      val ch = input(`type`:="checkbox")
      val a = Var(listGroupNode)
      a.trigger(println("Updated"))

      socket.onmessage = (e: dom.MessageEvent) => {
        val app = read[NewApp](e.data.toString)
        val newNode = li(`class`:="list-group-item list-group-item-info")(ch(app.familyName)).render
        listGroupNode.appendChild(newNode)
      }

      //get user data
      //action => viewSettings => data => html
      //message => data


      document.body.appendChild(node)
    })
  }

  def fff = {
    jQuery(() ⇒ {
      // Table tab will appear after 3 seconds
      // Show table tab in 3 seconds
      //      window.setTimeout(() ⇒ {
      //        tableVisible.update(true)
      //        println("Done!")
      //        window.setTimeout(() ⇒ { tabTitle() = "Table" }, 1000)
      //      }, 3000)

      //      document.body.appendChild(WebPage.html.render)
      //      setupSlip(g.document.getElementById("demo"))

      val echo = "ws://localhost:8080/course"
      val socket = new dom.WebSocket(echo)
      val listGroupNode = ul(`class`:="list-group").render

      socket.onmessage = (e: dom.MessageEvent) => {
        val app = read[NewApp](e.data.toString)
        listGroupNode.insertBefore(

          li(`class`:="list-group-item list-group-item-info")(app.familyName).render,
          listGroupNode.childNodes(0))
      }


      val bGroup = div(`class`:="btn-group mr-2 btn-group-toggle", attr("data-toggle"):="buttons", role:="group")
      val labelButton = label(`class`:="btn btn-secondary")
      val labelButtonActive = label(`class`:="btn btn-secondary active")
      val radioInput = input(`type`:="radio", autocomplete:="off")
      val checkboxInput = input(`type`:="checkbox", autocomplete:="off")
      val rButton = labelButton(radioInput)
      val b2 = label(`class`:="btn btn-secondary active")
      val rI = input(`type`:="radio", autocomplete:="off")
      val rButtonChecked = label(`class`:="btn btn-secondary active")(
        input(`type`:="radio", autocomplete:="off")
      )
      ButtonBuilder(active = true)
      val chButton =label(`class`:="btn btn-secondary")(
        input(`type`:="checkbox", name:="options", id:="option1", autocomplete:="off")
      )
      val chButtonChecked =label(`class`:="btn btn-secondary active")(
        input(`type`:="checkbox", name:="participant", id:="option1", autocomplete:="off", checked)
      )
      val toolbar = div( `class`:="btn-toolbar", role:="toolbar")
      val buttons = Var(toolbar(
        bGroup(
          labelButtonActive(radioInput(checked))("All"),
          labelButton(radioInput)("Male"),
          labelButton(radioInput)("Female")
        ),
        bGroup(
          labelButtonActive(checkboxInput(checked))("New"),
          labelButtonActive(checkboxInput(checked))("Old"),
          labelButtonActive(checkboxInput(checked))("Servers")
        ),
        div(`class`:="btn-group mr-2", role:="group"),
        div(`class`:="btn-group mr-2", role:="group")
      ).render)

      buttons.trigger(println(buttons.now))

      document.body.appendChild(buttons.now)

      span(`class`:="glyphicon glyphicon-phone")
      val data = Var(scala.collection.mutable.MutableList.empty[NewApp])
      val filterFunc = Var()

      document.body.appendChild(listGroupNode)
      import rx._

      //      socket.onmessage = (e: dom.MessageEvent) => document.body.appendChild(p(e.data.toString).render)

      socket.onopen = (e: dom.Event) => socket.send("start")
    })
  }

  def f: js.Dynamic => Unit = e => {
    //    e.preventDefault()
    println(e)
  }
  def ff: js.Dynamic => Unit = e => {
    e.target.parentNode.removeChild(e.target)
  }
  def setupSlip(list: js.Dynamic) {
    //Seq("beforereorder", "beforeswipe", "afterswipe")
    list.addEventListener("slip:beforereorder", f, false)
    list.addEventListener("slip:beforeswipe", f, false)
    list.addEventListener("slip:afterswipe", ff, false)
    //    list.addEventListener("slip:beforereorder", f, false)
    //    list.addEventListener("slip:beforereorder", f, false)
    jsnew (g.Slip)(list)
  }

  def appendPar(targetNode: dom.Node, text: String): Unit = {
    val parNode = document.createElement("p")
    val textNode = document.createTextNode(text)
    parNode.appendChild(textNode)
    targetNode.appendChild(parNode)
  }

}
