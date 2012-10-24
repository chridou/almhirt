package almhirt.environment

import almhirt._
import almhirt.messaging._
import almhirt.parts._

trait AlmhirtEnvironment {
  def context: AlmhirtContext
  def repositories: HasRepositories
  def commandExecutor: CommandExecutor
  def reportProblem(prob: Problem) { context.problemChannel.post(Message.createWithUuid(prob)) }
}