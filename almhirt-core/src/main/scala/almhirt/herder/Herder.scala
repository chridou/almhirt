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
import almhirt.herder.herdingdogs._

object Herder {
  def propsRaw(failuresHerdingDogProps: Props, rejectedCommandsHerdingDogProps: Props, missedEventsHerdingDogProps: Props)(implicit ctx: AlmhirtContext): Props =
    Props(new Pastor(failuresHerdingDogProps, rejectedCommandsHerdingDogProps, missedEventsHerdingDogProps))

  def props(address: Address)(implicit ctx: AlmhirtContext): AlmValidation[Props] = {
    import com.typesafe.config.Config
    import almhirt.configuration._
    val configPath = "almhirt.herder"
    for {
      section <- ctx.config.v[Config](configPath)
      failuresHerdingDogProps <- FailuresHerdingDog.props
      rejectedCommandsHerdingDogProps <- RejectedCommandsHerdingDog.props
      missedEventsHerdingDogProps <- MissedEventsHerdingDog.props
    } yield propsRaw(failuresHerdingDogProps, rejectedCommandsHerdingDogProps, missedEventsHerdingDogProps)
  }

  def props()(implicit ctx: AlmhirtContext): AlmValidation[Props] =
    props(Address("akka", "almhirt-system"))

  val actorname = "herder"
  def path(root: RootActorPath) = almhirt.context.ContextActorPaths.almhirt(root) / actorname
}

private[almhirt] class Pastor(
  failuresHerdingDogProps: Props,
  rejectedCommandsHerdingDogProps: Props,
  missedEventsHerdingDogProps: Props)(implicit override val almhirtContext: AlmhirtContext) extends Actor with ActorLogging with HasAlmhirtContext {
  import almhirt.components.{ EventSinkHub, EventSinkHubMessage }

  val circuitsHerdingDog: ActorRef = context.actorOf(Props(new CircuitsHerdingDog()), CircuitsHerdingDog.actorname)
  val failuresHerdingDog: ActorRef = context.actorOf(failuresHerdingDogProps, FailuresHerdingDog.actorname)
  val rejectedCommandsHerdingDog: ActorRef = context.actorOf(rejectedCommandsHerdingDogProps, RejectedCommandsHerdingDog.actorname)
  val missedEventsHerdingDog: ActorRef = context.actorOf(missedEventsHerdingDogProps, MissedEventsHerdingDog.actorname)

  def receiveRunning: Receive = {
    case m: HerderMessages.CircuitMessages.CircuitMessage => circuitsHerdingDog forward m

    case m: HerderMessages.FailureMessages.FailuresMessage => failuresHerdingDog forward m

    case m: HerderMessages.CommandMessages.CommandsMessage => rejectedCommandsHerdingDog forward m

    case m: HerderMessages.EventMessages.EventsMessage => missedEventsHerdingDog forward m
    
  }

  def receive = receiveRunning

  override def preStart() {
  }

}