package almhirt.corex.mongo

import almhirt.aggregates.{ AggregateRootId, AggregateRootVersion }
import reactivemongo.bson._

/**
 * @author douven
 */
package object snapshots {
  implicit object StoredSnapshotBSONMapper extends BSONDocumentWriter[StoredSnapshot] with BSONDocumentReader[StoredSnapshot] {
    def write(item: StoredSnapshot): BSONDocument =
      item match {
        case StoredBinaryVivusSnapshot(aggId, version, data) ⇒
          BSONDocument(
            "_id" -> aggId.value,
            "t" -> "vivus_bin",
            "v" -> version.value,
            "bin" -> BSONBinary(data, Subtype.GenericBinarySubtype))
        case StoredBsonVivusSnapshot(aggId, version, agg) ⇒
          BSONDocument(
            "_id" -> aggId.value,
            "t" -> "vivus_bson",
            "v" -> version.value,
            "agg" -> agg)
        case StoredMortuusSnapshot(aggId, version) ⇒
          BSONDocument(
            "_id" -> aggId.value,
            "t" -> "mortuus",
            "v" -> version.value)
      }

    def read(doc: BSONDocument): StoredSnapshot = {
      doc.getAs[String]("t").get match {
        case "vivus_bin" ⇒
          val binData = doc.getAs[BSONValue]("bin").get match {
            case BSONBinary(value, Subtype.GenericBinarySubtype) ⇒ value.readArray(value.size)
            case _ ⇒ throw new Exception("BSONBinary with Subtype.GenericBinarySubtype expected at 'bin'.")
          }
          StoredBinaryVivusSnapshot(
            AggregateRootId(doc.getAs[String]("_id").get),
            AggregateRootVersion(doc.getAs[Long]("v").get),
            binData)
        case "vivus_bson" ⇒
          StoredBsonVivusSnapshot(
            AggregateRootId(doc.getAs[String]("_id").get),
            AggregateRootVersion(doc.getAs[Long]("v").get),
            doc.getAs[BSONDocument]("agg").get)
        case "mortuus" ⇒
          StoredMortuusSnapshot(
            AggregateRootId(doc.getAs[String]("_id").get),
            AggregateRootVersion(doc.getAs[Long]("v").get))
        case x ⇒ throw new Exception(s"'x' is not a valid type marker for a stored snapshot")
      }
    }
  }

}