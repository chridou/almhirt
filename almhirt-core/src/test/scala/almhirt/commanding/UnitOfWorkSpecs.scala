package almhirt.commanding

import org.specs2.mutable._
import almhirt._
import almhirt.syntax.almvalidation._
import almhirt.environment._
import test._

class UnitOfWorkSpecs extends Specification with AlmhirtEnvironmentTestKit {
  "x" should {
    "y" in {
      inTestEnvironment{
        env =>
          implicit val duration = env.context.akkaContext.mediumDuration
          env.repositories.register(new TestPersonRepository(env.eventLog)(env.context))
          env.commandExecutor.addHandler(NewTestPersonUnitOfWork)
          env.commandExecutor.executeCommand(CommandEnvelope(NewTestPerson("Betty"), None))
          val events = env.eventLog.getAllEvents.awaitResult.forceResult.toList.map(_.asInstanceOf[TestPersonEvent])
          val created = TestPerson.rebuildFromHistory(events).forceResult
          created.name === "Betty"
      }
    }
  }
}