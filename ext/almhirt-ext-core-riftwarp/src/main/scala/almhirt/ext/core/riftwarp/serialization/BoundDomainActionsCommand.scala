package almhirt.ext.core.riftwarp.serialization

import java.util.{ UUID => JUUID }
import scalaz._, Scalaz._
import almhirt.common._
import almhirt.almvalidation.kit._
import almhirt.commanding._
import riftwarp._
import riftwarp.std.kit._
import almhirt.domain._

class BoundDomainActionsCommandWarpPacker[TCom <: BoundDomainActionsCommandContext[TAR, TEvent]#BoundDomainActionsCommand, TAR <: AggregateRoot[TAR, TEvent], TEvent <: DomainEvent](val warpDescriptor: WarpDescriptor) extends WarpPacker[TCom] {
  val alternativeWarpDescriptors = Nil
  override def pack(what: TCom)(implicit packers: WarpPackers): AlmValidation[WarpPackage] = {
    this.warpDescriptor ~>
      P("id", what.id) ~>
      WithOpt("aggRef", what.aggRef, AggregateRootRefWarpPacker) ~>
      CLookUp("actions", what.actions)
  }
}

class BoundDomainActionsCommandWarpUnpacker[TContext <: BoundDomainActionsCommandContext[TAR, TEvent], TCom <: TContext#BoundDomainActionsCommand, TAR <: AggregateRoot[TAR, TEvent], TEvent <: DomainEvent](val warpDescriptor: WarpDescriptor, construct: (JUUID, Option[AggregateRootRef], List[TContext#BoundCommandAction]) => TCom) extends RegisterableWarpUnpacker[TCom] {
  val alternativeWarpDescriptors = Nil
  def unpack(from: WarpPackage)(implicit unpackers: WarpUnpackers): AlmValidation[TCom] = {
    withFastLookUp(from) { lookup =>
      for {
        id <- lookup.getAs[JUUID]("id").toAgg
        aggRef <- lookup.tryGetWith("aggRef", AggregateRootRefWarpUnpacker)
        actions <- lookup.getManyTyped[TContext#BoundCommandAction]("actions").map(_.toList)
      } yield construct(id, aggRef, actions)
    }
  }
}