import org.scalajs.dom
import dom.document
import scala.scalajs.js
import scala.scalajs.js.annotation._

import js.Dynamic.{ global => g, newInstance => jsnew }
//val today = jsnew(g.Date)()

//function setupSlip(list) {
//  list.addEventListener('slip:beforereorder', function(e){
//    if (e.target.classList.contains('demo-no-reorder')) {
//      e.preventDefault();
//    }
//  }, false);
//  list.addEventListener('slip:beforeswipe', function(e){
//    if (e.target.nodeName == 'INPUT' || e.target.classList.contains('demo-no-swipe')) {
//      e.preventDefault();
//    }
//  }, false);
//  list.addEventListener('slip:beforewait', function(e){
//    if (e.target.classList.contains('instant')) e.preventDefault();
//  }, false);
//  list.addEventListener('slip:afterswipe', function(e){
//    e.target.parentNode.appendChild(e.target);
//  }, false);
//  list.addEventListener('slip:reorder', function(e){
//    e.target.parentNode.insertBefore(e.target, e.detail.insertBefore);
//    return false;
//  }, false);
//  return new Slip(list);
//}
//setupSlip(document.getElementById('demo1'));

object HelloApp {
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

//  def main(args: Array[String]): Unit = {
//    println("Hello world!")
//    appendPar(document.body, "Hello World")
//    println(setupSlip(g.document.getElementById("demo1")))
//  }

  def main(args: Array[String]) = {
    val list = g.document.getElementById("demo1")
    setupSlip(list)

    val echo = "ws://echo.websocket.org"
    val socket = new dom.WebSocket(echo)
//    val el = document.createElement("li")
//    <li class="demo-no-reorder">Swipe,</li>
    import scalatags.JsDom.all._
    val el = li(`class` := "demo-no-reorder")("WS").render
//    el.innerHTML = "<p>HelloWs!<p>"
    socket.onmessage = (e: dom.MessageEvent) =>
      list.appendChild(li(`class` := "demo-no-reorder")(e.data.toString).render)

    socket.onopen = (e: dom.Event) =>
        socket.send("Hello ws!")

  }
}