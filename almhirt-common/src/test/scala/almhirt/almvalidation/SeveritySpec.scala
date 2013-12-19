package almhirt.almvalidation

import org.scalatest._
import almhirt.common._
import almhirt.problem._

class SeveritySpec extends FlatSpec with Matchers {
  "A Severity has a zero of 'Warning' and" should
    "return 'Critical' when 'Warning' and 'Critical'" in {
      Warning.and(Critical) should equal(Critical)
    }
  it should "return 'Critical' when 'Critical' and 'Warning'" in {
    Critical.and(Warning) should equal(Critical)
  }
  it should "return 'Major' when 'Major' and 'Warning'" in {
    Major.and(Warning) should equal(Major)
  }
  it should "return 'Major' when 'Warning' and 'Major'" in {
    Warning.and(Major) should equal(Major)
  }
  it should "return 'Minor' when 'Warning' and 'Minor'" in {
    Warning.and(Minor) should equal(Minor)
  }
  it should "return 'Minor' when 'Minor' and 'Warning'" in {
    Minor.and(Warning) should equal(Minor)
  }
  it should "return 'Warning' when 'Warning' and 'Warning'" in {
    Warning.and(Warning) should equal(Warning)
  }

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
  it should "return 'Critical' when 'Critical' and 'Warning'" in {
    Critical.and(Warning) should equal(Critical)
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
  it should "return 'Major' when 'Major' and 'Warning'" in {
    Major.and(Warning) should equal(Major)
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
  it should "return 'Minor' when 'Minor' and 'Warning'" in {
    Minor.and(Warning) should equal(Minor)
  }

  it should "return 'Critical' when 'Warning' and 'Critical'" in {
    Warning.and(Critical) should equal(Critical)
  }
  it should "return 'Major' when 'Warning' and 'Major'" in {
    Warning.and(Major) should equal(Major)
  }
  it should "return 'Minor' when 'Warning' and 'Minor'" in {
    Warning.and(Minor) should equal(Minor)
  }
  it should "return 'Warning' when 'Warning' and 'Warning'" in {
    Warning.and(Warning) should equal(Warning)
  }
}