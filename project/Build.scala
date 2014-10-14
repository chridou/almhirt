import sbt._
import Keys._
import sbtrelease._
import ReleasePlugin._
import ReleaseKeys._
import sbtunidoc.Plugin._
import sbtunidoc.Plugin.UnidocKeys._

object BuildSettings {
  val buildOrganization = "org.almhirt"
  val buildScalaVersion = "2.11.3"

  val akkaVersion = "2.3.+"
  val akkaStreamsVersion = "0.7"
  val scalatestVersion = "2.2.+"
  val sprayVersion = "1.3.+"
  val reactiveMongoVersion = "0.10.5.0.akka23"
   
  val buildSettings = Defaults.defaultSettings ++ releaseSettings ++ Seq (
	organization := buildOrganization,
  scalaVersion := buildScalaVersion,
  resolvers += "Typesafe Repo" at "http://repo.typesafe.com/typesafe/releases/",
  resolvers += "Sonatype Releases" at "https://oss.sonatype.org/content/repositories/releases/",
  scalacOptions ++= Seq("-unchecked", "-deprecation", "-feature"),

  releaseProcess <<= thisProjectRef apply { ref =>
    import ReleaseStateTransformations._
      Seq[ReleaseStep](
        checkSnapshotDependencies,              
        inquireVersions,                       
        //runTest,                              
        setReleaseVersion,                   
        commitReleaseVersion,               
        tagRelease,                        
        publishArtifacts,                 
        setNextVersion,                  
        commitNextVersion,              
        pushChanges)
    }
  )
}

object Resolvers {
  val sonatypeSnapshots = "Sonatype snapshots" at "https://oss.sonatype.org/content/repositories/snapshots/" 
  val typesafeSnapshot  = "Typesafe Snapshots Repository" at "http://repo.typesafe.com/typesafe/snapshots/"
  val scalatools = "Scala Tools" at "https://oss.sonatype.org/content/groups/scala-tools/"
  val sprayRepo = "Spray Repo" at "http://repo.spray.io"
}

object Dependencies {
	lazy val scala_reflect = "org.scala-lang" % "scala-reflect" % BuildSettings.buildScalaVersion

	lazy val jodatime    = "joda-time" % "joda-time" % "2.+" % "compile"
	lazy val jodaconvert    = "org.joda" % "joda-convert" % "1.+" % "compile"
	lazy val scalaz       = "org.scalaz" %% "scalaz-core" % "7.1.+" % "provided"

	lazy val play2_iteratees   = "com.typesafe.play" %% "play-iteratees" % "2.3.+" % "provided"
	
	lazy val akka_actor  = "com.typesafe.akka" %% "akka-actor" % BuildSettings.akkaVersion % "provided"
	lazy val akka_agent  = "com.typesafe.akka" %% "akka-agent" % BuildSettings.akkaVersion % "provided"
	lazy val akka_streams  = "com.typesafe.akka" %% "akka-stream-experimental" % BuildSettings.akkaStreamsVersion % "provided"

	lazy val apache_codecs = "commons-codec" % "commons-codec" % "1.+" 

	lazy val spray_routing = "io.spray" %% "spray-routing" % BuildSettings.sprayVersion % "provided"
	lazy val spray_testkit =  "io.spray" %% "spray-testkit" % BuildSettings.sprayVersion % "test"
	lazy val spray_client = "io.spray" %% "spray-client" % BuildSettings.sprayVersion % "provided"
	lazy val spray_httpx = "io.spray" %% "spray-httpx" % BuildSettings.sprayVersion % "provided"

  lazy val logback = "ch.qos.logback" % "logback-classic" % "1.0.+" % "provided"
	lazy val typesafe_config = "com.typesafe" % "config" % "1.2.+" % "provided"

	
  lazy val scalatest = "org.scalatest" %% "scalatest" % BuildSettings.scalatestVersion % "test"	
	lazy val akka_testkit = "com.typesafe.akka" %% "akka-testkit" % BuildSettings.akkaVersion % "test"

}

trait CommonBuild {
  import Dependencies._
  import Resolvers._
  def commonProject(name: String, baseFile: java.io.File) = 
  	Project(id = name, base = baseFile, settings = BuildSettings.buildSettings).settings(
	  libraryDependencies += jodatime,
	  libraryDependencies += jodaconvert,
	  libraryDependencies += apache_codecs,
	  libraryDependencies += typesafe_config,
	  libraryDependencies += scalaz,
	  libraryDependencies += scalatest
  )
}

trait HttpxSprayBuild {
  import Dependencies._
  import Resolvers._
  def httpxSprayProject(name: String, baseFile: java.io.File) = 
  	Project(id = name, base = baseFile, settings = BuildSettings.buildSettings).settings(
  	  resolvers += sprayRepo,
	  libraryDependencies += jodatime,
	  libraryDependencies += jodaconvert,
	  libraryDependencies += akka_actor,
	  libraryDependencies += spray_httpx,
	  libraryDependencies += typesafe_config,
	  libraryDependencies += scalaz,
	  libraryDependencies += scalatest
  )
}

trait CorexSprayClientBuild {
  import Dependencies._
  import Resolvers._
  def corexSprayClientProject(name: String, baseFile: java.io.File) = 
  	Project(id = name, base = baseFile, settings = BuildSettings.buildSettings).settings(
  	  resolvers += sprayRepo,
	  libraryDependencies += jodatime,
	  libraryDependencies += jodaconvert,
	  libraryDependencies += akka_actor,
	  libraryDependencies += akka_streams,
	  libraryDependencies += spray_httpx,
	  libraryDependencies += spray_client,
	  libraryDependencies += typesafe_config,
	  libraryDependencies += scalaz,
	  libraryDependencies += scalatest
  )
}

trait HttpxSprayServiceBuild {
  import Dependencies._
  import Resolvers._
  def httpxSprayServiceProject(name: String, baseFile: java.io.File) = 
  	Project(id = name, base = baseFile, settings = BuildSettings.buildSettings).settings(
  	  resolvers += sprayRepo,
	  libraryDependencies += jodatime,
	  libraryDependencies += jodaconvert,
	  libraryDependencies += akka_actor,
	  libraryDependencies += spray_httpx,
	  libraryDependencies += spray_routing,
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
	  libraryDependencies += jodatime,
	  libraryDependencies += jodaconvert,
	  libraryDependencies += scalaz,
	  libraryDependencies += play2_iteratees,
	  libraryDependencies += akka_streams,
	  libraryDependencies += akka_actor,
	  libraryDependencies += logback,
	  libraryDependencies += akka_testkit,
	  libraryDependencies += scalatest
  )
}

trait CorexMongoBuild {
  import Dependencies._
  import Resolvers._
  def corexMongoProject(name: String, baseFile: java.io.File) = 
  	Project(id = name, base = baseFile, settings = BuildSettings.buildSettings).settings(
	  resolvers += sonatypeSnapshots,
	  libraryDependencies += jodatime,
	  libraryDependencies += jodaconvert,
	  libraryDependencies += scalaz,
	  libraryDependencies += akka_actor,
	  libraryDependencies += akka_streams,
	  libraryDependencies += play2_iteratees,
	  libraryDependencies += typesafe_config,
	  libraryDependencies += "org.reactivemongo" %% "reactivemongo" % BuildSettings.reactiveMongoVersion % "provided"
		exclude("ch.qos.logback", "logback-core")
		exclude("ch.qos.logback", "logback-classic"),
 	  libraryDependencies += scalatest
  )
}

trait CorexSprayServiceBuild {
  import Dependencies._
  import Resolvers._
  def corexSprayServiceProject(name: String, baseFile: java.io.File) = {
 	Project(id = name, base = baseFile, settings = BuildSettings.buildSettings).settings(
	  resolvers += sprayRepo,
	  libraryDependencies += scalaz,
	  libraryDependencies += akka_actor,
	  libraryDependencies += akka_streams,
	  libraryDependencies += play2_iteratees,
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
	  libraryDependencies += scala_reflect,
	  libraryDependencies += jodatime,
	  libraryDependencies += jodaconvert,
	  libraryDependencies += apache_codecs,
	  libraryDependencies += scalaz,
//	  libraryDependencies += "com.chuusai" %% "shapeless" % "1.2.4",
	  libraryDependencies += scalatest
  )
}

trait RiftWarpHttpSprayBuild {
  import Dependencies._
  import Resolvers._
  def riftwarpHttpSprayProject(name: String, baseFile: java.io.File) = 
  	Project(id = name, base = baseFile, settings = BuildSettings.buildSettings).settings(
	  libraryDependencies += scala_reflect,
	  libraryDependencies += jodatime,
	  libraryDependencies += jodaconvert,
    libraryDependencies += akka_actor,
	  libraryDependencies += apache_codecs,
	  libraryDependencies += spray_httpx,
	  libraryDependencies += scalaz,
	  libraryDependencies += scalatest
  )
}

trait RiftWarpMongoExtBuild {
  import Dependencies._
  import Resolvers._
  def riftwarpMongoExtProject(name: String, baseFile: java.io.File) = 
  	Project(id = name, base = baseFile, settings = BuildSettings.buildSettings).settings(
	  resolvers += sonatypeSnapshots,
	  libraryDependencies += scala_reflect,
	  libraryDependencies += jodatime,
	  libraryDependencies += jodaconvert,
	  libraryDependencies += apache_codecs,
	  libraryDependencies += "org.reactivemongo" %% "reactivemongo-bson" % BuildSettings.reactiveMongoVersion % "provided"     
		exclude("ch.qos.logback", "logback-core")
		exclude("ch.qos.logback", "logback-classic"),
	  libraryDependencies += scalaz,
	  libraryDependencies += scalatest
  )
}

trait RiftWarpAutomaticBuild {
  import Dependencies._
  import Resolvers._
  def riftwarpAutomaticProject(name: String, baseFile: java.io.File) = 
  	Project(id = name, base = baseFile, settings = BuildSettings.buildSettings).settings(
	  libraryDependencies += jodatime,
	  libraryDependencies += jodaconvert,
	  libraryDependencies += apache_codecs,
	  libraryDependencies += scalaz,
	  libraryDependencies += scalatest
  )
}

object AlmHirtBuild extends Build 
	with CommonBuild 
	with HttpxSprayBuild
	with CorexSprayClientBuild
	with HttpxSprayServiceBuild
	with CoreBuild 
	with CorexMongoBuild 
	with CorexSprayServiceBuild 
	with RiftWarpBuild 
	with RiftWarpHttpSprayBuild
	with RiftWarpMongoExtBuild 
	with RiftWarpAutomaticBuild {
  lazy val root = Project(
    id = "almhirt",
		settings = BuildSettings.buildSettings,
	  base = file("."))
      .settings(unidocSettings: _*)
      .aggregate(	common, 
									httpxSpray, 
									corexSprayClient, 
									httpxSprayService, 
									core, 
									mongoExtensions, 
									corexSprayService, 
									riftwarp, 
									riftwarpHttpSpray, 
									riftwarpMongoProject)	
  lazy val common = commonProject(	name = "almhirt-common",
                       			baseFile = file("almhirt-common"))

  lazy val httpxSpray = httpxSprayProject(	name = "almhirt-httpx-spray",
                       			baseFile = file("./ext/almhirt-httpx-spray")) dependsOn(common)

  lazy val corexSprayClient = corexSprayClientProject(	name = "almhirt-corex-spray-client",
                       			baseFile = file("./ext/almhirt-corex-spray-client")) dependsOn(common, httpxSpray, core)
								
  lazy val httpxSprayService = httpxSprayServiceProject(	name = "almhirt-httpx-spray-service",
                       			baseFile = file("./ext/almhirt-httpx-spray-service")) dependsOn(common, httpxSpray)
								
  lazy val core = coreProject(	name = "almhirt-core",
		baseFile = file("almhirt-core")) dependsOn(	common % "compile; test->compile; test->test"/*, 
																								corexRiftwarp % "test",
																								riftwarp % "test->test"*/)

  lazy val mongoExtensions = corexMongoProject(	name = "almhirt-corex-mongo",
                       			baseFile = file("./ext/almhirt-corex-mongo")) dependsOn(core, riftwarp % "test->test", riftwarpMongoProject % "test")
								
  lazy val corexSprayService = corexSprayServiceProject(	name = "almhirt-corex-spray-service",
	                       				baseFile = file("./ext/almhirt-corex-spray-service")) dependsOn(common, httpxSprayService, core, riftwarp % "test")
										
  lazy val riftwarp = riftwarpProject(	name = "riftwarp",
                       			baseFile = file("riftwarp")) dependsOn(common)
								
  lazy val riftwarpMongoProject = riftwarpMongoExtProject(	name = "riftwarpx-mongo",
                       			baseFile = file("./ext/riftwarpx-mongo")) dependsOn(common, riftwarp)

	lazy val riftwarpHttpSpray = riftwarpHttpSprayProject(	name = "riftwarpx-http-spray",
                       			baseFile = file("./ext/riftwarpx-http-spray")) dependsOn(common, riftwarp, httpxSpray)
								
/*
  lazy val riftwarpAutomatic = riftwarpAutomaticProject(	name = "riftwarp-automatic",
                       			baseFile = file("./ext/riftwarp-automatic")) dependsOn(common, riftwarp)

*/
								
}
