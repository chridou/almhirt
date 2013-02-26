package almhirt.ext.core.riftwarp.serialization

import scalaz._, Scalaz._
import almhirt.common._
import almhirt.almvalidation.kit._
import riftwarp._
import almhirt.util._

object PerformedCreateActionDecomposer extends Decomposer[PerformedCreateAction] {
  val riftDescriptor = RiftDescriptor(classOf[PerformedCreateAction])
  val alternativeRiftDescriptors = Nil
  def decompose[TDimension <: RiftDimension](what: PerformedCreateAction, into: Dematerializer[TDimension]): AlmValidation[Dematerializer[TDimension]] = {
    into.addRiftDescriptor(this.riftDescriptor)
      .addComplexSelective("aggRef", AggregateRootRefDecomposer, what.aggRef)
  }
}

object PerformedUpdateActionDecomposer extends Decomposer[PerformedUpdateAction] {
  val riftDescriptor = RiftDescriptor(classOf[PerformedUpdateAction])
  val alternativeRiftDescriptors = Nil
  def decompose[TDimension <: RiftDimension](what: PerformedUpdateAction, into: Dematerializer[TDimension]): AlmValidation[Dematerializer[TDimension]] = {
    into.addRiftDescriptor(this.riftDescriptor)
      .addComplexSelective("aggRef", AggregateRootRefDecomposer, what.aggRef)
  }
}

object PerformedUnspecifiedActionDecomposer extends Decomposer[PerformedUnspecifiedAction.type] {
  val riftDescriptor = RiftDescriptor("almhirt.util.PerformedUnspecifiedAction")
  val alternativeRiftDescriptors = Nil
  def decompose[TDimension <: RiftDimension](what: PerformedUnspecifiedAction.type, into: Dematerializer[TDimension]): AlmValidation[Dematerializer[TDimension]] = {
    into.addRiftDescriptor(this.riftDescriptor).ok
  }
}

object PerformedActionDecomposer extends Decomposer[PerformedAction] {
  val riftDescriptor = RiftDescriptor(classOf[PerformedAction])
  val alternativeRiftDescriptors = Nil
  def decompose[TDimension <: RiftDimension](what: PerformedAction, into: Dematerializer[TDimension]): AlmValidation[Dematerializer[TDimension]] = {
    what match {
      case act @ PerformedCreateAction(_) => into.includeDirect(act, PerformedCreateActionDecomposer)
      case act @ PerformedUpdateAction(_) => into.includeDirect(act, PerformedUpdateActionDecomposer)
      case act @ PerformedUnspecifiedAction => into.includeDirect(act, PerformedUnspecifiedActionDecomposer)
    }
  }
}

object PerformedCreateActionRecomposer extends Recomposer[PerformedCreateAction] {
  val riftDescriptor = RiftDescriptor(classOf[PerformedCreateAction])
  val alternativeRiftDescriptors = Nil
  def recompose(from: Rematerializer): AlmValidation[PerformedCreateAction] = {
    from.getComplexType("aggRef", AggregateRootRefRecomposer).map(PerformedCreateAction.apply)
  }
}

object PerformedUpdateActionRecomposer extends Recomposer[PerformedUpdateAction] {
  val riftDescriptor = RiftDescriptor(classOf[PerformedUpdateAction])
  val alternativeRiftDescriptors = Nil
  def recompose(from: Rematerializer): AlmValidation[PerformedUpdateAction] = {
    from.getComplexType("aggRef", AggregateRootRefRecomposer).map(PerformedUpdateAction.apply)
  }
}

object PerformedUnspecifiedActionRecomposer extends Recomposer[PerformedUnspecifiedAction.type] {
  val riftDescriptor = RiftDescriptor("almhirt.util.PerformedUnspecifiedAction")
  val alternativeRiftDescriptors = Nil
  def recompose(from: Rematerializer): AlmValidation[PerformedUnspecifiedAction.type] = {
    PerformedUnspecifiedAction.success
  }
}

object PerformedActionRecomposer extends DivertingRecomposer[PerformedAction] {
  val riftDescriptor = RiftDescriptor(classOf[PerformedAction])
  val alternativeRiftDescriptors = Nil
  val divert =
    Map(
      PerformedCreateActionRecomposer.riftDescriptor -> PerformedCreateActionRecomposer,
      PerformedUpdateActionRecomposer.riftDescriptor -> PerformedUpdateActionRecomposer,
      PerformedUnspecifiedActionRecomposer.riftDescriptor -> PerformedUnspecifiedActionRecomposer).lift
}