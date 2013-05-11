package almhirt.ext.core.riftwarp.serialization

import org.joda.time.DateTime
import scalaz._, Scalaz._
import almhirt.common._
import almhirt.almvalidation.kit._
import riftwarp._
import riftwarp.std.kit._
import almhirt.util._

object InProcessWarpPacker extends WarpPacker[InProcess] with RegisterableWarpPacker { 
  val warpDescriptor = WarpDescriptor("InProcess")
  val alternativeWarpDescriptors = WarpDescriptor(classOf[InProcess]) :: Nil
  override def pack(what: InProcess)(implicit packers: WarpPackers): AlmValidation[WarpPackage] = 
    this.warpDescriptor ~>
      With("ticket", what.ticket, TrackingTicketWarpPacker) ~>
      With("commandInfo", what.commandInfo, CommandInfoWarpPacker) ~>
      P("timestamp", what.timestamp)
}


object ExecutedWarpPacker extends WarpPacker[Executed] with RegisterableWarpPacker {
  val warpDescriptor = WarpDescriptor(classOf[Executed])
  val alternativeWarpDescriptors = Nil
  override def pack(what: Executed)(implicit packers: WarpPackers): AlmValidation[WarpPackage] = 
    this.warpDescriptor ~>
      With("ticket", what.ticket, TrackingTicketWarpPacker) ~>
      With("action", what.action, PerformedActionWarpPacker) ~>
      P("timestamp", what.timestamp)
}


object NotExecutedWarpPacker extends WarpPacker[NotExecuted] with RegisterableWarpPacker {
  val warpDescriptor = WarpDescriptor(classOf[NotExecuted])
  val alternativeWarpDescriptors = Nil
  override def pack(what: NotExecuted)(implicit packers: WarpPackers): AlmValidation[WarpPackage] = 
    this.warpDescriptor ~>
      With("ticket", what.ticket, TrackingTicketWarpPacker) ~>
      LookUp("problem", what.problem) ~>
      P("timestamp", what.timestamp)
}

object OperationStateWarpPacker extends WarpPacker[OperationState] with RegisterableWarpPacker {
  val warpDescriptor = WarpDescriptor(classOf[OperationState])
  val alternativeWarpDescriptors = Nil
  override def pack(what: OperationState)(implicit packers: WarpPackers): AlmValidation[WarpPackage] = {
    what match {
      case opstate: InProcess => InProcessWarpPacker(opstate)
      case opstate: Executed => ExecutedWarpPacker(opstate)
      case opstate: NotExecuted => NotExecutedWarpPacker(opstate)
    }
  }
}

object InProcessWarpUnpacker extends RegisterableWarpUnpacker[InProcess] {
  val warpDescriptor = WarpDescriptor(classOf[InProcess])
  val alternativeWarpDescriptors = Nil
  def unpack(from: WarpPackage)(implicit unpackers: WarpUnpackers): AlmValidation[InProcess] =
    withFastLookUp(from) { lookup =>
      for {
     ticket <- lookup.getWith[TrackingTicket]("ticket", TrackingTicketWarpUnpacker)
    commandInfo <- lookup.getWith[CommandInfo]("commandInfo", CommandInfoWarpUnpacker)
    timestamp <- lookup.getAs[DateTime]("timestamp")
      } yield InProcess(ticket, commandInfo, timestamp)
  }
}

object ExecutedWarpUnpacker extends RegisterableWarpUnpacker[Executed] {
  val warpDescriptor = WarpDescriptor(classOf[Executed])
  val alternativeWarpDescriptors = Nil
  def unpack(from: WarpPackage)(implicit unpackers: WarpUnpackers): AlmValidation[Executed] =
    withFastLookUp(from) { lookup =>
      for {
     ticket <- lookup.getWith("ticket", TrackingTicketWarpUnpacker)
    action <- lookup.getWith("action", PerformedActionWarpUnpacker)
    timestamp <- lookup.getAs[DateTime]("timestamp")
      } yield Executed(ticket , action , timestamp)
  }
}

object NotExecutedWarpUnpacker extends RegisterableWarpUnpacker[NotExecuted] {
  val warpDescriptor = WarpDescriptor(classOf[NotExecuted])
  val alternativeWarpDescriptors = Nil
  def unpack(from: WarpPackage)(implicit unpackers: WarpUnpackers): AlmValidation[NotExecuted] =
    withFastLookUp(from) { lookup =>
      for {
     ticket <- lookup.getWith("ticket", TrackingTicketWarpUnpacker)
    problem <- lookup.getTyped[Problem]("problem", None)
    timestamp <- lookup.getAs[DateTime]("timestamp")
      } yield NotExecuted(ticket, problem, timestamp)
  }
}

object OperationStateWarpUnpacker extends RegisterableWarpUnpacker[OperationState] with DivertingWarpUnpacker[OperationState] {
  val warpDescriptor = WarpDescriptor(classOf[OperationState])
  val alternativeWarpDescriptors = Nil
  val divert =
    Map(
      InProcessWarpUnpacker.warpDescriptor -> InProcessWarpUnpacker,
      ExecutedWarpUnpacker.warpDescriptor -> ExecutedWarpUnpacker,
      NotExecutedWarpUnpacker.warpDescriptor -> NotExecutedWarpUnpacker).lift
}



