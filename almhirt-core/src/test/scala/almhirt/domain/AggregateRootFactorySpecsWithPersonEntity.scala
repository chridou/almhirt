package almhirt.domain

import org.scalatest._
import org.scalatest.matchers.ShouldMatchers
import java.util.UUID
import almhirt.syntax.almvalidation._
import almhirt.core.test._
import almhirt.core.test.TestPerson

class AggregateRootFactorySpecsWithPersonEntity extends FlatSpec with ShouldMatchers {
  "An aggregate root factory for Persons" should
    "return a new Person(in an accpted Update) when given a TestPersonCreated event to 'create'" in {
      val event = TestPersonCreated(UUID.randomUUID(), UUID.randomUUID(), "Peter")
      val entityValidation = TestPerson.create(event)
      val entity = entityValidation.result.forceResult
      (entity.id) should equal(event.aggId)
      (entity.name should equal(event.name))
    }
  it should "return a new Person when given a TestPersonCreated event to 'applyEvent'" in {
    val event = TestPersonCreated(UUID.randomUUID(), UUID.randomUUID(), "Peter")
    val entityValidation = TestPerson.applyEvent(event)
    val entity = entityValidation.forceResult
    entity.id should equal(event.aggId)
    (entity.name should equal(event.name))
  }
  it should "return a new Person when given a TestPersonCreated event to 'buildFromHistory'" in {
    val event = TestPersonCreated(UUID.randomUUID(), UUID.randomUUID(), "Peter")
    val entityValidation = TestPerson.rebuildFromHistory(List(event))
    val entity = entityValidation.forceResult
    entity.id should equal(event.aggId)
    (entity.name should equal(event.name))
  }
  it should "return a Failure(in an Update) when given a TestPersonNameChanged event to 'create'" in {
    val event = TestPersonNameChanged(UUID.randomUUID(), UUID.randomUUID(), 1L, "Peter")
    val entityValidation = TestPerson.create(event).result
    entityValidation.isFailure should be(true)
  }
  it should "return a Failure when given a TestPersonNameChanged event to 'applyEvent'" in {
    val event = TestPersonNameChanged(UUID.randomUUID(), UUID.randomUUID(), 1L, "Peter")
    val entityValidation = TestPerson.applyEvent(event)
    entityValidation.isFailure should be(true)
  }
  it should "return a Failure when given a TestPersonNameChanged event to 'buildFromHistory'" in {
    val event = TestPersonNameChanged(UUID.randomUUID(), UUID.randomUUID(), 1L, "Peter")
    val entityValidation = TestPerson.rebuildFromHistory(List(event))
    entityValidation.isFailure should be(true)
  }
}