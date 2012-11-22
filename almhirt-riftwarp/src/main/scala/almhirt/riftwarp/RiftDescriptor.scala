package almhirt.riftwarp

sealed trait RiftDescriptor {
}

trait RiftChannelDescriptor extends RiftDescriptor {
  def channelType: String
  def contentType: Option[String]
  def contentTypeExt: Option[String]

  def canEqual(other: Any) = {
    other.isInstanceOf[almhirt.riftwarp.RiftChannelDescriptor]
  }

  override def equals(other: Any) = {
    other match {
      case that: almhirt.riftwarp.RiftChannelDescriptor => that.canEqual(RiftChannelDescriptor.this) && channelType == that.channelType
      case _ => false
    }
  }

  override def hashCode() = {
    channelType.hashCode
  }

  override def toString() = channelType
  
}

//object RiftChannelDescriptor {
//  def apply(aChannelType: String, aContentType: Option[String], aContentTypeExt: Option[String]): RiftChannelDescriptor =
//    new RiftChannelDescriptor {
//      val channelType = aChannelType
//      val contentTypeExt = aContentType
//      val contentType = aContentTypeExt
//    }
//  
//  def apply(aChannelType: String): RiftChannelDescriptor = apply(aChannelType, None, None)
//  
//}

sealed trait RiftFullDescriptor extends RiftDescriptor with Equals {
  def channelType: RiftChannelDescriptor
  def toolGroup: ToolGroup

  def canEqual(other: Any) = {
    other.isInstanceOf[almhirt.riftwarp.RiftFullDescriptor]
  }

  override def equals(other: Any) = {
    other match {
      case that: almhirt.riftwarp.RiftFullDescriptor => that.canEqual(RiftFullDescriptor.this) && channelType == that.channelType && toolGroup == that.toolGroup
      case _ => false
    }
  }

  override def hashCode() = {
    val prime = 41
    prime * (prime + channelType.hashCode) + toolGroup.hashCode
  }
  
  override def toString() = "%s/%s".format(channelType, toolGroup)
  
}

object RiftFullDescriptor {
  def apply(aChannelType: RiftChannelDescriptor, aToolGroup: ToolGroup) =
    new RiftFullDescriptor { val channelType = aChannelType; val toolGroup= aToolGroup }
}