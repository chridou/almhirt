package almhirt.domain

import org.specs2.mutable._
import scalaz.NonEmptyList
import akka.util.Duration
import almhirt._
import almhirt.almvalidation.kit._
import almhirt.eventsourcing.DomainEventLog
import almhirt.domain.impl.BasicAggregateRootRepository
import almhirt.almakka.AlmAkkaContextTestKit
import almhirt.eventsourcing.impl._

class BasicAggregateRootRepositorySpecs extends Specification with AlmAkkaContextTestKit {
  val shouldBe1 = TestPerson("Jim") flatMap {_.changeName("Fritz")} flatMap {_.addressAquired("Roma")} flatMap {_.move("New York")}
  val events1 = shouldBe1.events
  val person1 = shouldBe1.result.forceResult

  val shouldBe2 = TestPerson("Tom") flatMap {_.changeName("Moritz")} flatMap {_.addressAquired("Berlin")}
  val events2 = shouldBe2.events
  val person2 = shouldBe2.result.forceResult
  
  val shouldBe3 = TestPerson("Mike") flatMap {_.changeName("Michael")}
  val events3 = shouldBe3.events
  val person3 = shouldBe2.result.forceResult
  
  private def withNewRepository[T](withRepo: PersonRepository => T): T = {
    inOwnContext(ctx => {
      val repo = new PersonRepository(new InefficientSerialziedInMemoryDomainEventLog()(ctx))
      withRepo(repo)
    })
  }
  
  "A PersonRepository" should {
    "be able to store a person" in {
      withNewRepository(repo => {
        repo.store(person1, events1).result(Duration.Inf).isSuccess
      })
    }
    "return the same person as stored when get was called" in {
      withNewRepository(repo => {
        repo.store(person1, events1).result(Duration.Inf)
        val res = repo.get(person1.id).result(Duration.Inf)
        res.forceResult === person1
      })
    }
    "return a NotFoundProblem when it is queried with a wrong id" in {
      withNewRepository(repo => {
        repo.store(person1, events1).result(Duration.Inf)
        val res = repo.get(person2.id).result(Duration.Inf)
        classOf[NotFoundProblem].isAssignableFrom(res.forceProblem.getClass)
      })
    }
    "return the correct persons when queried by id" in {
      withNewRepository(repo => {
        repo.store(person1, events1).result(Duration.Inf)
        repo.store(person2, events2).result(Duration.Inf)
        repo.store(person3, events3).result(Duration.Inf)
        val p1 = repo.get(person1.id).result(Duration.Inf).forceResult
        val p2 = repo.get(person2.id).result(Duration.Inf).forceResult
        val p3 = repo.get(person3.id).result(Duration.Inf).forceResult
        p1 === person1 && p2 === person2 && p3 === person3
      })
    }
  }
}