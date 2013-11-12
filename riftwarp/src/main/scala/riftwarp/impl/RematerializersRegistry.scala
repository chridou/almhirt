package riftwarp.impl


import scalaz._, Scalaz._
import almhirt.common._
import riftwarp._
import riftwarp.std._

class RematerializersRegistry extends Rematerializers {
  private val rematerializers = new _root_.java.util.concurrent.ConcurrentHashMap[String, Rematerializer[_]](32)

  override def add[T](rematerializer: Rematerializer[T]) {
    rematerializers.put(rematerializer.channel.channelDescriptor, rematerializer)
  }
    
  override def get(channel: String): AlmValidation[Rematerializer[_]] =
    rematerializers.get(channel) match {
      case null => NoSuchElementProblem(s"""No Rematerialzer found  found for channel "$channel".""").failure
      case x => x.success
    }

}

object RematerializersRegistry {
  def apply(): RematerializersRegistry = {
    val messagePackRematerializer = new messagepack.FromMessagePackByteArrayRematerializer{}
    val reg = new RematerializersRegistry()
    reg.add(FromJsonStringRematerializer)
    reg.add(FromJsonCordRematerializer)
    reg.add(FromXmlStringRematerializer)
    reg.add(FromWarpPackageRematerializer)
    reg.add(messagePackRematerializer)
    reg
  }
  
  def empty: RematerializersRegistry = new RematerializersRegistry()
}