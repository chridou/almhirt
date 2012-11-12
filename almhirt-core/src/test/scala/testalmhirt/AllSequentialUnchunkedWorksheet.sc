package testalmhirt

import akka.dispatch.{ Await, Future }
import almhirt._
import almhirt.almvalidation.kit._
import almhirt.environment._
import test._
import almhirt.almfuture.inst._

// That this one fails is absolutely ok, since it does no harm, e.g. consistency is still maintained - REALLY?
object AllSequentialUnchunkedWorksheet extends TestAlmhirtKit {
  implicit val atMost = akka.util.Duration(3, "s")//> atMost  : akka.util.FiniteDuration = 3 seconds
  inTestAlmhirt { almhirt =>
    implicit val executor = almhirt.environment.context.system.futureDispatcher
    val idsAndNamesAndAdresses = Vector((for (i <- 0 until 10) yield (i, almhirt.getUuid, "Name%s".format(i), "Address%s".format(i))): _*)

		val repo = almhirt.environment.repositories.getForAggregateRoot[TestPerson, TestPersonEvent].awaitResult.forceResult

    idsAndNamesAndAdresses.foreach(x => almhirt.executeTrackedCommand(NewTestPerson(x._2, x._3), "A insert%s".format(x._1.toString)))
    idsAndNamesAndAdresses.foreach(x => almhirt.executeTrackedCommand(SetTestPersonAddress(x._2, Some(1), x._4), "A setaddress%s".format(x._1.toString)))
    idsAndNamesAndAdresses.foreach(x => almhirt.executeTrackedCommand(MoveBecauseOfMarriage(x._2, Some(2), "namemarriage%s".format(x._3), "addressmarriage%s".format(x._3)), "A updatemarriage%s".format(x._1.toString)))
    idsAndNamesAndAdresses.foreach(x => almhirt.executeTrackedCommand(ChangeTestPersonName(x._2, Some(4), "new%s".format(x._3)), "A updatename%s".format(x._1.toString)))

    val insertStatesFutures = idsAndNamesAndAdresses.map(x => almhirt.getResultOperationStateFor("A insert%s".format(x._1.toString)))
    val insertStatesRes = AlmFuture.sequence(insertStatesFutures).awaitResult
    println("--- insert done ---")
    if(insertStatesRes.isFailure) println(insertStatesRes)
//    insertStatesRes.foreach { insertStates =>
//      insertStates.foreach(x => x fold (f => println(f), succ => println(succ)))
//    }
  
    val update1StatesFutures = idsAndNamesAndAdresses.map(x => almhirt.getResultOperationStateFor("A setaddress%s".format(x._1.toString)))
    val update1StatesRes = AlmFuture.sequence(update1StatesFutures).awaitResult
    println("--- setaddress done ---")
    if(update1StatesRes.isFailure) println(update1StatesRes)
    update1StatesRes.foreach { updateStates =>
      updateStates.foreach(x => x fold (f => println(f), succ => println(succ)))
    }

    val update2StatesFutures = idsAndNamesAndAdresses.map(x => almhirt.getResultOperationStateFor("A updatemarriage%s".format(x._1.toString)))
    val update2StatesRes = AlmFuture.sequence(update2StatesFutures).awaitResult
    println("--- updatemarriage done ---")
    if(update2StatesRes.isFailure) println(update2StatesRes)
//    update2StatesRes.foreach { updateStates =>
//      updateStates.foreach(x => x fold (f => println(f), succ => println(succ)))
//    }


    val update3StatesFutures = idsAndNamesAndAdresses.map(x => almhirt.getResultOperationStateFor("A updatename%s".format(x._1.toString)))
    val update3StatesRes = AlmFuture.sequence(update3StatesFutures).awaitResult
    println("--- updatename done ---")
    if(update3StatesRes.isFailure) println(update3StatesRes)
   
//    almhirt.environment.eventLog.getAllEvents.awaitResult.forceResult.foreach(println)
//    AlmFuture.sequence(idsAndNamesAndAdresses.map(x => repo.get(x._2))).awaitResult.forceResult.foreach(println)
    
    
    
    update3StatesRes.map { updateStates =>
      updateStates.foreach(x => x fold (f => println(f), succ => println(succ)))
      updateStates
    }.fold(
      f => false,
      succ => succ.forall(_.isSuccess) && succ.forall(_.forceResult.isFinishedSuccesfully))
  }                                               //> cmd: Some(StringTrackingTicket(A setaddress3))
                                                  //| cmd: Some(StringTrackingTicket(A setaddress5))
                                                  //| cmd: Some(StringTrackingTicket(A updatename2))
                                                  //| cmd: Some(StringTrackingTicket(A setaddress2))
                                                  //| cmd: Some(StringTrackingTicket(A updatename4))
                                                  //| cmd: Some(StringTrackingTicket(A updatename0))
                                                  //| cmd: Some(StringTrackingTicket(A updatemarriage0))
                                                  //| cmd: Some(StringTrackingTicket(A updatename7))
                                                  //| cmd: Some(StringTrackingTicket(A setaddress1))
                                                  //| cmd: Some(StringTrackingTicket(A setaddress8))
                                                  //| cmd: Some(StringTrackingTicket(A updatemarriage1))
                                                  //| cmd: Some(StringTrackingTicket(A setaddress4))
                                                  //| cmd: Some(StringTrackingTicket(A updatename8))
                                                  //| cmd: Some(StringTrackingTicket(A updatemarriage8))
                                                  //| cmd: Some(StringTrackingTicket(A updatemarriage6))
                                                  //| cmd: Some(StringTrackingTicket(A setaddress0))
                                                  //| cmd: Some(StringTrackingTicket(A updatename5))
                                                  //| cmd: Some(StringTrackingTicket(A updatename1))
                                                  //| cmd: Some(StringTrackingTicket(A setaddress7))
                                                  //| cmd: Some(StringTrackingTicket(A updatemarriage2))
                                                  //| cmd: Some(StringTrackingTicket(A setaddress9))
                                                  //| cmd: Some(StringTrackingTicket(A updatename6))
                                                  //| cmd: Some(StringTrackingTicket(A updatemarriage3))
                                                  //| cmd: Some(StringTrackingTicket(A updatemarriage4))
                                                  //| cmd: Some(StringTrackingTicket(A updatemarriage9))
                                                  //| cmd: Some(StringTrackingTicket(A updatemarriage7))
                                                  //| cmd: Some(StringTrackingTicket(A setaddress6))
                                                  //| cmd: Some(StringTrackingTicket(A updatename3))
                                                  //| cmd: Some(StringTrackingTicket(A updatemarriage5))
                                                  //| cmd: Some(StringTrackingTicket(A updatename9))
                                                  //| --- insert done ---
                                                  //| --- setaddress done ---
                                                  //| Executed(StringTrackingTicket(A setaddress0))
                                                  //| Executed(StringTrackingTicket(A setaddress1))
                                                  //| Executed(StringTrackingTicket(A setaddress2))
                                                  //| Executed(StringTrackingTicket(A setaddress3))
                                                  //| Executed(StringTrackingTicket(A setaddress4))
                                                  //| Executed(StringTrackingTicket(A setaddress5))
                                                  //| Executed(StringTrackingTicket(A setaddress6))
                                                  //| Executed(StringTrackingTicket(A setaddress7))
                                                  //| Executed(StringTrackingTicket(A setaddress8))
                                                  //| Executed(StringTrackingTicket(A setaddress9))
                                                  //| --- updatemarriage done ---
                                                  //| --- updatename done ---
                                                  //| NotExecuted(StringTrackingTicket(A updatename0),almhirt.CollisionProblem
                                                  //| Refused to handle command: Versions do not match. Current version is '1', t
                                                  //| argetted version is '4'. The refused command is 'ChangeTestPersonName(6c434
                                                  //| 21d-fe76-467c-8848-ba9e5be71451,Some(4),newName0)'
                                                  //| Category: ApplicationProblem
                                                  //| Severity: Minor
                                                  //| Arguments: Map()
                                                  //| )
                                                  //| NotExecuted(StringTrackingTicket(A updatename1),almhirt.CollisionProblem
                                                  //| Refused to handle command: Versions do not match. Current version is '1', t
                                                  //| argetted version is '4'. The refused command is 'ChangeTestPersonName(ab9cb
                                                  //| e3f-1b5d-4934-9a0a-611cd6e94a85,Some(4),newName1)'
                                                  //| Category: ApplicationProblem
                                                  //| Severity: Minor
                                                  //| Arguments: Map()
                                                  //| )
                                                  //| NotExecuted(StringTrackingTicket(A updatename2),almhirt.CollisionProblem
                                                  //| Refused to handle command: Versions do not match. Current version is '1', t
                                                  //| argetted version is '4'. The refused command is 'ChangeTestPersonName(6ecc0
                                                  //| 2c1-6e42-4370-b412-c71773bb4f1e,Some(4),newName2)'
                                                  //| Category: ApplicationProblem
                                                  //| Severity: Minor
                                                  //| Arguments: Map()
                                                  //| )
                                                  //| NotExecuted(StringTrackingTicket(A updatename3),almhirt.CollisionProblem
                                                  //| Refused to handle command: Versions do not match. Current version is '1', t
                                                  //| argetted version is '4'. The refused command is 'ChangeTestPersonName(c0f76
                                                  //| 8c8-d010-46a8-b3ca-34ff3a61be47,Some(4),newName3)'
                                                  //| Category: ApplicationProblem
                                                  //| Severity: Minor
                                                  //| Arguments: Map()
                                                  //| )
                                                  //| NotExecuted(StringTrackingTicket(A updatename4),almhirt.CollisionProblem
                                                  //| Refused to handle command: Versions do not match. Current version is '1', t
                                                  //| argetted version is '4'. The refused command is 'ChangeTestPersonName(b6971
                                                  //| a60-b9d5-42e5-9141-7846bf557968,Some(4),newName4)'
                                                  //| Category: ApplicationProblem
                                                  //| Severity: Minor
                                                  //| Arguments: Map()
                                                  //| )
                                                  //| NotExecuted(StringTrackingTicket(A updatename5),almhirt.CollisionProblem
                                                  //| Refused to handle command: Versions do not match. Current version is '1', t
                                                  //| argetted version is '4'. The refused command is 'ChangeTestPersonName(d7bb5
                                                  //| 1bc-2d78-467f-a875-f2dd3a6beda0,Some(4),newName5)'
                                                  //| Category: ApplicationProblem
                                                  //| Severity: Minor
                                                  //| Arguments: Map()
                                                  //| )
                                                  //| NotExecuted(StringTrackingTicket(A updatename6),almhirt.CollisionProblem
                                                  //| Refused to handle command: Versions do not match. Current version is '1', t
                                                  //| argetted version is '4'. The refused command is 'ChangeTestPersonName(25d3b
                                                  //| d8e-13cd-4022-982a-886c60ab50cf,Some(4),newName6)'
                                                  //| Category: ApplicationProblem
                                                  //| Severity: Minor
                                                  //| Arguments: Map()
                                                  //| )
                                                  //| NotExecuted(StringTrackingTicket(A updatename7),almhirt.CollisionProblem
                                                  //| Refused to handle command: Versions do not match. Current version is '1', t
                                                  //| argetted version is '4'. The refused command is 'ChangeTestPersonName(815d4
                                                  //| 94c-4dbd-4e84-b32e-163fd19d9775,Some(4),newName7)'
                                                  //| Category: ApplicationProblem
                                                  //| Severity: Minor
                                                  //| Arguments: Map()
                                                  //| )
                                                  //| NotExecuted(StringTrackingTicket(A updatename8),almhirt.CollisionProblem
                                                  //| Refused to handle command: Versions do not match. Current version is '1', t
                                                  //| argetted version is '4'. The refused command is 'ChangeTestPersonName(6b0be
                                                  //| 214-7c15-4dd9-b905-5bf330d19026,Some(4),newName8)'
                                                  //| Category: ApplicationProblem
                                                  //| Severity: Minor
                                                  //| Arguments: Map()
                                                  //| )
                                                  //| NotExecuted(StringTrackingTicket(A updatename9),almhirt.CollisionProblem
                                                  //| Refused to handle command: Versions do not match. Current version is '1', t
                                                  //| argetted version is '4'. The refused command is 'ChangeTestPersonName(d7acb
                                                  //| 865-4a24-4822-ae60-90eb0b43fd6e,Some(4),newName9)'
                                                  //| Category: ApplicationProblem
                                                  //| Severity: Minor
                                                  //| Arguments: Map()
                                                  //| )
                                                  //| res0: Boolean = false
  }