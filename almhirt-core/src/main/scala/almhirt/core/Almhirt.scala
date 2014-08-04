package almhirt.core

import akka.actor._
import almhirt.common._
import com.typesafe.config._

trait Almhirt extends CanCreateUuidsAndDateTimes {
  def config: Config
}

object Almhirt {
  def apply(system: ActorSystem): Almhirt =
    ???
}