package almhirt.xtractnduce.xml

import scala.xml.Utility._
import XmlNDucer._
import org.specs2.mutable._

class XmlNDucerSpecs extends Specification {
  import XTractNDuceSamples._
  """A NDucerScript of Bob""" should {
    """generate the same XML as the original whan induced to an XmlInducer""" in {
      val generated = trim(induceFromScript(bobScript))
      generated must beEqualTo(trim(bob))
    }
  }
}