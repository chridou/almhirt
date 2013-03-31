package almhirt.ext.core.slick.snapshots

import scala.slick.session.Database
import almhirt.common._
import almhirt.almvalidation.kit._
import almhirt.ext.core.slick.shared.BlobStoreComponent
import almhirt.ext.core.slick.shared.Profile

class TextSnapshotsDataAccess(override val snapshotsTablename: String, override val blobTablename: String, override val getDb: Unit => Database, override val profile: scala.slick.driver.ExtendedProfile, override val hasExecutionContext: HasExecutionContext) extends BlobStoreComponent with TextSnapshotStorageComponent with Profile {
  import profile.simple._

  private val ddl = BlobRows.ddl ++ TextSnapshotRows.ddl

  def create(implicit session: Session): AlmValidation[Unit] =
    inTryCatch { getDb() withSession { implicit session: Session => ddl.create } }.leftMap(prob => 
      PersistenceProblem(s"Could not create schema for TextDomainEventLog: ${prob.message}", cause = Some(prob)))

  def drop(implicit session: Session): AlmValidation[Unit] =
    inTryCatch { getDb() withSession { implicit session: Session => ddl.drop } }.leftMap(prob => 
      PersistenceProblem(s"Could not drop schema for TextDomainEventLog: ${prob.message}", cause = Some(prob)))
}
