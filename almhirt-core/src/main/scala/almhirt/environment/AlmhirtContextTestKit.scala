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
  val systemTestKit = new AlmhirtsystemTestkit {}
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
  val defaultConf = ConfigFactory.parseString(configText).withFallback(ConfigFactory.load)

  def createTestContext(): AlmhirtContext = createTestContext(defaultConf)
  def createTestContext(aConf: Config): AlmhirtContext = {
    implicit val almhirtSys = systemTestKit.createTestSystem(aConf)
    implicit val context = AlmhirtContext().awaitResult(almhirtSys.shortDuration).forceResult
    context
  }

  def inTestContext[T](compute: AlmhirtContext => T): T = inTestContext[T](compute, defaultConf)
  def inTestContext[T](compute: AlmhirtContext => T, aConf: Config): T = {
    val context = createTestContext(aConf)
    val res = compute(context)
    context.dispose
    res
  }
}