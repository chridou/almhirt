package almhirt.commanding

import scala.concurrent.duration.FiniteDuration
import akka.actor._
import almhirt.common._
import almhirt.configuration._
import almhirt.core.Almhirt
import almhirt.commanding._
import almhirt.components._
import almhirt.messaging.MessagePublisher
import almhirt.commanding.impl.CommandExecutorImpl
import com.typesafe.config.Config

trait CommandExecutor { actor: Actor with ActorLogging =>
  def receiveCommandExecutorMessage: Receive
}

object CommandExecutor {
  def propsRaw(
    handlers: CommandHandlerRegistry,
    repositories: AggregateRootRepositoryRegistry,
    messagePublisher: MessagePublisher,
    theAlmhirt: Almhirt,
    maxExecutionTimePerCommandWarnThreshold: FiniteDuration): Props =
    Props(new CommandExecutorImpl(handlers, repositories, messagePublisher, theAlmhirt, maxExecutionTimePerCommandWarnThreshold))
    
  def propsRaw(handlers: CommandHandlerRegistry, repositories: AggregateRootRepositoryRegistry, theAlmhirt: Almhirt, maxExecutionTimePerCommandWarnThreshold: FiniteDuration): Props =
    propsRaw(handlers, repositories, theAlmhirt.messageBus, theAlmhirt, maxExecutionTimePerCommandWarnThreshold)
    
  def props(handlers: CommandHandlerRegistry, repositories: AggregateRootRepositoryRegistry, theAlmhirt: Almhirt, configSection: Config): AlmValidation[Props] =
    for {
      maxExecutionTimePerCommandWarnThreshold <- configSection.v[FiniteDuration]("max-execution-time-per-command-warn-threshold")
    } yield {
      theAlmhirt.log.info(s"""CommandExecutor: max-execution-time-per-command-warn-threshold = ${maxExecutionTimePerCommandWarnThreshold.defaultUnitString}""")
      propsRaw(handlers: CommandHandlerRegistry, repositories, theAlmhirt, maxExecutionTimePerCommandWarnThreshold)
    }
    
  def props(handlers: CommandHandlerRegistry, repositories: AggregateRootRepositoryRegistry, theAlmhirt: Almhirt, configPath: String): AlmValidation[Props] =
    theAlmhirt.config.v[Config](configPath).flatMap(configSection =>
      props(handlers, repositories, theAlmhirt, configSection))

  def props(handlers: CommandHandlerRegistry, repositories: AggregateRootRepositoryRegistry, theAlmhirt: Almhirt): AlmValidation[Props] =
      props(handlers, repositories, theAlmhirt, "almhirt.command-executor")
      
}

