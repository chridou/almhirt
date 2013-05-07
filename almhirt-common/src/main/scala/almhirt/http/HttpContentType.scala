package almhirt.http

import scalaz._, Scalaz._
import almhirt.common._
import almhirt.almvalidation.kit._

trait HttpContentTypeExtractor[T] {
  final def apply(from: T): AlmValidation[HttpContentType] = extractContentType(from)
  def extractContentType(from: T): AlmValidation[HttpContentType]
}

object HttpContentTypeExtractor {
  def alwaysFails[T]() = new HttpContentTypeExtractor[T] {
    def extractContentType(from: T): AlmValidation[HttpContentType] = UnspecifiedProblem("I always fail!").failure
  }
}

final case class HttpContentType(primary: String, options: Map[String, String]) {
  def toContentTypeString: String =
    (primary :: (options.map(x => s"${x._1}=${x._2}")).toList).mkString(";")
}

object HttpContentType {
  def fromString(str: String): AlmValidation[HttpContentType] =
    str.split(';').toSeq match {
      case Seq(primary, rest @ _*) =>
        rest.map(x =>
          (x.split('=') match {
            case Array(a) => (a, a).success
            case Array(a, b) => (a, b).success
            case x => ParsingProblem("Contains an invalid key value pair!").withInput(str).failure
          }).toAgg).toVector.sequence.map(options =>
          HttpContentType(primary, options.toMap))
      case x =>
        ParsingProblem("Not a valid content type!").withInput(str).failure
    }
}



