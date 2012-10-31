package almhirt.commanding

import almhirt.domain._

abstract class UnitOfWork[AR <: AggregateRoot[AR, TEvent], TEvent <: DomainEvent](implicit m: Manifest[AR]) extends HandlesCommand {
  val aggregateRootType = m.erasure.asInstanceOf[Class[AR]]
}

