package almhirt.corex.mongo

import java.util.{ UUID => JUUID }
import reactivemongo.bson._
import almhirt.util.UuidConverter

object BsonConverter {
  def uuidToBson(uuid: JUUID): BSONBinary =
    BSONBinary(UuidConverter.uuidToBytes(uuid), Subtype.OldUuidSubtype)

  def bsonToUuid(bin: BSONBinary) =
    UuidConverter.bytesToUuid(bin.value.readArray(16))
}