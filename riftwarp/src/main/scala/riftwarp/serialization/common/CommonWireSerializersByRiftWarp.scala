package riftwarp.serialization.common

import almhirt.serialization.HasCommonWireSerializers
import riftwarp.HasRiftWarp
import riftwarp.util.WarpWireSerializer
import almhirt.common.Event
import almhirt.common.Command

trait CommonWireSerializersByRiftWarp extends HasCommonWireSerializers { self: HasRiftWarp =>
  import riftwarp.std._
  override lazy val booleanWireSerializer = WarpWireSerializer.direct[Boolean](myRiftWarp)
  override lazy val stringWireSerializer = WarpWireSerializer.direct[String](myRiftWarp)
  override lazy val byteWireSerializer = WarpWireSerializer.direct[Byte](myRiftWarp)
  override lazy val shortWireSerializer = WarpWireSerializer.direct[Short](myRiftWarp)
  override lazy val intWireSerializer = WarpWireSerializer.direct[Int](myRiftWarp)
  override lazy val longWireSerializer = WarpWireSerializer.direct[Long](myRiftWarp)
  override lazy val bigIntWireSerializer = WarpWireSerializer.direct[BigInt](myRiftWarp)
  override lazy val floatWireSerializer = WarpWireSerializer.direct[Float](myRiftWarp)
  override lazy val doubleWireSerializer = WarpWireSerializer.direct[Double](myRiftWarp)
  override lazy val bigDecimalWireSerializer = WarpWireSerializer.direct[BigDecimal](myRiftWarp)
  override lazy val uriWireSerializer = WarpWireSerializer.direct[java.net.URI](myRiftWarp)
  override lazy val uuidWireSerializer = WarpWireSerializer.direct[java.util.UUID](myRiftWarp)
  override lazy val localDateTimeWireSerializer = WarpWireSerializer.direct[org.joda.time.LocalDateTime](myRiftWarp)
  override lazy val dateTimeWireSerializer = WarpWireSerializer.direct[org.joda.time.DateTime](myRiftWarp)
  override lazy val finiteDurationWireSerializer = WarpWireSerializer.direct[scala.concurrent.duration.FiniteDuration](myRiftWarp)

  override lazy val booleansWireSerializer = WarpWireSerializer.collectionDirect[Boolean](myRiftWarp)
  override lazy val stringsWireSerializer = WarpWireSerializer.collectionDirect[String](myRiftWarp)
  override lazy val bytesWireSerializer = WarpWireSerializer.collectionDirect[Byte](myRiftWarp)
  override lazy val shortsWireSerializer = WarpWireSerializer.collectionDirect[Short](myRiftWarp)
  override lazy val intsWireSerializer = WarpWireSerializer.collectionDirect[Int](myRiftWarp)
  override lazy val longsWireSerializer = WarpWireSerializer.collectionDirect[Long](myRiftWarp)
  override lazy val bigIntsWireSerializer = WarpWireSerializer.collectionDirect[BigInt](myRiftWarp)
  override lazy val floatsWireSerializer = WarpWireSerializer.collectionDirect[Float](myRiftWarp)
  override lazy val doublesWireSerializer = WarpWireSerializer.collectionDirect[Double](myRiftWarp)
  override lazy val bigDecimalsWireSerializer = WarpWireSerializer.collectionDirect[BigDecimal](myRiftWarp)
  override lazy val urisWireSerializer = WarpWireSerializer.collectionDirect[java.net.URI](myRiftWarp)
  override lazy val uuidsWireSerializer = WarpWireSerializer.collectionDirect[java.util.UUID](myRiftWarp)
  override lazy val localDateTimesWireSerializer = WarpWireSerializer.collectionDirect[org.joda.time.LocalDateTime](myRiftWarp)
  override lazy val dateTimesWireSerializer = WarpWireSerializer.collectionDirect[org.joda.time.DateTime](myRiftWarp)
  override lazy val finiteDurationsWireSerializer = WarpWireSerializer.collectionDirect[scala.concurrent.duration.FiniteDuration](myRiftWarp)

  override lazy val eventWireSerializer = WarpWireSerializer.event(myRiftWarp)
  override lazy val commandWireSerializer = WarpWireSerializer.command(myRiftWarp)
  override lazy val problemWireSerializer = WarpWireSerializer.problem(myRiftWarp)

  override lazy val eventsWireSerializer = WarpWireSerializer.collection[Event](myRiftWarp)
  override lazy val commandsWireSerializer = WarpWireSerializer.collection[Command](myRiftWarp)
  override lazy val problemsWireSerializer = WarpWireSerializer.collection[almhirt.common.Problem](myRiftWarp)
}