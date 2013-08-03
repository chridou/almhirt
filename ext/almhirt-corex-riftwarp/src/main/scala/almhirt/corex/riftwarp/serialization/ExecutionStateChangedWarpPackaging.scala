package almhirt.corex.riftwarp.serialization

import org.joda.time.DateTime
import scalaz._, Scalaz._
import almhirt.common._
import almhirt.almvalidation.kit._
import almhirt.commanding._
import riftwarp._
import riftwarp.std.kit._
import riftwarp.std.WarpObjectLookUp
import riftwarp.serialization.common.EventWarpPackagingTemplate

object ExecutionStateChangedWarpPackaging extends EventWarpPackagingTemplate[ExecutionStateChanged] with RegisterableWarpPacker {
  val warpDescriptor = WarpDescriptor("ExecutionStateChanged")
  val alternativeWarpDescriptors = WarpDescriptor(classOf[ExecutionStateChanged]) :: Nil

  override def addEventParams(what: ExecutionStateChanged, into: WarpObject)(implicit packers: WarpPackers): AlmValidation[WarpPackage] =
    into ~> LookUp("executionState", what.executionState)

  override def extractEventParams(from: WarpObjectLookUp, header: EventHeader)(implicit unpackers: WarpUnpackers): AlmValidation[ExecutionStateChanged] =
    from.getWith("executionState", ExecutionStateWarpPackaging).map(execState => ExecutionStateChanged(header, execState))
}

