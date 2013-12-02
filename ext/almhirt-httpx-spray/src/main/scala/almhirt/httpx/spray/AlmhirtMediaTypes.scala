package almhirt.httpx.spray

import almhirt.http._
import spray.http.MediaTypes
import spray.http.MediaType

object AlmhirtMediaTypes {

  val `application/x-msgpack` = MediaTypes.register(MediaType.custom(
    mainType = "application",
    subType = "x-msgpack",
    compressible = true,
    binary = true,
    fileExtensions = Seq("msgpack")))

  def createMessagePackMediaType(forType: String)(implicit mtvp: MediaTypeVendorProvider = AlmhirtMediaTypeVendorProvider): MediaType = {
    MediaType.custom(
      mainType = "application",
      subType = s"vnd.${mtvp.vendor}.$forType+msgpack",
      compressible = true,
      binary = true,
      fileExtensions = Seq(s"${forType.toLowerCase()}.msgpack"))
  }

  def createJsonMediaType(forType: String)(implicit mtvp: MediaTypeVendorProvider = AlmhirtMediaTypeVendorProvider): MediaType = {
    MediaType.custom(s"application/vnd.${mtvp.vendor}.$forType+json")
  }

  def createXmlMediaType(forType: String)(implicit mtvp: MediaTypeVendorProvider = AlmhirtMediaTypeVendorProvider): MediaType = {
    MediaType.custom(s"application/vnd.${mtvp.vendor}.$forType+xml")
  }

  def createDefaultMediaTypes(forType: String)(implicit mtvp: MediaTypeVendorProvider = AlmhirtMediaTypeVendorProvider): Seq[MediaType] = {
    List(createJsonMediaType(forType), createXmlMediaType(forType), createMessagePackMediaType(forType))
  }

}