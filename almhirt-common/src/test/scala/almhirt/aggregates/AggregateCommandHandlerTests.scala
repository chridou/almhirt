package almhirt.aggregates

import scala.language.postfixOps

import org.scalatest._

import scala.concurrent.ExecutionContext
import scala.concurrent.duration._
import org.joda.time.{ DateTime, LocalDateTime, DateTimeZone }
import scalaz._, Scalaz._
import almhirt.common._
import almhirt.almvalidation.kit._
import scala.concurrent.duration.FiniteDuration

class AggregateCommandHandlerTests extends FlatSpec with Matchers with UserCommandHandler with UserUpdater with UserEventHandler {
  import aggregatesforthelazyones._

  implicit override val futuresContext: ExecutionContext = scala.concurrent.ExecutionContext.Implicits.global
  implicit val cv = AggregateRootCommandValidator.Validated
  
  implicit val ccuad = {
    val dt = new LocalDateTime(0L)
    new CanCreateUuidsAndDateTimes {
      override def getUuid(): java.util.UUID = ???
      override def getUniqueString(): String = "unique"
      override def getDateTime(): DateTime = ???
      override def getUtcTimestamp(): LocalDateTime = dt
    }
  }

  val defaultAwait: FiniteDuration = 3000 millis

  behavior of "AggregateCommandHandler"

  it should "create an aggregate root" in {
    val (state, events) = handleAggregateCommand(CreateUser(CommandHeader(), "a", 0L, "hans", "meier"), Vacat).await(defaultAwait).forceResult

    state should equal(Vivus(User("a", 1, "hans", "meier", None)))
    events should equal(List(UserCreated(EventHeader(), "a", 0, "hans", "meier")))
  }

  it should "modify an aggregate root" in {
    val initial = Vivus(User("a", 1, "hans", "meier", None))

    val (state, events) = handleAggregateCommand(ChangeUserSurname(CommandHeader(), "a", 1L, "peter"), initial).await(defaultAwait).forceResult

    state should equal(Vivus(User("a", 2, "peter", "meier", None)))
    events should equal(List(UserSurnameChanged(EventHeader(), "a", 1, "peter")))
  }

  it should "delete an aggregate root" in {
    val initial = Vivus(User("a", 1, "hans", "meier", None))

    val (state, events) = handleAggregateCommand(ConfirmUserCancelled(CommandHeader(), "a", 1L), initial).await(defaultAwait).forceResult

    state should equal(Mortuus("a", 2L))
    events should equal(List(UserLeft(EventHeader(), "a", 1)))
  }

  it should "not handle a command with a version other than 0 when the aggregate root does not exist." in {
    val res = handleAggregateCommand(CreateUser(CommandHeader(), "a", 1L, "hans", "meier"), Vacat).await(defaultAwait)

    res.isFailure should equal(true)
  }

  it should "not handle a command with a version other than the aggragate root's when the aggregate root does exist." in {
    val initial = Vivus(User("a", 1, "hans", "meier", None))

    val res = handleAggregateCommand(ChangeUserSurname(CommandHeader(), "a", 2L, "peter"), initial).await(defaultAwait)

    res.isFailure should equal(true)
  }

  it should "not handle a command with an id other than the aggragate root's when the aggregate root does exist." in {
    val initial = Vivus(User("a", 1, "hans", "meier", None))

    val res = handleAggregateCommand(ChangeUserSurname(CommandHeader(), "b", 1L, "peter"), initial).await(defaultAwait)

    res.isFailure should equal(true)
  }

  it should "not handle a command when the aggragate root is already dead." in {
    val initial = Mortuus("a", 13)

    val res = handleAggregateCommand(ChangeUserSurname(CommandHeader(), "a", 13L, "peter"), initial).await(defaultAwait)

    res.isFailure should equal(true)
  }

  it should "create and modify an aggregate root" in {
    val (state, events) =
      handleAggregateCommand(CreateUser(CommandHeader(), "a", 0L, "hans", "meier"), Vacat)
        .andThen(ChangeUserAge(CommandHeader(), "a", 1L, 80))
        .await(defaultAwait).forceResult

    state should equal(Vivus(User("a", 2, "hans", "meier", Some(80))))
    events should equal(List(
      UserCreated(EventHeader(), "a", 0, "hans", "meier"),
      UserAgeChanged(EventHeader(), "a", 1, 80)))
  }

  it should "create, modify and delete an aggregate root" in {
    val (state, events) =
      handleAggregateCommand(CreateUser(CommandHeader(), "a", 0L, "hans", "meier"), Vacat)
        .andThen(ChangeUserAge(CommandHeader(), "a", 1L, 80))
        .andThen(ConfirmUserDeath(CommandHeader(), "a", 2L))
        .await(defaultAwait).forceResult

    state should equal(Mortuus("a", 3))
    events should equal(List(
      UserCreated(EventHeader(), "a", 0, "hans", "meier"),
      UserAgeChanged(EventHeader(), "a", 1, 80),
      UserDied(EventHeader(), "a", 2)))
  }

  it should "execute a command with an 'expensive' service call" in {
    val initial = Vivus(User("a", 1, "hans", "meier", None))

    val (state, events) = handleAggregateCommand(ChangeUserAgeForCreditCard(CommandHeader(), "a", 1L, 22), initial).await(defaultAwait).forceResult

    state should equal(Vivus(User("a", 2, "hans", "meier", Some(22))))
    events should equal(List(UserAgeChanged(EventHeader(), "a", 1, 22)))
  }

  it should "fail on an invalid command" in {
    val initial = Vivus(User("a", 1, "hans", "meier", None))

    val res = handleAggregateCommand(ChangeUserAgeForCreditCard(CommandHeader(), "a", 1L, 16), initial).await(defaultAwait)

    res.isFailure should equal(true)
  }

  it should "fail on an invalid command between valid commands" in {
    val res =
      handleAggregateCommand(CreateUser(CommandHeader(), "a", 0L, "hans", "meier"), Vacat)
        .andThen(ChangeUserSurname(CommandHeader(), "a", 1L, "     "))
        .andThen(ConfirmUserDeath(CommandHeader(), "a", 2L))
        .await(defaultAwait)

    res.isFailure should equal(true)
  }

  it should "execute a recursive command" in {
    // Look at how there is a gap between versions 4 and 6.
    // This shows why it is bad practice to pack commands like a matroska as 
    // you must know how many events are emitted for each single command.
    val cmd =
      UserUow(CommandHeader(), "a", 0L, List(
        CreateUser(CommandHeader(), "a", 0L, "hans", "meier"),
        UserUow(CommandHeader(), "a", 1L, List()),
        ChangeUserLastname(CommandHeader(), "a", 1L, "müller"),
        UserUow(CommandHeader(), "a", 2L, List(
          ChangeUserAgeForCreditCard(CommandHeader(), "a", 2L, 22),
          ChangeUserSurname(CommandHeader(), "a", 3L, "willi"))),
          ChangeUserFullName(CommandHeader(), "a", 4L, "peter", "pan"),
        ConfirmUserDeath(CommandHeader(), "a", 6L)))

    val (state, events) = handleAggregateCommand(cmd, Vacat)
      .await(defaultAwait)
      .forceResult

    state should equal(Mortuus("a", 7))
    events should equal(List(
      UserCreated(EventHeader(), "a", 0, "hans", "meier"),
      UserLastnameChanged(EventHeader(), "a", 1, "müller"),
      UserAgeChanged(EventHeader(), "a", 2, 22),
      UserSurnameChanged(EventHeader(), "a", 3, "willi"),
      UserSurnameChanged(EventHeader(), "a", 4, "peter"),
      UserLastnameChanged(EventHeader(), "a", 5, "pan"),
      UserDied(EventHeader(), "a", 6)))
  }
}