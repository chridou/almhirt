package almhirt.commanding.impl

import akka.actor._
import almhirt.core.Almhirt
import almhirt.commanding._
import almhirt.components._

class DomainCommandsSequencerImpl(val theAlmhirt: Almhirt) extends DomainCommandsSequencerTemplate with Actor with ActorLogging {

  def receive: Receive = receiveDomainCommandsSequencerMessage
}