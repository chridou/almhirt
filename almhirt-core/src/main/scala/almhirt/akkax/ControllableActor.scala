package almhirt.akkax

import almhirt.herder.HerderMessages

/**
 * @author douven
 */
trait ControllableActor { me: AlmActor with AlmActorLogging ⇒
  protected implicit class ControllableActorReceiveOps(val rec: Receive) {
    def terminateStartup: Receive = rec orElse startupTerminator
    def terminateStartup(onPrepareShutDownBecome: Receive, transitionAction: () ⇒ Unit = () ⇒ {}): Receive = rec orElse startupTerminator(onPrepareShutDownBecome, transitionAction)
    def terminateRunning: Receive = runningTerminator
    def terminateRunning(onPause: Receive): Receive = rec orElse runningTerminator(onPause)
    def terminateRunning(onPause: Receive, onPrepareShutdown: Receive): Receive = rec orElse runningTerminator(onPause, onPrepareShutdown)
    def terminateRunning(onPause: Receive, onPrepareShutdown: Receive, transitionAction: () ⇒ Unit): Receive = rec orElse runningTerminator(onPause, onPrepareShutdown)
    def terminateRunning(onPrepareShutdown: Receive, transitionAction: () ⇒ Unit): Receive = rec orElse runningTerminator(onPrepareShutdown, transitionAction)
    def terminatePause: Receive = rec orElse pauseTerminator
    def terminatePause(onResume: Receive): Receive = rec orElse pauseTerminator(onResume)
    def terminatePause(onResume: Receive, onResumeTransitionAction: () ⇒ Unit): Receive = rec orElse pauseTerminator(onResume, onResumeTransitionAction)
    def terminatePauseWithPrepareShutdown(onResume: Receive, onResumeTransitionAction: () ⇒ Unit, onPrepareShutdown: Receive, onPrepareShutdownTransitionAction: () ⇒ Unit): Receive = rec orElse pauseTerminatorWithPrepareShutdown(onResume, onResumeTransitionAction, onPrepareShutdown, onPrepareShutdownTransitionAction)
    def terminatePauseWithPrepareShutdown(onResume: Receive, onPrepareShutdown: Receive, onPrepareShutdownTransitionAction: () ⇒ Unit): Receive = rec orElse pauseTerminatorWithPrepareShutdown(onResume, onPrepareShutdown, onPrepareShutdownTransitionAction)
    def terminatePauseWithPrepareShutdown(onResume: Receive, onResumeTransitionAction: () ⇒ Unit, onPrepareShutdown: Receive): Receive = rec orElse pauseTerminatorWithPrepareShutdown(onResume, onResumeTransitionAction, onPrepareShutdown)
    def terminatePauseWithPrepareShutdown(onResume: Receive, onPrepareShutdown: Receive): Receive = rec orElse pauseTerminatorWithPrepareShutdown(onResume, onPrepareShutdown)
    def createTerminateErrorFactory: (almhirt.problem.ProblemCause) ⇒ Receive = problem ⇒ {
      rec orElse createErrorTerminatorFactory(problem)
    }
    def createTerminateErrorFactory(onPrepareShutdown: Receive): (almhirt.problem.ProblemCause) ⇒ Receive = problem ⇒ {
      rec orElse createErrorTerminatorFactory(onPrepareShutdown)(problem)
    }
    def createTerminateErrorFactory(onPrepareShutdown: Receive, onPrepareShutdownTransitionAction: () ⇒ Unit): (almhirt.problem.ProblemCause) ⇒ Receive = problem ⇒ {
      rec orElse createErrorTerminatorFactory(onPrepareShutdown, onPrepareShutdownTransitionAction)(problem)
    }
    def createTerminateErrorWithRestartFactory(onPrepareShutdown: Receive, onPrepareShutdownTransitionAction: () ⇒ Unit, onRestart: Receive, onRestartTransitionAction: () ⇒ Unit): (almhirt.problem.ProblemCause) ⇒ Receive = problem ⇒ {
      rec orElse createErrorTerminatorWithRestartFactory(onPrepareShutdown, onPrepareShutdownTransitionAction, onRestart, onRestartTransitionAction)(problem)
    }
    def createTerminateErrorWithRestartFactory(onPrepareShutdown: Receive, onRestart: Receive, onRestartTransitionAction: () ⇒ Unit): (almhirt.problem.ProblemCause) ⇒ Receive = problem ⇒ {
      rec orElse createErrorTerminatorWithRestartFactory(onPrepareShutdown, onRestart, onRestartTransitionAction)(problem)
    }
    def createTerminateErrorWithRestartFactory(onPrepareShutdown: Receive, onPrepareShutdownTransitionAction: () ⇒ Unit, onRestart: Receive): (almhirt.problem.ProblemCause) ⇒ Receive = problem ⇒ {
      rec orElse createErrorTerminatorWithRestartFactory(onPrepareShutdown, onPrepareShutdownTransitionAction, onRestart)(problem)
    }
    def createTerminateErrorWithRestartFactory(onPrepareShutdown: Receive, onRestart: Receive): (almhirt.problem.ProblemCause) ⇒ Receive = problem ⇒ {
      rec orElse createErrorTerminatorWithRestartFactory(onPrepareShutdown, onRestart)(problem)
    }
    def terminatePreparingForShutdown: Receive = rec orElse preparingForShutdownTerminator
    def terminateReadyForShutdown: Receive = rec orElse readyForShutdownTerminator
  }

  def supportedComponentControlActions: Set[ActorMessages.ComponentControlAction]

  val componentControl: ComponentControl = ComponentControl(self, supportedComponentControlActions, Some(logWarning))

  def registerComponentControl()(implicit cnp: ActorComponentIdProvider): Unit =
    almhirtContext.tellHerder(HerderMessages.ComponentControlMessages.RegisterComponentControl(cnp.componentId, componentControl))

  def deregisterComponentControl()(implicit cnp: ActorComponentIdProvider): Unit =
    almhirtContext.tellHerder(HerderMessages.ComponentControlMessages.DeregisterComponentControl(cnp.componentId))

  def startUpHandlerScaffolding(onPrepareShutdown: Option[(Receive, () ⇒ Unit)]): PartialFunction[ActorMessages.ComponentControlMessage, Unit] = {
    case ActorMessages.Pause ⇒
      if (!componentControl.supports(ActorMessages.Pause))
        logWarning("Pause is not supported.")
      else
        logDebug("Pause not possible in state Startup")
    case ActorMessages.Resume ⇒
      if (!componentControl.supports(ActorMessages.Resume))
        logWarning("Resume is not supported.")
      else
        logDebug("Resume not possible in state Startup")
    case ActorMessages.Restart ⇒
      if (!componentControl.supports(ActorMessages.Restart))
        logWarning("Restart is not supported.")
      else
        logDebug("Restart not possible in state Startup")
    case ActorMessages.PrepareForShutdown ⇒
      if (!componentControl.supports(ActorMessages.PrepareForShutdown))
        logWarning("PrepareForShutdown is not supported.")
      else {
        onPrepareShutdown match {
          case Some((rec, act)) ⇒
            me.context.become(rec)
            act()
          case None ⇒
            logWarning("Received a supported PrepareForShutdown but there is no handler in state Startup")
        }
      }
    case ActorMessages.ReportComponentState ⇒
      sender() ! ComponentState.Startup
  }

  def runningHandlerScaffolding(onPause: Option[Receive], onPrepareShutdown: Option[(Receive, () ⇒ Unit)]): PartialFunction[ActorMessages.ComponentControlMessage, Unit] = {
    case ActorMessages.Pause ⇒
      if (!componentControl.supports(ActorMessages.Pause))
        logWarning("Pause is not supported.")
      else {
        onPause match {
          case Some(handler) ⇒ me.context.become(handler)
          case None          ⇒ logWarning("Received a supported Pause but there is no handler in state Running")
        }
      }
    case ActorMessages.Resume ⇒
      if (!componentControl.supports(ActorMessages.Resume))
        logWarning("Resume is not supported.")
      else
        logDebug("Resume not possible in state Running")
    case ActorMessages.Restart ⇒
      if (!componentControl.supports(ActorMessages.Restart))
        logWarning("Restart is not supported.")
      else
        logDebug("Restart not possible in state Running")
    case ActorMessages.PrepareForShutdown ⇒
      if (!componentControl.supports(ActorMessages.PrepareForShutdown))
        logWarning("PrepareForShutdown is not supported.")
      else {
        onPrepareShutdown match {
          case Some((rec, act)) ⇒
            me.context.become(rec)
            act()
          case None ⇒
            logWarning("Received a supported PrepareForShutdown but there is no handler in state Running")
        }
      }
    case ActorMessages.ReportComponentState ⇒
      sender() ! ComponentState.Running
  }

  def pauseHandlerScaffolding(onResume: Option[(Receive, () ⇒ Unit)], onPrepareShutdown: Option[(Receive, () ⇒ Unit)]): PartialFunction[ActorMessages.ComponentControlMessage, Unit] = {
    case ActorMessages.Pause ⇒
      if (!componentControl.supports(ActorMessages.Pause))
        logWarning("Pause is not supported.")
      else
        logDebug("Pause not possible in state Pause")
    case ActorMessages.Resume ⇒
      if (!componentControl.supports(ActorMessages.Resume))
        logWarning("Resume is not supported.")
      else {
        onResume match {
          case Some((rec, act)) ⇒
            me.context.become(rec)
            act()
          case None ⇒
            logWarning("Received a supported Resume but there is no handler in state Paused")
        }
      }
    case ActorMessages.Restart ⇒
      if (!componentControl.supports(ActorMessages.Restart))
        logWarning("Restart is not supported.")
      else
        logDebug("Restart not possible in state Pause")
    case ActorMessages.PrepareForShutdown ⇒
      if (!componentControl.supports(ActorMessages.PrepareForShutdown))
        logWarning("PrepareForShutdown is not supported.")
      else {
        onPrepareShutdown match {
          case Some((rec, act)) ⇒
            me.context.become(rec)
            act()
          case None ⇒
            logWarning("Received a supported PrepareForShutdown but there is no handler in state Paused")
        }
      }
    case ActorMessages.ReportComponentState ⇒
      sender() ! ComponentState.Paused
  }

  def errorHandlerScaffolding(onPrepareShutdown: Option[(Receive, () ⇒ Unit)], onRestart: Option[(Receive, () ⇒ Unit)])(error: almhirt.problem.ProblemCause): PartialFunction[ActorMessages.ComponentControlMessage, Unit] = {
    case ActorMessages.Pause ⇒
      if (!componentControl.supports(ActorMessages.Pause))
        logWarning("Pause is not supported.")
      else
        logDebug("Pause not possible in state Error")
    case ActorMessages.Resume ⇒
      if (!componentControl.supports(ActorMessages.Resume))
        logWarning("Resume is not supported.")
      else
        logDebug("Resume not possible in state Error")
    case ActorMessages.Restart ⇒
      if (!componentControl.supports(ActorMessages.Restart))
        logWarning("Restart is not supported.")
      else {
        onRestart match {
          case Some((rec, act)) ⇒
            me.context.become(rec)
            act()
          case None ⇒
            logWarning("Received a supported Restart but there is no handler in state Error")
        }
      }
    case ActorMessages.PrepareForShutdown ⇒
      if (!componentControl.supports(ActorMessages.PrepareForShutdown))
        logWarning("PrepareForShutdown is not supported.")
      else {
        onPrepareShutdown match {
          case Some((rec, act)) ⇒
            me.context.become(rec)
            act()
          case None ⇒
            logWarning("Received a supported PrepareForShutdown but there is no handler in state Error")
        }
      }
    case ActorMessages.ReportComponentState ⇒
      sender() ! ComponentState.Error(error)
  }

  lazy val preparingForShutdownHandler: PartialFunction[ActorMessages.ComponentControlMessage, Unit] = {
    case ActorMessages.Pause ⇒
      if (!componentControl.supports(ActorMessages.Pause))
        logWarning("Pause is not supported.")
      else
        logDebug("Pause not possible in state PreparingForShutdown")
    case ActorMessages.Resume ⇒
      if (!componentControl.supports(ActorMessages.Resume))
        logWarning("Resume is not supported.")
      else
        logDebug("Resume not possible in state PreparingForShutdown")
    case ActorMessages.Restart ⇒
      if (!componentControl.supports(ActorMessages.Restart))
        logWarning("Restart is not supported.")
      else
        logDebug("Restart not possible in state PreparingForShutdown")
    case ActorMessages.PrepareForShutdown ⇒
      if (!componentControl.supports(ActorMessages.PrepareForShutdown))
        logWarning("PrepareForShutdown is not supported.")
      else
        logDebug("PrepareForShutdown not possible in state PreparingForShutdown")
    case ActorMessages.ReportComponentState ⇒
      sender() ! ComponentState.PreparingForShutdown
  }

  lazy val readyForShutdownHandler: PartialFunction[ActorMessages.ComponentControlMessage, Unit] = {
    case ActorMessages.Pause ⇒
      if (!componentControl.supports(ActorMessages.Pause))
        logWarning("Pause is not supported.")
      else
        logDebug("Pause not possible in state ReadyForShutdown")
    case ActorMessages.Resume ⇒
      if (!componentControl.supports(ActorMessages.Resume))
        logWarning("Resume is not supported.")
      else
        logDebug("Resume not possible in state ReadyForShutdown")
    case ActorMessages.Restart ⇒
      if (!componentControl.supports(ActorMessages.Restart))
        logWarning("Restart is not supported.")
      else
        logDebug("Restart not possible in state ReadyForShutdown")
    case ActorMessages.PrepareForShutdown ⇒
      if (!componentControl.supports(ActorMessages.PrepareForShutdown))
        logWarning("PrepareForShutdown is not supported.")
      else
        logDebug("PrepareForShutdown not possible in state ReadyForShutdown")
    case ActorMessages.ReportComponentState ⇒
      sender() ! ComponentState.ReadyForShutdown
  }

  val startUpHandler: PartialFunction[ActorMessages.ComponentControlMessage, Unit] = startUpHandlerScaffolding(None)
  def startUpHandler(onPrepareShutDownBecome: Receive, transitionAction: () ⇒ Unit = () ⇒ {}) = startUpHandlerScaffolding(Some(onPrepareShutDownBecome, transitionAction))
  val startupTerminator: Receive = {
    case m: ActorMessages.ComponentControlMessage ⇒
      startUpHandler(m)
  }
  def startupTerminator(onPrepareShutDownBecome: Receive, transitionAction: () ⇒ Unit = () ⇒ {}): Receive = {
    case m: ActorMessages.ComponentControlMessage ⇒
      startUpHandler(onPrepareShutDownBecome, transitionAction)(m)
  }

  def startup()(receive: Receive): Receive = receive.terminateStartup
  def startup(onPrepareShutDownBecome: Receive)(receive: Receive): Receive = receive.terminateStartup(onPrepareShutDownBecome)
  def startup(onPrepareShutDownBecome: Receive, transitionAction: () ⇒ Unit)(receive: Receive): Receive = receive.terminateStartup(onPrepareShutDownBecome, transitionAction)

  val runningHandler: PartialFunction[ActorMessages.ComponentControlMessage, Unit] = runningHandlerScaffolding(None, None)
  def runningHandler(onPause: Receive): PartialFunction[ActorMessages.ComponentControlMessage, Unit] = runningHandlerScaffolding(Some(onPause), None)
  def runningHandler(onPause: Receive, onPrepareShutdown: Receive): PartialFunction[ActorMessages.ComponentControlMessage, Unit] = runningHandlerScaffolding(Some(onPause), Some(onPrepareShutdown, () ⇒ {}))
  def runningHandler(onPause: Receive, onPrepareShutdown: Receive, transitionAction: () ⇒ Unit): PartialFunction[ActorMessages.ComponentControlMessage, Unit] = runningHandlerScaffolding(Some(onPause), Some(onPrepareShutdown, transitionAction))
  def runningHandler(onPrepareShutdown: Receive, transitionAction: () ⇒ Unit): PartialFunction[ActorMessages.ComponentControlMessage, Unit] = runningHandlerScaffolding(None, Some(onPrepareShutdown, transitionAction))
  val runningTerminator: Receive = {
    case m: ActorMessages.ComponentControlMessage ⇒ runningHandler(m)
  }
  def runningTerminator(onPause: Receive): Receive = {
    case m: ActorMessages.ComponentControlMessage ⇒ runningHandler(onPause)(m)
  }
  def runningTerminator(onPause: Receive, onPrepareShutdown: Receive): Receive = {
    case m: ActorMessages.ComponentControlMessage ⇒ runningHandler(onPause, onPrepareShutdown)(m)
  }
  def runningTerminator(onPause: Receive, onPrepareShutdown: Receive, transitionAction: () ⇒ Unit): Receive = {
    case m: ActorMessages.ComponentControlMessage ⇒ runningHandler(onPause, onPrepareShutdown, transitionAction)(m)
  }
  def runningTerminator(onPrepareShutdown: Receive, transitionAction: () ⇒ Unit): Receive = {
    case m: ActorMessages.ComponentControlMessage ⇒ runningHandler(onPrepareShutdown, transitionAction)(m)
  }

  def running()(receive: Receive): Receive = receive.terminateRunning
  def running(onPause: Receive)(receive: Receive): Receive = receive.terminateRunning(onPause)
  def runningWithPrepareShutdown(onPause: Receive, onPrepareShutdown: Receive)(receive: Receive): Receive = receive.terminateRunning(onPause, onPrepareShutdown)
  def runningWithPrepareShutdown(onPause: Receive, onPrepareShutdown: Receive, transitionAction: () ⇒ Unit)(receive: Receive): Receive = receive.terminateRunning(onPause, onPrepareShutdown, transitionAction)
  def runningPrepareShutdownOnly(onPrepareShutdown: Receive)(receive: Receive): Receive = receive.terminateRunning(onPrepareShutdown, () ⇒ {})
  def runningPrepareShutdownOnly(onPrepareShutdown: Receive, transitionAction: () ⇒ Unit)(receive: Receive): Receive = receive.terminateRunning(onPrepareShutdown, transitionAction)

  val pauseHandler: PartialFunction[ActorMessages.ComponentControlMessage, Unit] = pauseHandlerScaffolding(None, None)
  def pauseHandler(onResume: Receive): PartialFunction[ActorMessages.ComponentControlMessage, Unit] = pauseHandlerScaffolding(Some(onResume, () ⇒ {}), None)
  def pauseHandler(onResume: Receive, onResumeTransitionAction: () ⇒ Unit): PartialFunction[ActorMessages.ComponentControlMessage, Unit] = pauseHandlerScaffolding(Some(onResume, onResumeTransitionAction), None)
  def pauseHandlerWithPrepareShutdown(onResume: Receive, onResumeTransitionAction: () ⇒ Unit, onPrepareShutdown: Receive, onPrepareShutdownTransitionAction: () ⇒ Unit): PartialFunction[ActorMessages.ComponentControlMessage, Unit] = pauseHandlerScaffolding(Some(onResume, onResumeTransitionAction), Some(onPrepareShutdown, onPrepareShutdownTransitionAction))
  def pauseHandlerWithPrepareShutdown(onResume: Receive, onPrepareShutdown: Receive, onPrepareShutdownTransitionAction: () ⇒ Unit): PartialFunction[ActorMessages.ComponentControlMessage, Unit] = pauseHandlerScaffolding(Some(onResume, () ⇒ {}), Some(onPrepareShutdown, onPrepareShutdownTransitionAction))
  def pauseHandlerWithPrepareShutdown(onResume: Receive, onResumeTransitionAction: () ⇒ Unit, onPrepareShutdown: Receive): PartialFunction[ActorMessages.ComponentControlMessage, Unit] = pauseHandlerScaffolding(Some(onResume, onResumeTransitionAction), Some(onPrepareShutdown, () ⇒ {}))
  def pauseHandlerWithPrepareShutdown(onResume: Receive, onPrepareShutdown: Receive): PartialFunction[ActorMessages.ComponentControlMessage, Unit] = pauseHandlerScaffolding(Some(onResume, () ⇒ {}), Some(onPrepareShutdown, () ⇒ {}))

  val pauseTerminator: Receive = {
    case m: ActorMessages.ComponentControlMessage ⇒ pauseHandlerScaffolding(None, None)(m)
  }
  def pauseTerminator(onResume: Receive): Receive = {
    case m: ActorMessages.ComponentControlMessage ⇒ pauseHandlerScaffolding(Some(onResume, () ⇒ {}), None)(m)
  }
  def pauseTerminator(onResume: Receive, onResumeTransitionAction: () ⇒ Unit): Receive = {
    case m: ActorMessages.ComponentControlMessage ⇒ pauseHandlerScaffolding(Some(onResume, onResumeTransitionAction), None)(m)
  }
  def pauseTerminatorWithPrepareShutdown(onResume: Receive, onResumeTransitionAction: () ⇒ Unit, onPrepareShutdown: Receive, onPrepareShutdownTransitionAction: () ⇒ Unit): Receive = {
    case m: ActorMessages.ComponentControlMessage ⇒ pauseHandlerScaffolding(Some(onResume, onResumeTransitionAction), Some(onPrepareShutdown, onPrepareShutdownTransitionAction))(m)
  }
  def pauseTerminatorWithPrepareShutdown(onResume: Receive, onPrepareShutdown: Receive, onPrepareShutdownTransitionAction: () ⇒ Unit): Receive = {
    case m: ActorMessages.ComponentControlMessage ⇒ pauseHandlerScaffolding(Some(onResume, () ⇒ {}), Some(onPrepareShutdown, onPrepareShutdownTransitionAction))(m)
  }
  def pauseTerminatorWithPrepareShutdown(onResume: Receive, onResumeTransitionAction: () ⇒ Unit, onPrepareShutdown: Receive): Receive = {
    case m: ActorMessages.ComponentControlMessage ⇒ pauseHandlerScaffolding(Some(onResume, onResumeTransitionAction), Some(onPrepareShutdown, () ⇒ {}))(m)
  }
  def pauseTerminatorWithPrepareShutdown(onResume: Receive, onPrepareShutdown: Receive): Receive = {
    case m: ActorMessages.ComponentControlMessage ⇒ pauseHandlerScaffolding(Some(onResume, () ⇒ {}), Some(onPrepareShutdown, () ⇒ {}))(m)
  }

  def pause()(receive: Receive): Receive = receive.terminatePause
  def pause(onResume: Receive)(receive: Receive): Receive = receive.terminatePause(onResume)
  def pause(onResume: Receive, onResumeTransitionAction: () ⇒ Unit)(receive: Receive): Receive = receive.terminatePause(onResume, onResumeTransitionAction)
  def pauseWithPrepareShutdown(onResume: Receive, onResumeTransitionAction: () ⇒ Unit, onPrepareShutdown: Receive, onPrepareShutdownTransitionAction: () ⇒ Unit)(receive: Receive): Receive = receive.terminatePauseWithPrepareShutdown(onResume, onResumeTransitionAction, onPrepareShutdown, onPrepareShutdownTransitionAction)
  def pauseWithPrepareShutdown(onResume: Receive, onPrepareShutdown: Receive, onPrepareShutdownTransitionAction: () ⇒ Unit)(receive: Receive): Receive = receive.terminatePauseWithPrepareShutdown(onResume, onPrepareShutdown, onPrepareShutdownTransitionAction)
  def pauseWithPrepareShutdown(onResume: Receive, onResumeTransitionAction: () ⇒ Unit, onPrepareShutdown: Receive)(receive: Receive): Receive = receive.terminatePauseWithPrepareShutdown(onResume, onResumeTransitionAction, onPrepareShutdown)
  def pauseWithPrepareShutdown(onResume: Receive, onPrepareShutdown: Receive)(receive: Receive): Receive = receive.terminatePauseWithPrepareShutdown(onResume, onPrepareShutdown)

  def createErrorHandlerFactory: (almhirt.problem.ProblemCause) ⇒ PartialFunction[ActorMessages.ComponentControlMessage, Unit] = errorHandlerScaffolding(None, None)
  def createErrorHandlerFactory(onPrepareShutdown: Receive): (almhirt.problem.ProblemCause) ⇒ PartialFunction[ActorMessages.ComponentControlMessage, Unit] = errorHandlerScaffolding(Some(onPrepareShutdown, () ⇒ {}), None)
  def createErrorHandlerFactory(onPrepareShutdown: Receive, onPrepareShutdownTransitionAction: () ⇒ Unit): (almhirt.problem.ProblemCause) ⇒ PartialFunction[ActorMessages.ComponentControlMessage, Unit] = errorHandlerScaffolding(Some(onPrepareShutdown, onPrepareShutdownTransitionAction), None)
  def createErrorHandlerWithRestartFactory(onPrepareShutdown: Receive, onPrepareShutdownTransitionAction: () ⇒ Unit, onRestart: Receive, onRestartTransitionAction: () ⇒ Unit): (almhirt.problem.ProblemCause) ⇒ PartialFunction[ActorMessages.ComponentControlMessage, Unit] = errorHandlerScaffolding(Some(onPrepareShutdown, onPrepareShutdownTransitionAction), Some(onRestart, onRestartTransitionAction))
  def createErrorHandlerWithRestartFactory(onPrepareShutdown: Receive, onRestart: Receive, onRestartTransitionAction: () ⇒ Unit): (almhirt.problem.ProblemCause) ⇒ PartialFunction[ActorMessages.ComponentControlMessage, Unit] = errorHandlerScaffolding(Some(onPrepareShutdown, () ⇒ {}), Some(onRestart, onRestartTransitionAction))
  def createErrorHandlerWithRestartFactory(onPrepareShutdown: Receive, onPrepareShutdownTransitionAction: () ⇒ Unit, onRestart: Receive): (almhirt.problem.ProblemCause) ⇒ PartialFunction[ActorMessages.ComponentControlMessage, Unit] = errorHandlerScaffolding(Some(onPrepareShutdown, onPrepareShutdownTransitionAction), Some(onRestart, () ⇒ {}))
  def createErrorHandlerWithRestartFactory(onPrepareShutdown: Receive, onRestart: Receive): (almhirt.problem.ProblemCause) ⇒ PartialFunction[ActorMessages.ComponentControlMessage, Unit] = errorHandlerScaffolding(Some(onPrepareShutdown, () ⇒ {}), Some(onRestart, () ⇒ {}))

  def createErrorTerminatorFactory: (almhirt.problem.ProblemCause) ⇒ Receive = problem ⇒ {
    case m: ActorMessages.ComponentControlMessage ⇒ errorHandlerScaffolding(None, None)(problem)(m)
  }
  def createErrorTerminatorFactory(onPrepareShutdown: Receive): (almhirt.problem.ProblemCause) ⇒ Receive = problem ⇒ {
    case m: ActorMessages.ComponentControlMessage ⇒ errorHandlerScaffolding(Some(onPrepareShutdown, () ⇒ {}), None)(problem)(m)
  }
  def createErrorTerminatorFactory(onPrepareShutdown: Receive, onPrepareShutdownTransitionAction: () ⇒ Unit): (almhirt.problem.ProblemCause) ⇒ Receive = problem ⇒ {
    case m: ActorMessages.ComponentControlMessage ⇒ errorHandlerScaffolding(Some(onPrepareShutdown, onPrepareShutdownTransitionAction), None)(problem)(m)
  }
  def createErrorTerminatorWithRestartFactory(onPrepareShutdown: Receive, onPrepareShutdownTransitionAction: () ⇒ Unit, onRestart: Receive, onRestartTransitionAction: () ⇒ Unit): (almhirt.problem.ProblemCause) ⇒ Receive = problem ⇒ {
    case m: ActorMessages.ComponentControlMessage ⇒ errorHandlerScaffolding(Some(onPrepareShutdown, onPrepareShutdownTransitionAction), Some(onRestart, onRestartTransitionAction))(problem)(m)
  }
  def createErrorTerminatorWithRestartFactory(onPrepareShutdown: Receive, onRestart: Receive, onRestartTransitionAction: () ⇒ Unit): (almhirt.problem.ProblemCause) ⇒ Receive = problem ⇒ {
    case m: ActorMessages.ComponentControlMessage ⇒ errorHandlerScaffolding(Some(onPrepareShutdown, () ⇒ {}), Some(onRestart, onRestartTransitionAction))(problem)(m)
  }
  def createErrorTerminatorWithRestartFactory(onPrepareShutdown: Receive, onPrepareShutdownTransitionAction: () ⇒ Unit, onRestart: Receive): (almhirt.problem.ProblemCause) ⇒ Receive = problem ⇒ {
    case m: ActorMessages.ComponentControlMessage ⇒ errorHandlerScaffolding(Some(onPrepareShutdown, onPrepareShutdownTransitionAction), Some(onRestart, () ⇒ {}))(problem)(m)
  }
  def createErrorTerminatorWithRestartFactory(onPrepareShutdown: Receive, onRestart: Receive): (almhirt.problem.ProblemCause) ⇒ Receive = problem ⇒ {
    case m: ActorMessages.ComponentControlMessage ⇒ errorHandlerScaffolding(Some(onPrepareShutdown, () ⇒ {}), Some(onRestart, () ⇒ {}))(problem)(m)
  }

  def error(error: almhirt.problem.ProblemCause)(receive: Receive): Receive = receive.createTerminateErrorFactory(error)
  def error(onPrepareShutdown: Receive, error: almhirt.problem.ProblemCause)(receive: Receive): Receive = receive.createTerminateErrorFactory(onPrepareShutdown)(error)
  def error(onPrepareShutdown: Receive, onPrepareShutdownTransitionAction: () ⇒ Unit, error: almhirt.problem.ProblemCause)(receive: Receive): Receive = receive.createTerminateErrorFactory(onPrepareShutdown, onPrepareShutdownTransitionAction)(error)
  def errorWithRestart(onPrepareShutdown: Receive, onPrepareShutdownTransitionAction: () ⇒ Unit, onRestart: Receive, onRestartTransitionAction: () ⇒ Unit, error: almhirt.problem.ProblemCause)(receive: Receive): Receive = receive.createTerminateErrorWithRestartFactory(onPrepareShutdown, onPrepareShutdownTransitionAction, onRestart, onRestartTransitionAction)(error)
  def errorWithRestart(onPrepareShutdown: Receive, onRestart: Receive, onRestartTransitionAction: () ⇒ Unit, error: almhirt.problem.ProblemCause)(receive: Receive): Receive = receive.createTerminateErrorWithRestartFactory(onPrepareShutdown, onRestart, onRestartTransitionAction)(error)
  def errorWithRestart(onPrepareShutdown: Receive, onPrepareShutdownTransitionAction: () ⇒ Unit, onRestart: Receive, error: almhirt.problem.ProblemCause)(receive: Receive): Receive = receive.createTerminateErrorWithRestartFactory(onPrepareShutdown, onPrepareShutdownTransitionAction, onRestart)(error)
  def errorWithRestart(onPrepareShutdown: Receive, onRestart: Receive, error: almhirt.problem.ProblemCause)(receive: Receive): Receive = receive.createTerminateErrorWithRestartFactory(onPrepareShutdown, onRestart)(error)

  val preparingForShutdownTerminator: Receive = {
    case m: ActorMessages.ComponentControlMessage ⇒ preparingForShutdownHandler(m)
  }
  def preparingForShutdown(receive: Receive): Receive = receive.terminatePreparingForShutdown

  val readyForShutdownTerminator: Receive = {
    case m: ActorMessages.ComponentControlMessage ⇒ readyForShutdownHandler(m)
  }
  def readyForShutdown(receive: Receive): Receive = receive.terminateReadyForShutdown

}
