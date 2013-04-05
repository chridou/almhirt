package almhirt.ext.core.slick.eventlogs

import java.util.{ UUID => JUUID }
import scala.slick.session.Database
import scalaz.syntax.validation._
import almhirt.common._
import almhirt.almvalidation.kit._
import almhirt.ext.core.slick.shared._

trait CanStoreDomainEventLogRowWithBlobs[T <: DomainEventLogRow] {
  def storeRowAndBlobs(row: T, blobs: Vector[(JUUID, Array[Byte])]): AlmValidation[T]
  def storeManyRowsAndBlobs(rows: Iterable[(T, Vector[(JUUID, Array[Byte])])]): AlmValidation[Vector[T]]
}

class TextDomainEventLogDataAccess(override val eventlogtablename: String, override val blobTablename: String, override val getDb: Unit => Database, override val profile: scala.slick.driver.ExtendedProfile, override val hasExecutionContext: HasExecutionContext) extends BlobStoreComponent with TextDomainEventLogStoreComponent with CanStoreDomainEventLogRowWithBlobs[TextDomainEventLogRow] with Profile {
  import profile.simple._

  private val ddl = BlobRows.ddl ++ TextDomainEventLogRows.ddl

  def create: AlmValidation[Unit] =
    inTryCatch { getDb() withSession { implicit session: Session => ddl.create } }.leftMap(prob =>
      PersistenceProblem(s"Could not create schema for TextDomainEventLog: ${prob.message}", cause = Some(prob)))

  def drop: AlmValidation[Unit] =
    inTryCatch { getDb() withSession { implicit session: Session => ddl.drop } }.leftMap(prob =>
      PersistenceProblem(s"Could not drop schema for TextDomainEventLog: ${prob.message}", cause = Some(prob)))

  def storeRowAndBlobs(row: TextDomainEventLogRow, blobs: Vector[(JUUID, Array[Byte])]): AlmValidation[TextDomainEventLogRow] =
    computeSafely { getDb() withTransaction { implicit session: Session => store(row, blobs) } }

  def storeManyRowsAndBlobs(rows: Iterable[(TextDomainEventLogRow, Vector[(JUUID, Array[Byte])])]): AlmValidation[Vector[TextDomainEventLogRow]] = {
    import scalaz._, Scalaz._
    computeSafely {
      getDb() withTransaction { implicit session: Session =>
        rows.map(x => store(x._1, x._2).toAgg).toVector.sequence
      }
    }.leftMap(prob =>
      PersistenceProblem(s"""Could store TextDomainEventLogRows: ${prob.message}""", cause = Some(prob)))
  }

  private def store(row: TextDomainEventLogRow, blobs: Vector[(JUUID, Array[Byte])])(implicit session: Session): AlmValidation[TextDomainEventLogRow] = {
    computeSafely {
      TextDomainEventLogRows.insertSafe(row).map(_ =>
        BlobRows.insertAll(blobs.map(blob => BlobRow(blob._1, blob._2)): _*)).map(_ => row)
    }.leftMap(prob =>
      PersistenceProblem(s"""Could store TextDomainEventLogRow with event id "${row.id.toString()}"(AR:"${row.aggId.toString()}") and ${blobs.length} blobs: ${prob.message}""", cause = Some(prob)))
  }

}

class BinaryDomainEventLogDataAccess(override val eventlogtablename: String, override val blobTablename: String, override val getDb: Unit => Database, override val profile: scala.slick.driver.ExtendedProfile, override val hasExecutionContext: HasExecutionContext) extends BlobStoreComponent with BinaryDomainEventLogStoreComponent with CanStoreDomainEventLogRowWithBlobs[BinaryDomainEventLogRow] with Profile {
  import profile.simple._

  private val ddl = BlobRows.ddl ++ BinaryDomainEventLogRows.ddl

  def create: AlmValidation[Unit] =
    inTryCatch { getDb() withSession { implicit session: Session => ddl.create } }.leftMap(prob =>
      PersistenceProblem(s"Could not create schema for BinaryDomainEventLog: ${prob.message}", cause = Some(prob)))

  def drop(implicit session: Session): AlmValidation[Unit] =
    inTryCatch { getDb() withSession { implicit session: Session => ddl.drop } }.leftMap(prob =>
      PersistenceProblem(s"Could not drop schema for BinaryDomainEventLog: ${prob.message}", cause = Some(prob)))

  def storeRowAndBlobs(row: BinaryDomainEventLogRow, blobs: Vector[(JUUID, Array[Byte])]): AlmValidation[BinaryDomainEventLogRow] =
    computeSafely { getDb() withTransaction { implicit session: Session => store(row, blobs) } }

  def storeManyRowsAndBlobs(rows: Iterable[(BinaryDomainEventLogRow, Vector[(JUUID, Array[Byte])])]): AlmValidation[Vector[BinaryDomainEventLogRow]] = {
    import scalaz._, Scalaz._
    computeSafely {
      getDb() withTransaction { implicit session: Session =>
        rows.map(x => store(x._1, x._2).toAgg).toVector.sequence
      }
    }.leftMap(prob =>
      PersistenceProblem(s"""Could store BinaryDomainEventLogRows: ${prob.message}""", cause = Some(prob)))
  }

  private def store(row: BinaryDomainEventLogRow, blobs: Vector[(JUUID, Array[Byte])])(implicit session: Session): AlmValidation[BinaryDomainEventLogRow] = {
    computeSafely {
      BinaryDomainEventLogRows.insertSafe(row).map(_ =>
        BlobRows.insertAll(blobs.map(blob => BlobRow(blob._1, blob._2)): _*)).map(_ => row)
    }.leftMap(prob =>
      PersistenceProblem(s"""Could store BinaryDomainEventLogRow with event id "${row.id.toString()}"(AR:"${row.aggId.toString()}") and ${blobs.length} blobs: ${prob.message}""", cause = Some(prob)))
  }
}


