package almhirt.aggregates

import org.scalatest._

import scalaz._, Scalaz._
import almhirt.common._

class AggregateRootEventHandlerTests extends FlatSpec with Matchers with UserEventHandler {
  import aggregatesforthelazyones._

  implicit val ccuad = CanCreateUuidsAndDateTimes()

  behavior of "AggregateRootEventHandler"

  it should "create an aggregate root" in {
    val event = UserCreated(EventHeader(), arid("a"), arv(0L), "hans", "meier")
    val actual = fromEvent(event)
    actual should equal(Vivus(User("a", 1L, "hans", "meier", None)))
  }

  it should "modify an aggregate root" in {
    val event = UserAgeChanged(EventHeader(), "a", 1L, 2)
    val actual = applyEvent(User("a", 1L, "hans", "meier", None), event)
    actual should equal(Vivus(User("a", 2L, "hans", "meier", Some(2))))
  }

  it should "create and modify an aggregate root" in {
    val event1 = UserCreated(EventHeader(), arid("a"), arv(0L), "hans", "meier")
    val event2 = UserAgeChanged(EventHeader(), "a", 1L, 2)
    val actual = applyEventPostnatalis(fromEvent(event1), event2)
    actual should equal(Vivus(User("a", 2L, "hans", "meier", Some(2))))
  }

  it should "create and delete an aggregate root" in {
    val event1 = UserCreated(EventHeader(), "a", 0L, "hans", "meier")
    val event2 = UserDied(EventHeader(), "a", 1L)
    val actual = applyEventPostnatalis(fromEvent(event1), event2)
    actual should equal(Mortuus("a", 2L))
  }

  it should "create a dead aggregate root" in {
    val event = UserNotAccepted(EventHeader(), arid("a"), arv(0L), "hans", "meier")
    val actual = fromEvent(event)
    actual should equal(Mortuus("a", 1L))
  }

  it should "create, modify and delete an aggregate root" in {
    val event1 = UserCreated(EventHeader(), arid("a"), arv(0L), "hans", "meier")
    val event2 = UserAgeChanged(EventHeader(), "a", 1L, 2)
    val event3 = UserLeft(EventHeader(), "a", 2L)
    val actual = applyEventsPostnatalis(fromEvent(event1), event2 :: event3 :: Nil)
    actual should equal(Mortuus("a", 3L))
  }

  it should "return the aggregate for applyevents when there are no events" in {
    val agg = User("a", 1L, "hans", "meier", Some(2))
    applyEvents(User("a", 1L, "hans", "meier", Some(2)), Nil) should equal(Vivus(agg))
  }

  it should "return Vivus(aggregate) for applyEventsPostnatalis when there are no events" in {
    val agg = Vivus(User("a", 1L, "hans", "meier", Some(2)))
    applyEventsPostnatalis(agg, Nil) should equal(agg)
  }

  it should "return Mortuus for applyEventsPostnatalis when there are no events" in {
    val agg = Mortuus("a", 1L)
    applyEventsPostnatalis(agg, Nil) should equal(agg)
  }

  it should "throw an exception for applyevents when an event follows dead state " in {
    val event1 = UserDied(EventHeader(), "a", 1L)
    val event2 = UserCreated(EventHeader(), arid("a"), arv(0L), "hans", "meier")
    intercept[Exception] {
      applyEvents(User("a", 1L, "hans", "meier", Some(2)), event1 :: event2 :: Nil)
    }
  }

  it should "throw an exception for applyEventPostnatalis when the state is dead" in {
    val event = UserCreated(EventHeader(), arid("a"), arv(0L), "hans", "meier")
    intercept[Exception] {
      applyEventPostnatalis(Mortuus("a", 1L), event)
    }
  }

  it should "throw an exception for applyEventPostnatalis when the state is dead and there are events" in {
    val event = UserCreated(EventHeader(), arid("a"), arv(0L), "hans", "meier")
    intercept[Exception] {
      applyEventsPostnatalis(Mortuus("a", 1L), event :: Nil)
    }
  }

  it should "throw an exception for applyEventLifecycleAgnostic when the state is dead" in {
    val event = UserCreated(EventHeader(), arid("a"), arv(0L), "hans", "meier")
    intercept[Exception] {
      applyEventLifecycleAgnostic(Mortuus("a", 1L), event)
    }
  }

}