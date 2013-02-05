package almhirt.commanding

import org.scalatest._
import org.scalatest.matchers.ShouldMatchers
import java.util.UUID
import scala.concurrent.duration.FiniteDuration
import scala.collection.mutable.{ListBuffer, HashMap}
import akka.actor.ActorSystem
import almhirt.core.Almhirt
import almhirt.common._
import almhirt.almvalidation.kit._
import almhirt.environment.AlmhirtTestKit
import almhirt.core.test._
import almhirt.util.TrackingTicket

class BoundDomainActionsUnitOfWorkSpecs extends WordSpec with BeforeAndAfterAll with ShouldMatchers with AlmhirtTestKit {
  val actorSystem = ActorSystem("BoundDomainActionsUnitOfWorkSpecs-System")
  implicit val theAlmhirt = Almhirt.quickCreateFromSystem(actorSystem)
  implicit val atMost = FiniteDuration(1, "s")
 
  override def afterAll {
    theAlmhirt.dispose()
    actorSystem.shutdown()
    actorSystem.awaitTermination()
  }

  def createUOW(getsAnAR: UUID => AlmFuture[TestPerson], storesAnAr: (TestPerson, List[TestPersonEvent], Option[TrackingTicket]) => Unit): TestPersonContext.BoundUnitOfWork = 
    TestPersonContext.createBasicUow(classOf[TestPersonCommand], getsAnAR, storesAnAr, TestPersonContext.hasActionHandlers)
  
  def createUOWOnListBufferAndMap() = {
    val map = new HashMap[UUID, TestPerson]
    val buffer = new ListBuffer[TestPersonEvent]
    def store(ar: TestPerson, events: List[TestPersonEvent], ticket: Option[TrackingTicket]) {
      buffer.append(events: _*)
      map.put(ar.id, ar)
    }
    def get(id: UUID): AlmFuture[TestPerson] = AlmFuture.promise(map.lift >? id)
      
    val uow = createUOW(get, store)
    (uow, map, buffer)
  }
  
  "An UOW on an empty repository" when {
    "adding a new AR via NewTestPersonAction" should {
      "not crash" in {
        val (uow, map, buffer) = createUOWOnListBufferAndMap
        val id = theAlmhirt.getUuid
        val com = TestPersonCommand.createCreator(NewTestPersonAction(id, "Joe"))
        uow.handle(com, None)
      }
      "create exactly 1 event" in {
        val (uow, map, buffer) = createUOWOnListBufferAndMap
        val id = theAlmhirt.getUuid
        val com = TestPersonCommand.createCreator(NewTestPersonAction(id, "Joe"))
        uow.handle(com, None)
        Thread.sleep(50)
        buffer should have size(1)
      }
      "have created a TestPersonCreatedEvent with version 0" in {
        val (uow, map, buffer) = createUOWOnListBufferAndMap
        val id = theAlmhirt.getUuid
        val com = TestPersonCommand.createCreator(NewTestPersonAction(id, "Joe"))
        uow.handle(com, None)
        Thread.sleep(50)
        buffer.head.aggVersion should equal(0L)
      }
      "have created a TestPersonCreatedEvent with aggId equal to the commands aggId" in {
        val (uow, map, buffer) = createUOWOnListBufferAndMap
        val id = theAlmhirt.getUuid
        val com = TestPersonCommand.createCreator(NewTestPersonAction(id, "Joe"))
        uow.handle(com, None)
        Thread.sleep(50)
        buffer.head.aggId should equal(id)
      }
      "have created a TestPerson with id equal to the commands aggId" in {
        val (uow, map, buffer) = createUOWOnListBufferAndMap
        val id = theAlmhirt.getUuid
        val com = TestPersonCommand.createCreator(NewTestPersonAction(id, "Joe"))
        uow.handle(com, None)
        Thread.sleep(50)
        map.get(id).get.id should equal(id)
      }
      "have created a TestPerson with version = 1" in {
        val (uow, map, buffer) = createUOWOnListBufferAndMap
        val id = theAlmhirt.getUuid
        val com = TestPersonCommand.createCreator(NewTestPersonAction(id, "Joe"))
        uow.handle(com, None)
        Thread.sleep(50)
        map.get(id).get.version should equal(1L)
      }
      "have created a TestPerson with the name specified by the creating command" in {
        val (uow, map, buffer) = createUOWOnListBufferAndMap
        val id = theAlmhirt.getUuid
        val com = TestPersonCommand.createCreator(NewTestPersonAction(id, "Joe"))
        uow.handle(com, None)
        Thread.sleep(50)
        map.get(id).get.name should equal("Joe")
      }
    }
    "adding a new AR via NewTestPersonAction modifying the name in the same command" should {
      "not crash" in {
        val (uow, map, buffer) = createUOWOnListBufferAndMap
        val id = theAlmhirt.getUuid
        val com = TestPersonCommand.create(None, NewTestPersonAction(id, "Joe") :: ChangeTestPersonNameAction("Bill") :: Nil)
        uow.handle(com, None)
      }
      "create exactly 2 events" in {
        val (uow, map, buffer) = createUOWOnListBufferAndMap
        val id = theAlmhirt.getUuid
        val com = TestPersonCommand.create(None, NewTestPersonAction(id, "Joe") :: ChangeTestPersonNameAction("Bill") :: Nil)
        uow.handle(com, None)
        Thread.sleep(50)
        buffer should have size(2)
      }
      "have created a TestPersonCreatedEvent with version 0" in {
        val (uow, map, buffer) = createUOWOnListBufferAndMap
        val id = theAlmhirt.getUuid
        val com = TestPersonCommand.create(None, NewTestPersonAction(id, "Joe") :: ChangeTestPersonNameAction("Bill") :: Nil)
        uow.handle(com, None)
        Thread.sleep(50)
        buffer.head.aggVersion should equal(0L)
      }
      "have created a TestPersonCreatedEvent with aggId equal to the commands aggId" in {
        val (uow, map, buffer) = createUOWOnListBufferAndMap
        val id = theAlmhirt.getUuid
        val com = TestPersonCommand.create(None, NewTestPersonAction(id, "Joe") :: ChangeTestPersonNameAction("Bill") :: Nil)
        uow.handle(com, None)
        Thread.sleep(50)
        buffer.head.aggId should equal(id)
      }
      "have created a TestPersonCreatedEvent with aggId different from the events id" in {
        val (uow, map, buffer) = createUOWOnListBufferAndMap
        val id = theAlmhirt.getUuid
        val com = TestPersonCommand.create(None, NewTestPersonAction(id, "Joe") :: ChangeTestPersonNameAction("Bill") :: Nil)
        uow.handle(com, None)
        Thread.sleep(50)
        buffer.head.aggId should not equal( buffer.head.id)
      }
      "have created a TestPersonNameChanged with version 1" in {
        val (uow, map, buffer) = createUOWOnListBufferAndMap
        val id = theAlmhirt.getUuid
        val com = TestPersonCommand.create(None, NewTestPersonAction(id, "Joe") :: ChangeTestPersonNameAction("Bill") :: Nil)
        uow.handle(com, None)
        Thread.sleep(50)
        buffer.tail.head.aggVersion should equal(1L)
      }
      "have created a TestPersonNameChanged with aggId equal to the commands aggId" in {
        val (uow, map, buffer) = createUOWOnListBufferAndMap
        val id = theAlmhirt.getUuid
        val com = TestPersonCommand.create(None, NewTestPersonAction(id, "Joe") :: ChangeTestPersonNameAction("Bill") :: Nil)
        uow.handle(com, None)
        Thread.sleep(50)
        buffer.tail.head.aggId should equal(id)
      }
      "have created a TestPersonNameChanged with aggId different from the events id" in {
        val (uow, map, buffer) = createUOWOnListBufferAndMap
        val id = theAlmhirt.getUuid
        val com = TestPersonCommand.create(None, NewTestPersonAction(id, "Joe") :: ChangeTestPersonNameAction("Bill") :: Nil)
        uow.handle(com, None)
        Thread.sleep(50)
        buffer.tail.head.aggId should not equal( buffer.head.id)
      }
      "have created a TestPerson with id equal to the creating commands aggId" in {
        val (uow, map, buffer) = createUOWOnListBufferAndMap
        val id = theAlmhirt.getUuid
        val com = TestPersonCommand.create(None, NewTestPersonAction(id, "Joe") :: ChangeTestPersonNameAction("Bill") :: Nil)
        uow.handle(com, None)
        Thread.sleep(50)
        map.get(id).get.id should equal(id)
      }
      "have created a TestPerson with version = 2" in {
        val (uow, map, buffer) = createUOWOnListBufferAndMap
        val id = theAlmhirt.getUuid
        val com = TestPersonCommand.create(None, NewTestPersonAction(id, "Joe") :: ChangeTestPersonNameAction("Bill") :: Nil)
        uow.handle(com, None)
        Thread.sleep(50)
        map.get(id).get.version should equal(2L)
      }
      "have created a TestPerson with the name specified by the changing command" in {
        val (uow, map, buffer) = createUOWOnListBufferAndMap
        val id = theAlmhirt.getUuid
        val com = TestPersonCommand.create(None, NewTestPersonAction(id, "Joe") :: ChangeTestPersonNameAction("Bill") :: Nil)
        uow.handle(com, None)
        Thread.sleep(50)
        map.get(id).get.name should equal("Bill")
      }
    }
  }
  
}