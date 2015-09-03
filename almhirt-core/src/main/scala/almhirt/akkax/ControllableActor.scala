package almhirt.akkax

import almhirt.herder.HerderMessages

/**
 * @author douven
 */
trait ControllableActor { me: AlmActor with AlmActorLogging ⇒
  type ReceiveFun = () ⇒ Receive

  protected implicit class ControllableActorReceiveOps(val rec: Receive) {
    def terminateStartup: Receive = rec orElse startupTerminator
    def terminateStartup(onPrepareShutDownBecome: ⇒ Receive, transitionAction: () ⇒ Unit = () ⇒ {}): Receive = rec orElse startupTerminator(onPrepareShutDownBecome, transitionAction)
    def terminateWaitingForStartSignal: Receive = rec orElse waitingForStartSignalTerminator
    def terminateWaitingForStartSignal(onPrepareShutDownBecome: ⇒ Receive, transitionAction: () ⇒ Unit = () ⇒ {}): Receive = rec orElse waitingForStartSignalTerminator(onPrepareShutDownBecome, transitionAction)
    def terminateRunning: Receive = rec orElse runningTerminator
    def terminateRunningWithPause(onPause: ⇒ Receive): Receive = rec orElse runningTerminatorWithPause(onPause)
    def terminateRunningWithPauseAndPrepareShutdown(onPause: ⇒ Receive, onPrepareShutdown: ⇒ Receive): Receive = rec orElse runningTerminatorWithPauseAndPrepareShutdown(onPause, onPrepareShutdown)
    def terminateRunningWithPauseAndPrepareShutdown(onPause: ⇒ Receive, onPrepareShutdown: ⇒ Receive, transitionAction: () ⇒ Unit): Receive = rec orElse runningTerminatorWithPauseAndPrepareShutdown(onPause, onPrepareShutdown)
    def terminateRunningWithPrepareShutdown(onPrepareShutdown: ⇒ Receive, transitionAction: () ⇒ Unit): Receive = rec orElse runningTerminatorWithPrepareShutdown(onPrepareShutdown, transitionAction)
    def terminatePause: Receive = rec orElse pauseTerminator
    def terminatePause(onResume: ⇒ Receive): Receive = rec orElse pauseTerminator(onResume)
    def terminatePause(onResume: ⇒ Receive, onResumeTransitionAction: () ⇒ Unit): Receive = rec orElse pauseTerminator(onResume, onResumeTransitionAction)
    def terminatePauseWithPrepareShutdown1(onResume: ⇒ Receive, onResumeTransitionAction: () ⇒ Unit, onPrepareShutdown: ⇒ Receive, onPrepareShutdownTransitionAction: () ⇒ Unit): Receive = rec orElse pauseTerminatorWithPrepareShutdown1(onResume, onResumeTransitionAction, onPrepareShutdown, onPrepareShutdownTransitionAction)
    def terminatePauseWithPrepareShutdown2(onResume: ⇒ Receive, onPrepareShutdown: ⇒ Receive, onPrepareShutdownTransitionAction: () ⇒ Unit): Receive = rec orElse pauseTerminatorWithPrepareShutdown2(onResume, onPrepareShutdown, onPrepareShutdownTransitionAction)
    def terminatePauseWithPrepareShutdown3(onResume: ⇒ Receive, onResumeTransitionAction: () ⇒ Unit, onPrepareShutdown: ⇒ Receive): Receive = rec orElse pauseTerminatorWithPrepareShutdown3(onResume, onResumeTransitionAction, onPrepareShutdown)
    def terminatePauseWithPrepareShutdown4(onResume: ⇒ Receive, onPrepareShutdown: ⇒ Receive): Receive = rec orElse pauseTerminatorWithPrepareShutdown4(onResume, onPrepareShutdown)
    def terminatePreparingForPause: Receive = rec orElse preparingForPauseTerminator
    def terminatePreparingForPause(onResume: ⇒ Receive): Receive = rec orElse preparingForPauseTerminator(onResume)
    def terminatePreparingForPause(onResume: ⇒ Receive, onResumeTransitionAction: () ⇒ Unit): Receive = rec orElse preparingForPauseTerminator(onResume, onResumeTransitionAction)
    def terminatePreparingForPauseWithPrepareShutdown1(onResume: ⇒ Receive, onResumeTransitionAction: () ⇒ Unit, onPrepareShutdown: ⇒ Receive, onPrepareShutdownTransitionAction: () ⇒ Unit): Receive = rec orElse preparingForPauseTerminatorWithPrepareShutdown1(onResume, onResumeTransitionAction, onPrepareShutdown, onPrepareShutdownTransitionAction)
    def terminatePreparingForPauseWithPrepareShutdown2(onResume: ⇒ Receive, onPrepareShutdown: ⇒ Receive, onPrepareShutdownTransitionAction: () ⇒ Unit): Receive = rec orElse preparingForPauseTerminatorWithPrepareShutdown2(onResume, onPrepareShutdown, onPrepareShutdownTransitionAction)
    def terminatePreparingForPauseWithPrepareShutdown3(onResume: ⇒ Receive, onResumeTransitionAction: () ⇒ Unit, onPrepareShutdown: ⇒ Receive): Receive = rec orElse preparingForPauseTerminatorWithPrepareShutdown3(onResume, onResumeTransitionAction, onPrepareShutdown)
    def terminatePreparingForPauseWithPrepareShutdown4(onResume: ⇒ Receive, onPrepareShutdown: ⇒ Receive): Receive = rec orElse preparingForPauseTerminatorWithPrepareShutdown4(onResume, onPrepareShutdown)
    def createTerminateErrorFactory: (almhirt.problem.ProblemCause) ⇒ Receive = problem ⇒ {
      rec orElse createErrorTerminatorFactory(problem)
    }
    def createTerminateErrorFactory(onPrepareShutdown: ⇒ Receive): (almhirt.problem.ProblemCause) ⇒ Receive = problem ⇒ {
      rec orElse createErrorTerminatorFactory(onPrepareShutdown)(problem)
    }
    def createTerminateErrorFactory(onPrepareShutdown: ⇒ Receive, onPrepareShutdownTransitionAction: () ⇒ Unit): (almhirt.problem.ProblemCause) ⇒ Receive = problem ⇒ {
      rec orElse createErrorTerminatorFactory(onPrepareShutdown, onPrepareShutdownTransitionAction)(problem)
    }
    def createTerminateErrorWithRestartFactory1(onPrepareShutdown: ⇒ Receive, onPrepareShutdownTransitionAction: () ⇒ Unit, onRestart: Receive, onRestartTransitionAction: () ⇒ Unit): (almhirt.problem.ProblemCause) ⇒ Receive = problem ⇒ {
      rec orElse createErrorTerminatorWithRestartFactory1(onPrepareShutdown, onPrepareShutdownTransitionAction, onRestart, onRestartTransitionAction)(problem)
    }
    def createTerminateErrorWithRestartFactory2(onPrepareShutdown: ⇒ Receive, onRestart: ⇒ Receive, onRestartTransitionAction: () ⇒ Unit): (almhirt.problem.ProblemCause) ⇒ Receive = problem ⇒ {
      rec orElse createErrorTerminatorWithRestartFactory2(onPrepareShutdown, onRestart, onRestartTransitionAction)(problem)
    }
    def createTerminateErrorWithRestartFactory3(onPrepareShutdown: ⇒ Receive, onPrepareShutdownTransitionAction: () ⇒ Unit, onRestart: Receive): (almhirt.problem.ProblemCause) ⇒ Receive = problem ⇒ {
      rec orElse createErrorTerminatorWithRestartFactory3(onPrepareShutdown, onPrepareShutdownTransitionAction, onRestart)(problem)
    }
    def createTerminateErrorWithRestartFactory4(onPrepareShutdown: ⇒ Receive, onRestart: ⇒ Receive): (almhirt.problem.ProblemCause) ⇒ Receive = problem ⇒ {
      rec orElse createErrorTerminatorWithRestartFactory4(onPrepareShutdown, onRestart)(problem)
    }
    def terminatePreparingForShutdown: Receive = rec orElse preparingForShutdownTerminator
    def terminateReadyForShutdown: Receive = rec orElse readyForShutdownTerminator
  }

  def componentControl: LocalComponentControl 

  def registerComponentControl()(implicit cnp: ActorComponentIdProvider): Unit =
    almhirtContext.tellHerder(HerderMessages.ComponentControlMessages.RegisterComponentControl(cnp.componentId, componentControl.toRestrictedComponentControl))

  def deregisterComponentControl()(implicit cnp: ActorComponentIdProvider): Unit =
    almhirtContext.tellHerder(HerderMessages.ComponentControlMessages.DeregisterComponentControl(cnp.componentId))

  def startupHandlerScaffolding(onPrepareShutdown: Option[(ReceiveFun, () ⇒ Unit)]): PartialFunction[ActorMessages.ComponentControlMessage, Unit] = {
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
            me.context.become(rec())
            act()
          case None ⇒
            logWarning("Received a supported PrepareForShutdown but there is no handler in state Startup")
        }
      }
    case ActorMessages.ReportComponentState ⇒
      sender() ! ComponentState.Startup
  }

  def waitingForStartSignalHandlerScaffolding(onPrepareShutdown: Option[(ReceiveFun, () ⇒ Unit)]): PartialFunction[ActorMessages.ComponentControlMessage, Unit] = {
    case ActorMessages.Pause ⇒
      if (!componentControl.supports(ActorMessages.Pause))
        logWarning("Pause is not supported.")
      else
        logDebug("Pause not possible in state WaitingForStartSignal")
    case ActorMessages.Resume ⇒
      if (!componentControl.supports(ActorMessages.Resume))
        logWarning("Resume is not supported.")
      else
        logDebug("Resume not possible in state WaitingForStartSignal")
    case ActorMessages.Restart ⇒
      if (!componentControl.supports(ActorMessages.Restart))
        logWarning("Restart is not supported.")
      else
        logDebug("Restart not possible in state WaitingForStartSignal")
    case ActorMessages.PrepareForShutdown ⇒
      if (!componentControl.supports(ActorMessages.PrepareForShutdown))
        logWarning("PrepareForShutdown is not supported.")
      else {
        onPrepareShutdown match {
          case Some((rec, act)) ⇒
            me.context.become(rec())
            act()
          case None ⇒
            logWarning("Received a supported PrepareForShutdown but there is no handler in state WaitingForStartSignal")
        }
      }
    case ActorMessages.ReportComponentState ⇒
      sender() ! ComponentState.WaitingForStartSignal
  }

  
  def runningHandlerScaffolding(onPause: Option[ReceiveFun], onPrepareShutdown: Option[(ReceiveFun, () ⇒ Unit)]): PartialFunction[ActorMessages.ComponentControlMessage, Unit] = {
    case ActorMessages.Pause ⇒
      if (!componentControl.supports(ActorMessages.Pause))
        logWarning("Pause is not supported.")
      else {
        onPause match {
          case Some(handler) ⇒ me.context.become(handler())
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
            me.context.become(rec())
            act()
          case None ⇒
            logWarning("Received a supported PrepareForShutdown but there is no handler in state Running")
        }
      }
    case ActorMessages.ReportComponentState ⇒
      sender() ! ComponentState.Running
  }

  def pauseHandlerScaffolding(onResume: Option[(ReceiveFun, () ⇒ Unit)], onPrepareShutdown: Option[(ReceiveFun, () ⇒ Unit)]): PartialFunction[ActorMessages.ComponentControlMessage, Unit] = {
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
            me.context.become(rec())
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
            me.context.become(rec())
            act()
          case None ⇒
            logWarning("Received a supported PrepareForShutdown but there is no handler in state Paused")
        }
      }
    case ActorMessages.ReportComponentState ⇒
      sender() ! ComponentState.Paused
  }

  def preparingForPauseHandlerScaffolding(onResume: Option[(ReceiveFun, () ⇒ Unit)], onPrepareShutdown: Option[(ReceiveFun, () ⇒ Unit)]): PartialFunction[ActorMessages.ComponentControlMessage, Unit] = {
    case ActorMessages.Pause ⇒
      if (!componentControl.supports(ActorMessages.Pause))
        logWarning("Pause is not supported.")
      else
        logDebug("Pause not possible in state PreparingForPause")
    case ActorMessages.Resume ⇒
      if (!componentControl.supports(ActorMessages.Resume))
        logWarning("Resume is not supported.")
      else {
        onResume match {
          case Some((rec, act)) ⇒
            me.context.become(rec())
            act()
          case None ⇒
            logWarning("Received a supported Resume but there is no handler in state PreparingForPause")
        }
      }
    case ActorMessages.Restart ⇒
      if (!componentControl.supports(ActorMessages.Restart))
        logWarning("Restart is not supported.")
      else
        logDebug("Restart not possible in state PreparingForPause")
    case ActorMessages.PrepareForShutdown ⇒
      if (!componentControl.supports(ActorMessages.PrepareForShutdown))
        logWarning("PrepareForShutdown is not supported.")
      else {
        onPrepareShutdown match {
          case Some((rec, act)) ⇒
            me.context.become(rec())
            act()
          case None ⇒
            logWarning("Received a supported PrepareForShutdown but there is no handler in state PreparingForPause")
        }
      }
    case ActorMessages.ReportComponentState ⇒
      sender() ! ComponentState.PreparingForPause
  }
  
  def errorHandlerScaffolding(onPrepareShutdown: Option[(ReceiveFun, () ⇒ Unit)], onRestart: Option[(ReceiveFun, () ⇒ Unit)])(error: almhirt.problem.ProblemCause): PartialFunction[ActorMessages.ComponentControlMessage, Unit] = {
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
            me.context.become(rec())
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
            me.context.become(rec())
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

  val startupHandler: PartialFunction[ActorMessages.ComponentControlMessage, Unit] = startupHandlerScaffolding(None)
  def startupHandler(onPrepareShutDownBecome: ReceiveFun, transitionAction: () ⇒ Unit = () ⇒ {}) = startupHandlerScaffolding(Some(onPrepareShutDownBecome, transitionAction))
  val startupTerminator: Receive = {
    case m: ActorMessages.ComponentControlMessage ⇒
      startupHandler(m)
  }
  def startupTerminator(onPrepareShutDownBecome: ⇒ Receive, transitionAction: () ⇒ Unit = () ⇒ {}): Receive = {
    case m: ActorMessages.ComponentControlMessage ⇒
      startupHandler(() ⇒ onPrepareShutDownBecome, transitionAction)(m)
  }

  def startup()(receive: Receive): Receive = receive.terminateStartup
  def startup(onPrepareShutDownBecome: ⇒ Receive)(receive: Receive): Receive = receive.terminateStartup(onPrepareShutDownBecome)
  def startup(onPrepareShutDownBecome: ⇒ Receive, transitionAction: () ⇒ Unit)(receive: Receive): Receive = receive.terminateStartup(onPrepareShutDownBecome, transitionAction)

  val waitingForStartSignalHandler: PartialFunction[ActorMessages.ComponentControlMessage, Unit] = waitingForStartSignalHandlerScaffolding(None)
  def waitingForStartSignalHandler(onPrepareShutDownBecome: ReceiveFun, transitionAction: () ⇒ Unit = () ⇒ {}) = waitingForStartSignalHandlerScaffolding(Some(onPrepareShutDownBecome, transitionAction))
  val waitingForStartSignalTerminator: Receive = {
    case m: ActorMessages.ComponentControlMessage ⇒
      waitingForStartSignalHandler(m)
  }
  def waitingForStartSignalTerminator(onPrepareShutDownBecome: ⇒ Receive, transitionAction: () ⇒ Unit = () ⇒ {}): Receive = {
    case m: ActorMessages.ComponentControlMessage ⇒
      waitingForStartSignalHandler(() ⇒ onPrepareShutDownBecome, transitionAction)(m)
  }

  def waitingForStartSignal()(receive: Receive): Receive = receive.terminateWaitingForStartSignal
  def waitingForStartSignal(onPrepareShutDownBecome: ⇒ Receive)(receive: Receive): Receive = receive.terminateWaitingForStartSignal(onPrepareShutDownBecome)
  def waitingForStartSignal(onPrepareShutDownBecome: ⇒ Receive, transitionAction: () ⇒ Unit)(receive: Receive): Receive = receive.terminateWaitingForStartSignal(onPrepareShutDownBecome, transitionAction)
  
  val runningHandler: PartialFunction[ActorMessages.ComponentControlMessage, Unit] = runningHandlerScaffolding(None, None)
  def runningHandlerWithPause(onPause: ReceiveFun): PartialFunction[ActorMessages.ComponentControlMessage, Unit] = runningHandlerScaffolding(Some(onPause), None)
  def runningHandlerWithPauseAndPrepareShutdown(onPause: ReceiveFun, onPrepareShutdown: ReceiveFun): PartialFunction[ActorMessages.ComponentControlMessage, Unit] = runningHandlerScaffolding(Some(onPause), Some(onPrepareShutdown, () ⇒ {}))
  def runningHandlerWithPauseAndPrepareShutdown(onPause: ReceiveFun, onPrepareShutdown: ReceiveFun, transitionAction: () ⇒ Unit): PartialFunction[ActorMessages.ComponentControlMessage, Unit] = runningHandlerScaffolding(Some(onPause), Some(onPrepareShutdown, transitionAction))
  def runningHandlerWithPrepareShutdown(onPrepareShutdown: ReceiveFun, transitionAction: () ⇒ Unit): PartialFunction[ActorMessages.ComponentControlMessage, Unit] = runningHandlerScaffolding(None, Some(onPrepareShutdown, transitionAction))
  val runningTerminator: Receive = {
    case m: ActorMessages.ComponentControlMessage ⇒ runningHandler(m)
  }
  def runningTerminatorWithPause(onPause: ⇒ Receive): Receive = {
    case m: ActorMessages.ComponentControlMessage ⇒ runningHandlerWithPause(() ⇒ onPause)(m)
  }
  def runningTerminatorWithPauseAndPrepareShutdown(onPause: ⇒ Receive, onPrepareShutdown: ⇒ Receive): Receive = {
    case m: ActorMessages.ComponentControlMessage ⇒ runningHandlerWithPauseAndPrepareShutdown(() ⇒ onPause, () ⇒ onPrepareShutdown)(m)
  }
  def runningTerminatorWithPauseAndPrepareShutdown(onPause: ⇒ Receive, onPrepareShutdown: ⇒ Receive, transitionAction: () ⇒ Unit): Receive = {
    case m: ActorMessages.ComponentControlMessage ⇒ runningHandlerWithPauseAndPrepareShutdown(() ⇒ onPause, () ⇒ onPrepareShutdown, transitionAction)(m)
  }
  def runningTerminatorWithPrepareShutdown(onPrepareShutdown: ⇒ Receive, transitionAction: () ⇒ Unit): Receive = {
    case m: ActorMessages.ComponentControlMessage ⇒ runningHandlerWithPrepareShutdown(() ⇒ onPrepareShutdown, transitionAction)(m)
  }

  def running()(receive: Receive): Receive = receive.terminateRunning
  def runningWithPause(onPause: ⇒ Receive)(receive: Receive): Receive = receive.terminateRunningWithPause(onPause)
  def runningWithPauseAndPrepareShutdown(onPause: ⇒ Receive, onPrepareShutdown: ⇒ Receive)(receive: Receive): Receive = receive.terminateRunningWithPauseAndPrepareShutdown(onPause, onPrepareShutdown)
  def runningWithPauseAndPrepareShutdown(onPause: ⇒ Receive, onPrepareShutdown: ⇒ Receive, transitionAction: () ⇒ Unit)(receive: Receive): Receive = receive.terminateRunningWithPauseAndPrepareShutdown(onPause, onPrepareShutdown, transitionAction)
  def runningWithPrepareShutdown(onPrepareShutdown: ⇒ Receive)(receive: Receive): Receive = receive.terminateRunningWithPrepareShutdown(onPrepareShutdown, () ⇒ {})
  def runningWithPrepareShutdown(onPrepareShutdown: ⇒ Receive, transitionAction: () ⇒ Unit)(receive: Receive): Receive = receive.terminateRunningWithPrepareShutdown(onPrepareShutdown, transitionAction)

  val preparingForPauseHandler: PartialFunction[ActorMessages.ComponentControlMessage, Unit] = preparingForPauseHandlerScaffolding(None, None)
  def preparingForPauseHandler(onResume: ⇒ Receive): PartialFunction[ActorMessages.ComponentControlMessage, Unit] = preparingForPauseHandlerScaffolding(Some(() ⇒ onResume, () ⇒ {}), None)
  def preparingForPauseHandler(onResume: ⇒ Receive, onResumeTransitionAction: () ⇒ Unit): PartialFunction[ActorMessages.ComponentControlMessage, Unit] = preparingForPauseHandlerScaffolding(Some(() ⇒ onResume, onResumeTransitionAction), None)
  def preparingForPauseHandlerWithPrepareShutdown1(onResume: ⇒ Receive, onResumeTransitionAction: () ⇒ Unit, onPrepareShutdown: ⇒ Receive, onPrepareShutdownTransitionAction: () ⇒ Unit): PartialFunction[ActorMessages.ComponentControlMessage, Unit] = preparingForPauseHandlerScaffolding(Some(() ⇒ onResume, onResumeTransitionAction), Some(() ⇒ onPrepareShutdown, onPrepareShutdownTransitionAction))
  def preparingForPauseHandlerWithPrepareShutdown2(onResume: ⇒ Receive, onPrepareShutdown: ⇒ Receive, onPrepareShutdownTransitionAction: () ⇒ Unit): PartialFunction[ActorMessages.ComponentControlMessage, Unit] = preparingForPauseHandlerScaffolding(Some(() ⇒ onResume, () ⇒ {}), Some(() ⇒ onPrepareShutdown, onPrepareShutdownTransitionAction))
  def preparingForPauseHandlerWithPrepareShutdown3(onResume: ⇒ Receive, onResumeTransitionAction: () ⇒ Unit, onPrepareShutdown: ⇒ Receive): PartialFunction[ActorMessages.ComponentControlMessage, Unit] = preparingForPauseHandlerScaffolding(Some(() ⇒ onResume, onResumeTransitionAction), Some(() ⇒ onPrepareShutdown, () ⇒ {}))
  def preparingForPauseHandlerWithPrepareShutdown4(onResume: ⇒ Receive, onPrepareShutdown: ⇒ Receive): PartialFunction[ActorMessages.ComponentControlMessage, Unit] = preparingForPauseHandlerScaffolding(Some(() ⇒ onResume, () ⇒ {}), Some(() ⇒ onPrepareShutdown, () ⇒ {}))

  val preparingForPauseTerminator: Receive = {
    case m: ActorMessages.ComponentControlMessage ⇒ preparingForPauseHandlerScaffolding(None, None)(m)
  }
  def preparingForPauseTerminator(onResume: ⇒ Receive): Receive = {
    case m: ActorMessages.ComponentControlMessage ⇒ preparingForPauseHandlerScaffolding(Some(() ⇒ onResume, () ⇒ {}), None)(m)
  }
  def preparingForPauseTerminator(onResume: ⇒ Receive, onResumeTransitionAction: () ⇒ Unit): Receive = {
    case m: ActorMessages.ComponentControlMessage ⇒ preparingForPauseHandlerScaffolding(Some(() ⇒ onResume, onResumeTransitionAction), None)(m)
  }
  def preparingForPauseTerminatorWithPrepareShutdown1(onResume: ⇒ Receive, onResumeTransitionAction: () ⇒ Unit, onPrepareShutdown: ⇒ Receive, onPrepareShutdownTransitionAction: () ⇒ Unit): Receive = {
    case m: ActorMessages.ComponentControlMessage ⇒ preparingForPauseHandlerScaffolding(Some(() ⇒ onResume, onResumeTransitionAction), Some(() ⇒ onPrepareShutdown, onPrepareShutdownTransitionAction))(m)
  }
  def preparingForPauseTerminatorWithPrepareShutdown2(onResume: ⇒ Receive, onPrepareShutdown: ⇒ Receive, onPrepareShutdownTransitionAction: () ⇒ Unit): Receive = {
    case m: ActorMessages.ComponentControlMessage ⇒ preparingForPauseHandlerScaffolding(Some(() ⇒ onResume, () ⇒ {}), Some(() ⇒ onPrepareShutdown, onPrepareShutdownTransitionAction))(m)
  }
  def preparingForPauseTerminatorWithPrepareShutdown3(onResume: ⇒ Receive, onResumeTransitionAction: () ⇒ Unit, onPrepareShutdown: ⇒ Receive): Receive = {
    case m: ActorMessages.ComponentControlMessage ⇒ preparingForPauseHandlerScaffolding(Some(() ⇒ onResume, onResumeTransitionAction), Some(() ⇒ onPrepareShutdown, () ⇒ {}))(m)
  }
  def preparingForPauseTerminatorWithPrepareShutdown4(onResume: ⇒ Receive, onPrepareShutdown: ⇒ Receive): Receive = {
    case m: ActorMessages.ComponentControlMessage ⇒ preparingForPauseHandlerScaffolding(Some(() ⇒ onResume, () ⇒ {}), Some(() ⇒ onPrepareShutdown, () ⇒ {}))(m)
  }

  def preparingForPause()(receive: Receive): Receive = receive.terminatePreparingForPause
  def preparingForPause(onResume: ⇒ Receive)(receive: Receive): Receive = receive.terminatePreparingForPause(onResume)
  def preparingForPause(onResume: ⇒ Receive, onResumeTransitionAction: () ⇒ Unit)(receive: Receive): Receive = receive.terminatePreparingForPause(onResume, onResumeTransitionAction)
  def preparingForPauseWithPrepareShutdown1(onResume: ⇒ Receive, onResumeTransitionAction: () ⇒ Unit, onPrepareShutdown: ⇒ Receive, onPrepareShutdownTransitionAction: () ⇒ Unit)(receive: Receive): Receive = receive.terminatePreparingForPauseWithPrepareShutdown1(onResume, onResumeTransitionAction, onPrepareShutdown, onPrepareShutdownTransitionAction)
  def preparingForPauseWithPrepareShutdown2(onResume: ⇒ Receive, onPrepareShutdown: ⇒ Receive, onPrepareShutdownTransitionAction: () ⇒ Unit)(receive: Receive): Receive = receive.terminatePreparingForPauseWithPrepareShutdown2(onResume, onPrepareShutdown, onPrepareShutdownTransitionAction)
  def preparingForPauseWithPrepareShutdown3(onResume: ⇒ Receive, onResumeTransitionAction: () ⇒ Unit, onPrepareShutdown: ⇒ Receive)(receive: Receive): Receive = receive.terminatePreparingForPauseWithPrepareShutdown3(onResume, onResumeTransitionAction, onPrepareShutdown)
  def preparingForPauseWithPrepareShutdown4(onResume: ⇒ Receive, onPrepareShutdown: ⇒ Receive)(receive: Receive): Receive = receive.terminatePreparingForPauseWithPrepareShutdown4(onResume, onPrepareShutdown)
  
  
  val pauseHandler: PartialFunction[ActorMessages.ComponentControlMessage, Unit] = pauseHandlerScaffolding(None, None)
  def pauseHandler(onResume: ⇒ Receive): PartialFunction[ActorMessages.ComponentControlMessage, Unit] = pauseHandlerScaffolding(Some(() ⇒ onResume, () ⇒ {}), None)
  def pauseHandler(onResume: ⇒ Receive, onResumeTransitionAction: () ⇒ Unit): PartialFunction[ActorMessages.ComponentControlMessage, Unit] = pauseHandlerScaffolding(Some(() ⇒ onResume, onResumeTransitionAction), None)
  def pauseHandlerWithPrepareShutdown1(onResume: ⇒ Receive, onResumeTransitionAction: () ⇒ Unit, onPrepareShutdown: ⇒ Receive, onPrepareShutdownTransitionAction: () ⇒ Unit): PartialFunction[ActorMessages.ComponentControlMessage, Unit] = pauseHandlerScaffolding(Some(() ⇒ onResume, onResumeTransitionAction), Some(() ⇒ onPrepareShutdown, onPrepareShutdownTransitionAction))
  def pauseHandlerWithPrepareShutdown2(onResume: ⇒ Receive, onPrepareShutdown: ⇒ Receive, onPrepareShutdownTransitionAction: () ⇒ Unit): PartialFunction[ActorMessages.ComponentControlMessage, Unit] = pauseHandlerScaffolding(Some(() ⇒ onResume, () ⇒ {}), Some(() ⇒ onPrepareShutdown, onPrepareShutdownTransitionAction))
  def pauseHandlerWithPrepareShutdown3(onResume: ⇒ Receive, onResumeTransitionAction: () ⇒ Unit, onPrepareShutdown: ⇒ Receive): PartialFunction[ActorMessages.ComponentControlMessage, Unit] = pauseHandlerScaffolding(Some(() ⇒ onResume, onResumeTransitionAction), Some(() ⇒ onPrepareShutdown, () ⇒ {}))
  def pauseHandlerWithPrepareShutdown4(onResume: ⇒ Receive, onPrepareShutdown: ⇒ Receive): PartialFunction[ActorMessages.ComponentControlMessage, Unit] = pauseHandlerScaffolding(Some(() ⇒ onResume, () ⇒ {}), Some(() ⇒ onPrepareShutdown, () ⇒ {}))

  val pauseTerminator: Receive = {
    case m: ActorMessages.ComponentControlMessage ⇒ pauseHandlerScaffolding(None, None)(m)
  }
  def pauseTerminator(onResume: ⇒ Receive): Receive = {
    case m: ActorMessages.ComponentControlMessage ⇒ pauseHandlerScaffolding(Some(() ⇒ onResume, () ⇒ {}), None)(m)
  }
  def pauseTerminator(onResume: ⇒ Receive, onResumeTransitionAction: () ⇒ Unit): Receive = {
    case m: ActorMessages.ComponentControlMessage ⇒ pauseHandlerScaffolding(Some(() ⇒ onResume, onResumeTransitionAction), None)(m)
  }
  def pauseTerminatorWithPrepareShutdown1(onResume: ⇒ Receive, onResumeTransitionAction: () ⇒ Unit, onPrepareShutdown: ⇒ Receive, onPrepareShutdownTransitionAction: () ⇒ Unit): Receive = {
    case m: ActorMessages.ComponentControlMessage ⇒ pauseHandlerScaffolding(Some(() ⇒ onResume, onResumeTransitionAction), Some(() ⇒ onPrepareShutdown, onPrepareShutdownTransitionAction))(m)
  }
  def pauseTerminatorWithPrepareShutdown2(onResume: ⇒ Receive, onPrepareShutdown: ⇒ Receive, onPrepareShutdownTransitionAction: () ⇒ Unit): Receive = {
    case m: ActorMessages.ComponentControlMessage ⇒ pauseHandlerScaffolding(Some(() ⇒ onResume, () ⇒ {}), Some(() ⇒ onPrepareShutdown, onPrepareShutdownTransitionAction))(m)
  }
  def pauseTerminatorWithPrepareShutdown3(onResume: ⇒ Receive, onResumeTransitionAction: () ⇒ Unit, onPrepareShutdown: ⇒ Receive): Receive = {
    case m: ActorMessages.ComponentControlMessage ⇒ pauseHandlerScaffolding(Some(() ⇒ onResume, onResumeTransitionAction), Some(() ⇒ onPrepareShutdown, () ⇒ {}))(m)
  }
  def pauseTerminatorWithPrepareShutdown4(onResume: ⇒ Receive, onPrepareShutdown: ⇒ Receive): Receive = {
    case m: ActorMessages.ComponentControlMessage ⇒ pauseHandlerScaffolding(Some(() ⇒ onResume, () ⇒ {}), Some(() ⇒ onPrepareShutdown, () ⇒ {}))(m)
  }

  def pause()(receive: Receive): Receive = receive.terminatePause
  def pause(onResume: ⇒ Receive)(receive: Receive): Receive = receive.terminatePause(onResume)
  def pause(onResume: ⇒ Receive, onResumeTransitionAction: () ⇒ Unit)(receive: Receive): Receive = receive.terminatePause(onResume, onResumeTransitionAction)
  def pauseWithPrepareShutdown1(onResume: ⇒ Receive, onResumeTransitionAction: () ⇒ Unit, onPrepareShutdown: ⇒ Receive, onPrepareShutdownTransitionAction: () ⇒ Unit)(receive: Receive): Receive = receive.terminatePauseWithPrepareShutdown1(onResume, onResumeTransitionAction, onPrepareShutdown, onPrepareShutdownTransitionAction)
  def pauseWithPrepareShutdown2(onResume: ⇒ Receive, onPrepareShutdown: ⇒ Receive, onPrepareShutdownTransitionAction: () ⇒ Unit)(receive: Receive): Receive = receive.terminatePauseWithPrepareShutdown2(onResume, onPrepareShutdown, onPrepareShutdownTransitionAction)
  def pauseWithPrepareShutdown3(onResume: ⇒ Receive, onResumeTransitionAction: () ⇒ Unit, onPrepareShutdown: ⇒ Receive)(receive: Receive): Receive = receive.terminatePauseWithPrepareShutdown3(onResume, onResumeTransitionAction, onPrepareShutdown)
  def pauseWithPrepareShutdown4(onResume: ⇒ Receive, onPrepareShutdown: ⇒ Receive)(receive: Receive): Receive = receive.terminatePauseWithPrepareShutdown4(onResume, onPrepareShutdown)

  def createErrorHandlerFactory: (almhirt.problem.ProblemCause) ⇒ PartialFunction[ActorMessages.ComponentControlMessage, Unit] = errorHandlerScaffolding(None, None)
  def createErrorHandlerFactory(onPrepareShutdown: ⇒ Receive): (almhirt.problem.ProblemCause) ⇒ PartialFunction[ActorMessages.ComponentControlMessage, Unit] = errorHandlerScaffolding(Some(() ⇒ onPrepareShutdown, () ⇒ {}), None)
  def createErrorHandlerFactory(onPrepareShutdown: ⇒ Receive, onPrepareShutdownTransitionAction: () ⇒ Unit): (almhirt.problem.ProblemCause) ⇒ PartialFunction[ActorMessages.ComponentControlMessage, Unit] = errorHandlerScaffolding(Some(() ⇒ onPrepareShutdown, onPrepareShutdownTransitionAction), None)
  def createErrorHandlerWithRestartFactory1(onPrepareShutdown: ⇒ Receive, onPrepareShutdownTransitionAction: () ⇒ Unit, onRestart: ⇒ Receive, onRestartTransitionAction: () ⇒ Unit): (almhirt.problem.ProblemCause) ⇒ PartialFunction[ActorMessages.ComponentControlMessage, Unit] = errorHandlerScaffolding(Some(() ⇒ onPrepareShutdown, onPrepareShutdownTransitionAction), Some(() ⇒ onRestart, onRestartTransitionAction))
  def createErrorHandlerWithRestartFactory2(onPrepareShutdown: ⇒ Receive, onRestart: ⇒ Receive, onRestartTransitionAction: () ⇒ Unit): (almhirt.problem.ProblemCause) ⇒ PartialFunction[ActorMessages.ComponentControlMessage, Unit] = errorHandlerScaffolding(Some(() ⇒ onPrepareShutdown, () ⇒ {}), Some(() ⇒ onRestart, onRestartTransitionAction))
  def createErrorHandlerWithRestartFactory3(onPrepareShutdown: ⇒ Receive, onPrepareShutdownTransitionAction: () ⇒ Unit, onRestart: ⇒ Receive): (almhirt.problem.ProblemCause) ⇒ PartialFunction[ActorMessages.ComponentControlMessage, Unit] = errorHandlerScaffolding(Some(() ⇒ onPrepareShutdown, onPrepareShutdownTransitionAction), Some(() ⇒ onRestart, () ⇒ {}))
  def createErrorHandlerWithRestartFactory4(onPrepareShutdown: ⇒ Receive, onRestart: ⇒ Receive): (almhirt.problem.ProblemCause) ⇒ PartialFunction[ActorMessages.ComponentControlMessage, Unit] = errorHandlerScaffolding(Some(() ⇒ onPrepareShutdown, () ⇒ {}), Some(() ⇒ onRestart, () ⇒ {}))

  def createErrorTerminatorFactory: (almhirt.problem.ProblemCause) ⇒ Receive = problem ⇒ {
    case m: ActorMessages.ComponentControlMessage ⇒ errorHandlerScaffolding(None, None)(problem)(m)
  }
  def createErrorTerminatorFactory(onPrepareShutdown: ⇒ Receive): (almhirt.problem.ProblemCause) ⇒ Receive = problem ⇒ {
    case m: ActorMessages.ComponentControlMessage ⇒ errorHandlerScaffolding(Some(() ⇒ onPrepareShutdown, () ⇒ {}), None)(problem)(m)
  }
  def createErrorTerminatorFactory(onPrepareShutdown: ⇒ Receive, onPrepareShutdownTransitionAction: () ⇒ Unit): (almhirt.problem.ProblemCause) ⇒ Receive = problem ⇒ {
    case m: ActorMessages.ComponentControlMessage ⇒ errorHandlerScaffolding(Some(() ⇒ onPrepareShutdown, onPrepareShutdownTransitionAction), None)(problem)(m)
  }
  def createErrorTerminatorWithRestartFactory1(onPrepareShutdown: ⇒ Receive, onPrepareShutdownTransitionAction: () ⇒ Unit, onRestart: ⇒ Receive, onRestartTransitionAction: () ⇒ Unit): (almhirt.problem.ProblemCause) ⇒ Receive = problem ⇒ {
    case m: ActorMessages.ComponentControlMessage ⇒ errorHandlerScaffolding(Some(() ⇒ onPrepareShutdown, onPrepareShutdownTransitionAction), Some(() ⇒ onRestart, onRestartTransitionAction))(problem)(m)
  }
  def createErrorTerminatorWithRestartFactory2(onPrepareShutdown: ⇒ Receive, onRestart: ⇒ Receive, onRestartTransitionAction: () ⇒ Unit): (almhirt.problem.ProblemCause) ⇒ Receive = problem ⇒ {
    case m: ActorMessages.ComponentControlMessage ⇒ errorHandlerScaffolding(Some(() ⇒ onPrepareShutdown, () ⇒ {}), Some(() ⇒ onRestart, onRestartTransitionAction))(problem)(m)
  }
  def createErrorTerminatorWithRestartFactory3(onPrepareShutdown: ⇒ Receive, onPrepareShutdownTransitionAction: () ⇒ Unit, onRestart: ⇒ Receive): (almhirt.problem.ProblemCause) ⇒ Receive = problem ⇒ {
    case m: ActorMessages.ComponentControlMessage ⇒ errorHandlerScaffolding(Some(() ⇒ onPrepareShutdown, onPrepareShutdownTransitionAction), Some(() ⇒ onRestart, () ⇒ {}))(problem)(m)
  }
  def createErrorTerminatorWithRestartFactory4(onPrepareShutdown: ⇒ Receive, onRestart: ⇒ Receive): (almhirt.problem.ProblemCause) ⇒ Receive = problem ⇒ {
    case m: ActorMessages.ComponentControlMessage ⇒ errorHandlerScaffolding(Some(() ⇒ onPrepareShutdown, () ⇒ {}), Some(() ⇒ onRestart, () ⇒ {}))(problem)(m)
  }

  def error(error: almhirt.problem.ProblemCause)(receive: Receive): Receive = receive.createTerminateErrorFactory(error)
  def error(onPrepareShutdown:  ⇒ Receive, error: almhirt.problem.ProblemCause)(receive: Receive): Receive = receive.createTerminateErrorFactory(onPrepareShutdown)(error)
  def error(onPrepareShutdown:  ⇒ Receive, onPrepareShutdownTransitionAction: () ⇒ Unit, error: almhirt.problem.ProblemCause)(receive: Receive): Receive = receive.createTerminateErrorFactory(onPrepareShutdown, onPrepareShutdownTransitionAction)(error)
  def errorWithRestart1(onPrepareShutdown:  ⇒ Receive, onPrepareShutdownTransitionAction: () ⇒ Unit, onRestart:  ⇒ Receive, onRestartTransitionAction: () ⇒ Unit, error: almhirt.problem.ProblemCause)(receive: Receive): Receive = receive.createTerminateErrorWithRestartFactory1(onPrepareShutdown, onPrepareShutdownTransitionAction, onRestart, onRestartTransitionAction)(error)
  def errorWithRestart2(onPrepareShutdown:  ⇒ Receive, onRestart:  ⇒ Receive, onRestartTransitionAction: () ⇒ Unit, error: almhirt.problem.ProblemCause)(receive: Receive): Receive = receive.createTerminateErrorWithRestartFactory2(onPrepareShutdown, onRestart, onRestartTransitionAction)(error)
  def errorWithRestart3(onPrepareShutdown:  ⇒ Receive, onPrepareShutdownTransitionAction: () ⇒ Unit, onRestart:  ⇒ Receive, error: almhirt.problem.ProblemCause)(receive: Receive): Receive = receive.createTerminateErrorWithRestartFactory3(onPrepareShutdown, onPrepareShutdownTransitionAction, onRestart)(error)
  def errorWithRestart4(onPrepareShutdown:  ⇒ Receive, onRestart:  ⇒ Receive, error: almhirt.problem.ProblemCause)(receive: Receive): Receive = receive.createTerminateErrorWithRestartFactory4(onPrepareShutdown, onRestart)(error)

  val preparingForShutdownTerminator: Receive = {
    case m: ActorMessages.ComponentControlMessage ⇒ preparingForShutdownHandler(m)
  }
  def preparingForShutdown(receive: Receive): Receive = receive.terminatePreparingForShutdown

  val readyForShutdownTerminator: Receive = {
    case m: ActorMessages.ComponentControlMessage ⇒ readyForShutdownHandler(m)
  }
  def readyForShutdown(receive: Receive): Receive = receive.terminateReadyForShutdown

}
