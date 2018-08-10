import coursier.maven.MavenRepository
import mill._
import mill.scalalib._
import ammonite.ops._

object Libraries {
  val akkaVersion = "2.5.14"
  val akkaHttpVersion = "10.1.3"
  val airframeVersion = "0.52"
  val airframeLib = ivy"org.wvlet.airframe::airframe:${airframeVersion}"
  val airframeLogLib = ivy"org.wvlet.airframe::airframe-log:${airframeVersion}"
  val akkaStreamKafkaLib = ivy"com.typesafe.akka::akka-stream-kafka:0.20"
  val kafkaStreamsLib = ivy"com.lightbend::kafka-streams-scala:0.2.1"
  val reflectLib = ivy"org.scala-lang:scala-reflect:2.12.4"
  val fastparseLib = ivy"com.lihaoyi::fastparse:1.0.0"
  val scrapperLib = ivy"net.ruippeixotog::scala-scraper:2.1.0"
  val json4sNativeLib = ivy"org.json4s::json4s-native:3.5.3"
  val json4sJacksonLib = ivy"org.json4s::json4s-jackson:3.5.3"
  val redisclientLib = ivy"net.debasishg::redisclient:3.5"
  val akkaHttpLib = ivy"com.typesafe.akka::akka-http:${akkaHttpVersion}"
  val akkaStreamLib = ivy"com.typesafe.akka::akka-stream:${akkaVersion}"
  val kafkaClientLib = ivy"net.cakesolutions::scala-kafka-client:1.1.0"
  val kafkaClientAkkaLib = ivy"net.cakesolutions::scala-kafka-client-akka:1.1.0"
  val logbackLib = ivy"ch.qos.logback:logback-classic:1.2.3"
  val loggingLib = ivy"com.typesafe.scala-logging::scala-logging:3.9.0"
  val utestLib = ivy"com.lihaoyi::utest:0.6.4"
  val ammoniteLib = ivy"com.lihaoyi::ammonite-ops:1.1.0"
  val shapelessLib = ivy"com.chuusai::shapeless:2.3.3"
}
import Libraries._

trait ExtendedRepo extends ScalaModule {
  val repos = Seq(
    MavenRepository("https://dl.bintray.com/cakesolutions/maven/"),
    MavenRepository("https://oss.sonatype.org/content/repositories/releases"),
    MavenRepository("https://oss.sonatype.org/content/repositories/snapshots")
  )

  override def repositories = super.repositories ++ repos

}

trait TestableModule extends ScalaModule2_12 {

  object test extends Tests with ExtendedRepo {
    override def ivyDeps = Agg(
      utestLib,
      airframeLogLib,
      ammoniteLib
    )

    def testFrameworks = Seq("utest.runner.Framework")
  }
}

trait ScalaModule2_12 extends ScalaModule with ExtendedRepo {
  def scalaVersion = "2.12.4"
  def scalacOptions = Seq(
    "-feature",
    "-language:higherKinds",
    "-language:postfixOps",
    "-language:implicitConversions",
    "-language:existentials",
    "-language:reflectiveCalls"
  )
}

trait MacroModule extends ScalaModule2_12 {
  override def ivyDeps = Agg(reflectLib)
}

//object foo extends ScalaModule {
//  def scalaVersion = "2.12.6"
//  override def repositories =
//    super.repositories ++ Seq(MavenRepository("https://dl.bintray.com/cakesolutions/maven/"))
//
//  override def ivyDeps = Agg(
//    kafkaClientLib
//  )
//}

object utils extends TestableModule {
  object macroLib extends MacroModule {
    override def ivyDeps = Agg(
      reflectLib,
      airframeLogLib,
      akkaStreamLib,
//      akkaHttpLib
    )
  }
  override def moduleDeps = Seq(macroLib)
  override def ivyDeps = Agg(
    ammoniteLib,
    airframeLogLib
  )
}

object calm extends TestableModule {
  override def moduleDeps = Seq(utils)
  val modelPath = millSourcePath / "1-model"
  val corePath = millSourcePath / "2-core"
  val networkPath = millSourcePath / "3-network"
  val storagePath = millSourcePath / "3-storage"
  val appsPath = millSourcePath / "4-apps"
  object model extends ScalaModule2_12 {
    override def moduleDeps = Seq(utils)
    override def millSourcePath = modelPath
    override def ivyDeps = Agg(
      airframeLib
    )
  }

  def gitdiff = T.worker{
    %%("git", "rev-parse", "HEAD")(appsPath).out.string
  }

  object core extends ScalaModule2_12 {
    override def moduleDeps = Seq(utils, model)
    override def millSourcePath = corePath
  }

  object network extends ScalaModule2_12 {
    override def moduleDeps = Seq(utils, model, core)
    override def millSourcePath = networkPath
    override def ivyDeps = Agg(
      akkaHttpLib,
      akkaStreamLib,
      json4sNativeLib,
      json4sJacksonLib,
      airframeLib
    )
  }
  object storage extends ScalaModule2_12 {
    override def millSourcePath = storagePath
  }

  object apps extends TestableModule {
//    override def moduleDeps = Seq(utils, model, core, network, storage)
    def gitdiff = T.input{
//      T.sources(appsPath){
//        %%("git", "diff", "--stat", "HEAD")(appsPath)
//      }
      %%("git", "diff", "--stat", "HEAD")(appsPath)
    }
//    override def compile = ???
    override def moduleDeps = Seq(utils)
    override def millSourcePath = appsPath
    override def ivyDeps = Agg(
      akkaHttpLib,
      akkaStreamLib,
      json4sNativeLib,
      json4sJacksonLib,
      redisclientLib,
      shapelessLib,
    )
  }

  override def mainClass = Some("org.gbz.calm.CalmApps")
  override def ivyDeps = Agg(
    airframeLib,
    airframeLogLib,
    akkaStreamKafkaLib,
    kafkaStreamsLib,
    reflectLib,
    fastparseLib,
    scrapperLib,
    json4sNativeLib,
    json4sJacksonLib,
    redisclientLib,
    akkaHttpLib,
    akkaStreamLib,
    kafkaClientLib,
    kafkaClientAkkaLib,
    logbackLib,
    loggingLib
  )
}
