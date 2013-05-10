package almhirt.ext.core.riftwarp.serialization

import scalaz._, Scalaz._
import almhirt.common._
import almhirt.almvalidation.kit._
import almhirt.util._
import riftwarp._
import almhirt.commanding.DomainCommand
import almhirt.domain.AggregateRootRef

object FullComandInfoDecomposer extends Decomposer[FullComandInfo] {
  val riftDescriptor = RiftDescriptor(classOf[FullComandInfo])
  val alternativeRiftDescriptors = Nil
  def decompose[TDimension <: RiftDimension](what: FullComandInfo, into: WarpSequencer[TDimension]): AlmValidation[WarpSequencer[TDimension]] = {
    into.addRiftDescriptor(this.riftDescriptor)
      .addComplex("command", what.command, None)
  }
}

object HeadCommandInfoDecomposer extends Decomposer[HeadCommandInfo] {
  val riftDescriptor = RiftDescriptor(classOf[HeadCommandInfo])
  val alternativeRiftDescriptors = Nil
  def decompose[TDimension <: RiftDimension](what: HeadCommandInfo, into: WarpSequencer[TDimension]): AlmValidation[WarpSequencer[TDimension]] = {
    into.addRiftDescriptor(this.riftDescriptor)
      .addUuid("commandId", what.commandId)
      .addString("commandType", what.commandType)
      .addOptionalWith("aggRef", what.aggRef, AggregateRootRefDecomposer)
  }
}

object CommandInfoDecomposer extends Decomposer[CommandInfo] {
  val riftDescriptor = RiftDescriptor(classOf[CommandInfo])
  val alternativeRiftDescriptors = Nil
  def decompose[TDimension <: RiftDimension](what: CommandInfo, into: WarpSequencer[TDimension]): AlmValidation[WarpSequencer[TDimension]] = {
    what match {
      case ci: FullComandInfo => into.includeDirect(ci, FullComandInfoDecomposer)
      case ci: HeadCommandInfo => into.includeDirect(ci, HeadCommandInfoDecomposer)
    }
  }
}

object FullComandInfoRecomposer extends Recomposer[FullComandInfo] {
  val riftDescriptor = RiftDescriptor(classOf[FullComandInfo])
  val alternativeRiftDescriptors = Nil
  def recompose(from: Extractor): AlmValidation[FullComandInfo] = {
    from.getComplexByTag[DomainCommand]("command", None).map(FullComandInfo.apply)
  }
}

object HeadCommandInfoRecomposer extends Recomposer[HeadCommandInfo] {
  val riftDescriptor = RiftDescriptor(classOf[HeadCommandInfo])
  val alternativeRiftDescriptors = Nil
  def recompose(from: Extractor): AlmValidation[HeadCommandInfo] = {
    val commandId = from.getUuid("commandId").toAgg
    val commandType = from.getString("commandType").toAgg
    val aggRef = from.tryGetWith[AggregateRootRef]("aggRef", AggregateRootRefRecomposer.recompose).toAgg
    (commandId |@| commandType |@| aggRef)(HeadCommandInfo.apply)
  }
}

object CommandInfoRecomposer extends DivertingRecomposer[CommandInfo] {
  val riftDescriptor = RiftDescriptor(classOf[CommandInfo])
  val alternativeRiftDescriptors = Nil
  val divert =
    Map(
      FullComandInfoRecomposer.riftDescriptor -> FullComandInfoRecomposer,
      HeadCommandInfoRecomposer.riftDescriptor -> HeadCommandInfoRecomposer).lift
}