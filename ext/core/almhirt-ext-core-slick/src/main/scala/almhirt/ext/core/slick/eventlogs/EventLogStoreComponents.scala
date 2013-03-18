package almhirt.ext.core.slick.eventlogs

import java.util.{ UUID => JUUID }
import org.joda.time.DateTime
import scalaz.syntax.validation._
import almhirt.common._
import almhirt.almvalidation.kit._

trait TextEventLogStoreComponent extends SlickTypeMappers { this: Profile =>
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

trait BinaryEventLogStoreComponent extends SlickTypeMappers { this: Profile =>
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


