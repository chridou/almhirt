package almhirt.ext.core.slick.eventlogs

import java.util.{ UUID => JUUID }
import org.joda.time.{ DateTime, DateTimeZone }
import scalaz.syntax.validation._
import almhirt.common._
import almhirt.almvalidation.kit._

trait EventLogTypeMappers { this: Profile =>
  import profile.simple._
  import java.sql.Timestamp
  implicit val JodaTimeToSqlTimestampMapper: TypeMapper[DateTime] =
    MappedTypeMapper.base[DateTime, Timestamp](
      dateTime => new Timestamp(dateTime.getMillis),
      timestamp => new DateTime(timestamp.getTime, DateTimeZone.UTC))
}

trait BlobStoreComponent { this: Profile =>
  import profile.simple._

  val blobtablename: String

  object BlobRows extends Table[BlobRow](blobtablename) {
    def id = column[JUUID]("ID", O.PrimaryKey)
    def data = column[Array[Byte]]("DATA", O.NotNull)

    def * = id ~ data <> (BlobRow, BlobRow.unapply _)

    def insertSafe(blobRow: BlobRow)(implicit session: Session): AlmValidation[BlobRow] = {
      computeSafely {
        val insertedRows = this.insert(blobRow)
        if (insertedRows == 1)
          blobRow.success
        else
          PersistenceProblem(s"""Could not store blob row with id "${blobRow.id.toString()}". The returned row count was not 1. It was $insertedRows""").failure
      }
    }
  }
}

trait TextEventLogStoreComponent extends EventLogTypeMappers { this: Profile =>
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
}

trait BinaryEventLogStoreComponent extends EventLogTypeMappers { this: Profile =>
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
}


trait TextDomainEventLogStoreComponent extends EventLogTypeMappers { this: Profile =>
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
}

trait BinaryDomainEventLogStoreComponent extends EventLogTypeMappers { this: Profile =>
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
}