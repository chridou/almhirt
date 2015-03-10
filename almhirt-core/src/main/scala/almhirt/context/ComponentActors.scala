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
    dedicatedAppsDispatcher: Option[String],
    dedicatedAppsFuturesExecutor: Option[ExecutionContext])(implicit ctx: AlmhirtContext): Props =
    Props(new ComponentsSupervisor(dedicatedAppsDispatcher, dedicatedAppsFuturesExecutor))

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

  /**
   * This is all a bit hacky since I don't know yet, how I want this to behave like...
   */
  class ComponentsSupervisor(
    dedicatedAppsDispatcher: Option[String],
    dedicatedAppsFuturesExecutor: Option[ExecutionContext])(implicit override val almhirtContext: AlmhirtContext) extends AlmActor with AlmActorLogging {
    import akka.actor.SupervisorStrategy._
    override val supervisorStrategy = OneForOneStrategy(maxNrOfRetries = 10, withinTimeRange = 1.minute) {
      case exn ⇒
        logError("Stopping a child", exn)
        reportCriticalFailure(exn)
        Stop
    }

    implicit val executor = almhirtContext.futuresContext

    val eventLogs = context.actorOf(eventLogsProps(almhirtContext), "event-logs")
    logInfo(s"Created ${eventLogs.path.name}.")
    val views = context.actorOf(viewsProps(almhirtContext), "views")
    logInfo(s"Created ${views.path.name}.")
    val misc = context.actorOf(miscProps(almhirtContext), "misc")
    logInfo(s"Created ${misc.path.name}.")

    val appsFuturesExecutor =
      dedicatedAppsFuturesExecutor match {
        case Some(dafe) ⇒
          logInfo("Using dedicated futures executor for apps")
          dafe
        case None ⇒
          logWarning("Using default futures executor for apps")
          almhirtContext.futuresContext
      }

    val apps =
      dedicatedAppsDispatcher match {
        case Some(name) ⇒
          logInfo(s"Using dedicated apps dispatcher: $name")
          context.actorOf(appsProps(almhirtContext.withFuturesExecutor(appsFuturesExecutor)).withDispatcher(name), "apps")
        case None ⇒
          logWarning("Using default dispatcher as apps dispatcher.")
          context.actorOf(appsProps(almhirtContext.withFuturesExecutor(appsFuturesExecutor)), "apps")
      }
    logInfo(s"Created ${apps.path.name}.")

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
            context.become(receiveBuildEarlyComponents(factories.buildEventLogs.size + factories.buildNexus.size + factories.buildViews.size))
            self ! "early"
        }

      case ActorMessages.HerderServiceAppFailedToStart(problem) ⇒
        sys.error(problem.toString())

      case ActorMessages.HerderServiceAppStarted ⇒
        logInfo("No herder app started.")
        context.become(receiveBuildEarlyComponents(factories.buildEventLogs.size + factories.buildNexus.size + factories.buildViews.size))
        self ! "early"

    }

    def receiveBuildEarlyComponents(stillToCreate: Int): Receive = {
      case "early" ⇒ {
        logInfo(s"Unfolding early components($stillToCreate).")

        if (stillToCreate == 0) {
          context.become(receiveBuildLateComponents(factories.buildMisc.size + factories.buildApps.size))
          self ! "late"
        } else {
          factories.buildEventLogs.foreach({
            case ComponentFactoryBuilderEntry(factoryBuilder, severity) ⇒
              factoryBuilder(almhirtContext).onComplete(
                problem ⇒ {
                  logError(s"Could not create component factory for event logs:\n$problem")
                  reportFailure(problem, severity)
                },
                factory ⇒ eventLogs ! ActorMessages.CreateChildActor(factory, true, None))(almhirtContext.futuresContext)
          })

          factories.buildNexus.foreach(_(almhirtContext).onComplete(
            problem ⇒ {
              logError(s"Failed to create nexus props:\$problem")
              reportCriticalFailure(problem)
            },
            factory ⇒ self ! ActorMessages.CreateChildActor(factory, true, None))(context.dispatcher))

          factories.buildViews.foreach({
            case ComponentFactoryBuilderEntry(factoryBuilder, severity) ⇒
              factoryBuilder(almhirtContext).onComplete(
                problem ⇒ {
                  logError(s"Could not create component factory for views:\n$problem")
                  reportFailure(problem, severity)
                },
                factory ⇒ views ! ActorMessages.CreateChildActor(factory, true, None))(almhirtContext.futuresContext)
          })

        }

      }

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
        val newNumToCreate = stillToCreate - 1
        logError(s"A child actor was not created:\n$problem")
        reportCriticalFailure(problem)
        if (newNumToCreate == 0) {
          context.become(receiveBuildLateComponents(factories.buildMisc.size + factories.buildApps.size))
          self ! "late"
        } else {
          context.become(receiveBuildEarlyComponents(newNumToCreate))
        }

      case ActorMessages.ChildActorCreated(_, _) ⇒
        val newNumToCreate = stillToCreate - 1
        logInfo(s"Still to create $newNumToCreate.")
        if (newNumToCreate == 0) {
          context.become(receiveBuildLateComponents(factories.buildMisc.size + factories.buildApps.size))
          self ! "late"
        } else {
          context.become(receiveBuildEarlyComponents(newNumToCreate))
        }
    }

    def receiveBuildLateComponents(stillToCreate: Int): Receive = {
      case "late" ⇒ {
        logInfo(s"Unfolding late components($stillToCreate).")

        factories.buildMisc.foreach({
          case ComponentFactoryBuilderEntry(factoryBuilder, severity) ⇒
            factoryBuilder(almhirtContext).onComplete(
              problem ⇒ {
                logError(s"Could not create component factory for misc:\n$problem")
                reportFailure(problem, severity)
              },
              factory ⇒ misc ! ActorMessages.CreateChildActor(factory, true, None))(almhirtContext.futuresContext)
        })

        factories.buildApps.foreach({
          case ComponentFactoryBuilderEntry(factoryBuilder, severity) ⇒
            factoryBuilder(almhirtContext).onComplete(
              problem ⇒ {
                logError(s"Could not create component factory for apps:\n$problem")
                reportFailure(problem, severity)
              },
              factory ⇒ apps ! ActorMessages.CreateChildActor(factory, true, None))(almhirtContext.futuresContext)
        })

      }

      case ActorMessages.CreateChildActorFailed(problem, _) ⇒
        val newNumToCreate = stillToCreate - 1
        logError(s"A child actor was not created:\n$problem")
        reportCriticalFailure(problem)
        if (newNumToCreate != 0) {
          context.become(receiveBuildLateComponents(newNumToCreate))
        }

      case ActorMessages.ChildActorCreated(_, _) ⇒
        val newNumToCreate = stillToCreate - 1
        logInfo(s"Still to create $newNumToCreate.")
        if (newNumToCreate != 0) {
          context.become(receiveBuildLateComponents(newNumToCreate))
        }
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

      case ActorMessages.CreateChildActor(componentFactory, returnActorRef, correlationId) ⇒
        context.childFrom(componentFactory).fold(
          problem ⇒ {
            logError(s"""	|Failed to create "${componentFactory.name.getOrElse("<<<no name>>>")}":
            				      |$problem""".stripMargin)
            reportCriticalFailure(problem)
            sender() ! ActorMessages.CreateChildActorFailed(problem, correlationId)
          },
          actorRef ⇒ {
            logInfo(s"Created ${actorRef.path} @ ${actorRef.path} ")
            if (returnActorRef)
              sender() ! ActorMessages.ChildActorCreated(actorRef, correlationId)
          })

      case m: ActorMessages.HerderAppStartupMessage ⇒
        context.parent ! m
    }
  }
}