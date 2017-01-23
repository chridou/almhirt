package almhirt.aggregates

import org.scalatest._

import _root_.java.time.{ ZonedDateTime, LocalDateTime }
import scalaz._, Scalaz._
import almhirt.common._
import almhirt.almvalidation.kit._

class UpdateRecorderWithUpdaterTests extends FlatSpec with Matchers with Inside
  with UserEventHandler with UserUpdater with RebuildsAggregateRootFromTimeline[User, UserEvent] {
  import aggregatesforthelazyones._

  implicit val ccuad = {
    val dt = LocalDateTime.of(0: Int, 0: Int, 0: Int, 0: Int, 0: Int)
    new CanCreateUuidsAndDateTimes {
      override def getUuid(): java.util.UUID = ???
      override def getUniqueString(): String = "unique"
      override def getDateTime(): ZonedDateTime = ???
      override def getUtcTimestamp(): LocalDateTime = dt
    }
  }

  behavior of "UpdateRecorder used with an AggregateRootUpdater"

  it should "create an aggregate root" in {
    val (ar, events) = create("a", "hans", "meier").recordings.forceResult

    inside(ar) {
      case Vivus(User(id, version, surname, lastname, age)) ⇒
        id should equal(arid("a"))
        version should equal(arv(1))
        surname should equal("hans")
        lastname should equal("meier")
        age should equal(None)
    }

    events should equal(List(
      UserCreated(EventHeader(), arid("a"), arv(0L), "hans", "meier")))
  }

  it should "create an aggregate root and modify it" in {
    val (ar, events) =
      (for {
        a ← create("a", "hans", "meier")
        b ← (changeAge(_: User, 18)).liftWith(a)
      } yield b).recordings.forceResult

    inside(ar) {
      case Vivus(User(id, version, surname, lastname, age)) ⇒
        id should equal(arid("a"))
        version should equal(arv(2))
        surname should equal("hans")
        lastname should equal("meier")
        age should equal(Some(18))
    }

    val expectedEvents = List(UserCreated(EventHeader(), "a", 0L, "hans", "meier"), UserAgeChanged(EventHeader(), "a", 1L, 18))
    events should equal(expectedEvents)
  }

  it should "create an aggregate root, modify and then kill it" in {
    val (ar, events) =
      (for {
        a ← create("a", "hans", "meier")
        b ← (changeAge(_: User, 18)).liftWith(a)
        c ← (die(_: User)).liftWith(b)
      } yield c).recordings.forceResult

    inside(ar) {
      case Mortuus(id, version) ⇒
        id should equal(arid("a"))
        version should equal(arv(3))
    }

    val expectedEvents = List(
      UserCreated(EventHeader(), "a", 0L, "hans", "meier"),
      UserAgeChanged(EventHeader(), "a", 1L, 18),
      UserDied(EventHeader(), "a", 2L))
    events should equal(expectedEvents)
  }

  it should "create an aggregate root, modify it many times and then kill it" in {
    val (ar, events) =
      (for {
        a ← create("a", "hans", "meier")
        b ← (changeAge(_: User, 18)).liftWith(a)
        c ← (changeFullName(_: User, "Hans", "Meier")).liftWith(b)
        d ← (leave(_: User)).liftWith(c)
      } yield d).recordings.forceResult

    inside(ar) {
      case Mortuus(id, version) ⇒
        id should equal(arid("a"))
        version should equal(arv(5))
    }

    val expectedEvents = List(
      UserCreated(EventHeader(), "a", 0L, "hans", "meier"),
      UserAgeChanged(EventHeader(), "a", 1L, 18),
      UserSurnameChanged(EventHeader(), "a", 2L, "Hans"),
      UserLastnameChanged(EventHeader(), "a", 3L, "Meier"),
      UserLeft(EventHeader(), "a", 4L))
    events should equal(expectedEvents)
  }

  it should "allow a lifecycle transition from Vacat directly to Mortuus" in {
    val (ar, events) = doNotAccept("a", "hans", "meier").recordings.forceResult

    inside(ar) {
      case Mortuus(id, version) ⇒
        id should equal(arid("a"))
        version should equal(arv(1))
    }

    events should equal(List(UserNotAccepted(EventHeader(), arid("a"), arv(0L), "hans", "meier")))
  }

  it should "create a timeline that leads to the same result" in {
    val (ar, timeline) =
      (for {
        a ← create("a", "hans", "meier")
        b ← (changeAge(_: User, 18)).liftWith(a)
        c ← (changeFullName(_: User, "Hans", "Meier")).liftWith(b)
      } yield c).recordings.forceResult

    ar should equal(rebuildFromTimeline(timeline))
  }

  it should "not allow modification after death(Mortuus)" in {
    val ur =
      (for {
        a ← create("a", "hans", "meier")
        b ← (die(_: User)).liftWith(a)
        c ← (changeAge(_: User, 18)).liftWith(b)
      } yield c)

    ur.isRejected should equal(true)
  }

  it should "return the events until the invalid operation happened(excluding the invalid ones)" in {
    val ur =
      (for {
        a ← create("a", "hans", "meier")
        b ← (die(_: User)).liftWith(a)
        c ← (changeAge(_: User, 18)).liftWith(b)
      } yield c)

    val expectedEvents = List(
      UserCreated(EventHeader(), "a", 0L, "hans", "meier"),
      UserDied(EventHeader(), "a", 1L))
    ur.events should equal(expectedEvents)
  }

  it should "return events up to an invalid operation that form a valid timeline up to the last state before the invalid operation" in {
    val timeline =
      (for {
        a ← create("a", "hans", "meier")
        a ← (changeAge(_: User, 18)).liftWith(a)
        a ← (changeSurname(_: User, "      ")).liftWith(a)
        a ← (changeFullName(_: User, "Hans", "Meier")).liftWith(a)
      } yield a).events

    inside(rebuildFromTimeline(timeline)) {
      case Vivus(User(id, version, surname, lastname, age)) ⇒
        id should equal(arid("a"))
        version should equal(arv(2))
        surname should equal("hans")
        lastname should equal("meier")
        age should equal(Some(18))
    }
  }

  it should "ALLOW recreation(Vacat→Vivus→Mortuus→Vivus) even though the timeline becomes invalid" in {
    val (ar, events) =
      (for {
        a ← create("a", "hans", "meier")
        b ← (die(_: User)).liftWith(a)
        c ← create("a", "hans", "meier") // See, that there is no b here?
      } yield c).recordings.forceResult

    inside(ar) {
      case Vivus(User(id, version, surname, lastname, age)) ⇒
        id should equal(arid("a"))
        version should equal(arv(1))
        surname should equal("hans")
        lastname should equal("meier")
        age should equal(None)
    }

    val expectedEvents = List(
      UserCreated(EventHeader(), "a", 0L, "hans", "meier"),
      UserDied(EventHeader(), "a", 1L),
      UserCreated(EventHeader(), "a", 0L, "hans", "meier"))
    events should equal(expectedEvents)
  }

  it should "create an invalid timeline when the aggregate root is recreated(Vacat→Vivus→Mortuus→Vivus)" in {
    val (ar, timeline) =
      (for {
        a ← create("a", "hans", "meier")
        b ← (die(_: User)).liftWith(a)
        c ← create("a", "hans", "meier")
      } yield c).recordings.forceResult

    intercept[Exception] {
      rebuildFromTimeline(timeline)
    }
  }
}
