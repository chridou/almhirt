package almhirt.commanding.impl

import akka.actor._
import almhirt.core.Almhirt
import almhirt.commanding._
import almhirt.components._
import almhirt.messaging.MessagePublisher
import scala.concurrent.duration.FiniteDuration

class CommandExecutorImpl(
  val handlers: CommandHandlerRegistry,
  val repositories: AggregateRootRepositoryRegistry,
  val messagePublisher: MessagePublisher,
  val theAlmhirt: Almhirt,
  override val maxExecutionTimePerCommandWarnThreshold: FiniteDuration) extends CommandExecutor with CommandExecutorTemplate with Actor with ActorLogging {

  val domainCommandsSequencer = context.actorOf(Props(new DomainCommandsSequencerImpl(theAlmhirt)), "DomainCommandsSequencer")

  def receive: Receive = receiveCommandExecutorMessage

  override def preRestart(reason: Throwable, messgae: Option[Any]) {
    super.preRestart(reason, messgae)
    log.warning("""Command executor is going to restart!""")
  }

  override def postStop {
    super.postStop()
    log.info(s"Commands received: $commandsReceived, sequenced commands received: $sequencedCommandsReceived, command sequences received: $sequencesReceived.")
  }

}