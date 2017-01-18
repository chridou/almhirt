import sbt._
import Keys._
import sbtrelease._
import ReleasePlugin._
import ReleaseKeys._
import sbtunidoc.Plugin._
import sbtunidoc.Plugin.UnidocKeys._

object BuildSettings {
  val buildOrganization = "org.almhirt"
  val buildScalaVersion = "2.11.8"

  val akkaVersion = "2.4.16"
  val akkaHttpVersion = "10.0.1"
  val playVersion = "2.6.1"
  val scalatestVersion = "3.0.1"
  val sprayVersion = "1.3.4"
  val scalazVersion = "7.2.8"
  val reactiveMongoVersion = "0.12.0"
  val json4sVersion = "3.5.0"
  val ezRepsVersion = "0.6.2"
  val scalaXmlVersion = "1.0.5"
  val scalaParserVersion = "1.0.4"
  val akkaHttpJson4sVersion = "1.11.0"

  val buildSettings = Defaults.defaultSettings ++ releaseSettings ++ Seq (
	organization := buildOrganization,
  scalaVersion := buildScalaVersion,
  resolvers += "Typesafe Repo" at "http://repo.typesafe.com/typesafe/releases/",
  resolvers += "Sonatype Releases" at "https://oss.sonatype.org/content/repositories/releases/",
  testOptions in Test += Tests.Argument(TestFrameworks.ScalaTest, "-o", "-h", "target/html-test-reports", "-u", "target/test-reports"),
  fork in Test := true,
  parallelExecution in Test := false,
  scalacOptions ++= Seq("-unchecked", "-deprecation", "-feature"),

  releaseProcess <<= thisProjectRef apply { ref =>
    import ReleaseStateTransformations._
      Seq[ReleaseStep](
        checkSnapshotDependencies)
    }
  )
}

object Resolvers {
  val sonatypeSnapshots = "Sonatype snapshots" at "https://oss.sonatype.org/content/repositories/snapshots/"
  val typesafeSnapshot  = "Typesafe Snapshots Repository" at "http://repo.typesafe.com/typesafe/snapshots/"
  val scalatools = "Scala Tools" at "https://oss.sonatype.org/content/groups/scala-tools/"
}

object Dependencies {
  import BuildSettings._
	lazy val scala_reflect = "org.scala-lang" % "scala-reflect" % BuildSettings.buildScalaVersion
	lazy val scala_xml = "org.scala-lang.modules" %% "scala-xml" % BuildSettings.scalaXmlVersion
	lazy val scala_parser = "org.scala-lang.modules" %% "scala-parser-combinators" % BuildSettings.scalaParserVersion

	lazy val scalaz       = "org.scalaz" %% "scalaz-core" % BuildSettings.scalazVersion

	lazy val play2_iteratees   = "com.typesafe.play" %% "play-iteratees" % BuildSettings.playVersion

  lazy val json4s   = "org.json4s" %% "json4s-native" % json4sVersion % "compile"

  lazy val ezReps = "org.chridou" %% "ezreps-core" % ezRepsVersion % "compile"
  lazy val ezRepsJson4s = "org.chridou" %% "ezreps-json4s" % ezRepsVersion % "compile"

	lazy val akka_actor  = "com.typesafe.akka" %% "akka-actor" % BuildSettings.akkaVersion % "provided"
	lazy val akka_agent  = "com.typesafe.akka" %% "akka-agent" % BuildSettings.akkaVersion % "provided"
	lazy val akka_streams  = "com.typesafe.akka" %% "akka-stream" % BuildSettings.akkaVersion

	lazy val apache_codecs = "commons-codec" % "commons-codec" % "1.10"
	lazy val apache_commons_io = "commons-io" % "commons-io" % "2.4"
	lazy val icu4j = "com.ibm.icu" % "icu4j" % "56.1"

	lazy val akkaHttp = "com.typesafe.akka" %% "akka-http" % akkaHttpVersion
	lazy val akkaHttpCore = "com.typesafe.akka" %% "akka-http-core" % akkaHttpVersion
	lazy val akkaHttpCoreSprayJson = "com.typesafe.akka" %% "akka-http-spray-json" % akkaHttpVersion
	lazy val akkaHttpJsonSupport = "de.heikoseeberger" %% "akka-http-json4s" % akkaHttpJson4sVersion
	lazy val akkaHttpXmlSupport = "com.typesafe.akka" %% "akka-http-xml" % akkaHttpVersion

  lazy val snappy = "org.xerial.snappy" % "snappy-java" % "1.1.2.1"
  lazy val logback = "ch.qos.logback" % "logback-classic" % "1.1.6" % "provided"
	lazy val typesafe_config = "com.typesafe" % "config" % "1.3.0" % "provided"


  lazy val scalatest = "org.scalatest" %% "scalatest" % BuildSettings.scalatestVersion % "test"
	lazy val akka_testkit = "com.typesafe.akka" %% "akka-testkit" % BuildSettings.akkaVersion % "test"
  lazy val pegdown = "org.pegdown" % "pegdown" % "1.6.0"

}

trait CommonBuild {
  import Dependencies._
  import Resolvers._
  def commonProject(name: String, baseFile: java.io.File) =
  	Project(id = name, base = baseFile, settings = BuildSettings.buildSettings).settings(
	  libraryDependencies += apache_codecs,
	  libraryDependencies += scala_xml,
	  libraryDependencies += typesafe_config,
	  libraryDependencies += scalaz,
	  libraryDependencies += scalatest,
    libraryDependencies += pegdown
  )
}

trait I18nBuild {
  import Dependencies._
  import Resolvers._
  def i18nProject(name: String, baseFile: java.io.File) =
  	Project(id = name, base = baseFile, settings = BuildSettings.buildSettings).settings(
	  libraryDependencies += apache_commons_io,
    libraryDependencies += icu4j,
	  libraryDependencies += scalaz,
	  libraryDependencies += scalatest,
    libraryDependencies += pegdown
  )
}

trait HttpxAkkaHttpBuild {
  import Dependencies._
  import Resolvers._
  def httpxAkkaHttpProject(name: String, baseFile: java.io.File) =
  	Project(id = name, base = baseFile, settings = BuildSettings.buildSettings).settings(
	  libraryDependencies += akka_actor,
	  libraryDependencies += akkaHttp,
	  libraryDependencies += typesafe_config,
	  libraryDependencies += scalaz,
	  libraryDependencies += scalatest,
    libraryDependencies += pegdown
  )
}

trait CorexAkkaHttpClientBuild {
  import Dependencies._
  import Resolvers._
  def corexAkkaHttpClientProject(name: String, baseFile: java.io.File) =
  	Project(id = name, base = baseFile, settings = BuildSettings.buildSettings).settings(
	  libraryDependencies += akka_actor,
	  libraryDependencies += akka_streams,
	  libraryDependencies += scala_xml,
	  libraryDependencies += akkaHttp,
	  libraryDependencies += ezReps,
	  libraryDependencies += typesafe_config,
	  libraryDependencies += scalaz,
	  libraryDependencies += scalatest,
    libraryDependencies += pegdown
  )
}

trait HttpxAkkaHttpServiceBuild {
  import Dependencies._
  import Resolvers._
  def httpxAkkaHttpServiceProject(name: String, baseFile: java.io.File) =
  	Project(id = name, base = baseFile, settings = BuildSettings.buildSettings).settings(
	  libraryDependencies += akka_actor,
	  libraryDependencies += akkaHttp,
	  libraryDependencies += akkaHttpJsonSupport,
	  libraryDependencies += akkaHttpXmlSupport,
	  libraryDependencies += ezReps,
	  libraryDependencies += scala_xml,
	  libraryDependencies += typesafe_config,
	  libraryDependencies += scalaz,
	  libraryDependencies += scalatest,
    libraryDependencies += pegdown
  )
}

trait AlmhirtxReactiveMongoBuild {
  import Dependencies._
  import Resolvers._
  def almhirtxReactiveMongoProject(name: String, baseFile: java.io.File) =
  	Project(id = name, base = baseFile, settings = BuildSettings.buildSettings).settings(
	  libraryDependencies += scalaz,
	  libraryDependencies += typesafe_config,
	  libraryDependencies += scala_xml,
	  libraryDependencies += ezReps,
	  libraryDependencies += "org.reactivemongo" %% "reactivemongo" % BuildSettings.reactiveMongoVersion % "provided",
	  libraryDependencies += "org.reactivemongo" %% "reactivemongo-iteratees" % BuildSettings.reactiveMongoVersion
		exclude("ch.qos.logback", "logback-core")
		exclude("ch.qos.logback", "logback-classic"),
 	  libraryDependencies += scalatest,
    libraryDependencies += pegdown
  )
}

trait CoreBuild {
  import Dependencies._
  import Resolvers._
  def coreProject(name: String, baseFile: java.io.File) =
  	Project(id = name, base = baseFile, settings = BuildSettings.buildSettings).settings(
	  libraryDependencies += scalaz,
	  libraryDependencies += play2_iteratees,
	  libraryDependencies += akka_streams,
	  libraryDependencies += akka_actor,
	  libraryDependencies += scala_xml,
	  libraryDependencies += ezReps,
	  libraryDependencies += logback,
	  libraryDependencies += akka_testkit,
	  libraryDependencies += scalatest,
    libraryDependencies += pegdown
  )
}

trait DashboardBuild {
  import Dependencies._
  import Resolvers._
  def dashboardProject(name: String, baseFile: java.io.File) =
  	Project(id = name, base = baseFile, settings = BuildSettings.buildSettings).settings(
	  libraryDependencies += scalaz,
	  libraryDependencies += akka_streams,
	  libraryDependencies += akka_actor,
	  libraryDependencies += scala_xml,
	  libraryDependencies += logback,
	  libraryDependencies += akka_testkit,
	  libraryDependencies += scalatest,
    libraryDependencies += pegdown
  )
}

trait CorexMongoBuild {
  import Dependencies._
  import Resolvers._
  def corexMongoProject(name: String, baseFile: java.io.File) =
  	Project(id = name, base = baseFile, settings = BuildSettings.buildSettings).settings(
	  resolvers += sonatypeSnapshots,
	  libraryDependencies += scalaz,
	  libraryDependencies += akka_actor,
	  libraryDependencies += akka_streams,
	  libraryDependencies += scala_xml,
	  libraryDependencies += ezReps,
	  libraryDependencies += play2_iteratees,
	  libraryDependencies += typesafe_config,
	  libraryDependencies += snappy,
	  libraryDependencies += "org.reactivemongo" %% "reactivemongo" % BuildSettings.reactiveMongoVersion % "provided"
		exclude("ch.qos.logback", "logback-core")
		exclude("ch.qos.logback", "logback-classic"),
 	  libraryDependencies += scalatest,
    libraryDependencies += pegdown
  )
}

trait CorexAkkaHttpServiceBuild {
  import Dependencies._
  import Resolvers._
  def corexAkkaHttpServiceProject(name: String, baseFile: java.io.File) = {
 	Project(id = name, base = baseFile, settings = BuildSettings.buildSettings).settings(
	  libraryDependencies += scalaz,
	  libraryDependencies += akka_actor,
	  libraryDependencies += akka_streams,
	  libraryDependencies += play2_iteratees,
	  libraryDependencies += scala_xml,
	  libraryDependencies += akkaHttp,
	  libraryDependencies += akkaHttpCore,
	  libraryDependencies += akkaHttpJsonSupport,
	  libraryDependencies += akkaHttpXmlSupport,
	  libraryDependencies += akka_testkit,
	  libraryDependencies += ezRepsJson4s,
	  libraryDependencies += json4s,
	  libraryDependencies += scalatest,
    libraryDependencies += pegdown
	  )
	  }
}


trait RiftWarpBuild {
  import Dependencies._
  import Resolvers._
  def riftwarpProject(name: String, baseFile: java.io.File) =
  	Project(id = name, base = baseFile, settings = BuildSettings.buildSettings).settings(
	  libraryDependencies += scala_reflect,
	  libraryDependencies += apache_codecs,
	  libraryDependencies += scala_xml,
	  libraryDependencies += scala_parser,
	  libraryDependencies += scalaz,
//	  libraryDependencies += "com.chuusai" %% "shapeless" % "1.2.4",
	  libraryDependencies += scalatest,
    libraryDependencies += pegdown
  )
}

trait RiftWarpHttpAkkaHttpBuild {
  import Dependencies._
  import Resolvers._
  def riftwarpHttpAkkaHttpProject(name: String, baseFile: java.io.File) =
  	Project(id = name, base = baseFile, settings = BuildSettings.buildSettings).settings(
	  libraryDependencies += scala_reflect,
    libraryDependencies += akka_actor,
	  libraryDependencies += apache_codecs,
	  libraryDependencies += akkaHttp,
	  libraryDependencies += scalaz,
	  libraryDependencies += scalatest,
    libraryDependencies += pegdown
  )
}

trait RiftWarpMongoExtBuild {
  import Dependencies._
  import Resolvers._
  def riftwarpMongoExtProject(name: String, baseFile: java.io.File) =
  	Project(id = name, base = baseFile, settings = BuildSettings.buildSettings).settings(
	  resolvers += sonatypeSnapshots,
	  libraryDependencies += scala_reflect,
	  libraryDependencies += apache_codecs,
	  libraryDependencies += "org.reactivemongo" %% "reactivemongo-bson" % BuildSettings.reactiveMongoVersion % "provided"
		exclude("ch.qos.logback", "logback-core")
		exclude("ch.qos.logback", "logback-classic"),
	  libraryDependencies += scalaz,
	  libraryDependencies += scalatest,
    libraryDependencies += pegdown
  )
}

trait RiftAlmhirtCoreExtBuild {
  import Dependencies._
  import Resolvers._
  def riftwarpAlmhirtCoreExtProject(name: String, baseFile: java.io.File) =
  	Project(id = name, base = baseFile, settings = BuildSettings.buildSettings).settings(
	  resolvers += sonatypeSnapshots,
    libraryDependencies += akka_actor,
	  libraryDependencies += scala_reflect,
	  libraryDependencies += scalaz,
	  libraryDependencies += scalatest,
    libraryDependencies += pegdown
  )
}

trait RiftWarpAutomaticBuild {
  import Dependencies._
  import Resolvers._
  def riftwarpAutomaticProject(name: String, baseFile: java.io.File) =
  	Project(id = name, base = baseFile, settings = BuildSettings.buildSettings).settings(
	  libraryDependencies += apache_codecs,
	  libraryDependencies += scalaz,
	  libraryDependencies += scalatest,
    libraryDependencies += pegdown
  )
}

object AlmHirtBuild extends Build
	with CommonBuild
	with I18nBuild
	with HttpxAkkaHttpBuild
	with AlmhirtxReactiveMongoBuild
	with CorexAkkaHttpClientBuild
	with HttpxAkkaHttpServiceBuild
	with CoreBuild
	with DashboardBuild
	with CorexMongoBuild
	with CorexAkkaHttpServiceBuild
	with RiftWarpBuild
	with RiftWarpHttpAkkaHttpBuild
	with RiftWarpMongoExtBuild
	with RiftAlmhirtCoreExtBuild
	with RiftWarpAutomaticBuild {
  lazy val root = Project(
    id = "almhirt",
		settings = BuildSettings.buildSettings,
	  base = file("."))
      .settings(unidocSettings: _*)
      .aggregate(	common,
									i18n,
									httpxAkkaHttp,
									corexAkkaHttpClient,
									httpxAkkaHttpService,
									almhirtxReactiveMongo,
									core,
									dashboard,
									mongoExtensions,
									corexAkkaHttpService,
									riftwarp,
									riftwarpHttpAkkaHttp,
									riftwarpMongoProject,
                  riftwarpAlmhirtCoreProject)
  lazy val common = commonProject(	name = "almhirt-common",
                       			baseFile = file("almhirt-common"))

  lazy val i18n = i18nProject(	name = "almhirt-i18n",
                       			baseFile = file("almhirt-i18n")) dependsOn(common)

  lazy val httpxAkkaHttp = httpxAkkaHttpProject(	name = "almhirt-httpx-akka-http",
                       			baseFile = file("./ext/almhirt-httpx-akka-http")) dependsOn(common)

  lazy val corexAkkaHttpClient = corexAkkaHttpClientProject(	name = "almhirt-corex-akka-http-client",
                       			baseFile = file("./ext/almhirt-corex-akka-http-client")) dependsOn(common, httpxAkkaHttp, core)

  lazy val httpxAkkaHttpService = httpxAkkaHttpServiceProject(	name = "almhirt-httpx-akka-http-service",
                       			baseFile = file("./ext/almhirt-httpx-akka-http-service")) dependsOn(common, httpxAkkaHttp)

  lazy val almhirtxReactiveMongo = almhirtxReactiveMongoProject(	name = "almhirt-reactivemongox",
                       			baseFile = file("./ext/almhirt-reactivemongox")) dependsOn(common)

  lazy val core = coreProject(	name = "almhirt-core",
		baseFile = file("almhirt-core")) dependsOn(	common % "compile; test->compile; test->test", i18n/*,
																								corexRiftwarp % "test",
																								riftwarp % "test->test"*/)

  lazy val dashboard = dashboardProject(	name = "almhirt-dashboard", baseFile = file("./ext/almhirt-dashboard")) dependsOn(common, core, httpxAkkaHttpService)

  lazy val mongoExtensions = corexMongoProject(	name = "almhirt-corex-mongo",
                       			baseFile = file("./ext/almhirt-corex-mongo")) dependsOn(common, core, almhirtxReactiveMongo, riftwarp % "test->test", riftwarpMongoProject % "test")

  lazy val corexAkkaHttpService = corexAkkaHttpServiceProject(	name = "almhirt-corex-akka-http-service",
                       				baseFile = file("./ext/almhirt-corex-akka-http-service")) dependsOn(common, httpxAkkaHttpService, core, riftwarp % "test")

  lazy val riftwarp = riftwarpProject(	name = "riftwarp",
                       			baseFile = file("riftwarp")) dependsOn(common)

  lazy val riftwarpMongoProject = riftwarpMongoExtProject(	name = "riftwarpx-mongo",
                       			baseFile = file("./ext/riftwarpx-mongo")) dependsOn(common, riftwarp)

  lazy val riftwarpAlmhirtCoreProject = riftwarpAlmhirtCoreExtProject(	name = "riftwarpx-almhirt-core",
                       			baseFile = file("./ext/riftwarpx-almhirt-core")) dependsOn(common, core, riftwarp)

  lazy val riftwarpHttpAkkaHttp = riftwarpHttpAkkaHttpProject(	name = "riftwarpx-http-akka-http",
                       			baseFile = file("./ext/riftwarpx-http-akka-http")) dependsOn(common, riftwarp, httpxAkkaHttp)

/*
  lazy val riftwarpAutomatic = riftwarpAutomaticProject(	name = "riftwarp-automatic",
                       			baseFile = file("./ext/riftwarp-automatic")) dependsOn(common, riftwarp)

*/

}
