package almhirt.ext.core.slick.eventlogs

import java.util.{ UUID => JUUID }
import scala.slick.session.Database
import scalaz.syntax.validation._
import almhirt.common._
import almhirt.almvalidation.kit._

class TextEventLogDataAccess(override val eventlogtablename: String, override val blobtablename: String, override val getDb: Unit => Database, override val profile: scala.slick.driver.ExtendedProfile) extends BlobStoreComponent with TextEventLogStoreComponent with Profile {
  import profile.simple._

  private val ddl = BlobRows.ddl ++ TextEventLogRows.ddl

  def create: AlmValidation[Unit] =
    inTryCatchM { getDb() withSession { implicit session: Session => ddl.create } }("Could not create schema for TextEventLogDataAccessLayer")

  def drop: AlmValidation[Unit] =
    inTryCatchM { getDb() withSession { implicit session: Session => ddl.drop } }("Could not drop schema for TextEventLogDataAccessLayer")
}