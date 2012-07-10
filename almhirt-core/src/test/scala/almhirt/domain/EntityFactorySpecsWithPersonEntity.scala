package almhirt.domain

import java.util.UUID
import scalaz._
import Scalaz._
import org.specs2.mutable._
import almhirt.validation.{Problem}

class EntityFactorySpecsWithPersonEntity extends Specification {
  "An entityfactory for Persons" should {
  	"return a new Person(in an accpted Update) when given a TestPersonCreated event to 'create'" in {
  	  val event = TestPersonCreated(UUID.randomUUID(), "Peter")
  	  val entityValidation = TestPerson.create(event)
  	  val entity = entityValidation.result.either.right.get
  	  entity.id must beEqualTo(event.entityId) and (entity.name must beEqualTo(event.name))
  	}
  	"return a new Person when given a TestPersonCreated event to 'applyEvent'" in {
  	  val event = TestPersonCreated(UUID.randomUUID(), "Peter")
  	  val entityValidation = TestPerson.applyEvent(event)
  	  val entity = entityValidation.either.right.get
  	  entity.id must beEqualTo(event.entityId) and (entity.name must beEqualTo(event.name))
  	}
  	"return a new Person when given a TestPersonCreated event to 'buildFromHistory'" in {
  	  val event = TestPersonCreated(UUID.randomUUID(), "Peter")
  	  val entityValidation = TestPerson.rebuildFromHistory(NonEmptyList(event))
  	  val entity = entityValidation.either.right.get
  	  entity.id must beEqualTo(event.entityId) and (entity.name must beEqualTo(event.name))
  	}
  	"return a Failure(in an Update) when given a TestPersonNameChanged event to 'create'" in {
  	  val event = TestPersonNameChanged(UUID.randomUUID(), 1L, "Peter")
  	  val entityValidation = TestPerson.create(event).result
  	  entityValidation match {
  	  	case Success(_) => false
  	  	case Failure(_) => true
  	  }
  	}
  	"return a Failure when given a TestPersonNameChanged event to 'applyEvent'" in {
  	  val event = TestPersonNameChanged(UUID.randomUUID(), 1L, "Peter")
  	  val entityValidation = TestPerson.applyEvent(event)
  	  entityValidation match {
  	  	case Success(_) => false
  	  	case Failure(_) => true
  	  }
  	}
  	"return a Failure when given a TestPersonNameChanged event to 'buildFromHistory'" in {
  	  val event = TestPersonNameChanged(UUID.randomUUID(), 1L, "Peter")
  	  val entityValidation = TestPerson.rebuildFromHistory(NonEmptyList(event))
  	  entityValidation match {
  	  	case Success(_) => false
  	  	case Failure(_) => true
  	  }
  	}
  }
}