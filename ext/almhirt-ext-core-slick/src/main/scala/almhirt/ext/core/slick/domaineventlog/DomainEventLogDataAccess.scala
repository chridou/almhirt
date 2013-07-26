package almhirt.ext.core.slick.domaineventlog

import java.util.{ UUID => JUUID }
import scala.slick.session.Database
import scalaz.syntax.validation._
import almhirt.common._
import almhirt.almvalidation.kit._
import almhirt.configuration._
import almhirt.ext.core.slick.shared._
import com.typesafe.config.Config

class TextDomainEventLogDataAccess(override val eventlogtablename: String, override val db: Database, override val profile: scala.slick.driver.ExtendedProfile) extends TextDomainEventLogStoreComponent with Profile {
  import profile.simple._

  private val ddl = TextDomainEventLogRows.ddl

  def create: AlmValidation[Unit] =
    inTryCatch { db withSession { implicit session: Session => ddl.create } }.leftMap(prob =>
      PersistenceProblem(s"Could not create schema for TextDomainEventLog: ${prob.message}", cause = Some(prob)))

  def drop: AlmValidation[Unit] =
    inTryCatch { db withSession { implicit session: Session => ddl.drop } }.leftMap(prob =>
      PersistenceProblem(s"Could not drop schema for TextDomainEventLog: ${prob.message}", cause = Some(prob)))
}

object TextDomainEventLogDataAccess {
  def apply(configSection: Config): AlmValidation[TextDomainEventLogDataAccess] =
    for {
      profileSettings <- ProfileSettings.fromConfig(configSection)
      connectionString <- configSection.v[String]("connectionstring").flatMap(_.notEmptyOrWhitespace)
      props <- configSection.opt[java.util.Properties]("properties").map(_.getOrElse(new java.util.Properties()))
      tablename <- configSection.v[String]("tablename").flatMap(_.notEmptyOrWhitespace)
      database <- inTryCatch{ Database.forURL(connectionString, profileSettings.driver) }
    } yield new TextDomainEventLogDataAccess(tablename, database, profileSettings.slickDriver)
}

class BinaryDomainEventLogDataAccess(override val eventlogtablename: String, override val db: Database, override val profile: scala.slick.driver.ExtendedProfile) extends BinaryDomainEventLogStoreComponent with Profile {
  import profile.simple._

  private val ddl = BinaryDomainEventLogRows.ddl

  def create: AlmValidation[Unit] =
    inTryCatch { db withSession { implicit session: Session => ddl.create } }.leftMap(prob =>
      PersistenceProblem(s"Could not create schema for BinaryDomainEventLog: ${prob.message}", cause = Some(prob)))

  def drop(implicit session: Session): AlmValidation[Unit] =
    inTryCatch { db withSession { implicit session: Session => ddl.drop } }.leftMap(prob =>
      PersistenceProblem(s"Could not drop schema for BinaryDomainEventLog: ${prob.message}", cause = Some(prob)))
}

