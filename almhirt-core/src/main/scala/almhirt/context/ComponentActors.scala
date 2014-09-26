package almhirt.context

import akka.actor._
import almhirt.common._
import almhirt.akkax._

import almhirt.almvalidation.kit._

object SupervisorPaths {
  val eventLogs = ""
  val views = ""
  val misc = ""
  val apps = ""
}

private[almhirt] object componentactors {
  import almhirt.akkax.{ ActorMessages, ComponentFactory }

  def componentsProps(implicit ctx: AlmhirtContext): Props =
    Props(new ComponentsActor(ctx))

  def viewsProps(implicit ctx: AlmhirtContext): Props =
    Props(new ViewsSupervisor())

  def eventLogsProps(implicit ctx: AlmhirtContext): Props =
    Props(new EventLogsSupervisor())

  def miscProps(implicit ctx: AlmhirtContext): Props =
    Props(new MiscSupervisor())

  def appsProps(implicit ctx: AlmhirtContext): Props =
    Props(new AppsSupervisor())

  final case class Unfold(factories: ComponentFactories)

  class ComponentsActor(ctx: AlmhirtContext) extends Actor with ActorLogging {
    val eventLogs = context.actorOf(eventLogsProps(ctx), "event-logs")
    val views = context.actorOf(viewsProps(ctx), "views")
    val misc = context.actorOf(miscProps(ctx), "misc")
    val apps = context.actorOf(appsProps(ctx), "apps")

    override def receive: Receive = {
      case m: Unfold => {
        log.info("Unfolding components.")
        eventLogs ! m
        views ! m
        misc ! m
        apps ! m
        m.factories.buildNexus.foreach(_(ctx).onComplete(
          problem => log.error(s"Failed to create nexus props:\$problem"),
          factory => self ! ActorMessages.CreateChildActor(factory, false))(context.dispatcher))
      }

      case ActorMessages.CreateChildActor(componentFactory, returnActorRef) =>
        context.childFrom(componentFactory).fold(
          problem => {
            log.error(s"""	|Failed to create "${componentFactory.name.getOrElse("<<<no name>>>")}":
            				|$problem""".stripMargin)
            sender() ! ActorMessages.CreateChildActorFailed(problem)
          },
          actorRef => {
            log.info(s"Created ${actorRef.path} @ ${actorRef.path} ")
            if (returnActorRef)
              sender() ! ActorMessages.ChildActorCreated(actorRef)
          })
    }
  }

  trait SimpleUnfolder extends Actor with ActorLogging with HasAlmhirtContext with almhirt.akkax.AlmActorSupport {
    def extractFactories(cf: ComponentFactories): AlmFuture[Seq[ComponentFactory]]

    override def receive: Receive = {
      case Unfold(cf: ComponentFactories) =>
        extractFactories(cf).onComplete(
          fail => log.error(s"Failed to create component factories:\n$fail"),
          factories => {
            log.info(s"Unfolding:\n${factories.map(_.name.getOrElse("<<<no name>>>")).mkString(", ")}")
            factories.foreach { factory => self ! ActorMessages.CreateChildActor(factory, false) }
          })(context.dispatcher)

      case ActorMessages.CreateChildActor(componentFactory, returnActorRef) =>
        context.childFrom(componentFactory).fold(
          problem => {
            log.error(s"""	|Failed to create "${componentFactory.name.getOrElse("<<<no name>>>")}":
            				|$problem""".stripMargin)
            sender() ! ActorMessages.CreateChildActorFailed(problem)
          },
          actorRef => {
            log.info(s"Created ${actorRef.path} @ ${actorRef.path} ")
            if (returnActorRef)
              sender() ! ActorMessages.ChildActorCreated(actorRef)
          })
    }
  }

  class ViewsSupervisor()(implicit override val almhirtContext: AlmhirtContext) extends SimpleUnfolder {
    override def extractFactories(cf: ComponentFactories): AlmFuture[Seq[ComponentFactory]] =
      cf.buildViews(almhirtContext)
  }

  class EventLogsSupervisor()(implicit override val almhirtContext: AlmhirtContext) extends SimpleUnfolder {
    override def extractFactories(cf: ComponentFactories): AlmFuture[Seq[ComponentFactory]] =
      cf.buildEventLogs(almhirtContext)
  }

  class MiscSupervisor()(implicit override val almhirtContext: AlmhirtContext) extends SimpleUnfolder {
    override def extractFactories(cf: ComponentFactories): AlmFuture[Seq[ComponentFactory]] =
      cf.buildMisc(almhirtContext)
  }

  class AppsSupervisor()(implicit override val almhirtContext: AlmhirtContext) extends SimpleUnfolder {
    override def extractFactories(cf: ComponentFactories): AlmFuture[Seq[ComponentFactory]] =
      cf.buildApps(almhirtContext)
  }
}