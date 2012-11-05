package testalmhirt

import org.specs2.mutable._
import scalaz._, Scalaz._
import akka.dispatch.{Await, Future}
import almhirt._
import almhirt.almvalidation.kit._
import almhirt.environment._
import test._
import almhirt.almfuture.inst._

class TestAlmhirtMassSpecs extends Specification with TestAlmhirtKit {
  private implicit val atMost = akka.util.Duration(2, "s")
  "The TestAlmhirt" should {
    "create, modify and retrieve 100 persons" in {
      inTestAlmhirt{almhirt =>
        implicit val executor = almhirt.environment.context.system.futureDispatcher
        val idsAndNamesAndAdresses = Vector((for(i <- 0 to 99) yield (i, almhirt.getUuid, "Name%s".format(i), "Address%s".format(i))): _*)
        idsAndNamesAndAdresses.foreach(x => almhirt.executeTrackedCommand(NewTestPerson(x._2, x._3), "insert%s".format(x._1.toString)))      
        idsAndNamesAndAdresses.foreach(x => almhirt.executeTrackedCommand(SetTestPersonAddress(x._2, Some(1), x._4), "setaddress%s".format(x._1.toString)))      
        idsAndNamesAndAdresses.foreach(x => almhirt.executeTrackedCommand(ChangeTestPersonName(x._2, Some(2), "new%s".format(x._3)), "updatename%s".format(x._1.toString)))      
        val repo = almhirt.getRepository[TestPerson, TestPersonEvent].awaitResult.forceResult
//        val entities = 
//          idsAndNamesAndAdresses.map(x => repo.get(x._2).underlying).toList
//        val complete = Await.result(Future.sequence(entities), akka.util.Duration(5, "s"))
//        complete.foreach(x => x fold(f => println(f), succ => println(succ))) 
        
        val insertStatesFutures = idsAndNamesAndAdresses.map(x => almhirt.getResultOperationStateFor("insert%s".format(x._1.toString)))
        val insertStatesRes = Await.result(AlmFuture.sequenceValidations(insertStatesFutures), akka.util.Duration(5, "s"))
        insertStatesRes.foreach(x => x fold(f => println(f), succ => println(succ))) 

        val update1StatesFutures = idsAndNamesAndAdresses.map(x => almhirt.getResultOperationStateFor("setaddress%s".format(x._1.toString)))
        val update1StatesRes = Await.result(AlmFuture.sequenceValidations(update1StatesFutures), akka.util.Duration(5, "s"))
        update1StatesRes.foreach(x => x fold(f => println(f), succ => println(succ))) 

        val update2StatesFutures = idsAndNamesAndAdresses.map(x => almhirt.getResultOperationStateFor("updatename%s".format(x._1.toString)))
        val update2StatesRes = Await.result(AlmFuture.sequenceValidations(update2StatesFutures), akka.util.Duration(5, "s"))
        update2StatesRes.foreach(x => x fold(f => println(f), succ => println(succ))) 
        
        //val res = complete.sequence
        false
      }
    }
  }
}