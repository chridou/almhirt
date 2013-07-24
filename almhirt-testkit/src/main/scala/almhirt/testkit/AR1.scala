package almhirt.testkit

import scalaz._, Scalaz._
import almhirt.common._
import almhirt.almvalidation.kit._
import almhirt.commanding._
import almhirt.domain._

trait AR1Event extends DomainEvent

case class AR1Created(header: DomainEventHeader, newA: String) extends AR1Event with CreatesNewAggregateRootEvent {
  override def changeMetadata(newMetaData: Map[String, String]): AR1Created = copy(header = this.header.changeMetadata(newMetaData))
}

case class AR1AChanged(header: DomainEventHeader, newA: String) extends AR1Event {
  override def changeMetadata(newMetaData: Map[String, String]): AR1AChanged = copy(header = this.header.changeMetadata(newMetaData))
}

case class AR1BChanged(header: DomainEventHeader, newB: Option[String]) extends AR1Event {
  override def changeMetadata(newMetaData: Map[String, String]): AR1BChanged = copy(header = this.header.changeMetadata(newMetaData))
}

case class AR1Deleted(header: DomainEventHeader) extends AR1Event with CreatesNewAggregateRootEvent {
  override def changeMetadata(newMetaData: Map[String, String]): AR1Deleted = copy(header = this.header.changeMetadata(newMetaData))
}

case class AR1UnhandableEvent(header: DomainEventHeader) extends AR1Event {
  override def changeMetadata(newMetaData: Map[String, String]): AR1UnhandableEvent = copy(header = this.header.changeMetadata(newMetaData))
}

case class AR1(ref: AggregateRootRef, theA: String, theB: Option[String], isDeleted: Boolean)
  extends AggregateRoot[AR1, AR1Event]
  with AggregateRootWithHandlers[AR1, AR1Event]
  with AggregateRootMutationHelpers[AR1, AR1Event] {

  protected override def handlers = {
    case AR1AChanged(_, newA) => set((ar: AR1, v: String) => ar.copy(theA = v), newA)
    case AR1BChanged(_, newB) => setL(AR1Lenses.theBL, newB)
    case AR1Deleted(_) => markDeleted((ar: AR1, b: Boolean) => ar.copy(isDeleted = b))
  }

  def changeA(newA: String)(implicit ccuad: CanCreateUuidsAndDateTimes): UpdateRecorder[AR1, AR1Event] =
    newA.notEmptyAlm.fold(
      fail => reject(fail),
      succ => update(AR1AChanged(ref, succ)))

  def changeB(newB: Option[String])(implicit ccuad: CanCreateUuidsAndDateTimes): UpdateRecorder[AR1, AR1Event] =
    newB.map(_.notEmptyAlm).validationOut.fold(
      fail => reject(fail),
      succ => update(AR1BChanged(ref, succ)))

  def delete()(implicit ccuad: CanCreateUuidsAndDateTimes): UpdateRecorder[AR1, AR1Event] =
    update(AR1Deleted(ref))

  protected override def updateRef(newRef: AggregateRootRef): AR1 = this.copy(ref = newRef)
}

object AR1 extends CanCreateAggragateRoot[AR1, AR1Event] {
  protected override def creationHandler: PartialFunction[AR1Event, AR1] = {
    case AR1Created(header, newA) =>
      AR1(header.aggRef.inc, newA, None, false)
  }

  def fromScratch(id: java.util.UUID, a: String)(implicit ccuad: CanCreateUuidsAndDateTimes): UpdateRecorder[AR1, AR1Event] =
    create(AR1Created(DomainEventHeader(id, 0L), a))

  object Commanding {
    import almhirt.core.Almhirt
    import scala.concurrent.ExecutionContext
    def addCommands(registry: CommandHandlerRegistry)(implicit theAlmhirt: Almhirt): CommandHandlerRegistry = {
      val executionContext = theAlmhirt.futuresExecutor

      val createTestArAdder =
        CreatingDomainCommandHandler.createRegistryAdderFromSyncFun[AR1ComCreateAR1, AR1Event, AR1](
          command => AR1.fromScratch(command.targettedAggregateRootId, command.newA).result,
          executionContext)
      val changeAAdder =
        MutatingDomainCommandHandler.createRegistryAdderFromSyncFun[AR1ComChangeA, AR1Event, AR1](
          (ar, command) => ar.changeA(command.newA).result,
          executionContext)
      val changeBAdder =
        MutatingDomainCommandHandler.createRegistryAdderFromSyncFun[AR1ComChangeB, AR1Event, AR1](
          (ar, command) => ar.changeB(command.newB).result,
          executionContext)
      val deleteTestArAdder =
        MutatingDomainCommandHandler.createRegistryAdderFromSyncFun[AR1ComDeleteAR1, AR1Event, AR1](
          (ar, command) => ar.delete.result,
          executionContext)

      registry nextAdder createTestArAdder nextAdder changeAAdder nextAdder changeBAdder nextAdder deleteTestArAdder
    }
  }
}

object AR1Lenses {
  val theBL: AR1 @> Option[String] = Lens.lensu((a, b) => a.copy(theB = b), _.theB)
}

trait AR1Command extends DomainCommand
final case class AR1ComCreateAR1(header: DomainCommandHeader, newA: String) extends AR1Command with CreatingDomainCommand {
  override def changeMetadata(newMetadata: Map[String, String]): AR1ComCreateAR1 = copy(header = this.header.changeMetadata(newMetadata))
}

final case class AR1ComChangeA(header: DomainCommandHeader, newA: String) extends AR1Command {
  override def changeMetadata(newMetadata: Map[String, String]): AR1ComChangeA = copy(header = this.header.changeMetadata(newMetadata))
}

final case class AR1ComChangeB(header: DomainCommandHeader, newB: Option[String]) extends AR1Command {
  override def changeMetadata(newMetadata: Map[String, String]): AR1ComChangeB = copy(header = this.header.changeMetadata(newMetadata))
}

final case class AR1ComDeleteAR1(header: DomainCommandHeader) extends AR1Command {
  override def changeMetadata(newMetadata: Map[String, String]): AR1ComDeleteAR1 = copy(header = this.header.changeMetadata(newMetadata))
}

final case class AR1ComUnregisteredCommand(header: DomainCommandHeader) extends AR1Command {
  override def changeMetadata(newMetadata: Map[String, String]): AR1ComUnregisteredCommand = copy(header = this.header.changeMetadata(newMetadata))
}
