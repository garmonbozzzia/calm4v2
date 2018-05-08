import javax.naming.spi.Resolver

import coursier.ivy.IvyRepository
import mill._
import mill.scalalib._
import coursier.maven.MavenRepository

trait CommonModule extends ScalaModule{
  def scalaVersion = "2.12.4"
  override def repositories = {
    println("common")
    super.repositories ++ Seq(MavenRepository("https://dl.bintray.com/cakesolutions/maven/"))
  }
  object test extends Tests{
    override def repositories = {
      println("test")
      super.repositories ++ Seq(MavenRepository("https://dl.bintray.com/cakesolutions/maven/"))
    }
    def ivyDeps = Agg(
      ivy"com.lihaoyi::utest:0.6.4",
      ivy"com.lihaoyi::ammonite-ops:1.1.0")

    def testFrameworks = Seq("utest.runner.Framework")
  }
}

object foo extends ScalaModule {
  def scalaVersion = "2.12.4"
  override def repositories = {
    super.repositories ++ Seq(MavenRepository("https://dl.bintray.com/cakesolutions/maven/"))
  }
  def ivyDeps = Agg(
    ivy"net.cakesolutions::scala-kafka-client:1.1.0"
  )
}

object utils extends CommonModule {
  def ivyDeps = Agg(ivy"com.lihaoyi::ammonite-ops:1.1.0")
}

object calm extends CommonModule {
  def moduleDeps = Seq(utils)

  def ivyDeps = Agg(
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
    ivy"net.cakesolutions::scala-kafka-client-akka:1.1.0"
  )
}