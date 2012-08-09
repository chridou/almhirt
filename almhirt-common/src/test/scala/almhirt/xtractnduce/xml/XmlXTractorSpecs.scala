package almhirt.xtractnduce.xml

import org.specs2.mutable._
import scalaz._, Scalaz._
import almhirt.validation.AlmValidation._

class XmlXTractorSpecs extends Specification {
  import XmlXTractor._
  import XTractNDuceSamples._

  """A XmlXTractor for Bob when queried for "id"(PK!) which is a Long""" should {
    """return success 0L when queried with getLong""" in {
      bob.xtractor.getLong("id") must beEqualTo(0L.successSBD)
    }
    """return success 0 when queried with getInt""" in {
      bob.xtractor.getInt("id") must beEqualTo(0.successSBD)
    }
    """return success 0.0 when when queried with getDouble""" in {
      bob.xtractor.getDouble("id") must beEqualTo(0.0.successSBD)
    }
    """return success "0" when queried with getString""" in {
      bob.xtractor.getString("id") must beEqualTo("0".successSBD)
    }
    """return success Some(0L) when queried with tryGetLong""" in {
      bob.xtractor.tryGetLong("id") must beEqualTo(Some(0L).successSBD)
    }
    """return success Some(0) when when queried with tryGetInt""" in {
      bob.xtractor.tryGetInt("id") must beEqualTo(Some(0).successSBD)
    }
    """return success Some(0.0) when when queried with tryGetDouble""" in {
      bob.xtractor.tryGetDouble("id") must beEqualTo(Some(0.0).successSBD)
    }
    """return success Some("0") when queried with tryGetString""" in {
      bob.xtractor.tryGetString("id") must beEqualTo(Some("0").successSBD)
    }
  }
  
  """A XmlXTractor for Bob when queried for "name" which is a String""" should {
    """return a failure when queried with getLong""" in {
      bob.xtractor.getLong("name").isFailure
    }
    """return a failure when queried with getInt""" in {
      bob.xtractor.getInt("name").isFailure
    }
    """return a failure when queried with getDouble""" in {
      bob.xtractor.getDouble("name").isFailure
    }
    """return a success of "Bob" when queried with getString""" in {
      bob.xtractor.getString("name") must beEqualTo("Bob".successSBD)
    }
    """return a failure when queried with tryGetLong""" in {
      bob.xtractor.tryGetLong("name").isFailure
    }
    """return a failure when queried with tryGetInt""" in {
      bob.xtractor.tryGetInt("name").isFailure
    }
    """return a failure when queried with tryGetDouble""" in {
      bob.xtractor.tryGetDouble("name").isFailure
    }
    """return a success of Some("Bob") when queried with tryGetString""" in {
      bob.xtractor.tryGetString("name") must beEqualTo(Some("Bob").successSBD)
    }
    """return a failure when queried with getXTractor""" in {
      bob.xtractor.getXTractor("name").isFailure
    }
    """return a failure when queried with tryGetElement""" in {
      bob.xtractor.tryGetXTractor("name").isFailure
    }
    """return a failure when queried with getXTractors""" in {
      bob.xtractor.getXTractors("name").isFailure
    }
    """return a failure when queried with getAtomics""" in {
      bob.xtractor.getAtomics("name").isFailure
    }
  }

  """A XmlXTractor for Bob when queried for "age" which is an Int""" should {
    """return success 33L when queried with getLong""" in {
      bob.xtractor.getLong("age") must beEqualTo(33L.successSBD)
    }
    """return success 33 when queried with getInt""" in {
      bob.xtractor.getInt("age") must beEqualTo(33.successSBD)
    }
    """return success "33.0" when when queried with getDouble""" in {
      bob.xtractor.getDouble("age") must beEqualTo(33.0.successSBD)
    }
    """return success "33" when queried with getString""" in {
      bob.xtractor.getString("age") must beEqualTo("33".successSBD)
    }
    """return success Option 33L when queried with tryGetLong""" in {
      bob.xtractor.tryGetLong("age") must beEqualTo(Some(33L).successSBD)
    }
    """return success Option 33 when queried with tryGetInt""" in {
      bob.xtractor.tryGetInt("age") must beEqualTo(Some(33).successSBD)
    }
    """return success Option "33.0" when when queried with tryGetDouble""" in {
      bob.xtractor.tryGetDouble("age") must beEqualTo(Some(33.0).successSBD)
    }
    """return success Option "33" when queried with tryGetString""" in {
      bob.xtractor.tryGetString("age") must beEqualTo(Some("33").successSBD)
    }
    """return a failure when queried with getXTractor""" in {
      bob.xtractor.getXTractor("age").isFailure
    }
    """return a failure when queried with tryGetElement""" in {
      bob.xtractor.tryGetXTractor("age").isFailure
    }
    """return a failure when queried with getXTractors""" in {
      bob.xtractor.getXTractors("age").isFailure
    }
    """return a failue when queried with getAtomics""" in {
      bob.xtractor.getAtomics("age").isFailure
    }
  }

  """A XmlXTractor for Bob when queried for "dps" which is a Double""" should {
    """return a failure when queried with getLong""" in {
      bob.xtractor.getLong("dps").isFailure
    }
    """return a failure when queried with getInt""" in {
      bob.xtractor.getInt("dps").isFailure
    }
    """return a success of 1.37 when queried with getDouble""" in {
      bob.xtractor.getDouble("dps") must beEqualTo(1.37.successSBD)
    }
    """return success "33" when queried with getString""" in {
      bob.xtractor.getString("dps") must beEqualTo("1.37".successSBD)
    }
    """return a failure when queried with tryGetLong""" in {
      bob.xtractor.tryGetLong("dps").isFailure
    }
    """return a failurewhen queried with tryGetInt""" in {
      bob.xtractor.tryGetInt("dps").isFailure
    }
    """return a success of Some(1.37) when queried with tryGetDouble""" in {
      bob.xtractor.tryGetDouble("dps") must beEqualTo(Some(1.37).successSBD)
    }
    """return success Option "1.37" when queried with tryGetString""" in {
      bob.xtractor.tryGetString("dps") must beEqualTo(Some("1.37").successSBD)
    }
    """return a failure when queried with getXTractor""" in {
      bob.xtractor.getXTractor("dps").isFailure
    }
    """return a failure when queried with tryGetElement""" in {
      bob.xtractor.tryGetXTractor("dps").isFailure
    }
    """return a failure when queried with getXTractors""" in {
      bob.xtractor.getXTractors("dps").isFailure
    }
    """return a failure when queried with getAtomics""" in {
      bob.xtractor.getAtomics("dps").isFailure
    }
  }
  
  """A XmlXTractor for Bob when queried for "ageAsText" which is a String""" should {
    """return success 33L when queried with getLong""" in {
      bob.xtractor.getLong("ageAsText") must beEqualTo(33L.successSBD)
    }
    """return success 33 when queried with getInt""" in {
      bob.xtractor.getInt("ageAsText") must beEqualTo(33.successSBD)
    }
    """return success "33.0" when when queried with getDouble""" in {
      bob.xtractor.getDouble("ageAsText") must beEqualTo(33.0.successSBD)
    }
    """return success "33" when queried with getString""" in {
      bob.xtractor.getString("ageAsText") must beEqualTo("33".successSBD)
    }
    """return success Option 33L when queried with tryGetLong""" in {
      bob.xtractor.tryGetLong("ageAsText") must beEqualTo(Some(33L).successSBD)
    }
    """return success Option 33 when queried with tryGetInt""" in {
      bob.xtractor.tryGetInt("ageAsText") must beEqualTo(Some(33).successSBD)
    }
    """return success Option "33.0" when when queried with tryGetDouble""" in {
      bob.xtractor.tryGetDouble("ageAsText") must beEqualTo(Some(33.0).successSBD)
    }
    """return success Option "33" when queried with tryGetString""" in {
      bob.xtractor.tryGetString("ageAsText") must beEqualTo(Some("33").successSBD)
    }
    """return a failure when queried with getXTractor""" in {
      bob.xtractor.getXTractor("ageAsText").isFailure
    }
    """return a failure when queried with tryGetElement""" in {
      bob.xtractor.tryGetXTractor("ageAsText").isFailure
    }
    """return a failure when queried with getXTractors""" in {
      bob.xtractor.getXTractors("ageAsText").isFailure
    }
    """return a failure when queried with getAtomics""" in {
      bob.xtractor.getAtomics("ageAsText").isFailure
    }
  }

  """A XmlXTractor for Bob when queried for "spaces" which is a String of spaces:"  """" should {
    """return a failure when queried with getLong""" in {
      bob.xtractor.getLong("spaces").isFailure
    }
    """return a failure when queried with getInt""" in {
      bob.xtractor.getInt("spaces").isFailure
    }
    """return a failure when queried with getDouble""" in {
      bob.xtractor.getDouble("spaces").isFailure
    }
    """return a failure when queried with getString""" in {
      bob.xtractor.getString("spaces").isFailure
    }
    """return a success of None when queried with tryGetLong""" in {
      bob.xtractor.tryGetLong("spaces") must beEqualTo(None.successSBD)
    }
    """return a success of None when queried with tryGetInt""" in {
      bob.xtractor.tryGetInt("spaces") must beEqualTo(None.successSBD)
    }
    """return a success of None when queried with tryGetDouble""" in {
      bob.xtractor.tryGetDouble("spaces") must beEqualTo(None.successSBD)
    }
    """return a success of None when queried with tryGetString""" in {
      bob.xtractor.tryGetString("spaces") must beEqualTo(None.successSBD)
    }
    """return a failure when queried with getXTractor""" in {
      bob.xtractor.getXTractor("spaces").isFailure
    }
    """return a success of None when queried with tryGetElement""" in {
      bob.xtractor.tryGetXTractor("spaces") must beEqualTo(None.success)
    }
    """return a success of [] when queried with getXTractors""" in {
      bob.xtractor.getXTractors("spaces") must beEqualTo(Nil.success)
    }
    """return a success of [] when queried with getAtomics""" in {
      bob.xtractor.getAtomics("spaces") must beEqualTo(Nil.success)
    }
  }
  
  """A XmlXTractor for Bob when queried for "address" which is an Element"""" should {
    """return a failure when queried with getLong""" in {
      bob.xtractor.getLong("address").isFailure
    }
    """return a failure when queried with getInt""" in {
      bob.xtractor.getInt("address").isFailure
    }
    """return a failure when queried with getDouble""" in {
      bob.xtractor.getDouble("address").isFailure
    }
    """return a failure when queried with getString""" in {
      bob.xtractor.getString("address").isFailure
    }
    """return a failure when queried with tryGetLong""" in {
      bob.xtractor.tryGetLong("address").isFailure
    }
    """return a failure when queried with tryGetInt""" in {
      bob.xtractor.tryGetInt("address").isFailure
    }
    """return a failure when queried with tryGetDouble""" in {
      bob.xtractor.tryGetDouble("address").isFailure
    }
    """return a failure when queried with tryGetString""" in {
      bob.xtractor.tryGetString("address").isFailure
    }
    """return a success when queried with getXTractor""" in {
      bob.xtractor.getXTractor("address").isSuccess
    }
    """return a success when queried with getXTractors""" in {
      bob.xtractor.getXTractors("address").isSuccess
    }
    """return a success when queried with tryGetElement""" in {
      bob.xtractor.tryGetXTractor("address").isSuccess
    }
    """return a failure when queried with getAtomics""" in {
      bob.xtractor.getAtomics("address").isFailure
    }
    
  }
  

  """A XmlXTractor for Bob when queried for "scores" which is a Collection of Ints"""" should {
    """return a failure when queried with getLong""" in {
      bob.xtractor.getLong("scores").isFailure
    }
    """return a failure when queried with getInt""" in {
      bob.xtractor.getInt("scores").isFailure
    }
    """return a failure when queried with getDouble""" in {
      bob.xtractor.getDouble("scores").isFailure
    }
    """return a failure when queried with getString""" in {
      bob.xtractor.getString("scores").isFailure
    }
    """return a failure when queried with tryGetLong""" in {
      bob.xtractor.tryGetLong("scores").isFailure
    }
    """return a failure when queried with tryGetInt""" in {
      bob.xtractor.tryGetInt("scores").isFailure
    }
    """return a failure when queried with tryGetDouble""" in {
      bob.xtractor.tryGetDouble("scores").isFailure
    }
    """return a failure when queried with tryGetString""" in {
      bob.xtractor.tryGetString("scores").isFailure
    }
    """return a failure when queried with getXTractor because the collection has more than one element""" in {
      bob.xtractor.getXTractor("scores").isFailure
    }
    """return a failure when queried with getXTractors""" in {
      bob.xtractor.getXTractors("scores").isFailure
    }
    """return a failure when queried with tryGetElement because the collection has more than one element""" in {
      bob.xtractor.tryGetXTractor("scores").isFailure
    }
    """return a success when queried with getAtomics""" in {
      bob.xtractor.getAtomics("scores").isSuccess
    }
    """return a success with a list of 1 to 10 when queried with getAtomicsEvaluated""" in {
      bob.xtractor.getAtomicsEvaluated("scores", x => x.getInt) must beEqualTo((List(1 to 10: _*)).success)
    }
  }
  
  
  """A XmlXTractor for Bob when queried for "gameTimes" which is a Collection of Elements"""" should {
    """return a failure when queried with getLong""" in {
      bob.xtractor.getLong("gameTimes").isFailure
    }
    """return a failure when queried with getInt""" in {
      bob.xtractor.getInt("gameTimes").isFailure
    }
    """return a failure when queried with getDouble""" in {
      bob.xtractor.getDouble("gameTimes").isFailure
    }
    """return a failure when queried with getString""" in {
      bob.xtractor.getString("gameTimes").isFailure
    }
    """return a failure when queried with tryGetLong""" in {
      bob.xtractor.tryGetLong("gameTimes").isFailure
    }
    """return a failure when queried with tryGetInt""" in {
      bob.xtractor.tryGetInt("gameTimes").isFailure
    }
    """return a failure when queried with tryGetDouble""" in {
      bob.xtractor.tryGetDouble("gameTimes").isFailure
    }
    """return a failure when queried with tryGetString""" in {
      bob.xtractor.tryGetString("gameTimes").isFailure
    }
    """return a failure when queried with getXTractor""" in {
      bob.xtractor.getXTractor("gameTimes").isFailure
    }
    """return a success when queried with getXTractors""" in {
      bob.xtractor.getXTractors("gameTimes").isSuccess
    }
    """return a failure when queried with tryGetElement""" in {
      bob.xtractor.tryGetXTractor("gameTimes").isFailure
    }
    """return a failure when queried with getAtomics""" in {
      bob.xtractor.getAtomics("gameTimes").isFailure
    }
  }

  """A XmlXTractor for Bob when queried for the non-existing key "doesNotExist""""" should {
    """return a failure when queried with getLong""" in {
      bob.xtractor.getLong("doesNotExist").isFailure
    }
    """return a failure when queried with getInt""" in {
      bob.xtractor.getInt("doesNotExist").isFailure
    }
    """return a failure when queried with getDouble""" in {
      bob.xtractor.getDouble("doesNotExist").isFailure
    }
    """return a failure when queried with getString""" in {
      bob.xtractor.getString("doesNotExist").isFailure
    }
    """return a success of None when queried with tryGetInt""" in {
      bob.xtractor.tryGetInt("doesNotExist") must beEqualTo(None.successSBD)
    }
    """return a success of None when queried with tryGetLong""" in {
      bob.xtractor.tryGetLong("doesNotExist") must beEqualTo(None.successSBD)
    }
    """return a success of None when queried with tryGetDouble""" in {
      bob.xtractor.tryGetDouble("doesNotExist") must beEqualTo(None.successSBD)
    }
    """return a success of None when queried with tryGetString""" in {
      bob.xtractor.tryGetString("doesNotExist") must beEqualTo(None.successSBD)
    }
    """return a failure when queried with getXTractor""" in {
      bob.xtractor.getXTractor("doesNotExist").isFailure
    }
    """return a success of None when queried with tryGetElement""" in {
      bob.xtractor.tryGetXTractor("doesNotExist") must beEqualTo(None.successSBD)
    }
    """return a success of [] when queried with getXTractors""" in {
      bob.xtractor.getXTractors("doesNotExist") must beEqualTo(Nil.successSBD)
    }
    """return a success of [] when queried with getAtomics""" in {
      bob.xtractor.getAtomics("doesNotExist") must beEqualTo(Nil.successSBD)
    }
  }

  """The address of Bob""" should {
    """contain a street "Downing Street"""" in {
      val xtractor = bob.xtractor
      val address = xtractor.getXTractor("address")
      val street = address.bind{_.getString("street")}
      street must beEqualTo(("Downing Street").success)
    } 
    """contain a city "London"""" in {
      val xtractor = bob.xtractor
      val address = xtractor.getXTractor("address")
      val city = address.bind{_.getString("city")}
      city must beEqualTo("London".success)
    } 
  }
}