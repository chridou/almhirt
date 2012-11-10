package almhirt.environment

import akka.actor.ActorSystem
import akka.util.Timeout.durationToTimeout
import akka.util.duration.doubleToDurationDouble
import almhirt._
import almhirt.commanding._
import almhirt.domain.DomainEvent
import almhirt.messaging._
import almhirt.syntax.almvalidation._
import com.typesafe.config._
import almhirt.util._

trait AlmhirtContextTestKit {
  private val configText =
    """  
      akka {
		loglevel = WARNING
      }
      almhirt {
		systemname = "almhirt-testing"
		durations {
		  short = 0.5
		  medium = 2.5
		  long = 10.0
		}
     }
    """
  val conf = ConfigFactory.parseString(configText).withFallback(ConfigFactory.load)

  def createTestContext(): AlmhirtContext = createTestContext(conf)
  def createTestContext(conf: Config): AlmhirtContext = {
    implicit val almhirtSys = AlmhirtSystem(conf).forceResult
    implicit val context = AlmhirtContext(conf).awaitResult(almhirtSys.shortDuration).forceResult
    context
  }

  def inTestContext[T](compute: AlmhirtContext => T): T = inTestContext[T](compute, conf)
  def inTestContext[T](compute: AlmhirtContext => T, conf: Config): T = {
    val context = createTestContext(conf)
    val res = compute(context)
    context.dispose
    res
  }
}