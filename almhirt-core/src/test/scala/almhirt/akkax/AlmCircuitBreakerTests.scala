package almhirt.akkax

import org.scalatest._

class AlmCircuitBreakerTests extends FunSuite with Matchers {

  test("A CircuitBreaker should be instantiable") {
    val params = AlmCircuitBreaker.AlmCircuitBreakerParams(AlmCircuitBreaker.defaultSettings, None, None, None, None)
    val cb = AlmCircuitBreaker(params, null, null)
  }
}
