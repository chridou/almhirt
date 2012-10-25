package almhirt.commanding

import org.specs2.mutable._
import java.util.UUID
import scalaz.syntax.validation._
import almhirt._
import almhirt.syntax.almvalidation._
import test._

class UnitOfWorkHandlerSpecs extends Specification {
  """A NewTestPersonUnitOfWork's handler""" should {
    """be able to create a new TestPerson when a valid name is supplied""" in {
      val res = NewTestPersonUnitOfWork.handler(NewTestPerson("Harry"))
      res.isSuccess
    }
    """not be able to create a new TestPerson when an invalid name is supplied""" in {
      val res = NewTestPersonUnitOfWork.handler(NewTestPerson(""))
      res.isFailure
    }
    """create a new TestPerson with version 1""" in {
      val res = NewTestPersonUnitOfWork.handler(NewTestPerson("Harry"))
      res.forceResult._1.version === 1L
    }
    """create exactly one Event event""" in {
      val res = NewTestPersonUnitOfWork.handler(NewTestPerson("Harry")).forceResult
      res._2.length === 1
    }
    """create exactly one TestPersonCreated event""" in {
      val res = NewTestPersonUnitOfWork.handler(NewTestPerson("Harry")).forceResult
      res._2.head.isInstanceOf[TestPersonCreated]
    }
    """create exactly one TestPersonCreated event with version = 1""" in {
      val res = NewTestPersonUnitOfWork.handler(NewTestPerson("Harry")).forceResult
      val event =  res._2.head.asInstanceOf[TestPersonCreated]
      event.version === 1L
    }
    """create exactly one TestPersonCreated event with the correct name""" in {
      val res = NewTestPersonUnitOfWork.handler(NewTestPerson("Harry")).forceResult
      val event =  res._2.head.asInstanceOf[TestPersonCreated]
      event.name === "Harry"
    }
    """create exactly one TestPersonCreated event with the same id as the created entity""" in {
      val res = NewTestPersonUnitOfWork.handler(NewTestPerson("Harry")).forceResult
      val event =  res._2.head.asInstanceOf[TestPersonCreated]
      event.id === res._1.id
    }
  }
  
  """A ChangeTestPersonNameUnitOfWork's handler""" should {
    """return a success when a valid name is supplied incuding the correct version""" in {
      val originalEntity = NewTestPersonUnitOfWork.handler(NewTestPerson("Harry")).forceResult._1
      val res = ChangeTestPersonNameUnitOfWork.handler(ChangeTestPersonName(originalEntity.id, Some(originalEntity.version), "Bill"), originalEntity)
      res.isSuccess
    }
    """return a failure when an invalid name is supplied incuding the correct version""" in {
      val originalEntity = NewTestPersonUnitOfWork.handler(NewTestPerson("Harry")).forceResult._1
      val res = ChangeTestPersonNameUnitOfWork.handler(ChangeTestPersonName(originalEntity.id, Some(originalEntity.version), ""), originalEntity)
      res.isFailure
    }
    """return a failure when an valid name is supplied with an incorrect version""" in {
      val originalEntity = NewTestPersonUnitOfWork.handler(NewTestPerson("Harry")).forceResult._1
      val res = ChangeTestPersonNameUnitOfWork.handler(ChangeTestPersonName(originalEntity.id, Some(originalEntity.version+1), "Bill"), originalEntity)
      res.isFailure
    }
    """return a success when a valid name is supplied without a version""" in {
      val originalEntity = NewTestPersonUnitOfWork.handler(NewTestPerson("Harry")).forceResult._1
      val res = ChangeTestPersonNameUnitOfWork.handler(ChangeTestPersonName(originalEntity.id, None, "Bill"), originalEntity)
      res.isSuccess
    }
    """return a failure when an invalid name is supplied without a version""" in {
      val originalEntity = NewTestPersonUnitOfWork.handler(NewTestPerson("Harry")).forceResult._1
      val res = ChangeTestPersonNameUnitOfWork.handler(ChangeTestPersonName(originalEntity.id, None, ""), originalEntity)
      res.isFailure
    }
    """return a failure when a valid name and a different id and the correct version is supplied""" in {
      val originalEntity = NewTestPersonUnitOfWork.handler(NewTestPerson("Harry")).forceResult._1
      val res = ChangeTestPersonNameUnitOfWork.handler(ChangeTestPersonName(java.util.UUID.randomUUID, Some(originalEntity.version), "Bill"), originalEntity)
      res.isFailure
    }
    """return a failure when an invalid name and a different id and the correct version is supplied""" in {
      val originalEntity = NewTestPersonUnitOfWork.handler(NewTestPerson("Harry")).forceResult._1
      val res = ChangeTestPersonNameUnitOfWork.handler(ChangeTestPersonName(java.util.UUID.randomUUID, Some(originalEntity.version), ""), originalEntity)
      res.isFailure
    }
    """return a failure when a valid name and a different id and no version is supplied""" in {
      val originalEntity = NewTestPersonUnitOfWork.handler(NewTestPerson("Harry")).forceResult._1
      val res = ChangeTestPersonNameUnitOfWork.handler(ChangeTestPersonName(java.util.UUID.randomUUID, None, "Bill"), originalEntity)
      res.isFailure
    }
    """return a failure when an invalid name and a different id and no version is supplied""" in {
      val originalEntity = NewTestPersonUnitOfWork.handler(NewTestPerson("Harry")).forceResult._1
      val res = ChangeTestPersonNameUnitOfWork.handler(ChangeTestPersonName(java.util.UUID.randomUUID, None, ""), originalEntity)
      res.isFailure
    }
    """create a new TestPerson with version increased by 1""" in {
      val originalEntity = NewTestPersonUnitOfWork.handler(NewTestPerson("Harry")).forceResult._1
      val res = ChangeTestPersonNameUnitOfWork.handler(ChangeTestPersonName(originalEntity.id, Some(originalEntity.version), "Bill"), originalEntity)
      res.forceResult._1.version === originalEntity.version + 1L
    }
    """create a new TestPerson with the changed name""" in {
      val originalEntity = NewTestPersonUnitOfWork.handler(NewTestPerson("Harry")).forceResult._1
      val res = ChangeTestPersonNameUnitOfWork.handler(ChangeTestPersonName(originalEntity.id, Some(originalEntity.version), "Bill"), originalEntity)
      res.forceResult._1.name === "Bill"
    }
    """create exactly one event""" in {
      val originalEntity = NewTestPersonUnitOfWork.handler(NewTestPerson("Harry")).forceResult._1
      val res = ChangeTestPersonNameUnitOfWork.handler(ChangeTestPersonName(originalEntity.id, Some(originalEntity.version), "Bill"), originalEntity)
      res.forceResult._2.length === 1
    }
    """create a TestPersonNameChanged event""" in {
      val originalEntity = NewTestPersonUnitOfWork.handler(NewTestPerson("Harry")).forceResult._1
      val res = ChangeTestPersonNameUnitOfWork.handler(ChangeTestPersonName(originalEntity.id, Some(originalEntity.version), "Bill"), originalEntity)
      res.forceResult._2.head.isInstanceOf[TestPersonNameChanged]
    }
    """create exactly one TestPersonNameChanged with the id being the same as the entity's id""" in {
      val originalEntity = NewTestPersonUnitOfWork.handler(NewTestPerson("Harry")).forceResult._1
      val res = ChangeTestPersonNameUnitOfWork.handler(ChangeTestPersonName(originalEntity.id, Some(originalEntity.version), "Bill"), originalEntity)
      val event = res.forceResult._2.head.asInstanceOf[TestPersonNameChanged]
      event.id === originalEntity.id
    }
    """create exactly one TestPersonNameChanged with the version being one less then the new entity's version""" in {
      val originalEntity = NewTestPersonUnitOfWork.handler(NewTestPerson("Harry")).forceResult._1
      val res = ChangeTestPersonNameUnitOfWork.handler(ChangeTestPersonName(originalEntity.id, Some(originalEntity.version), "Bill"), originalEntity)
      val event = res.forceResult._2.head.asInstanceOf[TestPersonNameChanged]
      event.version === res.forceResult._1.version - 1L
    }
    """create exactly one TestPersonNameChanged with the version being the same as the original entity's version""" in {
      val originalEntity = NewTestPersonUnitOfWork.handler(NewTestPerson("Harry")).forceResult._1
      val res = ChangeTestPersonNameUnitOfWork.handler(ChangeTestPersonName(originalEntity.id, Some(originalEntity.version), "Bill"), originalEntity)
      val event = res.forceResult._2.head.asInstanceOf[TestPersonNameChanged]
      event.version === originalEntity.version
    }
    """create exactly one TestPersonNameChanged with name being the new entitie's name""" in {
      val originalEntity = NewTestPersonUnitOfWork.handler(NewTestPerson("Harry")).forceResult._1
      val res = ChangeTestPersonNameUnitOfWork.handler(ChangeTestPersonName(originalEntity.id, Some(originalEntity.version), "Bill"), originalEntity)
      val event = res.forceResult._2.head.asInstanceOf[TestPersonNameChanged]
      event.newName === res.forceResult._1.name
    }
  }
  
}