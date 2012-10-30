package testenvironment

import org.specs2.mutable._
//import almhirt._
//import almhirt.syntax.almvalidation._
//import almhirt.environment._
import test._

class TestEnvironmentSpecs extends Specification with TestEnvironment {
  private implicit val atMost = akka.util.Duration(2, "s")
  "The TestEnvironment" should {
    "create a person" in {
      inTestEnvironment{env =>
        val id1 = env.getUuid
        env.executeCommandWithTicket(NewTestPerson(id1, "Harry"), "1")
        env.operationStateTracker.getResultFor("1").awaitResult
        
        false}
    }
  }
}