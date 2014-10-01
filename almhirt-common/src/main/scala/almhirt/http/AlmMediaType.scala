package almhirt.http

import scalaz._, Scalaz._

sealed trait MediaTypeVendorPart {
  def value: Option[String] =
    this match {
      case NoVendor ⇒ None
      case UnspecifiedVendor ⇒ Some("vnd")
      case SpecificVendor(vendor) ⇒ Some(s"vnd.$vendor")
    }
}

case object NoVendor extends MediaTypeVendorPart
final case class SpecificVendor(vendor: String) extends MediaTypeVendorPart
case object UnspecifiedVendor extends MediaTypeVendorPart

sealed trait MediaContentPart {
  def content: String
  
  final def contentFormat = this match {
    case RawContent(content) ⇒ content
    case StructuredContent(_, format) ⇒ format
  }
  
  final def value: String = this match {
    case RawContent(content) ⇒ content
    case StructuredContent(content, format) ⇒ s"$content+$format"
  }
}
/** e.g, "jpg", "json" */
final case class RawContent(content: String) extends MediaContentPart
/** e.g. "myEntity+xml" or "svg+xml" ... */
final case class StructuredContent(content: String, format: String) extends MediaContentPart

final case class AlmMediaSubTypeParts(vendor: MediaTypeVendorPart, content: MediaContentPart) {
  def value: String = {
    val vnd = vendor.value.map(_ + ".") | ""
    s"$vnd${content.value}"
  }

  def contentValue: String = content.content
  def contentFormat: String = content.contentFormat
}

object AlmMediaSubTypeParts {
  def apply(content: String): AlmMediaSubTypeParts = AlmMediaSubTypeParts(NoVendor, RawContent(content))
  def apply(content: String, format: String): AlmMediaSubTypeParts = AlmMediaSubTypeParts(NoVendor, StructuredContent(content, format))
  def apply(vendor: String, content: String, format: String): AlmMediaSubTypeParts = AlmMediaSubTypeParts(SpecificVendor(vendor), StructuredContent(content, format))
}

sealed trait MediaTypeRepresentation
case object BinaryMedia extends MediaTypeRepresentation
case class TextualMedia(preferredEncoding: Option[AlmCharacterEncoding]) extends MediaTypeRepresentation

final case class AlmMediaType(
  mainType: String,
  subTypeParts: AlmMediaSubTypeParts,
  compressible: Boolean,
  streamRepresentation: MediaTypeRepresentation,
  fileExtensions: Seq[String],
  ianaRegistered: Boolean) {
  final def subTypeValue: String = subTypeParts.value
  final def value: String = s"$mainType/$subTypeValue"
  final def binary = streamRepresentation == BinaryMedia
  final def contentValue = subTypeParts.contentValue
  final def contentFormat = subTypeParts.contentFormat
  
  def matches(other: AlmMediaType): Boolean =
    this.mainType == other.mainType &&
    this.subTypeParts == other.subTypeParts 
}

object AlmMediaType {
  implicit class AlmMediaTypeOps(self: AlmMediaType) {
    def makeIanaRegistered = AlmMediaType(self.mainType, self.subTypeParts, self.compressible, self.streamRepresentation, self.fileExtensions, true)
    def makeCompressible = AlmMediaType(self.mainType, self.subTypeParts, true, self.streamRepresentation, self.fileExtensions, self.ianaRegistered)
    def addFileExtensions(fileExtensions: String*) = AlmMediaType(self.mainType, self.subTypeParts, self.compressible, self.streamRepresentation, self.fileExtensions ++ fileExtensions, self.ianaRegistered)
    def makeTextualWithEncoding(encoding: String) = AlmMediaType(self.mainType, self.subTypeParts, self.compressible, TextualMedia(None), self.fileExtensions, self.ianaRegistered)
    def makeTextual(preferredEncoding: AlmCharacterEncoding) = AlmMediaType(self.mainType, self.subTypeParts, self.compressible, TextualMedia(Some(preferredEncoding)), self.fileExtensions, self.ianaRegistered)
    def makeBinary(encoding: String) = AlmMediaType(self.mainType, self.subTypeParts, self.compressible, BinaryMedia, self.fileExtensions, self.ianaRegistered)
    def registered = {
      AlmMediaTypes.register(self)
      self
    }
  }

  def binary(mainType: String, rawMedia: String): AlmMediaType =
    AlmMediaType(mainType, AlmMediaSubTypeParts(rawMedia), false, BinaryMedia, Seq.empty, false)

  def binaryImage(imageType: String): AlmMediaType =
    binary("image", imageType)

  def structuredImageTextual(imageType: String, format: String): AlmMediaType =
    AlmMediaType("image", AlmMediaSubTypeParts(imageType, format), false, TextualMedia(None), Seq.empty, false)

  def applicationTextual(subType: AlmMediaSubTypeParts): AlmMediaType =
    AlmMediaType("application", subType, false, TextualMedia(None), Seq.empty, false)
    
  def applicationTextual(vendor: String, content: String, format: String): AlmMediaType =
    applicationTextual(AlmMediaSubTypeParts(vendor, content, format))

  def applicationTextual(content: String): AlmMediaType =
    applicationTextual(AlmMediaSubTypeParts(content))

  def applicationTextual(vendor: MediaTypeVendorPart, content: String, format: String): AlmMediaType =
    applicationTextual(AlmMediaSubTypeParts(vendor, StructuredContent(content, format)))

 
  def applicationBinary(subType: AlmMediaSubTypeParts): AlmMediaType =
    AlmMediaType("application", subType, false, BinaryMedia, Seq.empty, false)
    
  def applicationBinary(vendor: String, content: String, format: String): AlmMediaType =
    applicationBinary(AlmMediaSubTypeParts(vendor, content, format))

  def applicationBinary(content: String): AlmMediaType =
    applicationBinary(AlmMediaSubTypeParts(NoVendor, RawContent(content)))

  def applicationBinary(vendor: MediaTypeVendorPart, content: String, format: String): AlmMediaType =
    applicationBinary(AlmMediaSubTypeParts(vendor, StructuredContent(content, format)))
    
  def text(content: String) =
    AlmMediaType("text", AlmMediaSubTypeParts(NoVendor, RawContent(content)), false, TextualMedia(None), Seq.empty, false)
    
  def applicationStructured(vendor: MediaTypeVendorPart, content: String, format: String) =
    format match {
      case "json" ⇒ applicationTextual(vendor, content, format)
      case "xml" ⇒ applicationTextual(vendor, content, format)
      case "html" ⇒ applicationTextual(vendor, content, format)
      case "msgpack" ⇒ applicationBinary(vendor, content, format)
      case _ ⇒ throw new Exception(s""""$format" is an unknown media format.""")
    }
}
