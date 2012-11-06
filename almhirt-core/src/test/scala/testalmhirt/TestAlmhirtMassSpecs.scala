package testalmhirt

import org.specs2.mutable._
//import scalaz._, Scalaz._
import akka.dispatch.{Await, Future}
import almhirt._
import almhirt.almvalidation.kit._
import almhirt.environment._
import test._
import almhirt.almfuture.inst._

class TestAlmhirtMassSpecs extends Specification with TestAlmhirtKit {
  private implicit val atMost = akka.util.Duration(2, "s")
  "The TestAlmhirt" should {
    "create, modify and retrieve 100 persons when actions for all entities are processed as sequenced blocks (A)" in {
      inTestAlmhirt{almhirt =>
    implicit val executor = almhirt.environment.context.system.futureDispatcher
    val idsAndNamesAndAdresses = Vector((for (i <- 0 to 0) yield (i, almhirt.getUuid, "Name%s".format(i), "Address%s".format(i))): _*)

    idsAndNamesAndAdresses.foreach(x => almhirt.executeTrackedCommand(NewTestPerson(x._2, x._3), "A insert%s".format(x._1.toString)))
    val insertStatesFutures = idsAndNamesAndAdresses.map(x => almhirt.getResultOperationStateFor("A insert%s".format(x._1.toString)))
    val insertStatesRes = AlmFuture.sequence(insertStatesFutures).awaitResult
    if(insertStatesRes.isFailure) println(insertStatesRes)
//    insertStatesRes.foreach { insertStates =>
//      insertStates.foreach(x => x fold (f => println(f), succ => println(succ)))
//    }

    idsAndNamesAndAdresses.foreach(x => almhirt.executeTrackedCommand(SetTestPersonAddress(x._2, Some(1), x._4), "A setaddress%s".format(x._1.toString)))
    val update1StatesFutures = idsAndNamesAndAdresses.map(x => almhirt.getResultOperationStateFor("A setaddress%s".format(x._1.toString)))
    val update1StatesRes = AlmFuture.sequence(update1StatesFutures).awaitResult
    if(update1StatesRes.isFailure) println(update1StatesRes)
//    update1StatesRes.foreach { updateStates =>
//      updateStates.foreach(x => x fold (f => println(f), succ => println(succ)))
//    }

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
      }
    }
//    "create, modify and retrieve 100 persons when actions for all entities are processed as blocks (B)" in {
//      inTestAlmhirt{almhirt =>
//        implicit val executor = almhirt.environment.context.system.futureDispatcher
//        val idsAndNamesAndAdresses = Vector((for(i <- 0 to 99) yield (i, almhirt.getUuid, "Name%s".format(i), "Address%s".format(i))): _*)
//        idsAndNamesAndAdresses.foreach(x => almhirt.executeTrackedCommand(NewTestPerson(x._2, x._3), "B insert%s".format(x._1.toString)))      
//        idsAndNamesAndAdresses.foreach(x => almhirt.executeTrackedCommand(SetTestPersonAddress(x._2, Some(1), x._4), "B setaddress%s".format(x._1.toString)))      
//        idsAndNamesAndAdresses.foreach(x => almhirt.executeTrackedCommand(ChangeTestPersonName(x._2, Some(2), "new%s".format(x._3)), "B updatename%s".format(x._1.toString)))      
//        val repo = almhirt.getRepository[TestPerson, TestPersonEvent].awaitResult.forceResult
//        val insertStatesFutures = idsAndNamesAndAdresses.map(x => almhirt.getResultOperationStateFor("B insert%s".format(x._1.toString)))
//        val update1StatesFutures = idsAndNamesAndAdresses.map(x => almhirt.getResultOperationStateFor("B setaddress%s".format(x._1.toString)))
//        val update2StatesFutures = idsAndNamesAndAdresses.map(x => almhirt.getResultOperationStateFor("B updatename%s".format(x._1.toString)))
// 
////        val insertStatesRes = AlmFuture.sequence(insertStatesFutures).awaitResult(akka.util.Duration(5, "s"))
////        insertStatesRes.forceResult.foreach(x => x fold(f => println(f), succ => println(succ))) 
////
////        val update1StatesRes = AlmFuture.sequence(update1StatesFutures).awaitResult(akka.util.Duration(5, "s"))
////        update1StatesRes.forceResult.foreach(x => x fold(f => println(f), succ => println(succ))) 
//
//        val update2StatesRes = AlmFuture.sequence(update2StatesFutures).awaitResult(akka.util.Duration(5, "s"))
//        val finalRes = update2StatesRes.forceResult
//        //finalRes.foreach(x => x fold(f => println(f), succ => println(succ))) 
//        
//        finalRes.forall(_.isSuccess) && finalRes.forall(_.forceResult.isFinishedSuccesfully)
//      }
//    }
  }
}