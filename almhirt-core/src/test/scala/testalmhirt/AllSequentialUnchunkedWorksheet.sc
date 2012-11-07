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
    val idsAndNamesAndAdresses = Vector((for (i <- 0 until 100) yield (i, almhirt.getUuid, "Name%s".format(i), "Address%s".format(i))): _*)

		val repo = almhirt.environment.repositories.getForAggregateRoot[TestPerson, TestPersonEvent].awaitResult.forceResult

    idsAndNamesAndAdresses.foreach(x => almhirt.executeTrackedCommand(NewTestPerson(x._2, x._3), "A insert%s".format(x._1.toString)))
    idsAndNamesAndAdresses.foreach(x => almhirt.executeTrackedCommand(SetTestPersonAddress(x._2, Some(1), x._4), "A setaddress%s".format(x._1.toString)))
    idsAndNamesAndAdresses.foreach(x => almhirt.executeTrackedCommand(MoveBecauseOfMarriage(x._2, Some(2), "namemarriage%s".format(x._3), "addressmarriage%s".format(x._3)), "A updatemarriage%s".format(x._1.toString)))
    idsAndNamesAndAdresses.foreach(x => almhirt.executeTrackedCommand(ChangeTestPersonName(x._2, Some(4), "new%s".format(x._3)), "A updatename%s".format(x._1.toString)))


    val insertStatesFutures = idsAndNamesAndAdresses.map(x => almhirt.getResultOperationStateFor("A insert%s".format(x._1.toString)))
    val insertStatesRes = AlmFuture.sequence(insertStatesFutures).awaitResult
    println("--- insert done ---")
    if(insertStatesRes.isFailure) println(insertStatesRes)
    insertStatesRes.foreach { insertStates =>
      insertStates.foreach(x => x fold (f => println(f), succ => println(succ)))
    }
  
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
    update2StatesRes.foreach { updateStates =>
      updateStates.foreach(x => x fold (f => println(f), succ => println(succ)))
    }

 
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
  }                                               //> --- insert done ---
                                                  //| Executed(A insert0)
                                                  //| Executed(A insert1)
                                                  //| Executed(A insert2)
                                                  //| Executed(A insert3)
                                                  //| Executed(A insert4)
                                                  //| Executed(A insert5)
                                                  //| Executed(A insert6)
                                                  //| Executed(A insert7)
                                                  //| Executed(A insert8)
                                                  //| Executed(A insert9)
                                                  //| Executed(A insert10)
                                                  //| Executed(A insert11)
                                                  //| Executed(A insert12)
                                                  //| Executed(A insert13)
                                                  //| Executed(A insert14)
                                                  //| Executed(A insert15)
                                                  //| Executed(A insert16)
                                                  //| Executed(A insert17)
                                                  //| Executed(A insert18)
                                                  //| Executed(A insert19)
                                                  //| Executed(A insert20)
                                                  //| Executed(A insert21)
                                                  //| Executed(A insert22)
                                                  //| Executed(A insert23)
                                                  //| Executed(A insert24)
                                                  //| Executed(A insert25)
                                                  //| Executed(A insert26)
                                                  //| Executed(A insert27)
                                                  //| Executed(A insert28)
                                                  //| Executed(A insert29)
                                                  //| Executed(A insert30)
                                                  //| Executed(A insert31)
                                                  //| Executed(A insert32)
                                                  //| Executed(A insert33)
                                                  //| Executed(A insert34)
                                                  //| Executed(A insert35)
                                                  //| Executed(A insert36)
                                                  //| Executed(A insert37)
                                                  //| Executed(A insert38)
                                                  //| Executed(A insert39)
                                                  //| Executed(A insert40)
                                                  //| Executed(A insert41)
                                                  //| Executed(A insert42)
                                                  //| Executed(A insert43)
                                                  //| Executed(A insert44)
                                                  //| Executed(A insert45)
                                                  //| Executed(A insert46)
                                                  //| Executed(A insert47)
                                                  //| Executed(A insert48)
                                                  //| Executed(A insert49)
                                                  //| Executed(A insert50)
                                                  //| Executed(A insert51)
                                                  //| Executed(A insert52)
                                                  //| Executed(A insert53)
                                                  //| Executed(A insert54)
                                                  //| Executed(A insert55)
                                                  //| Executed(A insert56)
                                                  //| Executed(A insert57)
                                                  //| Executed(A insert58)
                                                  //| Executed(A insert59)
                                                  //| Executed(A insert60)
                                                  //| Executed(A insert61)
                                                  //| Executed(A insert62)
                                                  //| Executed(A insert63)
                                                  //| Executed(A insert64)
                                                  //| Executed(A insert65)
                                                  //| Executed(A insert66)
                                                  //| Executed(A insert67)
                                                  //| Executed(A insert68)
                                                  //| Executed(A insert69)
                                                  //| Executed(A insert70)
                                                  //| Executed(A insert71)
                                                  //| Executed(A insert72)
                                                  //| Executed(A insert73)
                                                  //| Executed(A insert74)
                                                  //| Executed(A insert75)
                                                  //| Executed(A insert76)
                                                  //| Executed(A insert77)
                                                  //| Executed(A insert78)
                                                  //| Executed(A insert79)
                                                  //| Executed(A insert80)
                                                  //| Executed(A insert81)
                                                  //| Executed(A insert82)
                                                  //| Executed(A insert83)
                                                  //| Executed(A insert84)
                                                  //| Executed(A insert85)
                                                  //| Executed(A insert86)
                                                  //| Executed(A insert87)
                                                  //| Executed(A insert88)
                                                  //| Executed(A insert89)
                                                  //| Executed(A insert90)
                                                  //| Executed(A insert91)
                                                  //| Executed(A insert92)
                                                  //| Executed(A insert93)
                                                  //| Executed(A insert94)
                                                  //| Executed(A insert95)
                                                  //| Executed(A insert96)
                                                  //| Executed(A insert97)
                                                  //| Executed(A insert98)
                                                  //| Executed(A insert99)
                                                  //| --- setaddress done ---
                                                  //| Executed(A setaddress0)
                                                  //| Executed(A setaddress1)
                                                  //| Executed(A setaddress2)
                                                  //| Executed(A setaddress3)
                                                  //| Executed(A setaddress4)
                                                  //| Executed(A setaddress5)
                                                  //| Executed(A setaddress6)
                                                  //| Executed(A setaddress7)
                                                  //| Executed(A setaddress8)
                                                  //| Executed(A setaddress9)
                                                  //| Executed(A setaddress10)
                                                  //| Executed(A setaddress11)
                                                  //| Executed(A setaddress12)
                                                  //| Executed(A setaddress13)
                                                  //| Executed(A setaddress14)
                                                  //| Executed(A setaddress15)
                                                  //| Executed(A setaddress16)
                                                  //| Executed(A setaddress17)
                                                  //| Executed(A setaddress18)
                                                  //| Executed(A setaddress19)
                                                  //| Executed(A setaddress20)
                                                  //| Executed(A setaddress21)
                                                  //| Executed(A setaddress22)
                                                  //| Executed(A setaddress23)
                                                  //| Executed(A setaddress24)
                                                  //| Executed(A setaddress25)
                                                  //| Executed(A setaddress26)
                                                  //| Executed(A setaddress27)
                                                  //| Executed(A setaddress28)
                                                  //| Executed(A setaddress29)
                                                  //| Executed(A setaddress30)
                                                  //| Executed(A setaddress31)
                                                  //| Executed(A setaddress32)
                                                  //| Executed(A setaddress33)
                                                  //| Executed(A setaddress34)
                                                  //| Executed(A setaddress35)
                                                  //| Executed(A setaddress36)
                                                  //| Executed(A setaddress37)
                                                  //| Executed(A setaddress38)
                                                  //| Executed(A setaddress39)
                                                  //| Executed(A setaddress40)
                                                  //| Executed(A setaddress41)
                                                  //| Executed(A setaddress42)
                                                  //| Executed(A setaddress43)
                                                  //| Executed(A setaddress44)
                                                  //| Executed(A setaddress45)
                                                  //| Executed(A setaddress46)
                                                  //| Executed(A setaddress47)
                                                  //| Executed(A setaddress48)
                                                  //| Executed(A setaddress49)
                                                  //| Executed(A setaddress50)
                                                  //| Executed(A setaddress51)
                                                  //| Executed(A setaddress52)
                                                  //| Executed(A setaddress53)
                                                  //| Executed(A setaddress54)
                                                  //| Executed(A setaddress55)
                                                  //| Executed(A setaddress56)
                                                  //| Executed(A setaddress57)
                                                  //| Executed(A setaddress58)
                                                  //| Executed(A setaddress59)
                                                  //| Executed(A setaddress60)
                                                  //| Executed(A setaddress61)
                                                  //| Executed(A setaddress62)
                                                  //| Executed(A setaddress63)
                                                  //| Executed(A setaddress64)
                                                  //| Executed(A setaddress65)
                                                  //| Executed(A setaddress66)
                                                  //| Executed(A setaddress67)
                                                  //| Executed(A setaddress68)
                                                  //| Executed(A setaddress69)
                                                  //| Executed(A setaddress70)
                                                  //| Executed(A setaddress71)
                                                  //| Executed(A setaddress72)
                                                  //| Executed(A setaddress73)
                                                  //| Executed(A setaddress74)
                                                  //| Executed(A setaddress75)
                                                  //| Executed(A setaddress76)
                                                  //| Executed(A setaddress77)
                                                  //| Executed(A setaddress78)
                                                  //| Executed(A setaddress79)
                                                  //| Executed(A setaddress80)
                                                  //| Executed(A setaddress81)
                                                  //| Executed(A setaddress82)
                                                  //| Executed(A setaddress83)
                                                  //| Executed(A setaddress84)
                                                  //| Executed(A setaddress85)
                                                  //| Executed(A setaddress86)
                                                  //| Executed(A setaddress87)
                                                  //| Executed(A setaddress88)
                                                  //| Executed(A setaddress89)
                                                  //| Executed(A setaddress90)
                                                  //| Executed(A setaddress91)
                                                  //| Executed(A setaddress92)
                                                  //| Executed(A setaddress93)
                                                  //| Executed(A setaddress94)
                                                  //| Executed(A setaddress95)
                                                  //| Executed(A setaddress96)
                                                  //| Executed(A setaddress97)
                                                  //| Executed(A setaddress98)
                                                  //| Executed(A setaddress99)
                                                  //| --- updatemarriage done ---
                                                  //| NotExecuted(A updatemarriage0,almhirt.CollisionProblem
                                                  //| Refused to handle command: Versions do not match. Current version is '1', t
                                                  //| argetted version is '2'. The refused command is 'MoveBecauseOfMarriage(4123
                                                  //| 2530-37da-4a02-8d6d-d11db0b22cf3,Some(2),namemarriageName0,addressmarriageN
                                                  //| ame0)'
                                                  //| Category: ApplicationProblem
                                                  //| Severity: Minor
                                                  //| Arguments: Map()
                                                  //| )
                                                  //| NotExecuted(A updatemarriage1,almhirt.CollisionProblem
                                                  //| Refused to handle command: Versions do not match. Current version is '1', t
                                                  //| argetted version is '2'. The refused command is 'MoveBecauseOfMarriage(c5ce
                                                  //| 9132-4984-48a7-82df-702289fbaeee,Some(2),namemarriageName1,addressmarriageN
                                                  //| ame1)'
                                                  //| Category: ApplicationProblem
                                                  //| Severity: Minor
                                                  //| Arguments: Map()
                                                  //| )
                                                  //| NotExecuted(A updatemarriage2,almhirt.CollisionProblem
                                                  //| Refused to handle command: Versions do not match. Current version is '1', t
                                                  //| argetted version is '2'. The refused command is 'MoveBecauseOfMarriage(aa78
                                                  //| f073-f44e-4b5c-8167-2fcd14e8bac0,Some(2),namemarriageName2,addressmarriageN
                                                  //| ame2)'
                                                  //| Category: ApplicationProblem
                                                  //| Severity: Minor
                                                  //| Arguments: Map()
                                                  //| )
                                                  //| NotExecuted(A updatemarriage3,almhirt.CollisionProblem
                                                  //| Refused to handle command: Versions do not match. Current version is '1', t
                                                  //| argetted version is '2'. The refused command is 'MoveBecauseOfMarriage(5c10
                                                  //| bd55-b8c8-4fa3-9962-2179aca3b0e9,Some(2),namemarriageName3,addressmarriageN
                                                  //| ame3)'
                                                  //| Category: ApplicationProblem
                                                  //| Severity: Minor
                                                  //| Arguments: Map()
                                                  //| )
                                                  //| NotExecuted(A updatemarriage4,almhirt.CollisionProblem
                                                  //| Refused to handle command: Versions do not match. Current version is '1', t
                                                  //| argetted version is '2'. The refused command is 'MoveBecauseOfMarriage(0cb8
                                                  //| b0c2-b82b-47bf-99a2-1436da61c607,Some(2),namemarriageName4,addressmarriageN
                                                  //| ame4)'
                                                  //| Category: ApplicationProblem
                                                  //| Severity: Minor
                                                  //| Arguments: Map()
                                                  //| )
                                                  //| NotExecuted(A updatemarriage5,almhirt.CollisionProblem
                                                  //| Refused to handle command: Versions do not match. Current version is '1', t
                                                  //| argetted version is '2'. The refused command is 'MoveBecauseOfMarriage(3589
                                                  //| 28da-f354-44c5-97be-8e224b62a6b2,Some(2),namemarriageName5,addressmarriageN
                                                  //| ame5)'
                                                  //| Category: ApplicationProblem
                                                  //| Severity: Minor
                                                  //| Arguments: Map()
                                                  //| )
                                                  //| NotExecuted(A updatemarriage6,almhirt.CollisionProblem
                                                  //| Refused to handle command: Versions do not match. Current version is '1', t
                                                  //| argetted version is '2'. The refused command is 'MoveBecauseOfMarriage(848b
                                                  //| 8c22-b9c4-4dc3-a81f-1200f83a0f50,Some(2),namemarriageName6,addressmarriageN
                                                  //| ame6)'
                                                  //| Category: ApplicationProblem
                                                  //| Severity: Minor
                                                  //| Arguments: Map()
                                                  //| )
                                                  //| NotExecuted(A updatemarriage7,almhirt.CollisionProblem
                                                  //| Refused to handle command: Versions do not match. Current version is '1', t
                                                  //| argetted version is '2'. The refused command is 'MoveBecauseOfMarriage(d5b8
                                                  //| 3031-c643-4234-ba61-51f378c20403,Some(2),namemarriageName7,addressmarriageN
                                                  //| ame7)'
                                                  //| Category: ApplicationProblem
                                                  //| Severity: Minor
                                                  //| Arguments: Map()
                                                  //| )
                                                  //| NotExecuted(A updatemarriage8,almhirt.CollisionProblem
                                                  //| Refused to handle command: Versions do not match. Current version is '1', t
                                                  //| argetted version is '2'. The refused command is 'MoveBecauseOfMarriage(aa63
                                                  //| 4614-86d9-4507-9206-9adc82e2b230,Some(2),namemarriageName8,addressmarriageN
                                                  //| ame8)'
                                                  //| Category: ApplicationProblem
                                                  //| Severity: Minor
                                                  //| Arguments: Map()
                                                  //| )
                                                  //| NotExecuted(A updatemarriage9,almhirt.CollisionProblem
                                                  //| Refused to handle command: Versions do not match. Current version is '1', t
                                                  //| argetted version is '2'. The refused command is 'MoveBecauseOfMarriage(92ea
                                                  //| 5900-b9c8-4d91-a4f6-3207e4b47398,Some(2),namemarriageName9,addressmarriageN
                                                  //| ame9)'
                                                  //| Category: ApplicationProblem
                                                  //| Severity: Minor
                                                  //| Arguments: Map()
                                                  //| )
                                                  //| NotExecuted(A updatemarriage10,almhirt.CollisionProblem
                                                  //| Refused to handle command: Versions do not match. Current version is '1', t
                                                  //| argetted version is '2'. The refused command is 'MoveBecauseOfMarriage(41f5
                                                  //| 6736-d98a-4333-be9f-ed5dea102a2c,Some(2),namemarriageName10,addressmarriage
                                                  //| Name10)'
                                                  //| Category: ApplicationProblem
                                                  //| Severity: Minor
                                                  //| Arguments: Map()
                                                  //| )
                                                  //| NotExecuted(A updatemarriage11,almhirt.CollisionProblem
                                                  //| Refused to handle command: Versions do not match. Current version is '1', t
                                                  //| argetted version is '2'. The refused command is 'MoveBecauseOfMarriage(cf9c
                                                  //| b371-9a00-4060-98dd-c7ddf8c15ab3,Some(2),namemarriageName11,addressmarriage
                                                  //| Name11)'
                                                  //| Category: ApplicationProblem
                                                  //| Severity: Minor
                                                  //| Arguments: Map()
                                                  //| )
                                                  //| NotExecuted(A updatemarriage12,almhirt.CollisionProblem
                                                  //| Refused to handle command: Versions do not match. Current version is '1', t
                                                  //| argetted version is '2'. The refused command is 'MoveBecauseOfMarriage(319d
                                                  //| 8a04-cce6-4c9b-a4e8-f2360133c2a6,Some(2),namemarriageName12,addressmarriage
                                                  //| Name12)'
                                                  //| Category: ApplicationProblem
                                                  //| Severity: Minor
                                                  //| Arguments: Map()
                                                  //| )
                                                  //| NotExecuted(A updatemarriage13,almhirt.CollisionProblem
                                                  //| Refused to handle command: Versions do not match. Current version is '1', t
                                                  //| argetted version is '2'. The refused command is 'MoveBecauseOfMarriage(2a8d
                                                  //| 81b4-664a-4d7b-ad8a-c79895ea6cd3,Some(2),namemarriageName13,addressmarriage
                                                  //| Name13)'
                                                  //| Category: ApplicationProblem
                                                  //| Severity: Minor
                                                  //| Arguments: Map()
                                                  //| )
                                                  //| NotExecuted(A updatemarriage14,almhirt.CollisionProblem
                                                  //| Refused to handle command: Versions do not match. Current version is '1', t
                                                  //| argetted version is '2'. The refused command is 'MoveBecauseOfMarriage(5efd
                                                  //| a59b-08c9-4e6b-a33a-39696ffa814a,Some(2),namemarriageName14,addressmarriage
                                                  //| Name14)'
                                                  //| Category: ApplicationProblem
                                                  //| Severity: Minor
                                                  //| Arguments: Map()
                                                  //| )
                                                  //| NotExecuted(A updatemarriage15,almhirt.CollisionProblem
                                                  //| Refused to handle command: Versions do not match. Current version is '1', t
                                                  //| argetted version is '2'. The refused command is 'MoveBecauseOfMarriage(95fd
                                                  //| cced-62a2-407f-a4c1-21b6ab24ed1c,Some(2),namemarriageName15,addressmarriage
                                                  //| Name15)'
                                                  //| Category: ApplicationProblem
                                                  //| Severity: Minor
                                                  //| Arguments: Map()
                                                  //| )
                                                  //| NotExecuted(A updatemarriage16,almhirt.CollisionProblem
                                                  //| Refused to handle command: Versions do not match. Current version is '1', t
                                                  //| argetted version is '2'. The refused command is 'MoveBecauseOfMarriage(01e3
                                                  //| cf6a-2e2a-4fc4-8e5a-71b95386b4df,Some(2),namemarriageName16,addressmarriage
                                                  //| Name16)'
                                                  //| Category: ApplicationProblem
                                                  //| Severity: Minor
                                                  //| Arguments: Map()
                                                  //| )
                                                  //| NotExecuted(A updatemarriage17,almhirt.CollisionProblem
                                                  //| Refused to handle command: Versions do not match. Current version is '1', t
                                                  //| argetted version is '2'. The refused command is 'MoveBecauseOfMarriage(5f09
                                                  //| 374b-be1a-4dba-8915-c289dacdedf2,Some(2),namemarriageName17,addressmarriage
                                                  //| Name17)'
                                                  //| Category: ApplicationProblem
                                                  //| Severity: Minor
                                                  //| Arguments: Map()
                                                  //| )
                                                  //| NotExecuted(A updatemarriage18,almhirt.CollisionProblem
                                                  //| Refused to handle command: Versions do not match. Current version is '1', t
                                                  //| argetted version is '2'. The refused command is 'MoveBecauseOfMarriage(7b4d
                                                  //| 5ed7-014c-45ca-998f-2daf03634948,Some(2),namemarriageName18,addressmarriage
                                                  //| Name18)'
                                                  //| Category: ApplicationProblem
                                                  //| Severity: Minor
                                                  //| Arguments: Map()
                                                  //| )
                                                  //| NotExecuted(A updatemarriage19,almhirt.CollisionProblem
                                                  //| Refused to handle command: Versions do not match. Current version is '1', t
                                                  //| argetted version is '2'. The refused command is 'MoveBecauseOfMarriage(d6f4
                                                  //| 704b-8dfc-45f4-a12d-6e56c4795fd0,Some(2),namemarriageName19,addressmarriage
                                                  //| Name19)'
                                                  //| Category: ApplicationProblem
                                                  //| Severity: Minor
                                                  //| Arguments: Map()
                                                  //| )
                                                  //| NotExecuted(A updatemarriage20,almhirt.CollisionProblem
                                                  //| Refused to handle command: Versions do not match. Current version is '1', t
                                                  //| argetted version is '2'. The refused command is 'MoveBecauseOfMarriage(eaac
                                                  //| 82b5-1f85-423d-835b-55725bbca1b3,Some(2),namemarriageName20,addressmarriage
                                                  //| Name20)'
                                                  //| Category: ApplicationProblem
                                                  //| Severity: Minor
                                                  //| Arguments: Map()
                                                  //| )
                                                  //| NotExecuted(A updatemarriage21,almhirt.CollisionProblem
                                                  //| Refused to handle command: Versions do not match. Current version is '1', t
                                                  //| argetted version is '2'. The refused command is 'MoveBecauseOfMarriage(aad5
                                                  //| a1ad-6395-4e2a-83f8-296c30be7cf5,Some(2),namemarriageName21,addressmarriage
                                                  //| Name21)'
                                                  //| Category: ApplicationProblem
                                                  //| Severity: Minor
                                                  //| Arguments: Map()
                                                  //| )
                                                  //| NotExecuted(A updatemarriage22,almhirt.CollisionProblem
                                                  //| Refused to handle command: Versions do not match. Current version is '1', t
                                                  //| argetted version is '2'. The refused command is 'MoveBecauseOfMarriage(0069
                                                  //| afda-fc12-45ee-861e-17912b29e4f9,Some(2),namemarriageName22,addressmarriage
                                                  //| Name22)'
                                                  //| Category: ApplicationProblem
                                                  //| Severity: Minor
                                                  //| Arguments: Map()
                                                  //| )
                                                  //| NotExecuted(A updatemarriage23,almhirt.CollisionProblem
                                                  //| Refused to handle command: Versions do not match. Current version is '1', t
                                                  //| argetted version is '2'. The refused command is 'MoveBecauseOfMarriage(a23d
                                                  //| 512a-a2ab-4fb2-8dbe-0ff932c80def,Some(2),namemarriageName23,addressmarriage
                                                  //| Name23)'
                                                  //| Category: ApplicationProblem
                                                  //| Severity: Minor
                                                  //| Arguments: Map()
                                                  //| )
                                                  //| NotExecuted(A updatemarriage24,almhirt.CollisionProblem
                                                  //| Refused to handle command: Versions do not match. Current version is '1', t
                                                  //| argetted version is '2'. The refused command is 'MoveBecauseOfMarriage(cbb0
                                                  //| f738-61cc-42f7-a42d-9ca3c4ad86d7,Some(2),namemarriageName24,addressmarriage
                                                  //| Name24)'
                                                  //| Category: ApplicationProblem
                                                  //| Severity: Minor
                                                  //| Arguments: Map()
                                                  //| )
                                                  //| NotExecuted(A updatemarriage25,almhirt.CollisionProblem
                                                  //| Refused to handle command: Versions do not match. Current version is '1', t
                                                  //| argetted version is '2'. The refused command is 'MoveBecauseOfMarriage(5e1c
                                                  //| bef5-d1b8-45e4-9dd7-778465ed92f4,Some(2),namemarriageName25,addressmarriage
                                                  //| Name25)'
                                                  //| Category: ApplicationProblem
                                                  //| Severity: Minor
                                                  //| Arguments: Map()
                                                  //| )
                                                  //| NotExecuted(A updatemarriage26,almhirt.CollisionProblem
                                                  //| Refused to handle command: Versions do not match. Current version is '1', t
                                                  //| argetted version is '2'. The refused command is 'MoveBecauseOfMarriage(afa7
                                                  //| 3410-3e1d-4e5b-b97e-8e9712e179c4,Some(2),namemarriageName26,addressmarriage
                                                  //| Name26)'
                                                  //| Category: ApplicationProblem
                                                  //| Severity: Minor
                                                  //| Arguments: Map()
                                                  //| )
                                                  //| NotExecuted(A updatemarriage27,almhirt.CollisionProblem
                                                  //| Refused to handle command: Versions do not match. Current version is '1', t
                                                  //| argetted version is '2'. The refused command is 'MoveBecauseOfMarriage(cbf4
                                                  //| 3e5f-9309-46a3-9ee9-d2f4d11d8eb7,Some(2),namemarriageName27,addressmarriage
                                                  //| Name27)'
                                                  //| Category: ApplicationProblem
                                                  //| Severity: Minor
                                                  //| Arguments: Map()
                                                  //| )
                                                  //| NotExecuted(A updatemarriage28,almhirt.CollisionProblem
                                                  //| Refused to handle command: Versions do not match. Current version is '1', t
                                                  //| argetted version is '2'. The refused command is 'MoveBecauseOfMarriage(92e6
                                                  //| 031d-0a10-40ef-b426-9e6d0d2aca29,Some(2),namemarriageName28,addressmarriage
                                                  //| Name28)'
                                                  //| Category: ApplicationProblem
                                                  //| Severity: Minor
                                                  //| Arguments: Map()
                                                  //| )
                                                  //| NotExecuted(A updatemarriage29,almhirt.CollisionProblem
                                                  //| Refused to handle command: Versions do not match. Current version is '1', t
                                                  //| argetted version is '2'. The refused command is 'MoveBecauseOfMarriage(2c9d
                                                  //| e18d-6eef-422e-a591-09979bd44b96,Some(2),namemarriageName29,addressmarriage
                                                  //| Name29)'
                                                  //| Category: ApplicationProblem
                                                  //| Severity: Minor
                                                  //| Arguments: Map()
                                                  //| )
                                                  //| NotExecuted(A updatemarriage30,almhirt.CollisionProblem
                                                  //| Refused to handle command: Versions do not match. Current version is '1', t
                                                  //| argetted version is '2'. The refused command is 'MoveBecauseOfMarriage(3419
                                                  //| 62c3-c55c-4749-88ba-fc87add8d15e,Some(2),namemarriageName30,addressmarriage
                                                  //| Name30)'
                                                  //| Category: ApplicationProblem
                                                  //| Severity: Minor
                                                  //| Arguments: Map()
                                                  //| )
                                                  //| NotExecuted(A updatemarriage31,almhirt.CollisionProblem
                                                  //| Refused to handle command: Versions do not match. Current version is '1', t
                                                  //| argetted version is '2'. The refused command is 'MoveBecauseOfMarriage(ffe1
                                                  //| 3182-4373-478a-b7c2-46321bb9609f,Some(2),namemarriageName31,addressmarriage
                                                  //| Name31)'
                                                  //| Category: ApplicationProblem
                                                  //| Severity: Minor
                                                  //| Arguments: Map()
                                                  //| )
                                                  //| NotExecuted(A updatemarriage32,almhirt.CollisionProblem
                                                  //| Refused to handle command: Versions do not match. Current version is '1', t
                                                  //| argetted version is '2'. The refused command is 'MoveBecauseOfMarriage(c650
                                                  //| 3a6f-442a-40f8-9c61-ef8ffeeea867,Some(2),namemarriageName32,addressmarriage
                                                  //| Name32)'
                                                  //| Category: ApplicationProblem
                                                  //| Severity: Minor
                                                  //| Arguments: Map()
                                                  //| )
                                                  //| NotExecuted(A updatemarriage33,almhirt.CollisionProblem
                                                  //| Refused to handle command: Versions do not match. Current version is '1', t
                                                  //| argetted version is '2'. The refused command is 'MoveBecauseOfMarriage(461d
                                                  //| be67-f9d2-423b-aaef-de3c3c6fc1cc,Some(2),namemarriageName33,addressmarriage
                                                  //| Name33)'
                                                  //| Category: ApplicationProblem
                                                  //| Severity: Minor
                                                  //| Arguments: Map()
                                                  //| )
                                                  //| NotExecuted(A updatemarriage34,almhirt.CollisionProblem
                                                  //| Refused to handle command: Versions do not match. Current version is '1', t
                                                  //| argetted version is '2'. The refused command is 'MoveBecauseOfMarriage(577a
                                                  //| f3eb-2c74-460f-bd16-e7470e725ec0,Some(2),namemarriageName34,addressmarriage
                                                  //| Name34)'
                                                  //| Category: ApplicationProblem
                                                  //| Severity: Minor
                                                  //| Arguments: Map()
                                                  //| )
                                                  //| NotExecuted(A updatemarriage35,almhirt.CollisionPr
                                                  //| Output exceeds cutoff limit.
  }