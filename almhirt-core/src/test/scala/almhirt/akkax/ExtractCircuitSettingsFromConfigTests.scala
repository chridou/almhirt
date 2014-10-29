package almhirt.akkax

import scala.concurrent.duration._
import org.scalatest._
import almhirt.almvalidation.kit._
import almhirt.configuration._
import com.typesafe.config._

class ExtractCircuitContralSettingsFromConfigTests extends FunSuite with Matchers {

  test("Parse 1") {
    val configStr = """	|circuit-control {
		    			|	max-failures = 5
	        			|	failures-warn-threshold = 3
	        			|	call-timeout = 15 seconds
	        			|	reset-timeout = 1 minute
		   				|	start-state = fuse-removed
		   				|}""".stripMargin

    val config = ConfigFactory.parseString(configStr)
    val ctrlSettings = config.v[CircuitControlSettings]("circuit-control").forceResult

    ctrlSettings should equal(CircuitControlSettings(
      maxFailures = 5,
      failuresWarnThreshold = Some(3),
      callTimeout = 15.seconds,
      resetTimeout = Some(1.minute),
      startState = CircuitStartState.FuseRemoved))
  }
}
