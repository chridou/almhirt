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
    val idsAndNamesAndAdresses = Vector((for (i <- 0 until 50) yield (i, almhirt.getUuid, "Name%s".format(i), "Address%s".format(i))): _*)

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
                                                  //| --- updatemarriage done ---
                                                  //| NotExecuted(A updatemarriage0,almhirt.CollisionProblem
                                                  //| Refused to handle command: Versions do not match. Current version is '1', t
                                                  //| argetted version is '2'. The refused command is 'MoveBecauseOfMarriage(fa87
                                                  //| f9d0-6b35-43f9-8f19-f3d2765508fc,Some(2),namemarriageName0,addressmarriageN
                                                  //| ame0)'
                                                  //| Category: ApplicationProblem
                                                  //| Severity: Minor
                                                  //| Arguments: Map()
                                                  //| )
                                                  //| NotExecuted(A updatemarriage1,almhirt.CollisionProblem
                                                  //| Refused to handle command: Versions do not match. Current version is '1', t
                                                  //| argetted version is '2'. The refused command is 'MoveBecauseOfMarriage(6d94
                                                  //| 6d1b-5e19-42c3-980c-8712154dbde2,Some(2),namemarriageName1,addressmarriageN
                                                  //| ame1)'
                                                  //| Category: ApplicationProblem
                                                  //| Severity: Minor
                                                  //| Arguments: Map()
                                                  //| )
                                                  //| NotExecuted(A updatemarriage2,almhirt.CollisionProblem
                                                  //| Refused to handle command: Versions do not match. Current version is '1', t
                                                  //| argetted version is '2'. The refused command is 'MoveBecauseOfMarriage(df3d
                                                  //| 585c-9529-49ae-a208-fbee69245c59,Some(2),namemarriageName2,addressmarriageN
                                                  //| ame2)'
                                                  //| Category: ApplicationProblem
                                                  //| Severity: Minor
                                                  //| Arguments: Map()
                                                  //| )
                                                  //| NotExecuted(A updatemarriage3,almhirt.CollisionProblem
                                                  //| Refused to handle command: Versions do not match. Current version is '1', t
                                                  //| argetted version is '2'. The refused command is 'MoveBecauseOfMarriage(ce4e
                                                  //| 2413-cbc0-449e-8343-8e50d8920ec8,Some(2),namemarriageName3,addressmarriageN
                                                  //| ame3)'
                                                  //| Category: ApplicationProblem
                                                  //| Severity: Minor
                                                  //| Arguments: Map()
                                                  //| )
                                                  //| NotExecuted(A updatemarriage4,almhirt.CollisionProblem
                                                  //| Refused to handle command: Versions do not match. Current version is '1', t
                                                  //| argetted version is '2'. The refused command is 'MoveBecauseOfMarriage(f9b4
                                                  //| f1e3-fd5d-4c43-bb0e-e1049642f4dd,Some(2),namemarriageName4,addressmarriageN
                                                  //| ame4)'
                                                  //| Category: ApplicationProblem
                                                  //| Severity: Minor
                                                  //| Arguments: Map()
                                                  //| )
                                                  //| NotExecuted(A updatemarriage5,almhirt.CollisionProblem
                                                  //| Refused to handle command: Versions do not match. Current version is '1', t
                                                  //| argetted version is '2'. The refused command is 'MoveBecauseOfMarriage(c30e
                                                  //| 7a6c-3435-4df9-a300-6731d8c577f0,Some(2),namemarriageName5,addressmarriageN
                                                  //| ame5)'
                                                  //| Category: ApplicationProblem
                                                  //| Severity: Minor
                                                  //| Arguments: Map()
                                                  //| )
                                                  //| NotExecuted(A updatemarriage6,almhirt.CollisionProblem
                                                  //| Refused to handle command: Versions do not match. Current version is '1', t
                                                  //| argetted version is '2'. The refused command is 'MoveBecauseOfMarriage(2a42
                                                  //| cb0f-fe01-4a34-b415-c456e306be50,Some(2),namemarriageName6,addressmarriageN
                                                  //| ame6)'
                                                  //| Category: ApplicationProblem
                                                  //| Severity: Minor
                                                  //| Arguments: Map()
                                                  //| )
                                                  //| NotExecuted(A updatemarriage7,almhirt.CollisionProblem
                                                  //| Refused to handle command: Versions do not match. Current version is '1', t
                                                  //| argetted version is '2'. The refused command is 'MoveBecauseOfMarriage(4fdb
                                                  //| fe21-e55e-4068-8923-fd9b83d2224a,Some(2),namemarriageName7,addressmarriageN
                                                  //| ame7)'
                                                  //| Category: ApplicationProblem
                                                  //| Severity: Minor
                                                  //| Arguments: Map()
                                                  //| )
                                                  //| NotExecuted(A updatemarriage8,almhirt.CollisionProblem
                                                  //| Refused to handle command: Versions do not match. Current version is '1', t
                                                  //| argetted version is '2'. The refused command is 'MoveBecauseOfMarriage(250b
                                                  //| aa6b-2c4e-4a8b-8987-9f2f91cd3900,Some(2),namemarriageName8,addressmarriageN
                                                  //| ame8)'
                                                  //| Category: ApplicationProblem
                                                  //| Severity: Minor
                                                  //| Arguments: Map()
                                                  //| )
                                                  //| NotExecuted(A updatemarriage9,almhirt.CollisionProblem
                                                  //| Refused to handle command: Versions do not match. Current version is '1', t
                                                  //| argetted version is '2'. The refused command is 'MoveBecauseOfMarriage(fea5
                                                  //| 652a-efde-4ad3-8f5d-a1b8c071fde7,Some(2),namemarriageName9,addressmarriageN
                                                  //| ame9)'
                                                  //| Category: ApplicationProblem
                                                  //| Severity: Minor
                                                  //| Arguments: Map()
                                                  //| )
                                                  //| NotExecuted(A updatemarriage10,almhirt.CollisionProblem
                                                  //| Refused to handle command: Versions do not match. Current version is '1', t
                                                  //| argetted version is '2'. The refused command is 'MoveBecauseOfMarriage(0add
                                                  //| 55bb-7272-4171-9eed-e215e49f8acf,Some(2),namemarriageName10,addressmarriage
                                                  //| Name10)'
                                                  //| Category: ApplicationProblem
                                                  //| Severity: Minor
                                                  //| Arguments: Map()
                                                  //| )
                                                  //| NotExecuted(A updatemarriage11,almhirt.CollisionProblem
                                                  //| Refused to handle command: Versions do not match. Current version is '1', t
                                                  //| argetted version is '2'. The refused command is 'MoveBecauseOfMarriage(0019
                                                  //| a589-e143-44c6-96a5-326834ae0416,Some(2),namemarriageName11,addressmarriage
                                                  //| Name11)'
                                                  //| Category: ApplicationProblem
                                                  //| Severity: Minor
                                                  //| Arguments: Map()
                                                  //| )
                                                  //| NotExecuted(A updatemarriage12,almhirt.CollisionProblem
                                                  //| Refused to handle command: Versions do not match. Current version is '1', t
                                                  //| argetted version is '2'. The refused command is 'MoveBecauseOfMarriage(80c7
                                                  //| 7d62-d403-429d-b694-66755d49213f,Some(2),namemarriageName12,addressmarriage
                                                  //| Name12)'
                                                  //| Category: ApplicationProblem
                                                  //| Severity: Minor
                                                  //| Arguments: Map()
                                                  //| )
                                                  //| NotExecuted(A updatemarriage13,almhirt.CollisionProblem
                                                  //| Refused to handle command: Versions do not match. Current version is '1', t
                                                  //| argetted version is '2'. The refused command is 'MoveBecauseOfMarriage(4894
                                                  //| 0e4d-8dea-41e5-954a-107ec9da25ad,Some(2),namemarriageName13,addressmarriage
                                                  //| Name13)'
                                                  //| Category: ApplicationProblem
                                                  //| Severity: Minor
                                                  //| Arguments: Map()
                                                  //| )
                                                  //| NotExecuted(A updatemarriage14,almhirt.CollisionProblem
                                                  //| Refused to handle command: Versions do not match. Current version is '1', t
                                                  //| argetted version is '2'. The refused command is 'MoveBecauseOfMarriage(84bc
                                                  //| 7cae-dc9b-47b6-810b-64144e554c18,Some(2),namemarriageName14,addressmarriage
                                                  //| Name14)'
                                                  //| Category: ApplicationProblem
                                                  //| Severity: Minor
                                                  //| Arguments: Map()
                                                  //| )
                                                  //| NotExecuted(A updatemarriage15,almhirt.CollisionProblem
                                                  //| Refused to handle command: Versions do not match. Current version is '1', t
                                                  //| argetted version is '2'. The refused command is 'MoveBecauseOfMarriage(f6bc
                                                  //| f159-90ea-433b-ac5c-15a4e1a320dd,Some(2),namemarriageName15,addressmarriage
                                                  //| Name15)'
                                                  //| Category: ApplicationProblem
                                                  //| Severity: Minor
                                                  //| Arguments: Map()
                                                  //| )
                                                  //| NotExecuted(A updatemarriage16,almhirt.CollisionProblem
                                                  //| Refused to handle command: Versions do not match. Current version is '1', t
                                                  //| argetted version is '2'. The refused command is 'MoveBecauseOfMarriage(2aab
                                                  //| 806f-6f1c-4e2c-9921-78cd2af36e12,Some(2),namemarriageName16,addressmarriage
                                                  //| Name16)'
                                                  //| Category: ApplicationProblem
                                                  //| Severity: Minor
                                                  //| Arguments: Map()
                                                  //| )
                                                  //| NotExecuted(A updatemarriage17,almhirt.CollisionProblem
                                                  //| Refused to handle command: Versions do not match. Current version is '1', t
                                                  //| argetted version is '2'. The refused command is 'MoveBecauseOfMarriage(cda8
                                                  //| 5786-db81-4f1b-a6cb-2af1caed5a53,Some(2),namemarriageName17,addressmarriage
                                                  //| Name17)'
                                                  //| Category: ApplicationProblem
                                                  //| Severity: Minor
                                                  //| Arguments: Map()
                                                  //| )
                                                  //| NotExecuted(A updatemarriage18,almhirt.CollisionProblem
                                                  //| Refused to handle command: Versions do not match. Current version is '1', t
                                                  //| argetted version is '2'. The refused command is 'MoveBecauseOfMarriage(ca80
                                                  //| d654-a876-4314-9124-3d711b842f2c,Some(2),namemarriageName18,addressmarriage
                                                  //| Name18)'
                                                  //| Category: ApplicationProblem
                                                  //| Severity: Minor
                                                  //| Arguments: Map()
                                                  //| )
                                                  //| NotExecuted(A updatemarriage19,almhirt.CollisionProblem
                                                  //| Refused to handle command: Versions do not match. Current version is '1', t
                                                  //| argetted version is '2'. The refused command is 'MoveBecauseOfMarriage(8fea
                                                  //| 0694-a50e-4c20-b272-1c1b9d82c533,Some(2),namemarriageName19,addressmarriage
                                                  //| Name19)'
                                                  //| Category: ApplicationProblem
                                                  //| Severity: Minor
                                                  //| Arguments: Map()
                                                  //| )
                                                  //| NotExecuted(A updatemarriage20,almhirt.CollisionProblem
                                                  //| Refused to handle command: Versions do not match. Current version is '1', t
                                                  //| argetted version is '2'. The refused command is 'MoveBecauseOfMarriage(b096
                                                  //| 5af5-9582-4707-bdf5-b75f3224da5f,Some(2),namemarriageName20,addressmarriage
                                                  //| Name20)'
                                                  //| Category: ApplicationProblem
                                                  //| Severity: Minor
                                                  //| Arguments: Map()
                                                  //| )
                                                  //| NotExecuted(A updatemarriage21,almhirt.CollisionProblem
                                                  //| Refused to handle command: Versions do not match. Current version is '1', t
                                                  //| argetted version is '2'. The refused command is 'MoveBecauseOfMarriage(d10c
                                                  //| 6a17-181b-4869-a680-f96b24708882,Some(2),namemarriageName21,addressmarriage
                                                  //| Name21)'
                                                  //| Category: ApplicationProblem
                                                  //| Severity: Minor
                                                  //| Arguments: Map()
                                                  //| )
                                                  //| NotExecuted(A updatemarriage22,almhirt.CollisionProblem
                                                  //| Refused to handle command: Versions do not match. Current version is '1', t
                                                  //| argetted version is '2'. The refused command is 'MoveBecauseOfMarriage(c87c
                                                  //| a8be-ce73-4bb3-a840-de83e148e671,Some(2),namemarriageName22,addressmarriage
                                                  //| Name22)'
                                                  //| Category: ApplicationProblem
                                                  //| Severity: Minor
                                                  //| Arguments: Map()
                                                  //| )
                                                  //| NotExecuted(A updatemarriage23,almhirt.CollisionProblem
                                                  //| Refused to handle command: Versions do not match. Current version is '1', t
                                                  //| argetted version is '2'. The refused command is 'MoveBecauseOfMarriage(ea0d
                                                  //| 468e-3ba4-4b67-bc5b-e6ebd4a6c45a,Some(2),namemarriageName23,addressmarriage
                                                  //| Name23)'
                                                  //| Category: ApplicationProblem
                                                  //| Severity: Minor
                                                  //| Arguments: Map()
                                                  //| )
                                                  //| NotExecuted(A updatemarriage24,almhirt.CollisionProblem
                                                  //| Refused to handle command: Versions do not match. Current version is '1', t
                                                  //| argetted version is '2'. The refused command is 'MoveBecauseOfMarriage(b4cb
                                                  //| a9c4-fb88-433a-acb9-e2e89a8225c2,Some(2),namemarriageName24,addressmarriage
                                                  //| Name24)'
                                                  //| Category: ApplicationProblem
                                                  //| Severity: Minor
                                                  //| Arguments: Map()
                                                  //| )
                                                  //| NotExecuted(A updatemarriage25,almhirt.CollisionProblem
                                                  //| Refused to handle command: Versions do not match. Current version is '1', t
                                                  //| argetted version is '2'. The refused command is 'MoveBecauseOfMarriage(f000
                                                  //| 4d9a-4e78-4e18-9dc2-08fc1a9e1a61,Some(2),namemarriageName25,addressmarriage
                                                  //| Name25)'
                                                  //| Category: ApplicationProblem
                                                  //| Severity: Minor
                                                  //| Arguments: Map()
                                                  //| )
                                                  //| NotExecuted(A updatemarriage26,almhirt.CollisionProblem
                                                  //| Refused to handle command: Versions do not match. Current version is '1', t
                                                  //| argetted version is '2'. The refused command is 'MoveBecauseOfMarriage(3c74
                                                  //| 8374-b706-4159-8d9d-33c89dd02a15,Some(2),namemarriageName26,addressmarriage
                                                  //| Name26)'
                                                  //| Category: ApplicationProblem
                                                  //| Severity: Minor
                                                  //| Arguments: Map()
                                                  //| )
                                                  //| NotExecuted(A updatemarriage27,almhirt.CollisionProblem
                                                  //| Refused to handle command: Versions do not match. Current version is '1', t
                                                  //| argetted version is '2'. The refused command is 'MoveBecauseOfMarriage(267e
                                                  //| 0c39-ecf0-4c3f-9078-d30ab3452617,Some(2),namemarriageName27,addressmarriage
                                                  //| Name27)'
                                                  //| Category: ApplicationProblem
                                                  //| Severity: Minor
                                                  //| Arguments: Map()
                                                  //| )
                                                  //| NotExecuted(A updatemarriage28,almhirt.CollisionProblem
                                                  //| Refused to handle command: Versions do not match. Current version is '1', t
                                                  //| argetted version is '2'. The refused command is 'MoveBecauseOfMarriage(f5ff
                                                  //| 8a93-68e8-4162-8dbe-0e6e32719360,Some(2),namemarriageName28,addressmarriage
                                                  //| Name28)'
                                                  //| Category: ApplicationProblem
                                                  //| Severity: Minor
                                                  //| Arguments: Map()
                                                  //| )
                                                  //| NotExecuted(A updatemarriage29,almhirt.CollisionProblem
                                                  //| Refused to handle command: Versions do not match. Current version is '1', t
                                                  //| argetted version is '2'. The refused command is 'MoveBecauseOfMarriage(55ca
                                                  //| 4d5e-7950-498a-9ee9-54ee699430e7,Some(2),namemarriageName29,addressmarriage
                                                  //| Name29)'
                                                  //| Category: ApplicationProblem
                                                  //| Severity: Minor
                                                  //| Arguments: Map()
                                                  //| )
                                                  //| NotExecuted(A updatemarriage30,almhirt.CollisionProblem
                                                  //| Refused to handle command: Versions do not match. Current version is '1', t
                                                  //| argetted version is '2'. The refused command is 'MoveBecauseOfMarriage(ff90
                                                  //| 54fd-d5d2-4522-8b48-04ad84ce7c4e,Some(2),namemarriageName30,addressmarriage
                                                  //| Name30)'
                                                  //| Category: ApplicationProblem
                                                  //| Severity: Minor
                                                  //| Arguments: Map()
                                                  //| )
                                                  //| NotExecuted(A updatemarriage31,almhirt.CollisionProblem
                                                  //| Refused to handle command: Versions do not match. Current version is '1', t
                                                  //| argetted version is '2'. The refused command is 'MoveBecauseOfMarriage(cc58
                                                  //| 2992-47ce-41f2-bc8f-6c62ad24d07c,Some(2),namemarriageName31,addressmarriage
                                                  //| Name31)'
                                                  //| Category: ApplicationProblem
                                                  //| Severity: Minor
                                                  //| Arguments: Map()
                                                  //| )
                                                  //| NotExecuted(A updatemarriage32,almhirt.CollisionProblem
                                                  //| Refused to handle command: Versions do not match. Current version is '1', t
                                                  //| argetted version is '2'. The refused command is 'MoveBecauseOfMarriage(3c4a
                                                  //| d318-3678-49c0-8a55-59e116026332,Some(2),namemarriageName32,addressmarriage
                                                  //| Name32)'
                                                  //| Category: ApplicationProblem
                                                  //| Severity: Minor
                                                  //| Arguments: Map()
                                                  //| )
                                                  //| NotExecuted(A updatemarriage33,almhirt.CollisionProblem
                                                  //| Refused to handle command: Versions do not match. Current version is '1', t
                                                  //| argetted version is '2'. The refused command is 'MoveBecauseOfMarriage(6fe7
                                                  //| f008-d035-4bcb-8c9c-1595c77f23c4,Some(2),namemarriageName33,addressmarriage
                                                  //| Name33)'
                                                  //| Category: ApplicationProblem
                                                  //| Severity: Minor
                                                  //| Arguments: Map()
                                                  //| )
                                                  //| NotExecuted(A updatemarriage34,almhirt.CollisionProblem
                                                  //| Refused to handle command: Versions do not match. Current version is '1', t
                                                  //| argetted version is '2'. The refused command is 'MoveBecauseOfMarriage(dd42
                                                  //| ed6c-08ca-4315-9fd6-12f1d5941581,Some(2),namemarriageName34,addressmarriage
                                                  //| Name34)'
                                                  //| Category: ApplicationProblem
                                                  //| Severity: Minor
                                                  //| Arguments: Map()
                                                  //| )
                                                  //| NotExecuted(A updatemarriage35,almhirt.CollisionProblem
                                                  //| Refused to handle command: Versions do not match. Current version is '1', t
                                                  //| argetted version is '2'. The refused command is 'MoveBecauseOfMarriage(1f33
                                                  //| 76e7-5eb5-4765-b158-8f95e72ff0f1,Some(2),namemarriageName35,addressmarriage
                                                  //| Name35)'
                                                  //| Category: ApplicationProblem
                                                  //| Severity: Minor
                                                  //| Arguments: Map()
                                                  //| )
                                                  //| NotExecuted(A updatemarriage36,almhirt.CollisionProblem
                                                  //| Refused to handle command: Versions do not match. Current version is '1', t
                                                  //| argetted version is '2'. The refused command is 'MoveBecauseOfMarriage(e450
                                                  //| b346-ab66-446a-80e5-8ca6c2fa2741,Some(2),namemarriageName36,addressmarriage
                                                  //| Name36)'
                                                  //| Category: ApplicationProblem
                                                  //| Severity: Minor
                                                  //| Arguments: Map()
                                                  //| )
                                                  //| NotExecuted(A updatemarriage37,almhirt.CollisionProblem
                                                  //| Refused to handle command: Versions do not match. Current version is '1', t
                                                  //| argetted version is '2'. The refused command is 'MoveBecauseOfMarriage(02fe
                                                  //| 3718-ba5e-44b8-a9b3-ddd062fdf3c0,Some(2),namemarriageName37,addressmarriage
                                                  //| Name37)'
                                                  //| Category: ApplicationProblem
                                                  //| Severity: Minor
                                                  //| Arguments: Map()
                                                  //| )
                                                  //| NotExecuted(A updatemarriage38,almhirt.CollisionProblem
                                                  //| Refused to handle command: Versions do not match. Current version is '1', t
                                                  //| argetted version is '2'. The refused command is 'MoveBecauseOfMarriage(c68e
                                                  //| f66a-0df5-4aa4-86b1-711b5bfde408,Some(2),namemarriageName38,addressmarriage
                                                  //| Name38)'
                                                  //| Category: ApplicationProblem
                                                  //| Severity: Minor
                                                  //| Arguments: Map()
                                                  //| )
                                                  //| NotExecuted(A updatemarriage39,almhirt.CollisionProblem
                                                  //| Refused to handle command: Versions do not match. Current version is '1', t
                                                  //| argetted version is '2'. The refused command is 'MoveBecauseOfMarriage(5ec3
                                                  //| 0d11-6644-4f4a-8195-dfd48f58e4fc,Some(2),namemarriageName39,addressmarriage
                                                  //| Name39)'
                                                  //| Category: ApplicationProblem
                                                  //| Severity: Minor
                                                  //| Arguments: Map()
                                                  //| )
                                                  //| NotExecuted(A updatemarriage40,almhirt.CollisionProblem
                                                  //| Refused to handle command: Versions do not match. Current version is '1', t
                                                  //| argetted version is '2'. The refused command is 'MoveBecauseOfMarriage(6a2e
                                                  //| e45f-5718-4f00-bb13-9a3d8f89ec8a,Some(2),namemarriageName40,addressmarriage
                                                  //| Name40)'
                                                  //| Category: ApplicationProblem
                                                  //| Severity: Minor
                                                  //| Arguments: Map()
                                                  //| )
                                                  //| NotExecuted(A updatemarriage41,almhirt.CollisionProblem
                                                  //| Refused to handle command: Versions do not match. Current version is '1', t
                                                  //| argetted version is '2'. The refused command is 'MoveBecauseOfMarriage(c60d
                                                  //| c789-5421-4a71-8912-f48fb6505cb1,Some(2),namemarriageName41,addressmarriage
                                                  //| Name41)'
                                                  //| Category: ApplicationProblem
                                                  //| Severity: Minor
                                                  //| Arguments: Map()
                                                  //| )
                                                  //| NotExecuted(A updatemarriage42,almhirt.CollisionProblem
                                                  //| Refused to handle command: Versions do not match. Current version is '1', t
                                                  //| argetted version is '2'. The refused command is 'MoveBecauseOfMarriage(3e59
                                                  //| b0c7-fb2b-43fa-9e9b-1fac2b30682a,Some(2),namemarriageName42,addressmarriage
                                                  //| Name42)'
                                                  //| Category: ApplicationProblem
                                                  //| Severity: Minor
                                                  //| Arguments: Map()
                                                  //| )
                                                  //| NotExecuted(A updatemarriage43,almhirt.CollisionProblem
                                                  //| Refused to handle command: Versions do not match. Current version is '1', t
                                                  //| argetted version is '2'. The refused command is 'MoveBecauseOfMarriage(c999
                                                  //| 0e34-2551-4bfb-82da-ad53e55cc97f,Some(2),namemarriageName43,addressmarriage
                                                  //| Name43)'
                                                  //| Category: ApplicationProblem
                                                  //| Severity: Minor
                                                  //| Arguments: Map()
                                                  //| )
                                                  //| NotExecuted(A updatemarriage44,almhirt.CollisionProblem
                                                  //| Refused to handle command: Versions do not match. Current version is '1', t
                                                  //| argetted version is '2'. The refused command is 'MoveBecauseOfMarriage(f26c
                                                  //| 1bcc-570a-4d8b-8c1f-d54b13d3faaa,Some(2),namemarriageName44,addressmarriage
                                                  //| Name44)'
                                                  //| Category: ApplicationProblem
                                                  //| Severity: Minor
                                                  //| Arguments: Map()
                                                  //| )
                                                  //| NotExecuted(A updatemarriage45,almhirt.CollisionProblem
                                                  //| Refused to handle command: Versions do not match. Current version is '1', t
                                                  //| argetted version is '2'. The refused command is 'MoveBecauseOfMarriage(27af
                                                  //| a35e-cadb-457d-9e9d-7c58adf9f8cf,Some(2),namemarriageName45,addressmarriage
                                                  //| Name45)'
                                                  //| Category: ApplicationProblem
                                                  //| Severity: Minor
                                                  //| Arguments: Map()
                                                  //| )
                                                  //| NotExecuted(A updatemarriage46,almhirt.CollisionProblem
                                                  //| Refused to handle command: Versions do not match. Current version is '1', t
                                                  //| argetted version is '2'. The refused command is 'MoveBecauseOfMarriage(70ea
                                                  //| 8249-a9cf-47a8-b2c3-5ef63562099e,Some(2),namemarriageName46,addressmarriage
                                                  //| Name46)'
                                                  //| Category: ApplicationProblem
                                                  //| Severity: Minor
                                                  //| Arguments: Map()
                                                  //| )
                                                  //| NotExecuted(A updatemarriage47,almhirt.CollisionProblem
                                                  //| Refused to handle command: Versions do not match. Current version is '1', t
                                                  //| argetted version is '2'. The refused command is 'MoveBecauseOfMarriage(1984
                                                  //| 080c-171a-4fd7-93cd-8ac45eeeae59,Some(2),namemarriageName47,addressmarriage
                                                  //| Name47)'
                                                  //| Category: ApplicationProblem
                                                  //| Severity: Minor
                                                  //| Arguments: Map()
                                                  //| )
                                                  //| NotExecuted(A updatemarriage48,almhirt.CollisionProblem
                                                  //| Refused to handle command: Versions do not match. Current version is '1', t
                                                  //| argetted version is '2'. The refused command is 'MoveBecauseOfMarriage(fd53
                                                  //| c1cb-1e0a-4b73-98d0-a37c98b5c71d,Some(2),namemarriageName48,addressmarriage
                                                  //| Name48)'
                                                  //| Category: ApplicationProblem
                                                  //| Severity: Minor
                                                  //| Arguments: Map()
                                                  //| )
                                                  //| NotExecuted(A updatemarriage49,almhirt.CollisionProblem
                                                  //| Refused to handle command: Versions do not match. Current version is '1', t
                                                  //| argetted version is '2'. The refused command is 'MoveBecauseOfMarriage(4ef9
                                                  //| 0308-70e6-4da2-a570-a2a3dc50fa52,Some(2),namemarriageName49,addressmarriage
                                                  //| Name49)'
                                                  //| Category: ApplicationProblem
                                                  //| Severity: Minor
                                                  //| Arguments: Map()
                                                  //| )
                                                  //| --- updatename done ---
                                                  //| NotExecuted(A updatename0,almhirt.CollisionProblem
                                                  //| Refused to handle command: Versions do not match. Current version is '1', t
                                                  //| argetted version is '4'. The refused command is 'ChangeTestPersonName(fa87f
                                                  //| 9d0-6b35-43f9-8f19-f3d2765508fc,Some(4),newName0)'
                                                  //| Category: ApplicationProblem
                                                  //| Severity: Minor
                                                  //| Arguments: Map()
                                                  //| )
                                                  //| NotExecuted(A updatename1,almhirt.CollisionProblem
                                                  //| Refused to handle command: Versions do not match. Current version is '1', t
                                                  //| argetted version is '4'. The refused command is 'ChangeTestPersonName(6d946
                                                  //| d1b-5e19-42c3-980c-8712154dbde2,Some(4),newName1)'
                                                  //| Category: ApplicationProblem
                                                  //| Severity: Minor
                                                  //| Arguments: Map()
                                                  //| )
                                                  //| NotExecuted(A updatename2,almhirt.CollisionProblem
                                                  //| Refused to handle command: Versions do not match. Current version is '1', t
                                                  //| argetted version is '4'. The refused command is 'ChangeTestPersonName(df3d5
                                                  //| 85c-9529-49ae-a208-fbee69245c59,Some(4),newName2)'
                                                  //| Category: ApplicationProblem
                                                  //| Severity: Minor
                                                  //| Arguments: Map()
                                                  //| )
                                                  //| NotExecuted(A updatename3,almhirt.CollisionProblem
                                                  //| Refused to handle command: Versions do not match. Current version is '1', t
                                                  //| argetted version is '4'. The refused command is 'ChangeTestPersonName(ce4e2
                                                  //| 413-cbc0-449e-8343-8e50d8920ec8,Some(4),newName3)'
                                                  //| Category: ApplicationProblem
                                                  //| Severity: Minor
                                                  //| Arguments: Map()
                                                  //| )
                                                  //| NotExecuted(A updatename4,almhirt.CollisionProblem
                                                  //| Refused to handle command: Versions do not match. Current version is '1', t
                                                  //| argetted version is '4'. The refused command is 'ChangeTestPersonName(f9b4f
                                                  //| 1e3-fd5d-4c43-bb0e-e1049642f4dd,Some(4),newName4)'
                                                  //| Category: ApplicationProblem
                                                  //| Severity: Minor
                                                  //| Arguments: Map()
                                                  //| )
                                                  //| NotExecuted(A updatename5,almhirt.CollisionProblem
                                                  //| Refused to handle command: Versions do not match. Current version is '1', t
                                                  //| argetted version is '4'. The refused command is 'ChangeTestPersonName(c30e7
                                                  //| a6c-3435-4df9-a300-6731d8c577f0,Some(4),newName5)'
                                                  //| Category: ApplicationProblem
                                                  //| Severity: Minor
                                                  //| Arguments: Map()
                                                  //| )
                                                  //| NotExecuted(A updatename6,almhirt.CollisionProblem
                                                  //| Refused to handle command: Versions do not match. Current version is '1', t
                                                  //| argetted version is '4'. The refused command is 'ChangeTestPersonName(2a42c
                                                  //| b0f-fe01-4a34-b415-c456e306be50,Some(4),newName6)'
                                                  //| Category: ApplicationProblem
                                                  //| Severity: Minor
                                                  //| Arguments: Map()
                                                  //| )
                                                  //| NotExecuted(A updatename7,almhirt.CollisionProblem
                                                  //| Refused to handle command: Versions do not match. Current version is '1', t
                                                  //| argetted version is '4'. The refused command is 'ChangeTestPersonName(4fdbf
                                                  //| e21-e55e-4068-8923-fd9b83d2224a,Some(4),newName7)'
                                                  //| Category: ApplicationProblem
                                                  //| Severity: Minor
                                                  //| Arguments: Map()
                                                  //| )
                                                  //| NotExecuted(A updatename8,almhirt.CollisionProblem
                                                  //| Refused to handle command: Versions do not match. Current version is '1', t
                                                  //| argetted version is '4'. The refused command is 'ChangeTestPersonName(250ba
                                                  //| a6b-2c4e-4a8b-8987-9f2f91cd3900,Some(4),newName8)'
                                                  //| Category: ApplicationProblem
                                                  //| Severity: Minor
                                                  //| Arguments: Map()
                                                  //| )
                                                  //| NotExecuted(A updatename9,almhirt.CollisionProblem
                                                  //| Refused to handle command: Versions do not match. Current version is '1', t
                                                  //| argetted version is '4'. The refused command is 'ChangeTestPersonName(fea56
                                                  //| 52a-efde-4ad3-8f5d-a1b8c071fde7,Some(4),newName9)'
                                                  //| Category: ApplicationProblem
                                                  //| Severity: Minor
                                                  //| Arguments: Map()
                                                  //| )
                                                  //| NotExecuted(A updatename10,almhirt.CollisionProblem
                                                  //| Refused to handle command: Versions do not match. Current version is '1', t
                                                  //| argetted version is '4'. The refused command is 'ChangeTestPersonName(0add5
                                                  //| 5bb-7272-4171-9eed-e215e49f8acf,Some(4),newName10)'
                                                  //| Category: ApplicationProblem
                                                  //| Severity: Minor
                                                  //| Arguments: Map()
                                                  //| )
                                                  //| NotExecuted(A updatename11,almhirt.CollisionProblem
                                                  //| Refused to handle command: Versions do not match. Current version is '1', t
                                                  //| argetted version is '4'. The refused command is 'ChangeTestPersonName(0019a
                                                  //| 589-e143-44c6-96a5-326834ae0416,Some(4),newName11)'
                                                  //| Category: ApplicationProblem
                                                  //| Severity: Minor
                                                  //| Arguments: Map()
                                                  //| )
                                                  //| NotExecuted(A updatename12,almhirt.CollisionProblem
                                                  //| Refused to handle command: Versions do not match. Current version is '1', t
                                                  //| argetted version is '4'. The refused command is 'ChangeTestPersonName(80c77
                                                  //| d62-d403-429d-b694-66755d49213f,Some(4),newName12)'
                                                  //| Category: ApplicationProblem
                                                  //| Severity: Minor
                                                  //| Arguments: Map()
                                                  //| )
                                                  //| NotExecuted(A updatename13,almhirt.CollisionProblem
                                                  //| Refused to handle command: Versions do not match. Current version is '1', t
                                                  //| argetted version is '4'. The refused command is 'ChangeTestPersonName(48940
                                                  //| e4d-8dea-41e5-954a-107ec9da25ad,Some(4),newName13)'
                                                  //| Category: ApplicationProblem
                                                  //| Severity: Minor
                                                  //| Arguments: Map()
                                                  //| )
                                                  //| NotExecuted(A updatename14,almhirt.CollisionProblem
                                                  //| Refused to handle command: Versions do not match. Current version is '1', t
                                                  //| argetted version is '4'. The refused command is 'ChangeTestPersonName(84bc7
                                                  //| cae-dc9b-47b6-810b-64144e554c18,Some(4),newName14)'
                                                  //| Category: ApplicationProblem
                                                  //| Severity: Minor
                                                  //| Arguments: Map()
                                                  //| )
                                                  //| NotExecuted(A updatename15,almhirt.CollisionProblem
                                                  //| Refused to handle command: Versions do not match. Current version is '1', t
                                                  //| argetted version is '4'. The refused command is 'ChangeTestPersonName(f6bcf
                                                  //| 159-90ea-433b-ac5c-15a4e1a320dd,Some(4),newName15)'
                                                  //| Category: ApplicationProblem
                                                  //| Severity: Minor
                                                  //| Arguments: Map()
                                                  //| )
                                                  //| NotExecuted(A updatename16,almhirt.CollisionProblem
                                                  //| Refused to handle command: Versions do not match. Current version is '1', t
                                                  //| argetted version is '4'. The refused command is 'ChangeTestPersonName(2aab8
                                                  //| 06f-6f1c-4e2c-9921-78cd2af36e12,Some(4),newName16)'
                                                  //| Category: ApplicationProblem
                                                  //| Severity: Minor
                                                  //| Arguments: Map()
                                                  //| )
                                                  //| NotExecuted(A updatename17,almhirt.CollisionProblem
                                                  //| Refused to handle command: Versions do not match. Current version is '1', t
                                                  //| argetted version is '4'. The refused command is 'ChangeTestPersonName(cda85
                                                  //| 786-db81-4f1b-a6cb-2af1caed5a53,Some(4),newName17)'
                                                  //| Category: ApplicationProblem
                                                  //| Severity: Minor
                                                  //| Arguments: Map()
                                                  //| )
                                                  //| NotExecuted(A updatename18,almhirt.CollisionProblem
                                                  //| Refused to handle command: Versions do not match. Current version is '1', t
                                                  //| argetted version is '4'. The refused command is 'ChangeTestPersonName(ca80d
                                                  //| 654-a876-4314-9124-3d711b842f2c,Some(4),newName18)'
                                                  //| Category: ApplicationProblem
                                                  //| Severity: Minor
                                                  //| Arguments: Map()
                                                  //| )
                                                  //| NotExecuted(A updatename19,almhirt.CollisionProblem
                                                  //| Refused to handle command: Versions do not match. Current version is '1', t
                                                  //| argetted version is '4'. The refused command is 'ChangeTestPersonName(8fea0
                                                  //| 694-a50e-4c20-b272-1c1b9d82c533,Some(4),newName19)'
                                                  //| Category: ApplicationProblem
                                                  //| Severity: Minor
                                                  //| Arguments: Map()
                                                  //| )
                                                  //| NotExecuted(A updatename20,almhirt.CollisionProblem
                                                  //| Refused to handle command: Versions do not match. Current version is '1', t
                                                  //| argetted version is '4'. The refused command is 'ChangeTestPersonName(b0965
                                                  //| af5-9582-4707-bdf5-b75f3224da5f,Some(4),newName20)'
                                                  //| Category: ApplicationProblem
                                                  //| Severity: Minor
                                                  //| Arguments: Map()
                                                  //| )
                                                  //| NotExecuted(A updatename21,almhirt.CollisionProblem
                                                  //| Refused to handle command: Versions do not match. Current version is '1', t
                                                  //| argetted version is '4'. The refused command is 'ChangeTestPersonName(d10c6
                                                  //| a17-181b-4869-a680-f96b24708882,Some(4),newName21)'
                                                  //| Category: ApplicationProblem
                                                  //| Severity: Minor
                                                  //| Arguments: Map()
                                                  //| )
                                                  //| NotExecuted(A updatename22,almhirt.CollisionProblem
                                                  //| Refused to handle command: Versions do not match. Current version is '1', t
                                                  //| argetted version is '4'. The refused command is 'ChangeTestPersonName(c87ca
                                                  //| 8be-ce73-4bb3-a840-de83e148e671,Some(4),newName22)'
                                                  //| Category: ApplicationProblem
                                                  //| Severity: Minor
                                                  //| Arguments: Map()
                                                  //| )
                                                  //| NotExecuted(A updatename23,almhirt.CollisionProblem
                                                  //| Refused to handle command: Versions do not match. Current version is '1', t
                                                  //| argetted version is '4'. The refused command is 'ChangeTestPersonName(ea0d4
                                                  //| 68e-3ba4-4b67-bc5b-e6ebd4a6c45a,Some(4),newName23)'
                                                  //| Category: ApplicationProblem
                                                  //| Severity: Minor
                                                  //| Arguments: Map()
                                                  //| )
                                                  //| NotExecuted(A updatename24,almhirt.CollisionProblem
                                                  //| Refused to handle command: Versions do not match. Current version is '1', t
                                                  //| argetted version is '4'. The refused command is 'ChangeTestPersonName(b4cba
                                                  //| 9c4-fb88-433a-acb9-e2e89a8225c2,Some(4),newName24)'
                                                  //| Category: ApplicationProblem
                                                  //| Severity: Minor
                                                  //| Arguments: Map()
                                                  //| )
                                                  //| NotExecuted(A updatename25,almhirt.CollisionProblem
                                                  //| Refused to handle command: Versions do not match. Current version is '1', t
                                                  //| argetted version is '4'. The refused command is 'ChangeTestPersonName(f0004
                                                  //| d9a-4e78-4e18-9dc2-08fc1a9e1a61,Some(4),newName25)'
                                                  //| Category: ApplicationProblem
                                                  //| Severity: Minor
                                                  //| Arguments: Map()
                                                  //| )
                                                  //| NotExecuted(A updatename26,almhirt.CollisionProblem
                                                  //| Refused to handle command: Versions do not match. Current version is '1', t
                                                  //| argetted version is '4'. The refused command is 'ChangeTestPersonName(3c748
                                                  //| 374-b706-4159-8d9d-33c89dd02a15,Some(4),newName26)'
                                                  //| Category: ApplicationProblem
                                                  //| Severity: Minor
                                                  //| Arguments: Map()
                                                  //| )
                                                  //| NotExecuted(A updatename27,almhirt.CollisionProblem
                                                  //| Refused to handle command: Versions do not match. Current version is '1', t
                                                  //| argetted version is '4'. The refused command is 'ChangeTestPersonName(267e0
                                                  //| c39-ecf0-4c3f-9078-d30ab3452617,Some(4),newName27)'
                                                  //| Category: ApplicationProblem
                                                  //| Severity: Minor
                                                  //| Arguments: Map()
                                                  //| )
                                                  //| NotExecuted(A updatename28,almhirt.CollisionProblem
                                                  //| Refused to handle command: Versions do not match. Current version is '1', t
                                                  //| argetted version is '4'. The refused command is 'ChangeTestPersonName(f5ff8
                                                  //| a93-68e8-4162-8dbe-0e6e32719360,Some(4),newName28)'
                                                  //| Category: ApplicationProblem
                                                  //| Severity: Minor
                                                  //| Arguments: Map()
                                                  //| )
                                                  //| NotExecuted(A updatename29,almhirt.CollisionProblem
                                                  //| Refused to handle command: Versions do not match. Current version is '1', t
                                                  //| argetted version is '4'. The refused command is 'ChangeTestPersonName(55ca4
                                                  //| d5e-7950-498a-9ee9-54ee699430e7,Some(4),newName29)'
                                                  //| Category: ApplicationProblem
                                                  //| Severity: Minor
                                                  //| Arguments: Map()
                                                  //| )
                                                  //| NotExecuted(A updatename30,almhirt.CollisionProblem
                                                  //| Refused to handle command: Versions do not match. Current version is '1', t
                                                  //| argetted version is '4'. The refused command is 'ChangeTestPersonName(ff905
                                                  //| 4fd-d5d2-4522-8b48-04ad84ce7c4e,Some(4),newName30)'
                                                  //| Category: ApplicationProblem
                                                  //| Severity: Minor
                                                  //| Arguments: Map()
                                                  //| )
                                                  //| NotExecuted(A updatename31,almhirt.CollisionProblem
                                                  //| Refused to handle command: Versions do not match. Current version is '1', t
                                                  //| argetted version is '4'. The refused command is 'ChangeTestPersonName(cc582
                                                  //| 992-47ce-41f2-bc8f-6c62ad24d07c,Some(4),newName31)'
                                                  //| Category: ApplicationProblem
                                                  //| Severity: Minor
                                                  //| Arguments: Map()
                                                  //| )
                                                  //| NotExecuted(A updatename32,almhirt.CollisionProblem
                                                  //| Refused to handle command: Versions do not match. Current version is '1', t
                                                  //| argetted version is '4'. The refused command is 'ChangeTestPersonName(3c4ad
                                                  //| 318-3678-49c0-8a55-59e116026332,Some(4),newName32)'
                                                  //| Category: ApplicationProblem
                                                  //| Severity: Minor
                                                  //| Arguments: Map()
                                                  //| )
                                                  //| NotExecuted(A updatename33,almhirt.CollisionProblem
                                                  //| Refused to handle command: Versions do not match. Current version is '1', t
                                                  //| argetted version is '4'. The refused command is 'ChangeTestPersonName(6fe7f
                                                  //| 008-d035-4bcb-8c9c-1595c77f23c4,Some(4),newName33)'
                                                  //| Category: ApplicationProblem
                                                  //| Severity: Minor
                                                  //| Arguments: Map()
                                                  //| )
                                                  //| NotExecuted(A updatename34,almhirt.CollisionProblem
                                                  //| Refused to handle command: Versions do not match. Current version is '1', t
                                                  //| argetted version is '4'. The refused command is 'ChangeTestPersonName(dd42e
                                                  //| d6c-08ca-4315-9fd6-12f1d5941581,Some(4),newName34)'
                                                  //| Category: ApplicationProblem
                                                  //| Severity: Minor
                                                  //| Arguments: Map()
                                                  //| )
                                                  //| NotExecuted(A updatename35,almhirt.CollisionProblem
                                                  //| Refused to handle command: Versions do not match. Current version is '1', t
                                                  //| argetted version is '4'. The refused command is 'ChangeTestPersonName(1f337
                                                  //| 6e7-5eb5-4765-b158-8f95e72ff0f1,Some(4),newName35)'
                                                  //| Category: ApplicationProblem
                                                  //| Severity: Minor
                                                  //| Arguments: Map()
                                                  //| )
                                                  //| NotExecuted(A updatename36,almhirt.CollisionProblem
                                                  //| Refused to handle command: Versions do not match. Current version is '1', t
                                                  //| argetted version is '4'. The refused command is 'ChangeTestPersonName(e450b
                                                  //| 346-ab66-446a-80e5-8ca6c2fa2741,Some(4),newName36)'
                                                  //| Category: ApplicationProblem
                                                  //| Severity: Minor
                                                  //| Arguments: Map()
                                                  //| )
                                                  //| NotExecuted(A updatename37,almhirt.CollisionProblem
                                                  //| Refused to handle command: Versions do not match. Current version is '1', t
                                                  //| argetted version is '4'. The refused command is 'ChangeTestPersonName(02fe3
                                                  //| 718-ba5e-44b8-a9b3-ddd062fdf3c0,Some(4),newName37)'
                                                  //| Category: ApplicationProblem
                                                  //| Severity: Minor
                                                  //| Arguments: Map()
                                                  //| )
                                                  //| NotExecuted(A updatename38,almhirt.CollisionProblem
                                                  //| Refused to handle command: Versions do not match. Current version is '1', t
                                                  //| argetted version is '4'. The refused command is 'ChangeTestPersonName(c68ef
                                                  //| 66a-0df5-4aa4-86b1-711b5bfde408,Some(4),newName38)'
                                                  //| Category: ApplicationProblem
                                                  //| Severity: Minor
                                                  //| Arguments: Map()
                                                  //| )
                                                  //| NotExecuted(A updatename39,almhirt.CollisionProblem
                                                  //| Refused to handle command: Versions do not match. Current version is '1', t
                                                  //| argetted version is '4'. The refused command is 'ChangeTestPersonName(5ec30
                                                  //| d11-6644-4f4a-8195-dfd48f58e4fc,Some(4),newName39)'
                                                  //| Category: ApplicationProblem
                                                  //| Severity: Minor
                                                  //| Arguments: Map()
                                                  //| )
                                                  //| NotExecuted(A updatename40,almhirt.CollisionProblem
                                                  //| Refused to handle command: Versions do not match. Current version is '1', t
                                                  //| argetted version is '4'. The refused command is 'ChangeTestPersonName(6a2ee
                                                  //| 45f-5718-4f00-bb13-9a3d8f89ec8a,Some(4),newName40)'
                                                  //| Category: ApplicationProblem
                                                  //| Severity: Minor
                                                  //| Arguments: Map()
                                                  //| )
                                                  //| NotExecuted(A updatename41,almhirt.CollisionProblem
                                                  //| Refused to handle command: Versions do not match. Current version is '1', t
                                                  //| argetted version is '4'. The refused command is 'ChangeTestPersonName(c60dc
                                                  //| 789-5421-4a71-8912-f48fb6505cb1,Some(4),newName41)'
                                                  //| Category: ApplicationProblem
                                                  //| Severity: Minor
                                                  //| Arguments: Map()
                                                  //| )
                                                  //| NotExecuted(A updatename42,almhirt.CollisionProblem
                                                  //| Refused to handle command: Versions do not match. Current version is '1', t
                                                  //| argetted version is '4'. The refused command is 'ChangeTestPersonName(3e59b
                                                  //| 0c7-fb2b-43fa-9e9b-1fac2b30682a,Some(4),newName42)'
                                                  //| Category: ApplicationProblem
                                                  //| Severity: Minor
                                                  //| Arguments: Map()
                                                  //| )
                                                  //| NotExecuted(A updatename43,almhirt.CollisionProblem
                                                  //| Refused to handle command: Versions do not match. Current version is '1', t
                                                  //| argetted version is '4'. The refused command is 'ChangeTestPersonName(c9990
                                                  //| e34-2551-4bfb-82da-ad53e55cc97f,Some(4),newName43)'
                                                  //| Category: ApplicationProblem
                                                  //| Severity: Minor
                                                  //| Arguments: Map()
                                                  //| )
                                                  //| NotExecuted(A updatename44,almhirt.CollisionProblem
                                                  //| Refused to handle command: Versions do not match. Current version is '1', t
                                                  //| argetted version is '4'. The refused command is 'ChangeTestPersonName(f26c1
                                                  //| bcc-570a-4d8b-8c1f-d54b13d3faaa,Some(4),newName44)'
                                                  //| Category: ApplicationProblem
                                                  //| Severity: Minor
                                                  //| Arguments: Map()
                                                  //| )
                                                  //| NotExecuted(A updatename45,almhirt.CollisionProblem
                                                  //| Refused to handle command: Versions do not match. Current version is '1', t
                                                  //| argetted version is '4'. The refused command is 'ChangeTestPersonName(27afa
                                                  //| 35e-cadb-457d-9e9d-7c58adf9f8cf,Some(4),newName45)'
                                                  //| Category: ApplicationProblem
                                                  //| Severity: Minor
                                                  //| Arguments: Map()
                                                  //| )
                                                  //| NotExecuted(A updatename46,almhirt.CollisionProblem
                                                  //| Refused to handle command: Versions do not match. Current version is '1', t
                                                  //| argetted version is '4'. The refused command is 'ChangeTestPersonName(70ea8
                                                  //| 249-a9cf-47a8-b2c3-5ef63562099e,Some(4),newName46)'
                                                  //| Category: ApplicationProblem
                                                  //| Severity: Minor
                                                  //| Arguments: Map()
                                                  //| )
                                                  //| NotExecuted(A updatename47,almhirt.CollisionProblem
                                                  //| Refused to handle command: Versions do not match. Current version is '1', t
                                                  //| argetted version is '4'. The refused command is 'ChangeTestPersonName(19840
                                                  //| 80c-171a-4fd7-93cd-8ac45eeeae59,Some(4),newName47)'
                                                  //| Category: ApplicationProblem
                                                  //| Severity: Minor
                                                  //| Arguments: Map()
                                                  //| )
                                                  //| NotExecuted(A updatename48,almhirt.CollisionProblem
                                                  //| Refused to handle command: Versions do not match. Current version is '1', t
                                                  //| argetted version is '4'. The refused command is 'ChangeTestPersonName(fd53c
                                                  //| 1cb-1e0a-4b73-98d0-a37c98b5c71d,Some(4),newName48)'
                                                  //| Category: ApplicationProblem
                                                  //| Severity: Minor
                                                  //| Arguments: Map()
                                                  //| )
                                                  //| NotExecuted(A updatename49,almhirt.CollisionProblem
                                                  //| Refused to handle command: Versions do not match. Current version is '1', t
                                                  //| argetted version is '4'. The refused command is 'ChangeTestPersonName(4ef90
                                                  //| 308-70e6-4da2-a570-a2a3dc50fa52,Some(4),newName49)'
                                                  //| Category: ApplicationProblem
                                                  //| Severity: Minor
                                                  //| Arguments: Map()
                                                  //| )
                                                  //| res0: Boolean = false
  }