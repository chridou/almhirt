package almhirt.domain

import java.util.UUID
import scalaz._, Scalaz._
import org.specs2.mutable._
import almhirt.syntax.almvalidation._
import test._

class AggregateRootFactorySpecsWithPersonEntity extends Specification {
  "An aggregate root factory for Persons" should {
  	"return a new Person(in an accpted Update) when given a TestPersonCreated event to 'create'" in {
  	  val event = TestPersonCreated(UUID.randomUUID(), UUID.randomUUID(), "Peter")
  	  val entityValidation = TestPerson.create(event)
  	  val entity = entityValidation.result.forceResult
  	  entity.id must beEqualTo(event.aggId) and (entity.name must beEqualTo(event.name))
  	}
  	"return a new Person when given a TestPersonCreated event to 'applyEvent'" in {
  	  val event = TestPersonCreated(UUID.randomUUID(), UUID.randomUUID(), "Peter")
  	  val entityValidation = TestPerson.applyEvent(event)
  	  val entity = entityValidation.forceResult
  	  entity.id must beEqualTo(event.aggId) and (entity.name must beEqualTo(event.name))
  	}
  	"return a new Person when given a TestPersonCreated event to 'buildFromHistory'" in {
  	  val event = TestPersonCreated(UUID.randomUUID(), UUID.randomUUID(), "Peter")
  	  val entityValidation = TestPerson.rebuildFromHistory(List(event))
  	  val entity = entityValidation.forceResult
  	  entity.id must beEqualTo(event.aggId) and (entity.name must beEqualTo(event.name))
  	}
  	"return a Failure(in an Update) when given a TestPersonNameChanged event to 'create'" in {
  	  val event = TestPersonNameChanged(UUID.randomUUID(), UUID.randomUUID(), 1L, "Peter")
  	  val entityValidation = TestPerson.create(event).result
  	  entityValidation.isFailure
  	}
  	"return a Failure when given a TestPersonNameChanged event to 'applyEvent'" in {
  	  val event = TestPersonNameChanged(UUID.randomUUID(), UUID.randomUUID(), 1L, "Peter")
  	  val entityValidation = TestPerson.applyEvent(event)
  	  entityValidation.isFailure
  	}
  	"return a Failure when given a TestPersonNameChanged event to 'buildFromHistory'" in {
  	  val event = TestPersonNameChanged(UUID.randomUUID(), UUID.randomUUID(), 1L, "Peter")
  	  val entityValidation = TestPerson.rebuildFromHistory(List(event))
  	  entityValidation.isFailure
  	}
  }
}