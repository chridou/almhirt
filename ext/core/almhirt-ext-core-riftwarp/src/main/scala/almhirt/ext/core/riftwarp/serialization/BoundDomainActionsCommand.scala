package almhirt.ext.core.riftwarp.serialization

import java.util.{ UUID => JUUID }
import scalaz._, Scalaz._
import almhirt.common._
import almhirt.almvalidation.kit._
import almhirt.commanding._
import riftwarp._
import almhirt.domain._

class BoundDomainActionsCommandDecomposer[TCom <: BoundDomainActionsCommandContext[TAR, TEvent]#BoundDomainActionsCommand, TAR <: AggregateRoot[TAR, TEvent], TEvent <: DomainEvent](val riftDescriptor: RiftDescriptor) extends Decomposer[TCom] {
  val alternativeRiftDescriptors = Nil
  def decompose[TDimension <: RiftDimension](what: TCom, into: Dematerializer[TDimension]): AlmValidation[Dematerializer[TDimension]] = {
    into
      .addRiftDescriptor(this.riftDescriptor)
      .addUuid("id", what.id)
      .addOptionalComplex("aggRef", what.aggRef, Some(classOf[AggregateRootRef])).flatMap(
        _.addIterableOfComplex("actions", what.actions, None))
  }
}

class BoundDomainActionsCommandRecomposer[TContext <: BoundDomainActionsCommandContext[TAR, TEvent], TCom <: TContext#BoundDomainActionsCommand, TAR <: AggregateRoot[TAR, TEvent], TEvent <: DomainEvent](val riftDescriptor: RiftDescriptor, construct: (JUUID, Option[AggregateRootRef], List[TContext#BoundCommandAction]) => TCom) extends Recomposer[TCom] {
  val alternativeRiftDescriptors = Nil
  def recompose(from: Rematerializer): AlmValidation[TCom] = {
    val id = from.getUuid("id").toAgg
    val aggRef = from.tryGetComplexType[AggregateRootRef]("aggRef").toAgg
    val actions = from.getComplexMALoose[List, TContext#BoundCommandAction]("actions").toAgg
    (id |@| aggRef |@| actions)(construct)
  }
}