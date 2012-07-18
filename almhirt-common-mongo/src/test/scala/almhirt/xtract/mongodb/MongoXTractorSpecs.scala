package almhirt.xtract.mongodb

import org.specs2.mutable._
import almhirt.validation.AlmValidation._
import com.mongodb.casbah.Imports._

class MongoXTractorSpecs extends Specification {
  import MongoXTractor._
  val bob: MongoDBObject = {
    val builder = MongoDBObject.newBuilder
    builder += "_id" -> 0L
    builder += "name" -> "Bob"
    builder += "age" -> 33
    builder += "dps" -> 1.37
    builder += "scores" -> MongoDBList(1 to 10)
    builder += "gameTimes" -> MongoDBList(MongoDBObject("aoe" -> 12.3), MongoDBObject("eve" -> 29.1), MongoDBObject("pacman" -> 1229.1))
    builder.result
  }

  """A MongoXTractor for Bob""" should {
    """return success 0L when queried for "id" with getLong""" in {
      bob.xtractor("Bob") must beEqualTo(0L.successMultipleBadData)
    }
  }
}