package almhirt.messaging

import scalaz._
import Scalaz._
import almhirt.validation._

object MessagingUtils {
  def patternMatchesTopic(pattern: String, topic: String): Boolean = true
  def validateTopic(topic: String): AlmValidationSBD[Unit] = ().success
  def validatePattern(pattern: String): AlmValidationSBD[Unit] = ().success
}