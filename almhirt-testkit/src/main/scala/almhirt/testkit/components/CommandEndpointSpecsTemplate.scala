package almhirt.testkit.components

import org.scalatest._
import akka.testkit.TestProbe
import almhirt.testkit._
import akka.actor._
import akka.pattern._
import almhirt.components.CommandEndpoint
import almhirt.commanding._
import almhirt.core.types._
import almhirt.common.Message
import almhirt.common.CanCreateUuidsAndDateTimes
import almhirt.components.ExecutionStateTracker._

abstract class CommandEndpointSpecsTemplate(theActorSystem: ActorSystem)
  extends AlmhirtTestKit(theActorSystem)
  with FunSpecLike
  with Matchers { self: CreatesCommandEndpoint =>

  implicit def execContext = theAlmhirt.futuresExecutor
    
  val fixedUniqueString = "x"
  def fixedStringGen() = fixedUniqueString
  
  def useCommandEndpoint[T](f: (CommandEndpoint, TestProbe, ActorRef) => T): T = {
    val testId = nextTestId
    val (endpoint, spy, tracker, cleanUp) = createCommandEndpoint(testId)
    try {
      val res = f(endpoint, spy, tracker)
      cleanUp()
      res
    } catch {
      case exn: Exception =>
        cleanUp()
        throw exn
    }
  }

  def useCommandEndpointAndTrackIdGen[T](genTrackId: () => String)(f: (CommandEndpoint, TestProbe, ActorRef) => T): T = {
    val testId = nextTestId
    val (endpoint, spy, tracker, cleanUp) = createCommandEndpoint(testId, genTrackId)
    try {
      val res = f(endpoint, spy, tracker)
      cleanUp()
      res
    } catch {
      case exn: Exception =>
        cleanUp()
        throw exn
    }
  }
  
  describe("A CommandEndpoint") {
    it("""should forward a command on "execute"""") {
      useCommandEndpoint{ (endpoint, spy, tracker) =>
        val cmd = AR1ComCreateAR1(DomainCommandHeader(AggregateRootRef(ccuad.getUuid)), "a")
        endpoint.execute(cmd)
        val res = spy.expectMsgPF(defaultDuration, "Waiting for a message containing the given command") {
          case Message(_, payload) => payload
        }
        res should equal(cmd)
      }
    }
    
    it("""should forward a command and add a tracking id to it when it doesn't have one on "tracked"""") {
      useCommandEndpoint{ (endpoint, spy, tracker) =>
        val cmd = AR1ComCreateAR1(DomainCommandHeader(AggregateRootRef(ccuad.getUuid)), "a")
        val trackIdRes = endpoint.executeTracked(cmd)
        val res = spy.expectMsgPF(defaultDuration, "Waiting for a message containing the given command") {
          case Message(_, payload) => payload
        }
        res should equal(cmd.track(trackIdRes))
      }
    }
    
    it("""should forward a command and use the given tracking id on "tracked"""") {
      useCommandEndpoint{ (endpoint, spy, tracker) =>
        val trackId = ccuad.getUniqueString
        val cmd = AR1ComCreateAR1(DomainCommandHeader(AggregateRootRef(ccuad.getUuid)), "a").track(trackId)
        val trackIdRes = endpoint.executeTracked(cmd)
        val res = spy.expectMsgPF(defaultDuration, "Waiting for a message containing the given command") {
          case Message(_, payload) => payload
        }
        res should equal(cmd)
        trackIdRes should equal(trackId)
      }
    }
    
    it("""should wait for a finished command and assign a tracking id when there is none on the command on "executeSync"""") {
      useCommandEndpointAndTrackIdGen(fixedStringGen){ (endpoint, spy, tracker) =>
        val cmd = AR1ComCreateAR1(DomainCommandHeader(AggregateRootRef(ccuad.getUuid)), "a")
        val execState = ExecutionSuccessful(fixedUniqueString, "ahh!")
        val resF = endpoint.executeSync(cmd, defaultDuration)
        tracker ! ExecutionStateChanged(execState)
        val res = resF.awaitResultOrEscalate(defaultDuration)
        res should equal(FinishedExecutionStateResult(execState))
      }
    }
    
    it("""should wait for a finished command and use a given tracking id on "executeSync"""") {
      useCommandEndpointAndTrackIdGen(fixedStringGen){ (endpoint, spy, tracker) =>
        val trackId = ccuad.getUniqueString
        val cmd = AR1ComCreateAR1(DomainCommandHeader(AggregateRootRef(ccuad.getUuid)), "a").track(trackId)
        val execState = ExecutionSuccessful(trackId, "ahh!")
        val resF = endpoint.executeSync(cmd, defaultDuration)
        tracker ! ExecutionStateChanged(execState)
        val res = resF.awaitResultOrEscalate(defaultDuration)
        res should equal(FinishedExecutionStateResult(execState))
      }
    }
  }
  
}