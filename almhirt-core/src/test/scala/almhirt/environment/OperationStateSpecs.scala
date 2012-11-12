package almhirt.environment

import org.specs2.mutable._
import almhirt._
import almhirt.util._
import almhirt.syntax.almvalidation._
import almhirt.environment._
import test._

class OperationStateSpecs extends Specification with AlmhirtEnvironmentTestKit {
  implicit val atMost = akka.util.Duration(1, "s")
  "In an AlmhirtEnvironment: An OperationStateTracker updated to an InProcess state when queried with 'queryStateFor'" should {
    "return an Some(InProcess)" in {
      inTestEnvironment{ env =>
        env.reportOperationState(InProcess("test"))
        env.operationStateTracker.queryStateFor("test").awaitResult.forceResult === Some(InProcess("test"))
      }
    }
    "return an Some(InProcess) even when is InProcess is submitted more than once" in {
      inTestEnvironment { env =>
        env.reportOperationState(InProcess("test"))
        env.reportOperationState(InProcess("test"))
        env.operationStateTracker.queryStateFor("test").awaitResult.forceResult === Some(InProcess("test"))
      }
    }
    "return a Some(Executed) when updated to Executed" in {
      inTestEnvironment { env =>
        env.reportOperationState(InProcess("test"))
        env.reportOperationState(Executed("test"))
        env.operationStateTracker.queryStateFor("test").awaitResult.forceResult === Some(Executed("test"))
      }
    }
    "return a Some(NotExecuted) when updated to NotExecuted" in {
      inTestEnvironment { env =>
        env.reportOperationState(InProcess("test"))
        env.reportOperationState(NotExecuted("test", UnspecifiedProblem("")))
        env.operationStateTracker.queryStateFor("test").awaitResult.forceResult === Some(NotExecuted("test", UnspecifiedProblem("")))
      }
    }
  }
  
  "In an AlmhirtEnvironment: An OperationStateTracker updated to an InProcess state when queried with 'getResultFor'" should {
    "fail" in {
      inTestEnvironment { env =>
        env.reportOperationState(InProcess("test"))
        env.operationStateTracker.getResultFor("test").awaitResult.isFailure
      }
    }
    "fail with a timeout" in {
      inTestEnvironment { env =>
        env.reportOperationState(InProcess("test"))
//        env.context.operationStateChannel <-<*(opState => println("---------------------------->".format(opState)))
        env.operationStateTracker.getResultFor("test").awaitResult.forceProblem.isInstanceOf[OperationTimedOutProblem]
      }
    }
  }

  "In an AlmhirtEnvironment: An OperationStateTracker updated to an Executed state when queried with 'getResultFor'" should {
    "succeed" in {
      inTestEnvironment { env =>
        env.reportOperationState(Executed("test"))
        env.operationStateTracker.getResultFor("test").awaitResult.isSuccess
      }
    }
    "succeed with a Executed" in {
      inTestEnvironment { env =>
        env.reportOperationState(Executed("test"))
        env.operationStateTracker.getResultFor("test").awaitResult.forceResult === Executed("test")
      }
    }
  }
  
}