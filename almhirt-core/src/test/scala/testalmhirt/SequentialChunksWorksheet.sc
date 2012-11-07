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
                                                  //| Cannot cast scala.None$ to scalaz.Validation
                                                  //| Category: SystemProblem
                                                  //| Severity: Major
                                                  //| Arguments: Map()
                                                  //| Exception: java.lang.ClassCastException: Cannot cast scala.None$ to scalaz.
                                                  //| Validation
                                                  //| Stacktrace:
                                                  //| java.lang.Class.cast(Unknown Source)
                                                  //| akka.dispatch.Future$$anonfun$mapTo$1.liftedTree4$1(Future.scala:649)
                                                  //| akka.dispatch.Future$$anonfun$mapTo$1.apply(Future.scala:648)
                                                  //| akka.dispatch.Future$$anonfun$mapTo$1.apply(Future.scala:645)
                                                  //| akka.dispatch.DefaultPromise.akka$dispatch$DefaultPromise$$notifyCompleted(
                                                  //| Future.scala:943)
                                                  //| akka.dispatch.DefaultPromise$$anonfun$tryComplete$1$$anonfun$apply$mcV$sp$4
                                                  //| .apply(Future.scala:920)
                                                  //| akka.dispatch.DefaultPromise$$anonfun$tryComplete$1$$anonfun$apply$mcV$sp$4
                                                  //| .apply(Future.scala:920)
                                                  //| scala.collection.LinearSeqOptimized$class.foreach(LinearSeqOptimized.scala:
                                                  //| 59)
                                                  //| scala.collection.immutable.Lis
                                                  //| Output exceeds cutoff limit.

}