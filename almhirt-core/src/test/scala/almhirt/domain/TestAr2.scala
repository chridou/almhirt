package almhirt.domain

import scalaz._, Scalaz._
import almhirt.common._
import almhirt.almvalidation.kit._

trait AnotherTestArEvent extends DomainEvent

case class AnotherTestArCreated(header: DomainEventHeader) extends AnotherTestArEvent with CreatesNewAggregateRootEvent {
  override def changeMetadata(newMetaData: Map[String, String]): AnotherTestArCreated = copy(header = this.header.changeMetadata(newMetaData))
}

case class CChanged(header: DomainEventHeader, newC: Option[Int]) extends AnotherTestArEvent {
  override def changeMetadata(newMetaData: Map[String, String]): CChanged = copy(header = this.header.changeMetadata(newMetaData))
}

case class AnotherTestArDeleted(header: DomainEventHeader) extends AnotherTestArEvent with CreatesNewAggregateRootEvent {
  override def changeMetadata(newMetaData: Map[String, String]): AnotherTestArDeleted = copy(header = this.header.changeMetadata(newMetaData))
}

case class UnhandableAnotherTestArEvent(header: DomainEventHeader) extends AnotherTestArEvent {
  override def changeMetadata(newMetaData: Map[String, String]): UnhandableAnotherTestArEvent = copy(header = this.header.changeMetadata(newMetaData))
}

case class AnotherTestAr(ref: AggregateRootRef, theC: Option[Int], isDeleted: Boolean)
  extends AggregateRoot[AnotherTestAr, AnotherTestArEvent]
  with AggregateRootWithHandlers[AnotherTestAr, AnotherTestArEvent]
  with AggregateRootMutationHelpers[AnotherTestAr, AnotherTestArEvent] {

  protected override def handlers = {
    case CChanged(_, newC) => set((ar: AnotherTestAr, v: Option[Int]) => ar.copy(theC = v), newC)
    case AnotherTestArDeleted(_) => markDeleted((ar: AnotherTestAr, b: Boolean) => ar.copy(isDeleted = b))
  }

  def setC(newC: Int)(implicit ccuad: CanCreateUuidsAndDateTimes): UpdateRecorder[AnotherTestAr, AnotherTestArEvent] =
    update(CChanged(ref, Some(newC)))

  def unsetC()(implicit ccuad: CanCreateUuidsAndDateTimes): UpdateRecorder[AnotherTestAr, AnotherTestArEvent] =
    update(CChanged(ref, None))

  def delete()(implicit ccuad: CanCreateUuidsAndDateTimes): UpdateRecorder[AnotherTestAr, AnotherTestArEvent] =
    update(AnotherTestArDeleted(ref))

  protected override def updateRef(newRef: AggregateRootRef): AnotherTestAr = this.copy(ref = newRef)
}

object AnotherTestAr extends CanCreateAggragateRoot[AnotherTestAr, AnotherTestArEvent] {
  protected override def creationHandler: PartialFunction[AnotherTestArEvent, AnotherTestAr] = {
    case AnotherTestArCreated(header) =>
      AnotherTestAr(header.aggRef.inc, None, false)
  }

  def fromScratch(id: java.util.UUID)(implicit ccuad: CanCreateUuidsAndDateTimes): UpdateRecorder[AnotherTestAr, AnotherTestArEvent] =
    create(AnotherTestArCreated(DomainEventHeader(id, 0L)))
  
}

object AnotherTestArLenses {
  val theCL: AnotherTestAr @> Option[Int] = Lens.lensu((a, b) => a.copy(theC = b), _.theC)
}

object AnotherTestArCommanding {
  import almhirt.commanding._

  trait AnotherTestArCommand extends DomainCommand

  final case class CreateAnotherTestAr(header: DomainCommandHeader, newA: String) extends AnotherTestArCommand with CreatingDomainCommand {
    override def changeMetadata(newMetadata: Map[String, String]): CreateAnotherTestAr = copy(header = this.header.changeMetadata(newMetadata))
  }

  final case class SetC(header: DomainCommandHeader, newC: Int) extends AnotherTestArCommand {
    override def changeMetadata(newMetadata: Map[String, String]): SetC = copy(header = this.header.changeMetadata(newMetadata))
  }

   final case class UnsetC(header: DomainCommandHeader) extends AnotherTestArCommand {
    override def changeMetadata(newMetadata: Map[String, String]): UnsetC = copy(header = this.header.changeMetadata(newMetadata))
  }

  final case class DeleteAnotherTestAr(header: DomainCommandHeader) extends AnotherTestArCommand {
    override def changeMetadata(newMetadata: Map[String, String]): DeleteAnotherTestAr = copy(header = this.header.changeMetadata(newMetadata))
  }

  object Handlers {
    import almhirt.core.Almhirt
    import scala.concurrent.ExecutionContext
    def addCommands(registry: CommandHandlerRegistry)(implicit theAlmhirt: Almhirt): CommandHandlerRegistry = {
      val executionContext = theAlmhirt.futuresExecutor
      val createAdder =
        CreatingDomainCommandHandler.createRegistryAdderFromSyncFun[CreateAnotherTestAr, AnotherTestArEvent, AnotherTestAr](
          command => AnotherTestAr.fromScratch(command.targettedAggregateRootId).result,
          executionContext)
      val setCAdder =
        MutatingDomainCommandHandler.createRegistryAdderFromSyncFun[SetC, AnotherTestArEvent, AnotherTestAr](
          (ar, command) => ar.setC(command.newC).result,
          executionContext)
      val unssetCAdder =
        MutatingDomainCommandHandler.createRegistryAdderFromSyncFun[UnsetC, AnotherTestArEvent, AnotherTestAr](
          (ar, command) => ar.unsetC.result,
          executionContext)
      val deleteAdder =
        MutatingDomainCommandHandler.createRegistryAdderFromSyncFun[DeleteAnotherTestAr, AnotherTestArEvent, AnotherTestAr](
          (ar, command) => ar.delete.result,
          executionContext)

      registry nextAdder createAdder nextAdder setCAdder nextAdder unssetCAdder nextAdder deleteAdder
    }
  }
}
