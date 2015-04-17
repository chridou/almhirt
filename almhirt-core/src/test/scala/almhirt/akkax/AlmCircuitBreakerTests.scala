package almhirt.akkax

import org.scalatest._
import akka.util.Unsafe
import akka.pattern.CircuitBreaker

class AlmCircuitBreakerTests extends FunSuite with Matchers {
  test("An akka circuit breaker should be instatiable") {
    val inst =
      new CircuitBreaker(null,
        maxFailures = 5,
        callTimeout = null,
        resetTimeout = null)(null)
    inst should not be (null)
  }

  test("The 'Unsafe' instance should be available") {
    val inst = Unsafe.instance
    inst should not be (null)
  }

  test("A CircuitBreaker should be instantiable") {
    val cb = AlmCircuitBreaker(CircuitControlSettings.defaultSettings, null, null)
    cb should not be (null)
  }
}


