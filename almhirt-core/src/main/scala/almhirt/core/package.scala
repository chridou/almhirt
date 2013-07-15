package almhirt

import almhirt.common.Event
import almhirt.common.Command
import almhirt.messaging.MessageChannel
import almhirt.domain.DomainEvent

package object core {
  type EventChannel = MessageChannel[Event]
  type DomainEventChannel = MessageChannel[DomainEvent]
  type CommandChannel = MessageChannel[Command]
}