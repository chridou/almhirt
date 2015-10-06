package almhirt.context

import scala.concurrent.duration._
import scala.concurrent.ExecutionContext
import akka.actor._
import almhirt.common._
import almhirt.akkax._
import almhirt.akkax.reporting._
import almhirt.akkax.reporting.Implicits._
import almhirt.almvalidation.kit._
import almhirt.tracking.CorrelationId
import almhirt.components.ResourcesService

object SupervisorPaths {
  val eventLogs = ""
  val views = ""
  val misc = ""
  val apps = ""
}

private[almhirt] object componentactors {
  import almhirt.akkax.{ ActorMessages, ComponentFactory }

  final case class EventPublisherHubCreated(actor: ActorRef)
  case object EventPublisherHubRegistered

  def componentsProps(
    dedicatedAppsFuturesExecutor: Option[String])(implicit ctx: AlmhirtContext): Props =
    Props(new ComponentsSupervisor(dedicatedAppsFuturesExecutor))

  def viewsProps(implicit ctx: AlmhirtContext): Props =
    Props(new ViewsSupervisor)

  def eventLogsProps(implicit ctx: AlmhirtContext): Props =
    Props(new EventLogsSupervisor())

  def miscProps(implicit ctx: AlmhirtContext): Props =
    Props(new MiscSupervisor)

  def appsProps(implicit ctx: AlmhirtContext): Props =
    Props(new AppsSupervisor)

  class ViewsSupervisor()(implicit override val almhirtContext: AlmhirtContext) extends SimpleUnfolder
  class EventLogsSupervisor()(implicit override val almhirtContext: AlmhirtContext) extends SimpleUnfolder
  class MiscSupervisor()(implicit override val almhirtContext: AlmhirtContext) extends SimpleUnfolder
  class AppsSupervisor()(implicit override val almhirtContext: AlmhirtContext) extends SimpleUnfolder

  final case class UnfoldFromFactories(factories: ComponentFactories)

  final case class UnfoldFactory(factory: ComponentFactoryBuilderEntry)

  /**
   * This is all a bit hacky since I don't know yet, how I want this to behave like...
   */
  class ComponentsSupervisor(
      dedicatedAppsFuturesExecutor: Option[String])(implicit override val almhirtContext: AlmhirtContext) extends AlmActor with AlmActorLogging with ControllableActor with StatusReportingActor {
    import akka.actor.SupervisorStrategy._
    override val supervisorStrategy = OneForOneStrategy(maxNrOfRetries = 10, withinTimeRange = 1.minute) {
      case exn ⇒
        logError("Stopping a child", exn)
        reportCriticalFailure(exn)
        Stop
    }

    implicit val executor = almhirtContext.futuresContext

    override val componentControl = LocalComponentControl(self, ActorMessages.ComponentControlActions.none, Some(logWarning))

    override val statusReportsCollector = Some(StatusReportsCollector(this.context))

    val eventLogs = context.actorOf(eventLogsProps(almhirtContext), "event-logs")
    logInfo(s"Created event logs supervisor as ${eventLogs.path.name}.")
    val views = context.actorOf(viewsProps(almhirtContext), "views")
    logInfo(s"Created views supervisor as ${views.path.name}.")
    val misc = context.actorOf(miscProps(almhirtContext), "misc")
    logInfo(s"Created misc supervisor as ${misc.path.name}.")

    val appsFuturesExecutor: ExecutionContext =
      dedicatedAppsFuturesExecutor match {
        case Some(name) ⇒
          logInfo("Using dedicated futures executor for apps")
          context.system.dispatchers.lookup(name)
        case None ⇒
          logWarning("Using default futures executor for apps")
          almhirtContext.futuresContext
      }

    val apps = context.actorOf(appsProps(almhirtContext.withFuturesExecutor(appsFuturesExecutor)), "apps")

    logInfo(s"Created apps supervisor as ${apps.path.name}.")

    var factories: ComponentFactories = null

    override def receive = receiveStart(false)

    def receiveStart(canAdvance: Boolean): Receive = startup() {
      reportsStatusF(onReportRequested = createStatusReport) {
        case UnfoldFromFactories(theFactories) ⇒
          factories = theFactories
          logInfo("Received unfold components.")

          val componentFactoryF = factories.createResourceServiceProps match {
            case None ⇒
              AlmFuture.successful(ComponentFactory(ResourcesService.emptyProps, ResourcesService.actorname))
            case Some(f) ⇒
              f(almhirtContext).map(props ⇒ ComponentFactory(props, ResourcesService.actorname))
          }

          componentFactoryF.onComplete(
            fail ⇒ self ! ResourcesService.InitializeResourcesFailed(fail),
            factory ⇒ self ! ActorMessages.CreateChildActor(factory, true, None))

        case ResourcesService.ResourcesInitialized ⇒
          if (canAdvance) {
            logInfo("Resources initialized.")
            context.become(receiveBuildHerderApp)
            self ! "herder"
          } else {
            context.become(receiveStart(true))
          }

        case ResourcesService.InitializeResourcesFailed(failure) ⇒
          logError(s"Failed to initialize resources\n$failure.")
          reportCriticalFailure(failure)
          sys.error(s"Failed to initialize resources\n$failure.")

        case ActorMessages.CreateChildActor(componentFactory, returnActorRef, correlationId) ⇒
          context.childFrom(componentFactory).fold(
            problem ⇒ {
              logError(s""" |Failed to create "${componentFactory.name.getOrElse("<<<no name>>>")}":
                    |$problem""".stripMargin)
              sender() ! ActorMessages.CreateChildActorFailed(problem, correlationId)
            },
            actorRef ⇒ {
              logInfo(s"Created ${actorRef.path} @ ${actorRef.path} ")
              if (returnActorRef)
                sender() ! ActorMessages.ChildActorCreated(actorRef, correlationId)
            })

        case ActorMessages.CreateChildActorFailed(problem, _) ⇒
          logError(s"A child actor was not created:\n$problem")
          reportCriticalFailure(problem)
          sys.error(s"A child actor was not created:\n$problem")

        case ActorMessages.ChildActorCreated(actor, _) ⇒
          if (canAdvance) {
            logInfo(s"Created ${actor.path}.")
            context.become(receiveBuildHerderApp)
            self ! "herder"
          } else {
            context.become(receiveStart(true))
          }
      }
    }

    def receiveBuildHerderApp: Receive = startup() {
      reportsStatusF(onReportRequested = createStatusReport) {
        case "herder" ⇒
          factories.buildHerderService match {
            case Some(ComponentFactoryBuilderEntry(factoryBuilder, severity)) ⇒
              logInfo("Create herder app")
              factoryBuilder(almhirtContext).onComplete(
                problem ⇒ {
                  logError(s"Could not create herder app factory :\n$problem")
                  reportFailure(problem, severity)
                  self ! ActorMessages.HerderServiceAppFailedToStart(problem)
                },
                factory ⇒ apps ! ActorMessages.CreateChildActor(factory, true, None))(almhirtContext.futuresContext)
            case None ⇒
              logInfo("No herder app.")
              context.become(receiveBuildEventPublisher(CorrelationId()))
              self ! "make_event_publisher_hub"
          }

        case ActorMessages.HerderServiceAppFailedToStart(problem) ⇒
          sys.error(problem.toString())

        case ActorMessages.HerderServiceAppStarted ⇒
          logInfo("Herder app started.")
          context.become(receiveBuildEventPublisher(CorrelationId()))
          self ! "make_event_publisher_hub"

      }
    }

    def receiveBuildEventPublisher(cid: CorrelationId): Receive = running() {
      reportsStatusF(onReportRequested = createStatusReport) {
        case "make_event_publisher_hub" ⇒
          almhirt.components.EventPublisherHub.componentFactory(Nil).fold(
            fail ⇒ {},
            factory ⇒ {
              misc ! ActorMessages.CreateChildActor(factory, true, Some(cid))
            })

        case ActorMessages.CreateChildActorFailed(problem, _) ⇒
          logError(s"A child actor was not created:\n$problem")
          reportCriticalFailure(problem)

        case ActorMessages.ChildActorCreated(created, Some(cid)) ⇒
          logInfo(s"Created Event-Publisher-Hub ${created.path.name}.")
          context.parent ! EventPublisherHubCreated(created)

        case EventPublisherHubRegistered ⇒
          logInfo("Created Event-Publisher-Hub was registered.")
          context.become(receiveBuildComponents())
          self ! "make_components"

        case ActorMessages.ChildActorCreated(created, _) ⇒
          logInfo(s"Created ${created.path.name}.")

      }
    }

    def receiveBuildComponents(): Receive = running() {
      reportsStatusF(onReportRequested = createStatusReport) {
        case "make_components" ⇒
          logInfo(s"Making components.")

          factories.buildEventLogs.foreach(factoryEntry ⇒ eventLogs ! UnfoldFactory(factoryEntry))

          factories.buildNexus.foreach(_(almhirtContext).onComplete(
            problem ⇒ {
              logError(s"Failed to create nexus props:\$problem")
              reportCriticalFailure(problem)
            },
            factory ⇒ self ! ActorMessages.CreateChildActor(factory, true, None)))

          factories.buildViews.foreach(factoryEntry ⇒ views ! UnfoldFactory(factoryEntry))
          factories.buildMisc.foreach(factoryEntry ⇒ misc ! UnfoldFactory(factoryEntry))
          factories.buildApps.foreach(factoryEntry ⇒ apps ! UnfoldFactory(factoryEntry))

        case ActorMessages.CreateChildActor(componentFactory, returnActorRef, correlationId) ⇒
          context.childFrom(componentFactory).fold(
            problem ⇒ {
              logError(s"""	|Failed to create "${componentFactory.name.getOrElse("<<<no name>>>")}":
            				|$problem""".stripMargin)
              sender() ! ActorMessages.CreateChildActorFailed(problem, correlationId)
            },
            actorRef ⇒ {
              logInfo(s"Created ${actorRef.path} @ ${actorRef.path} ")
              if (returnActorRef)
                sender() ! ActorMessages.ChildActorCreated(actorRef, correlationId)
            })

        case ActorMessages.CreateChildActorFailed(problem, _) ⇒
          logError(s"A child actor was not created:\n$problem")
          reportCriticalFailure(problem)

        case ActorMessages.ChildActorCreated(created, _) ⇒
          logInfo(s"Created ${created.path.name}.")
      }
    }

    def createStatusReport(options: StatusReportOptions): AlmFuture[StatusReport] = {
      val baseReport = StatusReport(s"${this.getClass.getSimpleName}-Report").withComponentState(componentState)

      appendToReportFromCollector(baseReport)(options)
    }

    override def preStart() {
      super.preStart()
      registerComponentControl()
      registerStatusReporter(description = Some("A report on ***ALL*** components(this might be pretty expensive...)"))
      logInfo("Starting...")
    }

    override def postStop() {
      super.postStop()
      deregisterComponentControl()
      deregisterStatusReporter()
      logWarning("Stopped")
    }

  }

  private[almhirt] abstract class SimpleUnfolder(implicit override val almhirtContext: AlmhirtContext) extends AlmActor with AlmActorLogging with ActorLogging with ControllableActor with StatusReportingActor {
    import akka.actor.SupervisorStrategy._

    implicit val executor = almhirtContext.futuresContext

    override val supervisorStrategy = OneForOneStrategy(maxNrOfRetries = 10, withinTimeRange = 1.minute) {
      case exn ⇒
        logError("Stopping a child", exn)
        reportCriticalFailure(exn)
        Stop
    }

    override val componentControl = LocalComponentControl(self, ActorMessages.ComponentControlActions.none, Some(logWarning))

    override val statusReportsCollector = Some(StatusReportsCollector(this.context))

    override def receive: Receive = running() {
      reportsStatusF(onReportRequested = createStatusReport) {
        case ActorMessages.CreateChildActors(factories, returnActorRefs, correlationId) ⇒
          logInfo(s"Creating:\n${factories.map(_.name.getOrElse("<<<no name>>>")).mkString(", ")}")
          factories.foreach { factory ⇒ self ! ActorMessages.CreateChildActor(factory, returnActorRefs, correlationId) }

        case UnfoldFactory(ComponentFactoryBuilderEntry(factoryBuilder, severity)) ⇒
          factoryBuilder(almhirtContext).onComplete(
            problem ⇒ {
              logError(s"Could not create component factory:\n$problem")
              reportFailure(problem, severity)
            },
            factory ⇒ self ! ActorMessages.CreateChildActor(factory, true, None))(almhirtContext.futuresContext)

        case ActorMessages.CreateChildActor(componentFactory, returnActorRef, correlationId) ⇒
          context.childFrom(componentFactory).fold(
            problem ⇒ {
              logError(s"""	|Failed to create "${componentFactory.name.getOrElse("<<<no name>>>")}":
            				      |$problem""".stripMargin)
              reportCriticalFailure(problem)
              sender() ! ActorMessages.CreateChildActorFailed(problem, correlationId)
            },
            actorRef ⇒ {
              logInfo(s"Created ${actorRef.path.name}.")
              if (returnActorRef)
                sender() ! ActorMessages.ChildActorCreated(actorRef, correlationId)
            })

        case ActorMessages.CreateChildActorFailed(problem, _) ⇒
          logError(s"A child actor was not created:\n$problem")
          reportCriticalFailure(problem)

        case ActorMessages.ChildActorCreated(created, _) ⇒

        case m: ActorMessages.HerderAppStartupMessage ⇒
          context.parent ! m
      }
    }

    def createStatusReport(options: StatusReportOptions): AlmFuture[StatusReport] = {
      val baseReport = StatusReport(s"${this.getClass.getSimpleName}-Report").withComponentState(componentState)

      appendToReportFromCollector(baseReport)(options)
    }

    override def preStart() {
      super.preStart()
      context.parent ! ActorMessages.ConsiderMeForReporting
      registerComponentControl()
      registerStatusReporter(description = None)
      logInfo("Starting...")
    }

    override def postStop() {
      super.postStop()
      deregisterComponentControl()
      deregisterStatusReporter()
      logWarning("Stopped")
    }

  }
}