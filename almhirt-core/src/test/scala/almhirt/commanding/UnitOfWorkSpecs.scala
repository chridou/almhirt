package almhirt.commanding

import org.specs2.mutable._
import almhirt._
import almhirt.environment._
import test._

class UnitOfWorkSpecs extends Specification with AlmhirtEnvironmentTestKit {
  "x" should {
    "y" in {
      inTestEnvironment{
        env =>
          env.repositories.register(repo)
          env.commandExecutor.addHandler(NewTestPersonUnitOfWork)
      }
    }
  }
}