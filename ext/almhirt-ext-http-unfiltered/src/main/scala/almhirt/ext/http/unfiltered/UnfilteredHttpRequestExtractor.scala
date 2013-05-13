package almhirt.ext.http.unfiltered

import scalaz.syntax.validation._
import almhirt.common._
import almhirt.almvalidation.kit._
import unfiltered.request._

trait UnfilteredHttpRequestExtractor extends almhirt.http.HttpRequestExtractorTemplate[HttpRequest[Any]] {
  override def getRawContentType(from: HttpRequest[Any]): AlmValidation[String] =
    RequestContentType(from) match {
      case Some(ct) => ct.success
      case None => BadDataProblem("No content type!").failure
    }

  override def getAcceptsContent(from: HttpRequest[Any]): AlmValidation[List[(almhirt.http.HttpContentType, Option[Double])]] =
    Nil.success

  override def getBinaryContent(from: HttpRequest[Any]): AlmValidation[almhirt.http.BinaryBody] =
    inTryCatch { almhirt.http.BinaryBody(Body.bytes(from)) }

  override def getTextContent(from: HttpRequest[Any]): AlmValidation[almhirt.http.TextBody] =
    inTryCatch { almhirt.http.TextBody(new String(Body.bytes(from), "UTF-8")) }

}
