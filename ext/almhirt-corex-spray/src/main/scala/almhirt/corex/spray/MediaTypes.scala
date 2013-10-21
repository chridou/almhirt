package almhirt.corex.spray

import spray.http.MediaTypes
import spray.http.MediaType

object AlmhirtMediaTypes {
  val `application/x-msgpack` = MediaTypes.register(MediaType.custom(
    mainType = "application",
    subType = "x-msgpack",
    compressible = true,
    binary = true,
    fileExtensions = Seq("msgpack")))

}