package almhirt

import almhirt.almakka.AlmAkka

object Almhirt {
  private var eventChannel = 1
  private var commandChannel = 1
  private var messagingChannel = 1
  
  val actorSystem = AlmAkka.actorSystem
}