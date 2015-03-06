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
  def propsRaw(failuresHerdingDogProps: Props, rejectedCommandsHerdingDogProps: Props, missedEventsHerdingDogProps: Props, informationHerdingDogProps: Props, runtimeHerdingDogProps: Props)(implicit ctx: AlmhirtContext): Props =
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
  val failuresHerdingDog: ActorRef = context.actorOf(failuresHerdingDogProps, FailuresHerdingDog.actorname)
  val rejectedCommandsHerdingDog: ActorRef = context.actorOf(rejectedCommandsHerdingDogProps, RejectedCommandsHerdingDog.actorname)
  val missedEventsHerdingDog: ActorRef = context.actorOf(missedEventsHerdingDogProps, MissedEventsHerdingDog.actorname)
  val informationHerdingDog: ActorRef = context.actorOf(informationHerdingDogProps, InformationHerdingDog.actorname)
  val runtimeHerdingDog: ActorRef = context.actorOf(runtimeHerdingDogProps, RuntimeHerdingDog.actorname)

  val eventBrokerId = ComponentId(AppName("almhirt-streams"), ComponentName("event-broker"))
  val commandBrokerId = ComponentId(AppName("almhirt-streams"), ComponentName("command-broker"))

  context.actorSelection(almhirtContext.localActorPaths.almhirt / "streams" / "event-broker") ! almhirt.streaming.InternalBrokerMessages.InternalNotifyOnNoDemand(
    5.minutes,
    (t, noDemand) ⇒ informationHerdingDog ! HerderMessages.InformationMessages.Information(
      eventBrokerId,
      if(noDemand) s"No demand for ${t.defaultUnitString}" else s"First demand after having no demand for ${t.defaultUnitString}",
      Importance.Mentionable,
      almhirtContext.getUtcTimestamp))

  context.actorSelection(almhirtContext.localActorPaths.almhirt / "streams" / "command-broker") ! almhirt.streaming.InternalBrokerMessages.InternalNotifyOnNoDemand(
    5.minutes,
    (t, noDemand) ⇒ informationHerdingDog ! HerderMessages.InformationMessages.Information(
      commandBrokerId,
      if(noDemand) s"No demand for ${t.defaultUnitString}" else s"First demand after having no demand for ${t.defaultUnitString}",
      Importance.Mentionable,
      almhirtContext.getUtcTimestamp))

  def receiveRunning: Receive = {
    case m: HerderMessages.CircuitMessages.CircuitMessage         ⇒ circuitsHerdingDog forward m

    case m: HerderMessages.FailureMessages.FailuresMessage        ⇒ failuresHerdingDog forward m

    case m: HerderMessages.CommandMessages.CommandsMessage        ⇒ rejectedCommandsHerdingDog forward m

    case m: HerderMessages.EventMessages.EventsMessage            ⇒ missedEventsHerdingDog forward m

    case m: HerderMessages.InformationMessages.InformationMessage ⇒ informationHerdingDog forward m
  }

  def receive = receiveRunning

  override def preStart() {
  }

}