package almhirt.commanding

import org.scalatest._
import org.scalatest.matchers.ShouldMatchers
import java.util.UUID
import scala.concurrent.duration.FiniteDuration
import almhirt.core.Almhirt
import almhirt.common._
import almhirt.almvalidation.kit._
import almhirt.environment.AlmhirtTestKit
import almhirt.core.test._
import almhirt.util.TrackingTicket
import scala.collection.mutable.ListBuffer

class BoundDomainActionsUnitOfWorkSpecs extends FlatSpec with BeforeAndAfterAll with ShouldMatchers with AlmhirtTestKit {
  implicit val theAlmhirt = Almhirt.quickCreateWithSystem("BoundDomainActionsUnitOfWorkSpecs-System")
  implicit val atMost = FiniteDuration(1, "s")
 
  override def afterAll {
    theAlmhirt.dispose()
  }

  def createUOW(getsAnAR: UUID => AlmFuture[TestPerson], storesAnAr: (TestPerson, List[TestPersonEvent], Option[TrackingTicket]) => Unit): TestPersonContext.BoundUnitOfWork = 
    TestPersonContext.createBasicUow(classOf[TestPersonCommand], getsAnAR, storesAnAr, TestPersonContext.hasActionHandlers)
  
  def createUOWOnListBufferAndMap(getsAnAR: UUID => AlmFuture[TestPerson]) = {
    val map = new scala.collection.mutable.HashMap[UUID, TestPerson]
    val buffer = new ListBuffer[TestPersonEvent]
    def store(ar: TestPerson, events: List[TestPersonEvent], ticket: Option[TrackingTicket]) {
      buffer.append(events: _*)
      map.put(ar.id, ar)
    }
    def get(id: UUID): AlmFuture[TestPerson] = AlmFuture.promise(map.lift >? id)
      
    val uow = createUOW(get, store)
    (uow, map, buffer)
  }
}