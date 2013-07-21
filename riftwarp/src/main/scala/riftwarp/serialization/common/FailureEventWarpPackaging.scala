package riftwarp.serialization.common

import org.joda.time.DateTime
import scalaz._, Scalaz._
import almhirt.common._
import almhirt.almvalidation.kit._
import riftwarp._
import riftwarp.std.kit._
import riftwarp.std.WarpObjectLookUp

object ProblemOccurredWarpPackaging extends EventWarpPackagingTemplate[ProblemOccurred] with RegisterableWarpPacker {
  val warpDescriptor = WarpDescriptor("ProblemOccurred")
  val alternativeWarpDescriptors = WarpDescriptor(classOf[ProblemOccurred]) :: Nil

  override def addEventParams(what: ProblemOccurred, into: WarpObject)(implicit packers: WarpPackers): AlmValidation[WarpPackage] =
    into ~>
      P("context", what.context) ~>
      With("problem", what.problem, ProblemPackaging) ~>
      P("severity", what.severity.parseableString)

  override def extractEventParams(from: WarpObjectLookUp, header: EventHeader)(implicit unpackers: WarpUnpackers): AlmValidation[ProblemOccurred] =
    for {
      context <- from.getAs[String]("context")
      problem <- from.getWith[Problem]("problem", ProblemPackaging)
      severityStr <- from.getAs[String]("severity")
      severity <- almhirt.problem.Severity.fromString(severityStr)
    } yield ProblemOccurred(header, context, problem, severity)
}

object ExceptionOccurredWarpPackaging extends EventWarpPackagingTemplate[ExceptionOccurred] with RegisterableWarpPacker {
  val warpDescriptor = WarpDescriptor("ExceptionOccurred")
  val alternativeWarpDescriptors = WarpDescriptor(classOf[ExceptionOccurred]) :: Nil

  override def addEventParams(what: ExceptionOccurred, into: WarpObject)(implicit packers: WarpPackers): AlmValidation[WarpPackage] =
    into ~>
      P("context", what.context) ~>
      P("exnType", what.exnType) ~>
      P("exnMessage", what.exnMessage) ~>
      POpt("exnStackTrace", what.exnStackTrace) ~>
      P("severity", what.severity.parseableString)

  override def extractEventParams(from: WarpObjectLookUp, header: EventHeader)(implicit unpackers: WarpUnpackers): AlmValidation[ExceptionOccurred] =
    for {
      context <- from.getAs[String]("context")
      exnType <- from.getAs[String]("exnType")
      exnMessage <- from.getAs[String]("exnMessage")
      exnType <- from.getAs[String]("exnType")
      exnStackTrace <- from.tryGetAs[String]("exnStackTrace")
      severityStr <- from.getAs[String]("severity")
      severity <- almhirt.problem.Severity.fromString(severityStr)
    } yield ExceptionOccurred(header, context, exnType, exnMessage, exnStackTrace, severity)
}

object FailureEventWarpPackaging extends WarpPacker[FailureEvent] with RegisterableWarpPacker with RegisterableWarpUnpacker[FailureEvent]  with DivertingWarpUnpacker[FailureEvent] with DivertingWarpUnpackerWithAutoRegistration[FailureEvent]{
  val warpDescriptor = WarpDescriptor("FailureEvent")
  val alternativeWarpDescriptors = WarpDescriptor(classOf[FailureEvent]) :: Nil
  override def pack(what: FailureEvent)(implicit packers: WarpPackers): AlmValidation[WarpPackage] =
    what match {
      case x: ProblemOccurred => ProblemOccurredWarpPackaging(x)
      case x: ExceptionOccurred => ExceptionOccurredWarpPackaging(x)
    }

  def unpackers = ProblemOccurredWarpPackaging :: ExceptionOccurredWarpPackaging :: Nil
}