package almhirt.serialization

import scalaz.syntax.validation._
import almhirt.common._

sealed trait BlobSerializationPolicy
final case class BlobSeparationEnabled(packer: BlobPacker) extends BlobSerializationPolicy
case object BlobSeparationDisabled extends BlobSerializationPolicy

sealed trait BlobDeserializationPolicy
final case class BlobIntegrationEnabled(unpacker: BlobUnpacker) extends BlobDeserializationPolicy
case object BlobIntegrationDisabled extends BlobDeserializationPolicy

sealed trait BlobPolicy {
  def serializationPolicy: BlobSerializationPolicy
  def deserializationPolicy: BlobDeserializationPolicy
}

case class BlobHandlingEnabled(packer: BlobPacker, unpacker: BlobUnpacker) extends BlobPolicy {
  val serializationPolicy = BlobSeparationEnabled(packer)
  val deserializationPolicy = BlobIntegrationEnabled(unpacker)
}

case object BlobHandlingDisabled extends BlobPolicy {
  val serializationPolicy = BlobSeparationDisabled
  val deserializationPolicy = BlobIntegrationDisabled
}

final case class ExtractedBlobReference(reference: BlobReference, binaryData: Array[Byte])

sealed trait BlobRepresentation

trait BlobValue extends BlobRepresentation {
  def dataAsArray: Array[Byte]
}



final case class BlobArrayValue(val data: Array[Byte]) extends BlobValue {
  override def dataAsArray: Array[Byte] = data
}

trait BlobReference extends BlobRepresentation

final case class BlobRefFilePath(path: String) extends BlobReference
final case class BlobRefByName(name: String) extends BlobReference
final case class BlobRefByUuid(uuid: java.util.UUID) extends BlobReference
final case class BlobRefByUri(uri: java.net.URI) extends BlobReference


sealed trait BlobIdentifier

case class BlobIdentifierFieldName(fieldName: String) extends BlobIdentifier
case class BlobIdentifierWithName(name: String) extends BlobIdentifier
case class BlobIdentifierWithUuid(uuid: java.util.UUID) extends BlobIdentifier
case class BlobIdentifierWithUri(uri: java.net.URI) extends BlobIdentifier
case class BlobIdentifierWithArgs(args: Map[String, Any]) extends BlobIdentifier

