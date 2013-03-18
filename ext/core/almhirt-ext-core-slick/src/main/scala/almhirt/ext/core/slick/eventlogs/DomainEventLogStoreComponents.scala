package almhirt.ext.core.slick.eventlogs

import java.util.{ UUID => JUUID }
import org.joda.time.DateTime
import scalaz.syntax.validation._
import almhirt.common._
import almhirt.almvalidation.kit._

trait DomainEventLogStoreComponent[T] {
  def insertEventRow(eventLogRow: T)(implicit session: scala.slick.session.Session): AlmValidation[T]
  def getEventRowById(id: JUUID)(implicit session: scala.slick.session.Session): AlmValidation[T]
  def getAllEventRowsFor(aggId: JUUID)(implicit session: scala.slick.session.Session): AlmValidation[Iterable[T]]
  def getAllEventRowsForFrom(fromVersion: Long)(implicit session: scala.slick.session.Session): AlmValidation[Iterable[T]]
  def getAllEventRowsForTo(toVersion: Long)(implicit session: scala.slick.session.Session): AlmValidation[Iterable[T]]
  def getAllEventRowsForFromTo(fromVersion: Long, toVersion: Long)(implicit session: scala.slick.session.Session): AlmValidation[Iterable[T]]
  def countEventRows(implicit session: scala.slick.session.Session): AlmValidation[Int]
}

trait TextDomainEventLogStoreComponent extends SlickTypeMappers with DomainEventLogStoreComponent[TextDomainEventLogRow] { this: Profile =>
  import profile.simple._

  val eventlogtablename: String

  object TextDomainEventLogRows extends Table[TextDomainEventLogRow](eventlogtablename) {
    def id = column[JUUID]("ID")
    def aggId = column[JUUID]("AGG_ID")
    def aggVersion = column[Long]("AGG_VERSION")
    def timestamp = column[DateTime]("TIMESTAMP", O.NotNull)
    def eventtype = column[String]("EVENTTYPE", O.NotNull)
    def channel = column[String]("CHANNEL", O.NotNull)
    def payload = column[String]("PAYLOAD", O.NotNull)

    def * = id ~ aggId ~ aggVersion ~ timestamp ~ eventtype ~ channel ~ payload <> (TextDomainEventLogRow, TextDomainEventLogRow.unapply _)

    def aggIdIdx = index("idx_agg_id", aggId)
    def aggIdVersionIdx = index("idx_agg_id_version", (aggId, aggVersion))

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

  override def insertEventRow(eventLogRow: TextDomainEventLogRow)(implicit session: scala.slick.session.Session): AlmValidation[TextDomainEventLogRow] =
    TextDomainEventLogRows.insertSafe(eventLogRow)

  override def getEventRowById(id: JUUID)(implicit session: scala.slick.session.Session): AlmValidation[TextDomainEventLogRow] =
    computeSafely {
      Query(TextDomainEventLogRows).filter(_.id === id.bind).list.headOption match {
        case Some(row) => row.success
        case None => NotFoundProblem(s"""No domain event with id "${id.toString}" found.""").failure
      }
    }

  override def getAllEventRowsFor(aggId: JUUID)(implicit session: scala.slick.session.Session): AlmValidation[Iterable[TextDomainEventLogRow]] =
    inTryCatch { Query(TextDomainEventLogRows).list }

  override def getAllEventRowsForFrom(fromVersion: Long)(implicit session: scala.slick.session.Session): AlmValidation[Iterable[TextDomainEventLogRow]] =
    inTryCatch { Query(TextDomainEventLogRows).filter(x => x.aggVersion >= fromVersion).list }

  override def getAllEventRowsForTo(toVersion: Long)(implicit session: scala.slick.session.Session): AlmValidation[Iterable[TextDomainEventLogRow]] =
    inTryCatch { Query(TextDomainEventLogRows).filter(x => x.aggVersion <= toVersion).list }

  override def getAllEventRowsForFromTo(fromVersion: Long, toVersion: Long)(implicit session: scala.slick.session.Session): AlmValidation[Iterable[TextDomainEventLogRow]] =
    inTryCatch { Query(TextDomainEventLogRows).filter(x => x.aggVersion >= fromVersion && x.aggVersion <= toVersion).list }

  override def countEventRows(implicit session: scala.slick.session.Session): AlmValidation[Int] =
    inTryCatchM { (for { row <- TextDomainEventLogRows } yield row.length).first }("Could not determine count for TextDomainEventLogRows")

}

trait BinaryDomainEventLogStoreComponent extends SlickTypeMappers with DomainEventLogStoreComponent[BinaryDomainEventLogRow] { this: Profile =>
  import profile.simple._

  val eventlogtablename: String

  object BinaryDomainEventLogRows extends Table[BinaryDomainEventLogRow](eventlogtablename) {
    def id = column[JUUID]("ID")
    def aggId = column[JUUID]("AGG_ID")
    def aggVersion = column[Long]("AGG_VERSION")
    def timestamp = column[DateTime]("TIMESTAMP", O.NotNull)
    def eventtype = column[String]("EVENTTYPE", O.NotNull)
    def channel = column[String]("CHANNEL", O.NotNull)
    def payload = column[Array[Byte]]("PAYLOAD", O.NotNull)

    def * = id ~ aggId ~ aggVersion ~ timestamp ~ eventtype ~ channel ~ payload <> (BinaryDomainEventLogRow, BinaryDomainEventLogRow.unapply _)

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

  override def insertEventRow(eventLogRow: BinaryDomainEventLogRow)(implicit session: scala.slick.session.Session): AlmValidation[BinaryDomainEventLogRow] =
    BinaryDomainEventLogRows.insertSafe(eventLogRow)

  override def getEventRowById(id: JUUID)(implicit session: scala.slick.session.Session): AlmValidation[BinaryDomainEventLogRow] =
    computeSafely {
      Query(BinaryDomainEventLogRows).filter(_.id === id.bind).list.headOption match {
        case Some(row) => row.success
        case None => NotFoundProblem(s"""No domain event with id "${id.toString}" found.""").failure
      }
    }

  override def getAllEventRowsFor(aggId: JUUID)(implicit session: scala.slick.session.Session): AlmValidation[Iterable[BinaryDomainEventLogRow]] =
    inTryCatch { Query(BinaryDomainEventLogRows).list }

  override def getAllEventRowsForFrom(fromVersion: Long)(implicit session: scala.slick.session.Session): AlmValidation[Iterable[BinaryDomainEventLogRow]] =
    inTryCatch { Query(BinaryDomainEventLogRows).filter(x => x.aggVersion >= fromVersion).list }

  override def getAllEventRowsForTo(toVersion: Long)(implicit session: scala.slick.session.Session): AlmValidation[Iterable[BinaryDomainEventLogRow]] =
    inTryCatch { Query(BinaryDomainEventLogRows).filter(x => x.aggVersion <= toVersion).list }

  override def getAllEventRowsForFromTo(fromVersion: Long, toVersion: Long)(implicit session: scala.slick.session.Session): AlmValidation[Iterable[BinaryDomainEventLogRow]] =
    inTryCatch { Query(BinaryDomainEventLogRows).filter(x => x.aggVersion >= fromVersion && x.aggVersion <= toVersion).list }

  override def countEventRows(implicit session: scala.slick.session.Session): AlmValidation[Int] =
    inTryCatchM { (for { row <- BinaryDomainEventLogRows } yield row.length).first }("Could not determine count for TextDomainEventLogRows")

}