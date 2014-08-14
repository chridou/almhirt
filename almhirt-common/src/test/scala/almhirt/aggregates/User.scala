package almhirt.aggregates

import almhirt.common._
import org.joda.time.LocalDateTime

case class User(id: AggregateRootId, version: AggregateRootVersion, surname: String, lastname: String, age: Option[Int]) extends AggregateRoot

sealed trait UserEvent extends AggregateEvent
case class UserCreated(header: EventHeader, aggId: AggregateRootId, aggVersion: AggregateRootVersion, surname: String, lastname: String) extends UserEvent
case class UserSurnameChanged(header: EventHeader, aggId: AggregateRootId, aggVersion: AggregateRootVersion, surname: String) extends UserEvent
case class UserLastnameChanged(header: EventHeader, aggId: AggregateRootId, aggVersion: AggregateRootVersion, lastname: String) extends UserEvent
case class UserAgeChanged(header: EventHeader, aggId: AggregateRootId, aggVersion: AggregateRootVersion, age: Int) extends UserEvent
case class UserLeft(header: EventHeader, aggId: AggregateRootId, aggVersion: AggregateRootVersion) extends UserEvent
case class UserDied(header: EventHeader, aggId: AggregateRootId, aggVersion: AggregateRootVersion) extends UserEvent

trait BuildsUser extends BuildsAggregetaRootAntemortem[User, UserEvent] {
  implicit def ccuad: CanCreateUuidsAndDateTimes
  override def applyEventAntemortem(state: Antemortem[User], event: UserEvent): Postnatalis[User] =
    (state, event) match {
      case (NeverExisted, UserCreated(_, id, v, surname, lastname)) =>
        Alive(User(id, v, surname, lastname, None))
      case (Alive(user), UserSurnameChanged(_, id, v, surname)) =>
        Alive(user.copy(surname = surname, version = v))
      case (Alive(user), UserLastnameChanged(_, id, v, lastname)) =>
        Alive(user.copy(lastname = lastname, version = v))
      case (Alive(user), UserAgeChanged(_, id, v, age)) =>
        Alive(user.copy(age = Some(age), version = v))
      case (Alive(user), UserLeft(_, id, v)) =>
        Dead(id, v)
      case (Alive(user), UserDied(_, id, v)) =>
        Dead(id, v)
      case (_, AggregateEvent(_, id, version)) =>
        throw new UnhandledAggregateEventException(id, event)
    }
}