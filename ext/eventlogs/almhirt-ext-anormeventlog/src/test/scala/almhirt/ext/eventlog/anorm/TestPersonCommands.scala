package almhirt.ext.eventlog.anorm

import java.util.UUID
import almhirt.common._
import almhirt.core.Almhirt
import almhirt.commanding._
import almhirt.environment._
import almhirt.domain.AggregateRootRepository

trait TestPersonCommand extends BoundDomainCommand
trait TestPersonCreatorCommand extends TestPersonCommand with CreatorCommandStyle
trait TestPersonMutatorCommand extends TestPersonCommand with MutatorCommandStyle

case class NewTestPerson(id: UUID, name: String) extends TestPersonCreatorCommand
case class ChangeTestPersonName(target: AggregateRootRef, newName: String) extends TestPersonMutatorCommand
case class SetTestPersonAddress(target: AggregateRootRef, aquiredAddress: String) extends TestPersonMutatorCommand
case class MoveTestPerson(target: AggregateRootRef, newAddress: String) extends TestPersonMutatorCommand
case class MoveBecauseOfMarriage(target: AggregateRootRef, newName: String, newAddress: String) extends TestPersonMutatorCommand

abstract class TestPersonUnitOfWork[TCom <: TestPersonCommand](implicit almhirt: Almhirt) extends BoundUnitOfWork[TestPerson, TestPersonEvent](almhirt: Almhirt)

abstract class TestPersonCreatorUnitOfWork[TCom <: TestPersonCommand](implicit almhirt: Almhirt) extends TestPersonUnitOfWork[TCom] with CreatorUnitOfWorkStyleFuture[TestPerson, TestPersonEvent, TCom]
abstract class TestPersonMutatorUnitOfWork[TCom <: TestPersonCommand](implicit almhirt: Almhirt) extends TestPersonUnitOfWork[TCom] with MutatorUnitOfWorkStyleFuture[TestPerson, TestPersonEvent, TCom]

object TestPersonHandlerFactory {
  def newTestPersonUnitOfWork(implicit theAlmhirt: Almhirt) = BoundUnitOfWork.createCreatorStyleFuture[TestPerson, TestPersonEvent, NewTestPerson]((cmd: NewTestPerson, passedAlmhirt: Almhirt) => AlmFuture { TestPerson(cmd.id, cmd.name).recordings }(passedAlmhirt))
  def changeTestPersonNameUnitOfWork(implicit theAlmhirt: Almhirt) = BoundUnitOfWork.createMutatorStyleFuture[TestPerson, TestPersonEvent, ChangeTestPersonName]((cmd: ChangeTestPersonName, person: TestPerson, passedAlmhirt: Almhirt) => AlmFuture { person.changeName(cmd.newName).recordings }(passedAlmhirt))
  def setTestPersonAdressUnitOfWork(implicit theAlmhirt: Almhirt) = BoundUnitOfWork.createMutatorStyleFuture[TestPerson, TestPersonEvent, SetTestPersonAddress]((cmd: SetTestPersonAddress, person: TestPerson, passedAlmhirt: Almhirt) => AlmFuture { person.addressAquired(cmd.aquiredAddress).recordings }(passedAlmhirt))
  def moveTestPersonNameUnitOfWork(implicit theAlmhirt: Almhirt) = BoundUnitOfWork.createMutatorStyleFuture[TestPerson, TestPersonEvent, MoveTestPerson]((cmd: MoveTestPerson, person: TestPerson, passedAlmhirt: Almhirt) => AlmFuture { person.move(cmd.newAddress).recordings }(theAlmhirt))
  def moveBecauseOfMarriageUnitOfWork(implicit theAlmhirt: Almhirt) = BoundUnitOfWork.createMutatorStyleFuture[TestPerson, TestPersonEvent, MoveBecauseOfMarriage]((cmd: MoveBecauseOfMarriage, person: TestPerson, passedAlmhirt: Almhirt) => AlmFuture { person.changeName(cmd.newName).flatMap(_.move(cmd.newAddress)).recordings }(passedAlmhirt))
}