package almhirt.domain

import test._
import almhirt._
import org.scalatest._
import org.scalatest.matchers.ShouldMatchers

class AggregateRootSpecsWithTestPerson extends FlatSpec with ShouldMatchers {
  val shouldBe = TestPerson("Jim") flatMap {_.changeName("Fritz")} flatMap {_.addressAquired("Roma")} flatMap {_.move("New York")}
  val events = shouldBe.events
  
  "A Person created by methods on the aggregate root" should 
    "be the same when created from the history of" in {
      val rebuilt = TestPerson.rebuildFromHistory(events)
      rebuilt should equal (shouldBe.result)
  }
}