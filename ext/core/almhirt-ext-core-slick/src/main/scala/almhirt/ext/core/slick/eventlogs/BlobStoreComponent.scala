package almhirt.ext.core.slick.eventlogs

import java.util.{ UUID => JUUID }
import scalaz.syntax.validation._
import almhirt.common._
import almhirt.almvalidation.kit._

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
  
  def insertBlobRow(blobRow: BlobRow)(implicit session: Session): AlmValidation[BlobRow] =
    BlobRows.insertSafe(blobRow)
  
}
