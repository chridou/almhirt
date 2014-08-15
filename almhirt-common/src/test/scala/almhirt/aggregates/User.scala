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

trait MutatesUser { self: UserEventHandler with AggregateRootUpdater[User, UserEvent] =>
  implicit def ccuad: CanCreateUuidsAndDateTimes
  
  def create(aggId: AggregateRootId, surname: String, lastname: String): UpdateRecorder[User, UserEvent] = {
    val res =
      for {
        sname <- surname.notEmptyOrWhitespace
        lname <- lastname.notEmptyOrWhitespace
      } yield (
        User(aggId, AggregateRootVersion(1), sname, lname, None),
        UserCreated(EventHeader(), aggId, AggregateRootVersion(0), sname, lname))
    res.record
  }

  // Old school
  def doNotAccept(aggId: AggregateRootId, surname: String, lastname: String): UpdateRecorder[User, UserEvent] = {
    val res =
      for {
        sname <- surname.notEmptyOrWhitespace
        lname <- lastname.notEmptyOrWhitespace
      } yield (
        Dead(aggId, AggregateRootVersion(1)),
        UserNotAccepted(EventHeader(), aggId, AggregateRootVersion(0), sname, lname))
    res.record
  }

  // Change via the event handler
  def changeLastname(user: User, lastname: String): UpdateRecorder[User, UserEvent] = {
    lastname.notEmptyOrWhitespace
      .map(lname => UserLastnameChanged(EventHeader(), user.id, user.version, lname))
      .map(event => (applyEvent(user, event), event))
      .record
  }

  // Use the update function(from trait AggregateRootUpdater) which uses the handler
  def changeSurname(user: User, surname: String): UpdateRecorder[User, UserEvent] = {
    surname.notEmptyOrWhitespace
      .map(sname => update(user, UserSurnameChanged(EventHeader(), user.id, user.version, sname)))
      .record
  }
  
  // Update via the extension method imported by trait AggregateRootUpdater which uses the handler
  def changeAge(user: User, age: Int): UpdateRecorder[User, UserEvent] = {
    almhirt.almvalidation.funs.numericConstrainedToMin(age, 18)
      .map(age => user.update(UserAgeChanged(EventHeader(), user.id, user.version, age)))
      .record
  }
  
  // Composition using ifAlive from UpdateRecorder to meet the signature of UpdateRecorder.flatMap
  def changeFullName(user: User, surname: String, lastname: String): UpdateRecorder[User, UserEvent] = {
    for {
      a <- changeSurname(user, surname)
      b <- UpdateRecorder.ifAlive(changeLastname(_: User, lastname))(a)
    } yield b
  }

  // Composition with liftWith(like UpdateRecorder.isAlive) from trait AggregateRootUpdater
  def changeFullNameAndAge(user: User, surname: String, lastname: String, age: Int): UpdateRecorder[User, UserEvent] = {
    import updaterimplicits._
    for {
      a <- changeFullName(user, surname, lastname)
      b <- (changeAge(_ : User, 18)).liftWith(a)
    } yield b
  }
  
  // classic
  def leave(user: User): UpdateRecorder[User, UserEvent] = {
    UpdateRecorder.accept(user.update(UserLeft(EventHeader(), user.id, user.version)))
  }

  // use the implicit extension "accept" on the aggragate provided via trait AggregateRootUpdater
  def die(user: User): UpdateRecorder[User, UserEvent] = {
    user accept UserDied(EventHeader(), user.id, user.version)
  }

  
}