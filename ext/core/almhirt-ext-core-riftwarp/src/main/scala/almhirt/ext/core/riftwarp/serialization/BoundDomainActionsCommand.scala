package almhirt.ext.core.riftwarp.serialization

import java.util.{ UUID => JUUID }
import scalaz._, Scalaz._
import almhirt.common._
import almhirt.almvalidation.kit._
import almhirt.commanding._
import riftwarp._
import almhirt.domain._

class BoundDomainActionsCommandDecomposer[TCom <: BoundDomainActionsCommandContext[TAR, TEvent]#BoundDomainActionsCommand, TAR <: AggregateRoot[TAR, TEvent], TEvent <: DomainEvent](val riftDescriptor: RiftDescriptor) extends Decomposer[TCom] {
  def decompose[TDimension <: RiftDimension](what: TCom)(into: Dematerializer[TDimension]): AlmValidation[Dematerializer[TDimension]] = {
    into
      .addRiftDescriptor(this.riftDescriptor).flatMap(
        _.addUuid("id", what.id).flatMap(
          _.addOptionalComplex("aggRef", what.aggRef, Some(classOf[AggregateRootRef])).flatMap(
            _.addComplexMALoose("actions", what.actions))))
  }
}

class BoundDomainActionsCommandRecomposer[TCom <: BoundDomainActionsCommandContext[TAR, TEvent]#BoundDomainActionsCommand, TAR <: AggregateRoot[TAR, TEvent], TEvent <: DomainEvent](val riftDescriptor: RiftDescriptor, construct: (JUUID, Option[AggregateRootRef], List[TCom#TAction]) => TCom) extends Recomposer[TCom] {
  def recompose(from: Rematerializer): AlmValidation[TCom] = {
    val id = from.getUuid("id").toAgg
    val aggRef = from.tryGetComplexType[AggregateRootRef]("aggRef").toAgg
    val actions = from.getComplexMALoose[List, TCom#TAction]("actions").toAgg
    (id |@| aggRef |@| actions)(construct)
  }
}