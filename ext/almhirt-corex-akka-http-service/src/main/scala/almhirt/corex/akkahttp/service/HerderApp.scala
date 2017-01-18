package almhirt.corex.akkahttp.service

import scala.concurrent.duration._
import scalaz._, Scalaz._
import scalaz.Validation.FlatMap._
import akka.actor._
import almhirt.common._
import almhirt.almvalidation.kit._
import almhirt.context._
import almhirt.akkax._
import akka.http.scaladsl.marshalling._
import com.typesafe.config.Config
import almhirt.configuration._

object HerderServiceApp {
  final case class HttpBind(interface: String, port: Int)
  final case object HttpBound
  
  def propsRaw(interface: String, port: Int, serviceFactory: ComponentFactory, maxStartupDur: FiniteDuration)(implicit almhirtContext: AlmhirtContext): Props = {
    Props(new HerderServiceApp(Some(serviceFactory, interface, port), maxStartupDur))
  }

  def props(problemMarshaller: ToEntityMarshaller[Problem])(implicit almhirtContext: AlmhirtContext): AlmValidation[Props] =
    for {
      configSection ← almhirtContext.config.v[Config]("almhirt.http.endpoints.herder-service")
      maxStartupDur ← configSection.v[FiniteDuration]("max-startup-duration")
      enabled ← configSection.v[Boolean]("enabled")
      params ← if (enabled) {
        for {
          serviceFactory ← HttpHerderService.componentFactory(problemMarshaller)
          interface ← configSection.v[String]("interface")
          port ← configSection.v[Int]("port")
        } yield Some((serviceFactory, interface, port))
      } else {
        None.success
      }
    } yield Props(new HerderServiceApp(params, maxStartupDur))

  def componentFactory(problemMarshaller: ToEntityMarshaller[Problem])(implicit almhirtContext: AlmhirtContext): AlmValidation[ComponentFactory] =
    props(problemMarshaller).map(props ⇒ ComponentFactory(props, actorname))

  def createHerderAppFactory(problemMarshaller: ToEntityMarshaller[Problem]): ComponentFactoryBuilderEntry =
    { (ctx: AlmhirtContext) ⇒
      {
        AlmFuture.completed {
          HerderServiceApp.componentFactory(problemMarshaller)(ctx)
        }
      }
    }.toCriticalEntry

  val actorname = "herder-service-app"
}

private[almhirt] class HerderServiceApp(
  httpServiceParams: Option[(ComponentFactory, String, Int)],
  maxStartupDur: FiniteDuration)(implicit override val almhirtContext: AlmhirtContext) extends AlmActor with AlmActorLogging {

  import akka.actor.SupervisorStrategy._
  import akka.io.IO

  override val supervisorStrategy = OneForOneStrategy(maxNrOfRetries = 10, withinTimeRange = 1.minute) {
    case exn ⇒
      reportCriticalFailure(exn)
      Stop
  }

  private case object StartupExpired

  def receive: Receive = {
    case "Initialize" ⇒
      logInfo("HerderApp Starting..")
      httpServiceParams match {
        case None ⇒
          logWarning("No herder service(it might be disabled in the config)!")
          context.parent ! ActorMessages.HerderServiceAppStarted
        case Some((factory, interface, port)) ⇒
          context.childFrom(factory).fold(
            problem ⇒ {
              val suProb = StartupProblem("Failed to start.", cause = Some(problem))
              reportCriticalFailure(suProb)
              logError(s"Could not create herder service: ${problem.message}")
              context.parent ! ActorMessages.HerderServiceAppFailedToStart(suProb)
              context.stop(self)
            },
            herderService ⇒ {
              logInfo(s"Created herder service: ${herderService.path}. Bind http to $interface:$port")
              herderService ! HerderServiceApp.HttpBind(interface,port)
            })
      }

    case HerderServiceApp.HttpBound ⇒
      logInfo(s"Bound HerderApp")
      context.parent ! ActorMessages.HerderServiceAppStarted
      context.become(receiveRunning)

    case StartupExpired ⇒
      val msg = s"Maximum startup duration of ${maxStartupDur.defaultUnitString} expired."
      logError(msg)
      reportCriticalFailure(StartupProblem(msg))
      context.parent ! ActorMessages.HerderServiceAppFailedToStart(StartupProblem(msg))
      context.stop(self)
  }

  def receiveRunning: Receive = {
    case StartupExpired ⇒
      logDebug("Receive the StartupExpired wich can be skipped now, since I'm already running...")
  }

  override def preStart() {
    context.system.scheduler.scheduleOnce(maxStartupDur, self, StartupExpired)(context.dispatcher)
    self ! "Initialize"

  }
}