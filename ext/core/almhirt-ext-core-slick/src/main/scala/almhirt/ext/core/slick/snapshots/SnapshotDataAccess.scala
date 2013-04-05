package almhirt.ext.core.slick.snapshots

import java.util.{ UUID => JUUID }
import scala.slick.session.Database
import almhirt.common._
import almhirt.almvalidation.kit._
import almhirt.ext.core.slick.shared._

class TextSnapshotsDataAccess(override val snapshotsTablename: String, override val blobTablename: String, override val getDb: Unit => Database, override val profile: scala.slick.driver.ExtendedProfile, override val hasExecutionContext: HasExecutionContext) extends BlobStoreComponent with TextSnapshotStorageComponent with Profile {
  import profile.simple._

  private val ddl = BlobRows.ddl ++ TextSnapshotRows.ddl

  def create: AlmValidation[Unit] =
    inTryCatch { getDb() withSession { implicit session: Session => ddl.create } }.leftMap(prob =>
      PersistenceProblem(s"Could not create schema for TextDomainEventLog: ${prob.message}", cause = Some(prob)))

  def drop: AlmValidation[Unit] =
    inTryCatch { getDb() withSession { implicit session: Session => ddl.drop } }.leftMap(prob =>
      PersistenceProblem(s"Could not drop schema for TextDomainEventLog: ${prob.message}", cause = Some(prob)))

  def storeRowAndBlobs(row: TextSnapshotRow, blobs: Vector[(JUUID, Array[Byte])]): AlmValidation[TextSnapshotRow] = {
    computeSafely {
      getDb() withTransaction { implicit session: Session =>
        TextSnapshotRows.insertSafely(row).map(_ =>
          inTryCatch { BlobRows.insertAll(blobs.map(blob => BlobRow(blob._1, blob._2)): _*) }).map(_ => row)
      }
    }.leftMap(prob =>
      PersistenceProblem(s"""Could store TextSnapshotRows for AR(${row.arId.toString()}) and ${blobs.length} blobs: ${prob.message}""", cause = Some(prob)))
  }

}

class BinarySnapshotsDataAccess(override val snapshotsTablename: String, override val blobTablename: String, override val getDb: Unit => Database, override val profile: scala.slick.driver.ExtendedProfile, override val hasExecutionContext: HasExecutionContext) extends BlobStoreComponent with BinarySnapshotStorageComponent with Profile {
  import profile.simple._

  private val ddl = BlobRows.ddl ++ BinarySnapshotRows.ddl

  def create: AlmValidation[Unit] =
    inTryCatch { getDb() withSession { implicit session: Session => ddl.create } }.leftMap(prob =>
      PersistenceProblem(s"Could not create schema for TextDomainEventLog: ${prob.message}", cause = Some(prob)))

  def drop: AlmValidation[Unit] =
    inTryCatch { getDb() withSession { implicit session: Session => ddl.drop } }.leftMap(prob =>
      PersistenceProblem(s"Could not drop schema for TextDomainEventLog: ${prob.message}", cause = Some(prob)))

  def storeRowAndBlobs(row: BinarySnapshotRow, blobs: Vector[(JUUID, Array[Byte])]): AlmValidation[BinarySnapshotRow] = {
    computeSafely {
      getDb() withTransaction { implicit session: Session =>
        BinarySnapshotRows.insertSafely(row).map(_ =>
          inTryCatch { BlobRows.insertAll(blobs.map(blob => BlobRow(blob._1, blob._2)): _*) }).map(_ => row)
      }
    }.leftMap(prob =>
      PersistenceProblem(s"""Could store BinarySnapshotRow for AR(${row.arId.toString()}) and ${blobs.length} blobs: ${prob.message}""", cause = Some(prob)))
  }

}
