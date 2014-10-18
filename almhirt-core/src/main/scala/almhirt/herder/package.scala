package almhirt

import org.joda.time.LocalDateTime
import almhirt.common.Event
import almhirt.akkax.ComponentId
import almhirt.problem.{ ProblemCause, Severity }

package object herder {
  type FailuresEntry = (ProblemCause, Severity, LocalDateTime)
  type MissedEventsEntry = (Event, ProblemCause, Severity, LocalDateTime)

  implicit object MissedEventsOrdering extends scala.math.Ordering[(ComponentId, BadThingsHistory[MissedEventsEntry])] {
    def compare(a: (ComponentId, BadThingsHistory[MissedEventsEntry]), b: (ComponentId, BadThingsHistory[MissedEventsEntry])): Int =
      if (a._1.app == b._1.app) {
        if (a._2.maxSeverity == b._2.maxSeverity) {
          if (a._2.occurencesCount == b._2.occurencesCount) {
            a._1.component compare b._1.component
          } else {
            b._2.occurencesCount compare a._2.occurencesCount
          }
        } else {
          b._2.maxSeverity compare a._2.maxSeverity
        }
      } else {
        a._1.app compare b._1.app
      }
  }

}