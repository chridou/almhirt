package worksheets

import akka.dispatch.{Await, Future}
import almhirt._
import almhirt.almvalidation.kit._
import almhirt.environment._
import test._
import almhirt.almfuture.inst._


object ws1 extends TestAlmhirtKit {
  implicit val atMost = akka.util.Duration(5, "s")//> atMost  : akka.util.FiniteDuration = 5 seconds
  println("Welcome to the Scala worksheet")       //> Welcome to the Scala worksheet

      inTestAlmhirt{almhirt =>
        implicit val executor = almhirt.environment.context.system.futureDispatcher
        val idsAndNamesAndAdresses = Vector((for(i <- 0 to 	19) yield (i, almhirt.getUuid, "Name%s".format(i), "Address%s".format(i))): _*)
        idsAndNamesAndAdresses.foreach(x => almhirt.executeTrackedCommand(NewTestPerson(x._2, x._3), "A insert%s".format(x._1.toString)))
        idsAndNamesAndAdresses.foreach(x => almhirt.executeTrackedCommand(SetTestPersonAddress(x._2, Some(1), x._4), "A setaddress%s".format(x._1.toString)))
        idsAndNamesAndAdresses.foreach(x => almhirt.executeTrackedCommand(ChangeTestPersonName(x._2, Some(2), "new%s".format(x._3)), "A updatename%s".format(x._1.toString)))
        val repo = almhirt.getRepository[TestPerson, TestPersonEvent].awaitResult.forceResult
        
        val insertStatesFutures = idsAndNamesAndAdresses.map(x => almhirt.getResultOperationStateFor("A insert%s".format(x._1.toString)))
        val insertStatesRes = AlmFuture.sequence(insertStatesFutures).awaitResult(akka.util.Duration(1, "s"))
        insertStatesRes.forceResult.foreach(x => x fold(f => println(f), succ => println(succ)))

        val update1StatesFutures = idsAndNamesAndAdresses.map(x => almhirt.getResultOperationStateFor("A setaddress%s".format(x._1.toString)))
        val update1StatesRes = AlmFuture.sequence(update1StatesFutures).awaitResult(akka.util.Duration(1, "s"))
        update1StatesRes.forceResult.foreach(x => x fold(f => println(f), succ => println(succ)))

        val update2StatesFutures = idsAndNamesAndAdresses.map(x => almhirt.getResultOperationStateFor("A updatename%s".format(x._1.toString)))
        val update2StatesRes = AlmFuture.sequence(update2StatesFutures).awaitResult(akka.util.Duration(1, "s"))
        val finalRes = update2StatesRes.forceResult
        finalRes.foreach(x => x fold(f => println(f), succ => println(succ)))
        
        finalRes.forall(_.isSuccess) && finalRes.forall(_.forceResult.isFinishedSuccesfully)
      }                                           //> Executed(A insert0)
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
                                                  //| NotExecuted(A setaddress0,almhirt.NotFoundProblem
                                                  //| No aggregate root found with id 'da8a638e-612c-4061-81a7-e6d070c90667'
                                                  //| Category: ApplicationProblem
                                                  //| Severity: Minor
                                                  //| Arguments: Map()
                                                  //| )
                                                  //| NotExecuted(A setaddress1,almhirt.NotFoundProblem
                                                  //| No aggregate root found with id 'fd162e2d-b69a-41d2-977a-b580eaae4d0e'
                                                  //| Category: ApplicationProblem
                                                  //| Severity: Minor
                                                  //| Arguments: Map()
                                                  //| )
                                                  //| NotExecuted(A setaddress2,almhirt.NotFoundProblem
                                                  //| No aggregate root found with id 'e3c16e95-d723-439e-b986-b0a281486671'
                                                  //| Category: ApplicationProblem
                                                  //| Severity: Minor
                                                  //| Arguments: Map()
                                                  //| )
                                                  //| NotExecuted(A setaddress3,almhirt.NotFoundProblem
                                                  //| No aggregate root found with id 'aa8c6a93-ca51-4af9-88f2-5f2d24843569'
                                                  //| Category: ApplicationProblem
                                                  //| Severity: Minor
                                                  //| Arguments: Map()
                                                  //| )
                                                  //| NotExecuted(A setaddress4,almhirt.NotFoundProblem
                                                  //| No aggregate root found with id 'ddb7b1aa-6ca8-4d4b-8ef4-c8ff976be909'
                                                  //| Category: ApplicationProblem
                                                  //| Severity: Minor
                                                  //| Arguments: Map()
                                                  //| )
                                                  //| NotExecuted(A setaddress5,almhirt.NotFoundProblem
                                                  //| No aggregate root found with id 'dda7f25f-9aa8-4702-b82a-8a2f2df60f7d'
                                                  //| Category: ApplicationProblem
                                                  //| Severity: Minor
                                                  //| Arguments: Map()
                                                  //| )
                                                  //| NotExecuted(A setaddress6,almhirt.NotFoundProblem
                                                  //| No aggregate root found with id 'e0970485-6722-40db-a283-a537b2b2f5d8'
                                                  //| Category: ApplicationProblem
                                                  //| Severity: Minor
                                                  //| Arguments: Map()
                                                  //| )
                                                  //| NotExecuted(A setaddress7,almhirt.NotFoundProblem
                                                  //| No aggregate root found with id '1e6f0dcf-f6e0-4791-b4e6-4b24e14ad31d'
                                                  //| Category: ApplicationProblem
                                                  //| Severity: Minor
                                                  //| Arguments: Map()
                                                  //| )
                                                  //| NotExecuted(A setaddress8,almhirt.NotFoundProblem
                                                  //| No aggregate root found with id '49c77d86-245b-4b73-9a93-9909e73899f1'
                                                  //| Category: ApplicationProblem
                                                  //| Severity: Minor
                                                  //| Arguments: Map()
                                                  //| )
                                                  //| NotExecuted(A setaddress9,almhirt.NotFoundProblem
                                                  //| No aggregate root found with id '963090cd-c81b-4ccd-9b5d-a6e8f9b017d1'
                                                  //| Category: ApplicationProblem
                                                  //| Severity: Minor
                                                  //| Arguments: Map()
                                                  //| )
                                                  //| NotExecuted(A setaddress10,almhirt.NotFoundProblem
                                                  //| No aggregate root found with id '4132c96a-17e8-456d-a966-8f4a291f07da'
                                                  //| Category: ApplicationProblem
                                                  //| Severity: Minor
                                                  //| Arguments: Map()
                                                  //| )
                                                  //| NotExecuted(A setaddress11,almhirt.NotFoundProblem
                                                  //| No aggregate root found with id '0b088eb1-e865-42a4-b020-12315238db19'
                                                  //| Category: ApplicationProblem
                                                  //| Severity: Minor
                                                  //| Arguments: Map()
                                                  //| )
                                                  //| NotExecuted(A setaddress12,almhirt.NotFoundProblem
                                                  //| No aggregate root found with id '34f97785-24fc-42cf-8768-149862d7987a'
                                                  //| Category: ApplicationProblem
                                                  //| Severity: Minor
                                                  //| Arguments: Map()
                                                  //| )
                                                  //| NotExecuted(A setaddress13,almhirt.NotFoundProblem
                                                  //| No aggregate root found with id '8cb55686-8008-496d-b264-93bb7021cbca'
                                                  //| Category: ApplicationProblem
                                                  //| Severity: Minor
                                                  //| Arguments: Map()
                                                  //| )
                                                  //| NotExecuted(A setaddress14,almhirt.NotFoundProblem
                                                  //| No aggregate root found with id '96055fdc-ee9f-43f3-8942-85e536a1e6b7'
                                                  //| Category: ApplicationProblem
                                                  //| Severity: Minor
                                                  //| Arguments: Map()
                                                  //| )
                                                  //| NotExecuted(A setaddress15,almhirt.NotFoundProblem
                                                  //| No aggregate root found with id '515b9083-3277-4032-9cb3-e903bd7c454a'
                                                  //| Category: ApplicationProblem
                                                  //| Severity: Minor
                                                  //| Arguments: Map()
                                                  //| )
                                                  //| NotExecuted(A setaddress16,almhirt.NotFoundProblem
                                                  //| No aggregate root found with id '1490d7a7-10d2-425c-a025-03e6cba2ae96'
                                                  //| Category: ApplicationProblem
                                                  //| Severity: Minor
                                                  //| Arguments: Map()
                                                  //| )
                                                  //| NotExecuted(A setaddress17,almhirt.NotFoundProblem
                                                  //| No aggregate root found with id '1041b9b8-0d44-4d32-bbe7-32835488b7a5'
                                                  //| Category: ApplicationProblem
                                                  //| Severity: Minor
                                                  //| Arguments: Map()
                                                  //| )
                                                  //| NotExecuted(A setaddress18,almhirt.NotFoundProblem
                                                  //| No aggregate root found with id '7b39a297-7f6f-4f91-b813-a54333da8efe'
                                                  //| Category: ApplicationProblem
                                                  //| Severity: Minor
                                                  //| Arguments: Map()
                                                  //| )
                                                  //| NotExecuted(A setaddress19,almhirt.NotFoundProblem
                                                  //| No aggregate root found with id '0ab89933-e900-4ce2-8f6e-fa623f4f5f31'
                                                  //| Category: ApplicationProblem
                                                  //| Severity: Minor
                                                  //| Arguments: Map()
                                                  //| )
                                                  //| NotExecuted(A updatename0,almhirt.NotFoundProblem
                                                  //| No aggregate root found with id 'da8a638e-612c-4061-81a7-e6d070c90667'
                                                  //| Category: ApplicationProblem
                                                  //| Severity: Minor
                                                  //| Arguments: Map()
                                                  //| )
                                                  //| NotExecuted(A updatename1,almhirt.NotFoundProblem
                                                  //| No aggregate root found with id 'fd162e2d-b69a-41d2-977a-b580eaae4d0e'
                                                  //| Category: ApplicationProblem
                                                  //| Severity: Minor
                                                  //| Arguments: Map()
                                                  //| )
                                                  //| NotExecuted(A updatename2,almhirt.NotFoundProblem
                                                  //| No aggregate root found with id 'e3c16e95-d723-439e-b986-b0a281486671'
                                                  //| Category: ApplicationProblem
                                                  //| Severity: Minor
                                                  //| Arguments: Map()
                                                  //| )
                                                  //| NotExecuted(A updatename3,almhirt.NotFoundProblem
                                                  //| No aggregate root found with id 'aa8c6a93-ca51-4af9-88f2-5f2d24843569'
                                                  //| Category: ApplicationProblem
                                                  //| Severity: Minor
                                                  //| Arguments: Map()
                                                  //| )
                                                  //| NotExecuted(A updatename4,almhirt.NotFoundProblem
                                                  //| No aggregate root found with id 'ddb7b1aa-6ca8-4d4b-8ef4-c8ff976be909'
                                                  //| Category: ApplicationProblem
                                                  //| Severity: Minor
                                                  //| Arguments: Map()
                                                  //| )
                                                  //| NotExecuted(A updatename5,almhirt.NotFoundProblem
                                                  //| No aggregate root found with id 'dda7f25f-9aa8-4702-b82a-8a2f2df60f7d'
                                                  //| Category: ApplicationProblem
                                                  //| Severity: Minor
                                                  //| Arguments: Map()
                                                  //| )
                                                  //| NotExecuted(A updatename6,almhirt.CollisionProblem
                                                  //| versions do not match. Current version is '1', target version is '2'
                                                  //| Category: ApplicationProblem
                                                  //| Severity: Minor
                                                  //| Arguments: Map()
                                                  //| )
                                                  //| NotExecuted(A updatename7,almhirt.CollisionProblem
                                                  //| versions do not match. Current version is '1', target version is '2'
                                                  //| Category: ApplicationProblem
                                                  //| Severity: Minor
                                                  //| Arguments: Map()
                                                  //| )
                                                  //| NotExecuted(A updatename8,almhirt.CollisionProblem
                                                  //| versions do not match. Current version is '1', target version is '2'
                                                  //| Category: ApplicationProblem
                                                  //| Severity: Minor
                                                  //| Arguments: Map()
                                                  //| )
                                                  //| NotExecuted(A updatename9,almhirt.CollisionProblem
                                                  //| versions do not match. Current version is '1', target version is '2'
                                                  //| Category: ApplicationProblem
                                                  //| Severity: Minor
                                                  //| Arguments: Map()
                                                  //| )
                                                  //| NotExecuted(A updatename10,almhirt.NotFoundProblem
                                                  //| No aggregate root found with id '4132c96a-17e8-456d-a966-8f4a291f07da'
                                                  //| Category: ApplicationProblem
                                                  //| Severity: Minor
                                                  //| Arguments: Map()
                                                  //| )
                                                  //| NotExecuted(A updatename11,almhirt.NotFoundProblem
                                                  //| No aggregate root found with id '0b088eb1-e865-42a4-b020-12315238db19'
                                                  //| Category: ApplicationProblem
                                                  //| Severity: Minor
                                                  //| Arguments: Map()
                                                  //| )
                                                  //| NotExecuted(A updatename12,almhirt.NotFoundProblem
                                                  //| No aggregate root found with id '34f97785-24fc-42cf-8768-149862d7987a'
                                                  //| Category: ApplicationProblem
                                                  //| Severity: Minor
                                                  //| Arguments: Map()
                                                  //| )
                                                  //| NotExecuted(A updatename13,almhirt.CollisionProblem
                                                  //| versions do not match. Current version is '1', target version is '2'
                                                  //| Category: ApplicationProblem
                                                  //| Severity: Minor
                                                  //| Arguments: Map()
                                                  //| )
                                                  //| NotExecuted(A updatename14,almhirt.CollisionProblem
                                                  //| versions do not match. Current version is '1', target version is '2'
                                                  //| Category: ApplicationProblem
                                                  //| Severity: Minor
                                                  //| Arguments: Map()
                                                  //| )
                                                  //| NotExecuted(A updatename15,almhirt.CollisionProblem
                                                  //| versions do not match. Current version is '1', target version is '2'
                                                  //| Category: ApplicationProblem
                                                  //| Severity: Minor
                                                  //| Arguments: Map()
                                                  //| )
                                                  //| NotExecuted(A updatename16,almhirt.CollisionProblem
                                                  //| versions do not match. Current version is '1', target version is '2'
                                                  //| Category: ApplicationProblem
                                                  //| Severity: Minor
                                                  //| Arguments: Map()
                                                  //| )
                                                  //| NotExecuted(A updatename17,almhirt.CollisionProblem
                                                  //| versions do not match. Current version is '1', target version is '2'
                                                  //| Category: ApplicationProblem
                                                  //| Severity: Minor
                                                  //| Arguments: Map()
                                                  //| )
                                                  //| NotExecuted(A updatename18,almhirt.NotFoundProblem
                                                  //| No aggregate root found with id '7b39a297-7f6f-4f91-b813-a54333da8efe'
                                                  //| Category: ApplicationProblem
                                                  //| Severity: Minor
                                                  //| Arguments: Map()
                                                  //| )
                                                  //| NotExecuted(A updatename19,almhirt.CollisionProblem
                                                  //| versions do not match. Current version is '1', target version is '2'
                                                  //| Category: ApplicationProblem
                                                  //| Severity: Minor
                                                  //| Arguments: Map()
                                                  //| )
                                                  //| res0: Boolean = false


}