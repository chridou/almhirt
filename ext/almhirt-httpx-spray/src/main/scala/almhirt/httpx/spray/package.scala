package almhirt.httpx

import scala.language.implicitConversions
import scalaz._, Scalaz._
import almhirt.http.AlmMediaType
import _root_.spray.http.{ MediaType, ContentType }
import _root_.spray.http.{ HttpCharset, HttpCharsets }
import almhirt.http.AlmCharacterEncoding
import almhirt.http._

package object spray {
  implicit def almMediaType2SprayMediaType(amt: AlmMediaType): MediaType = {
    MediaType.custom(
      mainType = amt.mainType,
      subType = amt.subTypeValue,
      compressible = amt.compressible,
      binary = amt.binary,
      fileExtensions = amt.fileExtensions)
  }

  implicit def almMediaTypes2SprayMediaTypes(amts: Seq[AlmMediaType]): Seq[MediaType] = {
    amts.map(almMediaType2SprayMediaType(_))
  }

  implicit def almEncoding2SprayEncoding(ac: AlmCharacterEncoding): HttpCharset = {
    ac match {
      case AlmCharacterEncodings.`UTF-8` => HttpCharsets.`UTF-8`
    }
  }

  implicit def almMediaType2SprayContentType(amt: AlmMediaType)(implicit defaultEncoding: AlmCharacterEncoding): ContentType = {
    val mt = almMediaType2SprayMediaType(amt)
    amt.streamRepresentation match {
      case BinaryMedia => ContentType(mt, None)
      case TextualMedia(preferredMt) =>
        val enc = preferredMt.map(almEncoding2SprayEncoding(_)) | defaultEncoding
        ContentType(mt, Some(enc))
    }
  }

  implicit def almMediaTypes2SprayContentTypes(amts: Seq[AlmMediaType])(implicit defaultEncoding: AlmCharacterEncoding): Seq[ContentType] = {
    amts.map(almMediaType2SprayContentType(_))
  }
  
  implicit class AlmMediaTypeOps(self: AlmMediaType) {
    def toSprayMediaType: MediaType = almMediaType2SprayMediaType(self)
    def toSprayCpntentType(implicit defaultEncoding: AlmCharacterEncoding): ContentType = almMediaType2SprayContentType(self)
  }

  implicit class AlmMediaTypesOps(self: Seq[AlmMediaType]) {
    def toSprayMediaTypes: Seq[MediaType] = almMediaTypes2SprayMediaTypes(self)
    def toSprayContentTypes(implicit defaultEncoding: AlmCharacterEncoding): Seq[ContentType] = almMediaTypes2SprayContentTypes(self)
  }

}