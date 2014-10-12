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
    } yield propsRaw()
  }

  def props()(implicit ctx: AlmhirtContext): AlmValidation[Props] =
    props(Address("akka", "almhirt-system"))

  val actorname = "herder"
  def path(root: RootActorPath) = almhirt.context.ContextActorPaths.almhirt(root) / actorname 
}

private[almhirt] class Pastor()(implicit override val almhirtContext: AlmhirtContext) extends Actor with ActorLogging with HasAlmhirtContext {
  import almhirt.components.{ EventSinkHub, EventSinkHubMessage }

  val circuitsHerdingDog: ActorRef = context.actorOf(Props(new herdingdogs.CircuitsHerdingDog()), herdingdogs.CircuitsHerdingDog.actorname)

  def receiveRunning: Receive = {
    case m: HerderMessage.RegisterCircuitControl => circuitsHerdingDog ! m
    case m: HerderMessage.DeregisterCircuitControl => circuitsHerdingDog ! m
    case HerderMessage.ReportCircuitStates => circuitsHerdingDog forward HerderMessage.ReportCircuitStates
    case m: HerderMessage.CircuitControlMessage => circuitsHerdingDog forward m
  }

  def receive = receiveRunning

  override def preStart() {
  }

}