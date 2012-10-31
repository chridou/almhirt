package almhirt.commanding

import org.specs2.mutable._
import java.util.UUID
import scalaz.syntax.validation._
import almhirt._
import almhirt.syntax.almvalidation._
import test._
import almhirt.environment.AlmhirtContextTestKit

class UnitOfWorkHandlerSpecs extends Specification with AlmhirtContextTestKit {
  implicit val duration = akka.util.Duration(1, "s")
  """A NewTestPersonUnitOfWork's handler""" should {
    """be able to create a new TestPerson when a valid name is supplied""" in {
      inFakeContext { implicit ctx =>
        val res = new NewTestPersonUnitOfWork().handler(NewTestPerson(java.util.UUID.randomUUID, "Harry"))
        res.awaitResult.isSuccess
      }
    }
    """not be able to create a new TestPerson when an invalid name is supplied""" in {
      inFakeContext { implicit ctx =>
        val res = new NewTestPersonUnitOfWork().handler(NewTestPerson(java.util.UUID.randomUUID, ""))
        res.awaitResult.isFailure
      }
    }
    """create a new TestPerson with version 1""" in {
      inFakeContext { implicit ctx =>
        val res = new NewTestPersonUnitOfWork().handler(NewTestPerson(java.util.UUID.randomUUID, "Harry"))
        res.awaitResult.forceResult._1.version === 1L
      }
    }
    """create exactly one Event event""" in {
      inFakeContext { implicit ctx =>
        val res = new NewTestPersonUnitOfWork().handler(NewTestPerson(java.util.UUID.randomUUID, "Harry")).awaitResult.forceResult
        res._2.length === 1
      }
    }
    """create exactly one TestPersonCreated event""" in {
      inFakeContext { implicit ctx =>
        val res = new NewTestPersonUnitOfWork().handler(NewTestPerson(java.util.UUID.randomUUID, "Harry")).awaitResult.forceResult
        res._2.head.isInstanceOf[TestPersonCreated]
      }
    }
    """create exactly one TestPersonCreated event with version = 1""" in {
      inFakeContext { implicit ctx =>
        val res = new NewTestPersonUnitOfWork().handler(NewTestPerson(java.util.UUID.randomUUID, "Harry")).awaitResult.forceResult
        val event = res._2.head.asInstanceOf[TestPersonCreated]
        event.version === 1L
      }
    }
    """create exactly one TestPersonCreated event with the correct name""" in {
      inFakeContext { implicit ctx =>
        val res = new NewTestPersonUnitOfWork().handler(NewTestPerson(java.util.UUID.randomUUID, "Harry")).awaitResult.forceResult
        val event = res._2.head.asInstanceOf[TestPersonCreated]
        event.name === "Harry"
      }
    }
    """create exactly one TestPersonCreated event with the same id as the created entity""" in {
      inFakeContext { implicit ctx =>
        val res = new NewTestPersonUnitOfWork().handler(NewTestPerson(java.util.UUID.randomUUID, "Harry")).awaitResult.forceResult
        val event = res._2.head.asInstanceOf[TestPersonCreated]
        event.id === res._1.id
      }
    }
  }

  """A ChangeTestPersonNameUnitOfWork's handler""" should {
    """return a success when a valid name is supplied incuding the correct version""" in {
      inFakeContext { implicit ctx =>
        val originalEntity = new NewTestPersonUnitOfWork().handler(NewTestPerson(java.util.UUID.randomUUID, "Harry")).awaitResult.forceResult._1
        val res = new ChangeTestPersonNameUnitOfWork().handler(ChangeTestPersonName(originalEntity.id, Some(originalEntity.version), "Bill"), originalEntity)
        res.awaitResult.isSuccess
      }
    }
    """return a failure when an invalid name is supplied incuding the correct version""" in {
      inFakeContext { implicit ctx =>
        val originalEntity = new NewTestPersonUnitOfWork().handler(NewTestPerson(java.util.UUID.randomUUID, "Harry")).awaitResult.forceResult._1
        val res = new ChangeTestPersonNameUnitOfWork().handler(ChangeTestPersonName(originalEntity.id, Some(originalEntity.version), ""), originalEntity)
        res.awaitResult.isFailure
      }
    }
    """return a failure when an valid name is supplied with an incorrect version""" in {
      inFakeContext { implicit ctx =>
        val originalEntity = new NewTestPersonUnitOfWork().handler(NewTestPerson(java.util.UUID.randomUUID, "Harry")).awaitResult.forceResult._1
        val res = new ChangeTestPersonNameUnitOfWork().handler(ChangeTestPersonName(originalEntity.id, Some(originalEntity.version + 1), "Bill"), originalEntity)
        res.awaitResult.isFailure
      }
    }
    """return a success when a valid name is supplied without a version""" in {
      inFakeContext { implicit ctx =>
        val originalEntity = new NewTestPersonUnitOfWork().handler(NewTestPerson(java.util.UUID.randomUUID, "Harry")).awaitResult.forceResult._1
        val res = new ChangeTestPersonNameUnitOfWork().handler(ChangeTestPersonName(originalEntity.id, None, "Bill"), originalEntity)
        res.awaitResult.isSuccess
      }
    }
    """return a failure when an invalid name is supplied without a version""" in {
      inFakeContext { implicit ctx =>
        val originalEntity = new NewTestPersonUnitOfWork().handler(NewTestPerson(java.util.UUID.randomUUID, "Harry")).awaitResult.forceResult._1
        val res = new ChangeTestPersonNameUnitOfWork().handler(ChangeTestPersonName(originalEntity.id, None, ""), originalEntity)
        res.awaitResult.isFailure
      }
    }
    """return a failure when an invalid name and a different id and the correct version is supplied""" in {
      inFakeContext { implicit ctx =>
        val originalEntity = new NewTestPersonUnitOfWork().handler(NewTestPerson(java.util.UUID.randomUUID, "Harry")).awaitResult.forceResult._1
        val res = new ChangeTestPersonNameUnitOfWork().handler(ChangeTestPersonName(java.util.UUID.randomUUID, Some(originalEntity.version), ""), originalEntity)
        res.awaitResult.isFailure
      }
    }
    """return a failure when a valid name and a different id and no version is supplied""" in {
      inFakeContext { implicit ctx =>
        val originalEntity = new NewTestPersonUnitOfWork().handler(NewTestPerson(java.util.UUID.randomUUID, "Harry")).awaitResult.forceResult._1
        val res = new ChangeTestPersonNameUnitOfWork().handler(ChangeTestPersonName(java.util.UUID.randomUUID, None, "Bill"), originalEntity)
        res.awaitResult.isFailure
      }
    }
    """return a failure when an invalid name and a different id and no version is supplied""" in {
      inFakeContext { implicit ctx =>
        val originalEntity = new NewTestPersonUnitOfWork().handler(NewTestPerson(java.util.UUID.randomUUID, "Harry")).awaitResult.forceResult._1
        val res = new ChangeTestPersonNameUnitOfWork().handler(ChangeTestPersonName(java.util.UUID.randomUUID, None, ""), originalEntity)
        res.awaitResult.isFailure
      }
    }
    """create a new TestPerson with version increased by 1""" in {
      inFakeContext { implicit ctx =>
        val originalEntity = new NewTestPersonUnitOfWork().handler(NewTestPerson(java.util.UUID.randomUUID, "Harry")).awaitResult.forceResult._1
        val res = new ChangeTestPersonNameUnitOfWork().handler(ChangeTestPersonName(originalEntity.id, Some(originalEntity.version), "Bill"), originalEntity)
        res.awaitResult.forceResult._1.version === originalEntity.version + 1L
      }
    }
    """create a new TestPerson with the changed name""" in {
      inFakeContext { implicit ctx =>
        val originalEntity = new NewTestPersonUnitOfWork().handler(NewTestPerson(java.util.UUID.randomUUID, "Harry")).awaitResult.forceResult._1
        val res = new ChangeTestPersonNameUnitOfWork().handler(ChangeTestPersonName(originalEntity.id, Some(originalEntity.version), "Bill"), originalEntity)
        res.awaitResult.forceResult._1.name === "Bill"
      }
    }
    """create exactly one event""" in {
      inFakeContext { implicit ctx =>
        val originalEntity = new NewTestPersonUnitOfWork().handler(NewTestPerson(java.util.UUID.randomUUID, "Harry")).awaitResult.forceResult._1
        val res = new ChangeTestPersonNameUnitOfWork().handler(ChangeTestPersonName(originalEntity.id, Some(originalEntity.version), "Bill"), originalEntity)
        res.awaitResult.forceResult._2.length === 1
      }
    }
    """create a TestPersonNameChanged event""" in {
      inFakeContext { implicit ctx =>
        val originalEntity = new NewTestPersonUnitOfWork().handler(NewTestPerson(java.util.UUID.randomUUID, "Harry")).awaitResult.forceResult._1
        val res = new ChangeTestPersonNameUnitOfWork().handler(ChangeTestPersonName(originalEntity.id, Some(originalEntity.version), "Bill"), originalEntity)
        res.awaitResult.forceResult._2.head.isInstanceOf[TestPersonNameChanged]
      }
    }
    """create exactly one TestPersonNameChanged with the id being the same as the entity's id""" in {
      inFakeContext { implicit ctx =>
        val originalEntity = new NewTestPersonUnitOfWork().handler(NewTestPerson(java.util.UUID.randomUUID, "Harry")).awaitResult.forceResult._1
        val res = new ChangeTestPersonNameUnitOfWork().handler(ChangeTestPersonName(originalEntity.id, Some(originalEntity.version), "Bill"), originalEntity)
        val event = res.awaitResult.forceResult._2.head.asInstanceOf[TestPersonNameChanged]
        event.id === originalEntity.id
      }
    }
    """create exactly one TestPersonNameChanged with the version being one less then the new entity's version""" in {
      inFakeContext { implicit ctx =>
        val originalEntity = new NewTestPersonUnitOfWork().handler(NewTestPerson(java.util.UUID.randomUUID, "Harry")).awaitResult.forceResult._1
        val res = new ChangeTestPersonNameUnitOfWork().handler(ChangeTestPersonName(originalEntity.id, Some(originalEntity.version), "Bill"), originalEntity)
        val event = res.awaitResult.forceResult._2.head.asInstanceOf[TestPersonNameChanged]
        event.version === res.awaitResult.forceResult._1.version - 1L
      }
    }
    """create exactly one TestPersonNameChanged with the version being the same as the original entity's version""" in {
      inFakeContext { implicit ctx =>
        val originalEntity = new NewTestPersonUnitOfWork().handler(NewTestPerson(java.util.UUID.randomUUID, "Harry")).awaitResult.forceResult._1
        val res = new ChangeTestPersonNameUnitOfWork().handler(ChangeTestPersonName(originalEntity.id, Some(originalEntity.version), "Bill"), originalEntity)
        val event = res.awaitResult.forceResult._2.head.asInstanceOf[TestPersonNameChanged]
        event.version === originalEntity.version
      }
    }
    """create exactly one TestPersonNameChanged with name being the new entitie's name""" in {
      inFakeContext { implicit ctx =>
        val originalEntity = new NewTestPersonUnitOfWork().handler(NewTestPerson(java.util.UUID.randomUUID, "Harry")).awaitResult.forceResult._1
        val res = new ChangeTestPersonNameUnitOfWork().handler(ChangeTestPersonName(originalEntity.id, Some(originalEntity.version), "Bill"), originalEntity)
        val event = res.awaitResult.forceResult._2.head.asInstanceOf[TestPersonNameChanged]
        event.newName === res.awaitResult.forceResult._1.name
      }
    }
  }

}