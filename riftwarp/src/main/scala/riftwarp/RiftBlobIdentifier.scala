package riftwarp

sealed trait RiftBlobIdentifier {
  def path: List[String]
}

case class PropertyPath(path: List[String]) extends RiftBlobIdentifier
case class PropertyPathAndIdentifier(path: List[String], name: String) extends RiftBlobIdentifier
case class PropertyPathAndIdentifiers(path: List[String], name: Map[String,String]) extends RiftBlobIdentifier