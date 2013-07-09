package almhirt

import almhirt.common.Event
import almhirt.common.Command
import almhirt.messaging.MessageStream
import almhirt.domain.DomainEvent

package object core {
  type EventStream = MessageStream[Event]
  type DomainEventStream = MessageStream[DomainEvent]
  type CommandStream = MessageStream[Command]
}