package almhirt.environment.configuration

import scala.concurrent.ExecutionContext
import akka.actor.ActorSystem
import almhirt.environment._
import almhirt.common.HasExecutionContext
import com.typesafe.config.Config

trait MessagingFoundations extends HasActorSystem with HasExecutionContext

object MessagingFoundations {
  def apply(anActorSystem: ActorSystem, anExecutionContext: ExecutionContext): MessagingFoundations = {
    new MessagingFoundations {
      override val actorSystem = anActorSystem
      override val executionContext = anExecutionContext
    }
  }
}