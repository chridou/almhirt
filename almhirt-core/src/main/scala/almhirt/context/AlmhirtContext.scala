package almhirt.context

import scala.concurrent.ExecutionContext
import scala.concurrent.duration._
import akka.actor._
import akka.pattern._
import scalaz.Validation.FlatMap._
import almhirt.common._
import almhirt.almfuture.all._
import almhirt.tooling.Reporter
import almhirt.streaming.AlmhirtStreams
import almhirt.akkax.ActorMessages
import almhirt.akkax.ComponentId
import almhirt.herder.HerderMessages
import com.typesafe.config._

trait AlmhirtContext extends CanCreateUuidsAndDateTimes with AlmhirtStreams with HasExecutionContexts {
  def config: Config
  def localActorPaths: ContextActorPaths
  def tellHerder(what: almhirt.herder.HerderMessages.HerderNotificicationMessage): Unit
  def publishNonStreamEvent(event: Event, maxDur: FiniteDuration = 3.seconds): AlmFuture[Event]
  def fireNonStreamEvent(event: Event): Unit

  def localNodeName: almhirt.akkax.NodeName

  def createReporter(forComponent: ComponentId): Reporter

  def withFuturesExecutor(executor: ExecutionContext): AlmhirtContext = {
    new AlmhirtContext {
      val config = AlmhirtContext.this.config
      val futuresContext = executor
      val crunchersContext = AlmhirtContext.this.crunchersContext
      val blockersContext = AlmhirtContext.this.blockersContext
      def getUuid() = AlmhirtContext.this.getUuid
      def getUniqueString() = AlmhirtContext.this.getUniqueString
      def getDateTime() = AlmhirtContext.this.getDateTime
      def getUtcTimestamp() = AlmhirtContext.this.getUtcTimestamp
      val eventBroker = AlmhirtContext.this.eventBroker
      val eventStream = AlmhirtContext.this.eventStream
      val commandBroker = AlmhirtContext.this.commandBroker
      val commandStream = AlmhirtContext.this.commandStream
      val localActorPaths = AlmhirtContext.this.localActorPaths
      def tellHerder(what: almhirt.herder.HerderMessages.HerderNotificicationMessage) {
        AlmhirtContext.this.tellHerder(what)
      }
      def createReporter(forComponent: ComponentId): Reporter = AlmhirtContext.this.createReporter(forComponent)
      def publishNonStreamEvent(event: Event, maxDur: FiniteDuration = 3.seconds): AlmFuture[Event] = AlmhirtContext.this.publishNonStreamEvent(event, maxDur)
      def fireNonStreamEvent(event: Event): Unit = AlmhirtContext.this.fireNonStreamEvent(event)
      val localNodeName = AlmhirtContext.this.localNodeName
    }
  }

  def withBlockingExecutor(executor: ExecutionContext): AlmhirtContext = {
    new AlmhirtContext {
      val config = AlmhirtContext.this.config
      val futuresContext = AlmhirtContext.this.futuresContext
      val crunchersContext = AlmhirtContext.this.crunchersContext
      val blockersContext = executor
      def getUuid() = AlmhirtContext.this.getUuid
      def getUniqueString() = AlmhirtContext.this.getUniqueString
      def getDateTime() = AlmhirtContext.this.getDateTime
      def getUtcTimestamp() = AlmhirtContext.this.getUtcTimestamp
      val eventBroker = AlmhirtContext.this.eventBroker
      val eventStream = AlmhirtContext.this.eventStream
      val commandBroker = AlmhirtContext.this.commandBroker
      val commandStream = AlmhirtContext.this.commandStream
      val localActorPaths = AlmhirtContext.this.localActorPaths
      def tellHerder(what: almhirt.herder.HerderMessages.HerderNotificicationMessage) {
        AlmhirtContext.this.tellHerder(what)
      }
      def createReporter(forComponent: ComponentId): Reporter = AlmhirtContext.this.createReporter(forComponent)
      def publishNonStreamEvent(event: Event, maxDur: FiniteDuration = 3.seconds): AlmFuture[Event] = AlmhirtContext.this.publishNonStreamEvent(event, maxDur)
      def fireNonStreamEvent(event: Event): Unit = AlmhirtContext.this.fireNonStreamEvent(event)
      val localNodeName = AlmhirtContext.this.localNodeName
    }
  }

  def withCrunchersExecutor(executor: ExecutionContext): AlmhirtContext = {
    new AlmhirtContext {
      val config = AlmhirtContext.this.config
      val futuresContext = AlmhirtContext.this.futuresContext
      val crunchersContext = executor
      val blockersContext = AlmhirtContext.this.blockersContext
      def getUuid() = AlmhirtContext.this.getUuid
      def getUniqueString() = AlmhirtContext.this.getUniqueString
      def getDateTime() = AlmhirtContext.this.getDateTime
      def getUtcTimestamp() = AlmhirtContext.this.getUtcTimestamp
      val eventBroker = AlmhirtContext.this.eventBroker
      val eventStream = AlmhirtContext.this.eventStream
      val commandBroker = AlmhirtContext.this.commandBroker
      val commandStream = AlmhirtContext.this.commandStream
      val localActorPaths = AlmhirtContext.this.localActorPaths
      def tellHerder(what: almhirt.herder.HerderMessages.HerderNotificicationMessage) {
        AlmhirtContext.this.tellHerder(what)
      }
      def createReporter(forComponent: ComponentId): Reporter = AlmhirtContext.this.createReporter(forComponent)
      def publishNonStreamEvent(event: Event, maxDur: FiniteDuration = 3.seconds): AlmFuture[Event] = AlmhirtContext.this.publishNonStreamEvent(event, maxDur)
      def fireNonStreamEvent(event: Event): Unit = AlmhirtContext.this.fireNonStreamEvent(event)
      val localNodeName = AlmhirtContext.this.localNodeName
    }
  }

}

trait ContextActorPaths {
  def root: RootActorPath
  def almhirt: ActorPath
  def herder: ActorPath
  def components: ActorPath
  def eventLogs: ActorPath
  def views: ActorPath
  def misc: ActorPath
  def apps: ActorPath
  def resources: ActorPath
}

object ContextActorPaths {
  def local(system: ActorSystem): ContextActorPaths = {
    new ContextActorPaths {
      val root = new RootActorPath(Address("akka", system.name))
      val almhirt = ContextActorPaths.almhirt(root)
      val herder = ContextActorPaths.herder(root)
      val components = ContextActorPaths.components(root)
      val eventLogs = ContextActorPaths.eventLogs(root)
      val views = ContextActorPaths.views(root)
      val misc = ContextActorPaths.misc(root)
      val apps = ContextActorPaths.apps(root)
      val resources = ContextActorPaths.resources(root)
    }
  }

  def almhirt(root: RootActorPath): ActorPath =
    root / "user" / "almhirt"

  def herder(root: RootActorPath): ActorPath =
    _root_.almhirt.herder.Herder.path(root)

  def components(root: RootActorPath): ActorPath =
    almhirt(root) / "components"

  def eventLogs(root: RootActorPath): ActorPath =
    components(root) / "event-logs"

  def views(root: RootActorPath): ActorPath =
    components(root) / "views"

  def misc(root: RootActorPath): ActorPath =
    components(root) / "misc"

  def apps(root: RootActorPath): ActorPath =
    components(root) / "apps"

  def resources(root: RootActorPath): ActorPath =
    components(root) / _root_.almhirt.components.ResourcesService.actorname
}

object AlmhirtContextMessages {
  private[almhirt] case object Start
  private[almhirt] case class StreamsCreated(streams: AlmhirtStreams with Stoppable)
  private[almhirt] case class StreamsNotCreated(problem: Problem)
  private[almhirt] case class HerderCreated(ctx: AlmhirtContext with Stoppable)
  private[almhirt] case class ContextCreated(ctx: AlmhirtContext with Stoppable)

  private[almhirt] sealed trait FinishedResponse
  private[almhirt] case class FinishedInitialization(ctx: AlmhirtContext with Stoppable) extends FinishedResponse
  private[almhirt] case class FailedInitialization(problem: Problem) extends FinishedResponse

}

object AlmhirtContext {
  type ComponentFactory = AlmhirtContext ⇒ AlmFuture[Props]

  def apply(system: ActorSystem, actorName: Option[String], componentFactories: ComponentFactories, specificCcuad: Option[CanCreateUuidsAndDateTimes] = None): AlmFuture[AlmhirtContext with Stoppable] = {
    import almhirt.configuration._

    val propsV =
      for {
        configSection ← system.settings.config.v[com.typesafe.config.Config]("almhirt.context")
        useFuturesCtx ← configSection.v[Boolean]("use-dedicated-futures-dispatcher")
        useBlockersCtx ← configSection.v[Boolean]("use-dedicated-blockers-dispatcher")
        useCrunchersCtx ← configSection.v[Boolean]("use-dedicated-cruncher-dispatcher")
        dedicatedAppsFuturesExecutorLookupName ← configSection.v[Boolean]("use-dedicated-apps-futures-executor").map(useDedAppfFutExeceutor ⇒
          if (useDedAppfFutExeceutor)
            Some("almhirt.context.dispatchers.apps-futures-dispatcher")
          else
            None)
      } yield {
        val futuresExecutor =
          if (useFuturesCtx)
            system.dispatchers.lookup("almhirt.context.dispatchers.futures-dispatcher")
          else {
            system.log.warning("""Using default dispatcher as futures executor""")
            system.dispatcher
          }
        val crunchersExecutor =
          if (useCrunchersCtx)
            system.dispatchers.lookup("almhirt.context.dispatchers.cruncher-dispatcher")
          else {
            system.log.warning("""Using default dispatcher as cruncher executor""")
            system.dispatcher
          }
        val blockersExecutor =
          if (useBlockersCtx)
            system.dispatchers.lookup("almhirt.context.dispatchers.blockers-dispatcher")
          else {
            system.log.warning("""Using default dispatcher as blockers executor""")
            system.dispatcher
          }

        import almhirt.akkax._
        Props(new AlmActor with AlmActorLogging with ActorLogging {
          implicit val execCtx = futuresExecutor
          var theReceiver: ActorRef = null
          var tellTheHerder: almhirt.herder.HerderMessages.HerderNotificicationMessage ⇒ Unit = x ⇒ ()
          var publishANonStreamEvent: (Event, FiniteDuration) ⇒ AlmFuture[Event] = (x, y) ⇒ AlmFuture.successful(x)
          var fireANonStreamEvent: Event ⇒ Unit = x ⇒ ()

          var _ctx: AlmhirtContext = null
          override def almhirtContext = _ctx

          def receive: Receive = {
            case AlmhirtContextMessages.Start ⇒
              theReceiver = sender()
              log.info("Create streams")
              AlmhirtStreams.createInternal(this.context, system.settings.config).onComplete(
                problem ⇒ self ! AlmhirtContextMessages.StreamsNotCreated(problem),
                streams ⇒ self ! AlmhirtContextMessages.StreamsCreated(streams))

            case AlmhirtContextMessages.StreamsCreated(streams) ⇒
              log.info("Created streams. Next: Create context")
              val ccuad = specificCcuad getOrElse CanCreateUuidsAndDateTimes()
              val theLocalActorPaths = ContextActorPaths.local(system)
              val ctx = new AlmhirtContext with Stoppable {
                val config = system.settings.config
                val futuresContext = futuresExecutor
                val crunchersContext = crunchersExecutor
                val blockersContext = blockersExecutor
                def getUuid() = ccuad.getUuid
                def getUniqueString() = ccuad.getUniqueString
                def getDateTime() = ccuad.getDateTime
                def getUtcTimestamp() = ccuad.getUtcTimestamp
                val eventBroker = streams.eventBroker
                val eventStream = streams.eventStream
                val commandBroker = streams.commandBroker
                val commandStream = streams.commandStream
                val localActorPaths = theLocalActorPaths
                def tellHerder(what: almhirt.herder.HerderMessages.HerderNotificicationMessage) { tellTheHerder(what) }
                def createReporter(forComponent: ComponentId): Reporter = new TellHerderReporter(this, forComponent)
                def publishNonStreamEvent(event: Event, maxDur: FiniteDuration = 3.seconds): AlmFuture[Event] = publishANonStreamEvent(event, maxDur)
                def fireNonStreamEvent(event: Event): Unit = fireANonStreamEvent(event)
                val localNodeName = almhirt.akkax.NodeName(context.system.name)

                def stop() {
                  log.info("Stopping.")
                  context.actorSelection(theLocalActorPaths.herder) ! HerderMessages.OnSystemShutdown
                  //streams.stop()
                  context.system.scheduler.scheduleOnce(5.seconds)(context.stop(self))
                }
              }
              _ctx = ctx
              self ! AlmhirtContextMessages.ContextCreated(ctx)

            case AlmhirtContextMessages.ContextCreated(ctx) ⇒
              log.info("Context created. Next: Configure herder")
              (for {
                herderProps ← almhirt.herder.Herder.props()(ctx)
              } yield herderProps).fold(
                prob ⇒ theReceiver ! AlmhirtContextMessages.FailedInitialization(prob),
                herderProps ⇒ {
                  val herder = context.actorOf(herderProps, almhirt.herder.Herder.actorname)
                  this.tellTheHerder = x ⇒ herder ! x
                  self ! AlmhirtContextMessages.HerderCreated(ctx)
                })
              theReceiver ! AlmhirtContextMessages.FinishedInitialization(ctx)

            case AlmhirtContextMessages.HerderCreated(ctx) ⇒
              logInfo("Herder created. Core system configured. Will now go on with the components...")
              theReceiver ! AlmhirtContextMessages.FinishedInitialization(ctx)
              val components = context.actorOf(componentactors.componentsProps(dedicatedAppsFuturesExecutorLookupName)(ctx), "components")
              components ! componentactors.UnfoldFromFactories(componentFactories)

            case componentactors.EventPublisherHubCreated(publisher) ⇒
              this.fireANonStreamEvent = event ⇒ publisher ! almhirt.components.EventPublisher.FireEvent(event)
              this.publishANonStreamEvent = (event, maxDur) ⇒
                (publisher ? almhirt.components.EventPublisher.PublishEvent(event))(maxDur).mapCastTo[almhirt.components.EventPublisher.PublishEventRsp].collectV {
                  case almhirt.components.EventPublisher.EventPublished(event)       ⇒ scalaz.Success(event)
                  case almhirt.components.EventPublisher.EventNotPublished(_, cause) ⇒ scalaz.Failure(cause)
                }

              sender() ! componentactors.EventPublisherHubRegistered

            case AlmhirtContextMessages.StreamsNotCreated(prob) ⇒
              logError(s"Could not create streams:\n$prob")
              theReceiver ! AlmhirtContextMessages.FailedInitialization(prob)
          }
        })
      }

    propsV.flatMap { almhirtProps ⇒
      import scala.concurrent.duration._
      system.settings.config.v[FiniteDuration]("almhirt.context.max-init-duration").map((almhirtProps, _))
    }.fold(
      fail ⇒ AlmFuture.failed(fail),
      Function.tupled((almhirtProps, maxInitDur) ⇒ {
        val theAlmhirt = system.actorOf(almhirtProps, this.actorname)

        import almhirt.almvalidation.kit._
        import almhirt.almfuture.all._
        import akka.pattern._
        implicit val execCtx = system.dispatchers.defaultGlobalDispatcher
        (theAlmhirt ? AlmhirtContextMessages.Start)(maxInitDur).mapCastTo[AlmhirtContextMessages.FinishedResponse].mapV {
          case AlmhirtContextMessages.FinishedInitialization(ctx) ⇒ scalaz.Success(ctx)
          case AlmhirtContextMessages.FailedInitialization(prob)  ⇒ scalaz.Failure(prob)
        }
      }))
  }

  val actorname = "almhirt"
  val componentsActorname = "components"

  object TestContext {
    def noComponentsDefaultGlobalDispatcher(contextName: String, maxDur: scala.concurrent.duration.FiniteDuration)(implicit system: ActorSystem): AlmFuture[AlmhirtContext with Stoppable] =
      noComponentsDefaultGlobalDispatcher(contextName, CanCreateUuidsAndDateTimes(), maxDur)

    def noComponentsDefaultGlobalDispatcher(contextName: String, ccuad: CanCreateUuidsAndDateTimes, maxDur: scala.concurrent.duration.FiniteDuration)(implicit system: ActorSystem): AlmFuture[AlmhirtContext with Stoppable] = {
      val futuresExecutor = system.dispatchers.defaultGlobalDispatcher
      val crunchersExecutor = system.dispatchers.defaultGlobalDispatcher
      val blockersExecutor = system.dispatchers.defaultGlobalDispatcher

      val almhirtProps = Props(new Actor with ActorLogging {

        override val supervisorStrategy = OneForOneStrategy(maxNrOfRetries = 0) {
          case scala.util.control.NonFatal(e) ⇒
            log.error(e, "Something failed in a child. Escalating")
            SupervisorStrategy.Escalate
        }

        implicit val execCtx = futuresExecutor
        var theReceiver: ActorRef = null
        def receive: Receive = {
          case AlmhirtContextMessages.Start ⇒
            theReceiver = sender()
            log.debug("Create streams")
            AlmhirtStreams.createInternal(this.context, system.settings.config).onComplete(
              problem ⇒ self ! AlmhirtContextMessages.StreamsNotCreated(problem),
              streams ⇒ self ! AlmhirtContextMessages.StreamsCreated(streams))

          case AlmhirtContextMessages.StreamsCreated(streams) ⇒
            log.debug("Created streams. Next: Create context")
            val ctx = new AlmhirtContext with Stoppable {
              val config = system.settings.config
              val futuresContext = futuresExecutor
              val crunchersContext = crunchersExecutor
              val blockersContext = blockersExecutor
              def getUuid() = ccuad.getUuid
              def getUniqueString() = ccuad.getUniqueString
              def getDateTime() = ccuad.getDateTime
              def getUtcTimestamp() = ccuad.getUtcTimestamp
              val eventBroker = streams.eventBroker
              val eventStream = streams.eventStream
              val commandBroker = streams.commandBroker
              val commandStream = streams.commandStream
              val localActorPaths = null
              def tellHerder(what: almhirt.herder.HerderMessages.HerderNotificicationMessage) {}
              def createReporter(forComponent: ComponentId): Reporter = Reporter.DevNull
              def publishNonStreamEvent(event: Event, maxDur: FiniteDuration = 3.seconds): AlmFuture[Event] = AlmFuture.successful(event)
              def fireNonStreamEvent(event: Event): Unit = {}
              val localNodeName = almhirt.akkax.NodeName(context.system.name)
              def stop() {
                log.debug("Stopping.")
                //streams.stop()
                context.stop(self)
              }
            }
            self ! AlmhirtContextMessages.ContextCreated(ctx)
            theReceiver ! AlmhirtContextMessages.FinishedInitialization(ctx)
        }
      })

      val theAlmhirt = system.actorOf(almhirtProps, contextName)

      import almhirt.almvalidation.kit._
      import almhirt.almfuture.all._
      import akka.pattern._
      implicit val execCtx = system.dispatchers.defaultGlobalDispatcher
      (theAlmhirt ? AlmhirtContextMessages.Start)(maxDur).mapCastTo[AlmhirtContextMessages.FinishedResponse].mapV {
        case AlmhirtContextMessages.FinishedInitialization(ctx) ⇒ scalaz.Success(ctx)
        case AlmhirtContextMessages.FailedInitialization(prob)  ⇒ scalaz.Failure(prob)
      }
    }
  }
}

private[context] class TellHerderReporter(almhirtContext: AlmhirtContext, componentId: ComponentId) extends Reporter {
  import almhirt.herder.HerderMessages
  def report(message: ⇒ String, importance: Importance): Unit = {
    almhirtContext.tellHerder(HerderMessages.InformationMessages.Information(componentId, message, importance, almhirtContext.getUtcTimestamp))
  }

  def reportDebug(message: ⇒ String): Unit = {
    reportNotWorthMentioning(message)
  }

  def reportError(message: String, cause: almhirt.problem.ProblemCause): Unit = {
    reportMajorFailure(cause)
    reportVeryImportant(message)
  }

  def reportFailure(cause: almhirt.problem.ProblemCause, severity: almhirt.problem.Severity): Unit = {
    almhirtContext.tellHerder(HerderMessages.FailureMessages.FailureOccured(componentId, cause, severity, almhirtContext.getUtcTimestamp))
  }

  def reportInfo(message: ⇒ String): Unit = {
    reportMentionable(message)
  }

  def reportMissedEvent(event: Event, severity: almhirt.problem.Severity, cause: almhirt.problem.ProblemCause): Unit = {
    almhirtContext.tellHerder(HerderMessages.EventMessages.MissedEvent(componentId, event, severity, cause, almhirtContext.getUtcTimestamp))
  }

  def reportRejectedCommand(command: almhirt.tracking.CommandRepresentation, severity: almhirt.problem.Severity, cause: almhirt.problem.ProblemCause): Unit = {
    almhirtContext.tellHerder(HerderMessages.CommandMessages.RejectedCommand(componentId, command, severity, cause, almhirtContext.getUtcTimestamp))
  }

  def reportWarning(message: ⇒ String): Unit = {
    reportImportant(message)
  }
}