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
                                                  //| NotExecuted(A setaddress0,almhirt.NotFoundProblem
                                                  //| No aggregate root found with id 'afd5cf95-3ccf-47c0-b274-61546e6e4ee9'
                                                  //| Category: ApplicationProblem
                                                  //| Severity: Minor
                                                  //| Arguments: Map()
                                                  //| )
                                                  //| NotExecuted(A setaddress1,almhirt.NotFoundProblem
                                                  //| No aggregate root found with id '8bfcf820-bc7d-4e81-8080-32d4b5eeedbc'
                                                  //| Category: ApplicationProblem
                                                  //| Severity: Minor
                                                  //| Arguments: Map()
                                                  //| )
                                                  //| NotExecuted(A setaddress2,almhirt.NotFoundProblem
                                                  //| No aggregate root found with id '04d273c3-393b-4d51-bd65-1d4095ff72d9'
                                                  //| Category: ApplicationProblem
                                                  //| Severity: Minor
                                                  //| Arguments: Map()
                                                  //| )
                                                  //| NotExecuted(A setaddress3,almhirt.NotFoundProblem
                                                  //| No aggregate root found with id '1a9dda24-2a20-41c9-8d96-740028d3f1a0'
                                                  //| Category: ApplicationProblem
                                                  //| Severity: Minor
                                                  //| Arguments: Map()
                                                  //| )
                                                  //| NotExecuted(A setaddress4,almhirt.NotFoundProblem
                                                  //| No aggregate root found with id 'f437639f-04a0-48f4-ab80-415e01eade32'
                                                  //| Category: ApplicationProblem
                                                  //| Severity: Minor
                                                  //| Arguments: Map()
                                                  //| )
                                                  //| NotExecuted(A setaddress5,almhirt.NotFoundProblem
                                                  //| No aggregate root found with id 'cf67d662-51fa-4e06-9624-725946ae59a9'
                                                  //| Category: ApplicationProblem
                                                  //| Severity: Minor
                                                  //| Arguments: Map()
                                                  //| )
                                                  //| NotExecuted(A setaddress6,almhirt.NotFoundProblem
                                                  //| No aggregate root found with id '8b69a528-958f-49db-841c-256b2ebce91c'
                                                  //| Category: ApplicationProblem
                                                  //| Severity: Minor
                                                  //| Arguments: Map()
                                                  //| )
                                                  //| NotExecuted(A setaddress7,almhirt.NotFoundProblem
                                                  //| No aggregate root found with id 'f28ce60c-b67f-4543-bbbc-c988db9fcbb6'
                                                  //| Category: ApplicationProblem
                                                  //| Severity: Minor
                                                  //| Arguments: Map()
                                                  //| )
                                                  //| NotExecuted(A setaddress8,almhirt.NotFoundProblem
                                                  //| No aggregate root found with id '763f712a-fe13-4a54-9082-6215ade78647'
                                                  //| Category: ApplicationProblem
                                                  //| Severity: Minor
                                                  //| Arguments: Map()
                                                  //| )
                                                  //| NotExecuted(A setaddress9,almhirt.NotFoundProblem
                                                  //| No aggregate root found with id '293e4bda-a4c8-4781-afc5-c8727c754e29'
                                                  //| Category: ApplicationProblem
                                                  //| Severity: Minor
                                                  //| Arguments: Map()
                                                  //| )
                                                  //| NotExecuted(A setaddress10,almhirt.NotFoundProblem
                                                  //| No aggregate root found with id 'a566ac87-a589-4ed0-a763-9a967821a691'
                                                  //| Category: ApplicationProblem
                                                  //| Severity: Minor
                                                  //| Arguments: Map()
                                                  //| )
                                                  //| NotExecuted(A setaddress11,almhirt.NotFoundProblem
                                                  //| No aggregate root found with id '18b386bd-8c35-446c-837f-18fbc84ce34c'
                                                  //| Category: ApplicationProblem
                                                  //| Severity: Minor
                                                  //| Arguments: Map()
                                                  //| )
                                                  //| NotExecuted(A setaddress12,almhirt.NotFoundProblem
                                                  //| No aggregate root found with id '353f4e51-bb8a-44d6-b935-bcca3f0c13be'
                                                  //| Category: ApplicationProblem
                                                  //| Severity: Minor
                                                  //| Arguments: Map()
                                                  //| )
                                                  //| NotExecuted(A setaddress13,almhirt.NotFoundProblem
                                                  //| No aggregate root found with id 'c274ab59-aff5-42d9-b152-3ef22c5df4f7'
                                                  //| Category: ApplicationProblem
                                                  //| Severity: Minor
                                                  //| Arguments: Map()
                                                  //| )
                                                  //| NotExecuted(A setaddress14,almhirt.NotFoundProblem
                                                  //| No aggregate root found with id '50f57989-d15a-4124-9e52-5ef1a3b4789a'
                                                  //| Category: ApplicationProblem
                                                  //| Severity: Minor
                                                  //| Arguments: Map()
                                                  //| )
                                                  //| NotExecuted(A setaddress15,almhirt.NotFoundProblem
                                                  //| No aggregate root found with id '67d34a34-18f4-46d8-a711-cead57880f6c'
                                                  //| Category: ApplicationProblem
                                                  //| Severity: Minor
                                                  //| Arguments: Map()
                                                  //| )
                                                  //| NotExecuted(A setaddress16,almhirt.NotFoundProblem
                                                  //| No aggregate root found with id '094fcd7e-7853-4b1f-a133-63ea0bfdea46'
                                                  //| Category: ApplicationProblem
                                                  //| Severity: Minor
                                                  //| Arguments: Map()
                                                  //| )
                                                  //| NotExecuted(A setaddress17,almhirt.NotFoundProblem
                                                  //| No aggregate root found with id '89b7a003-2ee4-4a88-bf68-7a57c46ea4dd'
                                                  //| Category: ApplicationProblem
                                                  //| Severity: Minor
                                                  //| Arguments: Map()
                                                  //| )
                                                  //| NotExecuted(A setaddress18,almhirt.NotFoundProblem
                                                  //| No aggregate root found with id '7f3ecd53-631f-4300-a52e-196998ea06e9'
                                                  //| Category: ApplicationProblem
                                                  //| Severity: Minor
                                                  //| Arguments: Map()
                                                  //| )
                                                  //| NotExecuted(A setaddress19,almhirt.NotFoundProblem
                                                  //| No aggregate root found with id '2d2c1da6-2ad2-4036-aca9-2fed7a098938'
                                                  //| Category: ApplicationProblem
                                                  //| Severity: Minor
                                                  //| Arguments: Map()
                                                  //| )
                                                  //| NotExecuted(A setaddress20,almhirt.NotFoundProblem
                                                  //| No aggregate root found with id '5b73b1cf-6f3c-4d1e-b032-14b9168f57ca'
                                                  //| Category: ApplicationProblem
                                                  //| Severity: Minor
                                                  //| Arguments: Map()
                                                  //| )
                                                  //| NotExecuted(A setaddress21,almhirt.NotFoundProblem
                                                  //| No aggregate root found with id 'ae681b39-88ad-49e0-b51d-fb38015c58e0'
                                                  //| Category: ApplicationProblem
                                                  //| Severity: Minor
                                                  //| Arguments: Map()
                                                  //| )
                                                  //| NotExecuted(A setaddress22,almhirt.NotFoundProblem
                                                  //| No aggregate root found with id '4740b507-2f95-4f77-bdb2-5cd504e64e87'
                                                  //| Category: ApplicationProblem
                                                  //| Severity: Minor
                                                  //| Arguments: Map()
                                                  //| )
                                                  //| NotExecuted(A setaddress23,almhirt.NotFoundProblem
                                                  //| No aggregate root found with id '12578e13-480c-42ff-988e-356566af5a9d'
                                                  //| Category: ApplicationProblem
                                                  //| Severity: Minor
                                                  //| Arguments: Map()
                                                  //| )
                                                  //| NotExecuted(A setaddress24,almhirt.NotFoundProblem
                                                  //| No aggregate root found with id '51a1690d-5bc4-4809-8710-c453e71af656'
                                                  //| Category: ApplicationProblem
                                                  //| Severity: Minor
                                                  //| Arguments: Map()
                                                  //| )
                                                  //| NotExecuted(A setaddress25,almhirt.NotFoundProblem
                                                  //| No aggregate root found with id 'a44f13a8-9d47-41e4-878d-c7245a2b1c67'
                                                  //| Category: ApplicationProblem
                                                  //| Severity: Minor
                                                  //| Arguments: Map()
                                                  //| )
                                                  //| NotExecuted(A setaddress26,almhirt.NotFoundProblem
                                                  //| No aggregate root found with id '19a32cd9-4d76-4d32-927b-18b6f15a7540'
                                                  //| Category: ApplicationProblem
                                                  //| Severity: Minor
                                                  //| Arguments: Map()
                                                  //| )
                                                  //| NotExecuted(A setaddress27,almhirt.NotFoundProblem
                                                  //| No aggregate root found with id 'e97cb33c-e09e-487b-b5bf-cedb4a6336a2'
                                                  //| Category: ApplicationProblem
                                                  //| Severity: Minor
                                                  //| Arguments: Map()
                                                  //| )
                                                  //| NotExecuted(A setaddress28,almhirt.NotFoundProblem
                                                  //| No aggregate root found with id '4203055e-e87a-43bc-885f-c0bab860232c'
                                                  //| Category: ApplicationProblem
                                                  //| Severity: Minor
                                                  //| Arguments: Map()
                                                  //| )
                                                  //| NotExecuted(A setaddress29,almhirt.NotFoundProblem
                                                  //| No aggregate root found with id '2e2a0136-1373-4795-991a-4b8e7a8a711a'
                                                  //| Category: ApplicationProblem
                                                  //| Severity: Minor
                                                  //| Arguments: Map()
                                                  //| )
                                                  //| NotExecuted(A setaddress30,almhirt.NotFoundProblem
                                                  //| No aggregate root found with id 'f3bd0c52-83bc-4006-b681-4aefd42829a5'
                                                  //| Category: ApplicationProblem
                                                  //| Severity: Minor
                                                  //| Arguments: Map()
                                                  //| )
                                                  //| NotExecuted(A setaddress31,almhirt.NotFoundProblem
                                                  //| No aggregate root found with id '79896e48-76bd-4055-8f57-d6b317df45e2'
                                                  //| Category: ApplicationProblem
                                                  //| Severity: Minor
                                                  //| Arguments: Map()
                                                  //| )
                                                  //| NotExecuted(A setaddress32,almhirt.NotFoundProblem
                                                  //| No aggregate root found with id 'f679cac1-d4e2-4eda-86ad-e1742308c351'
                                                  //| Category: ApplicationProblem
                                                  //| Severity: Minor
                                                  //| Arguments: Map()
                                                  //| )
                                                  //| NotExecuted(A setaddress33,almhirt.NotFoundProblem
                                                  //| No aggregate root found with id 'e4f49892-4c66-46eb-baab-32e250e945c0'
                                                  //| Category: ApplicationProblem
                                                  //| Severity: Minor
                                                  //| Arguments: Map()
                                                  //| )
                                                  //| NotExecuted(A setaddress34,almhirt.NotFoundProblem
                                                  //| No aggregate root found with id 'd7dc13bd-3df1-48b3-9c11-d5413cd2ec68'
                                                  //| Category: ApplicationProblem
                                                  //| Severity: Minor
                                                  //| Arguments: Map()
                                                  //| )
                                                  //| NotExecuted(A setaddress35,almhirt.NotFoundProblem
                                                  //| No aggregate root found with id '68a30024-c867-417f-9577-bfc3fae88695'
                                                  //| Category: ApplicationProblem
                                                  //| Severity: Minor
                                                  //| Arguments: Map()
                                                  //| )
                                                  //| NotExecuted(A setaddress36,almhirt.NotFoundProblem
                                                  //| No aggregate root found with id '11a42141-413f-4156-95b0-f26a26f6a2b5'
                                                  //| Category: ApplicationProblem
                                                  //| Severity: Minor
                                                  //| Arguments: Map()
                                                  //| )
                                                  //| NotExecuted(A setaddress37,almhirt.NotFoundProblem
                                                  //| No aggregate root found with id '84043ad5-a625-4893-92e6-d76957a006ad'
                                                  //| Category: ApplicationProblem
                                                  //| Severity: Minor
                                                  //| Arguments: Map()
                                                  //| )
                                                  //| NotExecuted(A setaddress38,almhirt.NotFoundProblem
                                                  //| No aggregate root found with id '99279c33-2aab-4197-b790-ebdb7d2852bf'
                                                  //| Category: ApplicationProblem
                                                  //| Severity: Minor
                                                  //| Arguments: Map()
                                                  //| )
                                                  //| NotExecuted(A setaddress39,almhirt.NotFoundProblem
                                                  //| No aggregate root found with id '4b7a6ae5-4715-4a06-b7e7-8c60a1ebbbaa'
                                                  //| Category: ApplicationProblem
                                                  //| Severity: Minor
                                                  //| Arguments: Map()
                                                  //| )
                                                  //| NotExecuted(A setaddress40,almhirt.NotFoundProblem
                                                  //| No aggregate root found with id 'dcc9539e-9cf4-4c05-a3b7-fca56da79e86'
                                                  //| Category: ApplicationProblem
                                                  //| Severity: Minor
                                                  //| Arguments: Map()
                                                  //| )
                                                  //| NotExecuted(A setaddress41,almhirt.NotFoundProblem
                                                  //| No aggregate root found with id '99d70e54-b886-4b28-9592-b6d248d959ec'
                                                  //| Category: ApplicationProblem
                                                  //| Severity: Minor
                                                  //| Arguments: Map()
                                                  //| )
                                                  //| NotExecuted(A setaddress42,almhirt.NotFoundProblem
                                                  //| No aggregate root found with id '22e54ead-44e0-4d35-9649-78142a3fcada'
                                                  //| Category: ApplicationProblem
                                                  //| Severity: Minor
                                                  //| Arguments: Map()
                                                  //| )
                                                  //| NotExecuted(A setaddress43,almhirt.NotFoundProblem
                                                  //| No aggregate root found with id '1075b35d-203b-4405-a248-405af9e3fefb'
                                                  //| Category: ApplicationProblem
                                                  //| Severity: Minor
                                                  //| Arguments: Map()
                                                  //| )
                                                  //| NotExecuted(A setaddress44,almhirt.NotFoundProblem
                                                  //| No aggregate root found with id '3c588db6-515d-4cbd-b54b-1ecc4d35b00f'
                                                  //| Category: ApplicationProblem
                                                  //| Severity: Minor
                                                  //| Arguments: Map()
                                                  //| )
                                                  //| NotExecuted(A setaddress45,almhirt.NotFoundProblem
                                                  //| No aggregate root found with id '6fb51ccb-795e-44de-a022-8d2c9d246f45'
                                                  //| Category: ApplicationProblem
                                                  //| Severity: Minor
                                                  //| Arguments: Map()
                                                  //| )
                                                  //| NotExecuted(A setaddress46,almhirt.NotFoundProblem
                                                  //| No aggregate root found with id '22e6dd43-e527-4004-ac73-0f38efe762a9'
                                                  //| Category: ApplicationProblem
                                                  //| Severity: Minor
                                                  //| Arguments: Map()
                                                  //| )
                                                  //| NotExecuted(A setaddress47,almhirt.NotFoundProblem
                                                  //| No aggregate root found with id '17f32f51-005e-4618-8de0-1fa842f6e088'
                                                  //| Category: ApplicationProblem
                                                  //| Severity: Minor
                                                  //| Arguments: Map()
                                                  //| )
                                                  //| NotExecuted(A setaddress48,almhirt.NotFoundProblem
                                                  //| No aggregate root found with id 'c41166e8-86b6-4173-bab5-734788371358'
                                                  //| Category: ApplicationProblem
                                                  //| Severity: Minor
                                                  //| Arguments: Map()
                                                  //| )
                                                  //| NotExecuted(A setaddress49,almhirt.NotFoundProblem
                                                  //| No aggregate root found with id 'e2b77265-ffed-4c6d-9cb9-6c047f1f98a3'
                                                  //| Category: ApplicationProblem
                                                  //| Severity: Minor
                                                  //| Arguments: Map()
                                                  //| )
                                                  //| NotExecuted(A setaddress50,almhirt.NotFoundProblem
                                                  //| No aggregate root found with id '5e3226de-4768-4633-b939-a53db32aeb02'
                                                  //| Category: ApplicationProblem
                                                  //| Severity: Minor
                                                  //| Arguments: Map()
                                                  //| )
                                                  //| NotExecuted(A setaddress51,almhirt.NotFoundProblem
                                                  //| No aggregate root found with id '6900f796-71d4-4f08-b03d-07c3a4c945b2'
                                                  //| Category: ApplicationProblem
                                                  //| Severity: Minor
                                                  //| Arguments: Map()
                                                  //| )
                                                  //| NotExecuted(A setaddress52,almhirt.NotFoundProblem
                                                  //| No aggregate root found with id 'ee5bf455-4e5c-41ee-bdd3-6935085c33fd'
                                                  //| Category: ApplicationProblem
                                                  //| Severity: Minor
                                                  //| Arguments: Map()
                                                  //| )
                                                  //| NotExecuted(A setaddress53,almhirt.NotFoundProblem
                                                  //| No aggregate root found with id '1f38cc5c-0a01-4466-b28a-eb97c6775ca3'
                                                  //| Category: ApplicationProblem
                                                  //| Severity: Minor
                                                  //| Arguments: Map()
                                                  //| )
                                                  //| NotExecuted(A setaddress54,almhirt.NotFoundProblem
                                                  //| No aggregate root found with id '02ecf65d-d24c-48ec-a50f-486d1c2c4f95'
                                                  //| Category: ApplicationProblem
                                                  //| Severity: Minor
                                                  //| Arguments: Map()
                                                  //| )
                                                  //| NotExecuted(A setaddress55,almhirt.NotFoundProblem
                                                  //| No aggregate root found with id 'c359cb21-8fc7-4220-bb38-7706ae8fb6f4'
                                                  //| Category: ApplicationProblem
                                                  //| Severity: Minor
                                                  //| Arguments: Map()
                                                  //| )
                                                  //| NotExecuted(A setaddress56,almhirt.NotFoundProblem
                                                  //| No aggregate root found with id '74d76af9-3e3b-431b-808a-495a8b262ba4'
                                                  //| Category: ApplicationProblem
                                                  //| Severity: Minor
                                                  //| Arguments: Map()
                                                  //| )
                                                  //| NotExecuted(A setaddress57,almhirt.NotFoundProblem
                                                  //| No aggregate root found with id 'f511ed89-1408-400f-b8ea-b81780e3f9ac'
                                                  //| Category: ApplicationProblem
                                                  //| Severity: Minor
                                                  //| Arguments: Map()
                                                  //| )
                                                  //| NotExecuted(A setaddress58,almhirt.NotFoundProblem
                                                  //| No aggregate root found with id '51f050d5-86f9-4f5b-94c1-906d83bf34d1'
                                                  //| Category: ApplicationProblem
                                                  //| Severity: Minor
                                                  //| Arguments: Map()
                                                  //| )
                                                  //| NotExecuted(A setaddress59,almhirt.NotFoundProblem
                                                  //| No aggregate root found with id 'e024ab35-841a-441c-9cb5-c6055741c1f5'
                                                  //| Category: ApplicationProblem
                                                  //| Severity: Minor
                                                  //| Arguments: Map()
                                                  //| )
                                                  //| NotExecuted(A setaddress60,almhirt.NotFoundProblem
                                                  //| No aggregate root found with id '92aa462b-d853-4b16-b856-fb49f8ae9b5d'
                                                  //| Category: ApplicationProblem
                                                  //| Severity: Minor
                                                  //| Arguments: Map()
                                                  //| )
                                                  //| Executed(A setaddress61)
                                                  //| NotExecuted(A setaddress62,almhirt.NotFoundProblem
                                                  //| No aggregate root found with id '1387fa07-b61a-4421-935f-85aa8558e257'
                                                  //| Category: ApplicationProblem
                                                  //| Severity: Minor
                                                  //| Arguments: Map()
                                                  //| )
                                                  //| NotExecuted(A setaddress63,almhirt.NotFoundProblem
                                                  //| No aggregate root found with id '9e0011e2-c7ea-4a0d-bfcc-84d235af7e73'
                                                  //| Category: ApplicationProblem
                                                  //| Severity: Minor
                                                  //| Arguments: Map()
                                                  //| )
                                                  //| NotExecuted(A setaddress64,almhirt.NotFoundProblem
                                                  //| No aggregate root found with id '143f1e9f-402c-4fb7-96af-0d9bab7fd32b'
                                                  //| Category: ApplicationProblem
                                                  //| Severity: Minor
                                                  //| Arguments: Map()
                                                  //| )
                                                  //| NotExecuted(A setaddress65,almhirt.NotFoundProblem
                                                  //| No aggregate root found with id '622e38be-e974-48f2-8c64-094263200c82'
                                                  //| Category: ApplicationProblem
                                                  //| Severity: Minor
                                                  //| Arguments: Map()
                                                  //| )
                                                  //| NotExecuted(A setaddress66,almhirt.NotFoundProblem
                                                  //| No aggregate root found with id 'f0a154b7-bf55-4c51-a285-59c6f6269d23'
                                                  //| Category: ApplicationProblem
                                                  //| Severity: Minor
                                                  //| Arguments: Map()
                                                  //| )
                                                  //| NotExecuted(A setaddress67,almhirt.NotFoundProblem
                                                  //| No aggregate root found with id '114da344-e708-4cc6-bac9-017f5a8745f9'
                                                  //| Category: ApplicationProblem
                                                  //| Severity: Minor
                                                  //| Arguments: Map()
                                                  //| )
                                                  //| NotExecuted(A setaddress68,almhirt.NotFoundProblem
                                                  //| No aggregate root found with id '429223a9-e4fb-4f97-a184-783deee8beca'
                                                  //| Category: ApplicationProblem
                                                  //| Severity: Minor
                                                  //| Arguments: Map()
                                                  //| )
                                                  //| NotExecuted(A setaddress69,almhirt.NotFoundProblem
                                                  //| No aggregate root found with id '71c531b8-117a-47a0-80f0-73c8fccb4b04'
                                                  //| Category: ApplicationProblem
                                                  //| Severity: Minor
                                                  //| Arguments: Map()
                                                  //| )
                                                  //| NotExecuted(A setaddress70,almhirt.NotFoundProblem
                                                  //| No aggregate root found with id '04dab377-e656-43ec-a9c9-c651b6b3d7cd'
                                                  //| Category: ApplicationProblem
                                                  //| Severity: Minor
                                                  //| Arguments: Map()
                                                  //| )
                                                  //| NotExecuted(A setaddress71,almhirt.NotFoundProblem
                                                  //| No aggregate root found with id '3e0ea99f-48a5-42b9-ad5c-45ee3a78b66d'
                                                  //| Category: ApplicationProblem
                                                  //| Severity: Minor
                                                  //| Arguments: Map()
                                                  //| )
                                                  //| NotExecuted(A setaddress72,almhirt.NotFoundProblem
                                                  //| No aggregate root found with id '35ccc7c7-ac4a-47e0-8518-07f6e6133303'
                                                  //| Category: ApplicationProblem
                                                  //| Severity: Minor
                                                  //| Arguments: Map()
                                                  //| )
                                                  //| NotExecuted(A setaddress73,almhirt.NotFoundProblem
                                                  //| No aggregate root found with id 'd058434a-9f32-48e8-8183-bf7e370c7db2'
                                                  //| Category: ApplicationProblem
                                                  //| Severity: Minor
                                                  //| Arguments: Map()
                                                  //| )
                                                  //| NotExecuted(A setaddress74,almhirt.NotFoundProblem
                                                  //| No aggregate root found with id '7d49005e-76d1-4300-bce6-d0f9dd832b3d'
                                                  //| Category: ApplicationProblem
                                                  //| Severity: Minor
                                                  //| Arguments: Map()
                                                  //| )
                                                  //| Executed(A setaddress75)
                                                  //| NotExecuted(A setaddress76,almhirt.NotFoundProblem
                                                  //| No aggregate root found with id 'c5fb945c-814f-4559-8e4a-8ebac20312ce'
                                                  //| Category: ApplicationProblem
                                                  //| Severity: Minor
                                                  //| Arguments: Map()
                                                  //| )
                                                  //| NotExecuted(A setaddress77,almhirt.NotFoundProblem
                                                  //| No aggregate root found with id '4a8fc4f6-7b7d-46ed-a99b-40337f076e50'
                                                  //| Category: ApplicationProblem
                                                  //| Severity: Minor
                                                  //| Arguments: Map()
                                                  //| )
                                                  //| NotExecuted(A setaddress78,almhirt.NotFoundProblem
                                                  //| No aggregate root found with id 'bf4ef60a-19c0-4357-8678-5104a1dce752'
                                                  //| Category: ApplicationProblem
                                                  //| Severity: Minor
                                                  //| Arguments: Map()
                                                  //| )
                                                  //| NotExecuted(A setaddress79,almhirt.NotFoundProblem
                                                  //| No aggregate root found with id 'de9d9185-150e-4f19-bab0-e26f8ef98f3e'
                                                  //| Category: ApplicationProblem
                                                  //| Severity: Minor
                                                  //| Arguments: Map()
                                                  //| )
                                                  //| NotExecuted(A setaddress80,almhirt.NotFoundProblem
                                                  //| No aggregate root found with id '0185e341-cfd1-42e3-8ae6-4eabfe621e7c'
                                                  //| Category: ApplicationProblem
                                                  //| Severity: Minor
                                                  //| Arguments: Map()
                                                  //| )
                                                  //| NotExecuted(A setaddress81,almhirt.NotFoundProblem
                                                  //| No aggregate root found with id 'a3358e4c-bebe-482e-8561-449248f825ea'
                                                  //| Category: ApplicationProblem
                                                  //| Severity: Minor
                                                  //| Arguments: Map()
                                                  //| )
                                                  //| NotExecuted(A setaddress82,almhirt.NotFoundProblem
                                                  //| No aggregate root found with id '8449dc31-a574-4a36-a104-0b9b08a6fa63'
                                                  //| Category: ApplicationProblem
                                                  //| Severity: Minor
                                                  //| Arguments: Map()
                                                  //| )
                                                  //| NotExecuted(A setaddress83,almhirt.NotFoundProblem
                                                  //| No aggregate root found with id 'b59a2586-91f9-4f5f-8c49-5b83fe136513'
                                                  //| Category: ApplicationProblem
                                                  //| Severity: Minor
                                                  //| Arguments: Map()
                                                  //| )
                                                  //| NotExecuted(A setaddress84,almhirt.NotFoundProblem
                                                  //| No aggregate root found with id '94ad6955-5c81-420f-8a4c-cef18201aaf5'
                                                  //| Category: ApplicationProblem
                                                  //| Severity: Minor
                                                  //| Arguments: Map()
                                                  //| )
                                                  //| NotExecuted(A setaddress85,almhirt.NotFoundProblem
                                                  //| No aggregate root found with id '57761ee7-ad0d-414b-9284-971d1b75ff35'
                                                  //| Category: ApplicationProblem
                                                  //| Severity: Minor
                                                  //| Arguments: Map()
                                                  //| )
                                                  //| NotExecuted(A setaddress86,almhirt.NotFoundProblem
                                                  //| No aggregate root found with id 'f92c58c7-a199-40f3-b8f2-605f0954c469'
                                                  //| Category: ApplicationProblem
                                                  //| Severity: Minor
                                                  //| Arguments: Map()
                                                  //| )
                                                  //| NotExecuted(A setaddress87,almhirt.NotFoundProblem
                                                  //| No aggregate root found with id '1b0dfdb2-4cc3-46df-8899-38f067d62fc7'
                                                  //| Category: ApplicationProblem
                                                  //| Severity: Minor
                                                  //| Arguments: Map()
                                                  //| )
                                                  //| NotExecuted(A setaddress88,almhirt.NotFoundProblem
                                                  //| No aggregate root found with id '7b183540-a5cf-493c-8e17-0d538471b130'
                                                  //| Category: ApplicationProblem
                                                  //| Severity: Minor
                                                  //| Arguments: Map()
                                                  //| )
                                                  //| NotExecuted(A setaddress89,almhirt.NotFoundProblem
                                                  //| No aggregate root found with id '8346dc03-3c06-4dfa-bdf0-90dc31e98ed3'
                                                  //| Category: ApplicationProblem
                                                  //| Severity: Minor
                                                  //| Arguments: Map()
                                                  //| )
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
                                                  //| argetted version is '2'. The refused command is 'MoveBecauseOfMarriage(afd5
                                                  //| cf95-3ccf-47c0-b274-61546e6e4ee9,Some(2),namemarriageName0,addressmarriageN
                                                  //| ame0)'
                                                  //| Category: ApplicationProblem
                                                  //| Severity: Minor
                                                  //| Arguments: Map()
                                                  //| )
                                                  //| NotExecuted(A updatemarriage1,almhirt.CollisionProblem
                                                  //| Refused to handle command: Versions do not match. Current version is '1', t
                                                  //| argetted version is '2'. The refused command is 'MoveBecauseOfMarriage(8bfc
                                                  //| f820-bc7d-4e81-8080-32d4b5eeedbc,Some(2),namemarriageName1,addressmarriageN
                                                  //| ame1)'
                                                  //| Category: ApplicationProblem
                                                  //| Severity: Minor
                                                  //| Arguments: Map()
                                                  //| )
                                                  //| NotExecuted(A updatemarriage2,almhirt.CollisionProblem
                                                  //| Refused to handle command: Versions do not match. Current version is '1', t
                                                  //| argetted version is '2'. The refused command is 'MoveBecauseOfMarriage(04d2
                                                  //| 73c3-393b-4d51-bd65-1d4095ff72d9,Some(2),namemarriageName2,addressmarriageN
                                                  //| ame2)'
                                                  //| Category: ApplicationProblem
                                                  //| Severity: Minor
                                                  //| Arguments: Map()
                                                  //| )
                                                  //| NotExecuted(A updatemarriage3,almhirt.CollisionProblem
                                                  //| Refused to handle command: Versions do not match. Current version is '1', t
                                                  //| argetted version is '2'. The refused command is 'MoveBecauseOfMarriage(1a9d
                                                  //| da24-2a20-41c9-8d96-740028d3f1a0,Some(2),namemarriageName3,addressmarriageN
                                                  //| ame3)'
                                                  //| Category: ApplicationProblem
                                                  //| Severity: Minor
                                                  //| Arguments: Map()
                                                  //| )
                                                  //| NotExecuted(A updatemarriage4,almhirt.CollisionProblem
                                                  //| Refused to handle command: Versions do not match. Current version is '1', t
                                                  //| argetted version is '2'. The refused command is 'MoveBecauseOfMarriage(f437
                                                  //| 639f-04a0-48f4-ab80-415e01eade32,Some(2),namemarriageName4,addressmarriageN
                                                  //| ame4)'
                                                  //| Category: ApplicationProblem
                                                  //| Severity: Minor
                                                  //| Arguments: Map()
                                                  //| )
                                                  //| NotExecuted(A updatemarriage5,almhirt.CollisionProblem
                                                  //| Refused to handle command: Versions do not match. Current version is '1', t
                                                  //| argetted version is '2'. The refused command is 'MoveBecauseOfMarriage(cf67
                                                  //| d662-51fa-4e06-9624-725946ae59a9,Some(2),namemarriageName5,addressmarriageN
                                                  //| ame5)'
                                                  //| Category: ApplicationProblem
                                                  //| Severity: Minor
                                                  //| Arguments: Map()
                                                  //| )
                                                  //| NotExecuted(A updatemarriage6,almhirt.CollisionProblem
                                                  //| Refused to handle command: Versions do not match. Current version is '1', t
                                                  //| argetted version is '2'. The refused command is 'MoveBecauseOfMarriage(8b69
                                                  //| a528-958f-49db-841c-256b2ebce91c,Some(2),namemarriageName6,addressmarriageN
                                                  //| ame6)'
                                                  //| Category: ApplicationProblem
                                                  //| Severity: Minor
                                                  //| Arguments: Map()
                                                  //| )
                                                  //| NotExecuted(A updatemarriage7,almhirt.CollisionProblem
                                                  //| Refused to handle command: Versions do not match. Current version is '1', t
                                                  //| argetted version is '2'. The refused command is 'MoveBecauseOfMarriage(f28c
                                                  //| e60c-b67f-4543-bbbc-c988db9fcbb6,Some(2),namemarriageName7,addressmarriageN
                                                  //| ame7)'
                                                  //| Category: ApplicationProblem
                                                  //| Severity: Minor
                                                  //| Arguments: Map()
                                                  //| )
                                                  //| NotExecuted(A updatemarriage8,almhirt.CollisionProblem
                                                  //| Refused to handle command: Versions do not match. Current version is '1', t
                                                  //| argetted version is '2'. The refused command is 'MoveBecauseOfMarriage(763f
                                                  //| 712a-fe13-4a54-9082-6215ade78647,Some(2),namemarriageName8,addressmarriageN
                                                  //| ame8)'
                                                  //| Category: ApplicationProblem
                                                  //| Severity: Minor
                                                  //| Arguments: Map()
                                                  //| )
                                                  //| NotExecuted(A updatemarriage9,almhirt.CollisionProblem
                                                  //| Refused to handle command: Versions do not match. Current version is '1', t
                                                  //| argetted version is '2'. The refused command is 'MoveBecauseOfMarriage(293e
                                                  //| 4bda-a4c8-4781-afc5-c8727c754e29,Some(2),namemarriageName9,addressmarriageN
                                                  //| ame9)'
                                                  //| Category: ApplicationProblem
                                                  //| Severity: Minor
                                                  //| Arguments: Map()
                                                  //| )
                                                  //| NotExecuted(A updatemarriage10,almhirt.CollisionProblem
                                                  //| Refused to handle command: Versions do not match. Current version is '1', t
                                                  //| argetted version is '2'. The refused command is 'MoveBecauseOfMarriage(a566
                                                  //| ac87-a589-4ed0-a763-9a967821a691,Some(2),namemarriageName10,addressmarriage
                                                  //| Name10)'
                                                  //| Category: ApplicationProblem
                                                  //| Severity: Minor
                                                  //| Arguments: Map()
                                                  //| )
                                                  //| NotExecuted(A updatemarriage11,almhirt.CollisionProblem
                                                  //| Refused to handle command: Versions do not match. Current version is '1', t
                                                  //| argetted version is '2'. The refused command is 'MoveBecauseOfMarriage(18b3
                                                  //| 86bd-8c35-446c-837f-18fbc84ce34c,Some(2),namemarriageName11,addressmarriage
                                                  //| Name11)'
                                                  //| Category: ApplicationProblem
                                                  //| Severity: Minor
                                                  //| Arguments: Map()
                                                  //| )
                                                  //| NotExecuted(A updatemarriage12,almhirt.CollisionProblem
                                                  //| Refused to handle command: Versions do not match. Current version is '1', t
                                                  //| argetted version is '2'. The refused command is 'MoveBecauseOfMarriage(353f
                                                  //| 4e51-bb8a-44d6-b935-bcca3f0c13be,Some(2),namemarriageName12,addressmarriage
                                                  //| Name12)'
                                                  //| Category: ApplicationProblem
                                                  //| Severity: Minor
                                                  //| Arguments: Map()
                                                  //| )
                                                  //| NotExecuted(A updatemarriage13,almhirt.CollisionProblem
                                                  //| Refused to handle command: Versions do not match. Current version is '1', t
                                                  //| argetted version is '2'. The refused command is 'MoveBecauseOfMarriage(c274
                                                  //| ab59-aff5-42d9-b152-3ef22c5df4f7,Some(2),namemarriageName13,addressmarriage
                                                  //| Name13)'
                                                  //| Category: ApplicationProblem
                                                  //| Severity: Minor
                                                  //| Arguments: Map()
                                                  //| )
                                                  //| NotExecuted(A updatemarriage14,almhirt.CollisionProblem
                                                  //| Refused to handle command: Versions do not match. Current version is '1', t
                                                  //| argetted version is '2'. The refused command is 'MoveBecauseOfMarriage(50f5
                                                  //| 7989-d15a-4124-9e52-5ef1a3b4789a,Some(2),namemarriageName14,addressmarriage
                                                  //| Name14)'
                                                  //| Category: ApplicationProblem
                                                  //| Severity: Minor
                                                  //| Arguments: Map()
                                                  //| )
                                                  //| NotExecuted(A updatemarriage15,almhirt.CollisionProblem
                                                  //| Refused to handle command: Versions do not match. Current version is '1', t
                                                  //| argetted version is '2'. The refused command is 'MoveBecauseOfMarriage(67d3
                                                  //| 4a34-18f4-46d8-a711-cead57880f6c,Some(2),namemarriageName15,addressmarriage
                                                  //| Name15)'
                                                  //| Category: ApplicationProblem
                                                  //| Severity: Minor
                                                  //| Arguments: Map()
                                                  //| )
                                                  //| NotExecuted(A updatemarriage16,almhirt.CollisionProblem
                                                  //| Refused to handle command: Versions do not match. Current version is '1', t
                                                  //| argetted version is '2'. The refused command is 'MoveBecauseOfMarriage(094f
                                                  //| cd7e-7853-4b1f-a133-63ea0bfdea46,Some(2),namemarriageName16,addressmarriage
                                                  //| Name16)'
                                                  //| Category: ApplicationProblem
                                                  //| Severity: Minor
                                                  //| Arguments: Map()
                                                  //| )
                                                  //| NotExecuted(A updatemarriage17,almhirt.CollisionProblem
                                                  //| Refused to handle command: Versions do not match. Current version is '1', t
                                                  //| argetted version is '2'. The refused command is 'MoveBecauseOfMarriage(89b7
                                                  //| a003-2ee4-4a88-bf68-7a57c46ea4dd,Some(2),namemarriageName17,addressmarriage
                                                  //| Name17)'
                                                  //| Category: ApplicationProblem
                                                  //| Severity: Minor
                                                  //| Arguments: Map()
                                                  //| )
                                                  //| NotExecuted(A updatemarriage18,almhirt.CollisionProblem
                                                  //| Refused to handle command: Versions do not match. Current version is '1', t
                                                  //| argetted version is '2'. The refused command is 'MoveBecauseOfMarriage(7f3e
                                                  //| cd53-631f-4300-a52e-196998ea06e9,Some(2),namemarriageName18,addressmarriage
                                                  //| Name18)'
                                                  //| Category: ApplicationProblem
                                                  //| Severity: Minor
                                                  //| Arguments: Map()
                                                  //| )
                                                  //| NotExecuted(A updatemarriage19,almhirt.CollisionProblem
                                                  //| Refused to handle command: Versions do not match. Current version is '1', t
                                                  //| argetted version is '2'. The refused command is 'MoveBecauseOfMarriage(2d2c
                                                  //| 1da6-2ad2-4036-aca9-2fed7a098938,Some(2),namemarriageName19,addressmarriage
                                                  //| Name19)'
                                                  //| Category: ApplicationProblem
                                                  //| 
                                                  //| Output exceeds cutoff limit.
  }