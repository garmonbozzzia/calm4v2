import mill._
import mill.scalalib._

trait CommonModule extends ScalaModule{
  def scalaVersion = "2.12.4"
  object test extends Tests{ 
    def ivyDeps = Agg(
      ivy"com.lihaoyi::utest:0.6.4",
      ivy"com.lihaoyi::ammonite-ops:1.1.0")

    def testFrameworks = Seq("utest.runner.Framework")
  }
}

object utils extends CommonModule {
  def ivyDeps = Agg(ivy"com.lihaoyi::ammonite-ops:1.1.0")
}

object calm extends CommonModule {
  def moduleDeps = Seq(utils)
  def ivyDeps = Agg(
    ivy"com.lihaoyi::fastparse:1.0.0",
    ivy"net.ruippeixotog::scala-scraper:2.1.0",
    ivy"org.json4s::json4s-native:3.5.3",
    ivy"org.json4s::json4s-jackson:3.5.3",
    ivy"net.debasishg::redisclient:3.5",
    ivy"com.typesafe.akka::akka-http:10.1.1", 
    ivy"com.typesafe.akka::akka-stream:2.5.11")
}