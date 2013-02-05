package almhirt.core.test

import java.util.UUID
import almhirt.common._
import almhirt.commanding._
import almhirt.environment._
import almhirt.core.Almhirt
import almhirt.core.CanCreateUuid
import scala.reflect.ClassTag

case class NewTestPersonAction(id: UUID, name: String) extends TestPersonContext.BoundCreatorAction
case class ChangeTestPersonNameAction(newName: String) extends TestPersonContext.BoundMutatorAction
case class SetTestPersonAddressAction(aquiredAddress: String) extends TestPersonContext.BoundMutatorAction
case class MoveTestPersonAction(newAddress: String) extends TestPersonContext.BoundMutatorAction
case class MoveBecauseOfMarriageAction(newName: String, newAddress: String) extends TestPersonContext.BoundMutatorAction

object TestPersonContext extends BoundDomainActionsCommandContext[TestPerson, TestPersonEvent] {
  val tagAR = implicitly[ClassTag[TestPerson]]
  val tagEvent = implicitly[ClassTag[TestPersonEvent]]

  val newTestPersonActionHandler: this.CreatingActionHandler[NewTestPersonAction] = (act: NewTestPersonAction, passedAlmhirt: Almhirt) => TestPerson(act.id, act.name)
  val changeTestPersonNameActionHandler: this.MutatingActionHandler[ChangeTestPersonNameAction] = (act: ChangeTestPersonNameAction, person: TestPerson, passedAlmhirt: Almhirt) => person.changeName(act.newName)
  val setTestPersonAdressActionHandler: this.MutatingActionHandler[SetTestPersonAddressAction] = (act: SetTestPersonAddressAction, person: TestPerson, passedAlmhirt: Almhirt) => person.addressAquired(act.aquiredAddress)
  val moveActionHandler: this.MutatingActionHandler[MoveTestPersonAction] = (act: MoveTestPersonAction, person: TestPerson, passedAlmhirt: Almhirt) => person.move(act.newAddress)
  val moveBecauseOfMarriageActionHandler: this.MutatingActionHandler[MoveBecauseOfMarriageAction] = (act: MoveBecauseOfMarriageAction, person: TestPerson, passedAlmhirt: Almhirt) => person.changeName(act.newName).flatMap(_.move(act.newAddress))

  override val actionHandlers = this.createHasActionHandlers(
    List(
      classOf[NewTestPersonAction] -> this.flattenCreatingActionHandler(newTestPersonActionHandler)),
    List(
      classOf[ChangeTestPersonNameAction] -> this.flattenMutatingActionHandler(changeTestPersonNameActionHandler),
      classOf[SetTestPersonAddressAction] -> this.flattenMutatingActionHandler(setTestPersonAdressActionHandler),
      classOf[MoveTestPersonAction] -> this.flattenMutatingActionHandler(moveActionHandler),
      classOf[MoveBecauseOfMarriageAction] -> this.flattenMutatingActionHandler(moveBecauseOfMarriageActionHandler)))

}

case class TestPersonCommand(id: UUID, aggRef: Option[AggregateRootRef], actions: List[TestPersonContext.BoundCommandAction]) extends TestPersonContext.BoundDomainActionsCommand

object TestPersonCommand extends TestPersonContext.BoundDomainCommandFactory[TestPersonCommand] {
  def apply(aggRef: Option[AggregateRootRef], actions: List[TestPersonContext.BoundCommandAction])(implicit resources: CanCreateUuid): TestPersonCommand =
    TestPersonCommand(resources.getUuid, aggRef, actions)
}

