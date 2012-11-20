package almhirt.eventlog.anorm

import org.specs2.mutable._
import scalaz.syntax.validation._
import akka.util.Duration
import almhirt.common._
import almhirt.almvalidation.kit._
import almhirt.environment._
import almhirt.eventlog._
import test._

class SerializingAnormEventLogSpecs extends Specification with TestAlmhirtKit {
  "A anorm SerializingAnormEventLogFactory" should {
    "create an eventlog with an SerializingAnormEventLogActor when configured" in {
      inTestAlmhirt(implicit almhirt => {
        true
      })
    }
  }

  private def withEventLog[T](f: (DomainEventLog, Almhirt) => T) =
    inTestAlmhirt(almhirt => f(almhirt.environment.eventLog, almhirt))

  "A anorm SerializingAnormEventLog which is empty" should {
    "return 0L as the next required version" in {
      withEventLog { (eventLog, almhirt) =>
        val next = eventLog.getRequiredNextEventVersion(almhirt.getUuid).awaitResult(Duration(1, "s")).forceResult
        next === 0L
      }
    }
  }

}