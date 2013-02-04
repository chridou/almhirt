package almhirt.core.test

import java.util.UUID
import almhirt.common._
import almhirt.commanding._
import almhirt.environment._
import almhirt.core.Almhirt

object TestPersonContext extends BoundDomainCommandContext[TestPerson, TestPersonEvent] {
}

trait TestPersonCommand extends TestPersonContext.BoundDomainActionsCommand

case class NewTestPerson(id: UUID, name: String) extends TestPersonContext.CreatorAction
case class ChangeTestPersonName(newName: String) extends TestPersonContext.MutatorAction
case class SetTestPersonAddress(aquiredAddress: String) extends TestPersonContext.MutatorAction
case class MoveTestPerson(newAddress: String) extends TestPersonContext.MutatorAction
case class MoveBecauseOfMarriage(newName: String, newAddress: String) extends TestPersonContext.MutatorAction

object TestPersonActionHandlers {
  val newTestPersonActionHandler: TestPersonContext.CreatingActionHandler[NewTestPerson] = (act: NewTestPerson, passedAlmhirt: Almhirt) => AlmFuture.successful { TestPerson(act.id, act.name) }
  val changeTestPersonNameActionHandler: TestPersonContext.MutatingActionHandler[ChangeTestPersonName] = (act: ChangeTestPersonName, person: TestPerson, passedAlmhirt: Almhirt) => AlmFuture.successful { person.changeName(act.newName) }
  val setTestPersonAdressActionHandler: TestPersonContext.MutatingActionHandler[SetTestPersonAddress] = (act: SetTestPersonAddress, person: TestPerson, passedAlmhirt: Almhirt) => AlmFuture.successful { person.addressAquired(act.aquiredAddress) }
  val moveActionHandler: TestPersonContext.MutatingActionHandler[MoveTestPerson] = (act: MoveTestPerson, person: TestPerson, passedAlmhirt: Almhirt) => AlmFuture.successful { person.move(act.newAddress) }
  val moveBecauseOfMarriageActionHandler: TestPersonContext.MutatingActionHandler[MoveBecauseOfMarriage] = (act: MoveBecauseOfMarriage, person: TestPerson, passedAlmhirt: Almhirt) => AlmFuture.successful { person.changeName(act.newName).flatMap(_.move(act.newAddress)) }
}
//}
//
//class SetTestPersonAdressUnitOfWork(implicit almhirt: Almhirt) extends TestPersonMutatorUnitOfWork[SetTestPersonAddress] {
//  private implicit val executionContext = almhirt.executionContext
//  val commandType = classOf[SetTestPersonAddress]
//  val handler = (cmd: SetTestPersonAddress, person: TestPerson) => AlmFuture { person.addressAquired(cmd.aquiredAddress).recordings }
//}
//
//class MoveTestPersonNameUnitOfWork(implicit almhirt: Almhirt) extends TestPersonMutatorUnitOfWork[MoveTestPerson] {
//  private implicit val executionContext = almhirt.executionContext
//  val commandType = classOf[MoveTestPerson]
//  val handler = (cmd: MoveTestPerson, person: TestPerson) => AlmFuture { person.move(cmd.newAddress).recordings }
//}
//
//class MoveBecauseOfMarriageUnitOfWork(implicit almhirt: Almhirt) extends TestPersonMutatorUnitOfWork[MoveBecauseOfMarriage] {
//  private implicit val executionContext = almhirt.executionContext
//  val commandType = classOf[MoveBecauseOfMarriage]
//  val handler = (cmd: MoveBecauseOfMarriage, person: TestPerson) =>
//    AlmFuture { person.changeName(cmd.newName).flatMap(_.move(cmd.newAddress)).recordings }
//}