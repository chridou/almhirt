//package almhirt.core.riftwarp
//
//import org.scalatest._
//import org.scalatest.matchers.MustMatchers
//import almhirt.common._
//import almhirt.domain.DomainEventHeader
//import almhirt.core._
//import almhirt.core.test._
//import almhirt.core.riftwarp.test._
//import almhirt.corex.riftwarp._
//import almhirt.serializing._
//import almhirt.serialization._
//import _root_.riftwarp._
//import _root_.riftwarp.util.Serializers
//
//class StandardSerializerTests extends FunSuite with MustMatchers {
//   val hasExecutionContext = HasExecutionContext.single
//   val canCreateUuidsAndDateTimes = CanCreateUuidsAndDateTimes()
//  
//   implicit val support = new HasExecutionContext with CanCreateUuidsAndDateTimes {
//     override def getUuid() = canCreateUuidsAndDateTimes.getUuid
//     override def getDateTime() = canCreateUuidsAndDateTimes.getDateTime
//     override def executionContext = hasExecutionContext.executionContext
//   }
//   
//   val riftWarp = {
//     val rw = RiftWarp()
//    rw.packers.addTyped(TestPersonCreatedPacker)
//    rw.unpackers.addTyped(TestPersonCreatedWarpUnpacker)
//    rw.packers.addTyped(TestPersonNameChangedPacker)
//    rw.unpackers.addTyped(TestPersonNameChangedWarpUnpacker)
//    rw.packers.addTyped(TestPersonAddressAquiredPacker)
//    rw.unpackers.addTyped(TestPersonAddressAquiredWarpUnpacker)
//    rw.packers.addTyped(TestPersonMovedPacker)
//    rw.unpackers.addTyped(TestPersonMovedWarpUnpacker)
//    rw.packers.addTyped(TestPersonUnhandledEventPacker)
//    rw.unpackers.addTyped(TestPersonUnhandledEventWarpUnpacker)
//    rw
//   }
//   
//   val eventSerializer = {
//      val serializer = Serializers.createForStrings[Event, Event](riftWarp)
//      new EventToStringSerializer {
//        def serialize(channel: String)(what: Event, options: Map[String, Any] = Map.empty) = serializer.serialize(channel)(what, options)
//        def deserialize(channel: String)(what: SerializedRepr, options: Map[String, Any] = Map.empty) = serializer.deserialize(channel)(what, options)
//      }
//   }
//   
//   test("""The EventSerializer must serialize a "TestPersonCreated"-Event to JSON""") {
//     val res = eventSerializer.serialize("json")(TestPersonCreated(DomainEventHeader((support.getUuid, 0L)), "test"))
//     res.isSuccess must be(true)
//   }
//
//   ignore("""The EventSerializer must deserialize a "TestPersonCreated"-Event from JSON which he created""") {
//     val res = eventSerializer.serialize("json")(TestPersonCreated(DomainEventHeader((support.getUuid, 0L)), "test"))
//     res.isSuccess must be(true)
//   }
//   
//   test("""The EventSerializer must serialize a "TestPersonCreated"-Event to XML""") {
//     val res = eventSerializer.serialize("xml")(TestPersonCreated(DomainEventHeader(support.getUuid, 0L), "test"))
//     res.isSuccess must be(true)
//   }
//}