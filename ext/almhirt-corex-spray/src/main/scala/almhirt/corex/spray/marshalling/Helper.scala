package almhirt.corex.spray.marshalling

import scalaz._, Scalaz._
import almhirt.common._
import almhirt.almvalidation.kit._
import spray.http.MediaType

private[marshalling] object Helper {
  def extractChannel(mediaType: MediaType): String =
    mediaType.value.split('+')(1)

  def validateMediaTypes(mediaTypes: Seq[MediaType]): AlmValidation[Seq[MediaType]] = {
    val res =
      mediaTypes.map(mediaType =>
        mediaType.value.mustFulfill(
          _.split('+').size == 2,
          x => s"""Not a valid command media type: $x""").toAgg.map(_ => mediaType)).toList
    res.sequence[AlmValidationAP, MediaType]
  }
}