package almhirt.almvalidation

import org.scalatest._
import almhirt.common._
import almhirt.problem._

class SeveritySpec extends FlatSpec with Matchers {

  "A Severity combines always to the most severe and so" should
    "return 'Critical' when 'Critical' and 'Critical'" in {
      Critical.and(Critical) should equal(Critical)
    }
  it should "return 'Critical' when 'Critical' and 'Major'" in {
    Critical.and(Major) should equal(Critical)
  }
  it should "return 'Critical' when 'Minor' and 'Minor'" in {
    Critical.and(Minor) should equal(Critical)
  }

  it should "return 'Critical' when 'Major' and 'Critical'" in {
    Major.and(Critical) should equal(Critical)
  }
  it should "return 'Major' when 'Major' and 'Major'" in {
    Major.and(Major) should equal(Major)
  }
  it should "return 'Major' when 'Major' and 'Minor'" in {
    Major.and(Minor) should equal(Major)
  }

  it should "return 'Critical' when 'Minor' and 'Critical'" in {
    Minor.and(Critical) should equal(Critical)
  }
  it should "return 'Major' when 'Minor' and 'Major'" in {
    Minor.and(Major) should equal(Major)
  }
  it should "return 'Minor' when 'Minor' and 'Minor'" in {
    Minor.and(Minor) should equal(Minor)
  }
}