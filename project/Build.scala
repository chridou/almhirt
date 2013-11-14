import sbt._
import Keys._

object BuildSettings {
  val buildOrganization = "org.almhirt"
  val buildVersion      = "0.5.130"
  val buildScalaVersion = "2.10.2"

  val akkaVersion = "2.2.+"
  val scalatestVersion = "2.0.M5b"
  val sprayVersion = "1.2-20130822"
   
  val buildSettings = Defaults.defaultSettings ++ Seq (
	organization := buildOrganization,
    version      := buildVersion,
    scalaVersion := buildScalaVersion,
    scalacOptions ++= Seq("-unchecked", "-deprecation", "-feature"))
}

object Resolvers {
  val sonatypeReleases = "Sonatype" at "https://oss.sonatype.org/content/repositories/releases/" 
  val sonatypeSnapshots = "Sonatype snapshots" at "http://oss.sonatype.org/content/repositories/snapshots/" 
  val typesafeRepo  = "Typesafe Repo"   at "http://repo.typesafe.com/typesafe/releases/"
  val typesafeSnapshot  = "Typesafe Snapshots Repository" at "http://repo.typesafe.com/typesafe/snapshots/"
  val scalatools = "Scala Tools" at "https://oss.sonatype.org/content/groups/scala-tools/"
  val scalatoolsSnapshots = "Scala Tools Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots/"
}

object Dependencies {
	lazy val scala_reflect = "org.scala-lang" % "scala-reflect" % "2.10.2"

	lazy val jodatime    = "joda-time" % "joda-time" % "2.1" % "compile"
	lazy val jodaconvert    = "org.joda" % "joda-convert" % "1.1" % "compile"
	lazy val scalaz       = "org.scalaz" %% "scalaz-core" % "7.0.+" % "provided"

	lazy val play2_iteratees   = "com.typesafe.play" %% "play-iteratees" % "2.2.+" % "provided"
	
	lazy val akka_actor  = "com.typesafe.akka" %% "akka-actor" % BuildSettings.akkaVersion % "provided"
	lazy val akka_agent  = "com.typesafe.akka" %% "akka-agent" % BuildSettings.akkaVersion % "provided"

	lazy val slick  = "com.typesafe.slick" %% "slick" % "1.0.+" % "provided"

	lazy val apache_codecs = "commons-codec" % "commons-codec" % "1.6" 

	lazy val spray_routing = "io.spray" % "spray-routing" % BuildSettings.sprayVersion % "provided"
	lazy val spray_json =  "io.spray" %%  "spray-json" % "1.2.5"
	lazy val spray_testkit =  "io.spray" % "spray-testkit" % BuildSettings.sprayVersion % "test"
	lazy val spray_client = "io.spray" % "spray-client" % BuildSettings.sprayVersion % "provided"
	
    lazy val logback = "ch.qos.logback" % "logback-classic" % "1.0.+" % "provided"
	lazy val typesafe_config = "com.typesafe" % "config" % "1.0.+" % "provided"

	
    lazy val scalatest = "org.scalatest" % "scalatest_2.10" % BuildSettings.scalatestVersion % "test"	
	lazy val akka_testkit = "com.typesafe.akka" %% "akka-testkit" % BuildSettings.akkaVersion % "test"

}

trait CommonBuild {
  import Dependencies._
  import Resolvers._
  def commonProject(name: String, baseFile: java.io.File) = 
  	Project(id = name, base = baseFile, settings = BuildSettings.buildSettings).settings(
  	  resolvers += typesafeRepo,
  	  resolvers += sonatypeReleases,
	  libraryDependencies += jodatime,
	  libraryDependencies += jodaconvert,
	  libraryDependencies += typesafe_config,
	  libraryDependencies += scalaz,
	  libraryDependencies += scalatest
  )
}

trait CoreBuild {
  import Dependencies._
  import Resolvers._
  def coreProject(name: String, baseFile: java.io.File) = 
  	Project(id = name, base = baseFile, settings = BuildSettings.buildSettings).settings(
  	  resolvers += typesafeRepo,
  	  resolvers += sonatypeReleases,
	  libraryDependencies += jodatime,
	  libraryDependencies += jodaconvert,
	  libraryDependencies += scalaz,
	  libraryDependencies += play2_iteratees,
	  libraryDependencies += akka_actor,
	  libraryDependencies += logback,
	  libraryDependencies += akka_testkit,
	  libraryDependencies += scalatest
  )
}

trait CoreTestingBuild {
  import Dependencies._
  import Resolvers._
  def coreTestingProject(name: String, baseFile: java.io.File) = 
  	Project(id = name, base = baseFile, settings = BuildSettings.buildSettings).settings(
  	  resolvers += typesafeRepo,
  	  resolvers += sonatypeReleases,
	  libraryDependencies += jodatime,
	  libraryDependencies += jodaconvert,
	  libraryDependencies += scalaz,
	  libraryDependencies += akka_actor,
	  libraryDependencies += logback,
	  libraryDependencies += akka_testkit,
	  libraryDependencies += scalatest
  )
}
trait TestKitBuild {
  import Dependencies._
  import Resolvers._
  def testKitProject(name: String, baseFile: java.io.File) = 
  	Project(id = name, base = baseFile, settings = BuildSettings.buildSettings).settings(
  	  resolvers += typesafeRepo,
  	  resolvers += sonatypeReleases,
	  libraryDependencies += jodatime,
	  libraryDependencies += jodaconvert,
	  libraryDependencies += scalaz,
	  libraryDependencies += play2_iteratees,
	  libraryDependencies += akka_actor,
	  libraryDependencies += logback,
	  libraryDependencies += "com.typesafe.akka" %% "akka-testkit" % BuildSettings.akkaVersion % "compile",
	  libraryDependencies += "org.scalatest" % "scalatest_2.10" % BuildSettings.scalatestVersion % "compile"
  )
}
trait CorexRiftwarpBuild {
  import Dependencies._
  import Resolvers._
  def corexRiftWarpProject(name: String, baseFile: java.io.File) = 
  	Project(id = name, base = baseFile, settings = BuildSettings.buildSettings).settings(
  	  resolvers += typesafeRepo,
  	  resolvers += sonatypeReleases,
	  libraryDependencies += jodatime,
	  libraryDependencies += jodaconvert,
	  libraryDependencies += scalaz,
	  libraryDependencies += akka_actor,
	  libraryDependencies += scalatest
  )
}

trait CorexSlickBuild {
  import Dependencies._
  import Resolvers._
  def corexSlickProject(name: String, baseFile: java.io.File) = 
  	Project(id = name, base = baseFile, settings = BuildSettings.buildSettings).settings(
  	  resolvers += typesafeRepo,
  	  resolvers += sonatypeReleases,
	  libraryDependencies += jodatime,
	  libraryDependencies += jodaconvert,
	  libraryDependencies += scalaz,
	  libraryDependencies += akka_actor,
	  libraryDependencies += play2_iteratees,
	  libraryDependencies += slick,
	  libraryDependencies += typesafe_config,
	  libraryDependencies += "com.h2database" % "h2" % "1.3.168" % "test",
	  libraryDependencies += scalatest
  )
}

trait CorexMongoBuild {
  import Dependencies._
  import Resolvers._
  def corexMongoProject(name: String, baseFile: java.io.File) = 
  	Project(id = name, base = baseFile, settings = BuildSettings.buildSettings).settings(
  	  resolvers += typesafeRepo,
  	  resolvers += sonatypeReleases,
	  resolvers += "Sonatype Snapshots" at "http://oss.sonatype.org/content/repositories/snapshots/",
	  libraryDependencies += jodatime,
	  libraryDependencies += jodaconvert,
	  libraryDependencies += scalaz,
	  libraryDependencies += akka_actor,
	  libraryDependencies += play2_iteratees,
	  libraryDependencies += typesafe_config,
	  libraryDependencies += "org.reactivemongo" %% "reactivemongo" % "0.10.0-SNAPSHOT" % "provided"
		exclude("ch.qos.logback", "logback-core")
		exclude("ch.qos.logback", "logback-classic"),
 	  libraryDependencies += scalatest
  )
}

trait CorexSprayBuild {
  import Dependencies._
  import Resolvers._
  def corexSprayProject(name: String, baseFile: java.io.File) = {
 	Project(id = name, base = baseFile, settings = BuildSettings.buildSettings).settings(
	  //resolvers += "spray repo" at "http://repo.spray.io",
	  resolvers += "spray nightlies repo" at "http://nightlies.spray.io",
	  libraryDependencies += scalaz,
	  libraryDependencies += akka_actor,
	  libraryDependencies += spray_client,
	  libraryDependencies += spray_routing,
	  libraryDependencies += spray_testkit,
	  libraryDependencies += scalatest
	  )
	  }
  
}

trait RiftWarpBuild {
  import Dependencies._
  import Resolvers._
  def riftwarpProject(name: String, baseFile: java.io.File) = 
  	Project(id = name, base = baseFile, settings = BuildSettings.buildSettings).settings(
  	  resolvers += sonatypeReleases,
	  libraryDependencies += scala_reflect,
	  libraryDependencies += jodatime,
	  libraryDependencies += jodaconvert,
	  libraryDependencies += apache_codecs,
	  libraryDependencies += scalaz,
	  libraryDependencies += "com.chuusai" %% "shapeless" % "1.2.4",
	  libraryDependencies += scalatest
  )
}

trait RiftWarpMongoExtBuild {
  import Dependencies._
  import Resolvers._
  def riftwarpMongoExtProject(name: String, baseFile: java.io.File) = 
  	Project(id = name, base = baseFile, settings = BuildSettings.buildSettings).settings(
  	  resolvers += sonatypeReleases,
	  resolvers += "Sonatype Snapshots" at "http://oss.sonatype.org/content/repositories/snapshots/",
	  libraryDependencies += scala_reflect,
	  libraryDependencies += jodatime,
	  libraryDependencies += jodaconvert,
	  libraryDependencies += apache_codecs,
	  libraryDependencies += "org.reactivemongo" %% "reactivemongo-bson" % "0.10.0-SNAPSHOT" % "provided"     
		exclude("ch.qos.logback", "logback-core")
		exclude("ch.qos.logback", "logback-classic"),
	  libraryDependencies += scalaz,
	  libraryDependencies += scalatest
  )
}

trait RiftWarpSprayJsonExtBuild {
  import Dependencies._
  import Resolvers._
  def riftwarpSprayJsonExtProject(name: String, baseFile: java.io.File) = 
  	Project(id = name, base = baseFile, settings = BuildSettings.buildSettings).settings(
	  resolvers += "spray repo" at "http://repo.spray.io",
	  libraryDependencies += scala_reflect,
	  libraryDependencies += jodatime,
	  libraryDependencies += jodaconvert,
	  libraryDependencies += apache_codecs,
	  libraryDependencies += scalaz,
	  libraryDependencies += spray_json,
	  libraryDependencies += scalatest
  )
}

trait RiftWarpAutomaticBuild {
  import Dependencies._
  import Resolvers._
  def riftwarpAutomaticProject(name: String, baseFile: java.io.File) = 
  	Project(id = name, base = baseFile, settings = BuildSettings.buildSettings).settings(
  	  resolvers += sonatypeReleases,
	  libraryDependencies += jodatime,
	  libraryDependencies += jodaconvert,
	  libraryDependencies += apache_codecs,
	  libraryDependencies += scalaz,
	  libraryDependencies += scalatest
  )
}

object AlmHirtBuild extends Build 
	with CommonBuild 
	with CoreBuild 
	with CoreTestingBuild 
	with TestKitBuild 
	with CorexRiftwarpBuild 
	with CorexSlickBuild 
	with CorexMongoBuild 
	with CorexSprayBuild 
	with RiftWarpBuild 
	with RiftWarpMongoExtBuild 
	with RiftWarpSprayJsonExtBuild 
	with RiftWarpAutomaticBuild {
  lazy val root = Project(	id = "almhirt",
				settings = BuildSettings.buildSettings ++ Unidoc.settings,
	                        base = file(".")) aggregate(common, core, coreTesting, testKit, corexRiftwarp, slickExtensions, mongoExtensions, corexSpray, riftwarp, riftwarpMongoProject, riftwarpSprayProject)
	
  lazy val common = commonProject(	name = "almhirt-common",
                       			baseFile = file("almhirt-common"))

  lazy val core = coreProject(	name = "almhirt-core",
	                       		baseFile = file("almhirt-core")) dependsOn(common % "compile; test->compile")

  lazy val coreTesting = coreTestingProject(	name = "almhirt-testing-core",
	                       		baseFile = file("./test/almhirt-testing-core")) dependsOn(core, testKit)
								
  lazy val testKit = testKitProject(	name = "almhirt-testkit",
	                       		baseFile = file("almhirt-testkit")) dependsOn(core % "compile -> compile", riftwarp % "compile -> compile", corexRiftwarp % "compile -> compile")

  lazy val corexRiftwarp = corexRiftWarpProject(	name = "almhirt-corex-riftwarp",
	                       		baseFile = file("./ext/almhirt-corex-riftwarp")) dependsOn(common, core % "compile; test->test", riftwarp)
								

  lazy val slickExtensions = corexSlickProject(	name = "almhirt-corex-slick",
                       			baseFile = file("./ext/almhirt-corex-slick")) dependsOn(core, riftwarp % "test->test", corexRiftwarp % "test->test", testKit % "test")

  lazy val mongoExtensions = corexMongoProject(	name = "almhirt-corex-mongo",
                       			baseFile = file("./ext/almhirt-corex-mongo")) dependsOn(core, riftwarp % "test->test", corexRiftwarp % "test->test", riftwarpMongoProject % "test", testKit % "test")
								
  lazy val corexSpray = corexSprayProject(	name = "almhirt-corex-spray",
	                       				baseFile = file("./ext/almhirt-corex-spray")) dependsOn(common, core, riftwarp % "test->test", corexRiftwarp % "test->test", testKit % "test")

  lazy val riftwarp = riftwarpProject(	name = "riftwarp",
                       			baseFile = file("riftwarp")) dependsOn(common)
								
  lazy val riftwarpMongoProject = riftwarpMongoExtProject(	name = "riftwarpx-mongo",
                       			baseFile = file("./ext/riftwarpx-mongo")) dependsOn(common, riftwarp)
								
  lazy val riftwarpSprayProject = riftwarpSprayJsonExtProject(	name = "riftwarpx-sprayjson",
                       			baseFile = file("./ext/riftwarpx-sprayjson")) dependsOn(common, riftwarp % "compile->compile;test->test" )
								
/*
  lazy val riftwarpAutomatic = riftwarpAutomaticProject(	name = "riftwarp-automatic",
                       			baseFile = file("./ext/riftwarp-automatic")) dependsOn(common, riftwarp)

*/
								
}
