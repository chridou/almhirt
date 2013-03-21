package almhirt.ext.core.slick.eventlogs

import java.util.{ UUID => JUUID }
import org.joda.time.DateTime
import scalaz.syntax.validation._
import almhirt.common._
import almhirt.almvalidation.kit._
import almhirt.ext.core.slick.SlickTypeMappers

trait EventLogStoreComponent[T] {
  def insertEventRow(eventLogRow: T): AlmValidation[T]
  def getEventRowById(id: JUUID): AlmValidation[T]
  def getAllEventRows: AlmValidation[Iterable[T]]
  def getAllEventRowsFrom(from: DateTime): AlmValidation[Iterable[T]]
  def getAllEventRowsUntil(until: DateTime): AlmValidation[Iterable[T]]
  def getAllEventRowsFromUntil(from: DateTime, until: DateTime): AlmValidation[Iterable[T]]
  def countEventRows: AlmValidation[Int]
}

trait TextEventLogStoreComponent extends SlickTypeMappers with EventLogStoreComponent[TextEventLogRow] { this: Profile =>
  import profile.simple._

  val eventlogtablename: String

  object TextEventLogRows extends Table[TextEventLogRow](eventlogtablename) {
    def id = column[JUUID]("ID")
    def timestamp = column[DateTime]("TIMESTAMP", O.NotNull)
    def eventtype = column[String]("EVENTTYPE", O.NotNull)
    def channel = column[String]("CHANNEL", O.NotNull)
    def payload = column[String]("PAYLOAD", O.NotNull)

    def * = id ~ timestamp ~ eventtype ~ channel ~ payload <> (TextEventLogRow, TextEventLogRow.unapply _)

    def timestampIdx = index("idx_timestamp", timestamp)

    def insertSafe(textEventLogRow: TextEventLogRow)(implicit session: Session): AlmValidation[TextEventLogRow] = {
      computeSafely {
        val insertedRows = this.insert(textEventLogRow)
        if (insertedRows == 1)
          textEventLogRow.success
        else
          PersistenceProblem(s"""Could not store string based eventlog row with id "${textEventLogRow.id.toString()}". The returned row count was not 1. It was $insertedRows""").failure
      }
    }
  }

  override def insertEventRow(eventLogRow: TextEventLogRow): AlmValidation[TextEventLogRow] =
    computeSafely {
      getDb() withSession { implicit session: Session =>
        TextEventLogRows.insertSafe(eventLogRow)
      }
    }

  override def getEventRowById(id: JUUID): AlmValidation[TextEventLogRow] =
    computeSafely {
      getDb() withSession { implicit session: Session =>
        Query(TextEventLogRows).filter(_.id === id.bind).list.headOption match {
          case Some(row) => row.success
          case None => NotFoundProblem(s"""No event with id "${id.toString}" found.""").failure
        }
      }
    }

  override def getAllEventRows: AlmValidation[Iterable[TextEventLogRow]] =
    inTryCatch {
      getDb() withSession { implicit session: Session =>
        Query(TextEventLogRows).list
      }
    }

  override def getAllEventRowsFrom(from: DateTime): AlmValidation[Iterable[TextEventLogRow]] =
    ??? // inTryCatch { Query(TextEventLogRows).filter(row => row.timestamp >= from).list }

  override def getAllEventRowsUntil(until: DateTime): AlmValidation[Iterable[TextEventLogRow]] =
    ???

  override def getAllEventRowsFromUntil(from: DateTime, until: DateTime): AlmValidation[Iterable[TextEventLogRow]] =
    ???

  override def countEventRows: AlmValidation[Int] =
    inTryCatchM {
      getDb() withSession { implicit session: Session =>
        (for { row <- TextEventLogRows } yield row.length).first
      }
    }("Could not determine count for TextEventLogRows")

}

trait BinaryEventLogStoreComponent extends SlickTypeMappers with EventLogStoreComponent[BinaryEventLogRow] { this: Profile =>
  import profile.simple._

  val eventlogtablename: String

  object BinaryEventLogRows extends Table[BinaryEventLogRow](eventlogtablename) {
    def id = column[JUUID]("ID")
    def timestamp = column[DateTime]("TIMESTAMP", O.NotNull)
    def eventtype = column[String]("EVENTTYPE", O.NotNull)
    def channel = column[String]("CHANNEL", O.NotNull)
    def payload = column[Array[Byte]]("PAYLOAD", O.NotNull)

    def * = id ~ timestamp ~ eventtype ~ channel ~ payload <> (BinaryEventLogRow, BinaryEventLogRow.unapply _)

    def timestampIdx = index("idx_timestamp", timestamp)

    def insertSafe(binaryEventLogRow: BinaryEventLogRow)(implicit session: Session): AlmValidation[BinaryEventLogRow] = {
      computeSafely {
        val insertedRows = this.insert(binaryEventLogRow)
        if (insertedRows == 1)
          binaryEventLogRow.success
        else
          PersistenceProblem(s"""Could not store binary based eventlog row with id "${binaryEventLogRow.id.toString()}". The returned row count was not 1. It was $insertedRows""").failure
      }
    }
  }

  override def insertEventRow(eventLogRow: BinaryEventLogRow): AlmValidation[BinaryEventLogRow] =
    computeSafely {
      getDb() withSession { implicit session: Session =>
        BinaryEventLogRows.insertSafe(eventLogRow)
      }
    }

  override def getEventRowById(id: JUUID): AlmValidation[BinaryEventLogRow] =
    computeSafely {
      getDb() withSession { implicit session: Session =>
        Query(BinaryEventLogRows).filter(_.id === id.bind).list.headOption match {
          case Some(row) => row.success
          case None => NotFoundProblem(s"""No event with id "${id.toString}" found.""").failure
        }
      }
    }

  override def getAllEventRows: AlmValidation[Iterable[BinaryEventLogRow]] =
    inTryCatch {
      getDb() withSession { implicit session: Session =>
        Query(BinaryEventLogRows).list
      }
    }

  override def getAllEventRowsFrom(from: DateTime): AlmValidation[Iterable[BinaryEventLogRow]] =
    ??? // inTryCatch { Query(TextEventLogRows).filter(row => row.timestamp >= from).list }

  override def getAllEventRowsUntil(until: DateTime): AlmValidation[Iterable[BinaryEventLogRow]] =
    ???

  override def getAllEventRowsFromUntil(from: DateTime, until: DateTime): AlmValidation[Iterable[BinaryEventLogRow]] =
    ???

  override def countEventRows: AlmValidation[Int] =
    inTryCatchM {
      getDb() withSession { implicit session: Session =>
        (for { row <- BinaryEventLogRows } yield row.length).first
      }
    }("Could not determine count for BinaryEventLogRows")

}

