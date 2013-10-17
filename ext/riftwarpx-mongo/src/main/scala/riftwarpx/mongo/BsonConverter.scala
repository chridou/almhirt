package riftwarpx.mongo

import java.util.{ UUID => JUUID }
import reactivemongo.bson._
import almhirt.converters.BinaryConverter

object BsonConverter {
  def uuidToBson(uuid: JUUID): BSONBinary =
    BSONBinary(BinaryConverter.uuidToBytes(uuid), Subtype.UuidSubtype)

  def bsonToUuid(bin: BSONBinary) =
    BinaryConverter.bytesToUuid(bin.value.readArray(16))
}