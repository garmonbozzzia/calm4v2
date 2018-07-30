import javax.naming.spi.Resolver

import coursier.ivy.IvyRepository
import mill._
import mill.scalalib._
import coursier.maven.MavenRepository
import mill.define.Target
import mill.scalajslib._
import mill.util.Loose

trait ScalaModule2_12 extends ScalaModule {
  def scalaVersion = "2.12.4"
}

trait MacroModule extends ScalaModule2_12 {
  override def ivyDeps = Agg(
    ivy"org.scala-lang:scala-reflect:2.12.4"
  )
}

trait CommonModule extends ScalaModule2_12{
  val repos = Seq(
    MavenRepository("https://dl.bintray.com/cakesolutions/maven/"),
    MavenRepository("https://oss.sonatype.org/content/repositories/releases"),
    MavenRepository("https://oss.sonatype.org/content/repositories/snapshots")
  )

  override def repositories = super.repositories ++ repos

  object test extends Tests{
    override def repositories = super.repositories ++ repos
    override def ivyDeps = Agg(
      ivy"com.lihaoyi::utest:0.6.4",
      ivy"org.wvlet.airframe::airframe-log:0.50",
      ivy"com.lihaoyi::ammonite-ops:1.1.0")

    def testFrameworks = Seq("utest.runner.Framework")
  }
}

object foo extends ScalaModule {
  def scalaVersion = "2.12.4"
  override def repositories =
    super.repositories ++ Seq(MavenRepository("https://dl.bintray.com/cakesolutions/maven/"))

  override def ivyDeps = Agg(
    ivy"net.cakesolutions::scala-kafka-client:1.1.0"
  )
}

object utils extends CommonModule {
  object macroLib extends MacroModule {
    override def ivyDeps = Agg(
      ivy"org.scala-lang:scala-reflect:2.12.4",
    ivy"org.wvlet.airframe::airframe-log:0.50"
    )
  }
  override def moduleDeps = Seq(macroLib)
  override def ivyDeps = Agg(
    ivy"com.lihaoyi::ammonite-ops:1.1.0",
    ivy"org.wvlet.airframe::airframe-log:0.50"
  )
}

object calm extends CommonModule {
  override def moduleDeps = Seq(utils)

  override def mainClass = Some("org.gbz.calm.CalmApps")
  override def ivyDeps = Agg(
    ivy"org.wvlet.airframe::airframe:0.52",
    ivy"org.wvlet.airframe::airframe-log:0.50",
    ivy"com.typesafe.akka::akka-stream-kafka:0.20",
    ivy"com.lightbend::kafka-streams-scala:0.2.1",
    ivy"org.scala-lang:scala-reflect:2.12.4",
    ivy"com.lihaoyi::fastparse:1.0.0",
    ivy"net.ruippeixotog::scala-scraper:2.1.0",
    ivy"org.json4s::json4s-native:3.5.3",
    ivy"org.json4s::json4s-jackson:3.5.3",
    ivy"net.debasishg::redisclient:3.5",
    ivy"com.typesafe.akka::akka-http:10.1.1",
    ivy"com.typesafe.akka::akka-stream:2.5.11",
    ivy"net.cakesolutions::scala-kafka-client:1.1.0",
    ivy"net.cakesolutions::scala-kafka-client-akka:1.1.0",
    ivy"ch.qos.logback:logback-classic:1.2.3",
    ivy"com.typesafe.scala-logging::scala-logging:3.9.0"
  )
}

import mill.scalajslib._
object scalajs extends ScalaJSModule {
  def scalaVersion = "2.12.4"
  def scalaJSVersion = "0.6.22"
  def mainClass = Some("DiodeTest")
//  def mainClass = Some("BootstrapTestApp")
//  def mainClass = Some("HelloApp")

//  override final def moduleKind = T { ModuleKind.CommonJSModule }

//  scalajslib.ScalaJSBridge.scalaJSBridge.

  override def ivyDeps: Target[Loose.Agg[Dep]] = Agg (
    ivy"io.suzaku::diode::1.1.3",
    ivy"com.lihaoyi::upickle::0.6.6",
    ivy"com.github.karasiq::scalajs-bootstrap::2.3.1",
    ivy"com.lihaoyi::scalarx::0.3.2",
    ivy"com.lihaoyi::scalatags::0.6.7",
    ivy"io.scalajs.npm::kafka-node::0.4.2",
    ivy"org.scala-js::scalajs-dom::0.9.5"
  )

}