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
//    update1StatesRes.foreach { updateStates =>
//      updateStates.foreach(x => x fold (f => println(f), succ => println(succ)))
//    }

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
  }                                               //> --- insert done ---
                                                  //| Executed(StringTrackingTicket(A insert0))
                                                  //| Executed(StringTrackingTicket(A insert1))
                                                  //| Executed(StringTrackingTicket(A insert2))
                                                  //| Executed(StringTrackingTicket(A insert3))
                                                  //| Executed(StringTrackingTicket(A insert4))
                                                  //| Executed(StringTrackingTicket(A insert5))
                                                  //| Executed(StringTrackingTicket(A insert6))
                                                  //| Executed(StringTrackingTicket(A insert7))
                                                  //| Executed(StringTrackingTicket(A insert8))
                                                  //| Executed(StringTrackingTicket(A insert9))
                                                  //| Executed(StringTrackingTicket(A insert10))
                                                  //| Executed(StringTrackingTicket(A insert11))
                                                  //| Executed(StringTrackingTicket(A insert12))
                                                  //| Executed(StringTrackingTicket(A insert13))
                                                  //| Executed(StringTrackingTicket(A insert14))
                                                  //| Executed(StringTrackingTicket(A insert15))
                                                  //| Executed(StringTrackingTicket(A insert16))
                                                  //| Executed(StringTrackingTicket(A insert17))
                                                  //| Executed(StringTrackingTicket(A insert18))
                                                  //| Executed(StringTrackingTicket(A insert19))
                                                  //| Executed(StringTrackingTicket(A insert20))
                                                  //| Executed(StringTrackingTicket(A insert21))
                                                  //| Executed(StringTrackingTicket(A insert22))
                                                  //| Executed(StringTrackingTicket(A insert23))
                                                  //| Executed(StringTrackingTicket(A insert24))
                                                  //| Executed(StringTrackingTicket(A insert25))
                                                  //| Executed(StringTrackingTicket(A insert26))
                                                  //| Executed(StringTrackingTicket(A insert27))
                                                  //| Executed(StringTrackingTicket(A insert28))
                                                  //| Executed(StringTrackingTicket(A insert29))
                                                  //| Executed(StringTrackingTicket(A insert30))
                                                  //| Executed(StringTrackingTicket(A insert31))
                                                  //| Executed(StringTrackingTicket(A insert32))
                                                  //| Executed(StringTrackingTicket(A insert33))
                                                  //| Executed(StringTrackingTicket(A insert34))
                                                  //| Executed(StringTrackingTicket(A insert35))
                                                  //| Executed(StringTrackingTicket(A insert36))
                                                  //| Executed(StringTrackingTicket(A insert37))
                                                  //| Executed(StringTrackingTicket(A insert38))
                                                  //| Executed(StringTrackingTicket(A insert39))
                                                  //| Executed(StringTrackingTicket(A insert40))
                                                  //| Executed(StringTrackingTicket(A insert41))
                                                  //| Executed(StringTrackingTicket(A insert42))
                                                  //| Executed(StringTrackingTicket(A insert43))
                                                  //| Executed(StringTrackingTicket(A insert44))
                                                  //| Executed(StringTrackingTicket(A insert45))
                                                  //| Executed(StringTrackingTicket(A insert46))
                                                  //| Executed(StringTrackingTicket(A insert47))
                                                  //| Executed(StringTrackingTicket(A insert48))
                                                  //| Executed(StringTrackingTicket(A insert49))
                                                  //| --- setaddress done ---
                                                  //| --- updatemarriage done ---
                                                  //| --- updatename done ---
                                                  //| NotExecuted(StringTrackingTicket(A updatename0),almhirt.CollisionProblem
                                                  //| Refused to handle command: Versions do not match. Current version is '1', t
                                                  //| argetted version is '4'. The refused command is 'ChangeTestPersonName(26368
                                                  //| a01-581c-4951-b616-8f9df8dd4329,Some(4),newName0)'
                                                  //| Category: ApplicationProblem
                                                  //| Severity: Minor
                                                  //| Arguments: Map()
                                                  //| )
                                                  //| NotExecuted(StringTrackingTicket(A updatename1),almhirt.CollisionProblem
                                                  //| Refused to handle command: Versions do not match. Current version is '1', t
                                                  //| argetted version is '4'. The refused command is 'ChangeTestPersonName(6b792
                                                  //| 439-c7db-4c77-9346-6eb127c94606,Some(4),newName1)'
                                                  //| Category: ApplicationProblem
                                                  //| Severity: Minor
                                                  //| Arguments: Map()
                                                  //| )
                                                  //| NotExecuted(StringTrackingTicket(A updatename2),almhirt.NotFoundProblem
                                                  //| No aggregate root found with id '2415e879-5ebc-49a9-9288-bddd27d9ee3e'
                                                  //| Category: ApplicationProblem
                                                  //| Severity: Minor
                                                  //| Arguments: Map()
                                                  //| )
                                                  //| NotExecuted(StringTrackingTicket(A updatename3),almhirt.CollisionProblem
                                                  //| Refused to handle command: Versions do not match. Current version is '1', t
                                                  //| argetted version is '4'. The refused command is 'ChangeTestPersonName(7586f
                                                  //| f56-5b9d-432a-98c9-63057b36512f,Some(4),newName3)'
                                                  //| Category: ApplicationProblem
                                                  //| Severity: Minor
                                                  //| Arguments: Map()
                                                  //| )
                                                  //| NotExecuted(StringTrackingTicket(A updatename4),almhirt.CollisionProblem
                                                  //| Refused to handle command: Versions do not match. Current version is '1', t
                                                  //| argetted version is '4'. The refused command is 'ChangeTestPersonName(de290
                                                  //| 551-9ffd-4a4d-93ec-e087c74bd3f4,Some(4),newName4)'
                                                  //| Category: ApplicationProblem
                                                  //| Severity: Minor
                                                  //| Arguments: Map()
                                                  //| )
                                                  //| NotExecuted(StringTrackingTicket(A updatename5),almhirt.NotFoundProblem
                                                  //| No aggregate root found with id '08b689ec-f6c6-4645-a25e-81d4b8ffdfee'
                                                  //| Category: ApplicationProblem
                                                  //| Severity: Minor
                                                  //| Arguments: Map()
                                                  //| )
                                                  //| NotExecuted(StringTrackingTicket(A updatename6),almhirt.CollisionProblem
                                                  //| Refused to handle command: Versions do not match. Current version is '1', t
                                                  //| argetted version is '4'. The refused command is 'ChangeTestPersonName(e89f1
                                                  //| d8d-56cf-499e-8b39-46dfa019805d,Some(4),newName6)'
                                                  //| Category: ApplicationProblem
                                                  //| Severity: Minor
                                                  //| Arguments: Map()
                                                  //| )
                                                  //| NotExecuted(StringTrackingTicket(A updatename7),almhirt.CollisionProblem
                                                  //| Refused to handle command: Versions do not match. Current version is '1', t
                                                  //| argetted version is '4'. The refused command is 'ChangeTestPersonName(d8ea6
                                                  //| 8aa-6599-4b85-a768-2701ad61d092,Some(4),newName7)'
                                                  //| Category: ApplicationProblem
                                                  //| Severity: Minor
                                                  //| Arguments: Map()
                                                  //| )
                                                  //| NotExecuted(StringTrackingTicket(A updatename8),almhirt.NotFoundProblem
                                                  //| No aggregate root found with id 'cde0a833-2e74-4c90-937d-5cf5b2544496'
                                                  //| Category: ApplicationProblem
                                                  //| Severity: Minor
                                                  //| Arguments: Map()
                                                  //| )
                                                  //| NotExecuted(StringTrackingTicket(A updatename9),almhirt.CollisionProblem
                                                  //| Refused to handle command: Versions do not match. Current version is '1', t
                                                  //| argetted version is '4'. The refused command is 'ChangeTestPersonName(7000c
                                                  //| 515-4903-4593-b9fd-da3d1c51491b,Some(4),newName9)'
                                                  //| Category: ApplicationProblem
                                                  //| Severity: Minor
                                                  //| Arguments: Map()
                                                  //| )
                                                  //| NotExecuted(StringTrackingTicket(A updatename10),almhirt.CollisionProblem
                                                  //| Refused to handle command: Versions do not match. Current version is '1', t
                                                  //| argetted version is '4'. The refused command is 'ChangeTestPersonName(b95e8
                                                  //| 9db-9ea5-4dbd-881b-d203871e0241,Some(4),newName10)'
                                                  //| Category: ApplicationProblem
                                                  //| Severity: Minor
                                                  //| Arguments: Map()
                                                  //| )
                                                  //| NotExecuted(StringTrackingTicket(A updatename11),almhirt.NotFoundProblem
                                                  //| No aggregate root found with id 'a271fd28-2f51-473c-80cc-b5dfbf62855c'
                                                  //| Category: ApplicationProblem
                                                  //| Severity: Minor
                                                  //| Arguments: Map()
                                                  //| )
                                                  //| NotExecuted(StringTrackingTicket(A updatename12),almhirt.NotFoundProblem
                                                  //| No aggregate root found with id '447d6888-2807-4f13-81df-b42bd7500068'
                                                  //| Category: ApplicationProblem
                                                  //| Severity: Minor
                                                  //| Arguments: Map()
                                                  //| )
                                                  //| NotExecuted(StringTrackingTicket(A updatename13),almhirt.CollisionProblem
                                                  //| Refused to handle command: Versions do not match. Current version is '1', t
                                                  //| argetted version is '4'. The refused command is 'ChangeTestPersonName(5ad30
                                                  //| 484-0017-4d3a-a63d-8f4d655db69c,Some(4),newName13)'
                                                  //| Category: ApplicationProblem
                                                  //| Severity: Minor
                                                  //| Arguments: Map()
                                                  //| )
                                                  //| NotExecuted(StringTrackingTicket(A updatename14),almhirt.CollisionProblem
                                                  //| Refused to handle command: Versions do not match. Current version is '1', t
                                                  //| argetted version is '4'. The refused command is 'ChangeTestPersonName(d09a8
                                                  //| f7e-36ee-4267-80d5-d0d0d7ec5744,Some(4),newName14)'
                                                  //| Category: ApplicationProblem
                                                  //| Severity: Minor
                                                  //| Arguments: Map()
                                                  //| )
                                                  //| NotExecuted(StringTrackingTicket(A updatename15),almhirt.CollisionProblem
                                                  //| Refused to handle command: Versions do not match. Current version is '1', t
                                                  //| argetted version is '4'. The refused command is 'ChangeTestPersonName(5bc9e
                                                  //| a8b-6c72-4212-b9df-f7c89c18c733,Some(4),newName15)'
                                                  //| Category: ApplicationProblem
                                                  //| Severity: Minor
                                                  //| Arguments: Map()
                                                  //| )
                                                  //| NotExecuted(StringTrackingTicket(A updatename16),almhirt.NotFoundProblem
                                                  //| No aggregate root found with id 'fda9eb55-fba0-4dd3-8a6c-73af6f6bee08'
                                                  //| Category: ApplicationProblem
                                                  //| Severity: Minor
                                                  //| Arguments: Map()
                                                  //| )
                                                  //| NotExecuted(StringTrackingTicket(A updatename17),almhirt.NotFoundProblem
                                                  //| No aggregate root found with id '89f8bf4f-ca4d-47f9-9c44-9e7cac9e92df'
                                                  //| Category: ApplicationProblem
                                                  //| Severity: Minor
                                                  //| Arguments: Map()
                                                  //| )
                                                  //| NotExecuted(StringTrackingTicket(A updatename18),almhirt.NotFoundProblem
                                                  //| No aggregate root found with id 'f83764fc-fa39-44e8-9c10-438d4aa2ad00'
                                                  //| Category: ApplicationProblem
                                                  //| Severity: Minor
                                                  //| Arguments: Map()
                                                  //| )
                                                  //| NotExecuted(StringTrackingTicket(A updatename19),almhirt.CollisionProblem
                                                  //| Refused to handle command: Versions do not match. Current version is '1', t
                                                  //| argetted version is '4'. The refused command is 'ChangeTestPersonName(f46dd
                                                  //| 71a-7b6e-4f2b-a0df-1c98206bd916,Some(4),newName19)'
                                                  //| Category: ApplicationProblem
                                                  //| Severity: Minor
                                                  //| Arguments: Map()
                                                  //| )
                                                  //| NotExecuted(StringTrackingTicket(A updatename20),almhirt.CollisionProblem
                                                  //| Refused to handle command: Versions do not match. Current version is '1', t
                                                  //| argetted version is '4'. The refused command is 'ChangeTestPersonName(aa35c
                                                  //| db8-6745-4066-8f9a-e07cefe1076a,Some(4),newName20)'
                                                  //| Category: ApplicationProblem
                                                  //| Severity: Minor
                                                  //| Arguments: Map()
                                                  //| )
                                                  //| NotExecuted(StringTrackingTicket(A updatename21),almhirt.CollisionProblem
                                                  //| Refused to handle command: Versions do not match. Current version is '1', t
                                                  //| argetted version is '4'. The refused command is 'ChangeTestPersonName(bbbb6
                                                  //| af0-b452-497a-b20a-4cdc73751c2e,Some(4),newName21)'
                                                  //| Category: ApplicationProblem
                                                  //| Severity: Minor
                                                  //| Arguments: Map()
                                                  //| )
                                                  //| NotExecuted(StringTrackingTicket(A updatename22),almhirt.CollisionProblem
                                                  //| Refused to handle command: Versions do not match. Current version is '1', t
                                                  //| argetted version is '4'. The refused command is 'ChangeTestPersonName(7a26e
                                                  //| 457-ea54-4abe-be94-48d5e1e8f096,Some(4),newName22)'
                                                  //| Category: ApplicationProblem
                                                  //| Severity: Minor
                                                  //| Arguments: Map()
                                                  //| )
                                                  //| NotExecuted(StringTrackingTicket(A updatename23),almhirt.NotFoundProblem
                                                  //| No aggregate root found with id '631bf2ab-9744-4010-ab3b-8fb3a04ee678'
                                                  //| Category: ApplicationProblem
                                                  //| Severity: Minor
                                                  //| Arguments: Map()
                                                  //| )
                                                  //| NotExecuted(StringTrackingTicket(A updatename24),almhirt.NotFoundProblem
                                                  //| No aggregate root found with id '449f658a-afcd-4d3f-98c4-50a488ad2a91'
                                                  //| Category: ApplicationProblem
                                                  //| Severity: Minor
                                                  //| Arguments: Map()
                                                  //| )
                                                  //| NotExecuted(StringTrackingTicket(A updatename25),almhirt.CollisionProblem
                                                  //| Refused to handle command: Versions do not match. Current version is '1', t
                                                  //| argetted version is '4'. The refused command is 'ChangeTestPersonName(580ee
                                                  //| dc5-ff54-43c1-af59-e74f3ba69094,Some(4),newName25)'
                                                  //| Category: ApplicationProblem
                                                  //| Severity: Minor
                                                  //| Arguments: Map()
                                                  //| )
                                                  //| NotExecuted(StringTrackingTicket(A updatename26),almhirt.CollisionProblem
                                                  //| Refused to handle command: Versions do not match. Current version is '1', t
                                                  //| argetted version is '4'. The refused command is 'ChangeTestPersonName(22156
                                                  //| afa-6318-4783-8ef3-542bcc58d4f3,Some(4),newName26)'
                                                  //| Category: ApplicationProblem
                                                  //| Severity: Minor
                                                  //| Arguments: Map()
                                                  //| )
                                                  //| NotExecuted(StringTrackingTicket(A updatename27),almhirt.CollisionProblem
                                                  //| Refused to handle command: Versions do not match. Current version is '1', t
                                                  //| argetted version is '4'. The refused command is 'ChangeTestPersonName(68025
                                                  //| 91f-0bcd-471c-ac3b-5343ee649b6c,Some(4),newName27)'
                                                  //| Category: ApplicationProblem
                                                  //| Severity: Minor
                                                  //| Arguments: Map()
                                                  //| )
                                                  //| NotExecuted(StringTrackingTicket(A updatename28),almhirt.CollisionProblem
                                                  //| Refused to handle command: Versions do not match. Current version is '1', t
                                                  //| argetted version is '4'. The refused command is 'ChangeTestPersonName(512c8
                                                  //| 1fd-33a5-475f-81c2-5bf6468b6975,Some(4),newName28)'
                                                  //| Category: ApplicationProblem
                                                  //| Severity: Minor
                                                  //| Arguments: Map()
                                                  //| )
                                                  //| NotExecuted(StringTrackingTicket(A updatename29),almhirt.CollisionProblem
                                                  //| Refused to handle command: Versions do not match. Current version is '1', t
                                                  //| argetted version is '4'. The refused command is 'ChangeTestPersonName(8b29c
                                                  //| 4d6-4445-4e8e-bb75-f47f7531824a,Some(4),newName29)'
                                                  //| Category: ApplicationProblem
                                                  //| Severity: Minor
                                                  //| Arguments: Map()
                                                  //| )
                                                  //| NotExecuted(StringTrackingTicket(A updatename30),almhirt.CollisionProblem
                                                  //| Refused to handle command: Versions do not match. Current version is '1', t
                                                  //| argetted version is '4'. The refused command is 'ChangeTestPersonName(0165f
                                                  //| e1c-c6d1-4716-b554-350a8b7d1415,Some(4),newName30)'
                                                  //| Category: ApplicationProblem
                                                  //| Severity: Minor
                                                  //| Arguments: Map()
                                                  //| )
                                                  //| NotExecuted(StringTrackingTicket(A updatename31),almhirt.CollisionProblem
                                                  //| Refused to handle command: Versions do not match. Current version is '1', t
                                                  //| argetted version is '4'. The refused command is 'ChangeTestPersonName(0c2f5
                                                  //| 306-9b4c-4ea8-a06c-c858e0b98224,Some(4),newName31)'
                                                  //| Category: ApplicationProblem
                                                  //| Severity: Minor
                                                  //| Arguments: Map()
                                                  //| )
                                                  //| NotExecuted(StringTrackingTicket(A updatename32),almhirt.CollisionProblem
                                                  //| Refused to handle command: Versions do not match. Current version is '1', t
                                                  //| argetted version is '4'. The refused command is 'ChangeTestPersonName(f0b09
                                                  //| 390-d115-49b9-afad-1e5c29f7aa60,Some(4),newName32)'
                                                  //| Category: ApplicationProblem
                                                  //| Severity: Minor
                                                  //| Arguments: Map()
                                                  //| )
                                                  //| NotExecuted(StringTrackingTicket(A updatename33),almhirt.CollisionProblem
                                                  //| Refused to handle command: Versions do not match. Current version is '1', t
                                                  //| argetted version is '4'. The refused command is 'ChangeTestPersonName(c5495
                                                  //| 0e1-aed9-4bb1-9cce-7b75143f8619,Some(4),newName33)'
                                                  //| Category: ApplicationProblem
                                                  //| Severity: Minor
                                                  //| Arguments: Map()
                                                  //| )
                                                  //| NotExecuted(StringTrackingTicket(A updatename34),almhirt.CollisionProblem
                                                  //| Refused to handle command: Versions do not match. Current version is '1', t
                                                  //| argetted version is '4'. The refused command is 'ChangeTestPersonName(35c98
                                                  //| 25c-cbd5-45fa-aa20-c33a516daa3e,Some(4),newName34)'
                                                  //| Category: ApplicationProblem
                                                  //| Severity: Minor
                                                  //| Arguments: Map()
                                                  //| )
                                                  //| NotExecuted(StringTrackingTicket(A updatename35),almhirt.CollisionProblem
                                                  //| Refused to handle command: Versions do not match. Current version is '1', t
                                                  //| argetted version is '4'. The refused command is 'ChangeTestPersonName(4a065
                                                  //| d0f-56eb-465b-867f-21caa121f450,Some(4),newName35)'
                                                  //| Category: ApplicationProblem
                                                  //| Severity: Minor
                                                  //| Arguments: Map()
                                                  //| )
                                                  //| NotExecuted(StringTrackingTicket(A updatename36),almhirt.CollisionProblem
                                                  //| Refused to handle command: Versions do not match. Current version is '1', t
                                                  //| argetted version is '4'. The refused command is 'ChangeTestPersonName(5215a
                                                  //| eb1-0dfe-49e4-a4b6-3fe454e71a5e,Some(4),newName36)'
                                                  //| Category: ApplicationProblem
                                                  //| Severity: Minor
                                                  //| Arguments: Map()
                                                  //| )
                                                  //| NotExecuted(StringTrackingTicket(A updatename37),almhirt.NotFoundProblem
                                                  //| No aggregate root found with id 'af1f9ea4-e866-4cfd-a461-49ed43648def'
                                                  //| Category: ApplicationProblem
                                                  //| Severity: Minor
                                                  //| Arguments: Map()
                                                  //| )
                                                  //| NotExecuted(StringTrackingTicket(A updatename38),almhirt.CollisionProblem
                                                  //| Refused to handle command: Versions do not match. Current version is '1', t
                                                  //| argetted version is '4'. The refused command is 'ChangeTestPersonName(fcc06
                                                  //| 7f7-7d0b-4a8c-8a03-25e2fbf9744c,Some(4),newName38)'
                                                  //| Category: ApplicationProblem
                                                  //| Severity: Minor
                                                  //| Arguments: Map()
                                                  //| )
                                                  //| NotExecuted(StringTrackingTicket(A updatename39),almhirt.CollisionProblem
                                                  //| Refused to handle command: Versions do not match. Current version is '1', t
                                                  //| argetted version is '4'. The refused command is 'ChangeTestPersonName(291d8
                                                  //| 60d-7df7-414c-a316-80f28dedd835,Some(4),newName39)'
                                                  //| Category: ApplicationProblem
                                                  //| Severity: Minor
                                                  //| Arguments: Map()
                                                  //| )
                                                  //| NotExecuted(StringTrackingTicket(A updatename40),almhirt.CollisionProblem
                                                  //| Refused to handle command: Versions do not match. Current version is '1', t
                                                  //| argetted version is '4'. The refused command is 'ChangeTestPersonName(c6116
                                                  //| 925-5b48-49ed-8924-8d082900d364,Some(4),newName40)'
                                                  //| Category: ApplicationProblem
                                                  //| Severity: Minor
                                                  //| Arguments: Map()
                                                  //| )
                                                  //| NotExecuted(StringTrackingTicket(A updatename41),almhirt.NotFoundProblem
                                                  //| No aggregate root found with id '9d1d0dd0-8e71-45cc-9c8f-fcb58eed6a71'
                                                  //| Category: ApplicationProblem
                                                  //| Severity: Minor
                                                  //| Arguments: Map()
                                                  //| )
                                                  //| NotExecuted(StringTrackingTicket(A updatename42),almhirt.CollisionProblem
                                                  //| Refused to handle command: Versions do not match. Current version is '1', t
                                                  //| argetted version is '4'. The refused command is 'ChangeTestPersonName(ae123
                                                  //| bdb-a30c-4370-b964-c7c84d15766c,Some(4),newName42)'
                                                  //| Category: ApplicationProblem
                                                  //| Severity: Minor
                                                  //| Arguments: Map()
                                                  //| )
                                                  //| NotExecuted(StringTrackingTicket(A updatename43),almhirt.CollisionProblem
                                                  //| Refused to handle command: Versions do not match. Current version is '1', t
                                                  //| argetted version is '4'. The refused command is 'ChangeTestPersonName(4456f
                                                  //| 2b3-2324-4a15-be72-886b90a0dc9d,Some(4),newName43)'
                                                  //| Category: ApplicationProblem
                                                  //| Severity: Minor
                                                  //| Arguments: Map()
                                                  //| )
                                                  //| NotExecuted(StringTrackingTicket(A updatename44),almhirt.CollisionProblem
                                                  //| Refused to handle command: Versions do not match. Current version is '1', t
                                                  //| argetted version is '4'. The refused command is 'ChangeTestPersonName(c40f9
                                                  //| c79-4c40-4799-bf68-a0b0ca71f9c6,Some(4),newName44)'
                                                  //| Category: ApplicationProblem
                                                  //| Severity: Minor
                                                  //| Arguments: Map()
                                                  //| )
                                                  //| NotExecuted(StringTrackingTicket(A updatename45),almhirt.NotFoundProblem
                                                  //| No aggregate root found with id 'de74e0bb-b415-41aa-901a-4b26b75fa452'
                                                  //| Category: ApplicationProblem
                                                  //| Severity: Minor
                                                  //| Arguments: Map()
                                                  //| )
                                                  //| NotExecuted(StringTrackingTicket(A updatename46),almhirt.CollisionProblem
                                                  //| Refused to handle command: Versions do not match. Current version is '1', t
                                                  //| argetted version is '4'. The refused command is 'ChangeTestPersonName(987e3
                                                  //| 770-090a-4816-8145-a995cf165f9c,Some(4),newName46)'
                                                  //| Category: ApplicationProblem
                                                  //| Severity: Minor
                                                  //| Arguments: Map()
                                                  //| )
                                                  //| NotExecuted(StringTrackingTicket(A updatename47),almhirt.NotFoundProblem
                                                  //| No aggregate root found with id '6bdf17ea-f7c7-4786-a7db-362e9b87bbd9'
                                                  //| Category: ApplicationProblem
                                                  //| Severity: Minor
                                                  //| Arguments: Map()
                                                  //| )
                                                  //| NotExecuted(StringTrackingTicket(A updatename48),almhirt.CollisionProblem
                                                  //| Refused to handle command: Versions do not match. Current version is '1', t
                                                  //| argetted version is '4'. The refused command is 'ChangeTestPersonName(7ac6d
                                                  //| 01a-1fc9-4087-9e0f-a2ac03b37dd7,Some(4),newName48)'
                                                  //| Category: ApplicationProblem
                                                  //| Severity: Minor
                                                  //| Arguments: Map()
                                                  //| )
                                                  //| NotExecuted(StringTrackingTicket(A updatename49),almhirt.CollisionProblem
                                                  //| Refused to handle command: Versions do not match. Current version is '1', t
                                                  //| argetted version is '4'. The refused command is 'ChangeTestPersonName(8e044
                                                  //| 45c-9890-4c3d-a618-cc67cf1e4b7f,Some(4),newName49)'
                                                  //| Category: ApplicationProblem
                                                  //| Severity: Minor
                                                  //| Arguments: Map()
                                                  //| )
                                                  //| res0: Boolean = false
  }