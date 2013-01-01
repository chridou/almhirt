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

class CommandEnvelopeDecomposer extends Decomposer[CommandEnvelope] {
  val typeDescriptor = TypeDescriptor(classOf[CommandEnvelope], 1)
  def decompose[TDimension <: RiftDimension](what: CommandEnvelope)(into: Dematerializer[TDimension]): AlmValidation[Dematerializer[TDimension]] = {
    into
      .addTypeDescriptor(this.typeDescriptor)
      .flatMap(_.addComplexType("command", what.command))
      .flatMap(_.addOptionalComplexType("ticket", what.ticket))
  }
}

class CommandEnvelopeRecomposer extends Recomposer[CommandEnvelope] {
  val typeDescriptor = TypeDescriptor(classOf[CommandEnvelope], 1)
  def recompose(from: Rematerializer): AlmValidation[CommandEnvelope] = {
    val command = from.getComplexType[DomainCommand]("command").toAgg
    val ticket = from.tryGetComplexType[TrackingTicket]("ticket").toAgg
    (command |@| ticket)(CommandEnvelope.apply)
  }
}