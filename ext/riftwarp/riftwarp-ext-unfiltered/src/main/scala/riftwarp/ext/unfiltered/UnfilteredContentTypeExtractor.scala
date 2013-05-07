package riftwarp.ext.unfiltered

import scalaz.syntax.validation._
import almhirt.common._
import unfiltered.request._

trait UnfilteredContentTypeExtractor extends almhirt.http.HttpContentTypeExtractor[HttpRequest[Any]] {
  def extractContentType(from: HttpRequest[Any]): AlmValidation[almhirt.http.HttpContentType] =
    RequestContentType(from) match {
      case Some(from) => almhirt.http.HttpContentType.fromString(from)
      case None => BadDataProblem("No content type!").failure
    }
}