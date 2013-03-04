package riftwarp

sealed trait RiftBlobIdentifier {
  def ident: String
}

case class RiftBlobIdentifierSimple(ident: String) extends RiftBlobIdentifier
case class RiftBlobIdentifierWithName(ident: String, name: String) extends RiftBlobIdentifier
case class RiftBlobIdentifierWithArgs(ident: String, args: Map[String,String]) extends RiftBlobIdentifier