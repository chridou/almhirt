package almhirt.commanding

import org.specs2.mutable._
import almhirt._
import almhirt.domain._
import almhirt.syntax.almvalidation._
import almhirt.environment._
import almhirt.util._
import test._

class UnitOfWorkSpecs extends Specification with AlmhirtEnvironmentTestKit {
  "A UnitOfWork(Creator) supplied with the required services" should {
    "create a new Person" in {
      inTestEnvironment{
        env =>
          implicit val duration = env.context.system.mediumDuration
          implicit val ctx = env.context
          val id = env.getUuid
          env.repositories.registerForAggregateRoot[TestPerson, TestPersonEvent](AggregateRootRepository.unsafe[TestPerson, TestPersonEvent](TestPerson, env.eventLog))
          env.commandExecutor.addHandler(new NewTestPersonUnitOfWork())
          val reg = (env.context.problemChannel <-<* (prob => println(prob))).awaitResult.forceResult
          env.executeCommand(CommandEnvelope(NewTestPerson(id, "Betty"), Some("create a new Person")))
          env.operationStateTracker.getResultFor("create a new Person").awaitResult
          val eventsRes = env.eventLog.getEvents(id).awaitResult
          val events = eventsRes.forceResult.toList.map(_.asInstanceOf[TestPersonEvent])
          val createdRes = TestPerson.rebuildFromHistory(events)
          val created = createdRes.forceResult
          reg.dispose
          created.name === "Betty"
      }
    }
    "create 2 new Persons" in {
      inTestEnvironment{
        env =>
          implicit val duration = env.context.system.mediumDuration
          implicit val ctx = env.context
          val id1 = env.getUuid
          val id2 = env.getUuid
          env.repositories.registerForAggregateRoot[TestPerson, TestPersonEvent](AggregateRootRepository.unsafe[TestPerson, TestPersonEvent](TestPerson, env.eventLog))
          env.commandExecutor.addHandler(new NewTestPersonUnitOfWork())
          val reg = (env.context.problemChannel <-<* (prob => println(prob))).awaitResult.forceResult
          env.executeCommand(CommandEnvelope(NewTestPerson(id1, "Betty"), None))
          env.executeCommand(CommandEnvelope(NewTestPerson(id2, "Brian"), Some("create 2 new Persons")))
          env.operationStateTracker.getResultFor("create 2 new Persons").awaitResult
          val eventsRes1 = env.eventLog.getEvents(id1).awaitResult
          val events1= eventsRes1.forceResult.toList.map(_.asInstanceOf[TestPersonEvent])
          val eventsRes2 = env.eventLog.getEvents(id2).awaitResult
          val events2 = eventsRes2.forceResult.toList.map(_.asInstanceOf[TestPersonEvent])
          val created1 = TestPerson.rebuildFromHistory(events1).forceResult
          val created2 = TestPerson.rebuildFromHistory(events2).forceResult
          reg.dispose
          created1.name === "Betty"
          created2.name === "Brian"
      }
    }
  }

  val jimRecorder = TestPerson(java.util.UUID.randomUUID, "Jim") flatMap {_.addressAquired("Roma")} flatMap {_.move("New York")}
  val jimEvents = jimRecorder.events
  val jim = jimRecorder.result.forceResult
  
  "A UnitOfWork(Mutator) executing an update command" should {
    "produce a successful operation result" in {
      inTestEnvironment{
        env =>
          implicit val duration = env.context.system.mediumDuration
          implicit val ctx = env.context
          env.eventLog.storeEvents(jimEvents).awaitResult
          env.repositories.registerForAggregateRoot[TestPerson, TestPersonEvent](AggregateRootRepository.unsafe[TestPerson, TestPersonEvent](TestPerson, env.eventLog))
          env.commandExecutor.addHandler(new ChangeTestPersonNameUnitOfWork)
          env.executeCommand(CommandEnvelope(ChangeTestPersonName(AggregateRootRef(jim.id, 9999), "Betty"), Some("produce a successful operation result")))
          val resV = env.operationStateTracker.getResultFor("produce a successful operation result").awaitResult
          val res = resV.forceResult
          res === Executed("produce a successful operation result")
      }
    }
    "update a Person" in {
      inTestEnvironment{
        env =>
          implicit val duration = env.context.system.mediumDuration
          implicit val ctx = env.context
          env.eventLog.storeEvents(jimEvents).awaitResult
          env.repositories.registerForAggregateRoot[TestPerson, TestPersonEvent](AggregateRootRepository.unsafe[TestPerson, TestPersonEvent](TestPerson, env.eventLog))
          env.commandExecutor.addHandler(new ChangeTestPersonNameUnitOfWork)
          env.executeCommand(CommandEnvelope(ChangeTestPersonName(AggregateRootRef(jim.id, 9999), "Betty"), Some("update a Person")))
          env.operationStateTracker.getResultFor("update a Person").awaitResult.forceResult
          val events = env.eventLog.getEvents(jim.id).awaitResult.forceResult.map(_.asInstanceOf[TestPersonEvent]).toList
          val jimUpdated = TestPerson.rebuildFromHistory(events).forceResult
          jimUpdated.name === "Betty" && jimUpdated.address === Some("New York")
      }
    }
  }

//  "2 UnitsOfWork(Creator+Mutator) supplied with the required services" should {
//    "create and then update a Person" in {
//      inTestEnvironment{
//        env =>
//          implicit val duration = env.context.system.mediumDuration
//          val id = env.getUuid
//          env.repositories.registerForAggregateRoot[TestPerson, TestPersonEvent](AggregateRootRepository.unsafe[TestPerson, TestPersonEvent](TestPerson, env.eventLog))
//          env.commandExecutor.addHandler(NewTestPersonUnitOfWork)
//          env.commandExecutor.addHandler(ChangeTestPersonNameUnitOfWork)
//          val reg = (env.context.problemChannel <-<* (prob => println(prob))).awaitResult.forceResult
//          env.commandExecutor.executeCommand(CommandEnvelope(NewTestPerson(id, "Jim"), None))
//          env.commandExecutor.executeCommand(CommandEnvelope(ChangeTestPersonName(id, None, "Betty"), None))
//          val eventsRes = env.eventLog.getEvents(id).awaitResult
//          val events = eventsRes.forceResult.toList.map(_.asInstanceOf[TestPersonEvent])
//          val createdRes = TestPerson.rebuildFromHistory(events)
//          val created = createdRes.forceResult
//          reg.dispose
//          created.name === "Betty"
//      }
//    }
//  }
  
}