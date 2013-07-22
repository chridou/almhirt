package almhirt.ext.core.riftwarp.serialization

import org.joda.time.LocalDateTime
import scalaz._, Scalaz._
import almhirt.common._
import almhirt.almvalidation.kit._
import almhirt.commanding._
import riftwarp._
import riftwarp.std.kit._

object ExecutionStartedWarpPackaging extends WarpPacker[ExecutionStarted] with RegisterableWarpPacker with RegisterableWarpUnpacker[ExecutionStarted] {
  val warpDescriptor = WarpDescriptor("ExecutionStarted")
  val alternativeWarpDescriptors = WarpDescriptor(classOf[ExecutionStarted]) :: Nil
  override def pack(what: ExecutionStarted)(implicit packers: WarpPackers): AlmValidation[WarpPackage] =
    this.warpDescriptor ~>
      P("trackId", what.trackId) ~>
      P("timestamp", what.timestamp) ~>
      MP("metadata", what.metadata)

  def unpack(from: WarpPackage)(implicit unpackers: WarpUnpackers): AlmValidation[ExecutionStarted] =
    withFastLookUp(from) { lookup =>
      for {
        trackId <- lookup.getAs[String]("trackId")
        timestamp <- lookup.getAs[LocalDateTime]("timestamp")
        metadata <- lookup.getPrimitiveAssocs[String, String]("metadata").map(_.toMap)
      } yield ExecutionStarted(trackId, timestamp, metadata)
    }
}

object ExecutionInProcessWarpPackaging extends WarpPacker[ExecutionInProcess] with RegisterableWarpPacker with RegisterableWarpUnpacker[ExecutionInProcess] {
  val warpDescriptor = WarpDescriptor("ExecutionInProcess")
  val alternativeWarpDescriptors = WarpDescriptor(classOf[ExecutionInProcess]) :: Nil
  override def pack(what: ExecutionInProcess)(implicit packers: WarpPackers): AlmValidation[WarpPackage] =
    this.warpDescriptor ~>
      P("trackId", what.trackId) ~>
      P("timestamp", what.timestamp) ~>
      MP("metadata", what.metadata)

  def unpack(from: WarpPackage)(implicit unpackers: WarpUnpackers): AlmValidation[ExecutionInProcess] =
    withFastLookUp(from) { lookup =>
      for {
        trackId <- lookup.getAs[String]("trackId")
        timestamp <- lookup.getAs[LocalDateTime]("timestamp")
        metadata <- lookup.getPrimitiveAssocs[String, String]("metadata").map(_.toMap)
      } yield ExecutionInProcess(trackId, timestamp, metadata)
    }
}

object ExecutionSuccessfulWarpPackaging extends WarpPacker[ExecutionSuccessful] with RegisterableWarpPacker with RegisterableWarpUnpacker[ExecutionSuccessful] {
  val warpDescriptor = WarpDescriptor("ExecutionSuccessful")
  val alternativeWarpDescriptors = WarpDescriptor(classOf[ExecutionSuccessful]) :: Nil
  override def pack(what: ExecutionSuccessful)(implicit packers: WarpPackers): AlmValidation[WarpPackage] =
    this.warpDescriptor ~>
      P("trackId", what.trackId) ~>
      P("message", what.message) ~>
      P("timestamp", what.timestamp) ~>
      MP("metadata", what.metadata)

  def unpack(from: WarpPackage)(implicit unpackers: WarpUnpackers): AlmValidation[ExecutionSuccessful] =
    withFastLookUp(from) { lookup =>
      for {
        trackId <- lookup.getAs[String]("trackId")
        message <- lookup.getAs[String]("message")
        timestamp <- lookup.getAs[LocalDateTime]("timestamp")
        metadata <- lookup.getPrimitiveAssocs[String, String]("metadata").map(_.toMap)
      } yield ExecutionSuccessful(trackId, message, timestamp, metadata)
    }
}

object ExecutionFailedWarpPackaging extends WarpPacker[ExecutionFailed] with RegisterableWarpPacker with RegisterableWarpUnpacker[ExecutionFailed] {
  val warpDescriptor = WarpDescriptor("ExecutionFailed")
  val alternativeWarpDescriptors = WarpDescriptor(classOf[ExecutionFailed]) :: Nil
  override def pack(what: ExecutionFailed)(implicit packers: WarpPackers): AlmValidation[WarpPackage] =
    this.warpDescriptor ~>
      P("trackId", what.trackId) ~>
      LookUp("problem", what.problem) ~>
      P("timestamp", what.timestamp) ~>
      MP("metadata", what.metadata)

  def unpack(from: WarpPackage)(implicit unpackers: WarpUnpackers): AlmValidation[ExecutionFailed] =
    withFastLookUp(from) { lookup =>
      for {
        trackId <- lookup.getAs[String]("trackId")
        problem <- lookup.getTyped[Problem]("problem")
        timestamp <- lookup.getAs[LocalDateTime]("timestamp")
        metadata <- lookup.getPrimitiveAssocs[String, String]("metadata").map(_.toMap)
      } yield ExecutionFailed(trackId, problem, timestamp, metadata)
    }
}

object ExecutionFinishedStateWarpPackaging extends WarpPacker[ExecutionFinishedState] with RegisterableWarpPacker with RegisterableWarpUnpacker[ExecutionFinishedState]  with DivertingWarpUnpacker[ExecutionFinishedState] with DivertingWarpUnpackerWithAutoRegistration[ExecutionFinishedState]{
  val warpDescriptor = WarpDescriptor("ExecutionFinishedState")
  val alternativeWarpDescriptors = WarpDescriptor(classOf[ExecutionFinishedState]) :: Nil
  override def pack(what: ExecutionFinishedState)(implicit packers: WarpPackers): AlmValidation[WarpPackage] =
    what match {
      case st: ExecutionSuccessful => ExecutionSuccessfulWarpPackaging(st)
      case st: ExecutionFailed => ExecutionFailedWarpPackaging(st)
    }

  def unpackers = ExecutionSuccessfulWarpPackaging :: ExecutionFailedWarpPackaging :: Nil
}

object ExecutionStateWarpPackaging extends WarpPacker[ExecutionState] with RegisterableWarpPacker with RegisterableWarpUnpacker[ExecutionFinishedState]  with DivertingWarpUnpacker[ExecutionState] with DivertingWarpUnpackerWithAutoRegistration[ExecutionState]{
  val warpDescriptor = WarpDescriptor("ExecutionState")
  val alternativeWarpDescriptors = WarpDescriptor(classOf[ExecutionState]) :: Nil
  override def pack(what: ExecutionState)(implicit packers: WarpPackers): AlmValidation[WarpPackage] =
    what match {
      case st: ExecutionStarted => ExecutionStartedWarpPackaging(st)
      case st: ExecutionInProcess => ExecutionInProcessWarpPackaging(st)
      case st: ExecutionFinishedState => ExecutionFinishedStateWarpPackaging(st)
    }

  def unpackers = ExecutionStartedWarpPackaging :: ExecutionInProcessWarpPackaging ::  ExecutionSuccessfulWarpPackaging :: ExecutionFailedWarpPackaging :: Nil
}


