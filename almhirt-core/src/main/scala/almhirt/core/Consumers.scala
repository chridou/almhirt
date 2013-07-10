package almhirt.core

import almhirt.common.Consumer
import almhirt.domain.DomainEvent

trait DomainEventConsumer extends Consumer[DomainEvent]
