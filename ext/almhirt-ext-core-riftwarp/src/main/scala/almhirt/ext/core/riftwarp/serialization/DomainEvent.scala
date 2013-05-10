package almhirt.ext.core.riftwarp.serialization

import scalaz._, Scalaz._
import almhirt.common._
import almhirt.almvalidation.kit._
import almhirt.domain._
import riftwarp._

trait DomainEventDecomposer[TEvent <: DomainEvent] extends Decomposer[TEvent] {
  override def decompose[TDimension <: RiftDimension](what: TEvent, into: WarpSequencer[TDimension]): AlmValidation[WarpSequencer[TDimension]] = {
    into
      .addRiftDescriptor(this.riftDescriptor)
      .addWith("header", what.header, DomainEventHeaderDecomposer).flatMap(addEventParams(what, _))
  }

  def addEventParams[TDimension <: RiftDimension](what: TEvent, into: WarpSequencer[TDimension]): AlmValidation[WarpSequencer[TDimension]]
}

trait DomainEventRecomposer[TEvent <: DomainEvent] extends Recomposer[TEvent] {
  def recompose(from: Extractor): AlmValidation[TEvent] = 
    from.getWith("header", DomainEventHeaderRecomposer.recompose).flatMap(header =>
      extractEventParams(from, header))
  
  def extractEventParams(from: Extractor, header: DomainEventHeader): AlmValidation[TEvent]
}