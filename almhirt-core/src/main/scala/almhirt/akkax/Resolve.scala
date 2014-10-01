package almhirt.akkax

import scala.concurrent.duration._
import akka.actor._
import almhirt.common._
import almhirt.tracking.CorrelationId
import scala.concurrent.duration.FiniteDuration
import almhirt.tracking.CorrelationId
import almhirt.configuration.{ RetrySettings, AttemptLimitedRetrySettings, TimeLimitedRetrySettings }
import almhirt.configuration.AttemptLimitedRetrySettings
import almhirt.configuration.TimeLimitedRetrySettings

final case class ResolveSettings(retrySettings: RetrySettings, resolveWait: FiniteDuration)

object ResolveSettings {
  val default = ResolveSettings(AttemptLimitedRetrySettings(pause = 1.second, maxAttempts = 50), resolveWait = 1.second)
}

sealed trait ToResolve
final case class NoResolvingRequired(actorRef: ActorRef) extends ToResolve
sealed trait ToReallyResolve extends ToResolve
final case class ResolvePath(path: ActorPath) extends ToReallyResolve
final case class ResolveSelection(selection: ActorSelection) extends ToReallyResolve

object SingleResolver {
  def props(toResolve: ToResolve, settings: ResolveSettings, correlationId: Option[CorrelationId]): Props =
    settings.retrySettings match {
      case tl: TimeLimitedRetrySettings =>
        Props(new MaxTimeBasedSingleResolverImpl(toResolve, settings.resolveWait, tl, correlationId))
      case al: AttemptLimitedRetrySettings =>
        Props(new MaxAttemptsBasedSingleResolverImpl(toResolve, settings.resolveWait, al, correlationId))
    }
}

object MultiResolver {
  def props(toResolve: Map[String, ToResolve], settings: ResolveSettings, correlationId: Option[CorrelationId]): Props =
    Props(new MultiResolverImpl(toResolve, settings, correlationId))
}

private[almhirt] class MaxTimeBasedSingleResolverImpl(toResolve: ToResolve, resolveWait: FiniteDuration, retrySettings: TimeLimitedRetrySettings, correlationId: Option[CorrelationId]) extends Actor with ActorLogging {
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
          selection.resolveOne(resolveWait).onComplete {
            case scala.util.Success(actor) ⇒ self ! Resolved(actor)
            case scala.util.Failure(ex) ⇒ self ! NotResolved(ex)
          }(context.dispatcher)
      }

    case Resolved(actor) ⇒
      if (log.isDebugEnabled)
        log.debug(s"*** Resolved: ${actor} ***")
      context.parent ! ActorMessages.ResolvedSingle(actor, correlationId)
      context.stop(self)

    case NotResolved(ex) ⇒
      if (startedAt.lapExceeds(retrySettings.maxTime)) {
        log.warning(s"""	|Could not resolve "${toResolve}" after ${retrySettings.maxTime.defaultUnitString}.
        				|Giving up.
        				|Cause:
    		  			|$ex""".stripMargin)
        context.parent ! ActorMessages.SingleNotResolved(UnspecifiedProblem(s"Resolve $toResolve failed after ${startedAt.lap.defaultUnitString}.", cause = Some(ex)), correlationId)
        context.stop(self)
      } else {
        log.warning(s"""	|Could not resolve "${toResolve}" after ${startedAt.lap.defaultUnitString}.
        					|Will retry in ${retrySettings.pause.defaultUnitString}.
        					|Cause:
        					|$ex""".stripMargin)
        context.system.scheduler.scheduleOnce(retrySettings.pause, self, Resolve)(context.dispatcher)
      }
  }

  def receive: Receive = receiveResolve(Deadline.now)

  override def preStart() {
    self ! Resolve
  }
}

private[almhirt] class MaxAttemptsBasedSingleResolverImpl(toResolve: ToResolve, resolveWait: FiniteDuration, retrySettings: AttemptLimitedRetrySettings, correlationId: Option[CorrelationId]) extends Actor with ActorLogging {
  private object Resolve
  private case class Resolved(actor: ActorRef)
  private case class NotResolved(ex: Throwable)
  def receiveResolve(attemptsLeft: Int): Receive = {
    case Resolve ⇒
      if (attemptsLeft > 0) {
        toResolve match {
          case NoResolvingRequired(actorRef) ⇒
            self ! Resolved(actorRef)
          case r: ToReallyResolve ⇒
            val selection =
              r match {
                case ResolvePath(path) ⇒ context.actorSelection(path)
                case ResolveSelection(selection) ⇒ selection
              }
            selection.resolveOne(resolveWait).onComplete {
              case scala.util.Success(actor) ⇒ self ! Resolved(actor)
              case scala.util.Failure(ex) ⇒ self ! NotResolved(ex)
            }(context.dispatcher)
            context.become(receiveResolve(attemptsLeft - 1))
        }
      } else {
        context.parent ! ActorMessages.SingleNotResolved(UnspecifiedProblem(s"Resolve $toResolve failed after ${retrySettings.maxAttempts} attempts. Giving up."), correlationId)
        context.stop(self)
      }

    case Resolved(actor) ⇒
      if (log.isDebugEnabled)
        log.debug(s"*** Resolved: ${actor} ***")
      context.parent ! ActorMessages.ResolvedSingle(actor, correlationId)
      context.stop(self)

    case NotResolved(ex) ⇒
      if (attemptsLeft <= 0) {
        log.warning(s"""|Could not resolve "${toResolve}" after ${retrySettings.maxAttempts} attempts.
        				|Giving up.
        				|Cause:
    		  			|$ex""".stripMargin)
        context.parent ! ActorMessages.SingleNotResolved(UnspecifiedProblem(s"Resolve $toResolve failed after ${retrySettings.maxAttempts} attempts.", cause = Some(ex)), correlationId)
        context.stop(self)
      } else {
        log.warning(s"""	|Could not resolve "${toResolve}". $attemptsLeft attmepts of ${retrySettings.maxAttempts} left.
        					|Will retry in ${retrySettings.pause.defaultUnitString}.
        					|Cause:
        					|$ex""".stripMargin)
        context.system.scheduler.scheduleOnce(retrySettings.pause, self, Resolve)(context.dispatcher)
      }
  }

  def receive: Receive = receiveResolve(retrySettings.maxAttempts)

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
