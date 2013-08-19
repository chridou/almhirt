package almhirt.corex.slick.domaineventlog

import java.util.{ UUID => JUUID }
import scalaz.syntax.validation._
import almhirt.common._
import almhirt.almvalidation.kit._
import almhirt.corex.slick.SlickTypeMappers
import almhirt.corex.slick.shared.Profile

trait DomainEventLogStoreComponent[T <: DomainEventLogRow] {
  def insertEventRow(eventLogRow: T): AlmValidation[T]
  def insertManyEventRows(eventLogRows: Seq[T]): AlmValidation[Unit]
  def getEventRowById(id: JUUID): AlmValidation[T]
  def getAllEventRows: AlmValidation[Seq[T]]
  def getAllEventRowsFor(aggId: JUUID): AlmValidation[Seq[T]]
  def getAllEventRowsForFrom(aggId: JUUID, fromVersion: Long): AlmValidation[Seq[T]]
  def getAllEventRowsForTo(aggId: JUUID, toVersion: Long): AlmValidation[Seq[T]]
  def getAllEventRowsForUntil(aggId: JUUID, untilVersion: Long): AlmValidation[Seq[T]]
  def getAllEventRowsForFromTo(aggId: JUUID, fromVersion: Long, toVersion: Long): AlmValidation[Seq[T]]
  def getAllEventRowsForFromUntil(aggId: JUUID, fromVersion: Long, untilVersion: Long): AlmValidation[Seq[T]]
  def countEventRows: AlmValidation[Int]
}

trait TextDomainEventLogStoreComponent extends DomainEventLogStoreComponent[TextDomainEventLogRow] { this: Profile =>
  import profile.simple._

  val eventlogtablename: String

  object TextDomainEventLogRows extends Table[TextDomainEventLogRow](eventlogtablename) {
    def id = column[JUUID]("ID", O.PrimaryKey)
    def aggId = column[JUUID]("AGG_ID", O.NotNull)
    def aggVersion = column[Long]("AGG_VERSION", O.NotNull)
    def channel = column[String]("CHANNEL", O.NotNull)
    def payload = column[String]("PAYLOAD", O.NotNull)

    def * = id ~ aggId ~ aggVersion ~ channel ~ payload <> (TextDomainEventLogRow, TextDomainEventLogRow.unapply _)

    def aggIdIdx = index(s"idx_${eventlogtablename}_agg_id", aggId)
    def aggIdVersionIdx = index(s"idx_${eventlogtablename}_agg_id_version", (aggId, aggVersion))

    def insertSafe(textDomainEventLogRow: TextDomainEventLogRow)(implicit session: Session): AlmValidation[TextDomainEventLogRow] = {
      computeSafely {
        val insertedRows = this.insert(textDomainEventLogRow)
        if (insertedRows == 1)
          textDomainEventLogRow.success
        else
          PersistenceProblem(s"""Could not store string based domaineventlog row with id "${textDomainEventLogRow.id.toString()}". The returned row count was not 1. It was $insertedRows""").failure
      }
    }
  }
  
  override def insertEventRow(eventLogRow: TextDomainEventLogRow): AlmValidation[TextDomainEventLogRow] =
    computeSafely {
      db withSession { implicit session: Session =>
        TextDomainEventLogRows.insertSafe(eventLogRow)
      }
    }

  override def insertManyEventRows(eventLogRows: Seq[TextDomainEventLogRow]): AlmValidation[Unit] =
    inTryCatch {
      db withSession { implicit session: Session =>
        TextDomainEventLogRows.insertAll(eventLogRows: _*)
      }
    }

  override def getEventRowById(id: JUUID): AlmValidation[TextDomainEventLogRow] =
    computeSafely {
      db withSession { implicit session: Session =>
        Query(TextDomainEventLogRows).filter(_.id === id.bind).list.headOption match {
          case Some(row) => row.success
          case None => NotFoundProblem(s"""No domain event with id "${id.toString}" found.""").failure
        }
      }
    }

  override def getAllEventRows: AlmValidation[Seq[TextDomainEventLogRow]] =
    inTryCatch { db withSession { implicit session: Session => Query(TextDomainEventLogRows).list } }

  override def getAllEventRowsFor(aggId: JUUID): AlmValidation[Seq[TextDomainEventLogRow]] =
    inTryCatch { db withSession { implicit session: Session => Query(TextDomainEventLogRows).filter(x => x.aggId === aggId.bind).list } }

  override def getAllEventRowsForFrom(aggId: JUUID, fromVersion: Long): AlmValidation[Seq[TextDomainEventLogRow]] =
    inTryCatch { db withSession { implicit session: Session => Query(TextDomainEventLogRows).filter(x => x.aggId === aggId.bind && x.aggVersion >= fromVersion).list } }

  override def getAllEventRowsForTo(aggId: JUUID, toVersion: Long): AlmValidation[Seq[TextDomainEventLogRow]] =
    inTryCatch { db withSession { implicit session: Session => Query(TextDomainEventLogRows).filter(x => x.aggId === aggId.bind && x.aggVersion <= toVersion).list } }

  override def getAllEventRowsForUntil(aggId: JUUID, untilVersion: Long): AlmValidation[Seq[TextDomainEventLogRow]] =
    inTryCatch { db withSession { implicit session: Session => Query(TextDomainEventLogRows).filter(x => x.aggId === aggId.bind && x.aggVersion < untilVersion).list } }
  
  override def getAllEventRowsForFromTo(aggId: JUUID, fromVersion: Long, toVersion: Long): AlmValidation[Seq[TextDomainEventLogRow]] =
    inTryCatch { db withSession { implicit session: Session => Query(TextDomainEventLogRows).filter(x => x.aggId === aggId.bind && x.aggVersion >= fromVersion && x.aggVersion <= toVersion).list } }

  override def getAllEventRowsForFromUntil(aggId: JUUID, fromVersion: Long, untilVersion: Long): AlmValidation[Seq[TextDomainEventLogRow]] =
    inTryCatch { db withSession { implicit session: Session => Query(TextDomainEventLogRows).filter(x => x.aggId === aggId.bind && x.aggVersion >= fromVersion && x.aggVersion < untilVersion).list } }
  
  override def countEventRows: AlmValidation[Int] =
    inTryCatchM { db withSession { implicit session: Session => (for { row <- TextDomainEventLogRows } yield row.length).first } }("Could not determine count for TextDomainEventLogRows")

}

trait BinaryDomainEventLogStoreComponent extends DomainEventLogStoreComponent[BinaryDomainEventLogRow] { this: Profile =>
  import profile.simple._

  val eventlogtablename: String

  object BinaryDomainEventLogRows extends Table[BinaryDomainEventLogRow](eventlogtablename) {
    def id = column[JUUID]("ID", O.PrimaryKey)
    def aggId = column[JUUID]("AGG_ID", O.NotNull)
    def aggVersion = column[Long]("AGG_VERSION", O.NotNull)
    def channel = column[String]("CHANNEL", O.NotNull)
    def payload = column[Array[Byte]]("PAYLOAD", O.NotNull)

    def * = id ~ aggId ~ aggVersion ~ channel ~ payload <> (BinaryDomainEventLogRow, BinaryDomainEventLogRow.unapply _)

    def aggIdIdx = index("idx_agg_id", aggId)
    def aggIdVersionIdx = index("idx_agg_id_version", (aggId, aggVersion))

    def insertSafe(binaryDomainEventLogRow: BinaryDomainEventLogRow)(implicit session: Session): AlmValidation[BinaryDomainEventLogRow] = {
      computeSafely {
        val insertedRows = this.insert(binaryDomainEventLogRow)
        if (insertedRows == 1)
          binaryDomainEventLogRow.success
        else
          PersistenceProblem(s"""Could not store binary based domaineventlog row with id "${binaryDomainEventLogRow.id.toString()}". The returned row count was not 1. It was $insertedRows""").failure
      }
    }
  }

  override def insertEventRow(eventLogRow: BinaryDomainEventLogRow): AlmValidation[BinaryDomainEventLogRow] =
    computeSafely { db withSession { implicit session: Session => BinaryDomainEventLogRows.insertSafe(eventLogRow) } }

  override def insertManyEventRows(eventLogRows: Seq[BinaryDomainEventLogRow]): AlmValidation[Unit] =
    inTryCatch {
      db withSession { implicit session: Session =>
        BinaryDomainEventLogRows.insertAll(eventLogRows: _*)
      }
    }

  override def getEventRowById(id: JUUID): AlmValidation[BinaryDomainEventLogRow] =
    computeSafely {
      db withSession { implicit session: Session =>
        Query(BinaryDomainEventLogRows).filter(_.id === id.bind).list.headOption match {
          case Some(row) => row.success
          case None => NotFoundProblem(s"""No domain event with id "${id.toString}" found.""").failure
        }
      }
    }

  override def getAllEventRows: AlmValidation[Seq[BinaryDomainEventLogRow]] =
    inTryCatch { db withSession { implicit session: Session => Query(BinaryDomainEventLogRows).list } }

  override def getAllEventRowsFor(aggId: JUUID): AlmValidation[Seq[BinaryDomainEventLogRow]] =
    inTryCatch { db withSession { implicit session: Session => Query(BinaryDomainEventLogRows).filter(x => x.aggId === aggId.bind).list } }

  override def getAllEventRowsForFrom(aggId: JUUID, fromVersion: Long): AlmValidation[Seq[BinaryDomainEventLogRow]] =
    inTryCatch { db withSession { implicit session: Session => Query(BinaryDomainEventLogRows).filter(x => x.aggId === aggId.bind &&  x.aggVersion >= fromVersion).list } }

  override def getAllEventRowsForTo(aggId: JUUID, toVersion: Long): AlmValidation[Seq[BinaryDomainEventLogRow]] =
    inTryCatch { db withSession { implicit session: Session => Query(BinaryDomainEventLogRows).filter(x => x.aggId === aggId.bind &&  x.aggVersion <= toVersion).list } }

  override def getAllEventRowsForUntil(aggId: JUUID, untilVersion: Long): AlmValidation[Seq[BinaryDomainEventLogRow]] =
    inTryCatch { db withSession { implicit session: Session => Query(BinaryDomainEventLogRows).filter(x => x.aggId === aggId.bind &&  x.aggVersion < untilVersion).list } }
  
  override def getAllEventRowsForFromTo(aggId: JUUID, fromVersion: Long, toVersion: Long): AlmValidation[Seq[BinaryDomainEventLogRow]] =
    inTryCatch { db withSession { implicit session: Session => Query(BinaryDomainEventLogRows).filter(x => x.aggId === aggId.bind &&  x.aggVersion >= fromVersion && x.aggVersion <= toVersion).list } }

  override def getAllEventRowsForFromUntil(aggId: JUUID, fromVersion: Long, untilVersion: Long): AlmValidation[Seq[BinaryDomainEventLogRow]] =
    inTryCatch { db withSession { implicit session: Session => Query(BinaryDomainEventLogRows).filter(x => x.aggId === aggId.bind &&  x.aggVersion >= fromVersion && x.aggVersion < untilVersion).list } }
  
  override def countEventRows: AlmValidation[Int] =
    inTryCatchM { db withSession { implicit session: Session => (for { row <- BinaryDomainEventLogRows } yield row.length).first } }("Could not determine count for TextDomainEventLogRows")

}