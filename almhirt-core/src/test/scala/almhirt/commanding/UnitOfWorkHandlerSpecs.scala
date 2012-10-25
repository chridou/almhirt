package almhirt.commanding

import org.specs2.mutable._
import java.util.UUID
import scalaz._, Scalaz._
import almhirt._
import almhirt.syntax.almvalidation._
import test._

class UnitOfWorkHandlerSpecs extends Specification {
  """A NewTestPersonUnitOfWork's handler""" should {
    """be able to create a new TestPerson""" in {
      false
    }
    """create a new TestPerson with version 1""" in {
      false
    }
    """create exactly one TestPersonCreated event""" in {
      false
    }
  }
  
  """A ChangeTestPersonNameUnitOfWork's handler""" should {
    """be able to be executed succesfully""" in {
      false
    }
    """create a new TestPerson with version increased by 1""" in {
      false
    }
    """create a new TestPerson with the changed name""" in {
      false
    }
    """create exactly one TestPersonNameChanged event""" in {
      false
    }
  }
  
}