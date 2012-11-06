package worksheets

import akka.dispatch.{ Await, Future }
import almhirt._
import almhirt.almvalidation.kit._
import almhirt.environment._
import test._
import almhirt.almfuture.inst._

object SequentialChunksWorksheet extends TestAlmhirtKit {
  implicit val atMost = akka.util.Duration(3, "s")//> atMost  : akka.util.FiniteDuration = 3 seconds
  inTestAlmhirt { almhirt =>
    implicit val executor = almhirt.environment.context.system.futureDispatcher
    val idsAndNamesAndAdresses = Vector((for (i <- 0 to 3) yield (i, almhirt.getUuid, "Name%s".format(i), "Address%s".format(i))): _*)

    idsAndNamesAndAdresses.foreach(x => almhirt.executeTrackedCommand(NewTestPerson(x._2, x._3), "A insert%s".format(x._1.toString)))
    val insertStatesFutures = idsAndNamesAndAdresses.map(x => almhirt.getResultOperationStateFor("A insert%s".format(x._1.toString)))
    val insertStatesRes = AlmFuture.sequence(insertStatesFutures).awaitResult
    if(insertStatesRes.isFailure) println(insertStatesRes)
    insertStatesRes.foreach { insertStates =>
      insertStates.foreach(x => x fold (f => println(f), succ => println(succ)))
    }


    idsAndNamesAndAdresses.foreach(x => almhirt.executeTrackedCommand(SetTestPersonAddress(x._2, Some(1), x._4), "A setaddress%s".format(x._1.toString)))
    val update1StatesFutures = idsAndNamesAndAdresses.map(x => almhirt.getResultOperationStateFor("A setaddress%s".format(x._1.toString)))
    val update1StatesRes = AlmFuture.sequence(update1StatesFutures).awaitResult
    if(update1StatesRes.isFailure) println(update1StatesRes)
    update1StatesRes.foreach { updateStates =>
      updateStates.foreach(x => x fold (f => println(f), succ => println(succ)))
    }


    idsAndNamesAndAdresses.foreach(x => almhirt.executeTrackedCommand(ChangeTestPersonName(x._2, Some(2), "new%s".format(x._3)), "A updatename%s".format(x._1.toString)))
    val update2StatesFutures = idsAndNamesAndAdresses.map(x => almhirt.getResultOperationStateFor("A updatename%s".format(x._1.toString)))
    val update2StatesRes = AlmFuture.sequence(update2StatesFutures).awaitResult
    if(update2StatesRes.isFailure) println(update2StatesRes)
    update2StatesRes.map { updateStates =>
      updateStates.foreach(x => x fold (f => println(f), succ => println(succ)))
      updateStates
    }.fold(
      f => false,
      succ => succ.forall(_.isSuccess) && succ.forall(_.forceResult.isFinishedSuccesfully))
  }                                               //> NotExecuted(A insert0,almhirt.UnspecifiedProblem
                                                  //| The last event's version must be one less that the aggregate root's version
                                                  //| : 1 + 1 != 1
                                                  //| Category: ApplicationProblem
                                                  //| Severity: Minor
                                                  //| Arguments: Map()
                                                  //| )
                                                  //| NotExecuted(A insert1,almhirt.UnspecifiedProblem
                                                  //| The last event's version must be one less that the aggregate root's version
                                                  //| : 1 + 1 != 1
                                                  //| Category: ApplicationProblem
                                                  //| Severity: Minor
                                                  //| Arguments: Map()
                                                  //| )
                                                  //| NotExecuted(A insert2,almhirt.UnspecifiedProblem
                                                  //| The last event's version must be one less that the aggregate root's version
                                                  //| : 1 + 1 != 1
                                                  //| Category: ApplicationProblem
                                                  //| Severity: Minor
                                                  //| Arguments: Map()
                                                  //| )
                                                  //| NotExecuted(A insert3,almhirt.UnspecifiedProblem
                                                  //| The last event's version must be one less that the aggregate root's version
                                                  //| : 1 + 1 != 1
                                                  //| Category: ApplicationProblem
                                                  //| Severity: Minor
                                                  //| Arguments: Map()
                                                  //| )
                                                  //| NotExecuted(A setaddress0,almhirt.NotFoundProblem
                                                  //| No aggregate root found with id '009e0143-62b3-4dd4-98da-3b0ac661ac79'
                                                  //| Category: ApplicationProblem
                                                  //| Severity: Minor
                                                  //| Arguments: Map()
                                                  //| )
                                                  //| NotExecuted(A setaddress1,almhirt.NotFoundProblem
                                                  //| No aggregate root found with id 'beb9dcd2-c9cf-46c3-924e-849c687309d8'
                                                  //| Category: ApplicationProblem
                                                  //| Severity: Minor
                                                  //| Arguments: Map()
                                                  //| )
                                                  //| NotExecuted(A setaddress2,almhirt.NotFoundProblem
                                                  //| No aggregate root found with id 'b13c9724-e64b-4e98-9fe5-4dc26148f5ca'
                                                  //| Category: ApplicationProblem
                                                  //| Severity: Minor
                                                  //| Arguments: Map()
                                                  //| )
                                                  //| NotExecuted(A setaddress3,almhirt.NotFoundProblem
                                                  //| No aggregate root found with id 'b1827bac-e16b-4f7a-ac9c-342f1783a884'
                                                  //| Category: ApplicationProblem
                                                  //| Severity: Minor
                                                  //| Arguments: Map()
                                                  //| )
                                                  //| NotExecuted(A updatename0,almhirt.NotFoundProblem
                                                  //| No aggregate root found with id '009e0143-62b3-4dd4-98da-3b0ac661ac79'
                                                  //| Category: ApplicationProblem
                                                  //| Severity: Minor
                                                  //| Arguments: Map()
                                                  //| )
                                                  //| NotExecuted(A updatename1,almhirt.NotFoundProblem
                                                  //| No aggregate root found with id 'beb9dcd2-c9cf-46c3-924e-849c687309d8'
                                                  //| Category: ApplicationProblem
                                                  //| Severity: Minor
                                                  //| Arguments: Map()
                                                  //| )
                                                  //| NotExecuted(A updatename2,almhirt.NotFoundProblem
                                                  //| No aggregate root found with id 'b13c9724-e64b-4e98-9fe5-4dc26148f5ca'
                                                  //| Category: ApplicationProblem
                                                  //| Severity: Minor
                                                  //| Arguments: Map()
                                                  //| )
                                                  //| NotExecuted(A updatename3,almhirt.NotFoundProblem
                                                  //| No aggregate root found with id 'b1827bac-e16b-4f7a-ac9c-342f1783a884'
                                                  //| Category: ApplicationProblem
                                                  //| Severity: Minor
                                                  //| Arguments: Map()
                                                  //| )
                                                  //| res0: Boolean = false

}