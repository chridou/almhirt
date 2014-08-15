package almhirt.aggregates

import org.joda.time.LocalDateTime

import almhirt.common._
import almhirt.almvalidation.kit._

case class User(id: AggregateRootId, version: AggregateRootVersion, surname: String, lastname: String, age: Option[Int]) extends AggregateRoot

sealed trait UserEvent extends AggregateEvent
case class UserCreated(header: EventHeader, aggId: AggregateRootId, aggVersion: AggregateRootVersion, surname: String, lastname: String) extends UserEvent
case class UserNotAccepted(header: EventHeader, aggId: AggregateRootId, aggVersion: AggregateRootVersion, surname: String, lastname: String) extends UserEvent
case class UserSurnameChanged(header: EventHeader, aggId: AggregateRootId, aggVersion: AggregateRootVersion, surname: String) extends UserEvent
case class UserLastnameChanged(header: EventHeader, aggId: AggregateRootId, aggVersion: AggregateRootVersion, lastname: String) extends UserEvent
case class UserAgeChanged(header: EventHeader, aggId: AggregateRootId, aggVersion: AggregateRootVersion, age: Int) extends UserEvent
case class UserLeft(header: EventHeader, aggId: AggregateRootId, aggVersion: AggregateRootVersion) extends UserEvent
case class UserDied(header: EventHeader, aggId: AggregateRootId, aggVersion: AggregateRootVersion) extends UserEvent

trait UserEventHandler extends AggregateRootEventHandler[User, UserEvent] {
  implicit def ccuad: CanCreateUuidsAndDateTimes
  override def applyEventAntemortem(state: Antemortem[User], event: UserEvent): Postnatalis[User] =
    (state, event) match {
      case (NeverExisted, UserCreated(_, id, _, surname, lastname)) =>
        Alive(User(id, AggregateRootVersion(1), surname, lastname, None))
      case (NeverExisted, UserNotAccepted(_, id, _, surname, lastname)) =>
        Dead(id, AggregateRootVersion(1))
      case (Alive(user), UserSurnameChanged(_, _, _, surname)) =>
        Alive(user.copy(surname = surname, version = user.version.inc))
      case (Alive(user), UserLastnameChanged(_, _, _, lastname)) =>
        Alive(user.copy(lastname = lastname, version = user.version.inc))
      case (Alive(user), UserAgeChanged(_, _, _, age)) =>
        Alive(user.copy(age = Some(age), version = user.version.inc))
      case (Alive(user), UserLeft(_, _, _)) =>
        Dead(user.id, user.version.inc)
      case (Alive(user), UserDied(_, _, _)) =>
        Dead(user.id, user.version.inc)
      case (_, AggregateEvent(_, id, version)) =>
        throw new UnhandledAggregateEventException(id, event)
    }
}

trait MutatesUser { self: UserEventHandler =>
  implicit def ccuad: CanCreateUuidsAndDateTimes
  private type UR = UpdateRecorder[User, UserEvent]
  def create(aggId: AggregateRootId, surname: String, lastname: String): UR = {
    val res =
      for {
        sname <- surname.notEmptyOrWhitespace
        lname <- lastname.notEmptyOrWhitespace
      } yield (
        User(aggId, AggregateRootVersion(1), sname, lname, None),
        UserCreated(EventHeader(), aggId, AggregateRootVersion(0), sname, lname))
    res.record
  }

  def doNotAccept(aggId: AggregateRootId, surname: String, lastname: String): UR = {
    val res =
      for {
        sname <- surname.notEmptyOrWhitespace
        lname <- lastname.notEmptyOrWhitespace
      } yield (
        Dead(aggId, AggregateRootVersion(1)),
        UserNotAccepted(EventHeader(), aggId, AggregateRootVersion(0), sname, lname))
    res.record
  }

  def changeSurname(user: User, surname: String): UR = {
    surname.notEmptyOrWhitespace
      .map(sname => UserSurnameChanged(EventHeader(), user.id, user.version, sname))
      .map(event => (applyEvent(user, event), event))
      .record
  }

  def changeLastname(user: User, lastname: String): UR = {
    lastname.notEmptyOrWhitespace
      .map(lname => UserLastnameChanged(EventHeader(), user.id, user.version, lname))
      .map(event => (applyEvent(user, event), event))
      .record
  }

  def changeFullname(user: User, surname: String, lastname: String): UR = {
    changeSurname(user, surname).flatMap(UpdateRecorder.ifAlive(changeLastname(_, lastname)))
  }
}