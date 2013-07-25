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

case class AR1Deleted(header: DomainEventHeader) extends AR1Event {
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

  object Serialization {
    import riftwarp._
    import riftwarp.std.kit._
    import riftwarp.std.WarpObjectLookUp
    import almhirt.ext.core.riftwarp.serialization._

    implicit object AR1WarpPackaging extends WarpPacker[AR1] with RegisterableWarpPacker with RegisterableWarpUnpacker[AR1] {
      val warpDescriptor = WarpDescriptor("AR1")
      val alternativeWarpDescriptors = WarpDescriptor(classOf[AR1]) :: Nil

      override def pack(what: AR1)(implicit packers: WarpPackers): AlmValidation[WarpPackage] =
        this.warpDescriptor ~>
          With("ref", what.ref, AggregateRootRefWarpPackaging) ~>
          P("theA", what.theA) ~>
          POpt("theB", what.theB) ~>
          P("isDeleted", what.isDeleted)

      override def unpack(from: WarpPackage)(implicit unpackers: WarpUnpackers): AlmValidation[AR1] =
        withFastLookUp(from) { lu =>
          for {
            ref <- lu.getWith("ref", AggregateRootRefWarpPackaging)
            theA <- lu.getAs[String]("theA")
            theB <- lu.tryGetAs[String]("theB")
            isDeleted <- lu.getAs[Boolean]("isDeleted")
          } yield AR1(
            ref,
            theA,
            theB,
            isDeleted)
        }
    }

    implicit object AR1CreatedWarpPackaging extends DomainEventWarpPackagingTemplate[AR1Created] with RegisterableWarpPacker {
      override val warpDescriptor = WarpDescriptor(classOf[AR1Created].getSimpleName())
      override val alternativeWarpDescriptors = WarpDescriptor(classOf[AR1Created]) :: Nil
      override def addEventParams(what: AR1Created, into: WarpObject)(implicit packers: WarpPackers): AlmValidation[WarpPackage] =
        into ~>
          P("newA", what.newA)

      override def extractEventParams(from: WarpObjectLookUp, header: DomainEventHeader)(implicit unpackers: WarpUnpackers): AlmValidation[AR1Created] =
        for {
          newA <- from.getAs[String]("newA")
        } yield AR1Created(header, newA)
    }

    implicit object AR1AChangedWarpPackaging extends DomainEventWarpPackagingTemplate[AR1AChanged] with RegisterableWarpPacker {
      override val warpDescriptor = WarpDescriptor(classOf[AR1AChanged].getSimpleName())
      override val alternativeWarpDescriptors = WarpDescriptor(classOf[AR1AChanged]) :: Nil
      override def addEventParams(what: AR1AChanged, into: WarpObject)(implicit packers: WarpPackers): AlmValidation[WarpPackage] =
        into ~>
          P("newA", what.newA)

      override def extractEventParams(from: WarpObjectLookUp, header: DomainEventHeader)(implicit unpackers: WarpUnpackers): AlmValidation[AR1AChanged] =
        for {
          newA <- from.getAs[String]("newA")
        } yield AR1AChanged(header, newA)
    }

    implicit object AR1BChangedWarpPackaging extends DomainEventWarpPackagingTemplate[AR1BChanged] with RegisterableWarpPacker {
      override val warpDescriptor = WarpDescriptor(classOf[AR1BChanged].getSimpleName())
      override val alternativeWarpDescriptors = WarpDescriptor(classOf[AR1BChanged]) :: Nil
      override def addEventParams(what: AR1BChanged, into: WarpObject)(implicit packers: WarpPackers): AlmValidation[WarpPackage] =
        into ~>
          POpt("newB", what.newB)

      override def extractEventParams(from: WarpObjectLookUp, header: DomainEventHeader)(implicit unpackers: WarpUnpackers): AlmValidation[AR1BChanged] =
        for {
          newB <- from.tryGetAs[String]("newB")
        } yield AR1BChanged(header, newB)
    }

    implicit object AR1DeletedWarpPackaging extends DomainEventWarpPackagingTemplate[AR1Deleted] with RegisterableWarpPacker {
      override val warpDescriptor = WarpDescriptor(classOf[AR1Deleted].getSimpleName())
      override val alternativeWarpDescriptors = WarpDescriptor(classOf[AR1Deleted]) :: Nil
      override def addEventParams(what: AR1Deleted, into: WarpObject)(implicit packers: WarpPackers): AlmValidation[WarpPackage] =
        into.success

      override def extractEventParams(from: WarpObjectLookUp, header: DomainEventHeader)(implicit unpackers: WarpUnpackers): AlmValidation[AR1Deleted] =
        AR1Deleted(header).success
    }

    implicit object AR1UnhandableEventWarpPackaging extends DomainEventWarpPackagingTemplate[AR1UnhandableEvent] with RegisterableWarpPacker {
      override val warpDescriptor = WarpDescriptor(classOf[AR1UnhandableEvent].getSimpleName())
      override val alternativeWarpDescriptors = WarpDescriptor(classOf[AR1UnhandableEvent]) :: Nil
      override def addEventParams(what: AR1UnhandableEvent, into: WarpObject)(implicit packers: WarpPackers): AlmValidation[WarpPackage] =
        into.success

      override def extractEventParams(from: WarpObjectLookUp, header: DomainEventHeader)(implicit unpackers: WarpUnpackers): AlmValidation[AR1UnhandableEvent] =
        AR1UnhandableEvent(header).success
    }

    implicit object AR1ComCreateAR1WarpPackaging extends DomainCommandWarpPackagingTemplate[AR1ComCreateAR1] with RegisterableWarpPacker {
      override val warpDescriptor = WarpDescriptor(classOf[AR1ComCreateAR1].getSimpleName())
      override val alternativeWarpDescriptors = WarpDescriptor(classOf[AR1ComCreateAR1]) :: Nil
      override def addCommandParams(what: AR1ComCreateAR1, into: WarpObject)(implicit packers: WarpPackers): AlmValidation[WarpPackage] =
        into ~>
          P("newA", what.newA)

      override def extractCommandParams(from: WarpObjectLookUp, header: DomainCommandHeader)(implicit unpackers: WarpUnpackers): AlmValidation[AR1ComCreateAR1] =
        for {
          newA <- from.getAs[String]("newA")
        } yield AR1ComCreateAR1(header, newA)
    }

    implicit object AR1ComChangeAWarpPackaging extends DomainCommandWarpPackagingTemplate[AR1ComChangeA] with RegisterableWarpPacker {
      override val warpDescriptor = WarpDescriptor(classOf[AR1ComChangeA].getSimpleName())
      override val alternativeWarpDescriptors = WarpDescriptor(classOf[AR1ComChangeA]) :: Nil
      override def addCommandParams(what: AR1ComChangeA, into: WarpObject)(implicit packers: WarpPackers): AlmValidation[WarpPackage] =
        into ~>
          P("newA", what.newA)

      override def extractCommandParams(from: WarpObjectLookUp, header: DomainCommandHeader)(implicit unpackers: WarpUnpackers): AlmValidation[AR1ComChangeA] =
        for {
          newA <- from.getAs[String]("newA")
        } yield AR1ComChangeA(header, newA)
    }

    implicit object AR1ComChangeBWarpPackaging extends DomainCommandWarpPackagingTemplate[AR1ComChangeB] with RegisterableWarpPacker {
      override val warpDescriptor = WarpDescriptor(classOf[AR1ComChangeB].getSimpleName())
      override val alternativeWarpDescriptors = WarpDescriptor(classOf[AR1ComChangeB]) :: Nil
      override def addCommandParams(what: AR1ComChangeB, into: WarpObject)(implicit packers: WarpPackers): AlmValidation[WarpPackage] =
        into ~>
          POpt("newB", what.newB)

      override def extractCommandParams(from: WarpObjectLookUp, header: DomainCommandHeader)(implicit unpackers: WarpUnpackers): AlmValidation[AR1ComChangeB] =
        for {
          newB <- from.tryGetAs[String]("newB")
        } yield AR1ComChangeB(header, newB)
    }

    implicit object AR1ComDeleteAR1WarpPackaging extends DomainCommandWarpPackagingTemplate[AR1ComDeleteAR1] with RegisterableWarpPacker {
      override val warpDescriptor = WarpDescriptor(classOf[AR1ComDeleteAR1].getSimpleName())
      override val alternativeWarpDescriptors = WarpDescriptor(classOf[AR1ComDeleteAR1]) :: Nil
      override def addCommandParams(what: AR1ComDeleteAR1, into: WarpObject)(implicit packers: WarpPackers): AlmValidation[WarpPackage] =
        into.success

      override def extractCommandParams(from: WarpObjectLookUp, header: DomainCommandHeader)(implicit unpackers: WarpUnpackers): AlmValidation[AR1ComDeleteAR1] =
        AR1ComDeleteAR1(header).success
    }

    implicit object AR1ComUnregisteredCommandWarpPackaging extends DomainCommandWarpPackagingTemplate[AR1ComUnregisteredCommand] with RegisterableWarpPacker {
      override val warpDescriptor = WarpDescriptor(classOf[AR1ComUnregisteredCommand].getSimpleName())
      override val alternativeWarpDescriptors = WarpDescriptor(classOf[AR1ComUnregisteredCommand]) :: Nil
      override def addCommandParams(what: AR1ComUnregisteredCommand, into: WarpObject)(implicit packers: WarpPackers): AlmValidation[WarpPackage] =
        into.success

      override def extractCommandParams(from: WarpObjectLookUp, header: DomainCommandHeader)(implicit unpackers: WarpUnpackers): AlmValidation[AR1ComUnregisteredCommand] =
        AR1ComUnregisteredCommand(header).success
    }

    implicit class AR1SerAddToRiftWarp(self: RiftWarp) {
      def addAr1Serializers: RiftWarp = {
        List(AR1WarpPackaging,
          AR1CreatedWarpPackaging, AR1AChangedWarpPackaging, AR1BChangedWarpPackaging, AR1DeletedWarpPackaging, AR1UnhandableEventWarpPackaging,
          AR1ComCreateAR1WarpPackaging, AR1ComChangeAWarpPackaging, AR1ComChangeBWarpPackaging, AR1ComDeleteAR1WarpPackaging, AR1ComUnregisteredCommandWarpPackaging).foreach { packaging =>
            self.packers.add(packaging)
            self.unpackers.add(packaging)
          }
        self
      }
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
