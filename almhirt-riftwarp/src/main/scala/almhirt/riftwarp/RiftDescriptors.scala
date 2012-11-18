package almhirt.riftwarp

object RiftMap extends RiftChannelDescriptor {
  val channelType = "channel_map"
  val contentTypeExt = None
  val contentType = None
}
object RiftJson extends RiftChannelDescriptor {
  val channelType = "channel_json"
  val contentTypeExt = Some("json")
  val contentType = Some("text/x-json")
}
object RiftBson extends RiftChannelDescriptor {
  val channelType = "channel_bson"
  val contentTypeExt = None
  val contentType = None
}
object RiftXml extends RiftChannelDescriptor {
  val channelType = "channel_xml"
  val contentTypeExt = Some("xml")
  val contentType = Some("text/xml")
}
object RiftMessagePack extends RiftChannelDescriptor {
  val channelType = "channel_msgpack"
  val contentTypeExt = None
  val contentType = None
}
object RiftProtocolBuffers extends RiftChannelDescriptor {
  val channelType = "channel_protobuf"
  val contentTypeExt = None
  val contentType = None
}
object RiftThrift extends RiftChannelDescriptor {
  val channelType = "channel_thrift"
  val contentTypeExt = None
  val contentType = None
}
object RiftYaml extends RiftChannelDescriptor {
  val channelType = "channel_yaml"
  val contentTypeExt = None
  val contentType = None
}

