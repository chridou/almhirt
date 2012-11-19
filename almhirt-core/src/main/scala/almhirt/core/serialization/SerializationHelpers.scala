package almhirt.core.serialization

import almhirt.common._
import almhirt.riftwarp._
import almhirt.domain.DomainEvent
import almhirt.commanding.MutatorCommandStyle
import almhirt.commanding.AggregateRootRef

trait SerializationFuns {
  def serializeCommonDomainEventFields(event: DomainEvent, funnel: DematerializationFunnel): AlmValidation[DematerializationFunnel] =
    funnel.addUuid("id", event.id).bind(f => f.addLong("version", event.version))

  def serializeCommonMutatorCommandFields(com: MutatorCommandStyle, funnel: DematerializationFunnel): AlmValidation[DematerializationFunnel] =
    funnel.addComplexType[AggregateRootRef]("target", com.target)
}

//trait DeserializationFuns {
//  def deserializeCommonDomainEventFields(remArray: RematerializationArray,  event: DomainEvent, funnel: DematerializationFunnel): AlmValidation[DematerializationFunnel] =
//    funnel.addUuid("id", event.id).bind(f => f.addLong("version", event.version))
//
//  def deserializeCommonMutatorCommandFields(com: MutatorCommandStyle, funnel: DematerializationFunnel): AlmValidation[DematerializationFunnel] =
//    funnel.addComplexType[AggregateRootRef]("target", com.target)
//}

object SerializationHelpers extends SerializationFuns