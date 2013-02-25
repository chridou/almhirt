package almhirt.core.test

import almhirt.common._
import almhirt.syntax.problem._
import almhirt.domain._
import almhirt.domain.impl._
import almhirt.environment._
import almhirt.eventlog._
import java.util.UUID

case class TestPerson(ref: AggregateRootRef, name: String, address: Option[String], balance: Int) extends AggregateRootWithHandlers[TestPerson, TestPersonEvent] with AddsUpdateToAggregateRoot[TestPerson, TestPersonEvent]{
  def handlers = {
    case TestPersonNameChanged( _, _, newName, _) => copy(name = newName, ref = this.ref.inc)
    case TestPersonAddressAquired( _, _, aquiredAddress, _) => copy(address = Some(aquiredAddress), ref = this.ref.inc)
    case TestPersonMoved( _, _, newAddress, _) => copy(address = Some(newAddress), ref = this.ref.inc)
  }

  def changeName(newName: String): UpdateRecorder[TestPerson, TestPersonEvent] = {
    if (newName.isEmpty)
      reject("Name must not be empty")
    else
      update(TestPersonNameChanged(UUID.randomUUID(), ref, newName))
  }

  def move(newAdress: String): UpdateRecorder[TestPerson, TestPersonEvent] = {
    if (newAdress.isEmpty)
      reject("NewAdress must not be empty")
    else if (address.isEmpty)
      reject("You cannot relocate someone who doesn't have an address")
    else
      update(TestPersonMoved(UUID.randomUUID(), ref, newAdress))
  }

  def addressAquired(aquiredAddress: String): UpdateRecorder[TestPerson, TestPersonEvent] = {
    if (aquiredAddress.isEmpty)
      reject("Aquired addresss must not be empty")
    else if (address.isDefined)
      reject("Address is already known")
    else
      update(TestPersonAddressAquired(UUID.randomUUID(), ref, aquiredAddress), handlers)
  }

  def unhandableAction(): UpdateRecorder[TestPerson, TestPersonEvent] = {
    update(TestPersonUnhandledEvent(UUID.randomUUID(), ref), handlers)
  }

}

object TestPerson extends CanCreateAggragateRoot[TestPerson, TestPersonEvent] {
  def creationHandler = {
    case e @ TestPersonCreated(_, newRef, name, _) => TestPerson(newRef.inc, name, None, 0)
  }

  def apply(name: String) =
    if (name.isEmpty)
      UpdateRecorder.reject(BusinessRuleViolatedProblem("Name must not be empty", severity = Minor).withIdentifier(name))
    else
      create(TestPersonCreated(UUID.randomUUID(), AggregateRootRef(UUID.randomUUID), name))

  def apply(id: UUID, name: String) =
    if (name.isEmpty)
      UpdateRecorder.reject(BusinessRuleViolatedProblem("Name must not be empty", severity = Minor).withIdentifier(name))
    else
      create(TestPersonCreated(UUID.randomUUID(), AggregateRootRef(id), name))
}
