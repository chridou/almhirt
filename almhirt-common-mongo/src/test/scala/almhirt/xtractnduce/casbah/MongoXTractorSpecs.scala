/* Copyright 2012 Christian Douven

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
*/
package almhirt.xtractnduce.casbah

import org.specs2.mutable._
import scalaz._, Scalaz._
import almhirt.syntax.almvalidation._

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
    builder += "address" -> MongoDBObject("street" -> "Downing Street", "city" -> "London").asDBObject.asInstanceOf[BasicDBObject]
    builder += "scores" -> MongoDBList(1 to 10: _*)
    builder += "gameTimes" -> MongoDBList(MongoDBObject("aoe" -> 12.3), MongoDBObject("eve" -> 29.1), MongoDBObject("pacman" -> 1229.1))
    builder.result
  }
}

class MongoXTractorSpecs extends Specification {
  import MongoXTractor._
  import MongoXTractorSpecsSamples._

  """A MongoXTractor for Bob using the default KeyMapper when queried for "id"(PK!) which is a Long""" should {
    """return success 0L when queried with getLong""" in {
      bob.xtractor("Bob").getLong("id") must beEqualTo(0L.successSBD)
    }
    """return a failure when when queried with getLong""" in {
      bob.xtractor("Bob").getLong("_id") must beEqualTo(0L.successSBD)
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
      bob.xtractor("Bob").tryGetLong("id") must beEqualTo(Some(0L).successSBD)
    }
    """return success Some(0L) when queried with tryGetLong""" in {
      bob.xtractor("Bob").tryGetLong("_id") must beEqualTo(Some(0L).successSBD)
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
      bob.xtractor("Bob").getString("name") must beEqualTo("Bob".successSBD)
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
      bob.xtractor("Bob").tryGetString("name") must beEqualTo(Some("Bob").successSBD)
    }
    """return a failure when queried with getXTractor""" in {
      bob.xtractor("Bob").getXTractor("name").isFailure
    }
    """return a failure when queried with tryGetXTractor""" in {
      bob.xtractor("Bob").tryGetXTractor("name").isFailure
    }
    """return a failure when queried with getAtomics""" in {
      bob.xtractor("Bob").getAtomics("name").isFailure
    }
  }

  """A MongoXTractor for Bob using the default KeyMapper when queried for "age" which is an Int""" should {
    """return a failure when queried with getLong""" in {
      bob.xtractor("Bob").getLong("age").isFailure
    }
    """return a success of 33 when queried with getInt""" in {
      bob.xtractor("Bob").getInt("age") must beEqualTo(33.successSBD)
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
      bob.xtractor("Bob").tryGetInt("age") must beEqualTo(Some(33).successSBD)
    }
    """return a failure when queried with tryGetDouble""" in {
      bob.xtractor("Bob").tryGetDouble("age").isFailure
    }
    """return a failure when queried with tryGetString""" in {
      bob.xtractor("Bob").tryGetString("age").isFailure
    }
    """return a failure when queried with getXTractor""" in {
      bob.xtractor("Bob").getXTractor("age").isFailure
    }
    """return a failure when queried with tryGetXTractor""" in {
      bob.xtractor("Bob").tryGetXTractor("age").isFailure
    }
    """return a failure when queried with getAtomics""" in {
      bob.xtractor("Bob").getAtomics("age").isFailure
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
      bob.xtractor("Bob").getDouble("dps") must beEqualTo(1.37.successSBD)
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
      bob.xtractor("Bob").tryGetDouble("dps") must beEqualTo(Some(1.37).successSBD)
    }
    """return a failure when queried with tryGetString""" in {
      bob.xtractor("Bob").tryGetString("dps").isFailure
    }
    """return a failure when queried with getXTractor""" in {
      bob.xtractor("Bob").getXTractor("dps").isFailure
    }
    """return a failure when queried with tryGetXTractor""" in {
      bob.xtractor("Bob").tryGetXTractor("dps").isFailure
    }
    """return a failure when queried with getAtomics""" in {
      bob.xtractor("Bob").getAtomics("dps").isFailure
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
      bob.xtractor("Bob").getString("ageAsText") must beEqualTo("33".successSBD)
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
      bob.xtractor("Bob").tryGetString("ageAsText") must beEqualTo(Some("33").successSBD)
    }
    """return a failure when queried with getXTractor""" in {
      bob.xtractor("Bob").getXTractor("ageAsText").isFailure
    }
    """return a failure when queried with tryGetXTractor""" in {
      bob.xtractor("Bob").tryGetXTractor("ageAsText").isFailure
    }
    """return a failure when queried with getAtomics""" in {
      bob.xtractor("Bob").getAtomics("ageAsText").isFailure
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
      bob.xtractor("Bob").tryGetString("spaces") must beEqualTo(None.successSBD)
    }
    """return a failure when queried with getXTractor""" in {
      bob.xtractor("Bob").getXTractor("spaces").isFailure
    }
    """return a failure when queried with tryGetXTractor""" in {
      bob.xtractor("Bob").tryGetXTractor("spaces").isFailure
    }
    """return a failure when queried with getAtomics""" in {
      bob.xtractor("Bob").getAtomics("spaces").isFailure
    }
  }
  
  """A MongoXTractor for Bob using the default KeyMapper when queried for "address" which is a MongoDBObject"""" should {
    """return a failure when queried with getLong""" in {
      bob.xtractor("Bob").getLong("address").isFailure
    }
    """return a failure when queried with getInt""" in {
      bob.xtractor("Bob").getInt("address").isFailure
    }
    """return a failure when queried with getDouble""" in {
      bob.xtractor("Bob").getDouble("address").isFailure
    }
    """return a failure when queried with getString""" in {
      bob.xtractor("Bob").getString("address").isFailure
    }
    """return a failure when queried with tryGetLong""" in {
      bob.xtractor("Bob").tryGetLong("address").isFailure
    }
    """return a failure when queried with tryGetInt""" in {
      bob.xtractor("Bob").tryGetInt("address").isFailure
    }
    """return a failure when queried with tryGetDouble""" in {
      bob.xtractor("Bob").tryGetDouble("address").isFailure
    }
    """return a failure when queried with tryGetString""" in {
      bob.xtractor("Bob").tryGetString("address").isFailure
    }
    """return a failure when queried with getXTractor""" in {
      bob.xtractor("Bob").getXTractor("address").isSuccess
    }
    """return a failure when queried with getXTractors""" in {
      bob.xtractor("Bob").getXTractors("address").isFailure
    }
    """return a failure when queried with tryGetXTractor""" in {
      bob.xtractor("Bob").tryGetXTractor("address").isSuccess
    }
    """return a success when queried with getAtomics""" in {
      bob.xtractor("Bob").getAtomics("address").isFailure
    }
    
  }
  

  """A MongoXTractor for Bob using the default KeyMapper when queried for "scores" which is a Collection of Ints"""" should {
    """return a failure when queried with getLong""" in {
      bob.xtractor("Bob").getLong("scores").isFailure
    }
    """return a failure when queried with getInt""" in {
      bob.xtractor("Bob").getInt("scores").isFailure
    }
    """return a failure when queried with getDouble""" in {
      bob.xtractor("Bob").getDouble("scores").isFailure
    }
    """return a failure when queried with getString""" in {
      bob.xtractor("Bob").getString("scores").isFailure
    }
    """return a failure when queried with tryGetLong""" in {
      bob.xtractor("Bob").tryGetLong("scores").isFailure
    }
    """return a failure when queried with tryGetInt""" in {
      bob.xtractor("Bob").tryGetInt("scores").isFailure
    }
    """return a failure when queried with tryGetDouble""" in {
      bob.xtractor("Bob").tryGetDouble("scores").isFailure
    }
    """return a failure when queried with tryGetString""" in {
      bob.xtractor("Bob").tryGetString("scores").isFailure
    }
    """return a failure when queried with getXTractor""" in {
      bob.xtractor("Bob").getXTractor("scores").isFailure
    }
    """return a failure when queried with getXTractors""" in {
      bob.xtractor("Bob").getXTractors("scores").isFailure
    }
    """return a failure when queried with tryGetXTractor""" in {
      bob.xtractor("Bob").tryGetXTractor("scores").isFailure
    }
    """return a success when queried with getAtomics""" in {
      bob.xtractor("Bob").getAtomics("scores").isSuccess
    }
    """return a success with a list of 1 to 10 when queried with getAtomicsEvaluated""" in {
      val res = bob.xtractor("Bob").getAtomicsEvaluated("scores", x => x.getInt())
      res must beEqualTo(List(1 to 10: _*).success)
    }
  }
  
  
  """A MongoXTractor for Bob using the default KeyMapper when queried for "gameTimes" which is a Collection of MongoDBObjects"""" should {
    """return a failure when queried with getLong""" in {
      bob.xtractor("Bob").getLong("gameTimes").isFailure
    }
    """return a failure when queried with getInt""" in {
      bob.xtractor("Bob").getInt("gameTimes").isFailure
    }
    """return a failure when queried with getDouble""" in {
      bob.xtractor("Bob").getDouble("gameTimes").isFailure
    }
    """return a failure when queried with getString""" in {
      bob.xtractor("Bob").getString("gameTimes").isFailure
    }
    """return a failure when queried with tryGetLong""" in {
      bob.xtractor("Bob").tryGetLong("gameTimes").isFailure
    }
    """return a failure when queried with tryGetInt""" in {
      bob.xtractor("Bob").tryGetInt("gameTimes").isFailure
    }
    """return a failure when queried with tryGetDouble""" in {
      bob.xtractor("Bob").tryGetDouble("gameTimes").isFailure
    }
    """return a failure when queried with tryGetString""" in {
      bob.xtractor("Bob").tryGetString("gameTimes").isFailure
    }
    """return a failure when queried with getXTractor""" in {
      bob.xtractor("Bob").getXTractor("gameTimes").isFailure
    }
    """return a success when queried with getXTractors""" in {
      bob.xtractor("Bob").getXTractors("gameTimes").isSuccess
    }
    """return a failure when queried with tryGetXTractor""" in {
      bob.xtractor("Bob").tryGetXTractor("gameTimes").isFailure
    }
    """return a failure when queried with getAtomics""" in {
      bob.xtractor("Bob").getAtomics("gameTimes").isFailure
    }
  }

  """A MongoXTractor for Bob using the default KeyMapper when queried for the non-existing key "doesNotExist""""" should {
    """return a failure when queried with getLong""" in {
      bob.xtractor("Bob").getLong("doesNotExist").isFailure
    }
    """return a failure when queried with getInt""" in {
      bob.xtractor("Bob").getInt("doesNotExist").isFailure
    }
    """return a failure when queried with getDouble""" in {
      bob.xtractor("Bob").getDouble("doesNotExist").isFailure
    }
    """return a failure when queried with getString""" in {
      bob.xtractor("Bob").getString("doesNotExist").isFailure
    }
   """return a success of None when queried with tryGetInt""" in {
      bob.xtractor("Bob").tryGetInt("doesNotExist") must beEqualTo(None.successSBD)
    }
    """return a success of None when queried with tryGetLong""" in {
      bob.xtractor("Bob").tryGetLong("doesNotExist") must beEqualTo(None.successSBD)
    }
    """return a success of None when queried with tryGetDouble""" in {
      bob.xtractor("Bob").tryGetDouble("doesNotExist") must beEqualTo(None.successSBD)
    }
    """return a success of None when queried with tryGetString""" in {
      bob.xtractor("Bob").tryGetString("doesNotExist") must beEqualTo(None.successSBD)
    }
    """return a failure when queried with getXTractor""" in {
      bob.xtractor("Bob").getXTractor("doesNotExist").isFailure
    }
     """return a success of None when queried with tryGetXTractor""" in {
      bob.xtractor("Bob").tryGetXTractor("doesNotExist") must beEqualTo(None.successSBD)
    }
    """return a success of [] when queried with getXTractors""" in {
      bob.xtractor("Bob").getXTractors("doesNotExist") must beEqualTo(Nil.successSBD)
    }
    """return a success of [] when queried with getAtomics""" in {
      bob.xtractor("Bob").getAtomics("doesNotExist") must beEqualTo(Nil.successSBD)
    }
  }

  """The address of Bob""" should {
    """contain a street "Downing Street"""" in {
      val xtractor = bob.xtractor("Bob")
      val address = xtractor.getXTractor("address")
      val street = address.bind{_.getString("street")}
      street must beEqualTo("Downing Street".success)
    } 
    """contain a city "London"""" in {
      val xtractor = bob.xtractor("Bob")
      val address = xtractor.getXTractor("address")
      val city = address.bind{_.getString("city")}
      city must beEqualTo("London".success)
    } 
  }
  
}