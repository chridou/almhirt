package almhirt.util

import org.specs2.mutable._
import almhirt._
import almhirt.syntax.almvalidation._
import almhirt.environment._
import test._

class OperationStateTrackerSpecs extends Specification with AlmhirtContextTestKit {
  private implicit val atMost = akka.util.Duration(2, "s")
  def withTrackerInFakeContext[T](compute: OperationStateTracker => T) =
      inTestContext { implicit ctx =>
        val tracker = OperationStateTracker()
        compute(tracker)
      }
  
  "An OperationStateTracker" should {
    "accept an InProgress state" in {
      withTrackerInFakeContext { tracker =>
        tracker.updateState(InProcess("test"))
        true
      }
    }
    "accept an Executed state" in {
      withTrackerInFakeContext { tracker =>
        tracker.updateState(Executed("test"))
        true
      }
    }
    "accept an NotExecuted state" in {
      withTrackerInFakeContext { tracker =>
        tracker.updateState(NotExecuted("test", UnspecifiedProblem("")))
        true
      }
    }
  }

  "An OperationStateTracker updated to an InProcess state when queried with 'queryStateFor'" should {
    "return an Some(InProcess)" in {
      withTrackerInFakeContext { tracker =>
        tracker.updateState(InProcess("test"))
        tracker.queryStateFor("test").awaitResult.forceResult === Some(InProcess("test"))
      }
    }
    "return an Some(InProcess) even when is InProcess is submitted more than once" in {
      withTrackerInFakeContext { tracker =>
        tracker.updateState(InProcess("test"))
        tracker.updateState(InProcess("test"))
        tracker.queryStateFor("test").awaitResult.forceResult === Some(InProcess("test"))
      }
    }
    "return a Some(Executed) when updated to Executed" in {
      withTrackerInFakeContext { tracker =>
        tracker.updateState(InProcess("test"))
        tracker.updateState(Executed("test"))
        tracker.queryStateFor("test").awaitResult.forceResult === Some(Executed("test"))
      }
    }
    "return a Some(NotExecuted) when updated to NotExecuted" in {
      withTrackerInFakeContext { tracker =>
        tracker.updateState(InProcess("test"))
        tracker.updateState(NotExecuted("test", UnspecifiedProblem("")))
        tracker.queryStateFor("test").awaitResult.forceResult === Some(NotExecuted("test", UnspecifiedProblem("")))
      }
    }
  }

  "An OperationStateTracker updated to an Executed state when queried with 'queryStateFor'" should {
    "return an Some(Executed)" in {
      withTrackerInFakeContext { tracker =>
        tracker.updateState(Executed("test"))
        tracker.queryStateFor("test").awaitResult.forceResult === Some(Executed("test"))
      }
    }
    "return an Some(Executed) even when is Executed is submitted more than once" in {
      withTrackerInFakeContext { tracker =>
        tracker.updateState(Executed("test"))
        tracker.updateState(Executed("test"))
        tracker.queryStateFor("test").awaitResult.forceResult === Some(Executed("test"))
      }
    }
    "return a Some(Executed) when updated to NotExecuted" in {
      withTrackerInFakeContext { tracker =>
        tracker.updateState(Executed("test"))
        tracker.updateState(NotExecuted("test", UnspecifiedProblem("")))
        tracker.queryStateFor("test").awaitResult.forceResult === Some(Executed("test"))
      }
    }
    "return a Some(Executed) when updated to InProcess" in {
      withTrackerInFakeContext { tracker =>
        tracker.updateState(Executed("test"))
        tracker.updateState(InProcess("test"))
        tracker.queryStateFor("test").awaitResult.forceResult === Some(Executed("test"))
      }
    }
  }
  
  "An OperationStateTracker updated to a NotExecuted state when queried with 'queryStateFor'" should {
    "return an Some(NotExecuted)" in {
      withTrackerInFakeContext { tracker =>
        tracker.updateState(NotExecuted("test", UnspecifiedProblem("")))
        tracker.queryStateFor("test").awaitResult.forceResult === Some(NotExecuted("test", UnspecifiedProblem("")))
      }
    }
    "return an Some(NotExecuted) even when is NotExecuted is submitted more than once" in {
      withTrackerInFakeContext { tracker =>
        tracker.updateState(NotExecuted("test", UnspecifiedProblem("")))
        tracker.updateState(NotExecuted("test", UnspecifiedProblem("")))
        tracker.queryStateFor("test").awaitResult.forceResult === Some(NotExecuted("test", UnspecifiedProblem("")))
      }
    }
    "return a Some(NotExecuted) when updated to Executed" in {
      withTrackerInFakeContext { tracker =>
        tracker.updateState(NotExecuted("test", UnspecifiedProblem("")))
        tracker.updateState(Executed("test"))
        tracker.queryStateFor("test").awaitResult.forceResult === Some(NotExecuted("test", UnspecifiedProblem("")))
      }
    }
    "return a Some(NotExecuted) when updated to InProcess" in {
      withTrackerInFakeContext { tracker =>
        tracker.updateState(NotExecuted("test", UnspecifiedProblem("")))
        tracker.updateState(InProcess("test"))
        tracker.queryStateFor("test").awaitResult.forceResult === Some(NotExecuted("test", UnspecifiedProblem("")))
      }
    }
  }

  "An OperationStateTracker updated to an InProcess state when queried with 'getResultFor'" should {
    "fail" in {
      withTrackerInFakeContext { tracker =>
        tracker.updateState(InProcess("test"))
        tracker.getResultFor("test").awaitResult.isFailure
      }
    }
    "fail with a timeout" in {
      withTrackerInFakeContext { tracker =>
        tracker.updateState(InProcess("test"))
        tracker.getResultFor("test").awaitResult.forceProblem.isInstanceOf[OperationTimedOutProblem]
      }
    }
  }

  "An OperationStateTracker updated to an Executed state when queried with 'getResultFor'" should {
    "succeed" in {
      withTrackerInFakeContext { tracker =>
        tracker.updateState(Executed("test"))
        tracker.getResultFor("test").awaitResult.isSuccess
      }
    }
    "succeed with a Executed" in {
      withTrackerInFakeContext { tracker =>
        tracker.updateState(Executed("test"))
        tracker.getResultFor("test").awaitResult.forceResult === Executed("test")
      }
    }
  }

  "An OperationStateTracker updated to an NotExecuted state when queried with 'getResultFor'" should {
    "succeed" in {
      withTrackerInFakeContext { tracker =>
        tracker.updateState(NotExecuted("test", UnspecifiedProblem("")))
        tracker.getResultFor("test").awaitResult.isSuccess
      }
    }
    "succeed with a Executed" in {
      withTrackerInFakeContext { tracker =>
        tracker.updateState(NotExecuted("test", UnspecifiedProblem("")))
        tracker.getResultFor("test").awaitResult.forceResult === NotExecuted("test", UnspecifiedProblem(""))
      }
    }
  }

  "An OperationStateTracker updated to an InProgress state and then to an Executed state when queried with 'getResultFor'" should {
    "succeed" in {
      withTrackerInFakeContext { tracker =>
        tracker.updateState(InProcess("test"))
        tracker.updateState(Executed("test"))
        tracker.getResultFor("test").awaitResult.isSuccess
      }
    }
    "succeed with a Executed" in {
      withTrackerInFakeContext { tracker =>
        tracker.updateState(InProcess("test"))
        tracker.updateState(Executed("test"))
        tracker.getResultFor("test").awaitResult.forceResult === Executed("test")
      }
    }
  }

  "An OperationStateTracker updated to an InProgress state and then to an NotExecuted state when queried with 'getResultFor'" should {
    "succeed" in {
      withTrackerInFakeContext { tracker =>
        tracker.updateState(InProcess("test"))
        tracker.updateState(NotExecuted("test", UnspecifiedProblem("")))
        tracker.getResultFor("test").awaitResult.isSuccess
      }
    }
    "succeed with a NotExecuted" in {
      withTrackerInFakeContext { tracker =>
        tracker.updateState(InProcess("test"))
        tracker.updateState(NotExecuted("test", UnspecifiedProblem("")))
        tracker.getResultFor("test").awaitResult.forceResult === NotExecuted("test", UnspecifiedProblem(""))
      }
    }
  }

  "An OperationStateTracker updated to an Executed state and then to an NotExecuted state when queried with 'getResultFor'" should {
    "succeed" in {
      withTrackerInFakeContext { tracker =>
        tracker.updateState(Executed("test"))
        tracker.updateState(NotExecuted("test", UnspecifiedProblem("")))
        tracker.getResultFor("test").awaitResult.isSuccess
      }
    }
    "succeed with a Executed" in {
      withTrackerInFakeContext { tracker =>
        tracker.updateState(Executed("test"))
        tracker.updateState(NotExecuted("test", UnspecifiedProblem("")))
        tracker.getResultFor("test").awaitResult.forceResult === Executed("test")
      }
    }
  }

  "An OperationStateTracker updated to an NotExecuted state and then to an Executed state when queried with 'getResultFor'" should {
    "succeed" in {
      withTrackerInFakeContext { tracker =>
        tracker.updateState(NotExecuted("test", UnspecifiedProblem("")))
        tracker.updateState(Executed("test"))
        tracker.getResultFor("test").awaitResult.isSuccess
      }
    }
    "succeed with a NotExecuted" in {
      withTrackerInFakeContext { tracker =>
        tracker.updateState(NotExecuted("test", UnspecifiedProblem("")))
        tracker.updateState(Executed("test"))
        tracker.getResultFor("test").awaitResult.forceResult === NotExecuted("test", UnspecifiedProblem(""))
      }
    }
  }

  "An OperationStateTracker updated to an Executed state and then to an InProcess state when queried with 'getResultFor'" should {
    "succeed" in {
      withTrackerInFakeContext { tracker =>
        tracker.updateState(Executed("test"))
        tracker.updateState(InProcess("test"))
        tracker.getResultFor("test").awaitResult.isSuccess
      }
    }
    "succeed with a Executed" in {
      withTrackerInFakeContext { tracker =>
        tracker.updateState(Executed("test"))
        tracker.updateState(InProcess("test"))
        tracker.getResultFor("test").awaitResult.forceResult === Executed("test")
      }
    }
  }

  "An OperationStateTracker updated to an NotExecuted state and then to an InProcess state when queried with 'getResultFor'" should {
    "succeed" in {
      withTrackerInFakeContext { tracker =>
        tracker.updateState(NotExecuted("test", UnspecifiedProblem("")))
        tracker.updateState(InProcess("test"))
        tracker.getResultFor("test").awaitResult.isSuccess
      }
    }
    "succeed with a NotExecuted" in {
      withTrackerInFakeContext { tracker =>
        tracker.updateState(NotExecuted("test", UnspecifiedProblem("")))
        tracker.updateState(InProcess("test"))
        tracker.getResultFor("test").awaitResult.forceResult === NotExecuted("test", UnspecifiedProblem(""))
      }
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