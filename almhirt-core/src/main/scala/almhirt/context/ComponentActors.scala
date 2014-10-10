package almhirt.context

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
    Props(new ComponentsSupervisor(ctx))

  def viewsProps(implicit ctx: AlmhirtContext): Props =
    Props(new ViewsSupervisor())

  def eventLogsProps(implicit ctx: AlmhirtContext): Props =
    Props(new EventLogsSupervisor())

  def miscProps(implicit ctx: AlmhirtContext): Props =
    Props(new MiscSupervisor())

  def appsProps(implicit ctx: AlmhirtContext): Props =
    Props(new AppsSupervisor())

  class ViewsSupervisor()(implicit override val almhirtContext: AlmhirtContext) extends SimpleUnfolder
  class EventLogsSupervisor()(implicit override val almhirtContext: AlmhirtContext) extends SimpleUnfolder
  class MiscSupervisor()(implicit override val almhirtContext: AlmhirtContext) extends SimpleUnfolder
  class AppsSupervisor()(implicit override val almhirtContext: AlmhirtContext) extends SimpleUnfolder

  final case class UnfoldFromFactories(factories: ComponentFactories)

  class ComponentsSupervisor(ctx: AlmhirtContext) extends Actor with ActorLogging {
    val eventLogs = context.actorOf(eventLogsProps(ctx), "event-logs")
    val views = context.actorOf(viewsProps(ctx), "views")
    val misc = context.actorOf(miscProps(ctx), "misc")
    val apps = context.actorOf(appsProps(ctx), "apps")

    override def receive: Receive = {
      case UnfoldFromFactories(factories) ⇒ {
        log.info("Unfolding components.")
        factories.buildEventLogs(ctx).onComplete(
          problem ⇒ log.error(s"Could not create component factories for event logs:\n$problem"),
          factories ⇒ eventLogs ! ActorMessages.CreateChildActors(factories, false, None))(ctx.futuresContext)
        factories.buildViews(ctx).onComplete(
          problem ⇒ log.error(s"Could not create component factories for views:\n$problem"),
          factories ⇒ views ! ActorMessages.CreateChildActors(factories, false, None))(ctx.futuresContext)
        factories.buildMisc(ctx).onComplete(
          problem ⇒ log.error(s"Could not create component factories for misc:\n$problem"),
          factories ⇒ misc ! ActorMessages.CreateChildActors(factories, false, None))(ctx.futuresContext)
        factories.buildApps(ctx).onComplete(
          problem ⇒ log.error(s"Could not create component factories for apps:\n$problem"),
          factories ⇒ apps ! ActorMessages.CreateChildActors(factories, false, None))(ctx.futuresContext)
        factories.buildNexus.foreach(_(ctx).onComplete(
          problem ⇒ log.error(s"Failed to create nexus props:\$problem"),
          factory ⇒ self ! ActorMessages.CreateChildActor(factory, false, None))(context.dispatcher))
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

  trait SimpleUnfolder extends Actor with ActorLogging with HasAlmhirtContext with almhirt.akkax.AlmActorSupport {
    override def receive: Receive = {
      case ActorMessages.CreateChildActors(factories, returnActorRefs, correlationId) ⇒
        log.info(s"Creating:\n${factories.map(_.name.getOrElse("<<<no name>>>")).mkString(", ")}")
        factories.foreach { factory ⇒ self ! ActorMessages.CreateChildActor(factory, returnActorRefs, correlationId) }

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
}