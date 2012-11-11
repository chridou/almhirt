package worksheets

import akka.dispatch.{ Await, Future }
import almhirt._
import almhirt.almvalidation.kit._
import almhirt.environment._
import test._
import almhirt.almfuture.inst._

object SequentialChunksWorksheet extends TestAlmhirtKit {
  implicit val atMost = akka.util.Duration(500, "ms")
                                                  //> atMost  : akka.util.FiniteDuration = 500 milliseconds
  inTestAlmhirt { almhirt =>
    implicit val executor = almhirt.environment.context.system.futureDispatcher
    val idsAndNamesAndAdresses = Vector((for (i <- 0 until 1) yield (i, almhirt.getUuid, "Name%s".format(i), "Address%s".format(i))): _*)

    val repo = almhirt.environment.repositories.getForAggregateRoot[TestPerson, TestPersonEvent].awaitResult.forceResult

    idsAndNamesAndAdresses.foreach(x => almhirt.executeTrackedCommand(NewTestPerson(x._2, x._3), "A insert%s".format(x._1.toString)))
    val insertStatesFutures = idsAndNamesAndAdresses.map(x => almhirt.getResultOperationStateFor("A insert%s".format(x._1.toString)))
    val insertStatesRes = AlmFuture.sequence(insertStatesFutures).awaitResult
    println("--- insert done ---")
    if (insertStatesRes.isFailure) println(insertStatesRes)
    insertStatesRes.foreach { insertStates =>
      insertStates.foreach(x => x fold (f => println(f), succ => println(succ)))
    }

    idsAndNamesAndAdresses.foreach(x => almhirt.executeTrackedCommand(SetTestPersonAddress(x._2, Some(1), x._4), "A setaddress%s".format(x._1.toString)))
    val update1StatesFutures = idsAndNamesAndAdresses.map(x => almhirt.getResultOperationStateFor("A setaddress%s".format(x._1.toString)))
    val update1StatesRes = AlmFuture.sequence(update1StatesFutures).awaitResult
    println("--- setaddress done ---")
    if (update1StatesRes.isFailure) println(update1StatesRes)
    update1StatesRes.foreach { updateStates =>
      updateStates.foreach(x => x fold (f => println(f), succ => println(succ)))
    }

    idsAndNamesAndAdresses.foreach(x => almhirt.executeTrackedCommand(MoveBecauseOfMarriage(x._2, Some(2), "namemarriage%s".format(x._3), "addressmarriage%s".format(x._3)), "A updatemarriage%s".format(x._1.toString)))
    val update2StatesFutures = idsAndNamesAndAdresses.map(x => almhirt.getResultOperationStateFor("A updatemarriage%s".format(x._1.toString)))
    val update2StatesRes = AlmFuture.sequence(update2StatesFutures).awaitResult
    println("--- updatemarriage done ---")
    if (update2StatesRes.isFailure) println(update2StatesRes)
    update2StatesRes.foreach { updateStates =>
      updateStates.foreach(x => x fold (f => println(f), succ => println(succ)))
    }


    idsAndNamesAndAdresses.foreach(x => almhirt.executeTrackedCommand(ChangeTestPersonName(x._2, Some(4), "new%s".format(x._3)), "A updatename%s".format(x._1.toString)))
    val update3StatesFutures = idsAndNamesAndAdresses.map(x => almhirt.getResultOperationStateFor("A updatename%s".format(x._1.toString)))
    val update3StatesRes = AlmFuture.sequence(update3StatesFutures).awaitResult
    println("--- updatename done ---")
    if (update3StatesRes.isFailure) println(update3StatesRes)

    //    almhirt.environment.eventLog.getAllEvents.awaitResult.forceResult.foreach(println)
    //    AlmFuture.sequence(idsAndNamesAndAdresses.map(x => repo.get(x._2))).awaitResult.forceResult.foreach(println)

    update3StatesRes.map { updateStates =>
      updateStates.foreach(x => x fold (f => println(f), succ => println(succ)))
      updateStates
    }.fold(
      f => false,
      succ => succ.forall(_.isSuccess) && succ.forall(_.forceResult.isFinishedSuccesfully))
  }                                               //> --- insert done ---
                                                  //| Failure(almhirt.OperationTimedOutProblem
                                                  //| A future operation timed out: Futures timed out after [500] milliseconds
                                                  //| Category: SystemProblem
                                                  //| Severity: Major
                                                  //| Arguments: Map()
                                                  //| Exception: java.util.concurrent.TimeoutException: Futures timed out after [
                                                  //| 500] milliseconds
                                                  //| Stacktrace:
                                                  //| akka.dispatch.DefaultPromise.ready(Future.scala:870)
                                                  //| akka.dispatch.DefaultPromise.result(Future.scala:874)
                                                  //| akka.dispatch.Await$.result(Future.scala:74)
                                                  //| almhirt.AlmFuture.awaitResult(AlmFuture.scala:97)
                                                  //| worksheets.SequentialChunksWorksheet$$anonfun$main$1$$anonfun$1.apply(works
                                                  //| heets.SequentialChunksWorksheet.scala:20)
                                                  //| worksheets.SequentialChunksWorksheet$$anonfun$main$1$$anonfun$1.apply(works
                                                  //| heets.SequentialChunksWorksheet.scala:12)
                                                  //| test.TestAlmhirtKit$class.inTestAlmhirt(TestAlmhirtKit.scala:26)
                                                  //| worksheets.SequentialChunksWorksheet$.inTestAlmhirt(worksheets.SequentialCh
                                                  //| unksWorksheet.scala:10)

}