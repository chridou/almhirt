package almhirt.akkax

import org.scalatest._

class AlmCircuitBreakerTests extends FunSuite with Matchers {

  test("A CircuitBreaker should be instantiable") {
   val cb = AlmCircuitBreaker(AlmCircuitBreaker.defaultSettings, null, null)
  }
}
