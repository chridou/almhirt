package riftwarp

import org.scalatest._
import org.scalatest.matchers.MustMatchers
import scalaz.Cord
import riftwarp.impl.dematerializers._

class JsonCordDematerializationTests extends FunSuite with MustMatchers {
  test(""""ToJsonCordDematerializermust give a value for "hello"""") {
    val dematerialized = ToJsonCordDematerializer.getString("hello")
    println(dematerialized.manifestation.toString)
    (dematerialized.manifestation.toString) must equal("\"hello\"")
  }

  test("It must be possible to create a ToJsonCordWarpSequencer"){
    val riftWarp = RiftWarp.concurrentWithDefaults()
    implicit val hasRecomposers = riftWarp.barracks
    implicit val toolShed = riftWarp.toolShed
    val sequencer = ToJsonCordWarpSequencer(NoDivertBlobDivert)
   }
  
  test("""ToJsonCordWarpSequencer give a value for "hello"""") {
    val riftWarp = RiftWarp.concurrentWithDefaults()
    implicit val hasRecomposers = riftWarp.barracks
    implicit val toolShed = riftWarp.toolShed
    val sequencer = ToJsonCordWarpSequencer(NoDivertBlobDivert)
    val res = sequencer.addString("v", "hello").dematerialize.manifestation.toString
    res must equal("""{"v":"hello"}""")
  }

}