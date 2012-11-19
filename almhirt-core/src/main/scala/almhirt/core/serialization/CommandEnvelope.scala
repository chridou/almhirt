package almhirt.core.serialization

import scalaz._, Scalaz._
import scalaz.syntax.validation._
import almhirt.common._
import almhirt.almvalidation.kit._
import almhirt.riftwarp._
import almhirt.commanding._
import almhirt.util._

class CommandEnvelopeDecomposer extends Decomposer[CommandEnvelope] {
  val typeDescriptor = TypeDescriptor(classOf[CommandEnvelope], 1)
  def decompose(envelope: CommandEnvelope)(implicit into: DematerializationFunnel): AlmValidation[DematerializationFunnel] = {
    into
      .addTypeDescriptor(this.typeDescriptor)
      .bind(_.addComplexType("command", envelope.command))
      .bind(_.addOptionalComplexType("ticket", envelope.ticket))
  }
}

class CommandEnvelopeRecomposer extends Recomposer[CommandEnvelope] {
  val typeDescriptor = TypeDescriptor(classOf[CommandEnvelope], 1)
  def recompose(from: RematerializationArray): AlmValidation[CommandEnvelope] = {
    val command = from.getComplexType[DomainCommand]("command").toAgg
    val ticket = from.tryGetComplexType[TrackingTicket]("ticket").toAgg
    (command |@| ticket)(CommandEnvelope.apply)
  }
}