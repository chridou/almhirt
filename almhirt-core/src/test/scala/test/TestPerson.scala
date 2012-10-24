package test

import almhirt.domain.impl.BasicAggregateRootRepository
import almhirt.domain.AggregateRoot
import almhirt.domain.CanCreateAggragateRoot
import almhirt.domain.CreatingNewAggregateRootEvent
import almhirt.domain.DomainEvent
import almhirt.domain.UpdateRecorder
import almhirt.environment.AlmhirtContext
import almhirt.eventsourcing.DomainEventLog
import java.util.UUID
import org.joda.time.DateTime


case class TestPerson(id: UUID, version: Long, name: String, address: Option[String], balance: Int) extends AggregateRoot[TestPerson, TestPersonEvent]{
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

object TestPerson extends CanCreateAggragateRoot[TestPerson, TestPersonEvent] {
  def creationHandler = {
  	case e @ TestPersonCreated(entityId, name,_) => TestPerson(entityId, e.aggRootVersion, name, None, 0)
  }
  
  def apply(name: String) = create(TestPersonCreated(UUID.randomUUID, name))
}
