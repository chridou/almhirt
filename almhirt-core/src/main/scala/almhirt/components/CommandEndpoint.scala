package almhirt.components

import almhirt.common._
import almhirt.commanding.ExecutionFinishedState

trait CommandEndpointWrapper {
  def execute(command: Command): Unit
  def executeTracked(command: Command): String
  def executeSync(command: Command, atMost: scala.concurrent.duration.FiniteDuration): AlmFuture[ExecutionFinishedState]
}