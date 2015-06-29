package almhirt.corex.mongo

import almhirt.aggregates.{ AggregateRootId, AggregateRootVersion }
import reactivemongo.bson._

/**
 * @author douven
 */
package object snapshots {
  implicit object PersistableSnapshotStateBSONMapper extends BSONDocumentWriter[PersistableSnapshotState] with BSONDocumentReader[PersistableSnapshotState] {
    def write(item: PersistableSnapshotState): BSONDocument =
      item match {
        case PersistableBinaryVivusSnapshotState(aggId, version, data) ⇒
          BSONDocument(
            "_id" -> aggId.value,
            "t" -> "vivus_bin",
            "v" -> version.value,
            "bin" -> BSONBinary(data, Subtype.GenericBinarySubtype))
        case PersistableBsonVivusSnapshotState(aggId, version, agg) ⇒
          BSONDocument(
            "_id" -> aggId.value,
            "t" -> "vivus_bson",
            "v" -> version.value,
            "agg" -> agg)
        case PersistableMortuusSnapshotState(aggId, version) ⇒
          BSONDocument(
            "_id" -> aggId.value,
            "t" -> "mortuus",
            "v" -> version.value)
      }

    def read(doc: BSONDocument): PersistableSnapshotState = {
      doc.getAs[String]("t").get match {
        case "vivus_bin" ⇒
          val binData = doc.getAs[BSONValue]("bin").get match {
            case BSONBinary(value, Subtype.GenericBinarySubtype) ⇒ value.readArray(value.size)
            case _ ⇒ throw new Exception("BSONBinary with Subtype.GenericBinarySubtype expected at 'bin'.")
          }
          PersistableBinaryVivusSnapshotState(
            AggregateRootId(doc.getAs[String]("_id").get),
            AggregateRootVersion(doc.getAs[Long]("v").get),
            binData)
        case "vivus_bson" ⇒
          PersistableBsonVivusSnapshotState(
            AggregateRootId(doc.getAs[String]("_id").get),
            AggregateRootVersion(doc.getAs[Long]("v").get),
            doc.getAs[BSONDocument]("agg").get)
        case "mortuus" ⇒
          PersistableMortuusSnapshotState(
            AggregateRootId(doc.getAs[String]("_id").get),
            AggregateRootVersion(doc.getAs[Long]("v").get))
        case x ⇒ throw new Exception(s"'x' is not a valid type marker for a stored snapshot")
      }
    }
  }

}