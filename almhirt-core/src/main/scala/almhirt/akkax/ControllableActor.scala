package almhirt.akkax

import almhirt.herder.HerderMessages

/**
 * @author douven
 */
trait ControllableActor { me: AlmActor with AlmActorLogging ⇒
  def componentControl: ComponentControl

  def registerComponentControl()(implicit cnp: ActorComponentIdProvider): Unit =
    almhirtContext.tellHerder(HerderMessages.ComponentControlMessages.RegisterComponentControl(cnp.componentId, componentControl))

  def deregisterComponentControl()(implicit cnp: ActorComponentIdProvider): Unit =
    almhirtContext.tellHerder(HerderMessages.ComponentControlMessages.DeregisterComponentControl(cnp.componentId))

  def startupTerminator: Receive = {
    case ActorMessages.Pause   ⇒
      logWarning("Pause not possible in Startup state.")
    case ActorMessages.Resume  ⇒
      logWarning("Resume not possible in Startup state.")
    case ActorMessages.Restart ⇒
      logWarning("Restart not possible in Startup state.")
    case ActorMessages.ReportComponentState =>
      sender() ! ComponentState.Startup
  }
}

trait ControllableActorReportsOnly { me: ControllableActor with AlmActor with AlmActorLogging =>
  val componentControl = ComponentControl.reportsOnly(self)
  
  def runningTerminator: Receive = {
    case ActorMessages.Pause   ⇒
      logWarning("Pause is not supported.")
    case ActorMessages.Resume  ⇒
      logWarning("Resume is not supported.")
    case ActorMessages.Restart ⇒
      logWarning("Restart is not supported.")
    case ActorMessages.ReportComponentState =>
      sender() ! ComponentState.Running
  } 
 
  val runningHandler: PartialFunction[ActorMessages.ComponentControlMessage, Unit] = {
    case ActorMessages.Pause   ⇒
      logWarning("Pause is not supported.")
    case ActorMessages.Resume  ⇒
      logWarning("Resume is not supported.")
    case ActorMessages.Restart ⇒
      logWarning("Restart is not supported.")
    case ActorMessages.ReportComponentState =>
      sender() ! ComponentState.Running
  } 
  
  def errorTerminator(cause: almhirt.problem.ProblemCause): Receive = {
    case ActorMessages.Pause   ⇒
      logWarning("Pause is not supported.")
    case ActorMessages.Resume  ⇒
      logWarning("Resume is not supported.")
    case ActorMessages.Restart ⇒
      logWarning("Restart is not supported.")
    case ActorMessages.ReportComponentState =>
      sender() ! ComponentState.Error(cause)
  } 
}

trait ControllableActorWithPauseResume { me: ControllableActor with AlmActor with AlmActorLogging =>
  val componentControl = ComponentControl.pauseResume(self)
  
  def runningControlHandler(receivePause: Receive): PartialFunction[ActorMessages.ComponentControlMessage, Unit] = {
    case ActorMessages.Pause   ⇒
      logInfo("Pause.")
      context.become(receivePause)
    case ActorMessages.Resume  ⇒
      logWarning("Resume is not possible when running.")
    case ActorMessages.Restart ⇒
      logWarning("Restart is not supported.")
    case ActorMessages.ReportComponentState =>
      sender() ! ComponentState.Running
  } 

  def pauseControlHandler(receiveRunning: Receive): PartialFunction[ActorMessages.ComponentControlMessage, Unit] = {
    case ActorMessages.Pause   ⇒
      logWarning("Pause is not possible when pausing.")
    case ActorMessages.Resume  ⇒
      logInfo("Resume.")
      context.become(receiveRunning)
    case ActorMessages.Restart ⇒
      logWarning("Restart is not supported.")
    case ActorMessages.ReportComponentState =>
      sender() ! ComponentState.Running
  } 
  
  def errorTerminator(cause: almhirt.problem.ProblemCause): Receive = {
    case ActorMessages.Pause   ⇒
      logWarning("Pause is not possible in error state.")
    case ActorMessages.Resume  ⇒
      logWarning("Resume is not possible in error state.")
    case ActorMessages.Restart ⇒
      logWarning("Restart is not supported.")
    case ActorMessages.ReportComponentState =>
      sender() ! ComponentState.Error(cause)
  } 
}