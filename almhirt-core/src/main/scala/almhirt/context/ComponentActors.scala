package almhirt.context

import scala.concurrent.duration._
import scala.concurrent.ExecutionContext
import akka.actor._
import almhirt.common._
import almhirt.akkax._
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
      dedicatedAppsFuturesExecutor: Option[String])(implicit override val almhirtContext: AlmhirtContext) extends AlmActor with AlmActorLogging {
    import akka.actor.SupervisorStrategy._
    override val supervisorStrategy = OneForOneStrategy(maxNrOfRetries = 10, withinTimeRange = 1.minute) {
      case exn ⇒
        logError("Stopping a child", exn)
        reportCriticalFailure(exn)
        Stop
    }

    implicit val executor = almhirtContext.futuresContext

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

    def receiveStart(canAdvance: Boolean): Receive = {
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

    def receiveBuildHerderApp: Receive = {
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
            context.become(receiveBuildComponents())
            self ! "make_components"
        }

      case ActorMessages.HerderServiceAppFailedToStart(problem) ⇒
        sys.error(problem.toString())

      case ActorMessages.HerderServiceAppStarted ⇒
        logInfo("No herder app started.")
        context.become(receiveBuildComponents())
        self ! "make_components"

    }

    def receiveBuildComponents(): Receive = {
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

        AlmhirtReporter.componentFactory().fold(
          problem ⇒ {
            logError(s"Failed to create AlmhirtReporter:\$problem")
            reportCriticalFailure(problem)
          },
          factory ⇒ self ! ActorMessages.CreateChildActor(factory, true, None))

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

  private[almhirt] abstract class SimpleUnfolder(implicit override val almhirtContext: AlmhirtContext) extends AlmActor with AlmActorLogging with ActorLogging {
    import akka.actor.SupervisorStrategy._

    override val supervisorStrategy = OneForOneStrategy(maxNrOfRetries = 10, withinTimeRange = 1.minute) {
      case exn ⇒
        logError("Stopping a child", exn)
        reportCriticalFailure(exn)
        Stop
    }

    override def receive: Receive = {
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

    override def preStart() {
      super.preStart()
      logInfo("Starting...")
    }
  }
}