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
  val default = ResolveSettings(AttemptLimitedRetrySettings(pause = 1.second, maxAttempts = 30, Some(1.minute)), resolveWait = 1.second)
}

sealed trait ToResolve
final case class NoResolvingRequired(actorRef: ActorRef) extends ToResolve {
  override def toString() =
    s"NoResolvingRequired(${actorRef.path})"
}
sealed trait ToReallyResolve extends ToResolve
final case class ResolvePath(path: ActorPath) extends ToReallyResolve {
  override def toString() =
    s"ResolvePath(${path})"
}
final case class ResolveSelection(selection: ActorSelection) extends ToReallyResolve {
  override def toString() =
    s"ResolveSelection(${selection.pathString})"
}

object SingleResolver {
  def props(toResolve: ToResolve, settings: ResolveSettings, correlationId: Option[CorrelationId]): Props =
    Props(new SingleResolverImpl(toResolve, settings.resolveWait, settings.retrySettings, correlationId))
}

object MultiResolver {
  def props(toResolve: Map[String, ToResolve], settings: ResolveSettings, correlationId: Option[CorrelationId]): Props =
    Props(new MultiResolverImpl(toResolve, settings, correlationId))
}

private[almhirt] class SingleResolverImpl(toResolve: ToResolve, resolveWait: FiniteDuration, retrySettings: RetrySettings, correlationId: Option[CorrelationId]) extends Actor with ActorLogging {

  implicit val execCtx = context.dispatcher

  private case object Resolve
  private case class Resolved(actor: ActorRef)
  private case class NotResolved(ex: Throwable)

  def resolve(started: Deadline, attempt: Int) {
    toResolve match {
      case NoResolvingRequired(actorRef) ⇒
        self ! Resolved(actorRef)
      case r: ToReallyResolve ⇒
        val selection =
          r match {
            case ResolvePath(path)           ⇒ context.actorSelection(path)
            case ResolveSelection(selection) ⇒ selection
          }
        selection.resolveOne(resolveWait).onComplete {
          case scala.util.Success(actor) ⇒ self ! Resolved(actor)
          case scala.util.Failure(exn)   ⇒ handleFailedAttempt(started, attempt, exn)
        }(context.dispatcher)
    }
  }

  def receiveResolve(started: Deadline, attempt: Int): Receive = {
    case Resolve ⇒
      resolve(started, attempt)
      context.become(receiveResolve(started, attempt + 1))

    case Resolved(actorRef: ActorRef) ⇒
      context.parent ! ActorMessages.ResolvedSingle(actorRef, correlationId)
      context.stop(self)

    case NotResolved(ex: Throwable) ⇒
      context.parent ! ActorMessages.SingleNotResolved(DependencyNotFoundProblem(s"$toResolve", cause = Some(ex)), correlationId)
      context.stop(self)
  }

  def handleFailedAttempt(started: Deadline, attempt: Int, exn: Throwable) {
    if (log.isWarningEnabled) {
      log.warning(s"An attempt(#$attempt) to resolve ${toResolve} failed: ${exn.getMessage}")
    }

    val isLoopFinalFailure =
      retrySettings match {
        case TimeLimitedRetrySettings(_, limit, _) ⇒
          started.lapExceeds(limit)
        case AttemptLimitedRetrySettings(_, limit, _) ⇒
          attempt >= limit
      }

    if (isLoopFinalFailure) {
      retrySettings.infiniteLoopPause match {
        case None ⇒
          self ! NotResolved(exn)
        case Some(pause) ⇒
          if (log.isErrorEnabled)
            log.error(s"""	|Resolve $toResolve:
								|Failed after $attempt attempts and ${started.lap.defaultUnitString}
								|Will pause for ${retrySettings.infiniteLoopPause.map(_.defaultUnitString).getOrElse("?")}
								|The exception was:
								|$exn""".stripMargin)
          context.system.scheduler.scheduleOnce(pause, self, Resolve)
      }
    } else {
      context.system.scheduler.scheduleOnce(retrySettings.pause, self, Resolve)
    }
  }

  def receive: Receive = receiveResolve(Deadline.now, 1)

  override def preStart() {
    self ! Resolve
  }
}

private[almhirt] class MultiResolverImpl(toResolve: Map[String, ToResolve], settings: ResolveSettings, correlationId: Option[CorrelationId]) extends Actor with ActorLogging {
  private case object Resolve

  var resolvedActors: Map[String, ActorRef] = Map.empty

  def receiveResolve: Receive = {
    case Resolve ⇒
      toResolve.zipWithIndex.foreach {
        case ((name, toResolve), index) ⇒
          context.resolveSingle(toResolve, settings, Some(CorrelationId(name)), Some(s"resolver-$index"))
      }

    case ActorMessages.ResolvedSingle(resolved, correlationIdOpt) ⇒
      resolvedActors = resolvedActors + (correlationIdOpt.get.value → resolved)
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
