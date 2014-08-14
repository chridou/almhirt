package almhirt.aggregates

import org.scalatest._

import scalaz._, Scalaz._
import almhirt.common._

class BuildsAggregateRootTests extends FlatSpec with Matchers {
  import aggregatesforthelazyones._

  private object BuildsUserInstance extends BuildsUser {
    override implicit lazy val ccuad = CanCreateUuidsAndDateTimes()
  }

  import BuildsUserInstance._

  behavior of "BuildsAggregateRoot"

  it should "create an aggregate root" in {
    val event = UserCreated(EventHeader(), arid("a"), arv(0L), "hans", "meier")
    val actual = fromEvent(event)
    actual should equal(Alive(User("a", 0L, "hans", "meier", None)))
  }

  it should "modify an aggregate root" in {
    val event = UserAgeChanged(EventHeader(), "a", 1L, 2)
    val actual = applyEvent(User("a", 0L, "hans", "meier", None), event)
    actual should equal(Alive(User("a", 1L, "hans", "meier", Some(2))))
  }

  it should "create and modify an aggregate root" in {
    val event1 = UserCreated(EventHeader(), arid("a"), arv(0L), "hans", "meier")
    val event2 = UserAgeChanged(EventHeader(), "a", 1L, 2)
    val actual = applyEventPostnatalis(fromEvent(event1), event2)
    actual should equal(Alive(User("a", 1L, "hans", "meier", Some(2))))
  }

  it should "create and delete an aggregate root" in {
    val event1 = UserCreated(EventHeader(), "a", 0L, "hans", "meier")
    val event2 = UserDied(EventHeader(), "a", 1L)
    val actual = applyEventPostnatalis(fromEvent(event1), event2)
    actual should equal(Dead("a", 1L))
  }

  it should "create a dead aggregate root" in {
    val event = UserNotAccepted(EventHeader(), arid("a"), arv(0L), "hans", "meier")
    val actual = fromEvent(event)
    actual should equal(Dead("a", 0L))
  }

  it should "create, modify and delete an aggregate root" in {
    val event1 = UserCreated(EventHeader(), arid("a"), arv(0L), "hans", "meier")
    val event2 = UserAgeChanged(EventHeader(), "a", 1L, 2)
    val event3 = UserLeft(EventHeader(), "a", 2L)
    val actual = applyEventsPostnatalis(fromEvent(event1), event2 :: event3 :: Nil)
    actual should equal(Dead("a", 2L))
  }

  it should "applyevents" in {
    ???
  }

  it should "applyEventPostnatalis" in {
    ???
  }

  it should "applyEventsPostnatalis" in {
    ???
  }

  it should "applyEventLifecycleAgnostic" in {
    ???
  }

}