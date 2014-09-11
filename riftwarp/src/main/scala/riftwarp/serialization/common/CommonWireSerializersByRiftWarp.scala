package riftwarp.serialization.common

import almhirt.http.HasCommonHttpSerializers
import riftwarp.HasRiftWarp
import riftwarp.util.WarpHttpSerializer
import almhirt.common.Event
import almhirt.common.Command

trait CommonHttpSerializersByRiftWarp extends HasCommonHttpSerializers { self: HasRiftWarp =>
  import riftwarp.std._
  override lazy val booleanHttpSerializer = WarpHttpSerializer.direct[Boolean](myRiftWarp)
  override lazy val stringHttpSerializer = WarpHttpSerializer.direct[String](myRiftWarp)
  override lazy val byteHttpSerializer = WarpHttpSerializer.direct[Byte](myRiftWarp)
  override lazy val shortHttpSerializer = WarpHttpSerializer.direct[Short](myRiftWarp)
  override lazy val intHttpSerializer = WarpHttpSerializer.direct[Int](myRiftWarp)
  override lazy val longHttpSerializer = WarpHttpSerializer.direct[Long](myRiftWarp)
  override lazy val bigIntHttpSerializer = WarpHttpSerializer.direct[BigInt](myRiftWarp)
  override lazy val floatHttpSerializer = WarpHttpSerializer.direct[Float](myRiftWarp)
  override lazy val doubleHttpSerializer = WarpHttpSerializer.direct[Double](myRiftWarp)
  override lazy val bigDecimalHttpSerializer = WarpHttpSerializer.direct[BigDecimal](myRiftWarp)
  override lazy val uriHttpSerializer = WarpHttpSerializer.direct[java.net.URI](myRiftWarp)
  override lazy val uuidHttpSerializer = WarpHttpSerializer.direct[java.util.UUID](myRiftWarp)
  override lazy val localDateTimeHttpSerializer = WarpHttpSerializer.direct[org.joda.time.LocalDateTime](myRiftWarp)
  override lazy val dateTimeHttpSerializer = WarpHttpSerializer.direct[org.joda.time.DateTime](myRiftWarp)
  override lazy val finiteDurationHttpSerializer = WarpHttpSerializer.direct[scala.concurrent.duration.FiniteDuration](myRiftWarp)

  override lazy val booleansHttpSerializer = WarpHttpSerializer.collectionDirect[Boolean](myRiftWarp)
  override lazy val stringsHttpSerializer = WarpHttpSerializer.collectionDirect[String](myRiftWarp)
  override lazy val bytesHttpSerializer = WarpHttpSerializer.collectionDirect[Byte](myRiftWarp)
  override lazy val shortsHttpSerializer = WarpHttpSerializer.collectionDirect[Short](myRiftWarp)
  override lazy val intsHttpSerializer = WarpHttpSerializer.collectionDirect[Int](myRiftWarp)
  override lazy val longsHttpSerializer = WarpHttpSerializer.collectionDirect[Long](myRiftWarp)
  override lazy val bigIntsHttpSerializer = WarpHttpSerializer.collectionDirect[BigInt](myRiftWarp)
  override lazy val floatsHttpSerializer = WarpHttpSerializer.collectionDirect[Float](myRiftWarp)
  override lazy val doublesHttpSerializer = WarpHttpSerializer.collectionDirect[Double](myRiftWarp)
  override lazy val bigDecimalsHttpSerializer = WarpHttpSerializer.collectionDirect[BigDecimal](myRiftWarp)
  override lazy val urisHttpSerializer = WarpHttpSerializer.collectionDirect[java.net.URI](myRiftWarp)
  override lazy val uuidsHttpSerializer = WarpHttpSerializer.collectionDirect[java.util.UUID](myRiftWarp)
  override lazy val localDateTimesHttpSerializer = WarpHttpSerializer.collectionDirect[org.joda.time.LocalDateTime](myRiftWarp)
  override lazy val dateTimesHttpSerializer = WarpHttpSerializer.collectionDirect[org.joda.time.DateTime](myRiftWarp)
  override lazy val finiteDurationsHttpSerializer = WarpHttpSerializer.collectionDirect[scala.concurrent.duration.FiniteDuration](myRiftWarp)

  override lazy val eventHttpSerializer = WarpHttpSerializer.event(myRiftWarp)
  override lazy val commandHttpSerializer = WarpHttpSerializer.command(myRiftWarp)
  override lazy val problemHttpSerializer = WarpHttpSerializer.problem(myRiftWarp)
  override lazy val commandResponseHttpSerializer = WarpHttpSerializer.commandResponse(myRiftWarp)

  override lazy val eventsHttpSerializer = WarpHttpSerializer.collection[Event](myRiftWarp)
  override lazy val commandsHttpSerializer = WarpHttpSerializer.collection[Command](myRiftWarp)
  override lazy val problemsHttpSerializer = WarpHttpSerializer.collection[almhirt.common.Problem](myRiftWarp)
}