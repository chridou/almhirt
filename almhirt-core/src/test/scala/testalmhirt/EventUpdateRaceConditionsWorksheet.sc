package testalmhirt

import akka.dispatch.{ Await, Future }
import almhirt.common._
import almhirt.almvalidation.kit._
import almhirt.environment._
import test._
import almhirt.almfuture.inst._
import almhirt.commanding.CommandEnvelope
import almhirt.commanding.AggregateRootRef

object EventUpdateRaceConditionsWorksheet extends TestAlmhirtKit {
  implicit val atMost = akka.util.Duration(1, "s")
  inTestAlmhirt { almhirtInstance =>
    implicit val executor = almhirtInstance.environment.context.system.futureDispatcher
    val repo = almhirtInstance.environment.repositories.getForAggregateRoot[TestPerson, TestPersonEvent].awaitResult.forceResult
    val count = 500
    val id = almhirtInstance.getUuid

    almhirtInstance.executeTrackedCommand(NewTestPerson(id, "InitialName"), "CREATE")
    almhirtInstance.getResultOperationStateFor("CREATE").awaitResult

    val commandEnvelopes = Vector((for (i <- 1 to count) yield CommandEnvelope(ChangeTestPersonName(AggregateRootRef(id, 1), "name%d".format(i)), Some(i.toString))): _*)
    
    commandEnvelopes.foreach(almhirtInstance.executeCommand(_))
    val statesFutures = commandEnvelopes.map(x => almhirtInstance.getResultOperationStateFor(x.ticket.get))
    val statesFuturesRes = AlmFuture.sequence(statesFutures).awaitResult
//    statesFuturesRes.foreach { updateStates =>
//      updateStates.foreach(x => x fold (f => println(f), succ => println(succ)))
//    }

    
    val events = almhirtInstance.environment.eventLog.getAllEvents.awaitResult.forceResult
    events.foreach(println)
    if(events.size == 2) println ("++++++++++++++++ SUCCESS ++++++++++++++++") else println ("---------- FAILURE -----------------")
    println("EVENTS: %d".format(events.size))
  }

}