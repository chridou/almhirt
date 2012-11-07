package testalmhirt

import akka.dispatch.{ Await, Future }
import almhirt._
import almhirt.almvalidation.kit._
import almhirt.environment._
import test._
import almhirt.almfuture.inst._

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
                                                  //| NotExecuted(A setaddress75,almhirt.NotFoundProblem
                                                  //| No aggregate root found with id '3a3055c3-ce9f-4d57-bb51-df2922f7fbf9'
                                                  //| Category: ApplicationProblem
                                                  //| Severity: Minor
                                                  //| Arguments: Map()
                                                  //| )
                                                  //| Executed(A setaddress76)
                                                  //| Executed(A setaddress77)
                                                  //| Executed(A setaddress78)
                                                  //| Executed(A setaddress79)
                                                  //| Executed(A setaddress80)
                                                  //| Executed(A setaddress81)
                                                  //| Executed(A setaddress82)
                                                  //| NotExecuted(A setaddress83,almhirt.NotFoundProblem
                                                  //| No aggregate root found with id '307db589-8f69-46b1-a4b1-5a006041bbff'
                                                  //| Category: ApplicationProblem
                                                  //| Severity: Minor
                                                  //| Arguments: Map()
                                                  //| )
                                                  //| Executed(A setaddress84)
                                                  //| NotExecuted(A setaddress85,almhirt.NotFoundProblem
                                                  //| No aggregate root found with id '3946ad1d-9f0c-413e-b275-b011b554dbba'
                                                  //| Category: ApplicationProblem
                                                  //| Severity: Minor
                                                  //| Arguments: Map()
                                                  //| )
                                                  //| Executed(A setaddress86)
                                                  //| NotExecuted(A setaddress87,almhirt.NotFoundProblem
                                                  //| No aggregate root found with id '2a26427f-7db4-4377-a253-c5a9732e59df'
                                                  //| Category: ApplicationProblem
                                                  //| Severity: Minor
                                                  //| Arguments: Map()
                                                  //| )
                                                  //| NotExecuted(A setaddress88,almhirt.NotFoundProblem
                                                  //| No aggregate root found with id '5fda58f2-fdd0-4b29-be13-da5fcb178726'
                                                  //| Category: ApplicationProblem
                                                  //| Severity: Minor
                                                  //| Arguments: Map()
                                                  //| )
                                                  //| NotExecuted(A setaddress89,almhirt.NotFoundProblem
                                                  //| No aggregate root found with id 'eed25b07-6a51-45c6-a840-78973725aaf8'
                                                  //| Category: ApplicationProblem
                                                  //| Severity: Minor
                                                  //| Arguments: Map()
                                                  //| )
                                                  //| NotExecuted(A setaddress90,almhirt.NotFoundProblem
                                                  //| No aggregate root found with id 'b617f406-b8e4-42b1-9f0e-dfabfe893071'
                                                  //| Category: ApplicationProblem
                                                  //| Severity: Minor
                                                  //| Arguments: Map()
                                                  //| )
                                                  //| NotExecuted(A setaddress91,almhirt.NotFoundProblem
                                                  //| No aggregate root found with id 'c4dda84e-bba5-4a6e-b73e-40c08c174639'
                                                  //| Category: ApplicationProblem
                                                  //| Severity: Minor
                                                  //| Arguments: Map()
                                                  //| )
                                                  //| NotExecuted(A setaddress92,almhirt.NotFoundProblem
                                                  //| No aggregate root found with id '1f4d629e-bde6-4447-9b6e-4f708db82653'
                                                  //| Category: ApplicationProblem
                                                  //| Severity: Minor
                                                  //| Arguments: Map()
                                                  //| )
                                                  //| NotExecuted(A setaddress93,almhirt.NotFoundProblem
                                                  //| No aggregate root found with id '3961a63f-b7d1-432d-96f9-ea501584ca4d'
                                                  //| Category: ApplicationProblem
                                                  //| Severity: Minor
                                                  //| Arguments: Map()
                                                  //| )
                                                  //| NotExecuted(A setaddress94,almhirt.NotFoundProblem
                                                  //| No aggregate root found with id '026995ed-9a62-4043-b829-9d8016a8e902'
                                                  //| Category: ApplicationProblem
                                                  //| Severity: Minor
                                                  //| Arguments: Map()
                                                  //| )
                                                  //| NotExecuted(A setaddress95,almhirt.NotFoundProblem
                                                  //| No aggregate root found with id 'b41dbb4d-4c13-4f7c-bcef-ad77002f96f3'
                                                  //| Category: ApplicationProblem
                                                  //| Severity: Minor
                                                  //| Arguments: Map()
                                                  //| )
                                                  //| NotExecuted(A setaddress96,almhirt.NotFoundProblem
                                                  //| No aggregate root found with id '174b7ba6-c93b-44e6-8211-2581e699eacb'
                                                  //| Category: ApplicationProblem
                                                  //| Severity: Minor
                                                  //| Arguments: Map()
                                                  //| )
                                                  //| NotExecuted(A setaddress97,almhirt.NotFoundProblem
                                                  //| No aggregate root found with id 'c479682c-92e2-444d-9087-3aac2ba4c260'
                                                  //| Category: ApplicationProblem
                                                  //| Severity: Minor
                                                  //| Arguments: Map()
                                                  //| )
                                                  //| NotExecuted(A setaddress98,almhirt.NotFoundProblem
                                                  //| No aggregate root found with id '20cd2685-21ac-4319-a74c-3b0f1e3f39fb'
                                                  //| Category: ApplicationProblem
                                                  //| Severity: Minor
                                                  //| Arguments: Map()
                                                  //| )
                                                  //| NotExecuted(A setaddress99,almhirt.NotFoundProblem
                                                  //| No aggregate root found with id 'f0e42b49-bdf6-4037-9659-90c9725dd438'
                                                  //| Category: ApplicationProblem
                                                  //| Severity: Minor
                                                  //| Arguments: Map()
                                                  //| )
                                                  //| --- updatemarriage done ---
                                                  //| NotExecuted(A updatemarriage0,almhirt.CollisionProblem
                                                  //| Refused to handle command: Versions do not match. Current version is '1', t
                                                  //| argetted version is '2'. The refused command is 'MoveBecauseOfMarriage(0691
                                                  //| 27cc-96ef-488b-83c3-9ab57fdad813,Some(2),namemarriageName0,addressmarriageN
                                                  //| ame0)'
                                                  //| Category: ApplicationProblem
                                                  //| Severity: Minor
                                                  //| Arguments: Map()
                                                  //| )
                                                  //| NotExecuted(A updatemarriage1,almhirt.CollisionProblem
                                                  //| Refused to handle command: Versions do not match. Current version is '1', t
                                                  //| argetted version is '2'. The refused command is 'MoveBecauseOfMarriage(82af
                                                  //| 6856-0f03-4767-954c-b58d930c26a0,Some(2),namemarriageName1,addressmarriageN
                                                  //| ame1)'
                                                  //| Category: ApplicationProblem
                                                  //| Severity: Minor
                                                  //| Arguments: Map()
                                                  //| )
                                                  //| NotExecuted(A updatemarriage2,almhirt.CollisionProblem
                                                  //| Refused to handle command: Versions do not match. Current version is '1', t
                                                  //| argetted version is '2'. The refused command is 'MoveBecauseOfMarriage(86c3
                                                  //| 70a3-d636-46c7-b8fe-2219dbe4a216,Some(2),namemarriageName2,addressmarriageN
                                                  //| ame2)'
                                                  //| Category: ApplicationProblem
                                                  //| Severity: Minor
                                                  //| Arguments: Map()
                                                  //| )
                                                  //| NotExecuted(A updatemarriage3,almhirt.CollisionProblem
                                                  //| Refused to handle command: Versions do not match. Current version is '1', t
                                                  //| argetted version is '2'. The refused command is 'MoveBecauseOfMarriage(3f76
                                                  //| 4079-e7b4-4d88-8482-4cf3547bb009,Some(2),namemarriageName3,addressmarriageN
                                                  //| ame3)'
                                                  //| Category: ApplicationProblem
                                                  //| Severity: Minor
                                                  //| Arguments: Map()
                                                  //| )
                                                  //| NotExecuted(A updatemarriage4,almhirt.CollisionProblem
                                                  //| Refused to handle command: Versions do not match. Current version is '1', t
                                                  //| argetted version is '2'. The refused command is 'MoveBecauseOfMarriage(7d44
                                                  //| f1b3-2f5e-4b00-af56-fc6608c6f693,Some(2),namemarriageName4,addressmarriageN
                                                  //| ame4)'
                                                  //| Category: ApplicationProblem
                                                  //| Severity: Minor
                                                  //| Arguments: Map()
                                                  //| )
                                                  //| NotExecuted(A updatemarriage5,almhirt.CollisionProblem
                                                  //| Refused to handle command: Versions do not match. Current version is '1', t
                                                  //| argetted version is '2'. The refused command is 'MoveBecauseOfMarriage(c0ec
                                                  //| ca52-4dad-4950-8b40-42b09a82d7d1,Some(2),namemarriageName5,addressmarriageN
                                                  //| ame5)'
                                                  //| Category: ApplicationProblem
                                                  //| Severity: Minor
                                                  //| Arguments: Map()
                                                  //| )
                                                  //| NotExecuted(A updatemarriage6,almhirt.CollisionProblem
                                                  //| Refused to handle command: Versions do not match. Current version is '1', t
                                                  //| argetted version is '2'. The refused command is 'MoveBecauseOfMarriage(b649
                                                  //| a814-f0e9-4442-bfba-e564b7b34dcb,Some(2),namemarriageName6,addressmarriageN
                                                  //| ame6)'
                                                  //| Category: ApplicationProblem
                                                  //| Severity: Minor
                                                  //| Arguments: Map()
                                                  //| )
                                                  //| NotExecuted(A updatemarriage7,almhirt.CollisionProblem
                                                  //| Refused to handle command: Versions do not match. Current version is '1', t
                                                  //| argetted version is '2'. The refused command is 'MoveBecauseOfMarriage(1dab
                                                  //| cb9b-6e4e-44be-802c-d4c9e8943e99,Some(2),namemarriageName7,addressmarriageN
                                                  //| ame7)'
                                                  //| Category: ApplicationProblem
                                                  //| Severity: Minor
                                                  //| Arguments: Map()
                                                  //| )
                                                  //| NotExecuted(A updatemarriage8,almhirt.CollisionProblem
                                                  //| Refused to handle command: Versions do not match. Current version is '1', t
                                                  //| argetted version is '2'. The refused command is 'MoveBecauseOfMarriage(3473
                                                  //| a5a8-a810-4ea7-9e1c-34824b6dc78e,Some(2),namemarriageName8,addressmarriageN
                                                  //| ame8)'
                                                  //| Category: ApplicationProblem
                                                  //| Severity: Minor
                                                  //| Arguments: Map()
                                                  //| )
                                                  //| NotExecuted(A updatemarriage9,almhirt.CollisionProblem
                                                  //| Refused to handle command: Versions do not match. Current version is '1', t
                                                  //| argetted version is '2'. The refused command is 'MoveBecauseOfMarriage(fe95
                                                  //| dfd4-0242-41a7-9956-5cea8e1e16ac,Some(2),namemarriageName9,addressmarriageN
                                                  //| ame9)'
                                                  //| Category: ApplicationProblem
                                                  //| Severity: Minor
                                                  //| Arguments: Map()
                                                  //| )
                                                  //| NotExecuted(A updatemarriage10,almhirt.CollisionProblem
                                                  //| Refused to handle command: Versions do not match. Current version is '1', t
                                                  //| argetted version is '2'. The refused command is 'MoveBecauseOfMarriage(f86d
                                                  //| f05d-9b6e-4eaa-a42d-3716525cf980,Some(2),namemarriageName10,addressmarriage
                                                  //| Name10)'
                                                  //| Category: ApplicationProblem
                                                  //| Severity: Minor
                                                  //| Arguments: Map()
                                                  //| )
                                                  //| NotExecuted(A updatemarriage11,almhirt.CollisionProblem
                                                  //| Refused to handle command: Versions do not match. Current version is '1', t
                                                  //| argetted version is '2'. The refused command is 'MoveBecauseOfMarriage(fed4
                                                  //| 12d8-3176-42f1-be7b-c9a5e2c91e3b,Some(2),namemarriageName11,addressmarriage
                                                  //| Name11)'
                                                  //| Category: ApplicationProblem
                                                  //| Severity: Minor
                                                  //| Arguments: Map()
                                                  //| )
                                                  //| NotExecuted(A updatemarriage12,almhirt.CollisionProblem
                                                  //| Refused to handle command: Versions do not match. Current version is '1', t
                                                  //| argetted version is '2'. The refused command is 'MoveBecauseOfMarriage(fe09
                                                  //| adb7-87ee-4a4d-8bf1-ae217f9b535a,Some(2),namemarriageName12,addressmarriage
                                                  //| Name12)'
                                                  //| Category: ApplicationProblem
                                                  //| Severity: Minor
                                                  //| Arguments: Map()
                                                  //| )
                                                  //| NotExecuted(A updatemarriage13,almhirt.CollisionProblem
                                                  //| Refused to handle command: Versions do not match. Current version is '1', t
                                                  //| argetted version is '2'. The refused command is 'MoveBecauseOfMarriage(2a08
                                                  //| f172-643a-411b-a9a2-7f78a119f9c9,Some(2),namemarriageName13,addressmarriage
                                                  //| Name13)'
                                                  //| Category: ApplicationProblem
                                                  //| Severity: Minor
                                                  //| Arguments: Map()
                                                  //| )
                                                  //| NotExecuted(A updatemarriage14,almhirt.CollisionProblem
                                                  //| Refused to handle command: Versions do not match. Current version is '1', t
                                                  //| argetted version is '2'. The refused command is 'MoveBecauseOfMarriage(0125
                                                  //| 8062-7caf-42c7-a4c4-2280908ae598,Some(2),namemarriageName14,addressmarriage
                                                  //| Name14)'
                                                  //| Category: ApplicationProblem
                                                  //| Severity: Minor
                                                  //| Arguments: Map()
                                                  //| )
                                                  //| NotExecuted(A updatemarriage15,almhirt.CollisionProblem
                                                  //| Refused to handle command: Versions do not match. Current version is '1', t
                                                  //| argetted version is '2'. The refused command is 'MoveBecauseOfMarriage(6f38
                                                  //| e078-a8c4-4744-9a49-499bfe605dce,Some(2),namemarriageName15,addressmarriage
                                                  //| Name15)'
                                                  //| Category: ApplicationProblem
                                                  //| Severity: Minor
                                                  //| Arguments: Map()
                                                  //| )
                                                  //| NotExecuted(A updatemarriage16,almhirt.CollisionProblem
                                                  //| Refused to handle command: Versions do not match. Current version is '1', t
                                                  //| argetted version is '2'. The refused command is 'MoveBecauseOfMarriage(b58b
                                                  //| 7070-aedf-4669-b1a8-c3173c635684,Some(2),namemarriageName16,addressmarriage
                                                  //| Name16)'
                                                  //| Category: ApplicationProblem
                                                  //| Severity: Minor
                                                  //| Arguments: Map()
                                                  //| )
                                                  //| NotExecuted(A updatemarriage17,almhirt.CollisionProblem
                                                  //| Refused to handle command: Versions do not match. Current version is '1', t
                                                  //| argetted version is '2'. The refused command is 'MoveBecauseOfMarriage(868a
                                                  //| 025e-f2c3-4c14-a8e8-9d7922db50e4,Some(2),namemarriageName17,addressmarriage
                                                  //| Name17)'
                                                  //| Category: ApplicationProblem
                                                  //| Severity: Minor
                                                  //| Arguments: Map()
                                                  //| )
                                                  //| NotExecuted(A updatemarriage18,almhirt.CollisionProblem
                                                  //| Refused to handle command: Versions do not match. Current version is '1', t
                                                  //| argetted version is '2'. The refused command is 'MoveBecauseOfMarriage(6386
                                                  //| 1792-3674-4c2e-bd87-5e2db16a3d28,Some(2),namemarriageName18,addressmarriage
                                                  //| Name18)'
                                                  //| Category: ApplicationProblem
                                                  //| Severity: Minor
                                                  //| Arguments: Map()
                                                  //| )
                                                  //| NotExecuted(A updatemarriage19,almhirt.CollisionProblem
                                                  //| Refused to handle command: Versions do not match. Current version is '1', t
                                                  //| argetted version is '2'. The refused command is 'MoveBecauseOfMarriage(e5ca
                                                  //| b394-391c-4d8b-8b6f-21065a876c9f,Some(2),namemarriageName19,addressmarriage
                                                  //| Name19)'
                                                  //| Category: ApplicationProblem
                                                  //| Severity: Minor
                                                  //| Arguments: Map()
                                                  //| )
                                                  //| NotExecuted(A updatemarriage20,almhirt.CollisionProblem
                                                  //| Refused to handle command: Versions do not match. Current version is '1', t
                                                  //| argetted version is '2'. The refused command is 'MoveBecauseOfMarriage(f9f2
                                                  //| 0d96-3ac4-448d-b52c-37df93b9ec5a,Some(2),namemarriageName20,addressmarriage
                                                  //| Name20)'
                                                  //| Category: ApplicationProblem
                                                  //| Severity: Minor
                                                  //| Arguments: Map()
                                                  //| )
                                                  //| NotExecuted(A updatemarriage21,almhirt.CollisionProblem
                                                  //| Refused to handle command: Versions do not match. Current version is '1', t
                                                  //| argetted version is '2'. The refused command is 'MoveBecauseOfMarriage(7b79
                                                  //| 9bbe-1ef8-42fe-98be-0dfd1bcf9ec7,Some(2),namemarriageName21,addressmarriage
                                                  //| Name21)'
                                                  //| Category: ApplicationProblem
                                                  //| Severity: Minor
                                                  //| Arguments: Map()
                                                  //| )
                                                  //| NotExecuted(A updatemarriage22,almhirt.CollisionProblem
                                                  //| Refused to handle command: Versions do not match. Current version is '1', t
                                                  //| argetted version is '2'. The refused command is 'MoveBecauseOfMarriage(2219
                                                  //| fd2d-a073-4239-882b-89759834f974,Some(2),namemarriageName22,addressmarriage
                                                  //| Name22)'
                                                  //| Category: ApplicationProblem
                                                  //| Severity: Minor
                                                  //| Arguments: Map()
                                                  //| )
                                                  //| NotExecuted(A updatemarriage23,almhirt.CollisionProblem
                                                  //| Refused to handle command: Versions do not match. Current version is '1', t
                                                  //| argetted version is '2'. The refused command is 'MoveBecauseOfMarriage(0dbf
                                                  //| f994-4294-4a42-b958-d6c65bf68766,Some(2),namemarriageName23,addressmarriage
                                                  //| Name23)'
                                                  //| Category: ApplicationProblem
                                                  //| Severity: Minor
                                                  //| Arguments: Map()
                                                  //| )
                                                  //| NotExecuted(A updatemarriage24,almhirt.CollisionProblem
                                                  //| Refused to handle command: Versions do not match. Current version is '1', t
                                                  //| argetted version is '2'. The refused command is 'MoveBecauseOfMarriage(d16f
                                                  //| 5e25-69b0-4467-9303-8c32aa89c458,Some(2),namemarriageName24,addressmarriage
                                                  //| Name24)'
                                                  //| Category: ApplicationProblem
                                                  //| Severity: Minor
                                                  //| Arguments: Map()
                                                  //| )
                                                  //| NotExecuted(A updatemarriage25,almhirt.CollisionProblem
                                                  //| Refused to handle command: Versions do not match. Current version is '1', t
                                                  //| argetted version is '2'. The refused command is 'MoveBecauseOfMarriage(baee
                                                  //| d82f-d1f4-49fa-87a7-b6a0cadddc43,Some(2),namemarriageName25,addressmarriage
                                                  //| Name25)'
                                                  //| Category: ApplicationProblem
                                                  //| Severity: Minor
                                                  //| Arguments: Map()
                                                  //| )
                                                  //| NotExecuted(A updatemarriage26,almhirt.CollisionProblem
                                                  //| Refused to handle command: Versions do not match. Current version is '1', t
                                                  //| argetted version is '2'. The refused command is 'MoveBecauseOfMarriage(e555
                                                  //| 1a5e-773f-4df7-996d-bb29d7914bf5,Some(2),namemarriageName26,addressmarriage
                                                  //| Name26)'
                                                  //| Category: ApplicationProblem
                                                  //| Severity: Minor
                                                  //| Arguments: Map()
                                                  //| )
                                                  //| NotExecuted(A updatemarriage27,almhirt.CollisionProblem
                                                  //| Refused to handle command: Versions do not match. Current version is '1', t
                                                  //| argetted version is '2'. The refused command is 'MoveBecauseOfMarriage(6426
                                                  //| ef3a-130f-4759-a262-6069a072fee3,Some(2),namemarriageName27,addressmarriage
                                                  //| Output exceeds cutoff limit.
  }