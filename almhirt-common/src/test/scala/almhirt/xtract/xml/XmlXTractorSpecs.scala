package almhirt.xtract.xml

import org.specs2.mutable._
import scalaz.Success
import almhirt.validation.AlmValidation._

object XmlXTractorSpecsSamples {
  val bob = 
    <Bob>
	  <id>0</id>
	  <name>Bob</name>
	  <age>33</age>
	  <dps>1.37</dps>
	  <ageAsText>33</ageAsText>
	  <spaces>  </spaces>
	  <address>
	    <street>Downing Street</street>
	    <city>London</city>
      </address>
	  <scores>
        <value>1</value>
        <value>2</value>
        <value>3</value>
        <value>4</value>
        <value>5</value>
        <value>6</value>
        <value>7</value>
        <value>8</value>
        <value>9</value>
        <value>10</value>
	  </scores>
	  <gameTimes>
          <value><aoe>12.3</aoe></value>
          <value><eve>29.1</eve></value>
          <value><pacman>1229.1</pacman></value>
	  </gameTimes>
    </Bob>
}

class XmlXTractorSpecs extends Specification {
  import XmlXTractor._
  import XmlXTractorSpecsSamples._

  """A XmlXTractor for Bob when queried for "id"(PK!) which is a Long""" should {
    """return success 0L when queried with getLong""" in {
      bob.xtractor.getLong("id") must beEqualTo(0L.successSingleBadData)
    }
    """return success 0 when queried with getInt""" in {
      bob.xtractor.getInt("id") must beEqualTo(0.successSingleBadData)
    }
    """return success 0.0 when when queried with getDouble""" in {
      bob.xtractor.getDouble("id") must beEqualTo(0.0.successSingleBadData)
    }
    """return success "0" when queried with getString""" in {
      bob.xtractor.getString("id") must beEqualTo("0".successSingleBadData)
    }
    """return success Some(0L) when queried with tryGetLong""" in {
      bob.xtractor.tryGetLong("id") must beEqualTo(Some(0L).successSingleBadData)
    }
    """return success Some(0) when when queried with tryGetInt""" in {
      bob.xtractor.tryGetInt("id") must beEqualTo(Some(0).successSingleBadData)
    }
    """return success Some(0.0) when when queried with tryGetDouble""" in {
      bob.xtractor.tryGetDouble("id") must beEqualTo(Some(0.0).successSingleBadData)
    }
    """return success Some("0") when queried with tryGetString""" in {
      bob.xtractor.tryGetString("id") must beEqualTo(Some("0").successSingleBadData)
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
      bob.xtractor.getString("name") must beEqualTo("Bob".successSingleBadData)
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
      bob.xtractor.tryGetString("name") must beEqualTo(Some("Bob").successSingleBadData)
    }
    """return a failure when queried with getElement""" in {
      bob.xtractor.getElement("name").isFailure
    }
    """return a success of None when queried with tryGetElement""" in {
      bob.xtractor.tryGetElement("name") must beEqualTo(Success(None))
    }
    """return a failure when queried with getAtomics""" in {
      bob.xtractor.getAtomics("name").isFailure
    }
  }

  """A XmlXTractor for Bob when queried for "age" which is an Int""" should {
    """return a failure when queried with getLong""" in {
      bob.xtractor.getLong("age").isFailure
    }
    """return a success of 33 when queried with getInt""" in {
      bob.xtractor.getInt("age") must beEqualTo(33.successSingleBadData)
    }
    """return a failure when queried with getDouble""" in {
      bob.xtractor.getDouble("age").isFailure
    }
    """return a failure when queried with getString""" in {
      bob.xtractor.getString("age").isFailure
    }
    """return a failure when queried with tryGetLong""" in {
      bob.xtractor.tryGetLong("age").isFailure
    }
    """return a success of Some(33) when queried with tryGetInt""" in {
      bob.xtractor.tryGetInt("age") must beEqualTo(Some(33).successSingleBadData)
    }
    """return a failure when queried with tryGetDouble""" in {
      bob.xtractor.tryGetDouble("age").isFailure
    }
    """return a failure when queried with tryGetString""" in {
      bob.xtractor.tryGetString("age").isFailure
    }
    """return a failure when queried with getElement""" in {
      bob.xtractor.getElement("age").isFailure
    }
    """return a success of None when queried with tryGetElement""" in {
      bob.xtractor.tryGetElement("age") must beEqualTo(Success(None))
    }
    """return a failure when queried with getAtomics""" in {
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
      bob.xtractor.getDouble("dps") must beEqualTo(1.37.successSingleBadData)
    }
    """return a failure when queried with getString""" in {
      bob.xtractor.getString("dps").isFailure
    }
    """return a failure when queried with tryGetLong""" in {
      bob.xtractor.tryGetLong("dps").isFailure
    }
    """return a failurewhen queried with tryGetInt""" in {
      bob.xtractor.tryGetInt("dps").isFailure
    }
    """return a success of Some(1.37) when queried with tryGetDouble""" in {
      bob.xtractor.tryGetDouble("dps") must beEqualTo(Some(1.37).successSingleBadData)
    }
    """return a failure when queried with tryGetString""" in {
      bob.xtractor.tryGetString("dps").isFailure
    }
    """return a failure when queried with getElement""" in {
      bob.xtractor.getElement("dps").isFailure
    }
    """return a success of None when queried with tryGetElement""" in {
      bob.xtractor.tryGetElement("dps") must beEqualTo(Success(None))
    }
    """return a failure when queried with getAtomics""" in {
      bob.xtractor.getAtomics("dps").isFailure
    }
  }
  
  """A XmlXTractor for Bob when queried for "ageAsText" which is a String""" should {
    """return a failure when queried with getLong""" in {
      bob.xtractor.getLong("ageAsText").isFailure
    }
    """return a failure when queried with getInt""" in {
      bob.xtractor.getInt("ageAsText").isFailure
    }
    """return a failure when queried with getDouble""" in {
      bob.xtractor.getDouble("ageAsText").isFailure
    }
    """return a success of "Bob" when queried with getString""" in {
      bob.xtractor.getString("ageAsText") must beEqualTo("33".successSingleBadData)
    }
    """return a failure when queried with tryGetLong""" in {
      bob.xtractor.tryGetLong("ageAsText").isFailure
    }
    """return a failure when queried with tryGetInt""" in {
      bob.xtractor.tryGetInt("ageAsText").isFailure
    }
    """return a failure when queried with tryGetDouble""" in {
      bob.xtractor.tryGetDouble("ageAsText").isFailure
    }
    """return a success of Some("Bob") when queried with tryGetString""" in {
      bob.xtractor.tryGetString("ageAsText") must beEqualTo(Some("33").successSingleBadData)
    }
    """return a failure when queried with getElement""" in {
      bob.xtractor.getElement("ageAsText").isFailure
    }
    """return a success of None when queried with tryGetElement""" in {
      bob.xtractor.tryGetElement("ageAsText") must beEqualTo(Success(None))
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
    """return a failure when queried with tryGetLong""" in {
      bob.xtractor.tryGetLong("spaces").isFailure
    }
    """return a failure when queried with tryGetInt""" in {
      bob.xtractor.tryGetInt("spaces").isFailure
    }
    """return a failure when queried with tryGetDouble""" in {
      bob.xtractor.tryGetDouble("spaces").isFailure
    }
    """return a success of None when queried with tryGetString""" in {
      bob.xtractor.tryGetString("spaces") must beEqualTo(None.successSingleBadData)
    }
    """return a failure when queried with getElement""" in {
      bob.xtractor.getElement("spaces").isFailure
    }
    """return a success of None when queried with tryGetElement""" in {
      bob.xtractor.tryGetElement("spaces") must beEqualTo(Success(None))
    }
    """return a failure when queried with getAtomics""" in {
      bob.xtractor.getAtomics("spaces").isFailure
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
    """return a failure when queried with getElement""" in {
      bob.xtractor.getElement("address").isSuccess
    }
    """return a failure when queried with getElements""" in {
      bob.xtractor.getElements("address").isFailure
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
    """return a failure when queried with tryGetElement""" in {
      bob.xtractor.tryGetElement("address").isSuccess
    }
    """return a success when queried with getAtomics""" in {
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
    """return a failure when queried with getElement""" in {
      bob.xtractor.getElement("scores").isFailure
    }
    """return a failure when queried with getElements""" in {
      bob.xtractor.getElements("scores").isFailure
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
    """return a failure when queried with tryGetElement""" in {
      bob.xtractor.tryGetElement("scores").isFailure
    }
    """return a success when queried with getAtomics""" in {
      bob.xtractor.getAtomics("scores").isSuccess
    }
    """return a success with a list of 1 to 10 when queried with getAtomicsEvaluated""" in {
      val res = bob.xtractor.getAtomicsEvaluated("scores", x => x.getInt())
      res must beEqualTo(Success(List(1 to 10: _*)))
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
    """return a failure when queried with getElement""" in {
      bob.xtractor.getElement("gameTimes").isFailure
    }
    """return a success when queried with getElements""" in {
      bob.xtractor.getElements("gameTimes").isSuccess
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
    """return a failure when queried with tryGetElement""" in {
      bob.xtractor.tryGetElement("gameTimes").isFailure
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
    """return a failure when queried with getElement""" in {
      bob.xtractor.getElement("doesNotExist").isFailure
    }
    """return a success of None when queried with tryGetInt""" in {
      bob.xtractor.tryGetInt("doesNotExist") must beEqualTo(None.successSingleBadData)
    }
    """return a success of None when queried with tryGetLong""" in {
      bob.xtractor.tryGetLong("doesNotExist") must beEqualTo(None.successSingleBadData)
    }
    """return a success of None when queried with tryGetDouble""" in {
      bob.xtractor.tryGetDouble("doesNotExist") must beEqualTo(None.successSingleBadData)
    }
    """return a success of None when queried with tryGetString""" in {
      bob.xtractor.tryGetString("doesNotExist") must beEqualTo(None.successSingleBadData)
    }
    """return a success of None when queried with tryGetElement""" in {
      bob.xtractor.tryGetElement("doesNotExist") must beEqualTo(None.successSingleBadData)
    }
    """return a success of [] when queried with getElements""" in {
      bob.xtractor.getElements("doesNotExist") must beEqualTo(Nil.successSingleBadData)
    }
    """return a success of [] when queried with getAtomics""" in {
      bob.xtractor.getAtomics("doesNotExist") must beEqualTo(Nil.successSingleBadData)
    }
  }

  """The address of Bob""" should {
    """contain a street "Downing Street"""" in {
      val xtractor = bob.xtractor
      val address = xtractor.getElement("address")
      val street = address.flatMap{_.getString("street")}
      street must beEqualTo(Success("Downing Street"))
    } 
    """contain a city "London"""" in {
      val xtractor = bob.xtractor
      val address = xtractor.getElement("address")
      val city = address.flatMap{_.getString("city")}
      city must beEqualTo(Success("London"))
    } 
  }
}