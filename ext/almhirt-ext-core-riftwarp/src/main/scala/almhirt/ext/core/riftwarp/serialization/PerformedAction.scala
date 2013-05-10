package almhirt.ext.core.riftwarp.serialization

import scalaz._, Scalaz._
import almhirt.common._
import almhirt.almvalidation.kit._
import riftwarp._
import almhirt.util._

object PerformedCreateActionDecomposer extends Decomposer[PerformedCreateAction] {
  val riftDescriptor = RiftDescriptor(classOf[PerformedCreateAction])
  val alternativeRiftDescriptors = Nil
  def decompose[TDimension <: RiftDimension](what: PerformedCreateAction, into: WarpSequencer[TDimension]): AlmValidation[WarpSequencer[TDimension]] = {
    into.addRiftDescriptor(this.riftDescriptor)
      .addWith("aggRef", what.aggRef, AggregateRootRefDecomposer)
  }
}

object PerformedUpdateActionDecomposer extends Decomposer[PerformedUpdateAction] {
  val riftDescriptor = RiftDescriptor(classOf[PerformedUpdateAction])
  val alternativeRiftDescriptors = Nil
  def decompose[TDimension <: RiftDimension](what: PerformedUpdateAction, into: WarpSequencer[TDimension]): AlmValidation[WarpSequencer[TDimension]] = {
    into.addRiftDescriptor(this.riftDescriptor)
      .addWith("aggRef", what.aggRef, AggregateRootRefDecomposer)
  }
}

object PerformedDeleteActionDecomposer extends Decomposer[PerformedDeleteAction] {
  val riftDescriptor = RiftDescriptor(classOf[PerformedDeleteAction])
  val alternativeRiftDescriptors = Nil
  def decompose[TDimension <: RiftDimension](what: PerformedDeleteAction, into: WarpSequencer[TDimension]): AlmValidation[WarpSequencer[TDimension]] = {
    into.addRiftDescriptor(this.riftDescriptor)
      .addWith("aggRef", what.aggRef, AggregateRootRefDecomposer)
  }
}

object PerformedNoActionDecomposer extends Decomposer[PerformedNoAction] {
  val riftDescriptor = RiftDescriptor(classOf[PerformedNoAction])
  val alternativeRiftDescriptors = Nil
  def decompose[TDimension <: RiftDimension](what: PerformedNoAction, into: WarpSequencer[TDimension]): AlmValidation[WarpSequencer[TDimension]] = {
    into.addRiftDescriptor(this.riftDescriptor).addString("reason", what.reason).ok
  }
}

object PerformedDomainActionDecomposer extends Decomposer[PerformedDomainAction] {
  val riftDescriptor = RiftDescriptor(classOf[PerformedDomainAction])
  val alternativeRiftDescriptors = Nil
  def decompose[TDimension <: RiftDimension](what: PerformedDomainAction, into: WarpSequencer[TDimension]): AlmValidation[WarpSequencer[TDimension]] = {
    what match {
      case act: PerformedCreateAction => into.includeDirect(act, PerformedCreateActionDecomposer)
      case act: PerformedUpdateAction => into.includeDirect(act, PerformedUpdateActionDecomposer)
      case act: PerformedDeleteAction => into.includeDirect(act, PerformedDeleteActionDecomposer)
      case act: PerformedNoAction => into.includeDirect(act, PerformedNoActionDecomposer)
    }
  }
}

object PerformedActionDecomposer extends Decomposer[PerformedAction] {
  val riftDescriptor = RiftDescriptor(classOf[PerformedAction])
  val alternativeRiftDescriptors = Nil
  def decompose[TDimension <: RiftDimension](what: PerformedAction, into: WarpSequencer[TDimension]): AlmValidation[WarpSequencer[TDimension]] = {
    what match {
      case act: PerformedDomainAction => into.includeDirect(act, PerformedDomainActionDecomposer)
    }
  }
}

object PerformedCreateActionRecomposer extends Recomposer[PerformedCreateAction] {
  val riftDescriptor = RiftDescriptor(classOf[PerformedCreateAction])
  val alternativeRiftDescriptors = Nil
  def recompose(from: Extractor): AlmValidation[PerformedCreateAction] = {
    from.getWith("aggRef", AggregateRootRefRecomposer.recompose).map(PerformedCreateAction.apply)
  }
}

object PerformedUpdateActionRecomposer extends Recomposer[PerformedUpdateAction] {
  val riftDescriptor = RiftDescriptor(classOf[PerformedUpdateAction])
  val alternativeRiftDescriptors = Nil
  def recompose(from: Extractor): AlmValidation[PerformedUpdateAction] = {
    from.getWith("aggRef", AggregateRootRefRecomposer.recompose).map(PerformedUpdateAction.apply)
  }
}

object PerformedDeleteActionRecomposer extends Recomposer[PerformedDeleteAction] {
  val riftDescriptor = RiftDescriptor(classOf[PerformedDeleteAction])
  val alternativeRiftDescriptors = Nil
  def recompose(from: Extractor): AlmValidation[PerformedDeleteAction] = {
    from.getWith("aggRef", AggregateRootRefRecomposer.recompose).map(PerformedDeleteAction.apply)
  }
}

object PerformedNoActionRecomposer extends Recomposer[PerformedNoAction] {
  val riftDescriptor = RiftDescriptor(classOf[PerformedNoAction])
  val alternativeRiftDescriptors = Nil
  def recompose(from: Extractor): AlmValidation[PerformedNoAction] = {
    from.getString("reason").map(PerformedNoAction)
  }
}

object PerformedDomainActionRecomposer extends DivertingRecomposer[PerformedDomainAction] {
  val riftDescriptor = RiftDescriptor(classOf[PerformedDomainAction])
  val alternativeRiftDescriptors = Nil
  val divert =
    Map(
      PerformedCreateActionRecomposer.riftDescriptor -> PerformedCreateActionRecomposer,
      PerformedUpdateActionRecomposer.riftDescriptor -> PerformedUpdateActionRecomposer,
      PerformedDeleteActionRecomposer.riftDescriptor -> PerformedDeleteActionRecomposer,
      PerformedNoActionRecomposer.riftDescriptor -> PerformedNoActionRecomposer).lift
}

object PerformedActionRecomposer extends DivertingRecomposer[PerformedAction] {
  val riftDescriptor = RiftDescriptor(classOf[PerformedAction])
  val alternativeRiftDescriptors = Nil
  val divert =
    Map(
      PerformedCreateActionRecomposer.riftDescriptor -> PerformedCreateActionRecomposer,
      PerformedUpdateActionRecomposer.riftDescriptor -> PerformedUpdateActionRecomposer,
      PerformedDeleteActionRecomposer.riftDescriptor -> PerformedDeleteActionRecomposer,
      PerformedNoActionRecomposer.riftDescriptor -> PerformedNoActionRecomposer).lift
}