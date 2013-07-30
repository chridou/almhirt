package almhirt.testkit.components

import org.scalatest.matchers.ShouldMatchers
import org.scalatest.FunSpec
import scala.concurrent.Await
import akka.testkit.TestProbe
import almhirt.testkit._
import scala.concurrent.Future
import akka.actor._
import akka.pattern._
import akka.util.Timeout
import almhirt.common._
import almhirt.almvalidation.kit._
import almhirt.almfuture.all._
import almhirt.core._
import almhirt.commanding._
import almhirt.components.ExecutionStateTracker

abstract class ExecutionStateTrackerSpecsTemplate(theActorSystem: ActorSystem)
  extends AlmhirtTestKit(theActorSystem)
  with HasAlmhirt
  with FunSpec
  with ShouldMatchers { self: CreatesExecutionTracker =>

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
      	val states = for(n <- 1 to 20) yield ExecutionInProcess(n.toString)
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
    
    it("""should notify a subscriber for an already stored ExecutionSuccessful"""") {
      useExecutionTracker { tracker =>
      	val state = ExecutionSuccessful("a", "ahh!")
        tracker ! ExecutionStateChanged(state)
        waitSomeTime()
        val probe = TestProbe()
        tracker ! SubscribeForFinishedState("a", probe.ref)
        probe.expectMsg(defaultDuration, FinishedExecutionStateResult(state))
      }
    }

    it("""should notify a subscriber for an already stored ExecutionFailed"""") {
      useExecutionTracker { tracker =>
      	val state = ExecutionFailed("a", UnspecifiedProblem("huh"))
        tracker ! ExecutionStateChanged(state)
        waitSomeTime()
        val probe = TestProbe()
        tracker ! SubscribeForFinishedState("a", probe.ref)
        probe.expectMsg(defaultDuration, FinishedExecutionStateResult(state))
      }
    }

    it("""should notify a subscriber as soon as an ExecutionSuccessful is recognized"""") {
      useExecutionTracker { tracker =>
      	val state = ExecutionSuccessful("a", "ahh!")
        val probe = TestProbe()
        tracker ! SubscribeForFinishedState("a", probe.ref)
        tracker ! ExecutionStateChanged(state)
        probe.expectMsg(defaultDuration, FinishedExecutionStateResult(state))
      }
    }

    it("""should notify a subscriber as soon as an ExecutionFailed is recognized"""") {
      useExecutionTracker { tracker =>
      	val state = ExecutionFailed("a", UnspecifiedProblem("huh"))
        val probe = TestProbe()
        tracker ! SubscribeForFinishedState("a", probe.ref)
        tracker ! ExecutionStateChanged(state)
        probe.expectMsg(defaultDuration, FinishedExecutionStateResult(state))
      }
    }
 
    it("""should notify a subscriber only once"""") {
      useExecutionTracker { tracker =>
      	val state = ExecutionFailed("a", UnspecifiedProblem("huh"))
        val probe = TestProbe()
        tracker ! SubscribeForFinishedState("a", probe.ref)
        tracker ! ExecutionStateChanged(state)
        probe.expectMsg(defaultDuration, FinishedExecutionStateResult(state))
        tracker ! ExecutionStateChanged(state)
        probe.expectNoMsg
      }
    }

    it("""should be idempotent on subscriptions for the same tracking ticket"""") {
      useExecutionTracker { tracker =>
      	val state = ExecutionFailed("a", UnspecifiedProblem("huh"))
        val probe = TestProbe()
        tracker ! SubscribeForFinishedState("a", probe.ref)
        tracker ! SubscribeForFinishedState("a", probe.ref)
        tracker ! SubscribeForFinishedState("a", probe.ref)
        tracker ! ExecutionStateChanged(state)
        probe.expectMsg(defaultDuration, FinishedExecutionStateResult(state))
        probe.expectNoMsg
      }
    }
    
  }
}