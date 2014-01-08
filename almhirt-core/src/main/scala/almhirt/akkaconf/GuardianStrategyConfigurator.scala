package almhirt.akkaconf

import akka.actor._
import akka.actor.SupervisorStrategy._
import almhirt.components.CriticalAggregateRootCellSourceException

class GuardianStrategyConfigurator extends SupervisorStrategyConfigurator {
  def create(): SupervisorStrategy = {
    OneForOneStrategy() {
      case _: CriticalAggregateRootCellSourceException => Escalate
      case _ => Restart
    }
  }
}