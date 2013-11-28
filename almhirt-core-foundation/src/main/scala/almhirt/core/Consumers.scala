package almhirt.core

import almhirt.common.Consumer
import almhirt.core.types._

trait DomainEventConsumer extends Consumer[DomainEvent]
