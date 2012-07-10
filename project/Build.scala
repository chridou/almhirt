import sbt._
import Keys._

object BuildSettings {
  val buildOrganization = "org.almhirt"
  val buildVersion      = "0.0.1-SNAPSHOT"
  val buildScalaVersion = "2.9.2"

  val buildSettings = Defaults.defaultSettings ++ Seq (
    organization := buildOrganization,
    version      := buildVersion,
    scalaVersion := buildScalaVersion,
    scalacOptions ++= Seq("-unchecked", "-deprecation"))
}

object Resolvers {
  val typesafeRepo  = "Typesafe Repo"   at "http://repo.typesafe.com/typesafe/releases/"
  val typesafeSnapshot  = "Typesafe Snapshots Repository" at "http://repo.typesafe.com/typesafe/snapshots/"
  val scalatoolsSnapshots = "Scala Tools Snapshots" at "http://scala-tools.org/repo-snapshots/"
}

object Dependencies {
	lazy val jodatime    = "joda-time" % "joda-time" % "2.1" % "compile"
	lazy val jodaconvert    = "org.joda" % "joda-convert" % "1.1" % "compile"
	lazy val scalaz       = "org.scalaz" %% "scalaz-core" % "7.0-SNAPSHOT" % "compile"
	
	lazy val akka_actor  = "com.typesafe.akka" % "akka-actor" % "2.0.2"

	lazy val unfiltered = "net.databinder" %% "unfiltered-netty" % "0.6.3"

	
	lazy val specs2 = "org.specs2" %% "specs2" % "1.11" % "test"
	lazy val akka_testkit = "com.typesafe.akka" % "akka-testkit" % "2.0.2"

}

trait CommonBuild {
  import Dependencies._
  import Resolvers._
  def commonProject(name: String, baseFile: java.io.File) = 
  	Project(id = name, base = baseFile, settings = BuildSettings.buildSettings).settings(
  	  resolvers += typesafeSnapshot,
	  libraryDependencies += jodatime,
	  libraryDependencies += jodaconvert,
	  libraryDependencies += scalaz,
	  libraryDependencies += akka_actor,
	  libraryDependencies += specs2,
	  libraryDependencies += akka_testkit
  )
  
}

trait CoreBuild {
  import Dependencies._
  import Resolvers._
  def coreProject(name: String, baseFile: java.io.File) = 
  	Project(id = name, base = baseFile, settings = BuildSettings.buildSettings).settings(
  	  resolvers += typesafeSnapshot,
	  libraryDependencies += jodatime,
	  libraryDependencies += jodaconvert,
	  libraryDependencies += scalaz,
	  libraryDependencies += akka_actor,
	  libraryDependencies += specs2,
	  libraryDependencies += akka_testkit
  )
  
}

trait UnfilteredBuild {
  import Dependencies._
  import Resolvers._
  def unfilteredProject(name: String, baseFile: java.io.File) = 
  	Project(id = name, base = baseFile, settings = BuildSettings.buildSettings).settings(
	  libraryDependencies += unfiltered,
  	  resolvers += typesafeSnapshot)
  
}

object HighDriveBuild extends Build with CommonBuild with CoreBuild with UnfilteredBuild {
  lazy val root = Project(	id = "almhirt",
	                        base = file(".")) aggregate(common, core, unfiltered)
	
  lazy val common = commonProject(	name = "almhirt-common",
                       			baseFile = file("almhirt-common"))

  lazy val core = coreProject(	name = "almhirt-core",
	                       		baseFile = file("almhirt-core")) dependsOn(common)

  lazy val unfiltered = unfilteredProject(	name = "almhirt-unfiltered",
	                       				baseFile = file("almhirt-unfiltered")) dependsOn(common)

}
