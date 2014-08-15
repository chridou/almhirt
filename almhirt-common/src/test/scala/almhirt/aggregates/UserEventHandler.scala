package almhirt.aggregates

import almhirt.common._

trait UserEventHandler extends AggregateRootUpdater[User, UserEvent] with AggregateRootEventHandler[User, UserEvent] {
  implicit def ccuad: CanCreateUuidsAndDateTimes
  override def applyEventAntemortem(state: Antemortem[User], event: UserEvent): Postnatalis[User] =
    (state, event) match {
      case (Vacat, UserCreated(_, id, _, surname, lastname)) =>
        Vivus(User(id, AggregateRootVersion(1), surname, lastname, None))
      case (Vacat, UserNotAccepted(_, id, _, surname, lastname)) =>
        Mortuus(id, AggregateRootVersion(1))
      case (Vivus(user), UserSurnameChanged(_, _, _, surname)) =>
        Vivus(user.copy(surname = surname, version = user.version.inc))
      case (Vivus(user), UserLastnameChanged(_, _, _, lastname)) =>
        Vivus(user.copy(lastname = lastname, version = user.version.inc))
      case (Vivus(user), UserAgeChanged(_, _, _, age)) =>
        Vivus(user.copy(age = Some(age), version = user.version.inc))
      case (Vivus(user), UserLeft(_, _, _)) =>
        Mortuus(user.id, user.version.inc)
      case (Vivus(user), UserDied(_, _, _)) =>
        Mortuus(user.id, user.version.inc)
      case (_, AggregateEvent(_, id, version)) =>
        throw new UnhandledAggregateEventException(id, event)
    }
}