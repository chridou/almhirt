package almhirt.httpx

import scala.language.implicitConversions
import scalaz._, Scalaz._
import scalaz.Validation.FlatMap._
import almhirt.common._
import almhirt.http._
import almhirt.configuration._
import com.typesafe.config.Config
import akka.http.scaladsl.model._
import akka.http.scaladsl.model.MediaType._

package object akkahttp {
  implicit def almMediaType2AkkaHttpMediaType(amt: AlmMediaType): MediaType = {
    MediaType.custom(
      value = s"${amt.mainType}/${amt.subTypeValue}",
      comp = if (amt.compressible) Compressible else NotCompressible,
      binary = amt.binary,
      fileExtensions = amt.fileExtensions.toList)
  }

  implicit def almMediaTypes2AkkaHttpMediaTypes(amts: Seq[AlmMediaType]): Seq[MediaType] = {
    amts.map(almMediaType2AkkaHttpMediaType(_))
  }

  implicit def almEncoding2AkkaHttpEncoding(ac: AlmCharacterEncoding): HttpCharset = {
    ac match {
      case AlmCharacterEncodings.`UTF-8` ⇒ HttpCharsets.`UTF-8`
    }
  }

  implicit def almMediaType2AkkaHttpContentType(amt: AlmMediaType)(implicit defaultEncoding: AlmCharacterEncoding): ContentType = {
    val mt = almMediaType2AkkaHttpMediaType(amt)
    amt.streamRepresentation match {
      case BinaryMedia => MediaType.applicationBinary(amt.subType, if (amt.compressible) Compressible else NotCompressible, amt.fileExtensions.toList: _*)
      case TextualMedia(preferredMt) ⇒
        val enc = preferredMt.map(almEncoding2AkkaHttpEncoding(_)) | defaultEncoding
        ContentType(mt, () => enc)
    }
  }

  implicit def almMediaTypes2AkkaHttpContentTypes(amts: Seq[AlmMediaType])(implicit defaultEncoding: AlmCharacterEncoding): Seq[ContentType] = {
    amts.map(almMediaType2AkkaHttpContentType(_))
  }

  implicit class AlmMediaTypeOps(self: AlmMediaType) {
    def toAkkaHttpMediaType: MediaType = almMediaType2AkkaHttpMediaType(self)
    def toAkkaHttpContentType(implicit defaultEncoding: AlmCharacterEncoding): ContentType = almMediaType2AkkaHttpContentType(self)
  }

  implicit class AlmMediaTypesOps(self: Seq[AlmMediaType]) {
    def toAkkaHttpMediaTypes: Seq[MediaType] = almMediaTypes2AkkaHttpMediaTypes(self)
    def toAkkaHttpContentTypes(implicit defaultEncoding: AlmCharacterEncoding): Seq[ContentType] = almMediaTypes2AkkaHttpContentTypes(self)
  }

  implicit class AkkaHttpMediaTypeOps(self: MediaType) {
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
                    case Array("vnd", raw @ _*) ⇒
                      AlmMediaSubTypeParts(UnspecifiedVendor, RawContent(raw.mkString(".")))
                    case Array(vendor, raw @ _*) ⇒
                      AlmMediaSubTypeParts(SpecificVendor(vendor), RawContent(raw.mkString(".")))
                  }
                case Array(pre, format) ⇒
                  pre.split("\\.") match {
                    case Array() ⇒
                      AlmMediaSubTypeParts(NoVendor, StructuredContent(pre, format))
                    case Array(content) ⇒
                      AlmMediaSubTypeParts(NoVendor, StructuredContent(content, format))
                    case Array("vnd", content @ _*) ⇒
                      AlmMediaSubTypeParts(UnspecifiedVendor, StructuredContent(content.mkString("."), format))
                    case Array(vendor, content @ _*) ⇒
                      AlmMediaSubTypeParts(SpecificVendor(vendor), StructuredContent(content.mkString("."), format))
                  }
              },
              self.comp match {
                case x: Compressibility => x.compressible
              },
              if (self.binary) BinaryMedia else TextualMedia(Some(AlmCharacterEncodings.`UTF-8`)),
              self.fileExtensions,
              false)
        }
      } catch {
        case scala.util.control.NonFatal(exn) ⇒
          throw new Exception(s"""Could not transform "${self.value}" to an AlmMediaType: ${exn.getMessage()}""", exn)
      }
  }

  implicit object HttpMethodConfigExtractor extends ConfigExtractor[HttpMethod] {
    def getValue(config: Config, path: String): AlmValidation[HttpMethod] =
      for {
        methodStr ← config.v[String](path)
        method ← methodStr.toUpperCase() match {
          case "GET"    ⇒ HttpMethods.GET.success
          case "PUT"    ⇒ HttpMethods.PUT.success
          case "POST"   ⇒ HttpMethods.POST.success
          case "PATCH"  ⇒ HttpMethods.PATCH.success
          case "DELETE" ⇒ HttpMethods.DELETE.success
          case x        ⇒ ParsingProblem(s""""$x" is not a HTTP-Method.""").failure
        }
      } yield method

    def tryGetValue(config: Config, path: String): AlmValidation[Option[HttpMethod]] =
      config.opt[Config](path).flatMap {
        case Some(_) ⇒ getValue(config, path).map(Some(_))
        case None    ⇒ scalaz.Success(None)
      }
  }
}