package testalmhirt

import akka.dispatch.{ Await, Future }
import almhirt._
import almhirt.almvalidation.kit._
import almhirt.environment._
import test._
import almhirt.almfuture.inst._
import almhirt.commanding.CommandEnvelope

object EventUpdateRaceConditionsWorksheet extends TestAlmhirtKit {
  implicit val atMost = akka.util.Duration(1, "s")//> atMost  : akka.util.FiniteDuration = 1 second
  inTestAlmhirt { almhirtInstance =>
    implicit val executor = almhirtInstance.environment.context.system.futureDispatcher
    val repo = almhirtInstance.environment.repositories.getForAggregateRoot[TestPerson, TestPersonEvent].awaitResult.forceResult
    val count = 500
    val id = almhirtInstance.getUuid

    almhirtInstance.executeTrackedCommand(NewTestPerson(id, "InitialName"), "CREATE")
    almhirtInstance.getResultOperationStateFor("CREATE").awaitResult

    val commandEnvelopes = Vector((for (i <- 1 to count) yield CommandEnvelope(ChangeTestPersonName(id, Some(1), "name%d".format(i)), Some(i.toString))): _*)
    
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
  }                                               //> TestPersonCreated(700836d8-bc9b-45a8-ac12-c13ff2ad7f3f,InitialName,2012-11-
                                                  //| 12T12:17:45.824+01:00)
                                                  //| TestPersonNameChanged(700836d8-bc9b-45a8-ac12-c13ff2ad7f3f,1,name104,2012-1
                                                  //| 1-12T12:17:46.168+01:00)
                                                  //| ++++++++++++++++ SUCCESS ++++++++++++++++
                                                  //| EVENTS: 2

}