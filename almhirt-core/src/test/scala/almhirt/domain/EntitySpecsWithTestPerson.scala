package almhirt.domain

import scalaz._
import scalaz.NonEmptyList
import Scalaz._
import org.specs2.mutable._
import scalaz.NonEmptyList
import almhirt.validation.{Problem}


class EntitySpecsWithTestPerson extends Specification {
  val shouldBe = TestPerson("Jim") flatMap {_.changeName("Fritz")} flatMap {_.addressAquired("Roma")} flatMap {_.move("New York")}
  val events = shouldBe.events
  
  "A Person created by methods on the entity" should {
    "be the same when created from the history of" in {
      val rebuilt = TestPerson.rebuildFromHistory(NonEmptyList(events.head, events.tail: _*))
      rebuilt must beEqualTo(shouldBe.result)
    }
  }
}