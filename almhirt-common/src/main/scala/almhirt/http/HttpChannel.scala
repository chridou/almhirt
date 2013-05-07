package almhirt.http

import almhirt.common.AlmValidation

trait ChannelExtractor {
  final def apply(from: HttpContentType): AlmValidation[String] = extractChannel(from)
  def extractChannel(from: HttpContentType): AlmValidation[String]
}

sealed trait ChannelClassifier
case object TextChannel extends ChannelClassifier
case object BinaryChannel extends ChannelClassifier

trait ClassifiesChannels {
  final def apply(that: String): AlmValidation[ChannelClassifier] = classify(that)
  def classify(that: String): AlmValidation[ChannelClassifier]
}