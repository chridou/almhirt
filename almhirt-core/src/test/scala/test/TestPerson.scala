package test

import almhirt.common._
import almhirt.syntax.problem._
import almhirt.domain._
import almhirt.domain.impl._
import almhirt.environment._
import almhirt.eventlog._
import java.util.UUID
import org.joda.time.DateTime

case class TestPerson(id: UUID, version: Long, name: String, address: Option[String], balance: Int) extends AggregateRootWithHandlers[TestPerson, TestPersonEvent] with AddsUpdateToAggregateRoot[TestPerson, TestPersonEvent]{
  def handlers = {
    case TestPersonNameChanged(_, _, _, newName, _) => copy(name = newName, version = this.version + 1)
    case TestPersonAddressAquired(_, _, _, aquiredAddress, _) => copy(address = Some(aquiredAddress), version = this.version + 1)
    case TestPersonMoved(_, _, _, newAddress, _) => copy(address = Some(newAddress), version = this.version + 1)
  }

  def changeName(newName: String): UpdateRecorder[TestPersonEvent, TestPerson] = {
    if (newName.isEmpty)
      reject("Name must not be empty")
    else
      update(TestPersonNameChanged(UUID.randomUUID(), id, version, newName))
  }

  def move(newAdress: String): UpdateRecorder[TestPersonEvent, TestPerson] = {
    if (newAdress.isEmpty)
      reject("NewAdress must not be empty")
    else if (address.isEmpty)
      reject("You cannot relocate someone who doesn't have an address")
    else
      update(TestPersonMoved(UUID.randomUUID(), id, version, newAdress))
  }

  def addressAquired(aquiredAddress: String): UpdateRecorder[TestPersonEvent, TestPerson] = {
    if (aquiredAddress.isEmpty)
      reject("Aquired addresss must not be empty")
    else if (address.isDefined)
      reject("Address is already known")
    else
      update(TestPersonAddressAquired(UUID.randomUUID(), id, version, aquiredAddress), handlers)
  }

  def unhandableAction(): UpdateRecorder[TestPersonEvent, TestPerson] = {
    update(TestPersonUnhandledEvent(UUID.randomUUID(), id, version), handlers)
  }

}

object TestPerson extends CanCreateAggragateRoot[TestPerson, TestPersonEvent] {
  def creationHandler = {
    case e @ TestPersonCreated(_, entityId, name, _) => TestPerson(entityId, 1, name, None, 0)
  }

  def apply(name: String) =
    if (name.isEmpty)
      UpdateRecorder.reject(BusinessRuleViolatedProblem("Name must not be empty", severity = Minor).withIdentifier(name))
    else
      create(TestPersonCreated(UUID.randomUUID(), UUID.randomUUID, name))

  def apply(id: UUID, name: String) =
    if (name.isEmpty)
      UpdateRecorder.reject(BusinessRuleViolatedProblem("Name must not be empty", severity = Minor).withIdentifier(name))
    else
      create(TestPersonCreated(UUID.randomUUID(), id, name))
}
