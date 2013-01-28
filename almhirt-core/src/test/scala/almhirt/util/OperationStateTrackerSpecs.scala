package almhirt.util

import scala.concurrent.duration.Duration
import almhirt.common._
import almhirt.syntax.almvalidation._
import almhirt.environment._
import test._
import org.scalatest._

class OperationStateTrackerSpecs extends FlatSpec with AlmhirtTestKit {
  private implicit val atMost = Duration(2, "s")
  def withTrackerInTestContext[T](compute: OperationStateTracker => T) =
      inExtendedTestAlmhirt(createExtendedBootStrapper()){ implicit ctx =>
        val tracker = ctx.operationStateTracker
        compute(tracker)
      }
  
  "An OperationStateTracker" should 
    "accept an InProgress state" in {
      withTrackerInTestContext { tracker =>
        tracker.updateState(InProcess("test"))
        true
      }
    }
    it should "accept an Executed state" in {
      withTrackerInTestContext { tracker =>
        tracker.updateState(Executed("test"))
        true
      }
    }
    it should "accept an NotExecuted state" in {
      withTrackerInTestContext { tracker =>
        tracker.updateState(NotExecuted("test", UnspecifiedProblem("")))
        true
      }
    }

  "An OperationStateTracker updated to an InProcess state when queried with 'queryStateFor'" should 
    "return an Some(InProcess)" in {
      withTrackerInTestContext { tracker =>
        tracker.updateState(InProcess("test"))
        tracker.queryStateFor(atMost)("test").awaitResult.forceResult === Some(InProcess("test"))
      }
    }
    it should "return an Some(InProcess) even when is InProcess is submitted more than once" in {
      withTrackerInTestContext { tracker =>
        tracker.updateState(InProcess("test"))
        tracker.updateState(InProcess("test"))
        tracker.queryStateFor(atMost)("test").awaitResult.forceResult === Some(InProcess("test"))
      }
    }
    it should "return a Some(Executed) when updated to Executed" in {
      withTrackerInTestContext { tracker =>
        tracker.updateState(InProcess("test"))
        tracker.updateState(Executed("test"))
        tracker.queryStateFor(atMost)("test").awaitResult.forceResult === Some(Executed("test"))
      }
    }
    it should "return a Some(NotExecuted) when updated to NotExecuted" in {
      withTrackerInTestContext { tracker =>
        tracker.updateState(InProcess("test"))
        tracker.updateState(NotExecuted("test", UnspecifiedProblem("")))
        tracker.queryStateFor(atMost)("test").awaitResult.forceResult === Some(NotExecuted("test", UnspecifiedProblem("")))
      }
    }

  "An OperationStateTracker updated to an Executed state when queried with 'queryStateFor'" should 
    "return an Some(Executed)" in {
      withTrackerInTestContext { tracker =>
        tracker.updateState(Executed("test"))
        tracker.queryStateFor(atMost)("test").awaitResult.forceResult === Some(Executed("test"))
      }
    }
    it should "return an Some(Executed) even when is Executed is submitted more than once" in {
      withTrackerInTestContext { tracker =>
        tracker.updateState(Executed("test"))
        tracker.updateState(Executed("test"))
        tracker.queryStateFor(atMost)("test").awaitResult.forceResult === Some(Executed("test"))
      }
    }
    it should "return a Some(Executed) when updated to NotExecuted" in {
      withTrackerInTestContext { tracker =>
        tracker.updateState(Executed("test"))
        tracker.updateState(NotExecuted("test", UnspecifiedProblem("")))
        tracker.queryStateFor(atMost)("test").awaitResult.forceResult === Some(Executed("test"))
      }
    }
    it should "return a Some(Executed) when updated to InProcess" in {
      withTrackerInTestContext { tracker =>
        tracker.updateState(Executed("test"))
        tracker.updateState(InProcess("test"))
        tracker.queryStateFor(atMost)("test").awaitResult.forceResult === Some(Executed("test"))
      }
    }
  
  "An OperationStateTracker updated to a NotExecuted state when queried with 'queryStateFor'" should 
    "return an Some(NotExecuted)" in {
      withTrackerInTestContext { tracker =>
        tracker.updateState(NotExecuted("test", UnspecifiedProblem("")))
        tracker.queryStateFor(atMost)("test").awaitResult.forceResult === Some(NotExecuted("test", UnspecifiedProblem("")))
      }
    }
    it should "return an Some(NotExecuted) even when is NotExecuted is submitted more than once" in {
      withTrackerInTestContext { tracker =>
        tracker.updateState(NotExecuted("test", UnspecifiedProblem("")))
        tracker.updateState(NotExecuted("test", UnspecifiedProblem("")))
        tracker.queryStateFor(atMost)("test").awaitResult.forceResult === Some(NotExecuted("test", UnspecifiedProblem("")))
      }
    }
    it should "return a Some(NotExecuted) when updated to Executed" in {
      withTrackerInTestContext { tracker =>
        tracker.updateState(NotExecuted("test", UnspecifiedProblem("")))
        tracker.updateState(Executed("test"))
        tracker.queryStateFor(atMost)("test").awaitResult.forceResult === Some(NotExecuted("test", UnspecifiedProblem("")))
      }
    }
    it should "return a Some(NotExecuted) when updated to InProcess" in {
      withTrackerInTestContext { tracker =>
        tracker.updateState(NotExecuted("test", UnspecifiedProblem("")))
        tracker.updateState(InProcess("test"))
        tracker.queryStateFor(atMost)("test").awaitResult.forceResult === Some(NotExecuted("test", UnspecifiedProblem("")))
      }
    }

  "An OperationStateTracker updated to an InProcess state when queried with 'getResultFor'" should 
    "fail" in {
      withTrackerInTestContext { tracker =>
        tracker.updateState(InProcess("test"))
        tracker.getResultFor(atMost)("test").awaitResult.isFailure
      }
    }
    it should "fail with a timeout" in {
      withTrackerInTestContext { tracker =>
        tracker.updateState(InProcess("test"))
        tracker.getResultFor(atMost)("test").awaitResult.forceProblem.isInstanceOf[OperationTimedOutProblem]
      }
    }

  "An OperationStateTracker updated to an Executed state when queried with 'getResultFor'" should 
    "succeed" in {
      withTrackerInTestContext { tracker =>
        tracker.updateState(Executed("test"))
        tracker.getResultFor(atMost)("test").awaitResult.isSuccess
      }
    }
    it should "succeed with a Executed" in {
      withTrackerInTestContext { tracker =>
        tracker.updateState(Executed("test"))
        tracker.getResultFor(atMost)("test").awaitResult.forceResult === Executed("test")
      }
    }

  "An OperationStateTracker updated to an NotExecuted state when queried with 'getResultFor'" should 
    "succeed" in {
      withTrackerInTestContext { tracker =>
        tracker.updateState(NotExecuted("test", UnspecifiedProblem("")))
        tracker.getResultFor(atMost)("test").awaitResult.isSuccess
      }
    }
    it should "succeed with a Executed" in {
      withTrackerInTestContext { tracker =>
        tracker.updateState(NotExecuted("test", UnspecifiedProblem("")))
        tracker.getResultFor(atMost)("test").awaitResult.forceResult === NotExecuted("test", UnspecifiedProblem(""))
      }
    }

  "An OperationStateTracker updated to an InProgress state and then to an Executed state when queried with 'getResultFor'" should 
    "succeed" in {
      withTrackerInTestContext { tracker =>
        tracker.updateState(InProcess("test"))
        tracker.updateState(Executed("test"))
        tracker.getResultFor(atMost)("test").awaitResult.isSuccess
      }
    }
    it should "succeed with a Executed" in {
      withTrackerInTestContext { tracker =>
        tracker.updateState(InProcess("test"))
        tracker.updateState(Executed("test"))
        tracker.getResultFor(atMost)("test").awaitResult.forceResult === Executed("test")
      }
    }

  "An OperationStateTracker updated to an InProgress state and then to an NotExecuted state when queried with 'getResultFor'" should 
    "succeed" in {
      withTrackerInTestContext { tracker =>
        tracker.updateState(InProcess("test"))
        tracker.updateState(NotExecuted("test", UnspecifiedProblem("")))
        tracker.getResultFor(atMost)("test").awaitResult.isSuccess
      }
    }
    it should "succeed with a NotExecuted" in {
      withTrackerInTestContext { tracker =>
        tracker.updateState(InProcess("test"))
        tracker.updateState(NotExecuted("test", UnspecifiedProblem("")))
        tracker.getResultFor(atMost)("test").awaitResult.forceResult === NotExecuted("test", UnspecifiedProblem(""))
      }
    }

  "An OperationStateTracker updated to an Executed state and then to an NotExecuted state when queried with 'getResultFor'" should 
    "succeed" in {
      withTrackerInTestContext { tracker =>
        tracker.updateState(Executed("test"))
        tracker.updateState(NotExecuted("test", UnspecifiedProblem("")))
        tracker.getResultFor(atMost)("test").awaitResult.isSuccess
      }
    }
    it should "succeed with a Executed" in {
      withTrackerInTestContext { tracker =>
        tracker.updateState(Executed("test"))
        tracker.updateState(NotExecuted("test", UnspecifiedProblem("")))
        tracker.getResultFor(atMost)("test").awaitResult.forceResult === Executed("test")
      }
    }

  "An OperationStateTracker updated to an NotExecuted state and then to an Executed state when queried with 'getResultFor'" should 
    "succeed" in {
      withTrackerInTestContext { tracker =>
        tracker.updateState(NotExecuted("test", UnspecifiedProblem("")))
        tracker.updateState(Executed("test"))
        tracker.getResultFor(atMost)("test").awaitResult.isSuccess
      }
    }
    it should "succeed with a NotExecuted" in {
      withTrackerInTestContext { tracker =>
        tracker.updateState(NotExecuted("test", UnspecifiedProblem("")))
        tracker.updateState(Executed("test"))
        tracker.getResultFor(atMost)("test").awaitResult.forceResult === NotExecuted("test", UnspecifiedProblem(""))
      }
    }

  "An OperationStateTracker updated to an Executed state and then to an InProcess state when queried with 'getResultFor'" should 
    "succeed" in {
      withTrackerInTestContext { tracker =>
        tracker.updateState(Executed("test"))
        tracker.updateState(InProcess("test"))
        tracker.getResultFor(atMost)("test").awaitResult.isSuccess
      }
    }
    it should "succeed with a Executed" in {
      withTrackerInTestContext { tracker =>
        tracker.updateState(Executed("test"))
        tracker.updateState(InProcess("test"))
        tracker.getResultFor(atMost)("test").awaitResult.forceResult === Executed("test")
      }
    }

  "An OperationStateTracker updated to an NotExecuted state and then to an InProcess state when queried with 'getResultFor'" should 
    "succeed" in {
      withTrackerInTestContext { tracker =>
        tracker.updateState(NotExecuted("test", UnspecifiedProblem("")))
        tracker.updateState(InProcess("test"))
        tracker.getResultFor(atMost)("test").awaitResult.isSuccess
      }
    }
    it should "succeed with a NotExecuted" in {
      withTrackerInTestContext { tracker =>
        tracker.updateState(NotExecuted("test", UnspecifiedProblem("")))
        tracker.updateState(InProcess("test"))
        tracker.getResultFor(atMost)("test").awaitResult.forceResult === NotExecuted("test", UnspecifiedProblem(""))
      }
    }
  
//    "fail on getResultFor" in {
//      withTrackerInFakeContext { tracker =>
//        tracker.updateState(InProcess("test"))
//        tracker.getResultFor("test").awaitResult.isFailure
//      }
//    }
//    "fail on getResultFor" in {
//      withTrackerInFakeContext { tracker =>
//        tracker.updateState(InProcess("test"))
//        tracker.getResultFor("test").awaitResult.isFailure
//      }
//    }
//    "return an Executed state after updated to Executed" in {
//      withTrackerInFakeContext { tracker =>
//        tracker.updateState(Executed("test"))
//        tracker.queryStateFor("test").awaitResult.forceResult === Some(Executed("test"))
//      }
//    }
//    "return a NotExecuted state after updated to NotExecuted" in {
//      withTrackerInFakeContext { tracker =>
//        tracker.updateState(NotExecuted("test", UnspecifiedProblem("")))
//        tracker.queryStateFor("test").awaitResult.forceResult === Some(NotExecuted("test", UnspecifiedProblem("")))
//      }
//    }
//    "return an Executed state after updated to an InProcess and then an Executed" in {
//      withTrackerInFakeContext { tracker =>
//        tracker.updateState(InProcess("test"))
//        tracker.updateState(Executed("test"))
//        tracker.queryStateFor("test").awaitResult.forceResult === Some(Executed("test"))
//      }
//    }
//    "return an NotExecuted state after updated to an InProcess and then an NotExecuted" in {
//      withTrackerInFakeContext { tracker =>
//        tracker.updateState(InProcess("test"))
//        tracker.updateState(NotExecuted("test", UnspecifiedProblem("")))
//        tracker.queryStateFor("test").awaitResult.forceResult === Some(NotExecuted("test", UnspecifiedProblem("")))
//      }
//    }
//    
//  }
}