package almhirt.riftwarp


trait RiftChannel {
  def channelType: String
  def contentType: Option[String]
  def contentTypeExt: Option[String]

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
    channelType.hashCode
  }

  override def toString() = channelType
  
}

class RiftMap() extends RiftChannel {
  val channelType = "channel_map"
  val contentTypeExt = None
  val contentType = None
}
object RiftMap {
  private val theInstance = new RiftMap()
  def apply() = theInstance
}

class RiftJson() extends RiftChannel {
  val channelType = "channel_json"
  val contentTypeExt = Some("json")
  val contentType = Some("text/x-json")
}
object RiftJson {
  private val theInstance = new RiftJson()
  def apply() = theInstance
}

class RiftBson() extends RiftChannel {
  val channelType = "channel_bson"
  val contentTypeExt = None
  val contentType = None
}
object RiftBson {
  private val theInstance = new RiftBson()
  def apply() = theInstance
}

class RiftXml() extends RiftChannel {
  val channelType = "channel_xml"
  val contentTypeExt = Some("xml")
  val contentType = Some("text/xml")
}
object RiftXml {
  private val theInstance = new RiftXml()
  def apply() = theInstance
}

class RiftMessagePack() extends RiftChannel {
  val channelType = "channel_msgpack"
  val contentTypeExt = None
  val contentType = None
}
object RiftMessagePack {
  private val theInstance = new RiftMessagePack()
  def apply() = theInstance
}

class RiftProtocolBuffers() extends RiftChannel {
  val channelType = "channel_protobuf"
  val contentTypeExt = None
  val contentType = None
}
object RiftProtocolBuffers {
  private val theInstance = new RiftProtocolBuffers()
  def apply() = theInstance
}

class RiftThrift() extends RiftChannel {
  val channelType = "channel_thrift"
  val contentTypeExt = None
  val contentType = None
}
object RiftThrift {
  private val theInstance = new RiftThrift()
  def apply() = theInstance
}

class RiftYaml() extends RiftChannel {
  val channelType = "channel_yaml"
  val contentTypeExt = None
  val contentType = None
}
object RiftYaml {
  private val theInstance = new RiftYaml()
  def apply() = theInstance
}

object RiftChannel {
  val Map = RiftMap()
  val Json = RiftJson()
  val Bson = RiftBson()
  val Xml = RiftXml()
  val MessagePack = RiftMessagePack()
  val Protobuf = RiftProtocolBuffers()
  val Thrift = RiftThrift()
  val Yaml = RiftYaml()
}
