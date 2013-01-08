import sbt._
import Keys._

object BuildSettings {
  val buildOrganization = "org.almhirt"
  val buildVersion      = "0.0.29-SNAPSHOT"
  val buildScalaVersion = "2.10.0"

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
	lazy val jodatime    = "joda-time" % "joda-time" % "2.1" % "compile"
	lazy val jodaconvert    = "org.joda" % "joda-convert" % "1.1" % "compile"
	lazy val scalaz       = "org.scalaz" %% "scalaz-core" % "7.0.0-M7" % "compile"
	
	lazy val akka_actor  = "com.typesafe.akka" %% "akka-actor" % "2.1.0"

//	lazy val slick  = "com.typesafe" %% "slick" % "0.11.2"

	lazy val apache_codecs = "commons-codec" % "commons-codec" % "1.6" 
	
	lazy val specs2 = "org.specs2" %% "specs2" % "1.13" % "test"
	lazy val akka_testkit = "com.typesafe.akka" %% "akka-testkit" % "2.1.0" % "test"

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
	  libraryDependencies += apache_codecs,
	  libraryDependencies += scalaz,
	  libraryDependencies += specs2
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
	  libraryDependencies += akka_testkit,
	  libraryDependencies += specs2
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
	  libraryDependencies += specs2
  )
}

trait AnormEventLogBuild {
  import Dependencies._
  import Resolvers._
  def anormEventLogProject(name: String, baseFile: java.io.File) = 
  	Project(id = name, base = baseFile, settings = BuildSettings.buildSettings).settings(
  	  resolvers += typesafeRepo,
  	  resolvers += sonatypeReleases,
	  libraryDependencies += jodatime,
	  libraryDependencies += jodaconvert,
	  libraryDependencies += scalaz,
	  libraryDependencies += "play" %% "anorm" % "2.1-12142012",
	  libraryDependencies += "com.h2database" % "h2" % "1.3.168" % "test",
	  libraryDependencies += "postgresql" % "postgresql" % "9.1-901.jdbc4" % "test",
	  libraryDependencies += specs2
  )
}

trait RiftWarpBuild {
  import Dependencies._
  import Resolvers._
  def riftwarpProject(name: String, baseFile: java.io.File) = 
  	Project(id = name, base = baseFile, settings = BuildSettings.buildSettings).settings(
  	  resolvers += sonatypeReleases,
	  libraryDependencies += jodatime,
	  libraryDependencies += jodaconvert,
	  libraryDependencies += apache_codecs,
	  libraryDependencies += scalaz,
	  libraryDependencies += specs2
  )
}

trait RiftWarpExtLiftJsonBuild {
  import Dependencies._
  import Resolvers._
  def riftwarpExtLiftJsonProject(name: String, baseFile: java.io.File) = 
  	Project(id = name, base = baseFile, settings = BuildSettings.buildSettings).settings(
  	  resolvers += sonatypeReleases,
	  libraryDependencies += jodatime,
	  libraryDependencies += jodaconvert,
	  libraryDependencies += "net.liftweb" %% "lift-json" % "2.5-M2",
	  libraryDependencies += apache_codecs,
	  libraryDependencies += scalaz,
	  libraryDependencies += specs2
  )
}

trait DocItBuild {
  import Dependencies._
  import Resolvers._
  def docitProject(name: String, baseFile: java.io.File) = 
  	Project(id = name, base = baseFile, settings = BuildSettings.buildSettings).settings(
  	  resolvers += sonatypeReleases,
	  libraryDependencies += jodatime,
	  libraryDependencies += jodaconvert,
	  libraryDependencies += scalaz,
	  libraryDependencies += specs2
  )
}


trait UnfilteredBuild {
  import Dependencies._
  import Resolvers._
  def unfilteredProject(name: String, baseFile: java.io.File) = 
  	Project(id = name, base = baseFile, settings = BuildSettings.buildSettings).settings(
	  libraryDependencies += "net.databinder" %% "unfiltered-netty" % "0.6.5",
  	  resolvers += typesafeRepo,
  	  resolvers += sonatypeReleases)
  
}

object AlmHirtBuild extends Build with CommonBuild with CoreBuild with CoreExtRiftwarpBuild with AnormEventLogBuild with RiftWarpBuild with RiftWarpExtLiftJsonBuild with UnfilteredBuild with DocItBuild {
  lazy val root = Project(	id = "almhirt",
				settings = BuildSettings.buildSettings,
	                        base = file(".")) aggregate(common, core, anormEventLog, docit, riftwarp, unfiltered)
	
  lazy val common = commonProject(	name = "almhirt-common",
                       			baseFile = file("almhirt-common"))

  lazy val core = coreProject(	name = "almhirt-core",
	                       		baseFile = file("almhirt-core")) dependsOn(common)

  lazy val coreExtRiftwarp = coreExtRiftWarpProject(	name = "almhirt-ext-core-riftwarp",
	                       		baseFile = file("./ext/core/almhirt-ext-core-riftwarp")) dependsOn(common, core, riftwarp)
								

  lazy val anormEventLog = anormEventLogProject(	name = "almhirt-ext-anormeventlog",
                       			baseFile = file("./ext/eventlogs/almhirt-ext-anormeventlog")) dependsOn(core, riftwarp)

  lazy val riftwarp = riftwarpProject(	name = "riftwarp",
                       			baseFile = file("riftwarp")) dependsOn(common)

 // lazy val riftwarpExtLiftJson = riftwarpExtLiftJsonProject(	name = "riftwarp-ext-liftjson",
 //                      			baseFile = file("./ext/riftwarp/riftwarp-ext-liftjson")) dependsOn(riftwarp)


  lazy val docit = docitProject(	name = "almhirt-docit",
                       			baseFile = file("almhirt-docit")) dependsOn(common)

								
 lazy val unfiltered = unfilteredProject(	name = "almhirt-ext-core-unfiltered",
	                       				baseFile = file("./ext/core/almhirt-ext-core-unfiltered")) dependsOn(core, riftwarp)

										
}
