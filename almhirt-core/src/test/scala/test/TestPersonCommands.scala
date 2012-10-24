package test

import java.util.UUID
import almhirt.commanding._

trait TestPersonCommand extends DomainCommand
trait TestPersonMutatorCommand extends TestPersonCommand with MutatorCommandStyle
trait TestPersonCreatorCommand extends TestPersonCommand with CreatorCommandStyle

case class ChangeTestPersonName(id: UUID, version: Long, ticket: Option[String], newName: String) extends TestPersonMutatorCommand
case class SetTestPersonAddress(id: UUID, version: Long, ticket: Option[String], aquiredAddress: String) extends TestPersonMutatorCommand
case class MoveTestPerson(id: UUID, version: Long, ticket: Option[String], newAddress: String) extends TestPersonMutatorCommand

trait TestPersonMutatorUnitOfWork[TCom <: TestPersonCommand] extends MutatorUnitOfWork[TestPerson, TestPersonEvent, TCom]{
  val repositoryType = classOf[TestPersonRepository]
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