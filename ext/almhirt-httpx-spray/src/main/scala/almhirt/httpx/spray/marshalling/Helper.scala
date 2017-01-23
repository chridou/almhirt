package almhirt.httpx.spray.marshalling

import scala.util.control.NonFatal
import scalaz._, Scalaz._
import almhirt.common._
import almhirt.http._
import almhirt.almvalidation.kit._
import spray.http.{ MediaType, MediaTypes }

object Helper {
  def extractChannel(mediaTypeValue: String): String = {
    val subtype = mediaTypeValue.split('/')(1)
    val potChannel = subtype.split('+') match {
      case Array(x) ⇒ x
      case Array(_, y) ⇒ y
      case _ ⇒ throw new Exception(s"""Invalid media type: "$mediaTypeValue"""")
    }
    if (potChannel.startsWith("x-"))
      potChannel.drop(2)
    else
      potChannel
  }

  def extractChannel(mediaType: MediaType): String = extractChannel(mediaType.value)
  def extractChannel(mediaType: AlmMediaType): String = extractChannel(mediaType.value)

  private val validMediaTypeChannels = Set("html", "json", "xml", "msgpack")
  def isValidMediaType(mediaType: MediaType): Boolean =
    try {
      validMediaTypeChannels.contains(extractChannel(mediaType))
    } catch {
      case NonFatal(exn) ⇒ false
    }

  def validateMediaTypes(mediaTypes: Seq[MediaType]): AlmValidation[Seq[MediaType]] = {
    val res =
      mediaTypes.map(mediaType ⇒
        if (isValidMediaType(mediaType))
          mediaType.success
        else
          UnspecifiedProblem(s"""Not a valid media type: "${mediaType.value}".""").failure).map(_.toAgg).toList
    res.sequence[AlmValidationAP, MediaType]
  }
}