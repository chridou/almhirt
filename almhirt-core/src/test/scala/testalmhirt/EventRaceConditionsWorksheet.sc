package testalmhirt

import akka.dispatch.{ Await, Future }
import almhirt._
import almhirt.almvalidation.kit._
import almhirt.environment._
import test._
import almhirt.almfuture.inst._
import almhirt.commanding.CommandEnvelope

object EventRaceConditionsWorksheet extends TestAlmhirtKit {
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
                                                  //| Executed(4)
                                                  //| Executed(5)
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
                                                  //| NotExecuted(8,almhirt.UnspecifiedProblem
                                                  //| The first event's version must be equal to the next required event version:
                                                  //|  1 != 2
                                                  //| Category: ApplicationProblem
                                                  //| Severity: Minor
                                                  //| Arguments: Map()
                                                  //| )
                                                  //| Executed(9)
                                                  //| Executed(10)
                                                  //| NotExecuted(11,almhirt.UnspecifiedProblem
                                                  //| The first event's version must be equal to the next required event version:
                                                  //|  1 != 2
                                                  //| Category: ApplicationProblem
                                                  //| Severity: Minor
                                                  //| Arguments: Map()
                                                  //| )
                                                  //| Executed(12)
                                                  //| NotExecuted(13,almhirt.UnspecifiedProblem
                                                  //| The first event's version must be equal to the next required event version:
                                                  //|  1 != 2
                                                  //| Category: ApplicationProblem
                                                  //| Severity: Minor
                                                  //| Arguments: Map()
                                                  //| )
                                                  //| Executed(14)
                                                  //| NotExecuted(15,almhirt.UnspecifiedProblem
                                                  //| The first event's version must be equal to the next required event version:
                                                  //|  1 != 2
                                                  //| Category: ApplicationProblem
                                                  //| Severity: Minor
                                                  //| Arguments: Map()
                                                  //| )
                                                  //| Executed(16)
                                                  //| Executed(17)
                                                  //| Executed(18)
                                                  //| Executed(19)
                                                  //| Executed(20)
                                                  //| Executed(21)
                                                  //| Executed(22)
                                                  //| Executed(23)
                                                  //| Executed(24)
                                                  //| Executed(25)
                                                  //| Executed(26)
                                                  //| Executed(27)
                                                  //| Executed(28)
                                                  //| Executed(29)
                                                  //| Executed(30)
                                                  //| Executed(31)
                                                  //| Executed(32)
                                                  //| Executed(33)
                                                  //| Executed(34)
                                                  //| Executed(35)
                                                  //| Executed(36)
                                                  //| Executed(37)
                                                  //| Executed(38)
                                                  //| Executed(39)
                                                  //| Executed(40)
                                                  //| Executed(41)
                                                  //| Executed(42)
                                                  //| Executed(43)
                                                  //| Executed(44)
                                                  //| Executed(45)
                                                  //| NotExecuted(46,almhirt.UnspecifiedProblem
                                                  //| The first event's version must be equal to the next required event version:
                                                  //|  1 != 2
                                                  //| Category: ApplicationProblem
                                                  //| Severity: Minor
                                                  //| Arguments: Map()
                                                  //| )
                                                  //| Executed(47)
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
                                                  //| TestPersonCreated(d2357eab-87a5-4f67-9608-4c990e4d2c72,InitialName,2012-11-
                                                  //| 07T16:21:03.693+01:00)
                                                  //| TestPersonNameChanged(d2357eab-87a5-4f67-9608-4c990e4d2c72,1,name10,2012-11
                                                  //| -07T16:21:03.956+01:00)
                                                  //| TestPersonNameChanged(d2357eab-87a5-4f67-9608-4c990e4d2c72,1,name4,2012-11-
                                                  //| 07T16:21:03.956+01:00)
                                                  //| TestPersonNameChanged(d2357eab-87a5-4f67-9608-4c990e4d2c72,1,name17,2012-11
                                                  //| -07T16:21:03.956+01:00)
                                                  //| TestPersonNameChanged(d2357eab-87a5-4f67-9608-4c990e4d2c72,1,name5,2012-11-
                                                  //| 07T16:21:03.956+01:00)
                                                  //| TestPersonNameChanged(d2357eab-87a5-4f67-9608-4c990e4d2c72,1,name16,2012-11
                                                  //| -07T16:21:03.956+01:00)
                                                  //| TestPersonNameChanged(d2357eab-87a5-4f67-9608-4c990e4d2c72,1,name19,2012-11
                                                  //| -07T16:21:03.956+01:00)
                                                  //| TestPersonNameChanged(d2357eab-87a5-4f67-9608-4c990e4d2c72,1,name20,2012-11
                                                  //| -07T16:21:03.956+01:00)
                                                  //| TestPersonNameChanged(d2357eab-87a5-4f67-9608-4c990e4d2c72,1,name18,2012-11
                                                  //| -07T16:21:03.956+01:00)
                                                  //| TestPersonNameChanged(d2357eab-87a5-4f67-9608-4c990e4d2c72,1,name9,2012-11-
                                                  //| 07T16:21:03.956+01:00)
                                                  //| TestPersonNameChanged(d2357eab-87a5-4f67-9608-4c990e4d2c72,1,name14,2012-11
                                                  //| -07T16:21:03.956+01:00)
                                                  //| TestPersonNameChanged(d2357eab-87a5-4f67-9608-4c990e4d2c72,1,name12,2012-11
                                                  //| -07T16:21:03.956+01:00)
                                                  //| TestPersonNameChanged(d2357eab-87a5-4f67-9608-4c990e4d2c72,1,name21,2012-11
                                                  //| -07T16:21:03.956+01:00)
                                                  //| TestPersonNameChanged(d2357eab-87a5-4f67-9608-4c990e4d2c72,1,name22,2012-11
                                                  //| -07T16:21:03.956+01:00)
                                                  //| TestPersonNameChanged(d2357eab-87a5-4f67-9608-4c990e4d2c72,1,name23,2012-11
                                                  //| -07T16:21:03.956+01:00)
                                                  //| TestPersonNameChanged(d2357eab-87a5-4f67-9608-4c990e4d2c72,1,name24,2012-11
                                                  //| -07T16:21:03.956+01:00)
                                                  //| TestPersonNameChanged(d2357eab-87a5-4f67-9608-4c990e4d2c72,1,name25,2012-11
                                                  //| -07T16:21:03.956+01:00)
                                                  //| TestPersonNameChanged(d2357eab-87a5-4f67-9608-4c990e4d2c72,1,name28,2012-11
                                                  //| -07T16:21:03.956+01:00)
                                                  //| TestPersonNameChanged(d2357eab-87a5-4f67-9608-4c990e4d2c72,1,name26,2012-11
                                                  //| -07T16:21:03.956+01:00)
                                                  //| TestPersonNameChanged(d2357eab-87a5-4f67-9608-4c990e4d2c72,1,name32,2012-11
                                                  //| -07T16:21:03.956+01:00)
                                                  //| TestPersonNameChanged(d2357eab-87a5-4f67-9608-4c990e4d2c72,1,name27,2012-11
                                                  //| -07T16:21:03.956+01:00)
                                                  //| TestPersonNameChanged(d2357eab-87a5-4f67-9608-4c990e4d2c72,1,name30,2012-11
                                                  //| -07T16:21:03.956+01:00)
                                                  //| TestPersonNameChanged(d2357eab-87a5-4f67-9608-4c990e4d2c72,1,name31,2012-11
                                                  //| -07T16:21:03.956+01:00)
                                                  //| TestPersonNameChanged(d2357eab-87a5-4f67-9608-4c990e4d2c72,1,name29,2012-11
                                                  //| -07T16:21:03.956+01:00)
                                                  //| TestPersonNameChanged(d2357eab-87a5-4f67-9608-4c990e4d2c72,1,name33,2012-11
                                                  //| -07T16:21:03.956+01:00)
                                                  //| TestPersonNameChanged(d2357eab-87a5-4f67-9608-4c990e4d2c72,1,name39,2012-11
                                                  //| -07T16:21:03.956+01:00)
                                                  //| TestPersonNameChanged(d2357eab-87a5-4f67-9608-4c990e4d2c72,1,name35,2012-11
                                                  //| -07T16:21:03.956+01:00)
                                                  //| TestPersonNameChanged(d2357eab-87a5-4f67-9608-4c990e4d2c72,1,name34,2012-11
                                                  //| -07T16:21:03.956+01:00)
                                                  //| TestPersonNameChanged(d2357eab-87a5-4f67-9608-4c990e4d2c72,1,name37,2012-11
                                                  //| -07T16:21:03.956+01:00)
                                                  //| TestPersonNameChanged(d2357eab-87a5-4f67-9608-4c990e4d2c72,1,name36,2012-11
                                                  //| -07T16:21:03.956+01:00)
                                                  //| TestPersonNameChanged(d2357eab-87a5-4f67-9608-4c990e4d2c72,1,name38,2012-11
                                                  //| -07T16:21:03.956+01:00)
                                                  //| TestPersonNameChanged(d2357eab-87a5-4f67-9608-4c990e4d2c72,1,name40,2012-11
                                                  //| -07T16:21:03.956+01:00)
                                                  //| TestPersonNameChanged(d2357eab-87a5-4f67-9608-4c990e4d2c72,1,name41,2012-11
                                                  //| -07T16:21:03.956+01:00)
                                                  //| TestPersonNameChanged(d2357eab-87a5-4f67-9608-4c990e4d2c72,1,name44,2012-11
                                                  //| -07T16:21:03.956+01:00)
                                                  //| TestPersonNameChanged(d2357eab-87a5-4f67-9608-4c990e4d2c72,1,name42,2012-11
                                                  //| -07T16:21:03.956+01:00)
                                                  //| TestPersonNameChanged(d2357eab-87a5-4f67-9608-4c990e4d2c72,1,name45,2012-11
                                                  //| -07T16:21:03.956+01:00)
                                                  //| TestPersonNameChanged(d2357eab-87a5-4f67-9608-4c990e4d2c72,1,name47,2012-11
                                                  //| -07T16:21:03.956+01:00)
                                                  //| TestPersonNameChanged(d2357eab-87a5-4f67-9608-4c990e4d2c72,1,name43,2012-11
                                                  //| -07T16:21:03.956+01:00)
                                                  //| ---------- FAILURE -----------------
                                                  //| EVENTS: 38

}