package almhirt.corex.spray.marshalling

import scalaz._, Scalaz._
import almhirt.common._
import almhirt.almvalidation.kit._
import spray.http.{ MediaType, MediaTypes }
import almhirt.corex.spray.AlmhirtMediaTypes

private[marshalling] object Helper {
  def extractChannel(mediaType: MediaType): String =
    if (mediaType == MediaTypes.`text/html`)
      "html"
    else if (mediaType == MediaTypes.`application/json`)
      "json"
    else if (mediaType == MediaTypes.`application/xml`)
      "xml"
    else if (mediaType == AlmhirtMediaTypes.`application/x-msgpack`)
      "msgpack"
    else
      mediaType.value.split('+')(1)

  def isValidMediaType(mediaType: MediaType): Boolean =
    if (mediaType == MediaTypes.`text/html`)
      true
    else if (mediaType == MediaTypes.`application/json`)
      true
    else if (mediaType == MediaTypes.`application/xml`)
      true
    else if (mediaType == AlmhirtMediaTypes.`application/x-msgpack`)
      true
    else
      mediaType.value.split('+').size == 2

  def validateMediaTypes(mediaTypes: Seq[MediaType]): AlmValidation[Seq[MediaType]] = {
    val res =
      mediaTypes.map(mediaType =>
        if (isValidMediaType(mediaType))
          mediaType.success
        else
          UnspecifiedProblem(s"""Not a valid media type: "${mediaType.value}".""").failure).map(_.toAgg).toList
    res.sequence[AlmValidationAP, MediaType]
  }
}