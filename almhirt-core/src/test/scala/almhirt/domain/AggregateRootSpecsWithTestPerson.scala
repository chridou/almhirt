package almhirt.domain

import org.scalatest._
import org.scalatest.matchers.ShouldMatchers
import almhirt.core.test._
import almhirt.common._
import almhirt.core.test.TestPerson

class AggregateRootSpecsWithTestPerson extends FlatSpec with ShouldMatchers {
  implicit object ccuad extends CanCreateUuidsAndDateTimes  
  val shouldBe = TestPerson("Jim") flatMap {_.changeName("Fritz")} flatMap {_.addressAquired("Roma")} flatMap {_.move("New York")}
  val events = shouldBe.events
  
  "A Person created by methods on the aggregate root" should 
    "be the same when created from the history of" in {
      val rebuilt = TestPerson.rebuildFromHistory(events)
      rebuilt should equal (shouldBe.ar)
  }
}