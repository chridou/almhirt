package almhirt.environment

import akka.actor.ActorSystem
import almhirt._
import almhirt.commanding._
import almhirt.eventlog.impl._
import almhirt.domain.DomainEvent
import almhirt.messaging._
import almhirt.syntax.almvalidation._
import almhirt.parts._
import com.typesafe.config._

trait AlmhirtEnvironmentTestKit {
  val contextTestKit = new AlmhirtContextTestKit {}
  private val configText =
    """  
      akka {
		loglevel = ERROR
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

  def createTestEnvironment(): AlmhirtEnvironment = createTestEnvironment(conf)
  def createTestEnvironment(aConf: Config): AlmhirtEnvironment = {
    implicit val almhirtSys = AlmhirtSystem(conf).forceResult
    implicit val context = AlmhirtContext(conf).awaitResult(almhirtSys.shortDuration).forceResult
    AlmhirtEnvironment(conf).awaitResult(almhirtSys.shortDuration).forceResult
  }

   def inTestEnvironment[T](compute: AlmhirtEnvironment => T): T = inTestEnvironment[T](compute, conf)
  def inTestEnvironment[T](compute: AlmhirtEnvironment => T, conf: Config): T = {
    val context = createTestEnvironment(conf)
    val res = compute(context)
    context.dispose
    res
  }
}