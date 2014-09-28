package almhirt.akkax

import scala.concurrent.duration._
import akka.actor._
import almhirt.common._
import almhirt.tracking.CorrelationId
import scala.concurrent.duration.FiniteDuration
import almhirt.tracking.CorrelationId

final case class ResolveSettings(maxResolveTime: FiniteDuration, resolveWait: FiniteDuration, resolvePause: FiniteDuration)

object ResolveSettings {
  val default = ResolveSettings(maxResolveTime = 10.seconds, resolveWait = 4.seconds, resolvePause = 1.second)
}

sealed trait ToResolve
final case class NoResolvingRequired(actorRef: ActorRef) extends ToResolve
sealed trait ToReallyResolve extends ToResolve
final case class ResolvePath(path: ActorPath) extends ToReallyResolve
final case class ResolveSelection(selection: ActorSelection) extends ToReallyResolve

object SingleResolver {
  def props(toResolve: ToResolve, settings: ResolveSettings, correlationId: Option[CorrelationId]): Props =
    Props(new SingleResolverImpl(toResolve, settings, correlationId))
}

object MultiResolver {
  def props(toResolve: Map[String, ToResolve], settings: ResolveSettings, correlationId: Option[CorrelationId]): Props =
    Props(new MultiResolverImpl(toResolve, settings, correlationId))
}

private[almhirt] class SingleResolverImpl(toResolve: ToResolve, settings: ResolveSettings, correlationId: Option[CorrelationId]) extends Actor with ActorLogging {
  private object Resolve
  private case class Resolved(actor: ActorRef)
  private case class NotResolved(ex: Throwable)
  def receiveResolve(startedAt: Deadline): Receive = {
    case Resolve ⇒
      toResolve match {
        case NoResolvingRequired(actorRef) ⇒ 
          self ! Resolved(actorRef)
        case r: ToReallyResolve ⇒
          val selection =
            r match {
              case ResolvePath(path) ⇒ context.actorSelection(path)
              case ResolveSelection(selection) ⇒ selection
            }
          selection.resolveOne(settings.resolveWait).onComplete {
            case scala.util.Success(actor) ⇒ self ! Resolved(actor)
            case scala.util.Failure(ex) ⇒ self ! NotResolved(ex)
          }(context.dispatcher)
      }

    case Resolved(actor) ⇒
      context.parent ! ActorMessages.ResolvedSingle(actor, correlationId)
      context.stop(self)

    case NotResolved(ex) ⇒
      if (startedAt.lapExceeds(settings.maxResolveTime)) {
        log.error(s"""	|Could not resolve "${toResolve}" after ${settings.maxResolveTime.defaultUnitString}.
        				|Giving up.
        				|Cause:
    		  			|$ex""".stripMargin)
        context.parent ! ActorMessages.SingleNotResolved(UnspecifiedProblem(s"Resolve $toResolve failed after ${startedAt.lap.defaultUnitString}.", cause = Some(ex)), correlationId)
        context.stop(self)
      } else {
        log.warning(s"""	|Could not resolve "${toResolve}" after ${startedAt.lap.defaultUnitString}.
        					|Will retry in ${settings.resolvePause.defaultUnitString}.
        					|Cause:
        					|$ex""".stripMargin)
        context.system.scheduler.scheduleOnce(settings.resolvePause, self, Resolve)(context.dispatcher)
      }
  }

  def receive: Receive = receiveResolve(Deadline.now)

  override def preStart() {
    self ! Resolve
  }
}

private[almhirt] class MultiResolverImpl(toResolve: Map[String, ToResolve], settings: ResolveSettings, correlationId: Option[CorrelationId]) extends Actor with ActorLogging {
  private object Resolve

  var resolvedActors: Map[String, ActorRef] = Map.empty

  def receiveResolve: Receive = {
    case Resolve ⇒
      toResolve.zipWithIndex.foreach {
        case ((name, toResolve), index) ⇒
          context.resolveSingle(toResolve, settings, Some(CorrelationId(name)), Some(s"resolver-$index"))
      }

    case ActorMessages.ResolvedSingle(resolved, correlationIdOpt) ⇒
      resolvedActors = resolvedActors + (correlationIdOpt.get.value -> resolved)
      if (resolvedActors.size == toResolve.size) {
        context.parent ! ActorMessages.ManyResolved(resolvedActors, correlationId)
        context.stop(self)
      }

    case ActorMessages.SingleNotResolved(problem, _) ⇒
      context.parent ! ActorMessages.ManyNotResolved(problem, correlationId)
      log.error(s"Aborting to resolve.\n$problem")
      context.stop(self)
  }

  def receive: Receive = receiveResolve

  override def preStart() {
    self ! Resolve
  }
}
