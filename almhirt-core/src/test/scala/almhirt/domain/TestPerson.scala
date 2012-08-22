package almhirt.domain

import java.util.UUID
import org.joda.time.DateTime
import scalaz.{Validation, Failure, Success}
import scalaz.Validation._
import almhirt.validation.Problem._

trait TestPersonEvent extends EntityEvent
case class TestPersonCreated(entityId: UUID, name: String, timestamp: DateTime = DateTime.now) extends TestPersonEvent with CreatingNewEntityEvent
case class TestPersonNameChanged(entityId: UUID, entityVersion: Long, newName: String, timestamp: DateTime = DateTime.now) extends TestPersonEvent
case class TestPersonAddressAquired(entityId: UUID, entityVersion: Long, aquiredAddress: String, timestamp: DateTime = DateTime.now) extends TestPersonEvent
case class TestPersonMoved(entityId: UUID, entityVersion: Long, newAddress: String, timestamp: DateTime = DateTime.now) extends TestPersonEvent
case class TestPersonUnhandledEvent(entityId: UUID, entityVersion: Long, timestamp: DateTime = DateTime.now) extends TestPersonEvent


case class TestPerson(id: UUID, version: Long, name: String, address: Option[String], balance: Int) extends Entity[TestPerson, TestPersonEvent]{
  def handlers = { 
  	case TestPersonNameChanged(_,_, newName,_) => copy(name = newName, version = this.version+1)
  	case TestPersonAddressAquired(_,_, aquiredAddress,_) => copy(address = Some(aquiredAddress), version = this.version+1)
  	case TestPersonMoved(_,_, newAddress,_) => copy(address = Some(newAddress), version = this.version+1)
  }
  
  def changeName(newName: String): UpdateRecorder[TestPersonEvent, TestPerson] = {
  	if(newName.isEmpty) 
  	  reject("Name must not be empty")
  	else 
  	  update(TestPersonNameChanged(id, version, newName))
  }
  
  def move(newAdress: String): UpdateRecorder[TestPersonEvent, TestPerson] = {
  	if(newAdress.isEmpty) 
  	  reject("NewAdress must not be empty")
  	else if (address.isEmpty)
  	  reject("You cannot relocate someone who doesn't have an address")
  	else 
  	  update(TestPersonMoved(id, version, newAdress))
  }
  
  def addressAquired(aquiredAddress: String): UpdateRecorder[TestPersonEvent, TestPerson] = {
  	if(aquiredAddress.isEmpty) 
  	  reject("Aquired adresss must not be empty")
  	else if (address.isDefined)
  	  reject("Adress is alredy known")
  	else 
  	  update(TestPersonAddressAquired(id, version, aquiredAddress), handlers)
  }
  
  def unhandableAction(): UpdateRecorder[TestPersonEvent, TestPerson] = {
  	update(TestPersonUnhandledEvent(id, version), handlers)
  }

}

object TestPerson extends CanCreateFromEntityEvents[TestPerson, TestPersonEvent] {
  def creationHandler = {
  	case e @ TestPersonCreated(entityId, name,_) => TestPerson(entityId, e.entityVersion, name, None, 0)
  }
  
  def apply(name: String) = create(TestPersonCreated(UUID.randomUUID, name))
}