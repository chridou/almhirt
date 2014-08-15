package almhirt.aggregates

import almhirt.common._

trait UserEventHandler extends AggregateRootUpdater[User, UserEvent] with AggregateRootEventHandler[User, UserEvent] {
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