package almhirt.httpx

import scala.language.implicitConversions
import scalaz._, Scalaz._
import scalaz.Validation.FlatMap._
import almhirt.common._
import almhirt.http._
import almhirt.configuration._
import _root_.spray.http.{ MediaType, ContentType, HttpCharset, HttpCharsets, HttpMethod, HttpMethods }
import com.typesafe.config.Config

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
      case AlmCharacterEncodings.`UTF-8` ⇒ HttpCharsets.`UTF-8`
    }
  }

  implicit def almMediaType2SprayContentType(amt: AlmMediaType)(implicit defaultEncoding: AlmCharacterEncoding): ContentType = {
    val mt = almMediaType2SprayMediaType(amt)
    amt.streamRepresentation match {
      case BinaryMedia ⇒ ContentType(mt, None)
      case TextualMedia(preferredMt) ⇒
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

  implicit class SprayMediaTypeOps(self: MediaType) {
    def toAlmMediaType: AlmMediaType =
      try {
        AlmMediaTypes.find(self.value) match {
          case Some(am) ⇒ am
          case None ⇒
            AlmMediaType(
              self.mainType,
              self.subType.split("\\+") match {
                case Array(unstructured) ⇒
                  unstructured.split(".") match {
                    case Array(raw) ⇒
                      AlmMediaSubTypeParts(NoVendor, RawContent(raw))
                    case Array("vnd", raw) ⇒
                      AlmMediaSubTypeParts(UnspecifiedVendor, RawContent(raw))
                    case Array(vendor, raw) ⇒
                      AlmMediaSubTypeParts(SpecificVendor(vendor), RawContent(raw))
                  }
                case Array(pre, format) ⇒
                  pre.split(".") match {
                    case Array(content) ⇒
                      AlmMediaSubTypeParts(NoVendor, StructuredContent(content, format))
                    case Array("vnd", content) ⇒
                      AlmMediaSubTypeParts(UnspecifiedVendor, StructuredContent(content, format))
                    case Array(vendor, content) ⇒
                      AlmMediaSubTypeParts(SpecificVendor(vendor), StructuredContent(content, format))
                  }
              },
              self.compressible,
              if (self.binary) BinaryMedia else TextualMedia(Some(AlmCharacterEncodings.`UTF-8`)),
              self.fileExtensions,
              false)
        }
      } catch {
        case scala.util.control.NonFatal(exn) =>
          throw new Exception(s"""Could not transform "${self.value}" to an AlmMediaType.""", exn)
      }
  }

  implicit object HttpMethodConfigExtractor extends ConfigExtractor[HttpMethod] {
    def getValue(config: Config, path: String): AlmValidation[HttpMethod] =
      for {
        methodStr <- config.v[String](path)
        method <- methodStr.toUpperCase() match {
          case "GET" => HttpMethods.GET.success
          case "PUT" => HttpMethods.PUT.success
          case "POST" => HttpMethods.POST.success
          case "PATCH" => HttpMethods.PATCH.success
          case "DELETE" => HttpMethods.DELETE.success
          case x => ParsingProblem(s""""$x" is not a HTTP-Method.""").failure
        }
      } yield method

    def tryGetValue(config: Config, path: String): AlmValidation[Option[HttpMethod]] =
      config.opt[Config](path).flatMap {
        case Some(_) ⇒ getValue(config, path).map(Some(_))
        case None ⇒ scalaz.Success(None)
      }
  }
}