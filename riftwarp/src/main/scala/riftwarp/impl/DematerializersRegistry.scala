package riftwarp.impl

import scalaz.syntax.validation._
import almhirt.common._
import riftwarp._
import riftwarp.std._
import almhirt.io.BinaryWriter

class DematerializersRegistry extends Dematerializers {
  private val dematerializers = new _root_.java.util.concurrent.ConcurrentHashMap[String, Dematerializer[Any]](32)

  override def add[T](dematerializer: Dematerializer[T]) {
    dematerializer.channels.foreach(channel ⇒ dematerializers.put(channel.channelDescriptor, dematerializer))
  }

  override def get(channel: String): AlmValidation[Dematerializer[Any]] =
    dematerializers.get(channel) match {
      case null ⇒ NoSuchElementProblem(s"""No Dematerialzer found  found for channel "$channel".""").failure
      case x ⇒ x.success
    }

}

object DematerializersRegistry {
  def apply(): DematerializersRegistry = {
    val reg = new DematerializersRegistry()
    reg.add(ToJsonStringDematerializer)
    reg.add(ToNoisyXmlStringDematerializer)
    reg.add(ToHtmlStringDematerializer)
    reg.add(ToWarpPackageDematerializer)
    reg.add(ToExplodedDematerializer)
    reg.add(new messagepack.ToMessagePackDematerializer { def createBinaryWriter(): BinaryWriter = BinaryWriter() })
    reg
  }

  def empty: DematerializersRegistry = new DematerializersRegistry()
}