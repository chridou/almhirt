package almhirt.akkax

import org.scalatest._

class AlmCircuitBreakerTests extends FunSuite with Matchers {

  test("A CircuitBreaker should be instantiable") {
   val cb = AlmCircuitBreaker(CircuitControlSettings.defaultSettings, null, null)
   cb should not be(null)
  }
}
