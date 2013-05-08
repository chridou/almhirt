package almhirt.util

import java.util.{UUID => JUUID}
import org.scalatest._
import org.scalatest.matchers.ShouldMatchers
import scala.concurrent.duration.Duration
import almhirt.common._
import almhirt.syntax.almvalidation._
import almhirt.environment._
import org.joda.time.DateTime
import almhirt.commanding.DomainCommand

class OperationStateTrackerSpecs extends FlatSpec with ShouldMatchers with AlmhirtTestKit {
  private implicit val atMost = Duration(2, "s")
  implicit val canCreateDateTimes = new CanCreateDateTime { override val getDateTime = DateTime.now }
  def withTrackerInTestContext[T](compute: OperationStateTracker => T) =
    inExtendedTestAlmhirt(createExtendedBootStrapper()) { implicit ctx =>
      val tracker = ctx.operationStateTracker
      compute(tracker)
    }
  
  object DummyCommand extends DomainCommand { val id = JUUID.randomUUID(); val aggRef = None }
  val dummyInfo = CommandInfo(DummyCommand)

  "An OperationStateTracker" should
    "accept an InProgress state" in {
      withTrackerInTestContext { tracker =>
        tracker.updateState(InProcess("test", dummyInfo))
      }
    }
  it should "accept an Executed state" in {
    withTrackerInTestContext { tracker =>
      tracker.updateState(Executed("test", PerformedNoAction("")))
    }
  }
  it should "accept an NotExecuted state" in {
    withTrackerInTestContext { tracker =>
      tracker.updateState(NotExecuted("test", UnspecifiedProblem("")))
    }
  }

  "An OperationStateTracker updated to an InProcess state when queried with 'queryStateFor'" should
    "return an Some(InProcess) with a HeadCommandInfo" in {
      withTrackerInTestContext { tracker =>
        tracker.updateState(InProcess("test", dummyInfo))
        tracker.queryStateFor("test").awaitResult.forceResult should equal(Some(InProcess("test", dummyInfo.toHeadCommandInfo)))
      }
    }
  it should "return an Some(InProcess) with a HeadCommandInfo even when is InProcess is submitted more than once" in {
    withTrackerInTestContext { tracker =>
      tracker.updateState(InProcess("test", dummyInfo))
      tracker.updateState(InProcess("test", dummyInfo))
      tracker.queryStateFor("test").awaitResult.forceResult should equal(Some(InProcess("test", dummyInfo.toHeadCommandInfo)))
    }
  }
  it should "return a Some(Executed) when updated to Executed" in {
    withTrackerInTestContext { tracker =>
      tracker.updateState(InProcess("test", dummyInfo))
      tracker.updateState(Executed("test", PerformedNoAction("")))
      tracker.queryStateFor("test").awaitResult.forceResult should equal(Some(Executed("test", PerformedNoAction(""))))
    }
  }
  it should "return a Some(NotExecuted) when updated to NotExecuted" in {
    withTrackerInTestContext { tracker =>
      tracker.updateState(InProcess("test", dummyInfo))
      tracker.updateState(NotExecuted("test", UnspecifiedProblem("")))
      tracker.queryStateFor("test").awaitResult.forceResult should equal(Some(NotExecuted("test", UnspecifiedProblem(""))))
    }
  }

  "An OperationStateTracker updated to an Executed state when queried with 'queryStateFor'" should
    "return an Some(Executed)" in {
      withTrackerInTestContext { tracker =>
        tracker.updateState(Executed("test", PerformedNoAction("")))
        tracker.queryStateFor("test").awaitResult.forceResult should equal(Some(Executed("test", PerformedNoAction(""))))
      }
    }
  it should "return an Some(Executed) even when is Executed is submitted more than once" in {
    withTrackerInTestContext { tracker =>
      tracker.updateState(Executed("test", PerformedNoAction("")))
      tracker.updateState(Executed("test", PerformedNoAction("")))
      tracker.queryStateFor("test").awaitResult.forceResult should equal(Some(Executed("test", PerformedNoAction(""))))
    }
  }
  it should "return a Some(Executed) when updated to NotExecuted" in {
    withTrackerInTestContext { tracker =>
      tracker.updateState(Executed("test", PerformedNoAction("")))
      tracker.updateState(NotExecuted("test", UnspecifiedProblem("")))
      tracker.queryStateFor("test").awaitResult.forceResult should equal(Some(Executed("test", PerformedNoAction(""))))
    }
  }
  it should "return a Some(Executed) when updated to InProcess" in {
    withTrackerInTestContext { tracker =>
      tracker.updateState(Executed("test", PerformedNoAction("")))
      tracker.updateState(InProcess("test", dummyInfo))
      tracker.queryStateFor("test").awaitResult.forceResult should equal(Some(Executed("test", PerformedNoAction(""))))
    }
  }

  "An OperationStateTracker updated to a NotExecuted state when queried with 'queryStateFor'" should
    "return an Some(NotExecuted)" in {
      withTrackerInTestContext { tracker =>
        tracker.updateState(NotExecuted("test", UnspecifiedProblem("")))
        tracker.queryStateFor("test").awaitResult.forceResult should equal(Some(NotExecuted("test", UnspecifiedProblem(""))))
      }
    }
  it should "return an Some(NotExecuted) even when is NotExecuted is submitted more than once" in {
    withTrackerInTestContext { tracker =>
      tracker.updateState(NotExecuted("test", UnspecifiedProblem("")))
      tracker.updateState(NotExecuted("test", UnspecifiedProblem("")))
      tracker.queryStateFor("test").awaitResult.forceResult should equal(Some(NotExecuted("test", UnspecifiedProblem(""))))
    }
  }
  it should "return a Some(NotExecuted) when updated to Executed" in {
    withTrackerInTestContext { tracker =>
      tracker.updateState(NotExecuted("test", UnspecifiedProblem("")))
      tracker.updateState(Executed("test", PerformedNoAction("")))
      tracker.queryStateFor("test").awaitResult.forceResult should equal(Some(NotExecuted("test", UnspecifiedProblem(""))))
    }
  }
  it should "return a Some(NotExecuted) when updated to InProcess" in {
    withTrackerInTestContext { tracker =>
      tracker.updateState(NotExecuted("test", UnspecifiedProblem("")))
      tracker.updateState(InProcess("test", dummyInfo))
      tracker.queryStateFor("test").awaitResult.forceResult should equal(Some(NotExecuted("test", UnspecifiedProblem(""))))
    }
  }

  "An OperationStateTracker updated to an InProcess state when queried with 'getResultFor'" should
    "fail" in {
      withTrackerInTestContext { tracker =>
        tracker.updateState(InProcess("test", dummyInfo))
        tracker.getResultFor("test").awaitResult.isFailure should be(true)
      }
    }
  it should "fail with a timeout" in {
    withTrackerInTestContext { tracker =>
      tracker.updateState(InProcess("test", dummyInfo))
      tracker.getResultFor("test").awaitResult.forceProblem.isInstanceOf[OperationTimedOutProblem] should be(true)
    }
  }

  "An OperationStateTracker updated to an Executed state when queried with 'getResultFor'" should
    "succeed" in {
      withTrackerInTestContext { tracker =>
        tracker.updateState(Executed("test", PerformedNoAction("")))
        tracker.getResultFor("test").awaitResult.isSuccess should be(true)
      }
    }
  it should "succeed with a Executed" in {
    withTrackerInTestContext { tracker =>
      tracker.updateState(Executed("test", PerformedNoAction("")))
      tracker.getResultFor("test").awaitResult.forceResult should equal(Executed("test", PerformedNoAction("")))
    }
  }

  "An OperationStateTracker updated to an NotExecuted state when queried with 'getResultFor'" should
    "succeed" in {
      withTrackerInTestContext { tracker =>
        tracker.updateState(NotExecuted("test", UnspecifiedProblem("")))
        tracker.getResultFor("test").awaitResult.isSuccess
      }
    }
  it should "succeed with a Executed" in {
    withTrackerInTestContext { tracker =>
      tracker.updateState(NotExecuted("test", UnspecifiedProblem("")))
      tracker.getResultFor("test").awaitResult.forceResult === NotExecuted("test", UnspecifiedProblem(""))
    }
  }

  "An OperationStateTracker updated to an InProgress state and then to an Executed state when queried with 'getResultFor'" should
    "succeed" in {
      withTrackerInTestContext { tracker =>
        tracker.updateState(InProcess("test", dummyInfo))
        tracker.updateState(Executed("test", PerformedNoAction("")))
        tracker.getResultFor("test").awaitResult.isSuccess
      }
    }
  it should "succeed with a Executed" in {
    withTrackerInTestContext { tracker =>
      tracker.updateState(InProcess("test", dummyInfo))
      tracker.updateState(Executed("test", PerformedNoAction("")))
      tracker.getResultFor("test").awaitResult.forceResult === Executed("test", PerformedNoAction(""))
    }
  }

  "An OperationStateTracker updated to an InProgress state and then to an NotExecuted state when queried with 'getResultFor'" should
    "succeed" in {
      withTrackerInTestContext { tracker =>
        tracker.updateState(InProcess("test", dummyInfo))
        tracker.updateState(NotExecuted("test", UnspecifiedProblem("")))
        tracker.getResultFor("test").awaitResult.isSuccess
      }
    }
  it should "succeed with a NotExecuted" in {
    withTrackerInTestContext { tracker =>
      tracker.updateState(InProcess("test", dummyInfo))
      tracker.updateState(NotExecuted("test", UnspecifiedProblem("")))
      tracker.getResultFor("test").awaitResult.forceResult === NotExecuted("test", UnspecifiedProblem(""))
    }
  }

  "An OperationStateTracker updated to an Executed state and then to an NotExecuted state when queried with 'getResultFor'" should
    "succeed" in {
      withTrackerInTestContext { tracker =>
        tracker.updateState(Executed("test", PerformedNoAction("")))
        tracker.updateState(NotExecuted("test", UnspecifiedProblem("")))
        tracker.getResultFor("test").awaitResult.isSuccess
      }
    }
  it should "succeed with a Executed" in {
    withTrackerInTestContext { tracker =>
      tracker.updateState(Executed("test", PerformedNoAction("")))
      tracker.updateState(NotExecuted("test", UnspecifiedProblem("")))
      tracker.getResultFor("test").awaitResult.forceResult === Executed("test", PerformedNoAction(""))
    }
  }

  "An OperationStateTracker updated to an NotExecuted state and then to an Executed state when queried with 'getResultFor'" should
    "succeed" in {
      withTrackerInTestContext { tracker =>
        tracker.updateState(NotExecuted("test", UnspecifiedProblem("")))
        tracker.updateState(Executed("test", PerformedNoAction("")))
        tracker.getResultFor("test").awaitResult.isSuccess
      }
    }
  it should "succeed with a NotExecuted" in {
    withTrackerInTestContext { tracker =>
      tracker.updateState(NotExecuted("test", UnspecifiedProblem("")))
      tracker.updateState(Executed("test", PerformedNoAction("")))
      tracker.getResultFor("test").awaitResult.forceResult === NotExecuted("test", UnspecifiedProblem(""))
    }
  }

  "An OperationStateTracker updated to an Executed state and then to an InProcess state when queried with 'getResultFor'" should
    "succeed" in {
      withTrackerInTestContext { tracker =>
        tracker.updateState(Executed("test", PerformedNoAction("")))
        tracker.updateState(InProcess("test", dummyInfo))
        tracker.getResultFor("test").awaitResult.isSuccess
      }
    }
  it should "succeed with a Executed" in {
    withTrackerInTestContext { tracker =>
      tracker.updateState(Executed("test", PerformedNoAction("")))
      tracker.updateState(InProcess("test", dummyInfo))
      tracker.getResultFor("test").awaitResult.forceResult === Executed("test", PerformedNoAction(""))
    }
  }

  "An OperationStateTracker updated to an NotExecuted state and then to an InProcess state when queried with 'getResultFor'" should
    "succeed" in {
      withTrackerInTestContext { tracker =>
        tracker.updateState(NotExecuted("test", UnspecifiedProblem("")))
        tracker.updateState(InProcess("test", dummyInfo))
        tracker.getResultFor("test").awaitResult.isSuccess
      }
    }
  it should "succeed with a NotExecuted" in {
    withTrackerInTestContext { tracker =>
      tracker.updateState(NotExecuted("test", UnspecifiedProblem("")))
      tracker.updateState(InProcess("test", dummyInfo))
      tracker.getResultFor("test").awaitResult.forceResult === NotExecuted("test", UnspecifiedProblem(""))
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