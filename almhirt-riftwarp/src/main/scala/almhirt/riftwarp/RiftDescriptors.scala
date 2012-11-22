package almhirt.riftwarp

case class RiftMap() extends RiftChannelDescriptor {
  val channelType = "channel_map"
  val contentTypeExt = None
  val contentType = None
}
case class RiftJson() extends RiftChannelDescriptor  {
  val channelType = "channel_json"
  val contentTypeExt = Some("json")
  val contentType = Some("text/x-json")
}
case class RiftBson() extends RiftChannelDescriptor  {
  val channelType = "channel_bson"
  val contentTypeExt = None
  val contentType = None
}
case class RiftXml() extends RiftChannelDescriptor  {
  val channelType = "channel_xml"
  val contentTypeExt = Some("xml")
  val contentType = Some("text/xml")
}
case class RiftMessagePack() extends RiftChannelDescriptor   {
  val channelType = "channel_msgpack"
  val contentTypeExt = None
  val contentType = None
}
case class RiftProtocolBuffers() extends RiftChannelDescriptor  {
  val channelType = "channel_protobuf"
  val contentTypeExt = None
  val contentType = None
}
case class RiftThrift() extends RiftChannelDescriptor  {
  val channelType = "channel_thrift"
  val contentTypeExt = None
  val contentType = None
}
case class RiftYaml() extends RiftChannelDescriptor  {
  val channelType = "channel_yaml"
  val contentTypeExt = None
  val contentType = None
}

object RiftChannel{
  val rawMap = new RiftMap 
  val json = new RiftMap 
  val bson = new RiftBson 
  val xml = new RiftXml 
  val messagePack = new RiftMessagePack 
  val protobuf = new RiftProtocolBuffers 
  val thrift = new RiftThrift 
  val yaml = new RiftYaml 
}

