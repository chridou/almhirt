package almhirt.herder

import scala.concurrent.duration._
import scalaz.Validation.FlatMap._
import akka.actor._
import almhirt.common._
import almhirt.almvalidation.kit._
import almhirt.almfuture.all._
import almhirt.context._
import almhirt.akkax._
import almhirt.components.EventSinkHub
import almhirt.components.EventSinkHubMessage
import almhirt.herder.herdingdogs._

object Herder {
  def propsRaw(
      failuresHerdingDogProps: Props, 
      rejectedCommandsHerdingDogProps: Props, 
      missedEventsHerdingDogProps: Props,
      informationHerdingDogProps: Props, 
      runtimeHerdingDogProps: Props)(implicit ctx: AlmhirtContext): Props =
    Props(new Pastor(failuresHerdingDogProps, rejectedCommandsHerdingDogProps, missedEventsHerdingDogProps, informationHerdingDogProps, runtimeHerdingDogProps))

  def props(address: Address)(implicit ctx: AlmhirtContext): AlmValidation[Props] = {
    import com.typesafe.config.Config
    import almhirt.configuration._
    val configPath = "almhirt.herder"
    for {
      section ← ctx.config.v[Config](configPath)
      failuresHerdingDogProps ← FailuresHerdingDog.props
      rejectedCommandsHerdingDogProps ← RejectedCommandsHerdingDog.props
      missedEventsHerdingDogProps ← MissedEventsHerdingDog.props
      informationHerdingDogProps ← InformationHerdingDog.props
      runtimeHerdingDogProps ← RuntimeHerdingDog.props
    } yield propsRaw(failuresHerdingDogProps, rejectedCommandsHerdingDogProps, missedEventsHerdingDogProps, informationHerdingDogProps, runtimeHerdingDogProps)
  }

  def props()(implicit ctx: AlmhirtContext): AlmValidation[Props] =
    props(Address("akka", "almhirt-system"))

  val actorname = "herder"
  def path(root: RootActorPath) = almhirt.context.ContextActorPaths.almhirt(root) / actorname
}

private[almhirt] class Pastor(
    failuresHerdingDogProps: Props,
    rejectedCommandsHerdingDogProps: Props,
    missedEventsHerdingDogProps: Props,
    informationHerdingDogProps: Props,
    runtimeHerdingDogProps: Props)(implicit override val almhirtContext: AlmhirtContext) extends AlmActor with AlmActorLogging with ActorLogging with HasAlmhirtContext {
  import almhirt.components.{ EventSinkHub, EventSinkHubMessage }

  val circuitsHerdingDog: ActorRef = context.actorOf(Props(new CircuitsHerdingDog()), CircuitsHerdingDog.actorname)
  val componentControlHerdingDog: ActorRef = context.actorOf(Props(new ComponentControlHerdingDog()), ComponentControlHerdingDog.actorname)
  val failuresHerdingDog: ActorRef = context.actorOf(failuresHerdingDogProps, FailuresHerdingDog.actorname)
  val rejectedCommandsHerdingDog: ActorRef = context.actorOf(rejectedCommandsHerdingDogProps, RejectedCommandsHerdingDog.actorname)
  val missedEventsHerdingDog: ActorRef = context.actorOf(missedEventsHerdingDogProps, MissedEventsHerdingDog.actorname)
  val informationHerdingDog: ActorRef = context.actorOf(informationHerdingDogProps, InformationHerdingDog.actorname)
  val runtimeHerdingDog: ActorRef = context.actorOf(runtimeHerdingDogProps, RuntimeHerdingDog.actorname)
  val reportHerdingDog: ActorRef = context.actorOf(Props(new StatusReportsHerdingDog()), StatusReportsHerdingDog.actorname)

  val eventBrokerId = ComponentId(AppName("almhirt-streams"), ComponentName("event-broker"))
  val commandBrokerId = ComponentId(AppName("almhirt-streams"), ComponentName("command-broker"))

  context.actorSelection(almhirtContext.localActorPaths.almhirt / "streams" / "event-broker") ! almhirt.streaming.InternalBrokerMessages.InternalAddReporter(almhirtContext.createReporter(eventBrokerId))
  context.actorSelection(almhirtContext.localActorPaths.almhirt / "streams" / "event-broker") ! almhirt.streaming.InternalBrokerMessages.InternalEnableNotifyOnNoDemand(5.minutes)

  context.actorSelection(almhirtContext.localActorPaths.almhirt / "streams" / "command-broker") ! almhirt.streaming.InternalBrokerMessages.InternalAddReporter(almhirtContext.createReporter(commandBrokerId))
  context.actorSelection(almhirtContext.localActorPaths.almhirt / "streams" / "command-broker") ! almhirt.streaming.InternalBrokerMessages.InternalEnableNotifyOnNoDemand(5.minutes)

  def receiveRunning: Receive = {
    case m: HerderMessages.CircuitMessages.CircuitMessage                   ⇒ circuitsHerdingDog forward m

    case m: HerderMessages.ComponentControlMessages.ComponentControlMessage ⇒ componentControlHerdingDog forward m

    case m: HerderMessages.FailureMessages.FailuresMessage                  ⇒ failuresHerdingDog forward m

    case m: HerderMessages.CommandMessages.CommandsMessage                  ⇒ rejectedCommandsHerdingDog forward m

    case m: HerderMessages.EventMessages.EventsMessage                      ⇒ missedEventsHerdingDog forward m

    case m: HerderMessages.InformationMessages.InformationMessage           ⇒ informationHerdingDog forward m

    case m: HerderMessages.StatusReportMessages.StatusReportMessage                     ⇒ reportHerdingDog forward m
  }

  def receive = receiveRunning

  override def preStart() {
  }

}