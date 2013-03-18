package almhirt.ext.core.slick.eventlogs

import java.util.{ UUID => JUUID }
import org.joda.time.DateTime
import scalaz.syntax.validation._
import almhirt.common._
import almhirt.almvalidation.kit._

trait DomainEventLogStoreComponent[T] {
  def insertEventRow(eventLogRow: T): AlmValidation[T]
  def getEventRowById(id: JUUID): AlmValidation[T]
  def getAllEventRowsFor(aggId: JUUID): AlmValidation[Iterable[T]]
  def getAllEventRowsForFrom(fromVersion: Long): AlmValidation[Iterable[T]]
  def getAllEventRowsForTo(toVersion: Long): AlmValidation[Iterable[T]]
  def getAllEventRowsForFromTo(fromVersion: Long, toVersion: Long): AlmValidation[Iterable[T]]
  def countEventRows: AlmValidation[Int]
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

  override def insertEventRow(eventLogRow: TextDomainEventLogRow): AlmValidation[TextDomainEventLogRow] =
    computeSafely {
      getDb() withSession { implicit session: Session =>
        TextDomainEventLogRows.insertSafe(eventLogRow)
      }
    }

  override def getEventRowById(id: JUUID): AlmValidation[TextDomainEventLogRow] =
    computeSafely {
      getDb() withSession { implicit session: Session =>
        Query(TextDomainEventLogRows).filter(_.id === id.bind).list.headOption match {
          case Some(row) => row.success
          case None => NotFoundProblem(s"""No domain event with id "${id.toString}" found.""").failure
        }
      }
    }

  override def getAllEventRowsFor(aggId: JUUID): AlmValidation[Iterable[TextDomainEventLogRow]] =
    inTryCatch { getDb() withSession { implicit session: Session => Query(TextDomainEventLogRows).list } }

  override def getAllEventRowsForFrom(fromVersion: Long): AlmValidation[Iterable[TextDomainEventLogRow]] =
    inTryCatch { getDb() withSession { implicit session: Session => Query(TextDomainEventLogRows).filter(x => x.aggVersion >= fromVersion).list } }

  override def getAllEventRowsForTo(toVersion: Long): AlmValidation[Iterable[TextDomainEventLogRow]] =
    inTryCatch { getDb() withSession { implicit session: Session => Query(TextDomainEventLogRows).filter(x => x.aggVersion <= toVersion).list } }

  override def getAllEventRowsForFromTo(fromVersion: Long, toVersion: Long): AlmValidation[Iterable[TextDomainEventLogRow]] =
    inTryCatch { getDb() withSession { implicit session: Session => Query(TextDomainEventLogRows).filter(x => x.aggVersion >= fromVersion && x.aggVersion <= toVersion).list } }

  override def countEventRows: AlmValidation[Int] =
    inTryCatchM { getDb() withSession { implicit session: Session => (for { row <- TextDomainEventLogRows } yield row.length).first } }("Could not determine count for TextDomainEventLogRows")

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

  override def insertEventRow(eventLogRow: BinaryDomainEventLogRow): AlmValidation[BinaryDomainEventLogRow] =
    computeSafely { getDb() withSession { implicit session: Session => BinaryDomainEventLogRows.insertSafe(eventLogRow) } }

  override def getEventRowById(id: JUUID): AlmValidation[BinaryDomainEventLogRow] =
    computeSafely {
      getDb() withSession { implicit session: Session =>
        Query(BinaryDomainEventLogRows).filter(_.id === id.bind).list.headOption match {
          case Some(row) => row.success
          case None => NotFoundProblem(s"""No domain event with id "${id.toString}" found.""").failure
        }
      }
    }

  override def getAllEventRowsFor(aggId: JUUID): AlmValidation[Iterable[BinaryDomainEventLogRow]] =
    inTryCatch { getDb() withSession { implicit session: Session => Query(BinaryDomainEventLogRows).list } }

  override def getAllEventRowsForFrom(fromVersion: Long): AlmValidation[Iterable[BinaryDomainEventLogRow]] =
    inTryCatch { getDb() withSession { implicit session: Session => Query(BinaryDomainEventLogRows).filter(x => x.aggVersion >= fromVersion).list } }

  override def getAllEventRowsForTo(toVersion: Long): AlmValidation[Iterable[BinaryDomainEventLogRow]] =
    inTryCatch { getDb() withSession { implicit session: Session => Query(BinaryDomainEventLogRows).filter(x => x.aggVersion <= toVersion).list } }

  override def getAllEventRowsForFromTo(fromVersion: Long, toVersion: Long): AlmValidation[Iterable[BinaryDomainEventLogRow]] =
    inTryCatch { getDb() withSession { implicit session: Session => Query(BinaryDomainEventLogRows).filter(x => x.aggVersion >= fromVersion && x.aggVersion <= toVersion).list } }

  override def countEventRows: AlmValidation[Int] =
    inTryCatchM { getDb() withSession { implicit session: Session => (for { row <- BinaryDomainEventLogRows } yield row.length).first } }("Could not determine count for TextDomainEventLogRows")

}