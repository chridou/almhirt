package almhirt.validation

import org.specs2.mutable._
import org.specs2.matcher._

class SeveritySpec extends Specification {
	"A Severity has a monadic zero of 'Critical' and" should {
		"return 'Fatal' when 'Fatal' and 'Fatal'" in {
			Critical.and(Critical) must beEqualTo(Critical)
		}
		"return 'Critical' when 'Critical' and 'Major'" in {
			Major.and(Critical) must beEqualTo(Critical)
		}
		"return 'Critical' when 'Major' and 'Fatal'" in {
			Critical.and(Major) must beEqualTo(Critical)
		}
		"return 'Critical' when 'Critical' and 'Minor'" in {
			Minor.and(Critical) must beEqualTo(Critical)
		}
		"return 'Critical' when 'Minor' and 'Fatal'" in {
			Critical.and(Minor) must beEqualTo(Critical)
		}
	}

	"A Severity combines always to the most severe and so" should {
		"return 'Critical' when 'Critical' and 'Critical'" in {
			Critical.and(Critical) must beEqualTo(Critical)
		}
		"return 'Major' when 'Major' and 'Major'" in {
			Major.and(Major) must beEqualTo(Major)
		}
		"return 'Critical' when 'Fatal' and 'Major'" in {
			Critical.and(Major) must beEqualTo(Critical)
		}
		"return 'Critical' when 'Major' and 'Fatal'" in {
			Major.and(Critical) must beEqualTo(Critical)
		}
		"return 'Critical' when 'Minor' and 'Fatal'" in {
			Minor.and(Critical) must beEqualTo(Critical)
		}
		"return 'Critical' when 'Fatal' and 'Minor'" in {
			Critical.and(Minor) must beEqualTo(Critical)
		}
		"return 'Major' when 'Major' and 'Minor'" in {
			Major.and(Minor) must beEqualTo(Major)
		}
		"return 'Major' when 'Minor' and 'Major'" in {
			Minor.and(Major) must beEqualTo(Major)
		}
		"return 'Minor' when 'Minor' and 'Minor'" in {
			Minor.and(Minor) must beEqualTo(Minor)
		}
	}
}