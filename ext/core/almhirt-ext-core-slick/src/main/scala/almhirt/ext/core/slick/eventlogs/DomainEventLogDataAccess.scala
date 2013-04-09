package almhirt.ext.core.slick.eventlogs

import java.util.{ UUID => JUUID }
import scala.slick.session.Database
import scalaz.syntax.validation._
import almhirt.common._
import almhirt.almvalidation.kit._
import almhirt.ext.core.slick.shared._

class TextDomainEventLogDataAccess(override val eventlogtablename: String, override val getDb: Unit => Database, override val profile: scala.slick.driver.ExtendedProfile) extends TextDomainEventLogStoreComponent with Profile {
  import profile.simple._

  private val ddl = TextDomainEventLogRows.ddl

  def create: AlmValidation[Unit] =
    inTryCatch { getDb() withSession { implicit session: Session => ddl.create } }.leftMap(prob =>
      PersistenceProblem(s"Could not create schema for TextDomainEventLog: ${prob.message}", cause = Some(prob)))

  def drop: AlmValidation[Unit] =
    inTryCatch { getDb() withSession { implicit session: Session => ddl.drop } }.leftMap(prob =>
      PersistenceProblem(s"Could not drop schema for TextDomainEventLog: ${prob.message}", cause = Some(prob)))
}

class BinaryDomainEventLogDataAccess(override val eventlogtablename: String, override val getDb: Unit => Database, override val profile: scala.slick.driver.ExtendedProfile) extends BinaryDomainEventLogStoreComponent with Profile {
  import profile.simple._

  private val ddl = BinaryDomainEventLogRows.ddl

  def create: AlmValidation[Unit] =
    inTryCatch { getDb() withSession { implicit session: Session => ddl.create } }.leftMap(prob =>
      PersistenceProblem(s"Could not create schema for BinaryDomainEventLog: ${prob.message}", cause = Some(prob)))

  def drop(implicit session: Session): AlmValidation[Unit] =
    inTryCatch { getDb() withSession { implicit session: Session => ddl.drop } }.leftMap(prob =>
      PersistenceProblem(s"Could not drop schema for BinaryDomainEventLog: ${prob.message}", cause = Some(prob)))
}


