package test

import java.util.UUID
import almhirt.commanding._

trait TestPersonCommand extends DomainCommand
trait TestPersonMutatorCommand extends TestPersonCommand with MutatorCommandStyle
trait TestPersonCreatorCommand extends TestPersonCommand with CreatorCommandStyle

case class ChangeTestPersonName(aggRootId: UUID, aggRootVersion: Long, ticket: Option[String], newName: String) extends TestPersonMutatorCommand
case class SetTestPersonAddress(aggRootId: UUID, aggRootVersion: Long, ticket: Option[String], aquiredAddress: String) extends TestPersonMutatorCommand
case class MoveTestPerson(aggRootId: UUID, aggRootVersion: Long, ticket: Option[String], newAddress: String) extends TestPersonMutatorCommand

trait TestPersonMutatorUnitOfWork[TCom <: DomainCommand] extends MutatorUnitOfWork[TestPerson, TestPersonEvent, TCom]{
  val repositoryType = classOf[TestPersonRepository]
}

object ChangeTestPersonNameUnitOfWork extends TestPersonMutatorUnitOfWork[ChangeTestPersonName] {
  val commandType = classOf[ChangeTestPersonName]
  val handler = (cmd: ChangeTestPersonName, person: TestPerson) => person.changeName(cmd.newName).recordings
}