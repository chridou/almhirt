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
                                                  //| Executed(11)
                                                  //| Executed(12)
                                                  //| Executed(13)
                                                  //| Executed(14)
                                                  //| Executed(15)
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
                                                  //| TestPersonCreated(3681c138-aaaa-4062-827e-d8d5537b44da,name6,2012-11-08T15:
                                                  //| 25:42.127+01:00)
                                                  //| TestPersonCreated(3681c138-aaaa-4062-827e-d8d5537b44da,name4,2012-11-08T15:
                                                  //| 25:42.127+01:00)
                                                  //| TestPersonCreated(3681c138-aaaa-4062-827e-d8d5537b44da,name12,2012-11-08T15
                                                  //| :25:42.127+01:00)
                                                  //| TestPersonCreated(3681c138-aaaa-4062-827e-d8d5537b44da,name8,2012-11-08T15:
                                                  //| 25:42.127+01:00)
                                                  //| TestPersonCreated(3681c138-aaaa-4062-827e-d8d5537b44da,name23,2012-11-08T15
                                                  //| :25:42.127+01:00)
                                                  //| TestPersonCreated(3681c138-aaaa-4062-827e-d8d5537b44da,name16,2012-11-08T15
                                                  //| :25:42.127+01:00)
                                                  //| TestPersonCreated(3681c138-aaaa-4062-827e-d8d5537b44da,name5,2012-11-08T15:
                                                  //| 25:42.127+01:00)
                                                  //| TestPersonCreated(3681c138-aaaa-4062-827e-d8d5537b44da,name24,2012-11-08T15
                                                  //| :25:42.127+01:00)
                                                  //| TestPersonCreated(3681c138-aaaa-4062-827e-d8d5537b44da,name2,2012-11-08T15:
                                                  //| 25:42.127+01:00)
                                                  //| TestPersonCreated(3681c138-aaaa-4062-827e-d8d5537b44da,name19,2012-11-08T15
                                                  //| :25:42.127+01:00)
                                                  //| TestPersonCreated(3681c138-aaaa-4062-827e-d8d5537b44da,name25,2012-11-08T15
                                                  //| :25:42.284+01:00)
                                                  //| TestPersonCreated(3681c138-aaaa-4062-827e-d8d5537b44da,name22,2012-11-08T15
                                                  //| :25:42.127+01:00)
                                                  //| TestPersonCreated(3681c138-aaaa-4062-827e-d8d5537b44da,name26,2012-11-08T15
                                                  //| :25:42.291+01:00)
                                                  //| TestPersonCreated(3681c138-aaaa-4062-827e-d8d5537b44da,name21,2012-11-08T15
                                                  //| :25:42.128+01:00)
                                                  //| TestPersonCreated(3681c138-aaaa-4062-827e-d8d5537b44da,name13,2012-11-08T15
                                                  //| :25:42.127+01:00)
                                                  //| TestPersonCreated(3681c138-aaaa-4062-827e-d8d5537b44da,name27,2012-11-08T15
                                                  //| :25:42.291+01:00)
                                                  //| TestPersonCreated(3681c138-aaaa-4062-827e-d8d5537b44da,name18,2012-11-08T15
                                                  //| :25:42.128+01:00)
                                                  //| TestPersonCreated(3681c138-aaaa-4062-827e-d8d5537b44da,name7,2012-11-08T15:
                                                  //| 25:42.127+01:00)
                                                  //| TestPersonCreated(3681c138-aaaa-4062-827e-d8d5537b44da,name29,2012-11-08T15
                                                  //| :25:42.291+01:00)
                                                  //| TestPersonCreated(3681c138-aaaa-4062-827e-d8d5537b44da,name28,2012-11-08T15
                                                  //| :25:42.291+01:00)
                                                  //| TestPersonCreated(3681c138-aaaa-4062-827e-d8d5537b44da,name30,2012-11-08T15
                                                  //| :25:42.291+01:00)
                                                  //| TestPersonCreated(3681c138-aaaa-4062-827e-d8d5537b44da,name33,2012-11-08T15
                                                  //| :25:42.291+01:00)
                                                  //| TestPersonCreated(3681c138-aaaa-4062-827e-d8d5537b44da,name1,2012-11-08T15:
                                                  //| 25:42.127+01:00)
                                                  //| TestPersonCreated(3681c138-aaaa-4062-827e-d8d5537b44da,name20,2012-11-08T15
                                                  //| :25:42.128+01:00)
                                                  //| TestPersonCreated(3681c138-aaaa-4062-827e-d8d5537b44da,name14,2012-11-08T15
                                                  //| :25:42.127+01:00)
                                                  //| TestPersonCreated(3681c138-aaaa-4062-827e-d8d5537b44da,name31,2012-11-08T15
                                                  //| :25:42.291+01:00)
                                                  //| TestPersonCreated(3681c138-aaaa-4062-827e-d8d5537b44da,name9,2012-11-08T15:
                                                  //| 25:42.128+01:00)
                                                  //| TestPersonCreated(3681c138-aaaa-4062-827e-d8d5537b44da,name32,2012-11-08T15
                                                  //| :25:42.291+01:00)
                                                  //| TestPersonCreated(3681c138-aaaa-4062-827e-d8d5537b44da,name34,2012-11-08T15
                                                  //| :25:42.292+01:00)
                                                  //| TestPersonCreated(3681c138-aaaa-4062-827e-d8d5537b44da,name3,2012-11-08T15:
                                                  //| 25:42.127+01:00)
                                                  //| TestPersonCreated(3681c138-aaaa-4062-827e-d8d5537b44da,name35,2012-11-08T15
                                                  //| :25:42.292+01:00)
                                                  //| TestPersonCreated(3681c138-aaaa-4062-827e-d8d5537b44da,name15,2012-11-08T15
                                                  //| :25:42.127+01:00)
                                                  //| TestPersonCreated(3681c138-aaaa-4062-827e-d8d5537b44da,name36,2012-11-08T15
                                                  //| :25:42.292+01:00)
                                                  //| TestPersonCreated(3681c138-aaaa-4062-827e-d8d5537b44da,name17,2012-11-08T15
                                                  //| :25:42.127+01:00)
                                                  //| TestPersonCreated(3681c138-aaaa-4062-827e-d8d5537b44da,name37,2012-11-08T15
                                                  //| :25:42.292+01:00)
                                                  //| TestPersonCreated(3681c138-aaaa-4062-827e-d8d5537b44da,name38,2012-11-08T15
                                                  //| :25:42.292+01:00)
                                                  //| TestPersonCreated(3681c138-aaaa-4062-827e-d8d5537b44da,name10,2012-11-08T15
                                                  //| :25:42.127+01:00)
                                                  //| TestPersonCreated(3681c138-aaaa-4062-827e-d8d5537b44da,name39,2012-11-08T15
                                                  //| :25:42.292+01:00)
                                                  //| TestPersonCreated(3681c138-aaaa-4062-827e-d8d5537b44da,name40,2012-11-08T15
                                                  //| :25:42.292+01:00)
                                                  //| TestPersonCreated(3681c138-aaaa-4062-827e-d8d5537b44da,name11,2012-11-08T15
                                                  //| :25:42.127+01:00)
                                                  //| TestPersonCreated(3681c138-aaaa-4062-827e-d8d5537b44da,name41,2012-11-08T15
                                                  //| :25:42.292+01:00)
                                                  //| TestPersonCreated(3681c138-aaaa-4062-827e-d8d5537b44da,name43,2012-11-08T15
                                                  //| :25:42.292+01:00)
                                                  //| TestPersonCreated(3681c138-aaaa-4062-827e-d8d5537b44da,name45,2012-11-08T15
                                                  //| :25:42.293+01:00)
                                                  //| TestPersonCreated(3681c138-aaaa-4062-827e-d8d5537b44da,name44,2012-11-08T15
                                                  //| :25:42.293+01:00)
                                                  //| TestPersonCreated(3681c138-aaaa-4062-827e-d8d5537b44da,name46,2012-11-08T15
                                                  //| :25:42.293+01:00)
                                                  //| TestPersonCreated(3681c138-aaaa-4062-827e-d8d5537b44da,name42,2012-11-08T15
                                                  //| :25:42.292+01:00)
                                                  //| TestPersonCreated(3681c138-aaaa-4062-827e-d8d5537b44da,name47,2012-11-08T15
                                                  //| :25:42.293+01:00)
                                                  //| TestPersonCreated(3681c138-aaaa-4062-827e-d8d5537b44da,name48,2012-11-08T15
                                                  //| :25:42.293+01:00)
                                                  //| TestPersonCreated(3681c138-aaaa-4062-827e-d8d5537b44da,name49,2012-11-08T15
                                                  //| :25:42.293+01:00)
                                                  //| TestPersonCreated(3681c138-aaaa-4062-827e-d8d5537b44da,name50,2012-11-08T15
                                                  //| :25:42.293+01:00)
                                                  //| ---------- FAILURE -----------------
                                                  //| EVENTS: 50
}