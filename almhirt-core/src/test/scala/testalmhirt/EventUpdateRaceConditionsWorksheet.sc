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
  }                                               //> NotExecuted(1,almhirt.UnspecifiedProblem
                                                  //| The first event's version must be equal to the next required event version:
                                                  //|  1 != 2
                                                  //| Category: ApplicationProblem
                                                  //| Severity: Minor
                                                  //| Arguments: Map()
                                                  //| )
                                                  //| NotExecuted(2,almhirt.UnspecifiedProblem
                                                  //| The first event's version must be equal to the next required event version:
                                                  //|  1 != 2
                                                  //| Category: ApplicationProblem
                                                  //| Severity: Minor
                                                  //| Arguments: Map()
                                                  //| )
                                                  //| NotExecuted(3,almhirt.UnspecifiedProblem
                                                  //| The first event's version must be equal to the next required event version:
                                                  //|  1 != 2
                                                  //| Category: ApplicationProblem
                                                  //| Severity: Minor
                                                  //| Arguments: Map()
                                                  //| )
                                                  //| NotExecuted(4,almhirt.UnspecifiedProblem
                                                  //| The first event's version must be equal to the next required event version:
                                                  //|  1 != 2
                                                  //| Category: ApplicationProblem
                                                  //| Severity: Minor
                                                  //| Arguments: Map()
                                                  //| )
                                                  //| NotExecuted(5,almhirt.UnspecifiedProblem
                                                  //| The first event's version must be equal to the next required event version:
                                                  //|  1 != 2
                                                  //| Category: ApplicationProblem
                                                  //| Severity: Minor
                                                  //| Arguments: Map()
                                                  //| )
                                                  //| NotExecuted(6,almhirt.UnspecifiedProblem
                                                  //| The first event's version must be equal to the next required event version:
                                                  //|  1 != 2
                                                  //| Category: ApplicationProblem
                                                  //| Severity: Minor
                                                  //| Arguments: Map()
                                                  //| )
                                                  //| NotExecuted(7,almhirt.UnspecifiedProblem
                                                  //| The first event's version must be equal to the next required event version:
                                                  //|  1 != 2
                                                  //| Category: ApplicationProblem
                                                  //| Severity: Minor
                                                  //| Arguments: Map()
                                                  //| )
                                                  //| Executed(8)
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
                                                  //| )
                                                  //| NotExecuted(11,almhirt.UnspecifiedProblem
                                                  //| The first event's version must be equal to the next required event version:
                                                  //|  1 != 2
                                                  //| Category: ApplicationProblem
                                                  //| Severity: Minor
                                                  //| Arguments: Map()
                                                  //| )
                                                  //| NotExecuted(12,almhirt.UnspecifiedProblem
                                                  //| The first event's version must be equal to the next required event version:
                                                  //|  1 != 2
                                                  //| Category: ApplicationProblem
                                                  //| Severity: Minor
                                                  //| Arguments: Map()
                                                  //| )
                                                  //| NotExecuted(13,almhirt.UnspecifiedProblem
                                                  //| The first event's version must be equal to the next required event version:
                                                  //|  1 != 2
                                                  //| Category: ApplicationProblem
                                                  //| Severity: Minor
                                                  //| Arguments: Map()
                                                  //| )
                                                  //| NotExecuted(14,almhirt.UnspecifiedProblem
                                                  //| The first event's version must be equal to the next required event version:
                                                  //|  1 != 2
                                                  //| Category: ApplicationProblem
                                                  //| Severity: Minor
                                                  //| Arguments: Map()
                                                  //| )
                                                  //| NotExecuted(15,almhirt.UnspecifiedProblem
                                                  //| The first event's version must be equal to the next required event version:
                                                  //|  1 != 2
                                                  //| Category: ApplicationProblem
                                                  //| Severity: Minor
                                                  //| Arguments: Map()
                                                  //| )
                                                  //| NotExecuted(16,almhirt.UnspecifiedProblem
                                                  //| The first event's version must be equal to the next required event version:
                                                  //|  1 != 2
                                                  //| Category: ApplicationProblem
                                                  //| Severity: Minor
                                                  //| Arguments: Map()
                                                  //| )
                                                  //| NotExecuted(17,almhirt.UnspecifiedProblem
                                                  //| The first event's version must be equal to the next required event version:
                                                  //|  1 != 2
                                                  //| Category: ApplicationProblem
                                                  //| Severity: Minor
                                                  //| Arguments: Map()
                                                  //| )
                                                  //| NotExecuted(18,almhirt.UnspecifiedProblem
                                                  //| The first event's version must be equal to the next required event version:
                                                  //|  1 != 2
                                                  //| Category: ApplicationProblem
                                                  //| Severity: Minor
                                                  //| Arguments: Map()
                                                  //| )
                                                  //| NotExecuted(19,almhirt.UnspecifiedProblem
                                                  //| The first event's version must be equal to the next required event version:
                                                  //|  1 != 2
                                                  //| Category: ApplicationProblem
                                                  //| Severity: Minor
                                                  //| Arguments: Map()
                                                  //| )
                                                  //| NotExecuted(20,almhirt.UnspecifiedProblem
                                                  //| The first event's version must be equal to the next required event version:
                                                  //|  1 != 2
                                                  //| Category: ApplicationProblem
                                                  //| Severity: Minor
                                                  //| Arguments: Map()
                                                  //| )
                                                  //| NotExecuted(21,almhirt.UnspecifiedProblem
                                                  //| The first event's version must be equal to the next required event version:
                                                  //|  1 != 2
                                                  //| Category: ApplicationProblem
                                                  //| Severity: Minor
                                                  //| Arguments: Map()
                                                  //| )
                                                  //| NotExecuted(22,almhirt.UnspecifiedProblem
                                                  //| The first event's version must be equal to the next required event version:
                                                  //|  1 != 2
                                                  //| Category: ApplicationProblem
                                                  //| Severity: Minor
                                                  //| Arguments: Map()
                                                  //| )
                                                  //| NotExecuted(23,almhirt.UnspecifiedProblem
                                                  //| The first event's version must be equal to the next required event version:
                                                  //|  1 != 2
                                                  //| Category: ApplicationProblem
                                                  //| Severity: Minor
                                                  //| Arguments: Map()
                                                  //| )
                                                  //| NotExecuted(24,almhirt.UnspecifiedProblem
                                                  //| The first event's version must be equal to the next required event version:
                                                  //|  1 != 2
                                                  //| Category: ApplicationProblem
                                                  //| Severity: Minor
                                                  //| Arguments: Map()
                                                  //| )
                                                  //| NotExecuted(25,almhirt.UnspecifiedProblem
                                                  //| The first event's version must be equal to the next required event version:
                                                  //|  1 != 2
                                                  //| Category: ApplicationProblem
                                                  //| Severity: Minor
                                                  //| Arguments: Map()
                                                  //| )
                                                  //| NotExecuted(26,almhirt.UnspecifiedProblem
                                                  //| The first event's version must be equal to the next required event version:
                                                  //|  1 != 2
                                                  //| Category: ApplicationProblem
                                                  //| Severity: Minor
                                                  //| Arguments: Map()
                                                  //| )
                                                  //| NotExecuted(27,almhirt.UnspecifiedProblem
                                                  //| The first event's version must be equal to the next required event version:
                                                  //|  1 != 2
                                                  //| Category: ApplicationProblem
                                                  //| Severity: Minor
                                                  //| Arguments: Map()
                                                  //| )
                                                  //| NotExecuted(28,almhirt.UnspecifiedProblem
                                                  //| The first event's version must be equal to the next required event version:
                                                  //|  1 != 2
                                                  //| Category: ApplicationProblem
                                                  //| Severity: Minor
                                                  //| Arguments: Map()
                                                  //| )
                                                  //| NotExecuted(29,almhirt.UnspecifiedProblem
                                                  //| The first event's version must be equal to the next required event version:
                                                  //|  1 != 2
                                                  //| Category: ApplicationProblem
                                                  //| Severity: Minor
                                                  //| Arguments: Map()
                                                  //| )
                                                  //| NotExecuted(30,almhirt.UnspecifiedProblem
                                                  //| The first event's version must be equal to the next required event version:
                                                  //|  1 != 2
                                                  //| Category: ApplicationProblem
                                                  //| Severity: Minor
                                                  //| Arguments: Map()
                                                  //| )
                                                  //| NotExecuted(31,almhirt.UnspecifiedProblem
                                                  //| The first event's version must be equal to the next required event version:
                                                  //|  1 != 2
                                                  //| Category: ApplicationProblem
                                                  //| Severity: Minor
                                                  //| Arguments: Map()
                                                  //| )
                                                  //| NotExecuted(32,almhirt.UnspecifiedProblem
                                                  //| The first event's version must be equal to the next required event version:
                                                  //|  1 != 2
                                                  //| Category: ApplicationProblem
                                                  //| Severity: Minor
                                                  //| Arguments: Map()
                                                  //| )
                                                  //| NotExecuted(33,almhirt.UnspecifiedProblem
                                                  //| The first event's version must be equal to the next required event version:
                                                  //|  1 != 2
                                                  //| Category: ApplicationProblem
                                                  //| Severity: Minor
                                                  //| Arguments: Map()
                                                  //| )
                                                  //| NotExecuted(34,almhirt.UnspecifiedProblem
                                                  //| The first event's version must be equal to the next required event version:
                                                  //|  1 != 2
                                                  //| Category: ApplicationProblem
                                                  //| Severity: Minor
                                                  //| Arguments: Map()
                                                  //| )
                                                  //| NotExecuted(35,almhirt.UnspecifiedProblem
                                                  //| The first event's version must be equal to the next required event version:
                                                  //|  1 != 2
                                                  //| Category: ApplicationProblem
                                                  //| Severity: Minor
                                                  //| Arguments: Map()
                                                  //| )
                                                  //| NotExecuted(36,almhirt.UnspecifiedProblem
                                                  //| The first event's version must be equal to the next required event version:
                                                  //|  1 != 2
                                                  //| Category: ApplicationProblem
                                                  //| Severity: Minor
                                                  //| Arguments: Map()
                                                  //| )
                                                  //| NotExecuted(37,almhirt.UnspecifiedProblem
                                                  //| The first event's version must be equal to the next required event version:
                                                  //|  1 != 2
                                                  //| Category: ApplicationProblem
                                                  //| Severity: Minor
                                                  //| Arguments: Map()
                                                  //| )
                                                  //| NotExecuted(38,almhirt.UnspecifiedProblem
                                                  //| The first event's version must be equal to the next required event version:
                                                  //|  1 != 2
                                                  //| Category: ApplicationProblem
                                                  //| Severity: Minor
                                                  //| Arguments: Map()
                                                  //| )
                                                  //| NotExecuted(39,almhirt.UnspecifiedProblem
                                                  //| The first event's version must be equal to the next required event version:
                                                  //|  1 != 2
                                                  //| Category: ApplicationProblem
                                                  //| Severity: Minor
                                                  //| Arguments: Map()
                                                  //| )
                                                  //| NotExecuted(40,almhirt.UnspecifiedProblem
                                                  //| The first event's version must be equal to the next required event version:
                                                  //|  1 != 2
                                                  //| Category: ApplicationProblem
                                                  //| Severity: Minor
                                                  //| Arguments: Map()
                                                  //| )
                                                  //| NotExecuted(41,almhirt.UnspecifiedProblem
                                                  //| The first event's version must be equal to the next required event version:
                                                  //|  1 != 2
                                                  //| Category: ApplicationProblem
                                                  //| Severity: Minor
                                                  //| Arguments: Map()
                                                  //| )
                                                  //| NotExecuted(42,almhirt.UnspecifiedProblem
                                                  //| The first event's version must be equal to the next required event version:
                                                  //|  1 != 2
                                                  //| Category: ApplicationProblem
                                                  //| Severity: Minor
                                                  //| Arguments: Map()
                                                  //| )
                                                  //| NotExecuted(43,almhirt.UnspecifiedProblem
                                                  //| The first event's version must be equal to the next required event version:
                                                  //|  1 != 2
                                                  //| Category: ApplicationProblem
                                                  //| Severity: Minor
                                                  //| Arguments: Map()
                                                  //| )
                                                  //| NotExecuted(44,almhirt.UnspecifiedProblem
                                                  //| The first event's version must be equal to the next required event version:
                                                  //|  1 != 2
                                                  //| Category: ApplicationProblem
                                                  //| Severity: Minor
                                                  //| Arguments: Map()
                                                  //| )
                                                  //| NotExecuted(45,almhirt.UnspecifiedProblem
                                                  //| The first event's version must be equal to the next required event version:
                                                  //|  1 != 2
                                                  //| Category: ApplicationProblem
                                                  //| Severity: Minor
                                                  //| Arguments: Map()
                                                  //| )
                                                  //| NotExecuted(46,almhirt.UnspecifiedProblem
                                                  //| The first event's version must be equal to the next required event version:
                                                  //|  1 != 2
                                                  //| Category: ApplicationProblem
                                                  //| Severity: Minor
                                                  //| Arguments: Map()
                                                  //| )
                                                  //| NotExecuted(47,almhirt.UnspecifiedProblem
                                                  //| The first event's version must be equal to the next required event version:
                                                  //|  1 != 2
                                                  //| Category: ApplicationProblem
                                                  //| Severity: Minor
                                                  //| Arguments: Map()
                                                  //| )
                                                  //| NotExecuted(48,almhirt.UnspecifiedProblem
                                                  //| The first event's version must be equal to the next required event version:
                                                  //|  1 != 2
                                                  //| Category: ApplicationProblem
                                                  //| Severity: Minor
                                                  //| Arguments: Map()
                                                  //| )
                                                  //| NotExecuted(49,almhirt.UnspecifiedProblem
                                                  //| The first event's version must be equal to the next required event version:
                                                  //|  1 != 2
                                                  //| Category: ApplicationProblem
                                                  //| Severity: Minor
                                                  //| Arguments: Map()
                                                  //| )
                                                  //| NotExecuted(50,almhirt.UnspecifiedProblem
                                                  //| The first event's version must be equal to the next required event version:
                                                  //|  1 != 2
                                                  //| Category: ApplicationProblem
                                                  //| Severity: Minor
                                                  //| Arguments: Map()
                                                  //| )
                                                  //| TestPersonCreated(fd2317df-e636-46f8-a511-67b41c95e133,InitialName,2012-11-
                                                  //| 09T17:53:16.099+01:00)
                                                  //| TestPersonNameChanged(fd2317df-e636-46f8-a511-67b41c95e133,1,name8,2012-11-
                                                  //| 09T17:53:16.352+01:00)
                                                  //| ++++++++++++++++ SUCCESS ++++++++++++++++
                                                  //| EVENTS: 2

}