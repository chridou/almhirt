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
          env.repositories.registerForAggregateRoot[TestPerson, TestPersonEvent, TestPersonRepository](new TestPersonRepository(env.eventLog)(env.context))
          env.commandExecutor.addHandler(NewTestPersonUnitOfWork)
          val reg = (env.context.problemChannel <-<* (prob => println(prob))).awaitResult.forceResult
          env.commandExecutor.executeCommand(CommandEnvelope(NewTestPerson("Betty"), None))
          val eventsRes = env.eventLog.getAllEvents.awaitResult
          val events = eventsRes.forceResult.toList.map(_.asInstanceOf[TestPersonEvent])
          val createdRes = TestPerson.rebuildFromHistory(events)
          val created = createdRes.forceResult
          reg.dispose
          created.name === "Betty"
      }
    }
  }
}