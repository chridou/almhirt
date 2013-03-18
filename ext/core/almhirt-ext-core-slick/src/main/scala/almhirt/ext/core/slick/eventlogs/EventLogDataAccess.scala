package almhirt.ext.core.slick.eventlogs

import java.util.{ UUID => JUUID }
import scalaz.syntax.validation._
import almhirt.common._
import almhirt.almvalidation.kit._

class TextEventLogDataAccessLayer(override val eventlogtablename: String, override val blobtablename: String, override val profile: scala.slick.driver.ExtendedProfile) extends BlobStoreComponent with TextEventLogStoreComponent with Profile {
  import profile.simple._

  private val ddl = BlobRows.ddl ++ TextEventLogRows.ddl

  def create(implicit session: Session): AlmValidation[Unit] =
    inTryCatchM { ddl.create }("Could not create schema for TextEventLogDataAccessLayer")

  def drop(implicit session: Session): AlmValidation[Unit] =
    inTryCatchM { ddl.drop }("Could not drop schema for TextEventLogDataAccessLayer")


}