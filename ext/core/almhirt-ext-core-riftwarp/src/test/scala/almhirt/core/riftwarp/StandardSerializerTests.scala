package almhirt.core.riftwarp

import org.scalatest._
import org.scalatest.matchers.MustMatchers
import almhirt.common._
import almhirt.domain.DomainEventHeader
import almhirt.core._
import almhirt.core.test._
import almhirt.core.riftwarp.test._
import almhirt.ext.core.riftwarp._
import almhirt.serializing._
import almhirt.serialization._
import _root_.riftwarp._
import _root_.riftwarp.util.Serializers

class StandardSerializerTests extends FunSuite with MustMatchers {
   val hasExecutionContext = HasExecutionContext.single
   val canCreateUuidsAndDateTimes = CanCreateUuidsAndDateTimes()
  
   implicit val support = new HasExecutionContext with CanCreateUuidsAndDateTimes {
     override def getUuid() = canCreateUuidsAndDateTimes.getUuid
     override def getDateTime() = canCreateUuidsAndDateTimes.getDateTime
     override def executionContext = hasExecutionContext.executionContext
   }
   
   val riftWarp = {
     val rw = RiftWarp.concurrentWithDefaults
        rw.barracks.addDecomposer(new TestPersonCreatedDecomposer)
        rw.barracks.addRecomposer(new TestPersonCreatedRecomposer)
        rw.barracks.addDecomposer(new TestPersonNameChangedDecomposer)
        rw.barracks.addRecomposer(new TestPersonNameChangedRecomposer)
        rw.barracks.addDecomposer(new TestPersonAddressAquiredDecomposer)
        rw.barracks.addRecomposer(new TestPersonAddressAquiredRecomposer)
        rw.barracks.addDecomposer(new TestPersonMovedDecomposer)
        rw.barracks.addRecomposer(new TestPersonMovedRecomposer)
        rw.barracks.addDecomposer(new TestPersonUnhandledEventDecomposer)
        rw.barracks.addRecomposer(new TestPersonUnhandledEventRecomposer)
     rw
   }
   
   val eventSerializer = {
      val serializer = Serializers.createForStrings[Event, Event](riftWarp)
      new EventToStringSerializer {
        def serialize(channel: String)(what: Event, typeHint: Option[String]) = serializer.serialize(channel)(what, typeHint)
        def serializeAsync(channel: String)(what: Event, typeHint: Option[String]) = serializer.serializeAsync(channel)(what, typeHint)
        def serializeBlobSeparating(blobPolicy: BlobSerializationPolicy)(channel: String)(what: Event, typeHint: Option[String]) = serializer.serializeBlobSeparating(blobPolicy)(channel)(what, typeHint)
        def serializeBlobSeparatingAsync(blobPolicy: BlobSerializationPolicy)(channel: String)(what: Event, typeHint: Option[String]) = serializer.serializeBlobSeparatingAsync(blobPolicy)(channel)(what, typeHint)
        def deserialize(channel: String)(what: String, typeHint: Option[String]) = serializer.deserialize(channel)(what, typeHint)
        def deserializeAsync(channel: String)(what: String, typeHint: Option[String]) = serializer.deserializeAsync(channel)(what, typeHint)
        def deserializeBlobIntegrating(blobPolicy: BlobDeserializationPolicy)(channel: String)(what: SerializedRepr, typeHint: Option[String]) = serializer.deserializeBlobIntegrating(blobPolicy)(channel)(what, typeHint)
        def deserializeBlobIntegratingAsync(blobPolicy: BlobDeserializationPolicy)(channel: String)(what: SerializedRepr, typeHint: Option[String]): AlmFuture[Event] = serializer.deserializeBlobIntegratingAsync(blobPolicy)(channel)(what, typeHint)
      }
   }
   
   test("""The EventSerializer must serialize a "TestPersonCreated"-Event to JSON""") {
     val res = eventSerializer.serialize("json")(TestPersonCreated(DomainEventHeader(support.getUuid, 0L), "test"), None)
     println(res)
     res.isSuccess must be(true)
   }

   ignore("""The EventSerializer must deserialize a "TestPersonCreated"-Event from JSON which he created""") {
     val res = eventSerializer.serialize("json")(TestPersonCreated(DomainEventHeader(support.getUuid, 0L), "test"), None)
     println(res)
     res.isSuccess must be(true)
   }
   
   test("""The EventSerializer must serialize a "TestPersonCreated"-Event to XML""") {
     val res = eventSerializer.serialize("xml")(TestPersonCreated(DomainEventHeader(support.getUuid, 0L), "test"), None)
     println(res)
     res.isSuccess must be(true)
   }
}