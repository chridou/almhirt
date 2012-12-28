package testalmhirt

import scala.concurrent.duration.Duration
import almhirt._
import almhirt.almvalidation.kit._
import almhirt.environment._
import test._
import almhirt.almfuture.inst._
import almhirt.common.AlmFuture
import almhirt.commanding._
import org.specs2.mutable._

class TestAlmhirtMassSpecs extends Specification with TestAlmhirtKit {
  private implicit val atMost = Duration(2, "s")
  "The TestAlmhirt" should {
    "create, modify and retrieve 100 persons when actions for all entities are processed as sequenced blocks (A)" in {
      inTestAlmhirt{almhirt =>
    implicit val executor = almhirt.executionContext
    val idsAndNamesAndAdresses = Vector((for (i <- 0 to 0) yield (i, almhirt.getUuid, "Name%s".format(i), "Address%s".format(i))): _*)

    idsAndNamesAndAdresses.foreach(x => almhirt.executeTrackedCommand(NewTestPerson(x._2, x._3), "A insert%s".format(x._1.toString)))
    val insertStatesFutures = idsAndNamesAndAdresses.map(x => almhirt.operationStateTracker.getResultFor("A insert%s".format(x._1.toString)))
    val insertStatesRes = AlmFuture.sequence(insertStatesFutures).awaitResult
    if(insertStatesRes.isFailure) println(insertStatesRes)
    idsAndNamesAndAdresses.foreach(x => almhirt.executeTrackedCommand(SetTestPersonAddress(AggregateRootRef(x._2, 1), x._4), "A setaddress%s".format(x._1.toString)))
    val update1StatesFutures = idsAndNamesAndAdresses.map(x => almhirt.operationStateTracker.getResultFor("A setaddress%s".format(x._1.toString)))
    val update1StatesRes = AlmFuture.sequence(update1StatesFutures).awaitResult
    if(update1StatesRes.isFailure) println(update1StatesRes)

    idsAndNamesAndAdresses.foreach(x => almhirt.executeTrackedCommand(ChangeTestPersonName(AggregateRootRef(x._2, 2), "new%s".format(x._3)), "A updatename%s".format(x._1.toString)))
    val update2StatesFutures = idsAndNamesAndAdresses.map(x => almhirt.operationStateTracker.getResultFor("A updatename%s".format(x._1.toString)))
    val update2StatesRes = AlmFuture.sequence(update2StatesFutures).awaitResult
    if(update2StatesRes.isFailure) println(update2StatesRes)
    update2StatesRes.map { updateStates =>
      updateStates.foreach(x => x fold (f => println(f), succ => println(succ)))
      updateStates
    }.fold(
      f => false,
      succ => succ.forall(_.isSuccess) && succ.forall(_.forceResult.isFinishedSuccesfully))
      }
    }
  }
}