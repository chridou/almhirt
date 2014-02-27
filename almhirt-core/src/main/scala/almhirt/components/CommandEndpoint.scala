package almhirt.components

import scalaz._, Scalaz._
import almhirt.common._
import almhirt.almvalidation.kit._
import almhirt.almfuture.all._
import almhirt.components.ExecutionStateTracker._
import akka.actor._
import akka.pattern._
import akka.util.Timeout
import almhirt.messaging.MessagePublisher
import almhirt.core.types._
import scala.concurrent.ExecutionContext

trait CommandEndpoint {
  def execute(command: Command): Unit
  def executeTracked(command: Command): AlmFuture[String]
  def executeSync(command: Command, atMost: scala.concurrent.duration.FiniteDuration): AlmFuture[ExecutionFinishedResultMessage]
  def executeDomainCommandSequence(commands: Seq[DomainCommand]): AlmFuture[Seq[DomainCommand]]
  def executeDomainCommandSequenceTracked(commands: Seq[DomainCommand]): AlmFuture[String]
  def executeDomainCommandSequenceSync(commands: Seq[DomainCommand], atMost: scala.concurrent.duration.FiniteDuration): AlmFuture[ExecutionFinishedResultMessage]
}

object CommandEndpoint {
  def apply(tracker: ActorRef)(implicit theAlmhirt: almhirt.core.Almhirt): CommandEndpoint =
    new CommandEndpointImpl(theAlmhirt.messageBus, tracker, theAlmhirt.getUniqueString)(theAlmhirt, theAlmhirt.futuresExecutor)
  def apply(publishTo: MessagePublisher, tracker: ActorRef)(implicit ccuad: CanCreateUuidsAndDateTimes, execContext: ExecutionContext): CommandEndpoint =
    new CommandEndpointImpl(publishTo, tracker, ccuad.getUniqueString)
  def apply(publishTo: MessagePublisher, tracker: ActorRef, getTrackingId: () => String)(implicit ccuad: CanCreateUuidsAndDateTimes, execContext: ExecutionContext): CommandEndpoint =
    new CommandEndpointImpl(publishTo, tracker, getTrackingId)
}

class CommandEndpointImpl(publishTo: MessagePublisher, tracker: ActorRef, getTrackingId: () => String)(implicit ccuad: CanCreateUuidsAndDateTimes, execContext: ExecutionContext) extends CommandEndpoint {
  override def execute(command: Command) {
    publishTo.publish(command)
  }

  def executeTracked(command: Command): AlmFuture[String] = {
    val cmd =
      if (command.canBeTracked)
        command
      else
        command.track(getTrackingId())
    publishTo.publish(cmd)
    AlmFuture.successful(cmd.trackingId)
  }

  override def executeSync(command: Command, atMost: scala.concurrent.duration.FiniteDuration): AlmFuture[ExecutionFinishedResultMessage] = {
    import scalaz.syntax.validation._
    import almhirt.components.ExecutionStateTracker._
    val cmd =
      if (command.canBeTracked)
        command
      else
        command.track(getTrackingId())
    val resF = (tracker ? SubscribeForFinishedState(cmd.trackingId))(atMost).successfulAlmFuture[ExecutionFinishedResultMessage]
    publishTo.publish(cmd)
    resF

  }

  override def executeDomainCommandSequence(commands: Seq[DomainCommand]): AlmFuture[Seq[DomainCommand]] = {
    AlmFuture.completed(DomainCommandSequence.validatedCommandSequence(commands).andThenWhenSucceeded(succ => succ.foreach(publishTo.publish)))
  }

  private def makeSequenceValidatedAndTrackable(commands: Seq[DomainCommand]): AlmValidation[Seq[DomainCommand]] = {
    DomainCommandSequence.validatedCommandSequence(commands).flatMap { cs =>
      val (head, tail) = (cs.head, cs.tail)
      if (head.canBeTrackedAsGroup) {
        commands.success
      } else {
        val headTrack = head.trackableGroup
        (headTrack +: tail).success
      }
    }
  }

  override def executeDomainCommandSequenceTracked(commands: Seq[DomainCommand]): AlmFuture[String] = {
    AlmFuture.completed {
      makeSequenceValidatedAndTrackable(commands).flatMap { cs =>
        cs.foreach(publishTo.publish)
        unsafe { cs.head.getGroupTrackingId }
      }
    }
  }

  override def executeDomainCommandSequenceSync(commands: Seq[DomainCommand], atMost: scala.concurrent.duration.FiniteDuration): AlmFuture[ExecutionFinishedResultMessage] = {
    makeSequenceValidatedAndTrackable(commands).flatMap(cs => unsafe { cs.head.getGroupTrackingId.map((_, cs)) }).fold(
      fail => AlmFuture.failed(fail),
      succ => {
        val resF = (tracker ? SubscribeForFinishedState(succ._1))(atMost).successfulAlmFuture[ExecutionFinishedResultMessage]
        succ._2.foreach(publishTo.publish)
        resF
      })
  }
}