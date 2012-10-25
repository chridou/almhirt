package almhirt.environment

import almhirt._
import almhirt.commanding.CommandEnvelope
import almhirt.messaging._
import almhirt.parts._

trait AlmhirtEnvironment extends Disposable {
  def context: AlmhirtContext
  
  def reportProblem(prob: Problem) { context.reportProblem(prob) }
  def executeCommand(commandEnvelope: CommandEnvelope) { context.commandExecutor.executeCommand(commandEnvelope) }
  
  def newUUID = context.uuidGenerator.generate 
  
  def dispose = context.dispose
}