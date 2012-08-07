package almhirt.validation

import org.specs2.mutable._
import org.specs2.matcher._

class SeveritySpec extends Specification {
	"A Severity has a monoid zero of 'NoProblem' and" should {
		"return 'Critical' when 'NoProblem' and 'Critical'" in {
			NoProblem.and(Critical) must beEqualTo(Critical)
		}
		"return 'Critical' when 'Critical' and 'NoProblem'" in {
			Critical.and(NoProblem) must beEqualTo(Critical)
		}
		"return 'Major' when 'Major' and 'NoProblem'" in {
			Major.and(NoProblem) must beEqualTo(Major)
		}
		"return 'Major' when 'NoProblem' and 'Major'" in {
			NoProblem.and(Major) must beEqualTo(Major)
		}
		"return 'Minor' when 'NoProblem' and 'Minor'" in {
			NoProblem.and(Minor) must beEqualTo(Minor)
		}
		"return 'Minor' when 'Minor' and 'NoProblem'" in {
			Minor.and(NoProblem) must beEqualTo(Minor)
		}
		"return 'NoProblem' when 'NoProblem' and 'NoProblem'" in {
			NoProblem.and(NoProblem) must beEqualTo(NoProblem)
		}
	}

	"A Severity combines always to the most severe and so" should {
		"return 'Critical' when 'Critical' and 'Critical'" in {
			Critical.and(Critical) must beEqualTo(Critical)
		}
		"return 'Critical' when 'Critical' and 'Major'" in {
			Critical.and(Major) must beEqualTo(Critical)
		}
		"return 'Critical' when 'Minor' and 'Minor'" in {
			Critical.and(Minor) must beEqualTo(Critical)
		}
		"return 'Critical' when 'Critical' and 'NoProblem'" in {
			Critical.and(NoProblem) must beEqualTo(Critical)
		}

		"return 'Critical' when 'Major' and 'Critical'" in {
			Major.and(Critical) must beEqualTo(Critical)
		}
		"return 'Major' when 'Major' and 'Major'" in {
			Major.and(Major) must beEqualTo(Major)
		}
		"return 'Major' when 'Major' and 'Minor'" in {
			Major.and(Minor) must beEqualTo(Major)
		}
		"return 'Major' when 'Major' and 'NoProblem'" in {
			Major.and(NoProblem) must beEqualTo(Major)
		}

		"return 'Critical' when 'Minor' and 'Critical'" in {
			Minor.and(Critical) must beEqualTo(Critical)
		}
		"return 'Major' when 'Minor' and 'Major'" in {
			Minor.and(Major) must beEqualTo(Major)
		}
		"return 'Minor' when 'Minor' and 'Minor'" in {
			Minor.and(Minor) must beEqualTo(Minor)
		}
		"return 'Minor' when 'Minor' and 'NoProblem'" in {
			Minor.and(NoProblem) must beEqualTo(Minor)
		}

		"return 'Critical' when 'NoProblem' and 'Critical'" in {
			NoProblem.and(Critical) must beEqualTo(Critical)
		}
		"return 'Major' when 'NoProblem' and 'Major'" in {
			NoProblem.and(Major) must beEqualTo(Major)
		}
		"return 'Minor' when 'NoProblem' and 'Minor'" in {
			NoProblem.and(Minor) must beEqualTo(Minor)
		}
		"return 'NoProblem' when 'NoProblem' and 'NoProblem'" in {
			NoProblem.and(NoProblem) must beEqualTo(NoProblem)
		}
	}
}