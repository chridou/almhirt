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
    val count = 50
    val id = almhirtInstance.getUuid
 
    val commandEnvelopes = Vector((for (i <- 1 to count) yield CommandEnvelope(NewTestPerson(id, "name%d".format(i)), Some(i.toString))): _*)

    commandEnvelopes.foreach(almhirtInstance.executeCommand(_))
    val statesFutures = commandEnvelopes.map(x => almhirtInstance.getResultOperationStateFor(x.ticket.get))
    val statesFuturesRes = AlmFuture.sequence(statesFutures).awaitResult
    statesFuturesRes.foreach { states =>
      states.foreach(x => x fold (f => println(f), succ => println(succ)))
    }

    val events = almhirtInstance.environment.eventLog.getAllEvents.awaitResult.forceResult
    events.foreach(println)
    if (events.size == 1) println("++++++++++++++++ SUCCESS ++++++++++++++++") else println("---------- FAILURE -----------------")
    println("EVENTS: %d".format(events.size))
  }                                               //> Executed(1)
                                                  //| Executed(2)
                                                  //| Executed(3)
                                                  //| Executed(4)
                                                  //| Executed(5)
                                                  //| Executed(6)
                                                  //| Executed(7)
                                                  //| Executed(8)
                                                  //| Executed(9)
                                                  //| Executed(10)
                                                  //| NotExecuted(11,almhirt.UnspecifiedProblem
                                                  //| The first event's version must be equal to the next required event version:
                                                  //|  0 != 1
                                                  //| Category: ApplicationProblem
                                                  //| Severity: Minor
                                                  //| Arguments: Map()
                                                  //| )
                                                  //| Executed(12)
                                                  //| Executed(13)
                                                  //| Executed(14)
                                                  //| NotExecuted(15,almhirt.UnspecifiedProblem
                                                  //| The first event's version must be equal to the next required event version:
                                                  //|  0 != 1
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
                                                  //| Executed(46)
                                                  //| Executed(47)
                                                  //| Executed(48)
                                                  //| Executed(49)
                                                  //| Executed(50)
                                                  //| TestPersonCreated(1914b48e-54de-4860-b73d-7cfee691df97,name2,2012-11-07T19:
                                                  //| 12:59.954+01:00)
                                                  //| TestPersonCreated(1914b48e-54de-4860-b73d-7cfee691df97,name42,2012-11-07T19
                                                  //| :13:00.128+01:00)
                                                  //| TestPersonCreated(1914b48e-54de-4860-b73d-7cfee691df97,name41,2012-11-07T19
                                                  //| :13:00.128+01:00)
                                                  //| TestPersonCreated(1914b48e-54de-4860-b73d-7cfee691df97,name44,2012-11-07T19
                                                  //| :13:00.128+01:00)
                                                  //| TestPersonCreated(1914b48e-54de-4860-b73d-7cfee691df97,name43,2012-11-07T19
                                                  //| :13:00.128+01:00)
                                                  //| TestPersonCreated(1914b48e-54de-4860-b73d-7cfee691df97,name45,2012-11-07T19
                                                  //| :13:00.128+01:00)
                                                  //| TestPersonCreated(1914b48e-54de-4860-b73d-7cfee691df97,name46,2012-11-07T19
                                                  //| :13:00.128+01:00)
                                                  //| TestPersonCreated(1914b48e-54de-4860-b73d-7cfee691df97,name24,2012-11-07T19
                                                  //| :12:59.993+01:00)
                                                  //| TestPersonCreated(1914b48e-54de-4860-b73d-7cfee691df97,name47,2012-11-07T19
                                                  //| :13:00.129+01:00)
                                                  //| TestPersonCreated(1914b48e-54de-4860-b73d-7cfee691df97,name48,2012-11-07T19
                                                  //| :13:00.129+01:00)
                                                  //| TestPersonCreated(1914b48e-54de-4860-b73d-7cfee691df97,name49,2012-11-07T19
                                                  //| :13:00.129+01:00)
                                                  //| TestPersonCreated(1914b48e-54de-4860-b73d-7cfee691df97,name50,2012-11-07T19
                                                  //| :13:00.129+01:00)
                                                  //| TestPersonCreated(1914b48e-54de-4860-b73d-7cfee691df97,name22,2012-11-07T19
                                                  //| :12:59.992+01:00)
                                                  //| TestPersonCreated(1914b48e-54de-4860-b73d-7cfee691df97,name14,2012-11-07T19
                                                  //| :12:59.954+01:00)
                                                  //| TestPersonCreated(1914b48e-54de-4860-b73d-7cfee691df97,name5,2012-11-07T19:
                                                  //| 12:59.954+01:00)
                                                  //| TestPersonCreated(1914b48e-54de-4860-b73d-7cfee691df97,name40,2012-11-07T19
                                                  //| :13:00.128+01:00)
                                                  //| TestPersonCreated(1914b48e-54de-4860-b73d-7cfee691df97,name39,2012-11-07T19
                                                  //| :13:00.128+01:00)
                                                  //| TestPersonCreated(1914b48e-54de-4860-b73d-7cfee691df97,name18,2012-11-07T19
                                                  //| :12:59.992+01:00)
                                                  //| TestPersonCreated(1914b48e-54de-4860-b73d-7cfee691df97,name6,2012-11-07T19:
                                                  //| 12:59.992+01:00)
                                                  //| TestPersonCreated(1914b48e-54de-4860-b73d-7cfee691df97,name20,2012-11-07T19
                                                  //| :12:59.992+01:00)
                                                  //| TestPersonCreated(1914b48e-54de-4860-b73d-7cfee691df97,name7,2012-11-07T19:
                                                  //| 12:59.992+01:00)
                                                  //| TestPersonCreated(1914b48e-54de-4860-b73d-7cfee691df97,name1,2012-11-07T19:
                                                  //| 12:59.954+01:00)
                                                  //| TestPersonCreated(1914b48e-54de-4860-b73d-7cfee691df97,name38,2012-11-07T19
                                                  //| :13:00.127+01:00)
                                                  //| TestPersonCreated(1914b48e-54de-4860-b73d-7cfee691df97,name8,2012-11-07T19:
                                                  //| 12:59.992+01:00)
                                                  //| TestPersonCreated(1914b48e-54de-4860-b73d-7cfee691df97,name16,2012-11-07T19
                                                  //| :12:59.955+01:00)
                                                  //| TestPersonCreated(1914b48e-54de-4860-b73d-7cfee691df97,name19,2012-11-07T19
                                                  //| :12:59.992+01:00)
                                                  //| TestPersonCreated(1914b48e-54de-4860-b73d-7cfee691df97,name3,2012-11-07T19:
                                                  //| 12:59.993+01:00)
                                                  //| TestPersonCreated(1914b48e-54de-4860-b73d-7cfee691df97,name10,2012-11-07T19
                                                  //| :12:59.954+01:00)
                                                  //| TestPersonCreated(1914b48e-54de-4860-b73d-7cfee691df97,name12,2012-11-07T19
                                                  //| :12:59.954+01:00)
                                                  //| TestPersonCreated(1914b48e-54de-4860-b73d-7cfee691df97,name23,2012-11-07T19
                                                  //| :12:59.992+01:00)
                                                  //| TestPersonCreated(1914b48e-54de-4860-b73d-7cfee691df97,name21,2012-11-07T19
                                                  //| :12:59.993+01:00)
                                                  //| TestPersonCreated(1914b48e-54de-4860-b73d-7cfee691df97,name9,2012-11-07T19:
                                                  //| 12:59.992+01:00)
                                                  //| TestPersonCreated(1914b48e-54de-4860-b73d-7cfee691df97,name25,2012-11-07T19
                                                  //| :13:00.123+01:00)
                                                  //| TestPersonCreated(1914b48e-54de-4860-b73d-7cfee691df97,name4,2012-11-07T19:
                                                  //| 12:59.954+01:00)
                                                  //| TestPersonCreated(1914b48e-54de-4860-b73d-7cfee691df97,name27,2012-11-07T19
                                                  //| :13:00.127+01:00)
                                                  //| TestPersonCreated(1914b48e-54de-4860-b73d-7cfee691df97,name26,2012-11-07T19
                                                  //| :13:00.127+01:00)
                                                  //| TestPersonCreated(1914b48e-54de-4860-b73d-7cfee691df97,name30,2012-11-07T19
                                                  //| :13:00.127+01:00)
                                                  //| TestPersonCreated(1914b48e-54de-4860-b73d-7cfee691df97,name31,2012-11-07T19
                                                  //| :13:00.127+01:00)
                                                  //| TestPersonCreated(1914b48e-54de-4860-b73d-7cfee691df97,name32,2012-11-07T19
                                                  //| :13:00.127+01:00)
                                                  //| TestPersonCreated(1914b48e-54de-4860-b73d-7cfee691df97,name29,2012-11-07T19
                                                  //| :13:00.127+01:00)
                                                  //| TestPersonCreated(1914b48e-54de-4860-b73d-7cfee691df97,name28,2012-11-07T19
                                                  //| :13:00.126+01:00)
                                                  //| TestPersonCreated(1914b48e-54de-4860-b73d-7cfee691df97,name17,2012-11-07T19
                                                  //| :12:59.992+01:00)
                                                  //| TestPersonCreated(1914b48e-54de-4860-b73d-7cfee691df97,name13,2012-11-07T19
                                                  //| :12:59.993+01:00)
                                                  //| TestPersonCreated(1914b48e-54de-4860-b73d-7cfee691df97,name36,2012-11-07T19
                                                  //| :13:00.127+01:00)
                                                  //| TestPersonCreated(1914b48e-54de-4860-b73d-7cfee691df97,name37,2012-11-07T19
                                                  //| :13:00.127+01:00)
                                                  //| TestPersonCreated(1914b48e-54de-4860-b73d-7cfee691df97,name33,2012-11-07T19
                                                  //| :13:00.127+01:00)
                                                  //| TestPersonCreated(1914b48e-54de-4860-b73d-7cfee691df97,name34,2012-11-07T19
                                                  //| :13:00.127+01:00)
                                                  //| TestPersonCreated(1914b48e-54de-4860-b73d-7cfee691df97,name35,2012-11-07T19
                                                  //| :13:00.127+01:00)
                                                  //| ---------- FAILURE -----------------
                                                  //| EVENTS: 48
}