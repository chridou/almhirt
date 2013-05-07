package almhirt.http

import scalaz.syntax.validation._
import almhirt.common._

trait ChannelExtractor {
  final def apply(from: HttpContentType): AlmValidation[String] = extractChannel(from)
  def extractChannel(from: HttpContentType): AlmValidation[String]
}

object ChannelExtractor {
  val alwaysFails = new ChannelExtractor {
    def extractChannel(from: HttpContentType): AlmValidation[String] = UnspecifiedProblem("I always fail!").failure
  }
}

sealed trait ChannelClassifier { def transportType: Class[_] }
case object TextChannel extends ChannelClassifier { val transportType = classOf[String] }
case object BinaryChannel extends ChannelClassifier { val transportType = classOf[Array[Byte]] }

trait ClassifiesChannels {
  final def apply(that: String): AlmValidation[ChannelClassifier] = classify(that)
  def classify(that: String): AlmValidation[ChannelClassifier]
}

object ClassifiesChannels {
  def apply(): ClassifiesChannels =
    new ClassifiesChannels {
      override def classify(that: String): AlmValidation[ChannelClassifier] =
        that match {
          case "json" => TextChannel.success
          case "xml" => TextChannel.success
          case x => BadDataProblem(s""""$x" is not a known HTTP channel""").failure
        }
    }
}