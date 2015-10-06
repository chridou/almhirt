package almhirt.components

import almhirt.common._
import almhirt.context.AlmhirtContext
import almhirt.akkax.ComponentFactory

sealed trait EventPublisherFactory {
  def create(implicit almhirtContext: AlmhirtContext): AlmValidation[ComponentFactory]
}

object EventPublisher {
  sealed trait EventPublisherMessage
  
  final case class FireEvent(event: Event) extends EventPublisherMessage
  
  final case class PublishEvent(event: Event) extends EventPublisherMessage
  sealed trait PublishEventRsp extends EventPublisherMessage
  final case class EventPublished(event: Event) extends PublishEventRsp
  final case class EventNotPublished(event: Event, cause: Problem) extends PublishEventRsp
}

