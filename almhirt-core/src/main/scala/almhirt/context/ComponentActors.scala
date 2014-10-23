package almhirt.context

import scala.concurrent.duration._
import akka.actor._
import almhirt.common._
import almhirt.akkax._
import almhirt.almvalidation.kit._
import almhirt.tracking.CorrelationId

object SupervisorPaths {
  val eventLogs = ""
  val views = ""
  val misc = ""
  val apps = ""
}

private[almhirt] object componentactors {
  import almhirt.akkax.{ ActorMessages, ComponentFactory }

  def componentsProps(implicit ctx: AlmhirtContext): Props =
    Props(new ComponentsSupervisor)

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

  class ComponentsSupervisor(implicit override val almhirtContext: AlmhirtContext) extends AlmActor with AlmActorLogging with ActorLogging {
    import akka.actor.SupervisorStrategy._
    override val supervisorStrategy = OneForOneStrategy(maxNrOfRetries = 10, withinTimeRange = 1.minute) {
      case exn =>
        logError("Stopping a child", exn)
        reportCriticalFailure(exn)
        Stop
    }

    val eventLogs = context.actorOf(eventLogsProps(almhirtContext), "event-logs")
    val views = context.actorOf(viewsProps(almhirtContext), "views")
    val misc = context.actorOf(miscProps(almhirtContext), "misc")
    val apps = context.actorOf(appsProps(almhirtContext), "apps")

    override def receive: Receive = {
      case UnfoldFromFactories(factories) ⇒ {
        log.info("Unfolding components.")

        factories.buildEventLogs.foreach({
          case ComponentFactoryBuilderEntry(factoryBuilder, severity) =>
            factoryBuilder(almhirtContext).onComplete(
              problem ⇒ {
                logError(s"Could not create component factory for event logs:\n$problem")
                reportFailure(problem, severity)
              },
              factory ⇒ eventLogs ! ActorMessages.CreateChildActor(factory, false, None))(almhirtContext.futuresContext)
        })

        factories.buildNexus.foreach(_(almhirtContext).onComplete(
          problem ⇒ {
            logError(s"Failed to create nexus props:\$problem")
            reportCriticalFailure(problem)
          },
          factory ⇒ self ! ActorMessages.CreateChildActor(factory, false, None))(context.dispatcher))

        factories.buildViews.foreach({
          case ComponentFactoryBuilderEntry(factoryBuilder, severity) =>
            factoryBuilder(almhirtContext).onComplete(
              problem ⇒ {
                logError(s"Could not create component factory for views:\n$problem")
                reportFailure(problem, severity)
              },
              factory ⇒ views ! ActorMessages.CreateChildActor(factory, false, None))(almhirtContext.futuresContext)
        })

        factories.buildMisc.foreach({
          case ComponentFactoryBuilderEntry(factoryBuilder, severity) =>
            factoryBuilder(almhirtContext).onComplete(
              problem ⇒ {
                logError(s"Could not create component factory for misc:\n$problem")
                reportFailure(problem, severity)
              },
              factory ⇒ misc ! ActorMessages.CreateChildActor(factory, false, None))(almhirtContext.futuresContext)
        })

        factories.buildApps.foreach({
          case ComponentFactoryBuilderEntry(factoryBuilder, severity) =>
            factoryBuilder(almhirtContext).onComplete(
              problem ⇒ {
                logError(s"Could not create component factory for apps:\n$problem")
                reportFailure(problem, severity)
              },
              factory ⇒ apps ! ActorMessages.CreateChildActor(factory, false, None))(almhirtContext.futuresContext)
        })

      }

      case ActorMessages.CreateChildActor(componentFactory, returnActorRef, correlationId) ⇒
        context.childFrom(componentFactory).fold(
          problem ⇒ {
            log.error(s"""	|Failed to create "${componentFactory.name.getOrElse("<<<no name>>>")}":
            				|$problem""".stripMargin)
            sender() ! ActorMessages.CreateChildActorFailed(problem, correlationId)
          },
          actorRef ⇒ {
            log.info(s"Created ${actorRef.path} @ ${actorRef.path} ")
            if (returnActorRef)
              sender() ! ActorMessages.ChildActorCreated(actorRef, correlationId)
          })
    }
  }

  private[almhirt] abstract class SimpleUnfolder(implicit override val almhirtContext: AlmhirtContext) extends AlmActor with AlmActorLogging with ActorLogging {
    import akka.actor.SupervisorStrategy._
    override val supervisorStrategy = OneForOneStrategy(maxNrOfRetries = 10, withinTimeRange = 1.minute) {
      case exn =>
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
    }
  }
}