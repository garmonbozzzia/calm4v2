/* Created on 28.05.18 */
import com.karasiq.bootstrap.Bootstrap.default._
import diode._
import org.scalajs.dom.document

import scala.language.postfixOps
import scala.scalajs.js.Dynamic.{global => g, newInstance => jsnew}
import scalaTags.all._
//import
import upickle.default.{ReadWriter => RW}

trait MyAction

object MyAction {
  implicit object actionType extends ActionType[MyAction]
}
case class SimpleAction(a: String) extends MyAction

case class Model(m: String = "")
case class UserModel(newMessages: Seq[String] = Seq.empty, checkedMessages: Seq[String] = Seq.empty, filter: Any)


object AppCircuit extends Circuit[Model] {
  override protected def initialModel = Model()

  val handler = new ActionHandler(zoomTo(_.m)) {
    override protected def handle = {
      case SimpleAction(a) => updated(a)
    }
  }
  override protected def actionHandler = handler
}

class SimpleView(simpleReader: ModelRO[String], dispatch: Dispatcher){
  def render = {
    div(p(simpleReader()), button(cls:="btn btn-default",
      onclick := (() => dispatch(SimpleAction("p"))))("Click"))
  }
}

object DiodeTest {
  val view = new SimpleView(AppCircuit.zoom(_.m), AppCircuit)
  def main(args: Array[String]): Unit = {
    jQuery( () => {
      val root = div(id:="root").render
      document.body.appendChild(root)
      AppCircuit.subscribe(AppCircuit.zoom(identity))(_ => render(root) )
      AppCircuit(SimpleAction("start"))
    })
  }

  def render(root: Element) = {
    root.innerHTML = ""
    root.appendChild(view.render.render)
  }
}
