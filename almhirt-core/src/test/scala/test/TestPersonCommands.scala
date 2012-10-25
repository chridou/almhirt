package test

import java.util.UUID
import almhirt.commanding._

trait TestPersonCommand extends DomainCommand
trait TestPersonCreatorCommand extends TestPersonCommand with CreatorCommandStyle
trait TestPersonMutatorCommand extends TestPersonCommand with MutatorCommandStyle

case class NewTestPerson(name: String) extends TestPersonCreatorCommand
case class ChangeTestPersonName(id: UUID, version: Option[Long], newName: String) extends TestPersonMutatorCommand
case class SetTestPersonAddress(id: UUID, version: Option[Long], aquiredAddress: String) extends TestPersonMutatorCommand
case class MoveTestPerson(id: UUID, version: Option[Long], newAddress: String) extends TestPersonMutatorCommand
case class MoveBecauseOfMarriage(id: UUID, version: Option[Long], newName: String, newAddress: String) extends TestPersonMutatorCommand

trait TestPersonUnitOfWork[TCom <: TestPersonCommand] extends UnitOfWork[TestPerson, TestPersonEvent]{
  val repositoryType = classOf[TestPersonRepository]
}

trait TestPersonCreatorUnitOfWork[TCom <: TestPersonCommand] extends TestPersonUnitOfWork[TCom] with CreatorUnitOfWorkStyle[TestPerson, TestPersonEvent, TCom]
trait TestPersonMutatorUnitOfWork[TCom <: TestPersonCommand] extends TestPersonUnitOfWork[TCom] with MutatorUnitOfWorkStyle[TestPerson, TestPersonEvent, TCom]

object NewTestPersonUnitOfWork extends TestPersonCreatorUnitOfWork[NewTestPerson] {
  val commandType = classOf[NewTestPerson]
  val handler = (cmd: NewTestPerson) => TestPerson(cmd.name).recordings
}

object ChangeTestPersonNameUnitOfWork extends TestPersonMutatorUnitOfWork[ChangeTestPersonName] {
  val commandType = classOf[ChangeTestPersonName]
  val handler = (cmd: ChangeTestPersonName, person: TestPerson) => person.changeName(cmd.newName).recordings
}

object SetTestPersonAdressUnitOfWork extends TestPersonMutatorUnitOfWork[SetTestPersonAddress] {
  val commandType = classOf[SetTestPersonAddress]
  val handler = (cmd: SetTestPersonAddress, person: TestPerson) => person.move(cmd.aquiredAddress).recordings
}

object MoveTestPersonNameUnitOfWork extends TestPersonMutatorUnitOfWork[MoveTestPerson] {
  val commandType = classOf[MoveTestPerson]
  val handler = (cmd: MoveTestPerson, person: TestPerson) => person.move(cmd.newAddress).recordings
}

object MoveBecauseOfMarriageUnitOfWork extends TestPersonMutatorUnitOfWork[MoveBecauseOfMarriage] {
  val commandType = classOf[MoveBecauseOfMarriage]
  val handler = (cmd: MoveBecauseOfMarriage, person: TestPerson) => 
    person.changeName(cmd.newName).flatMap(_.move(cmd.newAddress)).recordings
}