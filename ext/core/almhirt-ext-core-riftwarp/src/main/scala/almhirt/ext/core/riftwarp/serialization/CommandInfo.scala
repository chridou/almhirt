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
  def decompose[TDimension <: RiftDimension](what: FullComandInfo)(into: Dematerializer[TDimension]): AlmValidation[Dematerializer[TDimension]] = {
    into.addRiftDescriptor(this.riftDescriptor).flatMap(
      _.addComplex("command", what.command, None))
  }
}

object HeadCommandInfoDecomposer extends Decomposer[HeadCommandInfo] {
  val riftDescriptor = RiftDescriptor(classOf[HeadCommandInfo])
  val alternativeRiftDescriptors = Nil
  def decompose[TDimension <: RiftDimension](what: HeadCommandInfo)(into: Dematerializer[TDimension]): AlmValidation[Dematerializer[TDimension]] = {
    into.addRiftDescriptor(this.riftDescriptor).flatMap(
      _.addUuid("commandId", what.commandId).flatMap(
        _.addString("commandType", what.commandType).flatMap(
          _.addOptionalComplexSelective("aggRef", AggregateRootRefDecomposer, what.aggRef))))
  }
}

object CommandInfoDecomposer extends Decomposer[CommandInfo] {
  val riftDescriptor = RiftDescriptor(classOf[CommandInfo])
  val alternativeRiftDescriptors = Nil
  def decompose[TDimension <: RiftDimension](what: CommandInfo)(into: Dematerializer[TDimension]): AlmValidation[Dematerializer[TDimension]] = {
    what match {
      case ci: FullComandInfo => into.includeDirect(ci, FullComandInfoDecomposer)
      case ci: HeadCommandInfo => into.includeDirect(ci, HeadCommandInfoDecomposer)
    }
  }
}

object FullComandInfoRecomposer extends Recomposer[FullComandInfo] {
  val riftDescriptor = RiftDescriptor(classOf[FullComandInfo])
  val alternativeRiftDescriptors = Nil
  def recompose(from: Rematerializer): AlmValidation[FullComandInfo] = {
    from.getComplexType[DomainCommand]("command").map(FullComandInfo.apply)
  }
}

object HeadCommandInfoRecomposer extends Recomposer[HeadCommandInfo] {
  val riftDescriptor = RiftDescriptor(classOf[HeadCommandInfo])
  val alternativeRiftDescriptors = Nil
  def recompose(from: Rematerializer): AlmValidation[HeadCommandInfo] = {
    val commandId = from.getUuid("commandId").toAgg
    val commandType = from.getString("commandType").toAgg
    val aggRef = from.tryGetComplexType[AggregateRootRef]("aggRef", AggregateRootRefRecomposer).toAgg
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