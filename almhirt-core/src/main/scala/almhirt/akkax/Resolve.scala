package almhirt.akkax

import scala.concurrent.duration._
import akka.actor._
import almhirt.common._
import almhirt.tracking.CorrelationId
import scala.concurrent.duration.FiniteDuration

final case class ResolveSettings(maxResolveTime: FiniteDuration, resolveInterval: FiniteDuration)

sealed trait ToResolve
final case class ResolvePath(path: ActorPath) extends ToResolve
final case class ResolveSelection(selection: ActorSelection) extends ToResolve

private[almhirt] class SingleResolver(toResolve: ToResolve, settings: ResolveSettings, correlationId: Option[CorrelationId]) extends Actor with ActorLogging {
  private object Resolve
  private case class Resolved(actor: ActorRef)
  private case class NotResolved(ex: Throwable)
  def receiveResolve(startedAt: Deadline): Receive = {
    case Resolve =>
      val selection =
        toResolve match {
          case ResolvePath(path) => context.actorSelection(path)
          case ResolveSelection(selection) => selection
        }
      selection.resolveOne(1.second).onComplete {
        case scala.util.Success(actor) => self ! Resolved(actor)
        case scala.util.Failure(ex) => self ! NotResolved(ex)
      }(context.dispatcher)

    case Resolved(actor) =>
      context.parent ! ActorMessages.ResolvedSingle(actor, correlationId)
      context.stop(self)

    case NotResolved(ex) =>
      if (startedAt.lapExceeds(settings.maxResolveTime)) {
        log.error(s"""	|Could not resolve "${toResolve}" after ${settings.maxResolveTime.defaultUnitString}.
        				|Giving up.
        				|Cause:
    		  			|$ex""".stripMargin)
    	context.parent ! ActorMessages.SingleNotResolved(UnspecifiedProblem(s"Resolve $toResolve failed after ${startedAt.lap.defaultUnitString}."), correlationId)
        context.stop(self)
      } else {
        log.warning(s"""	|Could not resolve "${toResolve}" after ${startedAt.lap.defaultUnitString}.
        					|Will retry in ${settings.resolveInterval.defaultUnitString}.
        					|Cause:
        					|$ex""".stripMargin)
        context.system.scheduler.scheduleOnce(settings.resolveInterval, self, Resolve)(context.dispatcher)
      }
  }

  def receive: Receive = receiveResolve(Deadline.now)

  override def preStart() {
    self ! Resolve
  }
}