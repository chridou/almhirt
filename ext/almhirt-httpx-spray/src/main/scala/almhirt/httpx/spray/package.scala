package almhirt.httpx

import scala.language.implicitConversions
import almhirt.http.AlmMediaType
import _root_.spray.http.MediaType

package object spray {
  implicit def almMediaType2SprayMediaType(amt: AlmMediaType): MediaType = {
    MediaType.custom(amt.mainType, amt.subTypeValue, amt.compressible, amt.binary, amt.fileExtensions)
  }
  
  implicit def almMediaTypes2SprayMediaTypes(amts: Seq[AlmMediaType]): Seq[MediaType] = {
    amts.map(almMediaType2SprayMediaType(_))
  }
  
  implicit class AlmMediaTypeOps(self: AlmMediaType) {
    def toSprayMediaType: MediaType = almMediaType2SprayMediaType(self)
  }

  implicit class AlmMediaTypesOps(self: Seq[AlmMediaType]) {
    def toSprayMediaTypes: Seq[MediaType] = almMediaTypes2SprayMediaTypes(self)
  }
  
}