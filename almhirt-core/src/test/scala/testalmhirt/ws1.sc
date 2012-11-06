package worksheets

import akka.dispatch.{Await, Future}
import almhirt._
import almhirt.almvalidation.kit._
import almhirt.environment._
import test._
import almhirt.almfuture.inst._


object ws1 extends TestAlmhirtKit {
  implicit val atMost = akka.util.Duration(2, "s")//> atMost  : akka.util.FiniteDuration = 2 seconds
  println("Welcome to the Scala worksheet")       //> Welcome to the Scala worksheet

      inTestAlmhirt{almhirt =>
        implicit val executor = almhirt.environment.context.system.futureDispatcher
        val idsAndNamesAndAdresses = Vector((for(i <- 0 to 29) yield (i, almhirt.getUuid, "Name%s".format(i), "Address%s".format(i))): _*)
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
                                                  //| NotExecuted(A updatename0,almhirt.CollisionProblem
                                                  //| versions do not match. Current version is '1', target version is '2'
                                                  //| Category: ApplicationProblem
                                                  //| Severity: Minor
                                                  //| Arguments: Map()
                                                  //| )
                                                  //| NotExecuted(A updatename1,almhirt.CollisionProblem
                                                  //| versions do not match. Current version is '1', target version is '2'
                                                  //| Category: ApplicationProblem
                                                  //| Severity: Minor
                                                  //| Arguments: Map()
                                                  //| )
                                                  //| NotExecuted(A updatename2,almhirt.CollisionProblem
                                                  //| versions do not match. Current version is '1', target version is '2'
                                                  //| Category: ApplicationProblem
                                                  //| Severity: Minor
                                                  //| Arguments: Map()
                                                  //| )
                                                  //| NotExecuted(A updatename3,almhirt.CollisionProblem
                                                  //| versions do not match. Current version is '1', target version is '2'
                                                  //| Category: ApplicationProblem
                                                  //| Severity: Minor
                                                  //| Arguments: Map()
                                                  //| )
                                                  //| NotExecuted(A updatename4,almhirt.CollisionProblem
                                                  //| versions do not match. Current version is '1', target version is '2'
                                                  //| Category: ApplicationProblem
                                                  //| Severity: Minor
                                                  //| Arguments: Map()
                                                  //| )
                                                  //| NotExecuted(A updatename5,almhirt.CollisionProblem
                                                  //| versions do not match. Current version is '1', target version is '2'
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
                                                  //| NotExecuted(A updatename10,almhirt.CollisionProblem
                                                  //| versions do not match. Current version is '1', target version is '2'
                                                  //| Category: ApplicationProblem
                                                  //| Severity: Minor
                                                  //| Arguments: Map()
                                                  //| )
                                                  //| NotExecuted(A updatename11,almhirt.CollisionProblem
                                                  //| versions do not match. Current version is '1', target version is '2'
                                                  //| Category: ApplicationProblem
                                                  //| Severity: Minor
                                                  //| Arguments: Map()
                                                  //| )
                                                  //| NotExecuted(A updatename12,almhirt.CollisionProblem
                                                  //| versions do not match. Current version is '1', target version is '2'
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
                                                  //| Executed(A updatename18)
                                                  //| NotExecuted(A updatename19,almhirt.CollisionProblem
                                                  //| versions do not match. Current version is '1', target version is '2'
                                                  //| Category: ApplicationProblem
                                                  //| Severity: Minor
                                                  //| Arguments: Map()
                                                  //| )
                                                  //| NotExecuted(A updatename20,almhirt.CollisionProblem
                                                  //| versions do not match. Current version is '1', target version is '2'
                                                  //| Category: ApplicationProblem
                                                  //| Severity: Minor
                                                  //| Arguments: Map()
                                                  //| )
                                                  //| NotExecuted(A updatename21,almhirt.CollisionProblem
                                                  //| versions do not match. Current version is '1', target version is '2'
                                                  //| Category: ApplicationProblem
                                                  //| Severity: Minor
                                                  //| Arguments: Map()
                                                  //| )
                                                  //| Executed(A updatename22)
                                                  //| Executed(A updatename23)
                                                  //| NotExecuted(A updatename24,almhirt.CollisionProblem
                                                  //| versions do not match. Current version is '1', target version is '2'
                                                  //| Category: ApplicationProblem
                                                  //| Severity: Minor
                                                  //| Arguments: Map()
                                                  //| )
                                                  //| Executed(A updatename25)
                                                  //| Executed(A updatename26)
                                                  //| Executed(A updatename27)
                                                  //| Executed(A updatename28)
                                                  //| Executed(A updatename29)
                                                  //| res0: Boolean = false


}