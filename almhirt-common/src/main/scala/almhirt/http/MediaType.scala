package almhirt.http

import scala.annotation.tailrec
import scalaz._, Scalaz._
import java.util.concurrent.atomic.AtomicReference

sealed trait MediaTypeVendorPart {
  def value: Option[String] =
    this match {
      case NoVendor => None
      case UnspecifiedVendor => Some("vnd")
      case SpecificVendor(vendor) => Some(s"vnd.$vendor")
    }
}

case object NoVendor extends MediaTypeVendorPart
final case class SpecificVendor(vendor: String) extends MediaTypeVendorPart
case object UnspecifiedVendor extends MediaTypeVendorPart

sealed trait MediaContentPart {
  def content: String
  final def value: String = this match {
    case RawContent(content) => content
    case StructuredContent(content, format) => s"$content+$format"
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
}

object AlmMediaSubTypeParts {
  def apply(content: String): AlmMediaSubTypeParts = AlmMediaSubTypeParts(NoVendor, RawContent(content))
  def apply(content: String, format: String): AlmMediaSubTypeParts = AlmMediaSubTypeParts(NoVendor, StructuredContent(content, format))
  def apply(vendor: String, content: String, format: String): AlmMediaSubTypeParts = AlmMediaSubTypeParts(SpecificVendor(vendor), StructuredContent(content, format))
}

sealed trait MediaTypeRepresentation
case object BinaryMedia extends MediaTypeRepresentation
case class TextualMedia(defaultEncoding: Option[String]) extends MediaTypeRepresentation

trait AlmMediaType {
  def mainType: String
  def subTypeParts: AlmMediaSubTypeParts
  def compressible: Boolean
  def streamRepresentation: MediaTypeRepresentation
  def fileExtensions: Seq[String]
  def ianaRegistered: Boolean
  final def subTypeValue: String = subTypeParts.value
  final def value: String = s"$mainType/$subTypeValue"
  final def binary = streamRepresentation == BinaryMedia
  final def contentValue = subTypeParts.contentValue
}

object AlmMediaType {
  def apply(theMainType: String, theSubTypeParts: AlmMediaSubTypeParts, isCompressible: Boolean, theStreamRepresentation: MediaTypeRepresentation, theFileExtensions: Seq[String], isIanaRegistered: Boolean): AlmMediaType =
    new AlmMediaType {
      val mainType = theMainType
      val subTypeParts = theSubTypeParts
      val compressible = isCompressible
      val streamRepresentation = theStreamRepresentation
      val fileExtensions = theFileExtensions
      val ianaRegistered = isIanaRegistered
    }

  implicit class AlmMediaTypeOps(self: AlmMediaType) {
    def makeIanaRegistered = AlmMediaType(self.mainType, self.subTypeParts, self.compressible, self.streamRepresentation, self.fileExtensions, true)
    def makeCompressible = AlmMediaType(self.mainType, self.subTypeParts, true, self.streamRepresentation, self.fileExtensions, self.ianaRegistered)
    def addFileExtensions(fileExtensions: String*) = AlmMediaType(self.mainType, self.subTypeParts, self.compressible, self.streamRepresentation, self.fileExtensions ++ fileExtensions, self.ianaRegistered)
    def makeTextualWithEncoding(encoding: String) = AlmMediaType(self.mainType, self.subTypeParts, self.compressible, TextualMedia(None), self.fileExtensions, self.ianaRegistered)
    def makeTextual(encoding: String) = AlmMediaType(self.mainType, self.subTypeParts, self.compressible, TextualMedia(Some(encoding)), self.fileExtensions, self.ianaRegistered)
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

  def applicationTextual(vendor: String, content: String, format: String) =
    AlmMediaType("application", AlmMediaSubTypeParts(vendor, content, format), false, TextualMedia(None), Seq.empty, false)

  def applicationBinary(vendor: String, content: String, format: String) =
    AlmMediaType("application", AlmMediaSubTypeParts(vendor, content, format), false, BinaryMedia, Seq.empty, false)

  def applicationAuto(vendor: String, content: String, format: String) =
    format match {
      case "json" => AlmMediaType("application", AlmMediaSubTypeParts(vendor, content, format), false, TextualMedia(None), Seq.empty, false)
      case "xml" => AlmMediaType("application", AlmMediaSubTypeParts(vendor, content, format), false, TextualMedia(None), Seq.empty, false)
      case "html" => AlmMediaType("application", AlmMediaSubTypeParts(vendor, content, format), false, TextualMedia(None), Seq.empty, false)
      case "msgpack" => AlmMediaType("application", AlmMediaSubTypeParts(vendor, content, format), false, BinaryMedia, Seq.empty, false)
      case _ => AlmMediaType("application", AlmMediaSubTypeParts(vendor, content, format), false, BinaryMedia, Seq.empty, false)
    }
}

trait AlmMediaTypesRegistry {
  private val byMainAndSubTypes = new AtomicReference(Map.empty[String, Map[String, AlmMediaType]])
  private val byMainTypeAndContent = new AtomicReference(Map.empty[String, Map[String, List[AlmMediaType]]])

  def register(mediaType: AlmMediaType) {
    @tailrec def registerByMainAndSubTypeType(): Unit = {
      val current = byMainAndSubTypes.get()
      val updated = current.get(mediaType.mainType) match {
        case Some(bySubType) => current.updated(mediaType.mainType, bySubType.updated(mediaType.subTypeValue, mediaType))
        case None => current.updated(mediaType.mainType, Map(mediaType.subTypeValue -> mediaType))
      }
      if (!byMainAndSubTypes.compareAndSet(current, updated)) registerByMainAndSubTypeType()
    }

    @tailrec def registerByMainTypeAndContent(): Unit = {
      val current = byMainTypeAndContent.get()
      val updated = current.get(mediaType.mainType) match {
        case Some(byContentValue) =>
          byContentValue.get(mediaType.contentValue) match {
            case Some(mediaTypes) =>
              current.updated(mediaType.mainType, byContentValue.updated(mediaType.contentValue, (mediaType :: mediaTypes)))
            case None =>
              current.updated(mediaType.mainType, byContentValue.updated(mediaType.contentValue, List(mediaType)))
          }
        case None =>
          current.updated(mediaType.mainType, Map(mediaType.contentValue -> List(mediaType)))
      }
      if (!byMainTypeAndContent.compareAndSet(current, updated)) registerByMainTypeAndContent()
    }
    registerByMainAndSubTypeType()
    registerByMainTypeAndContent()
  }
}

object AlmMediaTypes extends AlmMediaTypesRegistry {

}