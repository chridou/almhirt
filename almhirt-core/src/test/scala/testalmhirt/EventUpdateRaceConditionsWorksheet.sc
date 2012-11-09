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
    val count = 50
    val id = almhirtInstance.getUuid

    almhirtInstance.executeTrackedCommand(NewTestPerson(id, "InitialName"), "CREATE")
    almhirtInstance.getResultOperationStateFor("CREATE").awaitResult

    val commandEnvelopes = Vector((for (i <- 1 to count) yield CommandEnvelope(ChangeTestPersonName(id, Some(1), "name%d".format(i)), Some(i.toString))): _*)
    
    commandEnvelopes.foreach(almhirtInstance.executeCommand(_))
    val statesFutures = commandEnvelopes.map(x => almhirtInstance.getResultOperationStateFor(x.ticket.get))
    val statesFuturesRes = AlmFuture.sequence(statesFutures).awaitResult
    statesFuturesRes.foreach { updateStates =>
      updateStates.foreach(x => x fold (f => println(f), succ => println(succ)))
    }
    
    val events = almhirtInstance.environment.eventLog.getAllEvents.awaitResult.forceResult
    events.foreach(println)
    if(events.size == 2) println ("++++++++++++++++ SUCCESS ++++++++++++++++") else println ("---------- FAILURE -----------------")
    println("EVENTS: %d".format(events.size))
  }                                               //> Executed(1)
                                                  //| Executed(2)
                                                  //| NotExecuted(3,almhirt.UnspecifiedProblem
                                                  //| The first event's version must be equal to the next required event version:
                                                  //|  1 != 2
                                                  //| Category: ApplicationProblem
                                                  //| Severity: Minor
                                                  //| Arguments: Map()
                                                  //| )
                                                  //| Executed(4)
                                                  //| Executed(5)
                                                  //| Executed(6)
                                                  //| Executed(7)
                                                  //| NotExecuted(8,almhirt.UnspecifiedProblem
                                                  //| The first event's version must be equal to the next required event version:
                                                  //|  1 != 2
                                                  //| Category: ApplicationProblem
                                                  //| Severity: Minor
                                                  //| Arguments: Map()
                                                  //| )
                                                  //| NotExecuted(9,almhirt.UnspecifiedProblem
                                                  //| The first event's version must be equal to the next required event version:
                                                  //|  1 != 2
                                                  //| Category: ApplicationProblem
                                                  //| Severity: Minor
                                                  //| Arguments: Map()
                                                  //| )
                                                  //| NotExecuted(10,almhirt.UnspecifiedProblem
                                                  //| The first event's version must be equal to the next required event version:
                                                  //|  1 != 2
                                                  //| Category: ApplicationProblem
                                                  //| Severity: Minor
                                                  //| Arguments: Map()
                                                  //| Output exceeds cutoff limit.

}