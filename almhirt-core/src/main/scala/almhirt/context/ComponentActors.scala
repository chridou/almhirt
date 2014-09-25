package almhirt.context

import akka.actor._
import almhirt.almvalidation.kit._

private[almhirt] object componentactors {
  import almhirt.akkax.{ ActorMessages, ComponentFactory }

  def componentsProps(ctx: AlmhirtContext): Props =
    Props(new ComponentsActor(ctx))

  def viewsProps(ctx: AlmhirtContext): Props =
    Props(new ViewsActor(ctx))

  def eventLogsProps(ctx: AlmhirtContext): Props =
    Props(new EventLogsActor(ctx))

  def miscProps(ctx: AlmhirtContext): Props =
    Props(new MiscActor(ctx))

  def appsProps(ctx: AlmhirtContext): Props =
    Props(new AppsActor(ctx))

  final case class Unfold(factories: ComponentFactories)
  class ComponentsActor(ctx: AlmhirtContext) extends Actor with ActorLogging {
    val eventLogs = context.actorOf(eventLogsProps(ctx), "event-logs")
    val views = context.actorOf(viewsProps(ctx), "views")
    val misc = context.actorOf(miscProps(ctx), "misc")
    val apps = context.actorOf(appsProps(ctx), "apps")

    override def receive: Receive = {
      case m: Unfold => {
        log.info("Unfolding.")
        eventLogs ! m
        views ! m
        misc ! m
        apps ! m
        m.factories.buildNexus.foreach(_(ctx).onComplete(
          problem => log.error(s"Failed to create nexus props:\$problem"),
          factory => self ! ActorMessages.CreateChildActor(factory, false))(context.dispatcher))
      }

      case ActorMessages.CreateChildActor(componentFactory, returnActorRef) =>
        componentFactory(context).fold(
          problem => {
            log.error(s"""Failed to create "${componentFactory.name.getOrElse("<<<no name>>>")}".""")
            sender() ! ActorMessages.CreateChildActorFailed(problem)
          },
          actorRef =>
            if (returnActorRef)
              sender() ! ActorMessages.ChildActorCreated(actorRef))
    }
  }

  class EventLogsActor(ctx: AlmhirtContext) extends Actor with ActorLogging with almhirt.akkax.AlmActorSupport {
    override def receive: Receive = {

      case Unfold(ComponentFactories(buildEventLogs, _, _, _, _)) =>
        log.info("Unfolding.")
        buildEventLogs(ctx).onComplete(
          fail => log.error(s"Failed to create Props:\n$fail"),
          factories => factories.foreach { factory => self ! ActorMessages.CreateChildActor(factory, false) })(context.dispatcher)

      case ActorMessages.CreateChildActor(componentFactory, returnActorRef) =>
        componentFactory(context).fold(
          problem => {
            log.error(s"""Failed to create "${componentFactory.name.getOrElse("<<<no name>>>")}".""")
            sender() ! ActorMessages.CreateChildActorFailed(problem)
          },
          actorRef =>
            if (returnActorRef)
              sender() ! ActorMessages.ChildActorCreated(actorRef))
    }
  }

  class ViewsActor(ctx: AlmhirtContext) extends Actor with ActorLogging with almhirt.akkax.AlmActorSupport {
    override def receive: Receive = {
      case Unfold(ComponentFactories(_, buildViews, _, _, _)) =>
        log.info("Unfolding.")
        buildViews(ctx).onComplete(
          fail => log.error(s"Failed to create Props:\n$fail"),
          factories => factories.foreach { factory => self ! ActorMessages.CreateChildActor(factory, false) })(context.dispatcher)

      case ActorMessages.CreateChildActor(componentFactory, returnActorRef) =>
        componentFactory(context).fold(
          problem => {
            log.error(s"""Failed to create "${componentFactory.name.getOrElse("<<<no name>>>")}".""")
            sender() ! ActorMessages.CreateChildActorFailed(problem)
          },
          actorRef =>
            if (returnActorRef)
              sender() ! ActorMessages.ChildActorCreated(actorRef))
    }
  }

  class MiscActor(ctx: AlmhirtContext) extends Actor with ActorLogging with almhirt.akkax.AlmActorSupport {
    override def receive: Receive = {
      case Unfold(ComponentFactories(_, _, buildMisc, _, _)) =>
        log.info("Unfolding.")
        buildMisc(ctx).onComplete(
          fail => log.error(s"Failed to create Props:\n$fail"),
          factories => factories.foreach { factory => self ! ActorMessages.CreateChildActor(factory, false) })(context.dispatcher)

      case ActorMessages.CreateChildActor(componentFactory, returnActorRef) =>
        componentFactory(context).fold(
          problem => {
            log.error(s"""Failed to create "${componentFactory.name.getOrElse("<<<no name>>>")}".""")
            sender() ! ActorMessages.CreateChildActorFailed(problem)
          },
          actorRef =>
            if (returnActorRef)
              sender() ! ActorMessages.ChildActorCreated(actorRef))
    }
  }

  class AppsActor(ctx: AlmhirtContext) extends Actor with ActorLogging with almhirt.akkax.AlmActorSupport {
    override def receive: Receive = {
      case Unfold(ComponentFactories(_, _, _, buildApps, _)) =>
        log.info("Unfolding.")
        buildApps(ctx).onComplete(
          fail => log.error(s"Failed to create Props:\n$fail"),
          factories => factories.foreach { factory => self ! ActorMessages.CreateChildActor(factory, false) })(context.dispatcher)

      case ActorMessages.CreateChildActor(componentFactory, returnActorRef) =>
        componentFactory(context).fold(
          problem => {
            log.error(s"""Failed to create "${componentFactory.name.getOrElse("<<<no name>>>")}".""")
            sender() ! ActorMessages.CreateChildActorFailed(problem)
          },
          actorRef =>
            if (returnActorRef)
              sender() ! ActorMessages.ChildActorCreated(actorRef))
    }
  }

}