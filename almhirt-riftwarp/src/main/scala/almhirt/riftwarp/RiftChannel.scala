package almhirt.riftwarp

trait RiftChannel extends Equals {
  def channelType: String
  def canEqual(other: Any) = {
    other.isInstanceOf[almhirt.riftwarp.RiftChannel]
  }

  override def equals(other: Any) = {
    other match {
      case that: almhirt.riftwarp.RiftChannel => that.canEqual(RiftChannel.this) && channelType == that.channelType
      case _ => false
    }
  }

  override def hashCode() = {
    val prime = 41
    prime + channelType.hashCode
  }
}

object RiftMap extends RiftChannel { val channelType = "map" }
object RiftJson extends RiftChannel { val channelType = "json" }
object RiftXml extends RiftChannel { val channelType = "xml" }

object RiftChannel {
  def apply(aChannelType: String): RiftChannel = new RiftChannel { val channelType = aChannelType }
}