package almhirt.ext.core.riftwarp.serialization

import scalaz._
import scalaz.Scalaz._
import scalaz.syntax.validation._
import almhirt.common._
import almhirt.almvalidation.kit._
import riftwarp._
import almhirt.commanding._
import almhirt.util._
import almhirt.commanding.CommandEnvelope

object CommandEnvelopeDecomposer extends Decomposer[CommandEnvelope] {
  val riftDescriptor = RiftDescriptor(classOf[CommandEnvelope])
  val alternativeRiftDescriptors = Nil
  def decompose[TDimension <: RiftDimension](what: CommandEnvelope, into: WarpSequencer[TDimension]): AlmValidation[WarpSequencer[TDimension]] = {
    into
      .addRiftDescriptor(this.riftDescriptor)
      .addComplex("command", what.command, None).flatMap(
        _.addOptionalComplex("ticket", what.ticket, None))
  }
}

object CommandEnvelopeRecomposer extends Recomposer[CommandEnvelope] {
  val riftDescriptor = RiftDescriptor(classOf[CommandEnvelope])
  val alternativeRiftDescriptors = Nil
  def recompose(from: Rematerializer): AlmValidation[CommandEnvelope] = {
    val command = from.getComplexType[DomainCommand]("command").toAgg
    val ticket = from.tryGetComplexType[TrackingTicket]("ticket").toAgg
    (command |@| ticket)(CommandEnvelope.apply)
  }
}