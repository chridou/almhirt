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
  }                                               //> Executed(A insert0)
                                                  //| Executed(A insert1)
                                                  //| Executed(A insert2)
                                                  //| Executed(A insert3)
                                                  //| NotExecuted(A setaddress0,almhirt.UnspecifiedProblem
                                                  //| The first event's version must be equal to the target aggregate root's vers
                                                  //| ion: 1 != 0
                                                  //| Category: ApplicationProblem
                                                  //| Severity: Minor
                                                  //| Arguments: Map()
                                                  //| )
                                                  //| NotExecuted(A setaddress1,almhirt.UnspecifiedProblem
                                                  //| The first event's version must be equal to the target aggregate root's vers
                                                  //| ion: 1 != 0
                                                  //| Category: ApplicationProblem
                                                  //| Severity: Minor
                                                  //| Arguments: Map()
                                                  //| )
                                                  //| NotExecuted(A setaddress2,almhirt.UnspecifiedProblem
                                                  //| The first event's version must be equal to the target aggregate root's vers
                                                  //| ion: 1 != 0
                                                  //| Category: ApplicationProblem
                                                  //| Severity: Minor
                                                  //| Arguments: Map()
                                                  //| )
                                                  //| NotExecuted(A setaddress3,almhirt.UnspecifiedProblem
                                                  //| The first event's version must be equal to the target aggregate root's vers
                                                  //| ion: 1 != 0
                                                  //| Category: ApplicationProblem
                                                  //| Severity: Minor
                                                  //| Arguments: Map()
                                                  //| )
                                                  //| NotExecuted(A updatename0,almhirt.CollisionProblem
                                                  //| Refused to handle command: Versions do not match. Current version is '1', t
                                                  //| argetted version is '2'. The refused command is 'ChangeTestPersonName(5adb8
                                                  //| 769-d8d6-4a8e-b20a-7d5560166cf8,Some(2),newName0)'
                                                  //| Category: ApplicationProblem
                                                  //| Severity: Minor
                                                  //| Arguments: Map()
                                                  //| )
                                                  //| NotExecuted(A updatename1,almhirt.CollisionProblem
                                                  //| Refused to handle command: Versions do not match. Current version is '1', t
                                                  //| argetted version is '2'. The refused command is 'ChangeTestPersonName(e163b
                                                  //| 65a-2be8-4978-9bd9-bfd1d6b009f5,Some(2),newName1)'
                                                  //| Category: ApplicationProblem
                                                  //| Severity: Minor
                                                  //| Arguments: Map()
                                                  //| )
                                                  //| NotExecuted(A updatename2,almhirt.CollisionProblem
                                                  //| Refused to handle command: Versions do not match. Current version is '1', t
                                                  //| argetted version is '2'. The refused command is 'ChangeTestPersonName(ed156
                                                  //| 912-a650-4315-af3f-d24122064f4d,Some(2),newName2)'
                                                  //| Category: ApplicationProblem
                                                  //| Severity: Minor
                                                  //| Arguments: Map()
                                                  //| )
                                                  //| NotExecuted(A updatename3,almhirt.CollisionProblem
                                                  //| Refused to handle command: Versions do not match. Current version is '1', t
                                                  //| argetted version is '2'. The refused command is 'ChangeTestPersonName(36cad
                                                  //| 592-cf17-4924-99ad-3fd24314a302,Some(2),newName3)'
                                                  //| Category: ApplicationProblem
                                                  //| Severity: Minor
                                                  //| Arguments: Map()
                                                  //| )
                                                  //| res0: Boolean = false

}