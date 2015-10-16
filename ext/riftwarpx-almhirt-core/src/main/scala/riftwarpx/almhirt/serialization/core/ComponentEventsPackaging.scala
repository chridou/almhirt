package riftwarpx.almhirt.serialization.core

import scalaz._, Scalaz._
import scalaz.Validation.FlatMap._
import almhirt.common._
import riftwarp._
import riftwarp.std.kit._
import riftwarp.std.WarpObjectLookUp
import almhirt.akkax.events._
import almhirt.akkax.{ GlobalComponentId }
import almhirt.herder.RuntimeStateRecorded

object FailureReportedWarpPackaging extends ComponentEventPackagingTemplate[FailureReported] {
  val warpDescriptor = WarpDescriptor("FailureReported")
  val alternativeWarpDescriptors = WarpDescriptor(classOf[FailureReported]) :: Nil

  def addEventParams(what: FailureReported, into: WarpObject)(implicit packers: WarpPackers): AlmValidation[WarpPackage] = {
    into ~>
      With("failure", what.failure, riftwarp.serialization.common.ProblemPackaging) ~>
      P("severity", what.severity.parseableString)
  }

  def extractEventParams(from: WarpObjectLookUp, header: EventHeader, origin: GlobalComponentId)(implicit unpackers: WarpUnpackers): AlmValidation[FailureReported] = {
    for {
      failure ← from.getWith("failure", riftwarp.serialization.common.ProblemPackaging)
      severity ← from.getAs[String]("severity").flatMap(almhirt.problem.Severity.fromString)
    } yield FailureReported(header, origin, failure, severity)
  }
}

object EventNotProcessedWarpPackaging extends ComponentEventPackagingTemplate[EventNotProcessed] {
  val warpDescriptor = WarpDescriptor("EventNotProcessed")
  val alternativeWarpDescriptors = WarpDescriptor(classOf[EventNotProcessed]) :: Nil

  def addEventParams(what: EventNotProcessed, into: WarpObject)(implicit packers: WarpPackers): AlmValidation[WarpPackage] = {
    into ~>
      P("missedEventId", what.missedEventId.value) ~>
      P("missedEventType", what.missedEventType) ~>
      P("severity", what.severity.parseableString)
  }

  def extractEventParams(from: WarpObjectLookUp, header: EventHeader, origin: GlobalComponentId)(implicit unpackers: WarpUnpackers): AlmValidation[EventNotProcessed] = {
    for {
      missedEventId ← from.getAs[String]("missedEventId")
      missedEventType ← from.getAs[String]("missedEventType")
      severity ← from.getAs[String]("severity").flatMap(almhirt.problem.Severity.fromString)
    } yield EventNotProcessed(header, origin, EventId(missedEventId), missedEventType, severity)
  }
}

object CommandRejectedWarpPackaging extends ComponentEventPackagingTemplate[CommandRejected] {
  val warpDescriptor = WarpDescriptor("CommandRejected")
  val alternativeWarpDescriptors = WarpDescriptor(classOf[CommandRejected]) :: Nil

  def addEventParams(what: CommandRejected, into: WarpObject)(implicit packers: WarpPackers): AlmValidation[WarpPackage] = {
    into ~>
      P("commandId", what.command.commandId.value) ~>
      P("commandType", what.command.commandType) ~>
      P("severity", what.severity.parseableString)
  }

  def extractEventParams(from: WarpObjectLookUp, header: EventHeader, origin: GlobalComponentId)(implicit unpackers: WarpUnpackers): AlmValidation[CommandRejected] = {
    for {
      commandId ← from.getAs[String]("commandId")
      commandType ← from.getAs[String]("commandType")
      severity ← from.getAs[String]("severity").flatMap(almhirt.problem.Severity.fromString)
    } yield CommandRejected(header, origin, almhirt.tracking.CommandRepresentation.CommandIdAndType(CommandId(commandId), commandType), severity)
  }
}

object RuntimeStateRecordedPackaging extends ComponentEventPackagingTemplate[RuntimeStateRecorded] {
  val warpDescriptor = WarpDescriptor("RuntimeStateRecorded")
  val alternativeWarpDescriptors = WarpDescriptor(classOf[RuntimeStateRecorded]) :: Nil

  def addEventParams(what: RuntimeStateRecorded, into: WarpObject)(implicit packers: WarpPackers): AlmValidation[WarpPackage] = {
    into ~>
      P("freeMemory", what.freeMemory) ~>
      P("maxMemory", what.maxMemory) ~>
      P("usedMemory", what.usedMemory) ~>
      P("totalMemory", what.totalMemory) ~>
      P("usedFractionFromTotal", what.usedFractionFromTotal) ~>
      P("usedFractionFromMax", what.usedFractionFromMax) ~>
      P("systemLoadAverage", what.systemLoadAverage)
  }

  def extractEventParams(from: WarpObjectLookUp, header: EventHeader, origin: GlobalComponentId)(implicit unpackers: WarpUnpackers): AlmValidation[RuntimeStateRecorded] = {
    for {
      freeMemory ← from.getAs[Long]("freeMemory")
      maxMemory ← from.getAs[Long]("maxMemory")
      usedMemory ← from.getAs[Long]("usedMemory")
      totalMemory ← from.getAs[Long]("totalMemory")
      usedFractionFromTotal ← from.getAs[Double]("usedFractionFromTotal")
      usedFractionFromMax ← from.getAs[Double]("usedFractionFromMax")
      systemLoadAverage ← from.getAs[Double]("systemLoadAverage")
    } yield RuntimeStateRecorded(header, origin,
      freeMemory = freeMemory,
      usedMemory = usedMemory,
      totalMemory = totalMemory,
      maxMemory = maxMemory,
      usedFractionFromTotal = usedFractionFromTotal,
      usedFractionFromMax = usedFractionFromMax,
      systemLoadAverage = systemLoadAverage)
  }
}