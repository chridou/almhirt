package almhirt.almvalidation

import org.scalatest._
import org.scalatest.matchers.ShouldMatchers
import almhirt.common._

class SeveritySpec extends FlatSpec with ShouldMatchers {
  "A Severity has a zero of 'NoProblem' and" should
    "return 'Critical' when 'NoProblem' and 'Critical'" in {
      NoProblem.and(Critical) should equal(Critical)
    }
  it should "return 'Critical' when 'Critical' and 'NoProblem'" in {
    Critical.and(NoProblem) should equal(Critical)
  }
  it should "return 'Major' when 'Major' and 'NoProblem'" in {
    Major.and(NoProblem) should equal(Major)
  }
  it should "return 'Major' when 'NoProblem' and 'Major'" in {
    NoProblem.and(Major) should equal(Major)
  }
  it should "return 'Minor' when 'NoProblem' and 'Minor'" in {
    NoProblem.and(Minor) should equal(Minor)
  }
  it should "return 'Minor' when 'Minor' and 'NoProblem'" in {
    Minor.and(NoProblem) should equal(Minor)
  }
  it should "return 'NoProblem' when 'NoProblem' and 'NoProblem'" in {
    NoProblem.and(NoProblem) should equal(NoProblem)
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
  it should "return 'Critical' when 'Critical' and 'NoProblem'" in {
    Critical.and(NoProblem) should equal(Critical)
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
  it should "return 'Major' when 'Major' and 'NoProblem'" in {
    Major.and(NoProblem) should equal(Major)
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
  it should "return 'Minor' when 'Minor' and 'NoProblem'" in {
    Minor.and(NoProblem) should equal(Minor)
  }

  it should "return 'Critical' when 'NoProblem' and 'Critical'" in {
    NoProblem.and(Critical) should equal(Critical)
  }
  it should "return 'Major' when 'NoProblem' and 'Major'" in {
    NoProblem.and(Major) should equal(Major)
  }
  it should "return 'Minor' when 'NoProblem' and 'Minor'" in {
    NoProblem.and(Minor) should equal(Minor)
  }
  it should "return 'NoProblem' when 'NoProblem' and 'NoProblem'" in {
    NoProblem.and(NoProblem) should equal(NoProblem)
  }
}