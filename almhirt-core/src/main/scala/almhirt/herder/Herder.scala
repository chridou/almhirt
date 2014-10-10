package almhirt.herder

import scalaz.Validation.FlatMap._
import akka.actor._
import almhirt.common._
import almhirt.almvalidation.kit._
import almhirt.almfuture.all._
import almhirt.context._
import almhirt.akkax._
import almhirt.components.EventSinkHub
import almhirt.components.EventSinkHubMessage
import almhirt.configuration.ConfigConfigExtractorInst
import almhirt.configuration.ConfigOps
import almhirt.configuration.ConfigStringExtractorInst
import com.typesafe.config.Config
import akka.actor.RootActorPath

object Herder {
  def propsRaw()(implicit ctx: AlmhirtContext): Props =
    Props(new Pastor())

  def props(address: Address)(implicit ctx: AlmhirtContext): AlmValidation[Props] = {
    import com.typesafe.config.Config
    import almhirt.configuration._
    val configPath = "almhirt.herder"
    for {
      section <- ctx.config.v[Config](configPath)
//      resolveSettings <- section.v[ResolveSettings]("resolve-settings")
//      eventSinkHubPathPart <- section.magicOption[String]("livestock.event-sink-hub")
//      eventSinkHubToResolve <- inTryCatch { eventSinkHubPathPart.map(pathPart => ResolvePath(ActorPath.fromString(s"${address}/$pathPart"))) }
    } yield propsRaw()
  }

  def props()(implicit ctx: AlmhirtContext): AlmValidation[Props] =
    props(Address("akka", "almhirt-system"))

  val actorname = "herder"
}

private[almhirt] class Pastor()(implicit override val almhirtContext: AlmhirtContext) extends Actor with ActorLogging with HasAlmhirtContext {
  import almhirt.components.{ EventSinkHub, EventSinkHubMessage }

  implicit val executor = almhirtContext.futuresContext

  private object _Start

  val circuitBreakerHerdingDog: ActorRef = context.actorOf(Props(new herdingdogs.CircuitBreakerHerdingDog()), herdingdogs.CircuitBreakerHerdingDog.actorname)
  
  def receiveInitialize: Receive = {
    case `_Start` =>
      log.info("Starting...")

  }

  def receiveRunning: Receive = {
    case m: HerderMessage.RegisterCircuitBreaker => circuitBreakerHerdingDog ! m
    case m: HerderMessage.DeregisterCircuitBreaker => circuitBreakerHerdingDog ! m
  }

  def receive = receiveInitialize

  override def preStart() {
    self ! _Start
  }

}