package almhirt.corex.mongo

import java.util.{ UUID => JUUID }
import reactivemongo.bson._
import almhirt.util.UuidConverter
import org.joda.time.LocalDateTime

object BsonConverter {
  def uuidToBson(uuid: JUUID): BSONBinary =
    BSONBinary(UuidConverter.uuidToBytes(uuid), Subtype.UuidSubtype)

  def bsonToUuid(bin: BSONBinary) =
    UuidConverter.bytesToUuid(bin.value.readArray(16))
    
  def localDateTimeToBsonDateTime(dt: LocalDateTime) =
    BSONDateTime(dt.toDateTime(org.joda.time.DateTimeZone.UTC).getMillis())
}