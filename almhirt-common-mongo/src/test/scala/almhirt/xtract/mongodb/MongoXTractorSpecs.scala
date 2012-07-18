package almhirt.xtract.mongodb

import org.specs2.mutable._
import almhirt.validation.AlmValidation._
import com.mongodb.casbah.Imports._

object MongoXTractorSpecsSamples {
  val bob: MongoDBObject = {
    val builder = MongoDBObject.newBuilder
    builder += "_id" -> 0L
    builder += "name" -> "Bob"
    builder += "age" -> 33
    builder += "dps" -> 1.37
    builder += "ageAsText" -> "33"
    builder += "spaces" -> "  "
    builder += "scores" -> MongoDBList(1 to 10)
    builder += "gameTimes" -> MongoDBList(MongoDBObject("aoe" -> 12.3), MongoDBObject("eve" -> 29.1), MongoDBObject("pacman" -> 1229.1))
    builder.result
  }
}

class MongoXTractorSpecs extends Specification {
  import MongoXTractor._
  import MongoXTractorSpecsSamples._

  """A MongoXTractor for Bob using the default KeyMapper when queried for "id"(PK!) which is a Long""" should {
    """return success 0L when queried with getLong""" in {
      bob.xtractor("Bob").getLong("id") must beEqualTo(0L.successSingleBadData)
    }
    """return a failure when when queried with getLong""" in {
      bob.xtractor("Bob").getLong("_id") must beEqualTo(0L.successSingleBadData)
    }
    """return a failure when when queried with getInt""" in {
      bob.xtractor("Bob").getInt("id").isFailure
    }
    """return a failure when when queried with getDouble""" in {
      bob.xtractor("Bob").getDouble("id").isFailure
    }
    """return a failure when queried with getString""" in {
      bob.xtractor("Bob").getString("id").isFailure
    }
    """return success Some(0L) when queried with tryGetLong""" in {
      bob.xtractor("Bob").tryGetLong("id") must beEqualTo(Some(0L).successSingleBadData)
    }
    """return success Some(0L) when queried with tryGetLong""" in {
      bob.xtractor("Bob").tryGetLong("_id") must beEqualTo(Some(0L).successSingleBadData)
    }
    """return a failure when when queried with tryGetInt""" in {
      bob.xtractor("Bob").tryGetInt("id").isFailure
    }
    """return a failure when when queried with tryGetDouble""" in {
      bob.xtractor("Bob").tryGetDouble("id").isFailure
    }
    """return a failure when queried with tryGetString""" in {
      bob.xtractor("Bob").tryGetString("id").isFailure
    }
  }
  
  """A MongoXTractor for Bob using the default KeyMapper when queried for "name" which is a String""" should {
    """return a failure when queried with getLong""" in {
      bob.xtractor("Bob").getLong("name").isFailure
    }
    """return a failure when queried with getInt""" in {
      bob.xtractor("Bob").getInt("name").isFailure
    }
    """return a failure when queried with getDouble""" in {
      bob.xtractor("Bob").getDouble("name").isFailure
    }
    """return a success of "Bob" when queried with getString""" in {
      bob.xtractor("Bob").getString("name") must beEqualTo("Bob".successSingleBadData)
    }
    """return a failure when queried with tryGetLong""" in {
      bob.xtractor("Bob").tryGetLong("name").isFailure
    }
    """return a failure when queried with tryGetInt""" in {
      bob.xtractor("Bob").tryGetInt("name").isFailure
    }
    """return a failure when queried with tryGetDouble""" in {
      bob.xtractor("Bob").tryGetDouble("name").isFailure
    }
    """return a success of Some("Bob") when queried with tryGetString""" in {
      bob.xtractor("Bob").tryGetString("name") must beEqualTo(Some("Bob").successSingleBadData)
    }
  }

  """A MongoXTractor for Bob using the default KeyMapper when queried for "age" which is an Int""" should {
    """return a failure when queried with getLong""" in {
      bob.xtractor("Bob").getLong("age").isFailure
    }
    """return a success of 33 when queried with getInt""" in {
      bob.xtractor("Bob").getInt("age") must beEqualTo(33.successSingleBadData)
    }
    """return a failure when queried with getDouble""" in {
      bob.xtractor("Bob").getDouble("age").isFailure
    }
    """return a failure when queried with getString""" in {
      bob.xtractor("Bob").getString("age").isFailure
    }
    """return a failure when queried with tryGetLong""" in {
      bob.xtractor("Bob").tryGetLong("age").isFailure
    }
    """return a success of Some(33) when queried with tryGetInt""" in {
      bob.xtractor("Bob").tryGetInt("age") must beEqualTo(Some(33).successSingleBadData)
    }
    """return a failure when queried with tryGetDouble""" in {
      bob.xtractor("Bob").tryGetDouble("age").isFailure
    }
    """return a failure when queried with tryGetString""" in {
      bob.xtractor("Bob").tryGetString("age").isFailure
    }
  }

  """A MongoXTractor for Bob using the default KeyMapper when queried for "dps" which is a Double""" should {
    """return a failure when queried with getLong""" in {
      bob.xtractor("Bob").getLong("dps").isFailure
    }
    """return a failure when queried with getInt""" in {
      bob.xtractor("Bob").getInt("dps").isFailure
    }
    """return a success of 1.37 when queried with getDouble""" in {
      bob.xtractor("Bob").getDouble("dps") must beEqualTo(1.37.successSingleBadData)
    }
    """return a failure when queried with getString""" in {
      bob.xtractor("Bob").getString("dps").isFailure
    }
    """return a failure when queried with tryGetLong""" in {
      bob.xtractor("Bob").tryGetLong("dps").isFailure
    }
    """return a failurewhen queried with tryGetInt""" in {
      bob.xtractor("Bob").tryGetInt("dps").isFailure
    }
    """return a success of Some(1.37) when queried with tryGetDouble""" in {
      bob.xtractor("Bob").tryGetDouble("dps") must beEqualTo(Some(1.37).successSingleBadData)
    }
    """return a failure when queried with tryGetString""" in {
      bob.xtractor("Bob").tryGetString("dps").isFailure
    }
  }
  
  """A MongoXTractor for Bob using the default KeyMapper when queried for "ageAsText" which is a String""" should {
    """return a failure when queried with getLong""" in {
      bob.xtractor("Bob").getLong("ageAsText").isFailure
    }
    """return a failure when queried with getInt""" in {
      bob.xtractor("Bob").getInt("ageAsText").isFailure
    }
    """return a failure when queried with getDouble""" in {
      bob.xtractor("Bob").getDouble("ageAsText").isFailure
    }
    """return a success of "Bob" when queried with getString""" in {
      bob.xtractor("Bob").getString("ageAsText") must beEqualTo("33".successSingleBadData)
    }
    """return a failure when queried with tryGetLong""" in {
      bob.xtractor("Bob").tryGetLong("ageAsText").isFailure
    }
    """return a failure when queried with tryGetInt""" in {
      bob.xtractor("Bob").tryGetInt("ageAsText").isFailure
    }
    """return a failure when queried with tryGetDouble""" in {
      bob.xtractor("Bob").tryGetDouble("ageAsText").isFailure
    }
    """return a success of Some("Bob") when queried with tryGetString""" in {
      bob.xtractor("Bob").tryGetString("ageAsText") must beEqualTo(Some("33").successSingleBadData)
    }
  }

  """A MongoXTractor for Bob using the default KeyMapper when queried for "spaces" which is a String of spaces:"  """" should {
    """return a failure when queried with getLong""" in {
      bob.xtractor("Bob").getLong("spaces").isFailure
    }
    """return a failure when queried with getInt""" in {
      bob.xtractor("Bob").getInt("spaces").isFailure
    }
    """return a failure when queried with getDouble""" in {
      bob.xtractor("Bob").getDouble("spaces").isFailure
    }
    """return a failure when queried with getString""" in {
      bob.xtractor("Bob").getString("spaces").isFailure
    }
    """return a failure when queried with tryGetLong""" in {
      bob.xtractor("Bob").tryGetLong("spaces").isFailure
    }
    """return a failure when queried with tryGetInt""" in {
      bob.xtractor("Bob").tryGetInt("spaces").isFailure
    }
    """return a failure when queried with tryGetDouble""" in {
      bob.xtractor("Bob").tryGetDouble("spaces").isFailure
    }
    """return a success of None when queried with tryGetString""" in {
      bob.xtractor("Bob").tryGetString("spaces") must beEqualTo(None.successSingleBadData)
    }
  }
  
}