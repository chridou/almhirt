package almhirt.corex.slick.eventlog

import java.util.{ UUID => JUUID }
import scala.slick.session.Database
import scalaz.syntax.validation._
import almhirt.common._
import almhirt.almvalidation.kit._
import almhirt.configuration._
import almhirt.corex.slick.shared._
import com.typesafe.config.Config

class TextEventLogDataAccess(override val eventlogtablename: String, override val db: Database, override val profile: scala.slick.driver.ExtendedProfile) extends TextEventLogStoreComponent with Profile {
  import profile.simple._

  private val ddl = TextEventLogRows.ddl

  def create: AlmValidation[Unit] =
    inTryCatch { db withSession { implicit session: Session => ddl.create } }.leftMap(prob =>
      PersistenceProblem(s"""Could not create schema for TextEventLog: "${prob.message}"""", cause = Some(prob)))

  def drop: AlmValidation[Unit] =
    inTryCatch { db withSession { implicit session: Session => ddl.drop } }.leftMap(prob =>
      PersistenceProblem(s"""Could not drop schema for TextEventLog: "${prob.message}"""", cause = Some(prob)))
}

object TextEventLogDataAccess {
  def apply(configSection: Config): AlmValidation[TextEventLogDataAccess] =
    for {
      profileSettings <- ProfileSettings.fromConfig(configSection)
      connectionString <- configSection.v[String]("connection").flatMap(_.notEmptyOrWhitespace)
      props <- configSection.opt[java.util.Properties]("properties").map(_.getOrElse(new java.util.Properties()))
      tablename <- configSection.v[String]("table-name").flatMap(_.notEmptyOrWhitespace)
      database <- inTryCatch{ Database.forURL(connectionString, profileSettings.driver) }
    } yield new TextEventLogDataAccess(tablename, database, profileSettings.slickDriver)
}

class BinaryEventLogDataAccess(override val eventlogtablename: String, override val db: Database, override val profile: scala.slick.driver.ExtendedProfile) extends BinaryEventLogStoreComponent with Profile {
  import profile.simple._

  private val ddl = BinaryEventLogRows.ddl

  def create: AlmValidation[Unit] =
    inTryCatch { db withSession { implicit session: Session => ddl.create } }.leftMap(prob =>
      PersistenceProblem(s"""Could not create schema for BinaryEventLog: "${prob.message}"""", cause = Some(prob)))

  def drop(implicit session: Session): AlmValidation[Unit] =
    inTryCatch { db withSession { implicit session: Session => ddl.drop } }.leftMap(prob =>
      PersistenceProblem(s"""Could not drop schema for BinaryEventLog: "${prob.message}"""", cause = Some(prob)))
}

