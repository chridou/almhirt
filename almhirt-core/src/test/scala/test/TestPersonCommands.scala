package test

import java.util.UUID
import almhirt._
import almhirt.commanding._
import almhirt.environment.AlmhirtContext
import akka.dispatch.ExecutionContext

trait TestPersonCommand extends DomainCommand
trait TestPersonCreatorCommand extends TestPersonCommand with CreatorCommandStyle
trait TestPersonMutatorCommand extends TestPersonCommand with MutatorCommandStyle

case class NewTestPerson(id: UUID, name: String) extends TestPersonCreatorCommand
case class ChangeTestPersonName(id: UUID, version: Option[Long], newName: String) extends TestPersonMutatorCommand
case class SetTestPersonAddress(id: UUID, version: Option[Long], aquiredAddress: String) extends TestPersonMutatorCommand
case class MoveTestPerson(id: UUID, version: Option[Long], newAddress: String) extends TestPersonMutatorCommand
case class MoveBecauseOfMarriage(id: UUID, version: Option[Long], newName: String, newAddress: String) extends TestPersonMutatorCommand

trait TestPersonUnitOfWork[TCom <: TestPersonCommand] extends UnitOfWork[TestPerson, TestPersonEvent] {
  val repositoryType = classOf[TestPersonRepository]
}

trait TestPersonCreatorUnitOfWork[TCom <: TestPersonCommand] extends TestPersonUnitOfWork[TCom] with CreatorUnitOfWorkStyleFuture[TestPerson, TestPersonEvent, TCom]
trait TestPersonMutatorUnitOfWork[TCom <: TestPersonCommand] extends TestPersonUnitOfWork[TCom] with MutatorUnitOfWorkStyleFuture[TestPerson, TestPersonEvent, TCom]

class NewTestPersonUnitOfWork(implicit ctx: AlmhirtContext) extends TestPersonCreatorUnitOfWork[NewTestPerson] {
  private implicit val executionContext = ctx.system.futureDispatcher
  val commandType = classOf[NewTestPerson]
  val handler = (cmd: NewTestPerson, ctx: ExecutionContext) => AlmFuture { TestPerson(cmd.id, cmd.name).recordings }
}

class ChangeTestPersonNameUnitOfWork(implicit ctx: AlmhirtContext) extends TestPersonMutatorUnitOfWork[ChangeTestPersonName] {
  private implicit val executionContext = ctx.system.futureDispatcher
  val commandType = classOf[ChangeTestPersonName]
  val handler = (cmd: ChangeTestPersonName, person: TestPerson, ctx: ExecutionContext) => AlmFuture { person.changeName(cmd.newName).recordings }
}

class SetTestPersonAdressUnitOfWork(implicit ctx: AlmhirtContext) extends TestPersonMutatorUnitOfWork[SetTestPersonAddress] {
  private implicit val executionContext = ctx.system.futureDispatcher
  val commandType = classOf[SetTestPersonAddress]
  val handler = (cmd: SetTestPersonAddress, person: TestPerson, ctx: ExecutionContext) => AlmFuture { person.addressAquired(cmd.aquiredAddress).recordings }
}

class MoveTestPersonNameUnitOfWork(implicit ctx: AlmhirtContext) extends TestPersonMutatorUnitOfWork[MoveTestPerson] {
  private implicit val executionContext = ctx.system.futureDispatcher
  val commandType = classOf[MoveTestPerson]
  val handler = (cmd: MoveTestPerson, person: TestPerson, ctx: ExecutionContext) => AlmFuture { person.move(cmd.newAddress).recordings }
}

class MoveBecauseOfMarriageUnitOfWork(implicit ctx: AlmhirtContext) extends TestPersonMutatorUnitOfWork[MoveBecauseOfMarriage] {
  private implicit val executionContext = ctx.system.futureDispatcher
  val commandType = classOf[MoveBecauseOfMarriage]
  val handler = (cmd: MoveBecauseOfMarriage, person: TestPerson, ctx: ExecutionContext) =>
    AlmFuture { person.changeName(cmd.newName).flatMap(_.move(cmd.newAddress)).recordings }
}