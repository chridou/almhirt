import sbt._
import Keys._

object BuildSettings {
  val buildOrganization = "org.almhirt"
  val buildVersion      = "0.0.109"
  val buildScalaVersion = "2.10.1"

  val buildSettings = Defaults.defaultSettings ++ Seq (
	organization := buildOrganization,
    version      := buildVersion,
    scalaVersion := buildScalaVersion,
    scalacOptions ++= Seq("-unchecked", "-deprecation", "-feature"),	
	publishTo := Some(Resolver.url("LocalRepo", url("http://10.20.0.85:8081/artifactory/libs-local"))(Resolver.ivyStylePatterns)),
	publishMavenStyle := false,
    credentials += Credentials(Path.userHome / ".ivy2" / ".credentials"))
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
	lazy val scala_reflect = "org.scala-lang" % "scala-reflect" % "2.10.0"

	lazy val jodatime    = "joda-time" % "joda-time" % "2.1" % "compile"
	lazy val jodaconvert    = "org.joda" % "joda-convert" % "1.1" % "compile"
	lazy val scalaz       = "org.scalaz" %% "scalaz-core" % "7.0.0-M9" % "compile"
	
	lazy val akka_actor  = "com.typesafe.akka" %% "akka-actor" % "2.1.0"

	lazy val slick  = "com.typesafe.slick" %% "slick" % "1.0.0"

	lazy val apache_codecs = "commons-codec" % "commons-codec" % "1.6" 

    lazy val logback = "ch.qos.logback" % "logback-classic" % "1.0.11" % "compile"

	
    lazy val scalatest = "org.scalatest" % "scalatest_2.10" % "1.9.1" % "test"	
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
	  libraryDependencies += scalatest
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
	  libraryDependencies += scalatest
  )
}


trait UnfilteredBuild {
  import Dependencies._
  import Resolvers._
  def unfilteredProject(name: String, baseFile: java.io.File) = 
  	Project(id = name, base = baseFile, settings = BuildSettings.buildSettings).settings(
//	  libraryDependencies += "net.databinder" %% "unfiltered-netty" % "0.6.7",
	  libraryDependencies += "net.databinder" %% "unfiltered-netty-server" % "0.6.7",
  	  resolvers += typesafeRepo,
  	  resolvers += sonatypeReleases)
  
}

trait ClientDispatchBuild {
  import Dependencies._
  import Resolvers._
  def clientDispatchProject(name: String, baseFile: java.io.File) = 
  	Project(id = name, base = baseFile, settings = BuildSettings.buildSettings).settings(
	  libraryDependencies += "net.databinder.dispatch" %% "dispatch-core" % "0.9.5",
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
	with CoreExtRiftwarpBuild 
	with CoreExtSlickBuild 
	with RiftWarpBuild 
	with RiftWarpAutomaticBuild 
	with RiftWarpExtLiftJsonBuild 
	with UnfilteredBuild 
	with ClientDispatchBuild 
	with AppBuild with DocItBuild {
  lazy val root = Project(	id = "almhirt",
				settings = BuildSettings.buildSettings ++ Unidoc.settings,
	                        base = file(".")) aggregate(common, core, coreExtRiftwarp, slickExtensions, app, docit, riftwarp, riftwarpAutomatic, unfiltered, clientDispatch)
	
  lazy val common = commonProject(	name = "almhirt-common",
                       			baseFile = file("almhirt-common"))

  lazy val core = coreProject(	name = "almhirt-core",
	                       		baseFile = file("almhirt-core")) dependsOn(common % "compile; test->compile")

  lazy val coreExtRiftwarp = coreExtRiftWarpProject(	name = "almhirt-ext-core-riftwarp",
	                       		baseFile = file("./ext/core/almhirt-ext-core-riftwarp")) dependsOn(common, core % "compile; test->test", riftwarp)
								

  lazy val slickExtensions = slickExtProject(	name = "almhirt-ext-core-slick",
                       			baseFile = file("./ext/core/almhirt-ext-core-slick")) dependsOn(core, riftwarp % "test->test", coreExtRiftwarp % "test->test")


  lazy val riftwarp = riftwarpProject(	name = "riftwarp",
                       			baseFile = file("riftwarp")) dependsOn(common)

  lazy val riftwarpAutomatic = riftwarpAutomaticProject(	name = "riftwarp-automatic",
                       			baseFile = file("./ext/riftwarp/riftwarp-automatic")) dependsOn(common, riftwarp)

 // lazy val riftwarpExtLiftJson = riftwarpExtLiftJsonProject(	name = "riftwarp-ext-liftjson",
 //                      			baseFile = file("./ext/riftwarp/riftwarp-ext-liftjson")) dependsOn(riftwarp)


  lazy val docit = docitProject(	name = "almhirt-docit",
                       			baseFile = file("almhirt-docit")) dependsOn(common)

								
 lazy val unfiltered = unfilteredProject(	name = "almhirt-ext-core-unfiltered",
	                       				baseFile = file("./ext/core/almhirt-ext-core-unfiltered")) dependsOn(core, riftwarp)

 lazy val clientDispatch = clientDispatchProject(	name = "almhirt-ext-client-dispatch",
	                       				baseFile = file("./ext/client/almhirt-ext-client-dispatch")) dependsOn(core, riftwarp)


 lazy val app = appProject(	name = "almhirt-app",
	                       	baseFile = file("almhirt-app")) dependsOn(core)										
}
