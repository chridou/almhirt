package almhirt.testkit.components

import org.scalatest._
import scala.concurrent.Await
import akka.testkit.TestProbe
import almhirt.testkit._
import scala.concurrent.Future
import akka.actor._
import akka.pattern._
import akka.util.Timeout
import almhirt.common._
import almhirt.core.types._
import almhirt.almvalidation.kit._
import almhirt.almfuture.all._
import almhirt.core._
import almhirt.commanding._
import almhirt.components.ExecutionStateTracker

abstract class ExecutionStateTrackerSpecsTemplate(theActorSystem: ActorSystem)
  extends AlmhirtTestKit(theActorSystem)
  with HasAlmhirt
  with FunSpecLike
  with Matchers { self: CreatesExecutionTracker =>

  import ExecutionStateTracker._  

  def sleepMillisAfterFireAndForget: Option[Int]

  protected def waitSomeTime() {
    sleepMillisAfterFireAndForget.foreach(t => Thread.sleep(t))
  }
  protected def waitSomeTime(times: Int) {
    sleepMillisAfterFireAndForget.foreach(t => Thread.sleep(t*times))
  }
  
  implicit def execContext = theAlmhirt.futuresExecutor

  def useExecutionTracker[T](f: ActorRef => T): T = {
    val testId = nextTestId
    val (executionTracker, eventLogCleanUp) = createExecutionTracker(testId)
    try {
      val res = f(executionTracker)
      system.stop(executionTracker)
      eventLogCleanUp()
      res
    } catch {
      case exn: Exception =>
        system.stop(executionTracker)
        eventLogCleanUp()
        throw exn
    }
  }
  
  describe("An execution state tracker") {
    it("""should accept an ExecutionStateChanged(ExecutionStarted) and return the state when queried with its tracking id""") {
      useExecutionTracker { tracker =>
      	val state = ExecutionStarted("a")
        tracker ! ExecutionStateChanged(state)
        waitSomeTime()
        val res = (tracker ? GetExecutionStateFor(state.trackId))(defaultDuration).successfulAlmFuture[QueriedExecutionState].awaitResultOrEscalate(defaultDuration)
        res should equal(QueriedExecutionState(state.trackId, Some(state)))
      }
    }
    it("""should accept an ExecutionStateChanged(ExecutionInProcess) and return the state when queried with its tracking id""") {
      useExecutionTracker { tracker =>
      	val state = ExecutionInProcess("a")
        tracker ! ExecutionStateChanged(state)
        waitSomeTime()
        val res = (tracker ? GetExecutionStateFor(state.trackId))(defaultDuration).successfulAlmFuture[QueriedExecutionState].awaitResultOrEscalate(defaultDuration)
        res should equal(QueriedExecutionState(state.trackId, Some(state)))
      }
    }
    it("""should accept an ExecutionStateChanged(ExecutionSuccessful) and return the state when queried with its tracking id""") {
      useExecutionTracker { tracker =>
      	val state = ExecutionSuccessful("did something", "a")
        tracker ! ExecutionStateChanged(state)
        waitSomeTime()
        val res = (tracker ? GetExecutionStateFor(state.trackId))(defaultDuration).successfulAlmFuture[QueriedExecutionState].awaitResultOrEscalate(defaultDuration)
        res should equal(QueriedExecutionState(state.trackId, Some(state)))
      }
    }
    it("""should accept an ExecutionStateChanged(ExecutionFailed) and return the state when queried with its tracking id""") {
      useExecutionTracker { tracker =>
      	val state = ExecutionFailed("a", UnspecifiedProblem("huh"))
        tracker ! ExecutionStateChanged(state)
        waitSomeTime()
        val res = (tracker ? GetExecutionStateFor(state.trackId))(defaultDuration).successfulAlmFuture[QueriedExecutionState].awaitResultOrEscalate(defaultDuration)
        res should equal(QueriedExecutionState(state.trackId, Some(state)))
      }
    }
    it("""should return QueriedExecutionState(tracking-id, None) when no state exists with the given tracking id""") {
      useExecutionTracker { tracker =>
        val res = (tracker ? GetExecutionStateFor("?"))(defaultDuration).successfulAlmFuture[QueriedExecutionState].awaitResultOrEscalate(defaultDuration)
        res should equal(QueriedExecutionState("?", None))
      }
    }
    it("""should return the correct states when there are many""") {
      useExecutionTracker { tracker =>
      	val states = for(n <- 1 to 10) yield ExecutionInProcess(n.toString)
        states foreach(tracker ! ExecutionStateChanged(_))
        waitSomeTime()
        val futures = states map(state => (tracker ? GetExecutionStateFor(state.trackId))(defaultDuration).mapTo[QueriedExecutionState])
        val res = Await.result(Future.sequence(futures),defaultDuration)
        res.map(_.executionState).flatten should equal(states)
      }
    }
    it("""should return ExecutionStarted for "ExecutionStarted -> ExecutionStarted"""") {
      useExecutionTracker { tracker =>
      	val state1 = ExecutionStarted("a")
      	val state2 = ExecutionStarted("a")
        tracker ! ExecutionStateChanged(state1)
        tracker ! ExecutionStateChanged(state2)
        waitSomeTime(2)
        val res = (tracker ? GetExecutionStateFor(state2.trackId))(defaultDuration).successfulAlmFuture[QueriedExecutionState].awaitResultOrEscalate(defaultDuration)
        res should equal(QueriedExecutionState(state2.trackId, Some(state2)))
      }
    }
    it("""should return ExecutionInProcess for "ExecutionStarted -> ExecutionInProcess"""") {
      useExecutionTracker { tracker =>
      	val state1 = ExecutionStarted("a")
      	val state2 = ExecutionInProcess("a")
        tracker ! ExecutionStateChanged(state1)
        tracker ! ExecutionStateChanged(state2)
        waitSomeTime(2)
        val res = (tracker ? GetExecutionStateFor(state2.trackId))(defaultDuration).successfulAlmFuture[QueriedExecutionState].awaitResultOrEscalate(defaultDuration)
        res should equal(QueriedExecutionState(state2.trackId, Some(state2)))
      }
    }
    it("""should return ExecutionSuccessful for "ExecutionStarted -> ExecutionSuccessful"""") {
      useExecutionTracker { tracker =>
      	val state1 = ExecutionStarted("a")
      	val state2 = ExecutionSuccessful("a", "ahh!")
        tracker ! ExecutionStateChanged(state1)
        tracker ! ExecutionStateChanged(state2)
        waitSomeTime(2)
        val res = (tracker ? GetExecutionStateFor(state2.trackId))(defaultDuration).successfulAlmFuture[QueriedExecutionState].awaitResultOrEscalate(defaultDuration)
        res should equal(QueriedExecutionState(state2.trackId, Some(state2)))
      }
    }
    it("""should return ExecutionFailed for "ExecutionStarted -> ExecutionFailed"""") {
      useExecutionTracker { tracker =>
      	val state1 = ExecutionStarted("a")
      	val state2 = ExecutionFailed("a", UnspecifiedProblem("huh"))
        tracker ! ExecutionStateChanged(state1)
        tracker ! ExecutionStateChanged(state2)
        waitSomeTime(2)
        val res = (tracker ? GetExecutionStateFor(state2.trackId))(defaultDuration).successfulAlmFuture[QueriedExecutionState].awaitResultOrEscalate(defaultDuration)
        res should equal(QueriedExecutionState(state2.trackId, Some(state2)))
      }
    }
    it("""should return ExecutionSuccessful for "ExecutionInProcess -> ExecutionStarted"""") {
      useExecutionTracker { tracker =>
      	val state1 = ExecutionInProcess("a")
      	val state2 = ExecutionStarted("a")
        tracker ! ExecutionStateChanged(state1)
        tracker ! ExecutionStateChanged(state2)
        waitSomeTime(2)
        val res = (tracker ? GetExecutionStateFor(state1.trackId))(defaultDuration).successfulAlmFuture[QueriedExecutionState].awaitResultOrEscalate(defaultDuration)
        res should equal(QueriedExecutionState(state1.trackId, Some(state1)))
      }
    }
    it("""should return ExecutionSuccessful for "ExecutionSuccessful -> ExecutionStarted"""") {
      useExecutionTracker { tracker =>
      	val state1 = ExecutionSuccessful("a", "ahh!")
      	val state2 = ExecutionStarted("a")
        tracker ! ExecutionStateChanged(state1)
        tracker ! ExecutionStateChanged(state2)
        waitSomeTime(2)
        val res = (tracker ? GetExecutionStateFor(state1.trackId))(defaultDuration).successfulAlmFuture[QueriedExecutionState].awaitResultOrEscalate(defaultDuration)
        res should equal(QueriedExecutionState(state1.trackId, Some(state1)))
      }
    }
    it("""should return ExecutionSuccessful for "ExecutionSuccessful -> ExecutionInProcess"""") {
      useExecutionTracker { tracker =>
      	val state1 = ExecutionSuccessful("a", "ahh!")
      	val state2 = ExecutionInProcess("a")
        tracker ! ExecutionStateChanged(state1)
        tracker ! ExecutionStateChanged(state2)
        waitSomeTime(2)
        val res = (tracker ? GetExecutionStateFor(state1.trackId))(defaultDuration).successfulAlmFuture[QueriedExecutionState].awaitResultOrEscalate(defaultDuration)
        res should equal(QueriedExecutionState(state1.trackId, Some(state1)))
      }
    }
    it("""should return ExecutionSuccessful for "ExecutionSuccessful -> ExecutionFailed"""") {
      useExecutionTracker { tracker =>
      	val state1 = ExecutionSuccessful("a", "ahh!")
      	val state2 = ExecutionFailed("a", UnspecifiedProblem("huh"))
        tracker ! ExecutionStateChanged(state1)
        tracker ! ExecutionStateChanged(state2)
        waitSomeTime(2)
        val res = (tracker ? GetExecutionStateFor(state1.trackId))(defaultDuration).successfulAlmFuture[QueriedExecutionState].awaitResultOrEscalate(defaultDuration)
        res should equal(QueriedExecutionState(state1.trackId, Some(state1)))
      }
    }
    it("""should return ExecutionFailed for "ExecutionInProcess -> ExecutionFailed"""") {
      useExecutionTracker { tracker =>
      	val state1 = ExecutionStarted("a")
      	val state2 = ExecutionFailed("a", UnspecifiedProblem("huh"))
        tracker ! ExecutionStateChanged(state1)
        tracker ! ExecutionStateChanged(state2)
        waitSomeTime(2)
        val res = (tracker ? GetExecutionStateFor(state2.trackId))(defaultDuration).successfulAlmFuture[QueriedExecutionState].awaitResultOrEscalate(defaultDuration)
        res should equal(QueriedExecutionState(state2.trackId, Some(state2)))
      }
    }
    it("""should return ExecutionFailed for "ExecutionFailed -> ExecutionStarted"""") {
      useExecutionTracker { tracker =>
      	val state1 = ExecutionFailed("a", UnspecifiedProblem("huh"))
      	val state2 = ExecutionStarted("a")
        tracker ! ExecutionStateChanged(state1)
        tracker ! ExecutionStateChanged(state2)
        waitSomeTime(2)
        val res = (tracker ? GetExecutionStateFor(state1.trackId))(defaultDuration).successfulAlmFuture[QueriedExecutionState].awaitResultOrEscalate(defaultDuration)
        res should equal(QueriedExecutionState(state1.trackId, Some(state1)))
      }
    }
    it("""should return ExecutionFailed for "ExecutionFailed -> ExecutionInProcess"""") {
      useExecutionTracker { tracker =>
      	val state1 = ExecutionFailed("a", UnspecifiedProblem("huh"))
      	val state2 = ExecutionInProcess("a")
        tracker ! ExecutionStateChanged(state1)
        tracker ! ExecutionStateChanged(state2)
        waitSomeTime(2)
        val res = (tracker ? GetExecutionStateFor(state1.trackId))(defaultDuration).successfulAlmFuture[QueriedExecutionState].awaitResultOrEscalate(defaultDuration)
        res should equal(QueriedExecutionState(state1.trackId, Some(state1)))
      }
    }
    it("""should return ExecutionFailed for "ExecutionFailed -> ExecutionSuccessful"""") {
      useExecutionTracker { tracker =>
      	val state1 = ExecutionFailed("a", UnspecifiedProblem("huh"))
      	val state2 = ExecutionSuccessful("a", "ahh!")
        tracker ! ExecutionStateChanged(state1)
        tracker ! ExecutionStateChanged(state2)
        waitSomeTime(2)
        val res = (tracker ? GetExecutionStateFor(state1.trackId))(defaultDuration).successfulAlmFuture[QueriedExecutionState].awaitResultOrEscalate(defaultDuration)
        res should equal(QueriedExecutionState(state1.trackId, Some(state1)))
      }
    }
    
    it("""should notify a subscriber for an already stored ExecutionSuccessful""") {
      useExecutionTracker { tracker =>
      	val state = ExecutionSuccessful("a", "ahh!")
        tracker ! ExecutionStateChanged(state)
        waitSomeTime()
        val probe = TestProbe()
        probe.send(tracker, SubscribeForFinishedState("a"))
        probe.expectMsg(defaultDuration, FinishedExecutionStateResult(state))
      }
    }

    it("""should notify a subscriber for an already stored ExecutionFailed""") {
      useExecutionTracker { tracker =>
      	val state = ExecutionFailed("a", UnspecifiedProblem("huh"))
        tracker ! ExecutionStateChanged(state)
        waitSomeTime()
        val probe = TestProbe()
        probe.send(tracker, SubscribeForFinishedState("a"))
        probe.expectMsg(defaultDuration, FinishedExecutionStateResult(state))
      }
    }

    it("""should notify a subscriber as soon as an ExecutionSuccessful is recognized""") {
      useExecutionTracker { tracker =>
      	val state = ExecutionSuccessful("a", "ahh!")
        val probe = TestProbe()
        probe.send(tracker, SubscribeForFinishedState("a"))
        tracker ! ExecutionStateChanged(state)
        probe.expectMsg(defaultDuration, FinishedExecutionStateResult(state))
      }
    }

    it("""should notify a subscriber as soon as an ExecutionFailed is recognized""") {
      useExecutionTracker { tracker =>
      	val state = ExecutionFailed("a", UnspecifiedProblem("huh"))
        val probe = TestProbe()
        probe.send(tracker, SubscribeForFinishedState("a"))
        tracker ! ExecutionStateChanged(state)
        probe.expectMsg(defaultDuration, FinishedExecutionStateResult(state))
      }
    }
 
    it("""should notify a subscriber only once""") {
      useExecutionTracker { tracker =>
      	val state = ExecutionFailed("a", UnspecifiedProblem("huh"))
        val probe = TestProbe()
        probe.send(tracker, SubscribeForFinishedState("a"))
        tracker ! ExecutionStateChanged(state)
        probe.expectMsg(defaultDuration, FinishedExecutionStateResult(state))
        tracker ! ExecutionStateChanged(state)
        probe.expectNoMsg
      }
    }

    it("""should be idempotent on subscriptions for the same tracking ticket""") {
      useExecutionTracker { tracker =>
      	val state = ExecutionFailed("a", UnspecifiedProblem("huh"))
        val probe = TestProbe()
        probe.send(tracker, SubscribeForFinishedState("a"))
        probe.send(tracker, SubscribeForFinishedState("a"))
        probe.send(tracker, SubscribeForFinishedState("a"))
        tracker ! ExecutionStateChanged(state)
        probe.expectMsg(defaultDuration, FinishedExecutionStateResult(state))
        probe.expectNoMsg
      }
    }

    it("""should notify on a transition to ExecutionSuccessful""") {
      useExecutionTracker { tracker =>
      	val state1 = ExecutionStarted("a")
      	val state2 = ExecutionSuccessful("a", "ahh!")
        val probe = TestProbe()
        probe.send(tracker, SubscribeForFinishedState("a"))
        tracker ! ExecutionStateChanged(state1)
        probe.expectNoMsg
        tracker ! ExecutionStateChanged(state2)
        probe.expectMsg(defaultDuration, FinishedExecutionStateResult(state2))
      }
    }

    it("""should notify on a transition to ExecutionFailed""") {
      useExecutionTracker { tracker =>
      	val state1 = ExecutionStarted("a")
      	val state2 = ExecutionFailed("a", UnspecifiedProblem("huh"))
        val probe = TestProbe()
        probe.send(tracker, SubscribeForFinishedState("a"))
        tracker ! ExecutionStateChanged(state1)
        probe.expectNoMsg
        tracker ! ExecutionStateChanged(state2)
        probe.expectMsg(defaultDuration, FinishedExecutionStateResult(state2))
      }
    }

    it("""should notify more than one subscriber subscribed to the same tracking id on a transition to an ExecutionFinishedState""") {
      useExecutionTracker { tracker =>
      	val state1 = ExecutionStarted("a")
      	val state2 = ExecutionFailed("a", UnspecifiedProblem("huh"))
        val probe1 = TestProbe()
        val probe2 = TestProbe()
        tracker ! ExecutionStateChanged(state1)
        probe1.send(tracker, SubscribeForFinishedState("a"))
        probe2.send(tracker, SubscribeForFinishedState("a"))
        tracker ! ExecutionStateChanged(state2)
        probe1.expectMsg(defaultDuration, FinishedExecutionStateResult(state2))
        probe2.expectMsg(defaultDuration, FinishedExecutionStateResult(state2))
      }
    }
    
    it("""should notify more than one subscriber subscribed to different tracking ids on a transition to an ExecutionFinishedState""") {
      useExecutionTracker { tracker =>
      	val state1a = ExecutionStarted("a")
      	val state2a = ExecutionFailed("a", UnspecifiedProblem("huh"))
      	val state1b = ExecutionStarted("b")
      	val state2b = ExecutionFailed("b", UnspecifiedProblem("huh"))
        val probe1 = TestProbe()
        val probe2 = TestProbe()
        tracker ! ExecutionStateChanged(state1a)
        tracker ! ExecutionStateChanged(state1b)
        probe1.send(tracker, SubscribeForFinishedState("a"))
        probe2.send(tracker, SubscribeForFinishedState("b"))
        tracker ! ExecutionStateChanged(state2a)
        tracker ! ExecutionStateChanged(state2b)
        probe1.expectMsg(defaultDuration, FinishedExecutionStateResult(state2a))
        probe2.expectMsg(defaultDuration, FinishedExecutionStateResult(state2b))
      }
    }

    it("""should not notify a subscriber that was unsubscribed""") {
      useExecutionTracker { tracker =>
      	val state1 = ExecutionStarted("a")
      	val state2 = ExecutionFailed("a", UnspecifiedProblem("huh"))
        val probe1 = TestProbe()
        probe1.send(tracker, SubscribeForFinishedState("a"))
        tracker ! ExecutionStateChanged(state1)
        probe1.send(tracker, UnsubscribeForFinishedState("a"))
        tracker ! ExecutionStateChanged(state2)
        probe1.expectNoMsg
      }
    }

    it("""should notify one of 2 subscribers subscribed to the same tracking id on a transition to an ExecutionFinishedState when one unsubscribed prior to the transition""") {
      useExecutionTracker { tracker =>
      	val state1 = ExecutionStarted("a")
      	val state2 = ExecutionFailed("a", UnspecifiedProblem("huh"))
        val probe1 = TestProbe()
        val probe2 = TestProbe()
        probe2.send(tracker, SubscribeForFinishedState("a"))
        tracker ! ExecutionStateChanged(state1)
        probe2.send(tracker, UnsubscribeForFinishedState("a"))
        probe1.send(tracker, SubscribeForFinishedState("a"))
        tracker ! ExecutionStateChanged(state2)
        probe1.expectMsg(defaultDuration, FinishedExecutionStateResult(state2))
        probe2.expectNoMsg
      }
    }
  }
}