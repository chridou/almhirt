package almhirt.corex.mongo

import java.util.{ UUID ⇒ JUUID }
import reactivemongo.bson._
import almhirt.converters.BinaryConverter
import org.joda.time.LocalDateTime

object BsonConverter {
  def uuidToBson(uuid: JUUID): BSONBinary =
    BSONBinary(BinaryConverter.uuidToBytes(uuid), Subtype.UuidSubtype)

  def bsonToUuid(bin: BSONBinary) =
    BinaryConverter.bytesToUuid(bin.value.readArray(16))
    
  def localDateTimeToBsonDateTime(dt: LocalDateTime) =
    BSONDateTime(dt.toDateTime(org.joda.time.DateTimeZone.UTC).getMillis())
}