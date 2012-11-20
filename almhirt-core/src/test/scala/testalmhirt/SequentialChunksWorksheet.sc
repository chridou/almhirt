package worksheets

import akka.dispatch.{ Await, Future }
import almhirt.core._
import almhirt.commanding.AggregateRootRef
import almhirt.almvalidation.kit._
import almhirt.environment._
import almhirt.almfuture.inst._
import test._

object SequentialChunksWorksheet extends TestAlmhirtKit {
  implicit val atMost = akka.util.Duration(500, "ms")
                                                  //> atMost  : akka.util.FiniteDuration = 500 milliseconds
  inTestAlmhirt { almhirt =>
    implicit val executor = almhirt.environment.context.system.futureDispatcher
    val idsAndNamesAndAdresses = Vector((for (i <- 0 until 50) yield (i, almhirt.getUuid, "Name%s".format(i), "Address%s".format(i))): _*)

    val repo = almhirt.environment.repositories.getForAggregateRoot[TestPerson, TestPersonEvent].awaitResult.forceResult

    idsAndNamesAndAdresses.foreach(x => almhirt.executeTrackedCommand(NewTestPerson(x._2, x._3), "A insert%s".format(x._1.toString)))
    val insertStatesFutures = idsAndNamesAndAdresses.map(x => almhirt.getResultOperationStateFor("A insert%s".format(x._1.toString)))
    val insertStatesRes = AlmFuture.sequence(insertStatesFutures).awaitResult
    println("--- insert done ---")
    if (insertStatesRes.isFailure) println(insertStatesRes)
//    insertStatesRes.foreach { insertStates =>
//      insertStates.foreach(x => x fold (f => println(f), succ => println(succ)))
//    }

    idsAndNamesAndAdresses.foreach(x => almhirt.executeTrackedCommand(SetTestPersonAddress(AggregateRootRef(x._2, 1), x._4), "A setaddress%s".format(x._1.toString)))
    val update1StatesFutures = idsAndNamesAndAdresses.map(x => almhirt.getResultOperationStateFor("A setaddress%s".format(x._1.toString)))
    val update1StatesRes = AlmFuture.sequence(update1StatesFutures).awaitResult
    println("--- setaddress done ---")
    if (update1StatesRes.isFailure) println(update1StatesRes)
//    update1StatesRes.foreach { updateStates =>
//      updateStates.foreach(x => x fold (f => println(f), succ => println(succ)))
//    }

    idsAndNamesAndAdresses.foreach(x => almhirt.executeTrackedCommand(MoveBecauseOfMarriage(AggregateRootRef(x._2, 2), "namemarriage%s".format(x._3), "addressmarriage%s".format(x._3)), "A updatemarriage%s".format(x._1.toString)))
    val update2StatesFutures = idsAndNamesAndAdresses.map(x => almhirt.getResultOperationStateFor("A updatemarriage%s".format(x._1.toString)))
    val update2StatesRes = AlmFuture.sequence(update2StatesFutures).awaitResult
    println("--- updatemarriage done ---")
    if (update2StatesRes.isFailure) println(update2StatesRes)
//    update2StatesRes.foreach { updateStates =>
//      updateStates.foreach(x => x fold (f => println(f), succ => println(succ)))
//    }

    idsAndNamesAndAdresses.foreach(x => almhirt.executeTrackedCommand(ChangeTestPersonName(AggregateRootRef(x._2, 4), "new%s".format(x._3)), "A updatename%s".format(x._1.toString)))
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
                                                  //| --- setaddress done ---
                                                  //| --- updatemarriage done ---
                                                  //| --- updatename done ---
                                                  //| Executed(StringTrackingTicket(A updatename0))
                                                  //| Executed(StringTrackingTicket(A updatename1))
                                                  //| Executed(StringTrackingTicket(A updatename2))
                                                  //| Executed(StringTrackingTicket(A updatename3))
                                                  //| Executed(StringTrackingTicket(A updatename4))
                                                  //| Executed(StringTrackingTicket(A updatename5))
                                                  //| Executed(StringTrackingTicket(A updatename6))
                                                  //| Executed(StringTrackingTicket(A updatename7))
                                                  //| Executed(StringTrackingTicket(A updatename8))
                                                  //| Executed(StringTrackingTicket(A updatename9))
                                                  //| Executed(StringTrackingTicket(A updatename10))
                                                  //| Executed(StringTrackingTicket(A updatename11))
                                                  //| Executed(StringTrackingTicket(A updatename12))
                                                  //| Executed(StringTrackingTicket(A updatename13))
                                                  //| Executed(StringTrackingTicket(A updatename14))
                                                  //| Executed(StringTrackingTicket(A updatename15))
                                                  //| Executed(StringTrackingTicket(A updatename16))
                                                  //| Executed(StringTrackingTicket(A updatename17))
                                                  //| Executed(StringTrackingTicket(A updatename18))
                                                  //| Executed(StringTrackingTicket(A updatename19))
                                                  //| Executed(StringTrackingTicket(A updatename20))
                                                  //| Executed(StringTrackingTicket(A updatename21))
                                                  //| Executed(StringTrackingTicket(A updatename22))
                                                  //| Executed(StringTrackingTicket(A updatename23))
                                                  //| Executed(StringTrackingTicket(A updatename24))
                                                  //| Executed(StringTrackingTicket(A updatename25))
                                                  //| Executed(StringTrackingTicket(A updatename26))
                                                  //| Executed(StringTrackingTicket(A updatename27))
                                                  //| Executed(StringTrackingTicket(A updatename28))
                                                  //| Executed(StringTrackingTicket(A updatename29))
                                                  //| Executed(StringTrackingTicket(A updatename30))
                                                  //| Executed(StringTrackingTicket(A updatename31))
                                                  //| Executed(StringTrackingTicket(A updatename32))
                                                  //| Executed(StringTrackingTicket(A updatename33))
                                                  //| Executed(StringTrackingTicket(A updatename34))
                                                  //| Executed(StringTrackingTicket(A updatename35))
                                                  //| Executed(StringTrackingTicket(A updatename36))
                                                  //| Executed(StringTrackingTicket(A updatename37))
                                                  //| Executed(StringTrackingTicket(A updatename38))
                                                  //| Executed(StringTrackingTicket(A updatename39))
                                                  //| Executed(StringTrackingTicket(A updatename40))
                                                  //| Executed(StringTrackingTicket(A updatename41))
                                                  //| Executed(StringTrackingTicket(A updatename42))
                                                  //| Executed(StringTrackingTicket(A updatename43))
                                                  //| Executed(StringTrackingTicket(A updatename44))
                                                  //| Executed(StringTrackingTicket(A updatename45))
                                                  //| Executed(StringTrackingTicket(A updatename46))
                                                  //| Executed(StringTrackingTicket(A updatename47))
                                                  //| Executed(StringTrackingTicket(A updatename48))
                                                  //| Executed(StringTrackingTicket(A updatename49))
                                                  //| res0: Boolean = true

}