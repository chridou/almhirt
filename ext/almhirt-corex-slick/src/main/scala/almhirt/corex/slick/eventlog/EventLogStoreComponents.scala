package almhirt.corex.slick.eventlog

import java.util.{ UUID => JUUID }
import org.joda.time.LocalDateTime
import scalaz.syntax.validation._
import almhirt.common._
import almhirt.almvalidation.kit._
import almhirt.corex.slick.SlickTypeMappers
import almhirt.corex.slick.shared.Profile

trait EventLogStoreComponent[T <: EventLogRow] {
  def insertEventRow(eventLogRow: T): AlmValidation[T]
  def getEventRowById(id: JUUID): AlmValidation[T]
  def getAllEventRows: AlmValidation[Seq[T]]
  def getAllEventRowsFrom(from: java.sql.Timestamp): AlmValidation[Seq[T]]
  def getAllEventRowsAfter(after: java.sql.Timestamp): AlmValidation[Seq[T]]
  def getAllEventRowsTo(to: java.sql.Timestamp): AlmValidation[Seq[T]]
  def getAllEventRowsUntil(until: java.sql.Timestamp): AlmValidation[Seq[T]]
  def getAllEventRowsFromTo(from: java.sql.Timestamp, to: java.sql.Timestamp): AlmValidation[Seq[T]]
  def getAllEventRowsFromUntil(from: java.sql.Timestamp, until: java.sql.Timestamp): AlmValidation[Seq[T]]
  def getAllEventRowsAfterTo(after: java.sql.Timestamp, to: java.sql.Timestamp): AlmValidation[Seq[T]]
  def getAllEventRowsAfterUntil(after: java.sql.Timestamp, until: java.sql.Timestamp): AlmValidation[Seq[T]]
  def countEventRows: AlmValidation[Int]
}

trait TextEventLogStoreComponent extends SlickTypeMappers with EventLogStoreComponent[TextEventLogRow] { this: Profile =>
  import profile.simple._

  val eventlogtablename: String

  object TextEventLogRows extends Table[TextEventLogRow](eventlogtablename) {
    def id = column[JUUID]("ID", O.PrimaryKey)
    def timestamp = column[java.sql.Timestamp]("TIMESTAMP", O.NotNull)
    def channel = column[String]("CHANNEL", O.NotNull)
    def payload = column[String]("PAYLOAD", O.NotNull)

    def * = id ~ timestamp ~ channel ~ payload <> (TextEventLogRow, TextEventLogRow.unapply _)

    def timestampIdx = index(s"idx_${eventlogtablename}_timestamp", (timestamp))

    def insertSafe(eventLogRow: TextEventLogRow)(implicit session: Session): AlmValidation[TextEventLogRow] = {
      computeSafely {
        val insertedRows = this.insert(eventLogRow)
        if (insertedRows == 1)
          eventLogRow.success
        else
          PersistenceProblem(s"""Could not store string based eventlog row with id "${eventLogRow.id.toString()}". The returned row count was not 1. It was $insertedRows""").failure
      }
    }
  }

  override def insertEventRow(eventLogRow: TextEventLogRow): AlmValidation[TextEventLogRow] =
    computeSafely {
      db withSession { implicit session: Session =>
        TextEventLogRows.insertSafe(eventLogRow)
      }
    }

  override def getAllEventRows: AlmValidation[Seq[TextEventLogRow]] =
    inTryCatch {
      db withSession { implicit session: Session =>
        Query(TextEventLogRows).sortBy(x => x.timestamp).list
      }
  }
  
  override def getEventRowById(id: JUUID): AlmValidation[TextEventLogRow] =
    computeSafely {
      db withSession { implicit session: Session =>
        Query(TextEventLogRows).filter(_.id === id.bind).list.headOption match {
          case Some(row) => row.success
          case None => NotFoundProblem(s"""No event with id "${id.toString}" found.""").failure
        }
      }
    }

  override def getAllEventRowsFrom(from: java.sql.Timestamp): AlmValidation[Seq[TextEventLogRow]] =
    inTryCatch {
      db withSession { implicit session: Session =>
        Query(TextEventLogRows).where(row => row.timestamp >= from).sortBy(x => x.timestamp).list
      }
    }

  override def getAllEventRowsAfter(after: java.sql.Timestamp): AlmValidation[Seq[TextEventLogRow]] =
    inTryCatch {
      db withSession { implicit session: Session =>
        Query(TextEventLogRows).where(row => row.timestamp > after).sortBy(x => x.timestamp).list
      }
    }

  override def getAllEventRowsTo(to: java.sql.Timestamp): AlmValidation[Seq[TextEventLogRow]] =
    inTryCatch {
      db withSession { implicit session: Session =>
        Query(TextEventLogRows).where(row => row.timestamp <= to).sortBy(x => x.timestamp).list
      }
    }

  override def getAllEventRowsUntil(until: java.sql.Timestamp): AlmValidation[Seq[TextEventLogRow]] =
     inTryCatch {
      db withSession { implicit session: Session =>
        Query(TextEventLogRows).where(row => row.timestamp < until).sortBy(x => x.timestamp).list
      }
    }

  override def getAllEventRowsFromTo(from: java.sql.Timestamp, to: java.sql.Timestamp): AlmValidation[Seq[TextEventLogRow]] =
     inTryCatch {
      db withSession { implicit session: Session =>
        Query(TextEventLogRows).where(row => row.timestamp >= from && row.timestamp <= to).sortBy(x => x.timestamp).list
      }
    }

  override def getAllEventRowsFromUntil(from: java.sql.Timestamp, until: java.sql.Timestamp): AlmValidation[Seq[TextEventLogRow]] =
     inTryCatch {
      db withSession { implicit session: Session =>
        Query(TextEventLogRows).where(row => row.timestamp >= from && row.timestamp < until).sortBy(x => x.timestamp).list
      }
    }

  override def getAllEventRowsAfterTo(after: java.sql.Timestamp, to: java.sql.Timestamp): AlmValidation[Seq[TextEventLogRow]] =
     inTryCatch {
      db withSession { implicit session: Session =>
        Query(TextEventLogRows).where(row => row.timestamp > after && row.timestamp <= to).sortBy(x => x.timestamp).list
      }
    }

  override def getAllEventRowsAfterUntil(after: java.sql.Timestamp, until: java.sql.Timestamp): AlmValidation[Seq[TextEventLogRow]] =
     inTryCatch {
      db withSession { implicit session: Session =>
        Query(TextEventLogRows).where(row => row.timestamp > after && row.timestamp < until).sortBy(x => x.timestamp).list
      }
    }

  override def countEventRows: AlmValidation[Int] =
    inTryCatchM { db withSession { implicit session: Session => (for { row <- TextEventLogRows } yield row.length).first } }("Could not determine count for TextDomainEventLogRows")

}

trait BinaryEventLogStoreComponent extends SlickTypeMappers with EventLogStoreComponent[BinaryEventLogRow] { this: Profile =>
  import profile.simple._

  val eventlogtablename: String

  object BinaryEventLogRows extends Table[BinaryEventLogRow](eventlogtablename) {
    def id = column[JUUID]("ID", O.PrimaryKey)
    def timestamp = column[java.sql.Timestamp]("TIMESTAMP", O.NotNull)
    def channel = column[String]("CHANNEL", O.NotNull)
    def payload = column[Array[Byte]]("PAYLOAD", O.NotNull)

    def * = id ~ timestamp ~ channel ~ payload <> (BinaryEventLogRow, BinaryEventLogRow.unapply _)

    def timestampIdx = index(s"idx_${eventlogtablename}_timestamp", (timestamp))

    def insertSafe(eventLogRow: BinaryEventLogRow)(implicit session: Session): AlmValidation[BinaryEventLogRow] = {
      computeSafely {
        val insertedRows = this.insert(eventLogRow)
        if (insertedRows == 1)
          eventLogRow.success
        else
          PersistenceProblem(s"""Could not store binary based eventlog row with id "${eventLogRow.id.toString()}". The returned row count was not 1. It was $insertedRows""").failure
      }
    }
  }

  override def insertEventRow(eventLogRow: BinaryEventLogRow): AlmValidation[BinaryEventLogRow] =
    computeSafely {
      db withSession { implicit session: Session =>
        BinaryEventLogRows.insertSafe(eventLogRow)
      }
    }

  override def getAllEventRows: AlmValidation[Seq[BinaryEventLogRow]] =
    inTryCatch {
      db withSession { implicit session: Session =>
        Query(BinaryEventLogRows).sortBy(x => x.timestamp).list
      }
  }
  
  override def getEventRowById(id: JUUID): AlmValidation[BinaryEventLogRow] =
    computeSafely {
      db withSession { implicit session: Session =>
        Query(BinaryEventLogRows).filter(_.id === id.bind).list.headOption match {
          case Some(row) => row.success
          case None => NotFoundProblem(s"""No event with id "${id.toString}" found.""").failure
        }
      }
    }

  override def getAllEventRowsFrom(from: java.sql.Timestamp): AlmValidation[Seq[BinaryEventLogRow]] =
    inTryCatch {
      db withSession { implicit session: Session =>
        Query(BinaryEventLogRows).where(row => row.timestamp >= from).sortBy(x => x.timestamp).list
      }
    }

  override def getAllEventRowsAfter(after: java.sql.Timestamp): AlmValidation[Seq[BinaryEventLogRow]] =
    inTryCatch {
      db withSession { implicit session: Session =>
        Query(BinaryEventLogRows).where(row => row.timestamp > after).sortBy(x => x.timestamp).list
      }
    }

  override def getAllEventRowsTo(to: java.sql.Timestamp): AlmValidation[Seq[BinaryEventLogRow]] =
    inTryCatch {
      db withSession { implicit session: Session =>
        Query(BinaryEventLogRows).where(row => row.timestamp <= to).sortBy(x => x.timestamp).list
      }
    }

  override def getAllEventRowsUntil(until: java.sql.Timestamp): AlmValidation[Seq[BinaryEventLogRow]] =
     inTryCatch {
      db withSession { implicit session: Session =>
        Query(BinaryEventLogRows).where(row => row.timestamp < until).sortBy(x => x.timestamp).list
      }
    }

  override def getAllEventRowsFromTo(from: java.sql.Timestamp, to: java.sql.Timestamp): AlmValidation[Seq[BinaryEventLogRow]] =
     inTryCatch {
      db withSession { implicit session: Session =>
        Query(BinaryEventLogRows).where(row => row.timestamp >= from && row.timestamp <= to).sortBy(x => x.timestamp).list
      }
    }

  override def getAllEventRowsFromUntil(from: java.sql.Timestamp, until: java.sql.Timestamp): AlmValidation[Seq[BinaryEventLogRow]] =
     inTryCatch {
      db withSession { implicit session: Session =>
        Query(BinaryEventLogRows).where(row => row.timestamp >= from && row.timestamp < until).sortBy(x => x.timestamp).list
      }
    }

  override def getAllEventRowsAfterTo(after: java.sql.Timestamp, to: java.sql.Timestamp): AlmValidation[Seq[BinaryEventLogRow]] =
     inTryCatch {
      db withSession { implicit session: Session =>
        Query(BinaryEventLogRows).where(row => row.timestamp > after && row.timestamp <= to).sortBy(x => x.timestamp).list
      }
    }

  override def getAllEventRowsAfterUntil(after: java.sql.Timestamp, until: java.sql.Timestamp): AlmValidation[Seq[BinaryEventLogRow]] =
     inTryCatch {
      db withSession { implicit session: Session =>
        Query(BinaryEventLogRows).where(row => row.timestamp > after && row.timestamp < until).sortBy(x => x.timestamp).list
      }
    }

  override def countEventRows: AlmValidation[Int] =
    inTryCatchM { db withSession { implicit session: Session => (for { row <- BinaryEventLogRows } yield row.length).first } }("Could not determine count for TextDomainEventLogRows")

}
  
  