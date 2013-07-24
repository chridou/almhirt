import sbt._
import Keys._

object BuildSettings {
  val buildOrganization = "org.almhirt"
  val buildVersion      = "0.5.4"
  val buildScalaVersion = "2.10.2"

  val akkaVersion = "2.2.0"
  val scalatestVersion = "2.0.M5b"
  
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
	lazy val scalaz       = "org.scalaz" %% "scalaz-core" % "7.0.0" % "compile"
	
	lazy val akka_actor  = "com.typesafe.akka" %% "akka-actor" % BuildSettings.akkaVersion

	lazy val slick  = "com.typesafe.slick" %% "slick" % "1.0.1"

	lazy val apache_codecs = "commons-codec" % "commons-codec" % "1.6" 

    lazy val logback = "ch.qos.logback" % "logback-classic" % "1.0.11" % "compile"

	
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
	  libraryDependencies += akka_actor,
	  libraryDependencies += logback,
	  libraryDependencies += "com.typesafe.akka" %% "akka-testkit" % BuildSettings.akkaVersion % "compile",
	  libraryDependencies += "org.scalatest" % "scalatest_2.10" % BuildSettings.scalatestVersion % "compile"
  )
}
trait CoreExtRiftwarpBuild {
  import Dependencies._
  import Resolvers._
  def coreExtRiftWarpProject(name: String, baseFile: java.io.File) = 
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

trait CoreExtSlickBuild {
  import Dependencies._
  import Resolvers._
  def slickExtProject(name: String, baseFile: java.io.File) = 
  	Project(id = name, base = baseFile, settings = BuildSettings.buildSettings).settings(
  	  resolvers += typesafeRepo,
  	  resolvers += sonatypeReleases,
	  libraryDependencies += jodatime,
	  libraryDependencies += jodaconvert,
	  libraryDependencies += scalaz,
	  libraryDependencies += slick,
	  libraryDependencies += "com.h2database" % "h2" % "1.3.168" % "test",
	  libraryDependencies += "postgresql" % "postgresql" % "9.1-901.jdbc4" % "test",
	  libraryDependencies += scalatest
  )
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

trait AlmhirtExtHttpUnfilteredBuild {
  import Dependencies._
  import Resolvers._
  def almhirtExtHttpUnfilteredProject(name: String, baseFile: java.io.File) = 
  	Project(id = name, base = baseFile, settings = BuildSettings.buildSettings).settings(
  	  resolvers += sonatypeReleases,
	  libraryDependencies += jodatime,
	  libraryDependencies += jodaconvert,
	  libraryDependencies += scalaz,
	  libraryDependencies += "net.databinder" %% "unfiltered-netty-server" % "0.6.7",
	  libraryDependencies += scalatest
  )
}

trait ExtCoreUnfilteredBuild {
  import Dependencies._
  import Resolvers._
  def extCoreUnfilteredProject(name: String, baseFile: java.io.File) = 
  	Project(id = name, base = baseFile, settings = BuildSettings.buildSettings).settings(
	  libraryDependencies += "net.databinder" %% "unfiltered-netty-server" % "0.6.7",
  	  resolvers += typesafeRepo,
  	  resolvers += sonatypeReleases)
  
}

trait AppBuild {
  import Dependencies._
  import Resolvers._
  def appProject(name: String, baseFile: java.io.File) = 
  	Project(id = name, base = baseFile, settings = BuildSettings.buildSettings).settings(
	  libraryDependencies += "org.clapper" %% "argot" % "1.0.0" % "compile",
  	  resolvers += typesafeRepo,
  	  resolvers += sonatypeReleases)
  
}

object AlmHirtBuild extends Build 
	with CommonBuild 
	with CoreBuild 
	with CoreTestingBuild 
	with TestKitBuild 
	with CoreExtRiftwarpBuild 
	with CoreExtSlickBuild 
	with RiftWarpBuild 
	with RiftWarpAutomaticBuild 
	with AlmhirtExtHttpUnfilteredBuild 
	with ExtCoreUnfilteredBuild 
	with AppBuild {
  lazy val root = Project(	id = "almhirt",
				settings = BuildSettings.buildSettings ++ Unidoc.settings,
	                        base = file(".")) aggregate(common, core, coreTesting, testKit, riftwarp, coreExtRiftwarp, slickExtensions)
	
  lazy val common = commonProject(	name = "almhirt-common",
                       			baseFile = file("almhirt-common"))

  lazy val core = coreProject(	name = "almhirt-core",
	                       		baseFile = file("almhirt-core")) dependsOn(common % "compile; test->compile")

  lazy val coreTesting = coreTestingProject(	name = "almhirt-testing-core",
	                       		baseFile = file("./test/almhirt-testing-core")) dependsOn(core, testKit)
								
  lazy val testKit = testKitProject(	name = "almhirt-testkit",
	                       		baseFile = file("almhirt-testkit")) dependsOn(core % "compile -> compile")

  lazy val coreExtRiftwarp = coreExtRiftWarpProject(	name = "almhirt-ext-core-riftwarp",
	                       		baseFile = file("./ext/almhirt-ext-core-riftwarp")) dependsOn(common, core % "compile; test->test", riftwarp)
								

  lazy val slickExtensions = slickExtProject(	name = "almhirt-ext-core-slick",
                       			baseFile = file("./ext/almhirt-ext-core-slick")) dependsOn(core, riftwarp % "test->test", coreExtRiftwarp % "test->test")


  lazy val riftwarp = riftwarpProject(	name = "riftwarp",
                       			baseFile = file("riftwarp")) dependsOn(common)
/*
  lazy val riftwarpAutomatic = riftwarpAutomaticProject(	name = "riftwarp-automatic",
                       			baseFile = file("./ext/riftwarp-automatic")) dependsOn(common, riftwarp)

  lazy val almhirtExtHttpUnfiltered = almhirtExtHttpUnfilteredProject(	name = "almhirt-ext-http-unfiltered",
                       			baseFile = file("./ext/almhirt-ext-http-unfiltered")) dependsOn(common)


  lazy val docit = docitProject(	name = "almhirt-docit",
                       			baseFile = file("almhirt-docit")) dependsOn(common)

								
 lazy val almhirtExtCoreUnfiltered = extCoreUnfilteredProject(	name = "almhirt-ext-core-unfiltered",
	                       				baseFile = file("./ext/almhirt-ext-core-unfiltered")) dependsOn(core, almhirtExtHttpUnfiltered)

 val app = appProject(	name = "almhirt-app",
	                       	baseFile = file("almhirt-app")) dependsOn(core) */										
}
