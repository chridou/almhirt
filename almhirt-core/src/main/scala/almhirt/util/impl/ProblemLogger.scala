package almhirt.util.impl

import akka.actor.Actor
import almhirt.common._
import almhirt.almakka.AlmActorLogging

class ProblemLogger(minSeverity: Severity = Minor) extends Actor with AlmActorLogging {
  def receive: Receive = {
    case problem: Problem =>
      if (!problem.isLogged) {
        logProblem(problem, minSeverity)
      }
  }
}