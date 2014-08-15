package almhirt.aggregates

import almhirt.common._

case class User(id: AggregateRootId, version: AggregateRootVersion, surname: String, lastname: String, age: Option[Int]) extends AggregateRoot

sealed trait UserEvent extends AggregateEvent

case class UserCreated(header: EventHeader, aggId: AggregateRootId, aggVersion: AggregateRootVersion, surname: String, lastname: String) extends UserEvent
case class UserNotAccepted(header: EventHeader, aggId: AggregateRootId, aggVersion: AggregateRootVersion, surname: String, lastname: String) extends UserEvent
case class UserSurnameChanged(header: EventHeader, aggId: AggregateRootId, aggVersion: AggregateRootVersion, surname: String) extends UserEvent
case class UserLastnameChanged(header: EventHeader, aggId: AggregateRootId, aggVersion: AggregateRootVersion, lastname: String) extends UserEvent
case class UserAgeChanged(header: EventHeader, aggId: AggregateRootId, aggVersion: AggregateRootVersion, age: Int) extends UserEvent
case class UserLeft(header: EventHeader, aggId: AggregateRootId, aggVersion: AggregateRootVersion) extends UserEvent
case class UserDied(header: EventHeader, aggId: AggregateRootId, aggVersion: AggregateRootVersion) extends UserEvent


