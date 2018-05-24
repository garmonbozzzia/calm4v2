/* Created on 19.05.18 */
import scalatags.JsDom
import scalatags.JsDom.all._

object WebPage {
  def f = () => println("Invoke")
  def buttons = div(`class`:="btn-group-vertical")(
    button(`type`:="button", `class`:="btn btn-secondary", onclick := f)("1"),
    button(`type`:="button", `class`:="btn btn-secondary")("2")
  )

  def list = div(id:="demo")((1 to 20).map(x => div(`class`:="card")(div(`class`:="card-header")("Akjskjsjsa"))))

  def verticalTablist = div(`class`:="nav flex-column nav-pills",id := "v-pills-tab",
    role:="tablist")


  def bbb(t: Any) = a(`class`:="nav-link", id:=s"v-pills-$t-tab", attr("data-toggle"):="pill", href:=s"#v-pills-$t",
    role:="tab", attr("aria-controls"):=s"v-pills-$t", attr("aria-selected"):="true")

  def ccc(t: Any) = div(`class`:="tab-pane fade", id:=s"v-pills-$t", role:="tabpanel", attr("aria-labelledby"):=s"v-pills-$t-tab")


  def navContent = div(`class`:="tab-content", id :="v-pills-tabContent")

  def navbar(pairs: (Modifier,Modifier)*) = div(`class`:="container-fluid")(
    div(`class`:="row")(
      div(`class` := "col-sm-3")(verticalTablist(pairs.map(_._1).zipWithIndex.map(x => bbb(x._2)(x._1)))),
      div(`class` := "col-sm-9")(navContent(pairs.map(_._2).zipWithIndex.map(x=>ccc(x._2)(x._1))))
    )
  )

  implicit def ss2hh(d: (String,String)) = new JsDom.StringFrag(d._1) -> new JsDom.StringFrag(d._2)
  implicit def ss2hh2(d: (String,Modifier)): (Modifier, Modifier) = new JsDom.StringFrag(d._1) -> d._2
//  def html = navbar(div("a")->div("b"))

  def demoTable = table(`class`:="table table-hover")(
    thead(tr(
      th(attr("scope"):="col")("#"),
      th(attr("scope"):="col")("First"),
      th(attr("scope"):="col")("Last"),
      th(attr("scope"):="col")("Handle")
    )), tbody(id:="demo")(
      tr(th(attr("scope"):="col")("1"), td("Mark"), td("Otto"), td("@mdo")),
      tr(th(attr("scope"):="col")("2"), td("Jacob"), td("Thornton"), td("@fat")),
      tr(th(attr("scope"):="col")("3"), td(colspan:=2)("Larry the Bird"), td("@twitter")),
    )
  )

//  def html = navbar("a"-> list, "b" -> list)
  def html = demoTable
}
