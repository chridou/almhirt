package almhirt.context

import akka.actor._
import almhirt.almvalidation.kit._

private[almhirt] object componentactors {
  import almhirt.akkax.ActorMessages

  def componentsProps(ctx: AlmhirtContext): Props =
    Props(new ComponentsActor(ctx))

  def viewsProps(ctx: AlmhirtContext): Props =
    Props(new ViewsActor(ctx))

  def eventLogsProps(ctx: AlmhirtContext): Props =
    Props(new EventLogsActor(ctx))

  def miscProps(ctx: AlmhirtContext): Props =
    Props(new MiscActor(ctx))

  final case class Unfold(factories: ComponentFactories)
  class ComponentsActor(ctx: AlmhirtContext) extends Actor with ActorLogging {
    val eventLogs = context.actorOf(eventLogsProps(ctx), "event-logs")
    val views = context.actorOf(viewsProps(ctx), "views")
    val misc = context.actorOf(miscProps(ctx), "misc")
    override def receive: Receive = {
      case m: Unfold => {
        log.info("Unfolding.")
        eventLogs ! m
        views ! m
        misc ! m
      }

      case ActorMessages.CreateChildActor(props, nameOpt, returnActorRef) =>
        inTryCatch {
          val actorRef =
            nameOpt match {
              case Some(name) =>
                context.actorOf(props, name)
              case None =>
                context.actorOf(props)
            }
          log.info(s"""Created "${actorRef.path}".""")
          actorRef
        }.fold(
          problem => {
            log.error(s"""Failed to create "${nameOpt.getOrElse("<<<no name>>>")}".""")
            sender() ! ActorMessages.CreateChildActorFailed(problem)
          },
          actorRef =>
            if (returnActorRef)
              sender() ! ActorMessages.ChildActorCreated(actorRef))
    }
  }

  class EventLogsActor(ctx: AlmhirtContext) extends Actor with ActorLogging with almhirt.akkax.AlmActorSupport {
    override def receive: Receive = {

      case Unfold(ComponentFactories(buildEventLogs, _, _)) =>
        log.info("Unfolding.")
        buildEventLogs(ctx).onComplete(
          fail => log.error("Failed to create Props."),
          propsByName => propsByName.foreach { case (name, props) => self ! ActorMessages.CreateChildActor(props, Some(name), false) })(context.dispatcher)

      case ActorMessages.CreateChildActor(props, nameOpt, returnActorRef) =>
        inTryCatch {
          val actorRef =
            nameOpt match {
              case Some(name) =>
                context.actorOf(props, name)
              case None =>
                context.actorOf(props)
            }
          log.info(s"""Created "${actorRef.path}".""")
          actorRef
        }.fold(
          problem => {
            log.error(s"""Failed to create "${nameOpt.getOrElse("<<<no name>>>")}".""")
            sender() ! ActorMessages.CreateChildActorFailed(problem)
          },
          actorRef =>
            if (returnActorRef)
              sender() ! ActorMessages.ChildActorCreated(actorRef))
    }
  }

  class ViewsActor(ctx: AlmhirtContext) extends Actor with ActorLogging with almhirt.akkax.AlmActorSupport {
    override def receive: Receive = {
      case Unfold(ComponentFactories(_, buildViews, _)) =>
        log.info("Unfolding.")
        buildViews(ctx).onComplete(
          fail => log.error("Failed to create Props."),
          propsByName => propsByName.foreach { case (name, props) => self ! ActorMessages.CreateChildActor(props, Some(name), false) })(context.dispatcher)

      case ActorMessages.CreateChildActor(props, nameOpt, returnActorRef) =>
        inTryCatch {
          val actorRef =
            nameOpt match {
              case Some(name) =>
                context.actorOf(props, name)
              case None =>
                context.actorOf(props)
            }
          log.info(s"""Created "${actorRef.path}".""")
          actorRef
        }.fold(
          problem => {
            log.error(s"""Failed to create "${nameOpt.getOrElse("<<<no name>>>")}".""")
            sender() ! ActorMessages.CreateChildActorFailed(problem)
          },
          actorRef =>
            if (returnActorRef)
              sender() ! ActorMessages.ChildActorCreated(actorRef))
    }
  }

  class MiscActor(ctx: AlmhirtContext) extends Actor with ActorLogging with almhirt.akkax.AlmActorSupport {
    override def receive: Receive = {
      case Unfold(ComponentFactories(_, _, buildMisc)) =>
        log.info("Unfolding.")
        buildMisc(ctx).onComplete(
          fail => log.error("Failed to create Props."),
          propsByName => propsByName.foreach { case (name, props) => self ! ActorMessages.CreateChildActor(props, Some(name), false) })(context.dispatcher)

      case ActorMessages.CreateChildActor(props, nameOpt, returnActorRef) =>
        inTryCatch {
          val actorRef =
            nameOpt match {
              case Some(name) =>
                context.actorOf(props, name)
              case None =>
                context.actorOf(props)
            }
          log.info(s"""Created "${actorRef.path}".""")
          actorRef
        }.fold(
          problem => {
            log.error(s"""Failed to create "${nameOpt.getOrElse("<<<no name>>>")}".""")
            sender() ! ActorMessages.CreateChildActorFailed(problem)
          },
          actorRef =>
            if (returnActorRef)
              sender() ! ActorMessages.ChildActorCreated(actorRef))
    }
  }

}