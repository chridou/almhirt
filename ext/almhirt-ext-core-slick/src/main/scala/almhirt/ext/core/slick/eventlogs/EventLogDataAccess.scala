package almhirt.ext.core.slick.eventlogs

import java.util.{ UUID => JUUID }
import scala.slick.session.Database
import scalaz.syntax.validation._
import almhirt.common._
import almhirt.almvalidation.kit._
import almhirt.ext.core.slick.shared._


class TextEventLogDataAccess(override val eventlogTablename: String, override val getDb: Unit => Database, override val profile: scala.slick.driver.ExtendedProfile) extends  TextEventLogStoreComponent  with Profile {
  import profile.simple._

  private val ddl = TextEventLogRows.ddl

  def create: AlmValidation[Unit] =
    inTryCatch { getDb() withSession { implicit session: Session => ddl.create } }.leftMap(prob => 
      PersistenceProblem(s"Could not create schema for TextEventLog: ${prob.message}", cause = Some(prob)))

  def drop: AlmValidation[Unit] =
    inTryCatch { getDb() withSession { implicit session: Session => ddl.drop } }.leftMap(prob => 
      PersistenceProblem(s"Could not drop schema for TextEventLog: ${prob.message}", cause = Some(prob)))
}

class BinaryLogDataAccess(override val eventlogTablename: String, override val getDb: Unit => Database, override val profile: scala.slick.driver.ExtendedProfile) extends BinaryEventLogStoreComponent with Profile {
  import profile.simple._

  private val ddl = BinaryEventLogRows.ddl

  def create: AlmValidation[Unit] =
    inTryCatch { getDb() withSession { implicit session: Session => ddl.create } }.leftMap(prob => 
      PersistenceProblem(s"Could not create schema for TextEventLog: ${prob.message}", cause = Some(prob)))

  def drop: AlmValidation[Unit] =
    inTryCatch { getDb() withSession { implicit session: Session => ddl.drop } }.leftMap(prob => 
      PersistenceProblem(s"Could not drop schema for TextEventLog: ${prob.message}", cause = Some(prob)))
}