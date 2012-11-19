import sbt._
import Keys._

object BuildSettings {
  val buildOrganization = "org.almhirt"
  val buildVersion      = "0.0.20-SNAPSHOT"
  val buildScalaVersion = "2.9.2"

  val buildSettings = Defaults.defaultSettings ++ Seq (
	organization := buildOrganization,
    version      := buildVersion,
    scalaVersion := buildScalaVersion,
    scalacOptions ++= Seq("-unchecked", "-deprecation"))
}

object Resolvers {
  val sonatypeReleases = "Sonatype" at "http://oss.sonatype.org/content/repositories/releases/" 
  val sonatypeSnapshots = "Sonatype snapshots" at "http://oss.sonatype.org/content/repositories/snapshots/" 
  val typesafeRepo  = "Typesafe Repo"   at "http://repo.typesafe.com/typesafe/releases/"
  val typesafeSnapshot  = "Typesafe Snapshots Repository" at "http://repo.typesafe.com/typesafe/snapshots/"
  val scalatools = "Scala Tools" at "https://oss.sonatype.org/content/groups/scala-tools/"
  val scalatoolsSnapshots = "Scala Tools Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots/"
}

object Dependencies {
	lazy val jodatime    = "joda-time" % "joda-time" % "2.1" % "compile"
	lazy val jodaconvert    = "org.joda" % "joda-convert" % "1.1" % "compile"
	lazy val scalaz       = "org.scalaz" %% "scalaz-core" % "7.0.0-M2" % "compile"
	
	lazy val akka_actor  = "com.typesafe.akka" % "akka-actor" % "2.0.3"

	lazy val unfiltered = "net.databinder" %% "unfiltered-netty" % "0.6.3"

	lazy val casbah  = "org.mongodb" %% "casbah" % "2.3.0"

	lazy val apache_codecs = "commons-codec" % "commons-codec" % "1.6" 
	
	lazy val specs2 = "org.specs2" %% "specs2" % "1.11" % "test"
	lazy val akka_testkit = "com.typesafe.akka" % "akka-testkit" % "2.0.3" % "test"

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
	  libraryDependencies += specs2,
	  libraryDependencies += akka_testkit
  )
  
}

/*
trait UnfilteredBuild {
  import Dependencies._
  import Resolvers._
  def unfilteredProject(name: String, baseFile: java.io.File) = 
  	Project(id = name, base = baseFile, settings = BuildSettings.buildSettings).settings(
	  libraryDependencies += unfiltered,
  	  resolvers += typesafeRepo,
  	  resolvers += sonatypeReleases)
  
}
*/

object AlmHirtBuild extends Build with CommonBuild with CoreBuild with DocItBuild with RiftWarpBuild{
  lazy val root = Project(	id = "almhirt",
	                        base = file(".")) aggregate(common, core, riftwarp, docit)
	
  lazy val common = commonProject(	name = "almhirt-common",
                       			baseFile = file("almhirt-common"))

  lazy val core = coreProject(	name = "almhirt-core",
	                       		baseFile = file("almhirt-core")) dependsOn(common, riftwarp)

/*
  lazy val xtractnduce = xtractnduceProject(	name = "almhirt-xtractnduce",
                       			baseFile = file("almhirt-xtractnduce")) dependsOn(common) */

  lazy val riftwarp = riftwarpProject(	name = "almhirt-riftwarp",
                       			baseFile = file("almhirt-riftwarp")) dependsOn(common)


  lazy val docit = docitProject(	name = "almhirt-docit",
                       			baseFile = file("almhirt-docit")) dependsOn(common)

/*								
  lazy val commonMongo = commonMongoProject(	name = "almhirt-xtractnduce-mongo",
                       			baseFile = file("almhirt-xtractnduce-mongo")) dependsOn(common, xtractnduce)

  lazy val unfiltered = unfilteredProject(	name = "almhirt-unfiltered",
	                       				baseFile = file("almhirt-unfiltered")) dependsOn(common)*/

										
}
