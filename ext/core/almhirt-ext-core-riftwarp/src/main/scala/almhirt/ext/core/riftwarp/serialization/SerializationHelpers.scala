package almhirt.ext.core.riftwarp.serialization

import almhirt.common._
import riftwarp._

trait SerializationFuns {
//  def serializeCommonDomainEventFields(event: DomainEvent, funnel: Dematerializer[_,_]): AlmValidation[Dematerializer[_,_]] =
//    funnel.addUuid("aggId", event.aggId).bind(f => f.addLong("aggVersion", event.aggVersion))
//
//  def serializeCommonMutatorCommandFields(com: MutatorCommandStyle, funnel: Dematerializer[_,_]): AlmValidation[Dematerializer[_,_]] =
//    funnel.addComplexType[AggregateRootRef]("target", com.target)
}

//trait DeserializationFuns {
//  def deserializeCommonDomainEventFields(remArray: RematerializationArray,  event: DomainEvent, funnel: DematerializationFunnel): AlmValidation[DematerializationFunnel] =
//    funnel.addUuid("id", event.id).bind(f => f.addLong("version", event.version))
//
//  def deserializeCommonMutatorCommandFields(com: MutatorCommandStyle, funnel: DematerializationFunnel): AlmValidation[DematerializationFunnel] =
//    funnel.addComplexType[AggregateRootRef]("target", com.target)
//}

object SerializationHelpers extends SerializationFuns