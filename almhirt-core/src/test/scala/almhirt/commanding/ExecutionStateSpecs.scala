package almhirt.commanding

import org.scalatest.FunSpec
import org.scalatest.matchers.ShouldMatchers
import almhirt.commanding._
import almhirt.core.types._
import org.joda.time.LocalDateTime
import almhirt.problem.Problem
import almhirt.problem.problemtypes.UnspecifiedProblem

class ExecutionStateSpecs extends FunSpec with ShouldMatchers{
  val executionState = ExecutionState
  val problem = Problem("failedProblem", UnspecifiedProblem)
  val today = new LocalDateTime()
  val yesterday = today.minusDays(1)
  val tomorrow = today.plusDays(1)
  
  val startedToday = ExecutionStarted("StartedToday",today, Map.empty)
  val inProcessToday = ExecutionInProcess("InProcessToday",today, Map.empty)
  val successfulToday = ExecutionSuccessful("SuccessfulToday","Success", today, Map.empty)
  val failedToday = ExecutionFailed("FailedToday", problem,today, Map.empty)
  
  val startedYesterday = ExecutionStarted("StartedYesterday",yesterday, Map.empty)
  val inProcessYesterday = ExecutionInProcess("InProcessYesterday",yesterday, Map.empty)
  val successfulYesterday = ExecutionSuccessful("SuccessfulYesterday","Success", yesterday, Map.empty)
  val failedYesterday = ExecutionFailed("FailedYesterday", problem,yesterday, Map.empty)

  val startedTomorrow = ExecutionStarted("StartedTomorrow",tomorrow, Map.empty)
  val inProcessTomorrow = ExecutionInProcess("InProcessTomorrow",tomorrow, Map.empty)
  val successfulTomorrow = ExecutionSuccessful("SuccessfulTomorrow","Success", tomorrow, Map.empty)
  val failedTomorrow = ExecutionFailed("FailedTomorrow", problem,tomorrow, Map.empty)

  describe("""ExecutionStateStarted today compared to"""){
    it("""ExecutionStateStarted today"""){
      executionState.compareExecutionState(startedToday, startedToday) should be(0)
    }
    it("""ExecutionStateStarted yesterday"""){
      executionState.compareExecutionState(startedToday, startedYesterday) should be(0)
    }
    it("""ExecutionStateStarted tomorrow"""){
      executionState.compareExecutionState(startedToday, startedTomorrow) should be(0)
    }
    it("""ExecutionStateInProcess today"""){
      executionState.compareExecutionState(startedToday, inProcessToday) should be(-1)
    }
    it("""ExecutionStateInProcess yesterday"""){
      executionState.compareExecutionState(startedToday, inProcessYesterday) should be(-1)
    }
    it("""ExecutionStateInProcess tomorrow"""){
      executionState.compareExecutionState(startedToday, inProcessTomorrow) should be(-1)
    }
    it("""ExecutionStateSuccessful today"""){
      executionState.compareExecutionState(startedToday, successfulToday) should be(-1)
    }
    it("""ExecutionStateSuccessful yesterday"""){
      executionState.compareExecutionState(startedToday, successfulYesterday) should be(-1)
    }
    it("""ExecutionStateSuccessful tomorrow"""){
      executionState.compareExecutionState(startedToday, successfulTomorrow) should be(-1)
    }
    it("""ExecutionStateFailed today"""){
      executionState.compareExecutionState(startedToday, failedToday) should be(-1)
    }
    it("""ExecutionStateFailed yesterday"""){
      executionState.compareExecutionState(startedToday, failedYesterday) should be(-1)
    }
    it("""ExecutionStateFailed tomorrow"""){
      executionState.compareExecutionState(startedToday, failedTomorrow) should be(-1)
    }
  }
  
  describe("""ExecutionStateStarted yesterday compared to"""){
    it("""ExecutionStateStarted today"""){
      executionState.compareExecutionState(startedYesterday, startedToday) should be(0)
    }
    it("""ExecutionStateStarted yesterday"""){
      executionState.compareExecutionState(startedYesterday, startedYesterday) should be(0)
    }
    it("""ExecutionStateStarted tomorrow"""){
      executionState.compareExecutionState(startedYesterday, startedTomorrow) should be(0)
    }
    it("""ExecutionStateInProcess today"""){
      executionState.compareExecutionState(startedYesterday, inProcessToday) should be(-1)
    }
    it("""ExecutionStateInProcess yesterday"""){
      executionState.compareExecutionState(startedYesterday, inProcessYesterday) should be(-1)
    }
    it("""ExecutionStateInProcess tomorrow"""){
      executionState.compareExecutionState(startedYesterday, inProcessTomorrow) should be(-1)
    }
    it("""ExecutionStateSuccessful today"""){
      executionState.compareExecutionState(startedYesterday, successfulToday) should be(-1)
    }
    it("""ExecutionStateSuccessful yesterday"""){
      executionState.compareExecutionState(startedYesterday, successfulYesterday) should be(-1)
    }
    it("""ExecutionStateSuccessful tomorrow"""){
      executionState.compareExecutionState(startedYesterday, successfulTomorrow) should be(-1)
    }
    it("""ExecutionStateFailed today"""){
      executionState.compareExecutionState(startedYesterday, failedToday) should be(-1)
    }
    it("""ExecutionStateFailed yesterday"""){
      executionState.compareExecutionState(startedYesterday, failedYesterday) should be(-1)
    }
    it("""ExecutionStateFailed tomorrow"""){
      executionState.compareExecutionState(startedYesterday, failedTomorrow) should be(-1)
    }
  }
  
  describe("""ExecutionStateStarted tomorrow compared to"""){
    it("""ExecutionStateStarted today"""){
      executionState.compareExecutionState(startedTomorrow, startedToday) should be(0)
    }
    it("""ExecutionStateStarted yesterday"""){
      executionState.compareExecutionState(startedTomorrow, startedYesterday) should be(0)
    }
    it("""ExecutionStateStarted tomorrow"""){
      executionState.compareExecutionState(startedTomorrow, startedTomorrow) should be(0)
    }
    it("""ExecutionStateInProcess today"""){
      executionState.compareExecutionState(startedTomorrow, inProcessToday) should be(-1)
    }
    it("""ExecutionStateInProcess yesterday"""){
      executionState.compareExecutionState(startedTomorrow, inProcessYesterday) should be(-1)
    }
    it("""ExecutionStateInProcess tomorrow"""){
      executionState.compareExecutionState(startedTomorrow, inProcessTomorrow) should be(-1)
    }
    it("""ExecutionStateSuccessful today"""){
      executionState.compareExecutionState(startedTomorrow, successfulToday) should be(-1)
    }
    it("""ExecutionStateSuccessful yesterday"""){
      executionState.compareExecutionState(startedTomorrow, successfulYesterday) should be(-1)
    }
    it("""ExecutionStateSuccessful tomorrow"""){
      executionState.compareExecutionState(startedTomorrow, successfulTomorrow) should be(-1)
    }
    it("""ExecutionStateFailed today"""){
      executionState.compareExecutionState(startedTomorrow, failedToday) should be(-1)
    }
    it("""ExecutionStateFailed yesterday"""){
      executionState.compareExecutionState(startedTomorrow, failedYesterday) should be(-1)
    }
    it("""ExecutionStateFailed tomorrow"""){
      executionState.compareExecutionState(startedTomorrow, failedTomorrow) should be(-1)
    }
  }
  
  describe("""ExecutionStateInProcess today compared to"""){
    it("""ExecutionStateStarted today"""){
      executionState.compareExecutionState(inProcessToday, startedToday) should be(1)
    }
    it("""ExecutionStateStarted yesterday"""){
      executionState.compareExecutionState(inProcessToday, startedYesterday) should be(1)
    }
    it("""ExecutionStateStarted tomorrow"""){
      executionState.compareExecutionState(inProcessToday, startedTomorrow) should be(1)
    }
    it("""ExecutionStateInProcess today"""){
      executionState.compareExecutionState(inProcessToday, inProcessToday) should be(0)
    }
    it("""ExecutionStateInProcess yesterday"""){
      executionState.compareExecutionState(inProcessToday, inProcessYesterday) should be(1)
    }
    it("""ExecutionStateInProcess tomorrow"""){
      executionState.compareExecutionState(inProcessToday, inProcessTomorrow) should be(-1)
    }
    it("""ExecutionStateSuccessful today"""){
      executionState.compareExecutionState(inProcessToday, successfulToday) should be(-1)
    }
    it("""ExecutionStateSuccessful yesterday"""){
      executionState.compareExecutionState(inProcessToday, successfulYesterday) should be(-1)
    }
    it("""ExecutionStateSuccessful tomorrow"""){
      executionState.compareExecutionState(inProcessToday, successfulTomorrow) should be(-1)
    }
    it("""ExecutionStateFailed today"""){
      executionState.compareExecutionState(inProcessToday, failedToday) should be(-1)
    }
    it("""ExecutionStateFailed yesterday"""){
      executionState.compareExecutionState(inProcessToday, failedYesterday) should be(-1)
    }
    it("""ExecutionStateFailed tomorrow"""){
      executionState.compareExecutionState(inProcessToday, failedTomorrow) should be(-1)
    }
  }
  
  describe("""ExecutionStateInProcess yesterday compared to"""){
    it("""ExecutionStateStarted today"""){
      executionState.compareExecutionState(inProcessYesterday, startedToday) should be(1)
    }
    it("""ExecutionStateStarted yesterday"""){
      executionState.compareExecutionState(inProcessYesterday, startedYesterday) should be(1)
    }
    it("""ExecutionStateStarted tomorrow"""){
      executionState.compareExecutionState(inProcessYesterday, startedTomorrow) should be(1)
    }
    it("""ExecutionStateInProcess today"""){
      executionState.compareExecutionState(inProcessYesterday, inProcessToday) should be(-1)
    }
    it("""ExecutionStateInProcess yesterday"""){
      executionState.compareExecutionState(inProcessYesterday, inProcessYesterday) should be(0)
    }
    it("""ExecutionStateInProcess tomorrow"""){
      executionState.compareExecutionState(inProcessYesterday, inProcessTomorrow) should be(-1)
    }
    it("""ExecutionStateSuccessful today"""){
      executionState.compareExecutionState(inProcessYesterday, successfulToday) should be(-1)
    }
    it("""ExecutionStateSuccessful yesterday"""){
      executionState.compareExecutionState(inProcessYesterday, successfulYesterday) should be(-1)
    }
    it("""ExecutionStateSuccessful tomorrow"""){
      executionState.compareExecutionState(inProcessYesterday, successfulTomorrow) should be(-1)
    }
    it("""ExecutionStateFailed today"""){
      executionState.compareExecutionState(inProcessYesterday, failedToday) should be(-1)
    }
    it("""ExecutionStateFailed yesterday"""){
      executionState.compareExecutionState(inProcessYesterday, failedYesterday) should be(-1)
    }
    it("""ExecutionStateFailed tomorrow"""){
      executionState.compareExecutionState(inProcessYesterday, failedTomorrow) should be(-1)
    }
  }
  
  describe("""ExecutionStateInProcess tomorrow compared to"""){
    it("""ExecutionStateStarted today"""){
      executionState.compareExecutionState(inProcessTomorrow, startedToday) should be(1)
    }
    it("""ExecutionStateStarted yesterday"""){
      executionState.compareExecutionState(inProcessTomorrow, startedYesterday) should be(1)
    }
    it("""ExecutionStateStarted tomorrow"""){
      executionState.compareExecutionState(inProcessTomorrow, startedTomorrow) should be(1)
    }
    it("""ExecutionStateInProcess today"""){
      executionState.compareExecutionState(inProcessTomorrow, inProcessToday) should be(1)
    }
    it("""ExecutionStateInProcess yesterday"""){
      executionState.compareExecutionState(inProcessTomorrow, inProcessYesterday) should be(1)
    }
    it("""ExecutionStateInProcess tomorrow"""){
      executionState.compareExecutionState(inProcessTomorrow, inProcessTomorrow) should be(0)
    }
    it("""ExecutionStateSuccessful today"""){
      executionState.compareExecutionState(inProcessTomorrow, successfulToday) should be(-1)
    }
    it("""ExecutionStateSuccessful yesterday"""){
      executionState.compareExecutionState(inProcessTomorrow, successfulYesterday) should be(-1)
    }
    it("""ExecutionStateSuccessful tomorrow"""){
      executionState.compareExecutionState(inProcessTomorrow, successfulTomorrow) should be(-1)
    }
    it("""ExecutionStateFailed today"""){
      executionState.compareExecutionState(inProcessTomorrow, failedToday) should be(-1)
    }
    it("""ExecutionStateFailed yesterday"""){
      executionState.compareExecutionState(inProcessTomorrow, failedYesterday) should be(-1)
    }
    it("""ExecutionStateFailed tomorrow"""){
      executionState.compareExecutionState(inProcessTomorrow, failedTomorrow) should be(-1)
    }
  }
  
  describe("""ExecutionStateSuccesful today compared to"""){
    it("""ExecutionStateStarted today"""){
      executionState.compareExecutionState(successfulToday, startedToday) should be(1)
    }
    it("""ExecutionStateStarted yesterday"""){
      executionState.compareExecutionState(successfulToday, startedYesterday) should be(1)
    }
    it("""ExecutionStateStarted tomorrow"""){
      executionState.compareExecutionState(successfulToday, startedTomorrow) should be(1)
    }
    it("""ExecutionStateInProcess today"""){
      executionState.compareExecutionState(successfulToday, inProcessToday) should be(1)
    }
    it("""ExecutionStateInProcess yesterday"""){
      executionState.compareExecutionState(successfulToday, inProcessYesterday) should be(1)
    }
    it("""ExecutionStateInProcess tomorrow"""){
      executionState.compareExecutionState(successfulToday, inProcessTomorrow) should be(1)
    }
    it("""ExecutionStateSuccessful today"""){
      executionState.compareExecutionState(successfulToday, successfulToday) should be(0)
    }
    it("""ExecutionStateSuccessful yesterday"""){
      executionState.compareExecutionState(successfulToday, successfulYesterday) should be(0)
    }
    it("""ExecutionStateSuccessful tomorrow"""){
      executionState.compareExecutionState(successfulToday, successfulTomorrow) should be(0)
    }
    it("""ExecutionStateFailed today"""){
      executionState.compareExecutionState(successfulToday, failedToday) should be(0)
    }
    it("""ExecutionStateFailed yesterday"""){
      executionState.compareExecutionState(successfulToday, failedYesterday) should be(0)
    }
    it("""ExecutionStateFailed tomorrow"""){
      executionState.compareExecutionState(successfulToday, failedTomorrow) should be(0)
    }
  }
  
  describe("""ExecutionStateSuccesful yesterday compared to"""){
    it("""ExecutionStateStarted today"""){
      executionState.compareExecutionState(successfulYesterday, startedToday) should be(1)
    }
    it("""ExecutionStateStarted yesterday"""){
      executionState.compareExecutionState(successfulYesterday, startedYesterday) should be(1)
    }
    it("""ExecutionStateStarted tomorrow"""){
      executionState.compareExecutionState(successfulYesterday, startedTomorrow) should be(1)
    }
    it("""ExecutionStateInProcess today"""){
      executionState.compareExecutionState(successfulYesterday, inProcessToday) should be(1)
    }
    it("""ExecutionStateInProcess yesterday"""){
      executionState.compareExecutionState(successfulYesterday, inProcessYesterday) should be(1)
    }
    it("""ExecutionStateInProcess tomorrow"""){
      executionState.compareExecutionState(successfulYesterday, inProcessTomorrow) should be(1)
    }
    it("""ExecutionStateSuccessful today"""){
      executionState.compareExecutionState(successfulYesterday, successfulToday) should be(0)
    }
    it("""ExecutionStateSuccessful yesterday"""){
      executionState.compareExecutionState(successfulYesterday, successfulYesterday) should be(0)
    }
    it("""ExecutionStateSuccessful tomorrow"""){
      executionState.compareExecutionState(successfulYesterday, successfulTomorrow) should be(0)
    }
    it("""ExecutionStateFailed today"""){
      executionState.compareExecutionState(successfulYesterday, failedToday) should be(0)
    }
    it("""ExecutionStateFailed yesterday"""){
      executionState.compareExecutionState(successfulYesterday, failedYesterday) should be(0)
    }
    it("""ExecutionStateFailed tomorrow"""){
      executionState.compareExecutionState(successfulYesterday, failedTomorrow) should be(0)
    }
  }
  
  describe("""ExecutionStateSuccesful tomorrow compared to"""){
    it("""ExecutionStateStarted today"""){
      executionState.compareExecutionState(successfulTomorrow, startedToday) should be(1)
    }
    it("""ExecutionStateStarted yesterday"""){
      executionState.compareExecutionState(successfulTomorrow, startedYesterday) should be(1)
    }
    it("""ExecutionStateStarted tomorrow"""){
      executionState.compareExecutionState(successfulTomorrow, startedTomorrow) should be(1)
    }
    it("""ExecutionStateInProcess today"""){
      executionState.compareExecutionState(successfulTomorrow, inProcessToday) should be(1)
    }
    it("""ExecutionStateInProcess yesterday"""){
      executionState.compareExecutionState(successfulTomorrow, inProcessYesterday) should be(1)
    }
    it("""ExecutionStateInProcess tomorrow"""){
      executionState.compareExecutionState(successfulTomorrow, inProcessTomorrow) should be(1)
    }
    it("""ExecutionStateSuccessful today"""){
      executionState.compareExecutionState(successfulTomorrow, successfulToday) should be(0)
    }
    it("""ExecutionStateSuccessful yesterday"""){
      executionState.compareExecutionState(successfulTomorrow, successfulYesterday) should be(0)
    }
    it("""ExecutionStateSuccessful tomorrow"""){
      executionState.compareExecutionState(successfulTomorrow, successfulTomorrow) should be(0)
    }
    it("""ExecutionStateFailed today"""){
      executionState.compareExecutionState(successfulTomorrow, failedToday) should be(0)
    }
    it("""ExecutionStateFailed yesterday"""){
      executionState.compareExecutionState(successfulTomorrow, failedYesterday) should be(0)
    }
    it("""ExecutionStateFailed tomorrow"""){
      executionState.compareExecutionState(successfulTomorrow, failedTomorrow) should be(0)
    }
  }
  
  describe("""ExecutionStateFailed today compared to"""){
    it("""ExecutionStateStarted today"""){
      executionState.compareExecutionState(failedToday, startedToday) should be(1)
    }
    it("""ExecutionStateStarted yesterday"""){
      executionState.compareExecutionState(failedToday, startedYesterday) should be(1)
    }
    it("""ExecutionStateStarted tomorrow"""){
      executionState.compareExecutionState(failedToday, startedTomorrow) should be(1)
    }
    it("""ExecutionStateInProcess today"""){
      executionState.compareExecutionState(failedToday, inProcessToday) should be(1)
    }
    it("""ExecutionStateInProcess yesterday"""){
      executionState.compareExecutionState(failedToday, inProcessYesterday) should be(1)
    }
    it("""ExecutionStateInProcess tomorrow"""){
      executionState.compareExecutionState(failedToday, inProcessTomorrow) should be(1)
    }
    it("""ExecutionStateSuccessful today"""){
      executionState.compareExecutionState(failedToday, successfulToday) should be(0)
    }
    it("""ExecutionStateSuccessful yesterday"""){
      executionState.compareExecutionState(failedToday, successfulYesterday) should be(0)
    }
    it("""ExecutionStateSuccessful tomorrow"""){
      executionState.compareExecutionState(failedToday, successfulTomorrow) should be(0)
    }
    it("""ExecutionStateFailed today"""){
      executionState.compareExecutionState(failedToday, failedToday) should be(0)
    }
    it("""ExecutionStateFailed yesterday"""){
      executionState.compareExecutionState(failedToday, failedYesterday) should be(0)
    }
    it("""ExecutionStateFailed tomorrow"""){
      executionState.compareExecutionState(failedToday, failedTomorrow) should be(0)
    }
  }
  
  describe("""ExecutionStateFailed yesterday compared to"""){
    it("""ExecutionStateStarted today"""){
      executionState.compareExecutionState(failedYesterday, startedToday) should be(1)
    }
    it("""ExecutionStateStarted yesterday"""){
      executionState.compareExecutionState(failedYesterday, startedYesterday) should be(1)
    }
    it("""ExecutionStateStarted tomorrow"""){
      executionState.compareExecutionState(failedYesterday, startedTomorrow) should be(1)
    }
    it("""ExecutionStateInProcess today"""){
      executionState.compareExecutionState(failedYesterday, inProcessToday) should be(1)
    }
    it("""ExecutionStateInProcess yesterday"""){
      executionState.compareExecutionState(failedYesterday, inProcessYesterday) should be(1)
    }
    it("""ExecutionStateInProcess tomorrow"""){
      executionState.compareExecutionState(failedYesterday, inProcessTomorrow) should be(1)
    }
    it("""ExecutionStateSuccessful today"""){
      executionState.compareExecutionState(failedYesterday, successfulToday) should be(0)
    }
    it("""ExecutionStateSuccessful yesterday"""){
      executionState.compareExecutionState(failedYesterday, successfulYesterday) should be(0)
    }
    it("""ExecutionStateSuccessful tomorrow"""){
      executionState.compareExecutionState(failedYesterday, successfulTomorrow) should be(0)
    }
    it("""ExecutionStateFailed today"""){
      executionState.compareExecutionState(failedYesterday, failedToday) should be(0)
    }
    it("""ExecutionStateFailed yesterday"""){
      executionState.compareExecutionState(failedYesterday, failedYesterday) should be(0)
    }
    it("""ExecutionStateFailed tomorrow"""){
      executionState.compareExecutionState(failedYesterday, failedTomorrow) should be(0)
    }
  }
  
  describe("""ExecutionStateFailed tomorrow compared to"""){
    it("""ExecutionStateStarted today"""){
      executionState.compareExecutionState(failedTomorrow, startedToday) should be(1)
    }
    it("""ExecutionStateStarted yesterday"""){
      executionState.compareExecutionState(failedTomorrow, startedYesterday) should be(1)
    }
    it("""ExecutionStateStarted tomorrow"""){
      executionState.compareExecutionState(failedTomorrow, startedTomorrow) should be(1)
    }
    it("""ExecutionStateInProcess today"""){
      executionState.compareExecutionState(failedTomorrow, inProcessToday) should be(1)
    }
    it("""ExecutionStateInProcess yesterday"""){
      executionState.compareExecutionState(failedTomorrow, inProcessYesterday) should be(1)
    }
    it("""ExecutionStateInProcess tomorrow"""){
      executionState.compareExecutionState(failedTomorrow, inProcessTomorrow) should be(1)
    }
    it("""ExecutionStateSuccessful today"""){
      executionState.compareExecutionState(failedTomorrow, successfulToday) should be(0)
    }
    it("""ExecutionStateSuccessful yesterday"""){
      executionState.compareExecutionState(failedTomorrow, successfulYesterday) should be(0)
    }
    it("""ExecutionStateSuccessful tomorrow"""){
      executionState.compareExecutionState(failedTomorrow, successfulTomorrow) should be(0)
    }
    it("""ExecutionStateFailed today"""){
      executionState.compareExecutionState(failedTomorrow, failedToday) should be(0)
    }
    it("""ExecutionStateFailed yesterday"""){
      executionState.compareExecutionState(failedTomorrow, failedYesterday) should be(0)
    }
    it("""ExecutionStateFailed tomorrow"""){
      executionState.compareExecutionState(failedTomorrow, failedTomorrow) should be(0)
    }
  }
  

}