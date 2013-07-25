package almhirt.testkit

import scalaz._
import scalaz.Scalaz._
import almhirt.common._
import almhirt.almvalidation.kit._
import almhirt.domain._
import almhirt.commanding._
import almhirt.commanding.CommandHandlerRegistry.CommandHandlerRegistryOps
import almhirt.core.Almhirt
import almhirt.domain.DomainEventHeader.aggregateRootRef2DomainEventHeader

trait AR2Event extends DomainEvent

case class AR2Created(header: DomainEventHeader) extends AR2Event with CreatesNewAggregateRootEvent {
  override def changeMetadata(newMetaData: Map[String, String]): AR2Created = copy(header = this.header.changeMetadata(newMetaData))
}

case class AR2CChanged(header: DomainEventHeader, newC: Option[Int]) extends AR2Event {
  override def changeMetadata(newMetaData: Map[String, String]): AR2CChanged = copy(header = this.header.changeMetadata(newMetaData))
}

case class AR2Deleted(header: DomainEventHeader) extends AR2Event with CreatesNewAggregateRootEvent {
  override def changeMetadata(newMetaData: Map[String, String]): AR2Deleted = copy(header = this.header.changeMetadata(newMetaData))
}

case class AR2UnhandableEvent(header: DomainEventHeader) extends AR2Event {
  override def changeMetadata(newMetaData: Map[String, String]): AR2UnhandableEvent = copy(header = this.header.changeMetadata(newMetaData))
}

case class AR2(ref: AggregateRootRef, theC: Option[Int], isDeleted: Boolean)
  extends AggregateRoot[AR2, AR2Event]
  with AggregateRootWithHandlers[AR2, AR2Event]
  with AggregateRootMutationHelpers[AR2, AR2Event] {

  protected override def handlers = {
    case AR2CChanged(_, newC) => set((ar: AR2, v: Option[Int]) => ar.copy(theC = v), newC)
    case AR2Deleted(_) => markDeleted((ar: AR2, b: Boolean) => ar.copy(isDeleted = b))
  }

  def setC(newC: Int)(implicit ccuad: CanCreateUuidsAndDateTimes): UpdateRecorder[AR2, AR2Event] =
    update(AR2CChanged(ref, Some(newC)))

  def unsetC()(implicit ccuad: CanCreateUuidsAndDateTimes): UpdateRecorder[AR2, AR2Event] =
    update(AR2CChanged(ref, None))

  def delete()(implicit ccuad: CanCreateUuidsAndDateTimes): UpdateRecorder[AR2, AR2Event] =
    update(AR2Deleted(ref))

  protected override def updateRef(newRef: AggregateRootRef): AR2 = this.copy(ref = newRef)
}

object AR2 extends CanCreateAggragateRoot[AR2, AR2Event] {
  protected override def creationHandler: PartialFunction[AR2Event, AR2] = {
    case AR2Created(header) =>
      AR2(header.aggRef.inc, None, false)
  }

  def fromScratch(id: java.util.UUID)(implicit ccuad: CanCreateUuidsAndDateTimes): UpdateRecorder[AR2, AR2Event] =
    create(AR2Created(DomainEventHeader(id, 0L)))

  object Commanding {
    import almhirt.core.Almhirt
    import scala.concurrent.ExecutionContext
    def addCommands(registry: CommandHandlerRegistry)(implicit theAlmhirt: Almhirt): CommandHandlerRegistry = {
      val executionContext = theAlmhirt.futuresExecutor
      val createAdder =
        CreatingDomainCommandHandler.createRegistryAdderFromSyncFun[AR2ComCreateAR2, AR2Event, AR2](
          command => AR2.fromScratch(command.targettedAggregateRootId).result,
          executionContext)
      val setCAdder =
        MutatingDomainCommandHandler.createRegistryAdderFromSyncFun[AR2ComSetC, AR2Event, AR2](
          (ar, command) => ar.setC(command.newC).result,
          executionContext)
      val unssetCAdder =
        MutatingDomainCommandHandler.createRegistryAdderFromSyncFun[AR2ComUnsetC, AR2Event, AR2](
          (ar, command) => ar.unsetC.result,
          executionContext)
      val deleteAdder =
        MutatingDomainCommandHandler.createRegistryAdderFromSyncFun[AR2ComDeleteAR2, AR2Event, AR2](
          (ar, command) => ar.delete.result,
          executionContext)

      registry nextAdder createAdder nextAdder setCAdder nextAdder unssetCAdder nextAdder deleteAdder
    }
  }
}

object AR2Lenses {
  val theCL: AR2 @> Option[Int] = Lens.lensu((a, b) => a.copy(theC = b), _.theC)
}

trait AR2Command extends DomainCommand

final case class AR2ComCreateAR2(header: DomainCommandHeader, newA: String) extends AR2Command with CreatingDomainCommand {
  override def changeMetadata(newMetadata: Map[String, String]): AR2ComCreateAR2 = copy(header = this.header.changeMetadata(newMetadata))
}

final case class AR2ComSetC(header: DomainCommandHeader, newC: Int) extends AR2Command {
  override def changeMetadata(newMetadata: Map[String, String]): AR2ComSetC = copy(header = this.header.changeMetadata(newMetadata))
}

final case class AR2ComUnsetC(header: DomainCommandHeader) extends AR2Command {
  override def changeMetadata(newMetadata: Map[String, String]): AR2ComUnsetC = copy(header = this.header.changeMetadata(newMetadata))
}

final case class AR2ComDeleteAR2(header: DomainCommandHeader) extends AR2Command {
  override def changeMetadata(newMetadata: Map[String, String]): AR2ComDeleteAR2 = copy(header = this.header.changeMetadata(newMetadata))
}


