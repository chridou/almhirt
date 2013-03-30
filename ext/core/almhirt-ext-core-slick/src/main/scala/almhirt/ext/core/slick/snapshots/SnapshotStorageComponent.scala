package almhirt.ext.core.slick.snapshots

import java.util.{ UUID => JUUID }
import scalaz.syntax.validation._
import almhirt.common._
import almhirt.almvalidation.kit._
import almhirt.ext.core.slick.shared.Profile

trait SnapshotStorageComponent[T <: SnapshotRow] {
  def snapshotsTablename: String
  def insertSnapshotRow(snapshotRow: T): AlmValidation[T]
  def getSnapshotRowById(arId: JUUID): AlmValidation[T]
  def getVersionForId(arId: JUUID): AlmValidation[Long]
  def isSnapshotContained(arId: JUUID): AlmValidation[Boolean]
  def countSnapshotRows: AlmValidation[Int]
}

trait TextSnapshotStorageComponent extends SnapshotStorageComponent[TextSnapshotRow] { this: Profile =>
  import profile.simple._

  override val snapshotsTablename: String

  object TextSnapshotRows extends Table[TextSnapshotRow](snapshotsTablename) {
    def arId = column[JUUID]("AR_ID", O.PrimaryKey)
    def arVersion = column[Long]("AR_VERSION", O.NotNull)
    def arType = column[String]("AR_TYPE", O.NotNull)
    def channel = column[String]("CHANNEL", O.NotNull)
    def payload = column[String]("PAYLOAD", O.NotNull)

    def * = arId ~ arVersion ~ arType ~ channel ~ payload <> (TextSnapshotRow, TextSnapshotRow.unapply _)

    def insertSafely(snapshotRow: TextSnapshotRow)(implicit session: Session): AlmValidation[TextSnapshotRow] = {
      computeSafely {
        val insertedRows = this.insert(snapshotRow)
        if (insertedRows == 1)
          snapshotRow.success
        else
          PersistenceProblem(s"""Could not store string based snapshot row for aggregate root id "${snapshotRow.arId.toString()}". The returned row count was not 1. It was $insertedRows""").failure
      }
    }
  }

  override def insertSnapshotRow(snapshotRow: TextSnapshotRow): AlmValidation[TextSnapshotRow] =
    computeSafely {
      getDb() withSession { implicit session: Session =>
        TextSnapshotRows.insertSafely(snapshotRow)
      }
    }

  override def getSnapshotRowById(arId: JUUID): AlmValidation[TextSnapshotRow] =
    computeSafely {
      getDb() withSession { implicit session: Session =>
        Query(TextSnapshotRows).filter(_.arId === arId.bind).list.headOption match {
          case Some(row) => row.success
          case None => NotFoundProblem(s"""No snapshot row found for aggregate root id "${arId.toString}".""").failure
        }
      }
    }

  override def getVersionForId(arId: JUUID): AlmValidation[Long] =
    computeSafely {
      getDb() withSession { implicit session: Session =>
        Query(TextSnapshotRows).filter(_.arId === arId.bind).map(_.arVersion).list.headOption match {
          case Some(version) => version.success
          case None => NotFoundProblem(s"""No snapshot row found for aggregate root id "${arId.toString}".""").failure
        }
      }
    }

  override def isSnapshotContained(arId: JUUID): AlmValidation[Boolean] =
    computeSafely {
      getDb() withSession { implicit session: Session =>
        Query(TextSnapshotRows).filter(_.arId === arId.bind).list.headOption match {
          case Some(_) => true.success
          case None => false.success
        }
      }
    }

  override def countSnapshotRows: AlmValidation[Int] =
    inTryCatchM {
      getDb() withSession { implicit session: Session =>
        (for { row <- TextSnapshotRows } yield row.length).first
      }
    }("Could not determine count for TextSnapshotRows")

}

trait BinarySnapshotStorageComponent extends SnapshotStorageComponent[BinarySnapshotRow] { this: Profile =>
  import profile.simple._

  override val snapshotsTablename: String

  object BinarySnapshotRows extends Table[BinarySnapshotRow](snapshotsTablename) {
    def arId = column[JUUID]("AR_ID", O.PrimaryKey)
    def arVersion = column[Long]("AR_VERSION", O.NotNull)
    def arType = column[String]("AR_TYPE", O.NotNull)
    def channel = column[String]("CHANNEL", O.NotNull)
    def payload = column[Array[Byte]]("PAYLOAD", O.NotNull)

    def * = arId ~ arVersion ~ arType ~ channel ~ payload <> (BinarySnapshotRow, BinarySnapshotRow.unapply _)

    def insertSafely(snapshotRow: BinarySnapshotRow)(implicit session: Session): AlmValidation[BinarySnapshotRow] = {
      computeSafely {
        val insertedRows = this.insert(snapshotRow)
        if (insertedRows == 1)
          snapshotRow.success
        else
          PersistenceProblem(s"""Could not store binary based snapshot row for aggregate root id "${snapshotRow.arId.toString()}". The returned row count was not 1. It was $insertedRows""").failure
      }
    }
  }

  override def insertSnapshotRow(snapshotRow: BinarySnapshotRow): AlmValidation[BinarySnapshotRow] =
    computeSafely {
      getDb() withSession { implicit session: Session =>
        BinarySnapshotRows.insertSafely(snapshotRow)
      }
    }

  override def getSnapshotRowById(arId: JUUID): AlmValidation[BinarySnapshotRow] =
    computeSafely {
      getDb() withSession { implicit session: Session =>
        Query(BinarySnapshotRows).filter(_.arId === arId.bind).list.headOption match {
          case Some(row) => row.success
          case None => NotFoundProblem(s"""No snapshot row found for aggregate root id "${arId.toString}".""").failure
        }
      }
    }

  override def getVersionForId(arId: JUUID): AlmValidation[Long] =
    computeSafely {
      getDb() withSession { implicit session: Session =>
        Query(BinarySnapshotRows).filter(_.arId === arId.bind).map(_.arVersion).list.headOption match {
          case Some(version) => version.success
          case None => NotFoundProblem(s"""No snapshot row found for aggregate root id "${arId.toString}".""").failure
        }
      }
    }

  override def isSnapshotContained(arId: JUUID): AlmValidation[Boolean] =
    computeSafely {
      getDb() withSession { implicit session: Session =>
        Query(BinarySnapshotRows).filter(_.arId === arId.bind).list.headOption match {
          case Some(_) => true.success
          case None => false.success
        }
      }
    }

  override def countSnapshotRows: AlmValidation[Int] =
    inTryCatchM {
      getDb() withSession { implicit session: Session =>
        (for { row <- BinarySnapshotRows } yield row.length).first
      }
    }("Could not determine count for BinarySnapshotRows")

}