package almhirt.aggregates

import almhirt.common._

case class User(id: AggregateRootId, version: AggregateRootVersion, surname: String, lastname: String, age: Option[Int]) extends AggregateRoot

sealed trait UserEvent extends AggregateRootEvent

case class UserCreated(header: EventHeader, aggId: AggregateRootId, aggVersion: AggregateRootVersion, surname: String, lastname: String) extends UserEvent
case class UserNotAccepted(header: EventHeader, aggId: AggregateRootId, aggVersion: AggregateRootVersion, surname: String, lastname: String) extends UserEvent
case class UserSurnameChanged(header: EventHeader, aggId: AggregateRootId, aggVersion: AggregateRootVersion, surname: String) extends UserEvent
case class UserLastnameChanged(header: EventHeader, aggId: AggregateRootId, aggVersion: AggregateRootVersion, lastname: String) extends UserEvent
case class UserAgeChanged(header: EventHeader, aggId: AggregateRootId, aggVersion: AggregateRootVersion, age: Int) extends UserEvent
case class UserLeft(header: EventHeader, aggId: AggregateRootId, aggVersion: AggregateRootVersion) extends UserEvent
case class UserDied(header: EventHeader, aggId: AggregateRootId, aggVersion: AggregateRootVersion) extends UserEvent
case class UserLetItCrash(header: EventHeader, aggId: AggregateRootId, aggVersion: AggregateRootVersion) extends UserEvent

sealed trait UserCommand extends AggregateCommand
case class CreateUser(header: CommandHeader, aggId: AggregateRootId, aggVersion: AggregateRootVersion, surname: String, lastname: String) extends UserCommand
case class RejectUser(header: CommandHeader, aggId: AggregateRootId, aggVersion: AggregateRootVersion, surname: String, lastname: String) extends UserCommand
case class ChangeUserSurname(header: CommandHeader, aggId: AggregateRootId, aggVersion: AggregateRootVersion, surname: String) extends UserCommand
case class ChangeUserLastname(header: CommandHeader, aggId: AggregateRootId, aggVersion: AggregateRootVersion, lastname: String) extends UserCommand
case class ChangeUserFullName(header: CommandHeader, aggId: AggregateRootId, aggVersion: AggregateRootVersion, surname: String, lastname: String) extends UserCommand
case class ChangeUserAge(header: CommandHeader, aggId: AggregateRootId, aggVersion: AggregateRootVersion, age: Int) extends UserCommand
case class ChangeUserAgeForCreditCard(header: CommandHeader, aggId: AggregateRootId, aggVersion: AggregateRootVersion, age: Int) extends UserCommand
case class ConfirmUserCancelled(header: CommandHeader, aggId: AggregateRootId, aggVersion: AggregateRootVersion) extends UserCommand
case class ConfirmUserDeath(header: CommandHeader, aggId: AggregateRootId, aggVersion: AggregateRootVersion) extends UserCommand
case class UserUow(header: CommandHeader, aggId: AggregateRootId, aggVersion: AggregateRootVersion, commands: Seq[UserCommand]) extends UserCommand
case class DoSomethingSilly(header: CommandHeader, aggId: AggregateRootId, aggVersion: AggregateRootVersion, duration: scala.concurrent.duration.FiniteDuration) extends UserCommand



