package testalmhirt

import akka.dispatch.{ Await, Future }
import almhirt._
import almhirt.almvalidation.kit._
import almhirt.environment._
import test._
import almhirt.almfuture.inst._
import almhirt.commanding.CommandEnvelope

object EventCreateRaceConditionsWorksheet extends TestAlmhirtKit {
  implicit val atMost = akka.util.Duration(1, "s")//> atMost  : akka.util.FiniteDuration = 1 second
  inTestAlmhirt { almhirtInstance =>
    implicit val executor = almhirtInstance.environment.context.system.futureDispatcher
    val repo = almhirtInstance.environment.repositories.getForAggregateRoot[TestPerson, TestPersonEvent].awaitResult.forceResult
    val count = 500
    val id = almhirtInstance.getUuid
 
    val commandEnvelopes = Vector((for (i <- 1 to count) yield CommandEnvelope(NewTestPerson(id, "name%d".format(i)), Some(i.toString))): _*)

    commandEnvelopes.foreach(almhirtInstance.executeCommand(_))
    val statesFutures = commandEnvelopes.map(x => almhirtInstance.getResultOperationStateFor(x.ticket.get))
    val statesFuturesRes = AlmFuture.sequence(statesFutures).awaitResult
//    statesFuturesRes.foreach { states =>
//      states.foreach(x => x fold (f => println(f), succ => println(succ)))
//    }

    val events = almhirtInstance.environment.eventLog.getAllEvents.awaitResult.forceResult
    events.foreach(println)
    if (events.size == 1) println("++++++++++++++++ SUCCESS ++++++++++++++++") else println("---------- FAILURE -----------------")
    println("EVENTS: %d".format(events.size))
  }                                               //> TestPersonCreated(2cfb82e8-8a43-4ce3-a1b7-8e82df5ff48e,name7,2012-11-09T20:
                                                  //| 40:13.687+01:00)
                                                  //| ++++++++++++++++ SUCCESS ++++++++++++++++
                                                  //| EVENTS: 1
}