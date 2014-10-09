package almhirt.components

import scalaz.Validation.FlatMap._
import akka.actor._
import almhirt.common._
import almhirt.almvalidation.kit._
import almhirt.almfuture.all._
import almhirt.context._
import almhirt.akkax._

object Herder {
  case class HerderComponentsToResolve(
    eventSinkHubToResolve: Option[ToResolve])

  def propsRaw(componentsToResolve: HerderComponentsToResolve, resolveSettings: ResolveSettings)(implicit ctx: AlmhirtContext): Props =
    Props(new Pastor(componentsToResolve, resolveSettings))

  def props(address: Address)(implicit ctx: AlmhirtContext): AlmValidation[Props] = {
    import com.typesafe.config.Config
    import almhirt.configuration._
    val configPath = "almhirt.components.herder"
    for {
      section <- ctx.config.v[Config](configPath)
      resolveSettings <- section.v[ResolveSettings]("resolve-settings")
      eventSinkHubPathPart <- section.magicOption[String]("livestock.event-sink-hub")
      eventSinkHubToResolve <- inTryCatch { eventSinkHubPathPart.map(pathPart => ResolvePath(ActorPath.fromString(s"${address}/$pathPart"))) }
    } yield propsRaw(HerderComponentsToResolve(eventSinkHubToResolve), resolveSettings)
  }

  def props()(implicit ctx: AlmhirtContext): AlmValidation[Props] =
    props(Address("akka", "almhirt-system"))

  val actorname = "herder"
}

private[almhirt] class Pastor(params: Herder.HerderComponentsToResolve, resolveSettings: ResolveSettings)(implicit override val almhirtContext: AlmhirtContext) extends Actor with ActorLogging with HasAlmhirtContext {
  import almhirt.components.{ EventSinkHub, EventSinkHubMessage }

  implicit val executor = almhirtContext.futuresContext

  private var eventSinkHub: Option[ActorRef] = None
  private object _Start

  def receiveResolve: Receive = {
    case `_Start` =>
      log.info("Starting...")
      val toResolve: Map[String, ToResolve] =
        (params.eventSinkHubToResolve.map(tr => EventSinkHub.actorname -> tr) :: Nil).flatten.toMap
      context.resolveMany(toResolve, resolveSettings, None, Some("resolver"))

    case ActorMessages.ManyResolved(resolved, _) =>
      log.info("Found my livestock.")
      eventSinkHub = resolved get EventSinkHub.actorname
      context.become(receiveRunning)

    case ActorMessages.ManyNotResolved(problem, _) =>
      val msg = s"Could not find my lifestock!\n$problem"
      log.error(msg)
      sys.error(msg)
  }

  def receiveRunning: Receive = {
    case EventSinkHubMessage.ReportEventSinkStates =>
      eventSinkHub match {
        case Some(esh) =>
          esh forward EventSinkHubMessage.ReportEventSinkStates
        case None =>
          sender() ! EventSinkHubMessage.ReportEventSinkStatesFailed(IllegalOperationProblem("The event sink hub is not configured to be herded."))
      }

    case m: EventSinkHubMessage.AttemptResetComponentCircuit =>
      eventSinkHub foreach (_ forward m)
  }

  def receive = receiveResolve

  override def preStart() {
    self ! _Start
  }

}