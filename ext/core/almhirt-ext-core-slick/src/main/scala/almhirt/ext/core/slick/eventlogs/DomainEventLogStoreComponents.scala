package almhirt.ext.core.slick.eventlogs

import java.util.{ UUID => JUUID }
import org.joda.time.DateTime
import scalaz.syntax.validation._
import almhirt.common._
import almhirt.almvalidation.kit._

trait TextDomainEventLogStoreComponent extends SlickTypeMappers { this: Profile =>
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

trait BinaryDomainEventLogStoreComponent extends SlickTypeMappers { this: Profile =>
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