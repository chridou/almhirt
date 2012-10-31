package testalmhirt

import org.specs2.mutable._
import scalaz._, Scalaz._
import akka.dispatch.{Await, Future}
import almhirt._
import almhirt.almvalidation.kit._
import almhirt.environment._
import test._
import almhirt.almfuture.inst._

class TestAlmHirtMassInsertSpecs extends Specification with TestAlmhirtKit {
  private implicit val atMost = akka.util.Duration(2, "s")
  "The TestAlmhirt" should {
    "create, modify and retrieve 1000 persons" in {
      inTestAlmhirt{almhirt =>
        implicit val executor = almhirt.environment.context.system.futureDispatcher
        val idsAndNamesAndAdresses = Vector((for(i <- 0 to 999) yield (i, almhirt.getUuid, "Name%s".format(i), "Adress%s".format(i))): _*)
        idsAndNamesAndAdresses.foreach(x => almhirt.executeTrackedCommand(NewTestPerson(x._2, x._3), "insert%s".format(x._1.toString)))      
        idsAndNamesAndAdresses.foreach(x => almhirt.executeTrackedCommand(SetTestPersonAddress(x._2, Some(2), x._4), "setaddress%s".format(x._1.toString)))      
        idsAndNamesAndAdresses.foreach(x => almhirt.executeTrackedCommand(ChangeTestPersonName(x._2, Some(2), "new%s".format(x._3)), "updatename%s".format(x._1.toString)))      
        val repo = almhirt.getRepository[TestPerson, TestPersonEvent].awaitResult.forceResult
        val entities = 
          idsAndNamesAndAdresses.map(x => repo.get(x._2).underlying).toList
        val complete = Await.result(Future.sequence(entities), akka.util.Duration(5, "s"))
        complete.foreach(x => x fold(f => println(f), succ => println(succ)))      
        //val res = complete.sequence
        false
      }
    }
  }
}