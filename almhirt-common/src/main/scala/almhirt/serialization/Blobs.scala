package almhirt.serialization

import scalaz.syntax.validation._
import almhirt.common._

sealed trait BlobHandlingPolicy
case object BlobHandlingEnabled extends BlobHandlingPolicy
case object BlobHandlingDisabled extends BlobHandlingPolicy

final case class ExtractedBlobReference(reference: BlobReference, binaryData: Array[Byte])

sealed trait BlobRepresentation

trait BlobValue extends BlobRepresentation {
  def dataAsArray: AlmValidation[Array[Byte]]
}


final case class BlobArrayValue(val data: Array[Byte]) extends BlobValue {
  override def dataAsArray: AlmValidation[Array[Byte]] = data.success
}

trait BlobReference extends BlobRepresentation

final case class BlobRefFilePath(path: String) extends BlobReference
final case class BlobRefByName(name: String) extends BlobReference
final case class BlobRefByUuid(uuid: java.util.UUID) extends BlobReference
final case class BlobRefByUri(uri: java.net.URI) extends BlobReference


sealed trait BlobIdentifier {
  def fieldName: String
}

case class BlobIdentifierFieldName(fieldName: String) extends BlobIdentifier
case class BlobIdentifierWithName(fieldName: String, name: String) extends BlobIdentifier
case class BlobIdentifierWithUuid(fieldName: String, uuid: java.util.UUID) extends BlobIdentifier
