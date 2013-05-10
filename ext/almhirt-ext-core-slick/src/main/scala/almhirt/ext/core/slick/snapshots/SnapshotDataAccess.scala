package almhirt.ext.core.slick.snapshots

import java.util.{ UUID => JUUID }
import scala.slick.session.Database
import almhirt.common._
import almhirt.almvalidation.kit._
import almhirt.ext.core.slick.shared._

class TextSnapshotsDataAccess(override val snapshotsTablename: String, override val getDb: Unit => Database, override val profile: scala.slick.driver.ExtendedProfile) extends TextSnapshotStorageComponent with  Profile {
  import profile.simple._

  private val ddl = TextSnapshotRows.ddl

  def create: AlmValidation[Unit] =
    inTryCatch { getDb() withSession { implicit session: Session => ddl.create } }.leftMap(prob =>
      PersistenceProblem(s"Could not create schema for SnapshotStore: ${prob.message}", cause = Some(prob)))

  def drop: AlmValidation[Unit] =
    inTryCatch { getDb() withSession { implicit session: Session => ddl.drop } }.leftMap(prob =>
      PersistenceProblem(s"Could not drop schema for SnapshotStore: ${prob.message}", cause = Some(prob)))
}

class BinarySnapshotsDataAccess(override val snapshotsTablename: String, override val getDb: Unit => Database, override val profile: scala.slick.driver.ExtendedProfile) extends  BinarySnapshotStorageComponent with Profile {
  import profile.simple._

  private val ddl = BinarySnapshotRows.ddl

  def create: AlmValidation[Unit] =
    inTryCatch { getDb() withSession { implicit session: Session => ddl.create } }.leftMap(prob =>
      PersistenceProblem(s"Could not create schema for SnapshotStore: ${prob.message}", cause = Some(prob)))

  def drop: AlmValidation[Unit] =
    inTryCatch { getDb() withSession { implicit session: Session => ddl.drop } }.leftMap(prob =>
      PersistenceProblem(s"Could not drop schema for SnapshotStore: ${prob.message}", cause = Some(prob)))
}
