package almhirt.ext.core.slick.eventlogs

import java.util.{ UUID => JUUID }
import scala.slick.session.Database
import scalaz.syntax.validation._
import almhirt.common._
import almhirt.almvalidation.kit._
import almhirt.ext.core.slick.shared._

trait CanStoreEventLogRowWithBlobs[T <: EventLogRow] {
  def storeRowAndBlobs(row: T, blobs: Vector[(JUUID, Array[Byte])]): AlmValidation[T]
}

class TextEventLogDataAccess(override val eventlogTablename: String, override val blobTablename: String, override val getDb: Unit => Database, override val profile: scala.slick.driver.ExtendedProfile, override val hasExecutionContext: HasExecutionContext) extends BlobStoreComponent with TextEventLogStoreComponent with CanStoreEventLogRowWithBlobs[TextEventLogRow] with Profile {
  import profile.simple._

  private val ddl = BlobRows.ddl ++ TextEventLogRows.ddl

  def create: AlmValidation[Unit] =
    inTryCatch { getDb() withSession { implicit session: Session => ddl.create } }.leftMap(prob => 
      PersistenceProblem(s"Could not create schema for TextEventLog: ${prob.message}", cause = Some(prob)))

  def drop: AlmValidation[Unit] =
    inTryCatch { getDb() withSession { implicit session: Session => ddl.drop } }.leftMap(prob => 
      PersistenceProblem(s"Could not drop schema for TextEventLog: ${prob.message}", cause = Some(prob)))

  override def storeRowAndBlobs(row: TextEventLogRow, blobs: Vector[(JUUID, Array[Byte])]): AlmValidation[TextEventLogRow] = {
    computeSafely {
      getDb() withTransaction { implicit session: Session =>
        TextEventLogRows.insertSafe(row).map(_ =>
          inTryCatch { BlobRows.insertAll(blobs.map(blob => BlobRow(blob._1, blob._2)): _*) }).map(_ => row)
      }
    }.leftMap(prob =>
      PersistenceProblem(s"""Could store TextEventLogRow with event id "${row.id.toString()}" and ${blobs.length} blobs: ${prob.message}""", cause = Some(prob)))
  }
}

class BinaryLogDataAccess(override val eventlogTablename: String, override val blobTablename: String, override val getDb: Unit => Database, override val profile: scala.slick.driver.ExtendedProfile, override val hasExecutionContext: HasExecutionContext) extends BlobStoreComponent with BinaryEventLogStoreComponent with CanStoreEventLogRowWithBlobs[BinaryEventLogRow] with Profile {
  import profile.simple._

  private val ddl = BlobRows.ddl ++ BinaryEventLogRows.ddl

  def create: AlmValidation[Unit] =
    inTryCatch { getDb() withSession { implicit session: Session => ddl.create } }.leftMap(prob => 
      PersistenceProblem(s"Could not create schema for TextEventLog: ${prob.message}", cause = Some(prob)))

  def drop: AlmValidation[Unit] =
    inTryCatch { getDb() withSession { implicit session: Session => ddl.drop } }.leftMap(prob => 
      PersistenceProblem(s"Could not drop schema for TextEventLog: ${prob.message}", cause = Some(prob)))

  override def storeRowAndBlobs(row: BinaryEventLogRow, blobs: Vector[(JUUID, Array[Byte])]): AlmValidation[BinaryEventLogRow] = {
    computeSafely {
      getDb() withTransaction { implicit session: Session =>
        BinaryEventLogRows.insertSafe(row).map(_ =>
          inTryCatch { BlobRows.insertAll(blobs.map(blob => BlobRow(blob._1, blob._2)): _*) }).map(_ => row)
      }
    }.leftMap(prob =>
      PersistenceProblem(s"""Could store BinaryEventLogRow with event id "${row.id.toString()}" and ${blobs.length} blobs: ${prob.message}""", cause = Some(prob)))
  }
}