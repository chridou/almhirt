package almhirt.http

import scalaz.syntax.validation._
import almhirt.common._

sealed trait HttpPayload { def data: Any }
final case class BinaryPayload(data: Array[Byte]) extends HttpPayload
final case class TextPayload(data: String) extends HttpPayload


trait PayloadExtractor[T] {
  final def apply(from: T, classifier: ChannelClassifier): AlmValidation[HttpPayload] = getPayload(from, classifier)
  def getPayload(from: T, classifier: ChannelClassifier): AlmValidation[HttpPayload]
}

object PayloadExtractor{
  def alwaysFails[T]: PayloadExtractor[T] = new PayloadExtractor[T]{
    def getPayload(from: T, classifier: ChannelClassifier): AlmValidation[HttpPayload] = UnspecifiedProblem("I always fail!").failure
  }
}