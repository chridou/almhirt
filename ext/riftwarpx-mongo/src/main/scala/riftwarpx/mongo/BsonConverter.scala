package riftwarpx.mongo

import java.util.{ UUID => JUUID }
import reactivemongo.bson._
import almhirt.converters.UuidConverter

object BsonConverter {
  def uuidToBson(uuid: JUUID): BSONBinary =
    BSONBinary(UuidConverter.uuidToBytes(uuid), Subtype.UuidSubtype)

  def bsonToUuid(bin: BSONBinary) =
    UuidConverter.bytesToUuid(bin.value.readArray(16))
}