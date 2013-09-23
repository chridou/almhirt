package almhirt.commanding.impl

import akka.actor._
import almhirt.core.Almhirt
import almhirt.commanding._
import almhirt.components._
import almhirt.messaging.MessagePublisher

class CommandExecutorImpl(
    val handlers: CommandHandlerRegistry,
    val repositories: AggregateRootRepositoryRegistry,
    val messagePublisher: MessagePublisher,
    val theAlmhirt: Almhirt) extends CommandExecutor with CommandExecutorTemplate with Actor with ActorLogging {

    val domainCommandsSequencer = context.actorOf(Props(new DomainCommandsSequencerImpl(theAlmhirt)), "DomainCommandsSequencer")
    
    def receive: Receive = receiveCommandExecutorMessage
}