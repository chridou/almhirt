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
        def serialize(channel: String)(what: Event, typeHint: Option[String], args: Map[String, Any])  = serializer.serialize(channel)(what, typeHint, args)
        def serializeAsync(channel: String)(what: Event, typeHint: Option[String], args: Map[String, Any]) = serializer.serializeAsync(channel)(what, typeHint, args)
        def deserialize(channel: String)(what: String, typeHint: Option[String], args: Map[String, Any]) = serializer.deserialize(channel)(what, typeHint, args)
        def deserializeAsync(channel: String)(what: String, typeHint: Option[String], args: Map[String, Any]) = serializer.deserializeAsync(channel)(what, typeHint, args)
      }
   }
   
   test("""The EventSerializer must serialize a "TestPersonCreated"-Event to JSON""") {
     val res = eventSerializer.serialize("json")(TestPersonCreated(DomainEventHeader((support.getUuid, 0L)), "test"), None)
     res.isSuccess must be(true)
   }

   ignore("""The EventSerializer must deserialize a "TestPersonCreated"-Event from JSON which he created""") {
     val res = eventSerializer.serialize("json")(TestPersonCreated(DomainEventHeader((support.getUuid, 0L)), "test"), None)
     res.isSuccess must be(true)
   }
   
   test("""The EventSerializer must serialize a "TestPersonCreated"-Event to XML""") {
     val res = eventSerializer.serialize("xml")(TestPersonCreated(DomainEventHeader(support.getUuid, 0L), "test"), None)
     res.isSuccess must be(true)
   }
}