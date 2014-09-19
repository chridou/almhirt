package riftwarp.messagepack

import org.scalatest._
import java.util.{ UUID ⇒ JUUID }
import org.joda.time.{ DateTime, LocalDateTime }
import scalaz._, Scalaz._
import almhirt.common._
import almhirt.almvalidation.kit._
import riftwarp._
import riftwarp.std._
import riftwarp.std.kit._
import riftwarp.std.default._
import scala.concurrent.duration.FiniteDuration
import almhirt.io.BinaryWriter

class MessagePackTreeSerializationTests extends FunSuite with Matchers {
  implicit val packers = Serialization.addPackers(WarpPackers())
  implicit val unpackers = Serialization.addUnpackers(WarpUnpackers())

  test(s"""WarpTree("A") should dematerialize and rematerialize""") {
    val sample = WarpTree(WarpString("A").asInstanceOf[WarpPackage].leaf)
    val dematerialized = sample.dematerialize[Array[Byte] @@ WarpTags.MessagePack]
    val rematerialized = dematerialized.rematerialize.forceResult
  }

  test(s"""WarpTree("A(B(), C(D(), E()))") should dematerialize and rematerialize""") {
    val sample = WarpTree(
      WarpString("A").asInstanceOf[WarpPackage].node(
        WarpString("B").asInstanceOf[WarpPackage].leaf,
        WarpString("C").asInstanceOf[WarpPackage].node(
          WarpString("D").asInstanceOf[WarpPackage].leaf,
          WarpString("E").asInstanceOf[WarpPackage].leaf)))
    val dematerialized = sample.dematerialize[Array[Byte] @@ WarpTags.MessagePack]
    val rematerialized = dematerialized.rematerialize.forceResult
  }
}