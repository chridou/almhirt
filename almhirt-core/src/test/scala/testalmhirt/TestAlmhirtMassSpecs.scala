package testalmhirt

import scala.concurrent.duration.Duration
import almhirt._
import almhirt.almvalidation.kit._
import almhirt.environment._
import almhirt.core.test._
import almhirt.almfuture.inst._
import almhirt.common.AlmFuture
import almhirt.domain._
import almhirt.commanding._
import org.scalatest._

class TestAlmhirtMassSpecs extends FlatSpec with AlmhirtTestKit {
  private implicit val atMost = Duration(5, "s")
  "The TestAlmhirt" should
    "create, modify and retrieve 100 persons when actions for all entities are processed as sequenced blocks (A)" in {
      inExtendedTestAlmhirt(new BlockingRepoCoreBootstrapper(this.defaultConf)) { implicit almhirt =>

        val getResultFor = almhirt.operationStateTracker.getResultFor(atMost)_

        val idsAndNamesAndAdresses = Vector((for (i <- 1 to 100) yield (i, almhirt.getUuid, "Name%s".format(i), "Address%s".format(i))): _*)

        idsAndNamesAndAdresses.foreach(x => almhirt.executeTrackedCommand(TestPersonCommand.creator(NewTestPersonAction(x._2, x._3)), "A insert%s".format(x._1.toString)))
        val insertStatesFutures = idsAndNamesAndAdresses.map(x => getResultFor("A insert%s".format(x._1.toString)))
        val insertStatesRes = AlmFuture.sequence(insertStatesFutures).awaitResult
        if (insertStatesRes.isFailure) println("TestAlmhirtMassSpecs(INSERT):" + insertStatesRes)
        idsAndNamesAndAdresses.foreach(x => almhirt.executeTrackedCommand(TestPersonCommand.mutator(AggregateRootRef(x._2, 1), SetTestPersonAddressAction(x._4)), "A setaddress%s".format(x._1.toString)))
        val update1StatesFutures = idsAndNamesAndAdresses.map(x => getResultFor("A setaddress%s".format(x._1.toString)))
        val update1StatesRes = AlmFuture.sequence(update1StatesFutures).awaitResult
        if (update1StatesRes.isFailure) println("TestAlmhirtMassSpecs(UPDATE1):" + update1StatesRes)

        idsAndNamesAndAdresses.foreach(x => almhirt.executeTrackedCommand(TestPersonCommand.mutator(AggregateRootRef(x._2, 2), ChangeTestPersonNameAction("new%s".format(x._3))), "A updatename%s".format(x._1.toString)))
        val update2StatesFutures = idsAndNamesAndAdresses.map(x => getResultFor("A updatename%s".format(x._1.toString)))
        val update2StatesRes = AlmFuture.sequence(update2StatesFutures).awaitResult
        if (update2StatesRes.isFailure) println("TestAlmhirtMassSpecs(UPDATE2):" + update2StatesRes)
        update2StatesRes.map { updateStates =>
          updateStates.foreach(x => x fold (f => println(f), succ => ()))
          updateStates
        }.fold(
          f => false,
          succ => succ.forall(_.isSuccess) && succ.forall(_.forceResult.isFinishedSuccesfully))
      }
    }
}