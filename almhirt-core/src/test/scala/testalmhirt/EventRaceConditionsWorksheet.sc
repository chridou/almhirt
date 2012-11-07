package testalmhirt

import akka.dispatch.{ Await, Future }
import almhirt._
import almhirt.almvalidation.kit._
import almhirt.environment._
import test._
import almhirt.almfuture.inst._
import almhirt.commanding.CommandEnvelope

object EventRaceConditionsWorksheet extends TestAlmhirtKit {
  implicit val atMost = akka.util.Duration(3, "s")//> atMost  : akka.util.FiniteDuration = 3 seconds
  inTestAlmhirt { almhirtInstance =>
    implicit val executor = almhirtInstance.environment.context.system.futureDispatcher
    val repo = almhirtInstance.environment.repositories.getForAggregateRoot[TestPerson, TestPersonEvent].awaitResult.forceResult
    val count = 100
    val id = almhirtInstance.getUuid

    almhirtInstance.executeTrackedCommand(NewTestPerson(id, "InitialName"), "CREATE")
    almhirtInstance.getResultOperationStateFor("CREATE").awaitResult

    val commandEnvelopes = Vector((for (i <- 0 to count) yield CommandEnvelope(ChangeTestPersonName(id, Some(1), "name%d".format(i)), Some(i.toString))): _*)
    
    commandEnvelopes.foreach(almhirtInstance.executeCommand(_))
    val statesFutures = commandEnvelopes.map(x => almhirtInstance.getResultOperationStateFor(x.ticket.get))
    val statesFuturesRes = AlmFuture.sequence(statesFutures).awaitResult
    
    val events = almhirtInstance.environment.eventLog.getAllEvents.awaitResult.forceResult
    events.foreach(println)
    if(events.size == 2) println ("SUCCESS") else println ("FAILURE")
  }                                               //> TestPersonCreated(1e0b831a-3820-441d-a655-a409fb6e0b70,InitialName,2012-11-
                                                  //| 07T16:09:13.584+01:00)
                                                  //| TestPersonNameChanged(1e0b831a-3820-441d-a655-a409fb6e0b70,1,name9,2012-11-
                                                  //| 07T16:09:13.854+01:00)
                                                  //| TestPersonNameChanged(1e0b831a-3820-441d-a655-a409fb6e0b70,1,name14,2012-11
                                                  //| -07T16:09:13.854+01:00)
                                                  //| TestPersonNameChanged(1e0b831a-3820-441d-a655-a409fb6e0b70,1,name3,2012-11-
                                                  //| 07T16:09:13.854+01:00)
                                                  //| TestPersonNameChanged(1e0b831a-3820-441d-a655-a409fb6e0b70,1,name2,2012-11-
                                                  //| 07T16:09:13.854+01:00)
                                                  //| TestPersonNameChanged(1e0b831a-3820-441d-a655-a409fb6e0b70,1,name10,2012-11
                                                  //| -07T16:09:13.854+01:00)
                                                  //| TestPersonNameChanged(1e0b831a-3820-441d-a655-a409fb6e0b70,1,name16,2012-11
                                                  //| -07T16:09:13.854+01:00)
                                                  //| TestPersonNameChanged(1e0b831a-3820-441d-a655-a409fb6e0b70,1,name17,2012-11
                                                  //| -07T16:09:13.854+01:00)
                                                  //| TestPersonNameChanged(1e0b831a-3820-441d-a655-a409fb6e0b70,1,name15,2012-11
                                                  //| -07T16:09:13.854+01:00)
                                                  //| TestPersonNameChanged(1e0b831a-3820-441d-a655-a409fb6e0b70,1,name18,2012-11
                                                  //| -07T16:09:13.854+01:00)
                                                  //| TestPersonNameChanged(1e0b831a-3820-441d-a655-a409fb6e0b70,1,name21,2012-11
                                                  //| -07T16:09:13.854+01:00)
                                                  //| TestPersonNameChanged(1e0b831a-3820-441d-a655-a409fb6e0b70,1,name23,2012-11
                                                  //| -07T16:09:13.854+01:00)
                                                  //| TestPersonNameChanged(1e0b831a-3820-441d-a655-a409fb6e0b70,1,name20,2012-11
                                                  //| -07T16:09:13.854+01:00)
                                                  //| TestPersonNameChanged(1e0b831a-3820-441d-a655-a409fb6e0b70,1,name25,2012-11
                                                  //| -07T16:09:13.854+01:00)
                                                  //| TestPersonNameChanged(1e0b831a-3820-441d-a655-a409fb6e0b70,1,name28,2012-11
                                                  //| -07T16:09:13.854+01:00)
                                                  //| TestPersonNameChanged(1e0b831a-3820-441d-a655-a409fb6e0b70,1,name22,2012-11
                                                  //| -07T16:09:13.854+01:00)
                                                  //| TestPersonNameChanged(1e0b831a-3820-441d-a655-a409fb6e0b70,1,name0,2012-11-
                                                  //| 07T16:09:13.854+01:00)
                                                  //| TestPersonNameChanged(1e0b831a-3820-441d-a655-a409fb6e0b70,1,name26,2012-11
                                                  //| -07T16:09:13.854+01:00)
                                                  //| TestPersonNameChanged(1e0b831a-3820-441d-a655-a409fb6e0b70,1,name13,2012-11
                                                  //| -07T16:09:13.854+01:00)
                                                  //| TestPersonNameChanged(1e0b831a-3820-441d-a655-a409fb6e0b70,1,name4,2012-11-
                                                  //| 07T16:09:13.854+01:00)
                                                  //| TestPersonNameChanged(1e0b831a-3820-441d-a655-a409fb6e0b70,1,name7,2012-11-
                                                  //| 07T16:09:13.854+01:00)
                                                  //| TestPersonNameChanged(1e0b831a-3820-441d-a655-a409fb6e0b70,1,name5,2012-11-
                                                  //| 07T16:09:13.854+01:00)
                                                  //| TestPersonNameChanged(1e0b831a-3820-441d-a655-a409fb6e0b70,1,name31,2012-11
                                                  //| -07T16:09:13.854+01:00)
                                                  //| TestPersonNameChanged(1e0b831a-3820-441d-a655-a409fb6e0b70,1,name33,2012-11
                                                  //| -07T16:09:13.854+01:00)
                                                  //| TestPersonNameChanged(1e0b831a-3820-441d-a655-a409fb6e0b70,1,name27,2012-11
                                                  //| -07T16:09:13.854+01:00)
                                                  //| TestPersonNameChanged(1e0b831a-3820-441d-a655-a409fb6e0b70,1,name32,2012-11
                                                  //| -07T16:09:13.854+01:00)
                                                  //| TestPersonNameChanged(1e0b831a-3820-441d-a655-a409fb6e0b70,1,name36,2012-11
                                                  //| -07T16:09:13.854+01:00)
                                                  //| TestPersonNameChanged(1e0b831a-3820-441d-a655-a409fb6e0b70,1,name35,2012-11
                                                  //| -07T16:09:13.854+01:00)
                                                  //| FAILURE

}