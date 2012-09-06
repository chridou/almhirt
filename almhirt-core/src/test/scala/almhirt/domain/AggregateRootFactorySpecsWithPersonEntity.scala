package almhirt.domain

import java.util.UUID
import scalaz._, Scalaz._
import org.specs2.mutable._
import almhirt.validation._
import almhirt.validation.syntax._

class AggregateRootFactorySpecsWithPersonEntity extends Specification {
  "An aggregate root factory for Persons" should {
  	"return a new Person(in an accpted Update) when given a TestPersonCreated event to 'create'" in {
  	  val event = TestPersonCreated(UUID.randomUUID(), "Peter")
  	  val entityValidation = TestPerson.create(event)
  	  val entity = entityValidation.result.forceResult
  	  entity.id must beEqualTo(event.aggRootId) and (entity.name must beEqualTo(event.name))
  	}
  	"return a new Person when given a TestPersonCreated event to 'applyEvent'" in {
  	  val event = TestPersonCreated(UUID.randomUUID(), "Peter")
  	  val entityValidation = TestPerson.applyEvent(event)
  	  val entity = entityValidation.forceResult
  	  entity.id must beEqualTo(event.aggRootId) and (entity.name must beEqualTo(event.name))
  	}
  	"return a new Person when given a TestPersonCreated event to 'buildFromHistory'" in {
  	  val event = TestPersonCreated(UUID.randomUUID(), "Peter")
  	  val entityValidation = TestPerson.rebuildFromHistory(NonEmptyList(event))
  	  val entity = entityValidation.forceResult
  	  entity.id must beEqualTo(event.aggRootId) and (entity.name must beEqualTo(event.name))
  	}
  	"return a Failure(in an Update) when given a TestPersonNameChanged event to 'create'" in {
  	  val event = TestPersonNameChanged(UUID.randomUUID(), 1L, "Peter")
  	  val entityValidation = TestPerson.create(event).result
  	  entityValidation.isFailure
  	}
  	"return a Failure when given a TestPersonNameChanged event to 'applyEvent'" in {
  	  val event = TestPersonNameChanged(UUID.randomUUID(), 1L, "Peter")
  	  val entityValidation = TestPerson.applyEvent(event)
  	  entityValidation.isFailure
  	}
  	"return a Failure when given a TestPersonNameChanged event to 'buildFromHistory'" in {
  	  val event = TestPersonNameChanged(UUID.randomUUID(), 1L, "Peter")
  	  val entityValidation = TestPerson.rebuildFromHistory(NonEmptyList(event))
  	  entityValidation.isFailure
  	}
  }
}