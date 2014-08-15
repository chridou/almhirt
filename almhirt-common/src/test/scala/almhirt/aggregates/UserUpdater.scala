package almhirt.aggregates

import almhirt.common._
import almhirt.almvalidation.kit._

/** Examples on how to update the state of an AR and collect the associated events. 
 *  
 *  Some of the methods are arguable since they could/should be modeled with an own Event.
 *  Like appendSurname. In this example, it is just a name changed but one might be
 *  interested in how many users really forgot to provide their second surname upon
 *  registration....
 *  
 */
trait UserUpdater { self: UserEventHandler with AggregateRootUpdater[User, UserEvent] =>
  implicit def ccuad: CanCreateUuidsAndDateTimes

  // Old school, everything manually
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

  // Update via recordCreate imported by trait AggregateRootUpdater which uses the handler
  def doNotAccept(aggId: AggregateRootId, surname: String, lastname: String): UpdateRecorder[User, UserEvent] = {
    recordCreate {
      for {
        sname <- surname.notEmptyOrWhitespace
        lname <- lastname.notEmptyOrWhitespace
      } yield UserNotAccepted(EventHeader(), aggId, AggregateRootVersion(0), sname, lname)
    }
  }

  // Update via recordUpdate imported by trait AggregateRootUpdater which uses the handler
  def changeLastname(user: User, lastname: String): UpdateRecorder[User, UserEvent] = {
    user recordUpdate {
      for {
        lname <- lastname.notEmptyOrWhitespace
      } yield UserLastnameChanged(EventHeader(), user.id, user.version, lname)
    }
  }

  // Change via the event handler
  def appendSurname(user: User, anotherSurname: String): UpdateRecorder[User, UserEvent] = {
    anotherSurname.notEmptyOrWhitespace
      .map(sname => UserSurnameChanged(EventHeader(), user.id, user.version, s"${user.surname} $sname"))
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

  // Composition using ifVivus from UpdateRecorder to meet the signature of UpdateRecorder.flatMap
  def changeFullName(user: User, surname: String, lastname: String): UpdateRecorder[User, UserEvent] = {
    for {
      a <- changeSurname(user, surname)
      b <- UpdateRecorder.ifVivus(changeLastname(_: User, lastname))(a)
    } yield b
  }

  // Composition with liftWith(like UpdateRecorder.isVivus) from trait AggregateRootUpdater
  def changeFullNameAndAge(user: User, surname: String, lastname: String, age: Int): UpdateRecorder[User, UserEvent] = {
    import updaterimplicits._
    for {
      a <- changeFullName(user, surname, lastname)
      b <- (changeAge(_: User, 18)).liftWith(a)
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