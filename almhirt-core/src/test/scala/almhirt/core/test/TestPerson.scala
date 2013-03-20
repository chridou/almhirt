package almhirt.core.test

import java.util.UUID
import almhirt.common._
import almhirt.syntax.problem._
import almhirt.domain._
import almhirt.domain.impl._
import almhirt.environment._
import almhirt.eventlog._

case class TestPerson(ref: AggregateRootRef, name: String, address: Option[String], balance: Int) extends AggregateRootWithHandlers[TestPerson, TestPersonEvent] with AddsUpdateToAggregateRoot[TestPerson, TestPersonEvent]{
  def handlers = {
    case TestPersonNameChanged(_, newName) => copy(name = newName, ref = this.ref.inc)
    case TestPersonAddressAquired( _, aquiredAddress) => copy(address = Some(aquiredAddress), ref = this.ref.inc)
    case TestPersonMoved(_, newAddress) => copy(address = Some(newAddress), ref = this.ref.inc)
  }

  protected def updateRef(newRef: AggregateRootRef):TestPerson = this.copy(ref = newRef) 
  
  def changeName(newName: String)(implicit ccuad: CanCreateUuidsAndDateTimes): UpdateRecorder[TestPerson, TestPersonEvent] = {
    if (newName.isEmpty)
      reject("Name must not be empty")
    else
      update(TestPersonNameChanged(DomainEventHeader(ref), newName))
  }

  def move(newAdress: String)(implicit ccuad: CanCreateUuidsAndDateTimes): UpdateRecorder[TestPerson, TestPersonEvent] = {
    if (newAdress.isEmpty)
      reject("NewAdress must not be empty")
    else if (address.isEmpty)
      reject("You cannot relocate someone who doesn't have an address")
    else
      update(TestPersonMoved(DomainEventHeader(ref), newAdress))
  }

  def addressAquired(aquiredAddress: String)(implicit ccuad: CanCreateUuidsAndDateTimes): UpdateRecorder[TestPerson, TestPersonEvent] = {
    if (aquiredAddress.isEmpty)
      reject("Aquired addresss must not be empty")
    else if (address.isDefined)
      reject("Address is already known")
    else
      update(TestPersonAddressAquired(DomainEventHeader(ref), aquiredAddress), handlers)
  }

  def unhandableAction()(implicit ccuad: CanCreateUuidsAndDateTimes): UpdateRecorder[TestPerson, TestPersonEvent] = {
    update(TestPersonUnhandledEvent(DomainEventHeader(ref)), handlers)
  }

}

object TestPerson extends CanCreateAggragateRoot[TestPerson, TestPersonEvent] {
  def creationHandler = {
    case e @ TestPersonCreated(header, name) => TestPerson(header.aggRef.inc, name, None, 0)
  }

  def apply(name: String)(implicit ccuad: CanCreateUuidsAndDateTimes) =
    if (name.isEmpty)
      UpdateRecorder.reject(BusinessRuleViolatedProblem("Name must not be empty", severity = Minor).withIdentifier(name))
    else
      create(TestPersonCreated(DomainEventHeader(AggregateRootRef(ccuad.getUuid)), name))

  def apply(id: UUID, name: String)(implicit ccuad: CanCreateUuidsAndDateTimes) =
    if (name.isEmpty)
      UpdateRecorder.reject(BusinessRuleViolatedProblem("Name must not be empty", severity = Minor).withIdentifier(name))
    else
      create(TestPersonCreated(DomainEventHeader(AggregateRootRef(id)), name))
}
