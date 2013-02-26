package almhirt.commanding

import org.scalatest._
import org.scalatest.matchers.ShouldMatchers
import java.util.UUID
import scala.concurrent.duration.FiniteDuration
import scala.collection.mutable.{ ListBuffer, HashMap }
import akka.actor.ActorSystem
import almhirt.core.Almhirt
import almhirt.common._
import almhirt.almvalidation.kit._
import almhirt.environment.AlmhirtTestKit
import almhirt.core.test._
import almhirt.util.TrackingTicket
import almhirt.core.CanCreateUuid

class BoundDomainActionsUnitOfWorkSpecs extends WordSpec with BeforeAndAfterAll with ShouldMatchers with AlmhirtTestKit {
  val actorSystem = ActorSystem("BoundDomainActionsUnitOfWorkSpecs-System")
  implicit val theAlmhirt = Almhirt.quickCreateFromSystem(actorSystem)
  implicit val atMost = FiniteDuration(1, "s")

  override def afterAll {
    theAlmhirt.dispose()
    actorSystem.shutdown()
    actorSystem.awaitTermination()
  }

  def createUOW(getsAnAR: UUID => AlmFuture[TestPerson], storesAnAr: (TestPerson, IndexedSeq[TestPersonEvent], Option[TrackingTicket]) => Unit): TestPersonContext.BoundUnitOfWork =
    TestPersonContext.createBasicUow(classOf[TestPersonCommand], getsAnAR, storesAnAr, None)

  def createUOWOnListBufferAndMap() = {
    val map = new HashMap[UUID, TestPerson]
    val buffer = new ListBuffer[TestPersonEvent]
    def store(ar: TestPerson, events: IndexedSeq[TestPersonEvent], ticket: Option[TrackingTicket]) {
      buffer.append(events: _*)
      map.put(ar.id, ar)
    }
    def get(id: UUID): AlmFuture[TestPerson] = AlmFuture.promise(map.lift >? id)

    val uow = createUOW(get, store)
    (uow, map, buffer)
  }

  "An UnitOfWork on an empty repository" when {
    "adding a new AR via NewTestPersonAction" should {
      "not crash" in {
        val (uow, map, buffer) = createUOWOnListBufferAndMap
        val id = theAlmhirt.getUuid
        val com = TestPersonCommand.creator(NewTestPersonAction(id, "Joe"))
        uow.handle(com, None)
      }
      "create exactly 1 event" in {
        val (uow, map, buffer) = createUOWOnListBufferAndMap
        val id = theAlmhirt.getUuid
        val com = TestPersonCommand.creator(NewTestPersonAction(id, "Joe"))
        uow.handle(com, None)
        Thread.sleep(100)
        buffer should have size (1)
      }
      "have created a TestPersonCreatedEvent with version 0" in {
        val (uow, map, buffer) = createUOWOnListBufferAndMap
        val id = theAlmhirt.getUuid
        val com = TestPersonCommand.creator(NewTestPersonAction(id, "Joe"))
        uow.handle(com, None)
        Thread.sleep(100)
        buffer.head.aggVersion should equal(0L)
      }
      "have created a TestPersonCreatedEvent with aggId equal to the commands aggId" in {
        val (uow, map, buffer) = createUOWOnListBufferAndMap
        val id = theAlmhirt.getUuid
        val com = TestPersonCommand.creator(NewTestPersonAction(id, "Joe"))
        uow.handle(com, None)
        Thread.sleep(100)
        buffer.head.aggId should equal(id)
      }
      "have created a TestPerson with id equal to the commands aggId" in {
        val (uow, map, buffer) = createUOWOnListBufferAndMap
        val id = theAlmhirt.getUuid
        val com = TestPersonCommand.creator(NewTestPersonAction(id, "Joe"))
        uow.handle(com, None)
        Thread.sleep(100)
        map.get(id).get.id should equal(id)
      }
      "have created a TestPerson with version = 1" in {
        val (uow, map, buffer) = createUOWOnListBufferAndMap
        val id = theAlmhirt.getUuid
        val com = TestPersonCommand.creator(NewTestPersonAction(id, "Joe"))
        uow.handle(com, None)
        Thread.sleep(100)
        map.get(id).get.version should equal(1L)
      }
      "have created a TestPerson with the name specified by the creating command" in {
        val (uow, map, buffer) = createUOWOnListBufferAndMap
        val id = theAlmhirt.getUuid
        val com = TestPersonCommand.creator(NewTestPersonAction(id, "Joe"))
        uow.handle(com, None)
        Thread.sleep(100)
        map.get(id).get.name should equal("Joe")
      }
    }
    "adding a new AR via NewTestPersonAction modifying the name in the same command" should {
      "not crash" in {
        val (uow, map, buffer) = createUOWOnListBufferAndMap
        val id = theAlmhirt.getUuid
        val com = TestPersonCommand(None, NewTestPersonAction(id, "Joe") :: ChangeTestPersonNameAction("Bill") :: Nil)
        uow.handle(com, None)
      }
      "create exactly 2 events" in {
        val (uow, map, buffer) = createUOWOnListBufferAndMap
        val id = theAlmhirt.getUuid
        val com = TestPersonCommand(None, NewTestPersonAction(id, "Joe") :: ChangeTestPersonNameAction("Bill") :: Nil)
        uow.handle(com, None)
        Thread.sleep(100)
        buffer should have size (2)
      }
      "have created a TestPersonCreatedEvent with version 0" in {
        val (uow, map, buffer) = createUOWOnListBufferAndMap
        val id = theAlmhirt.getUuid
        val com = TestPersonCommand(None, NewTestPersonAction(id, "Joe") :: ChangeTestPersonNameAction("Bill") :: Nil)
        uow.handle(com, None)
        Thread.sleep(100)
        buffer.head.aggVersion should equal(0L)
      }
      "have created a TestPersonCreatedEvent with aggId equal to the commands aggId" in {
        val (uow, map, buffer) = createUOWOnListBufferAndMap
        val id = theAlmhirt.getUuid
        val com = TestPersonCommand(None, NewTestPersonAction(id, "Joe") :: ChangeTestPersonNameAction("Bill") :: Nil)
        uow.handle(com, None)
        Thread.sleep(100)
        buffer.head.aggId should equal(id)
      }
      "have created a TestPersonCreatedEvent with aggId different from the events id" in {
        val (uow, map, buffer) = createUOWOnListBufferAndMap
        val id = theAlmhirt.getUuid
        val com = TestPersonCommand(None, NewTestPersonAction(id, "Joe") :: ChangeTestPersonNameAction("Bill") :: Nil)
        uow.handle(com, None)
        Thread.sleep(100)
        buffer.head.aggId should not equal (buffer.head.id)
      }
      "have created a TestPersonNameChanged with version 1" in {
        val (uow, map, buffer) = createUOWOnListBufferAndMap
        val id = theAlmhirt.getUuid
        val com = TestPersonCommand(None, NewTestPersonAction(id, "Joe") :: ChangeTestPersonNameAction("Bill") :: Nil)
        uow.handle(com, None)
        Thread.sleep(100)
        buffer.tail.head.aggVersion should equal(1L)
      }
      "have created a TestPersonNameChanged with aggId equal to the commands aggId" in {
        val (uow, map, buffer) = createUOWOnListBufferAndMap
        val id = theAlmhirt.getUuid
        val com = TestPersonCommand(None, NewTestPersonAction(id, "Joe") :: ChangeTestPersonNameAction("Bill") :: Nil)
        uow.handle(com, None)
        Thread.sleep(100)
        buffer.tail.head.aggId should equal(id)
      }
      "have created a TestPersonNameChanged with aggId different from the events id" in {
        val (uow, map, buffer) = createUOWOnListBufferAndMap
        val id = theAlmhirt.getUuid
        val com = TestPersonCommand(None, NewTestPersonAction(id, "Joe") :: ChangeTestPersonNameAction("Bill") :: Nil)
        uow.handle(com, None)
        Thread.sleep(100)
        buffer.tail.head.aggId should not equal (buffer.head.id)
      }
      "have created a TestPerson with id equal to the creating commands aggId" in {
        val (uow, map, buffer) = createUOWOnListBufferAndMap
        val id = theAlmhirt.getUuid
        val com = TestPersonCommand(None, NewTestPersonAction(id, "Joe") :: ChangeTestPersonNameAction("Bill") :: Nil)
        uow.handle(com, None)
        Thread.sleep(100)
        map.get(id).get.id should equal(id)
      }
      "have created a TestPerson with version = 2" in {
        val (uow, map, buffer) = createUOWOnListBufferAndMap
        val id = theAlmhirt.getUuid
        val com = TestPersonCommand(None, NewTestPersonAction(id, "Joe") :: ChangeTestPersonNameAction("Bill") :: Nil)
        uow.handle(com, None)
        Thread.sleep(100)
        map.get(id).get.version should equal(2L)
      }
      "have created a TestPerson with the name specified by the changing command" in {
        val (uow, map, buffer) = createUOWOnListBufferAndMap
        val id = theAlmhirt.getUuid
        val com = TestPersonCommand(None, NewTestPersonAction(id, "Joe") :: ChangeTestPersonNameAction("Bill") :: Nil)
        uow.handle(com, None)
        Thread.sleep(100)
        map.get(id).get.name should equal("Bill")
      }
    }
    "changing an existing AR's name" should {
      val id = theAlmhirt.getUuid
      val testPerson = TestPerson.apply(id, "Joe").ar.forceResult
      val com = TestPersonCommand(Some(id, 1L), ChangeTestPersonNameAction("Bill") :: Nil)
      "not crash" in {
        val (uow, map, buffer) = createUOWOnListBufferAndMap
        map += (id -> testPerson)
        uow.handle(com, None)
      }
      "create exactly 1 event" in {
        val (uow, map, buffer) = createUOWOnListBufferAndMap
        map += (id -> testPerson)
        uow.handle(com, None)
        Thread.sleep(100)
        buffer should have size (1)
      }
      "have created a TestPersonNameChanged with version 1" in {
        val (uow, map, buffer) = createUOWOnListBufferAndMap
        map += (id -> testPerson)
        uow.handle(com, None)
        Thread.sleep(100)
        buffer.head.aggVersion should equal(1L)
      }
      "have created a TestPersonNameChanged with aggId equal to the commands aggId" in {
        val (uow, map, buffer) = createUOWOnListBufferAndMap
        map += (id -> testPerson)
        uow.handle(com, None)
        Thread.sleep(100)
        buffer.head.aggId should equal(id)
      }
      "have created a TestPersonNameChanged with aggId different from the events id" in {
        val (uow, map, buffer) = createUOWOnListBufferAndMap
        map += (id -> testPerson)
        uow.handle(com, None)
        Thread.sleep(100)
        buffer.head.aggId should not equal (buffer.head.id)
      }
      "have increased the TestPerson's id to 2" in {
        val (uow, map, buffer) = createUOWOnListBufferAndMap
        map += (id -> testPerson)
        map.get(id).get.version should equal(1L)
        uow.handle(com, None)
        Thread.sleep(100)
        map.get(id).get.version should equal(2L)
      }
      "have changed the TestPerson's name to the name specified by the changing command" in {
        val (uow, map, buffer) = createUOWOnListBufferAndMap
        map += (id -> testPerson)
        uow.handle(com, None)
        Thread.sleep(100)
        map.get(id).get.name should equal("Bill")
      }
    }
  }

  "prevalidate" should {
    "succeed on a single creator action without an AggregateRootRef" in {
      TestPersonContext.prevalidate(TestPersonCommand(None, List(NewTestPersonAction(theAlmhirt.getUuid, "Joe")))).isSuccess should be(true)
    }
    "succeed on a single mutator action with an AggregateRootRef" in {
      TestPersonContext.prevalidate(TestPersonCommand(Some(theAlmhirt.getUuid, 0L), List(ChangeTestPersonNameAction("Bill")))).isSuccess should be(true)
    }
    "fail on a single creator action with an AggregateRootRef" in {
      TestPersonContext.prevalidate(TestPersonCommand(Some(theAlmhirt.getUuid, 0L), List(NewTestPersonAction(theAlmhirt.getUuid, "Joe")))).isFailure should be(true)
    }
    "fail on a single mutator action without an AggregateRootRef" in {
      TestPersonContext.prevalidate(TestPersonCommand(None, List(ChangeTestPersonNameAction("Bill")))).isFailure should be(true)
    }
    "fail on 2 creator actions with an AggregateRootRef" in {
      TestPersonContext.prevalidate(TestPersonCommand(Some(theAlmhirt.getUuid, 0L), List(NewTestPersonAction(theAlmhirt.getUuid, "Joe"), NewTestPersonAction(theAlmhirt.getUuid, "Jim")))).isFailure should be(true)
    }
    "fail on 2 creator actions without an AggregateRootRef" in {
      TestPersonContext.prevalidate(TestPersonCommand(None, List(NewTestPersonAction(theAlmhirt.getUuid, "Joe"), NewTestPersonAction(theAlmhirt.getUuid, "Jim")))).isFailure should be(true)
    }
    "succeed on 2 mutator actions with an AggregateRootRef" in {
      TestPersonContext.prevalidate(TestPersonCommand(Some(theAlmhirt.getUuid, 0L), List(ChangeTestPersonNameAction("Bill"), ChangeTestPersonNameAction("Bob")))).isSuccess should be(true)
    }
    "fail on 2 mutator actions without an AggregateRootRef" in {
      TestPersonContext.prevalidate(TestPersonCommand(None, List(ChangeTestPersonNameAction("Bill"), ChangeTestPersonNameAction("Bob")))).isFailure should be(true)
    }
    "succeed on a creator action followed by a mutator action without an AggregateRootRef" in {
      TestPersonContext.prevalidate(TestPersonCommand(None, NewTestPersonAction(theAlmhirt.getUuid, "Joe") :: ChangeTestPersonNameAction("Bill") :: Nil)).isSuccess should be(true)
    }
    "fail on a creator action followed by a mutator action with an AggregateRootRef" in {
      TestPersonContext.prevalidate(TestPersonCommand(Some(theAlmhirt.getUuid, 0L), NewTestPersonAction(theAlmhirt.getUuid, "Joe") :: ChangeTestPersonNameAction("Bill") :: Nil)).isFailure should be(true)
    }
    "fail on a mutator action followed by a creator action without an AggregateRootRef" in {
      TestPersonContext.prevalidate(TestPersonCommand(None, ChangeTestPersonNameAction("Bill") :: NewTestPersonAction(theAlmhirt.getUuid, "Joe") :: Nil)).isFailure should be(true)
    }
    "fail on a mutator action followed by a creator action with an AggregateRootRef" in {
      TestPersonContext.prevalidate(TestPersonCommand(Some(theAlmhirt.getUuid, 0L), ChangeTestPersonNameAction("Bill") :: NewTestPersonAction(theAlmhirt.getUuid, "Joe") :: Nil)).isFailure should be(true)
    }
    "succeed on without actions and without an AggregateRootRef" in {
      TestPersonContext.prevalidate(TestPersonCommand(None, Nil)).isSuccess should be(true)
    }
    "succeed on without actions and with an AggregateRootRef" in {
      TestPersonContext.prevalidate(TestPersonCommand(Some(theAlmhirt.getUuid, 0L), Nil)).isSuccess should be(true)
    }
    "succeed with the creator factory method" in {
      TestPersonContext.prevalidate(TestPersonCommand.creator(NewTestPersonAction(theAlmhirt.getUuid, "Joe"))).isSuccess should be(true)
    }
    "succeed with the creatorAndMutator factory method" in {
      TestPersonContext.prevalidate(TestPersonCommand.creatorAndMutator(NewTestPersonAction(theAlmhirt.getUuid, "Joe"), ChangeTestPersonNameAction("Bill"))).isSuccess should be(true)
    }
    "succeed with the creatorAndMutators factory method" in {
      TestPersonContext.prevalidate(TestPersonCommand.creatorAndMutators(NewTestPersonAction(theAlmhirt.getUuid, "Joe"), List(ChangeTestPersonNameAction("Bill"), ChangeTestPersonNameAction("Bob")))).isSuccess should be(true)
    }
    "succeed with the creatorAndMutators factory method even if mutators is empty" in {
      TestPersonContext.prevalidate(TestPersonCommand.creatorAndMutators(NewTestPersonAction(theAlmhirt.getUuid, "Joe"), Nil)).isSuccess should be(true)
    }
    "succeed with the mutator factory method" in {
      TestPersonContext.prevalidate(TestPersonCommand.mutator((theAlmhirt.getUuid, 0L), ChangeTestPersonNameAction("Bill"))).isSuccess should be(true)
    }
    "succeed with the mutators factory method" in {
      TestPersonContext.prevalidate(TestPersonCommand.mutators((theAlmhirt.getUuid, 0L), List(ChangeTestPersonNameAction("Bill"), ChangeTestPersonNameAction("Bob")))).isSuccess should be(true)
    }
    "succeed with the mutators factory method even if mutators is empty" in {
      TestPersonContext.prevalidate(TestPersonCommand.mutators((theAlmhirt.getUuid, 0L), Nil)).isSuccess should be(true)
    }
  }

}