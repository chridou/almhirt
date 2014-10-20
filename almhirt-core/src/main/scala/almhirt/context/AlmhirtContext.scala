package almhirt.context

import akka.actor._
import scalaz.Validation.FlatMap._
import almhirt.common._
import almhirt.streaming.AlmhirtStreams
import almhirt.akkax.ActorMessages
import com.typesafe.config._

trait AlmhirtContext extends CanCreateUuidsAndDateTimes with AlmhirtStreams with HasExecutionContexts {
  def config: Config
  def localActorPaths: ContextActorPaths
  def tellHerder(what: almhirt.herder.HerderMessages.HerderNotificicationMessage): Unit
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

  def apply(system: ActorSystem, actorName: Option[String], componentFactories: ComponentFactories): AlmFuture[AlmhirtContext with Stoppable] = {
    import almhirt.configuration._

    val propsV =
      for {
        configSection <- system.settings.config.v[com.typesafe.config.Config]("almhirt.context")
        useFuturesCtx <- configSection.v[Boolean]("use-dedicated-futures-dispatcher")
        useBlockersCtx <- configSection.v[Boolean]("use-dedicated-blockers-dispatcher")
        useCrunchersCtx <- configSection.v[Boolean]("use-dedicated-cruncher-dispatcher")
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

        Props(new Actor with ActorLogging {
          implicit val execCtx = futuresExecutor
          var theReceiver: ActorRef = null
          var tellTheHerder: almhirt.herder.HerderMessages.HerderNotificicationMessage => Unit = x => ()
          def receive: Receive = {
            case AlmhirtContextMessages.Start ⇒
              theReceiver = sender()
              log.info("Create streams")
              AlmhirtStreams.createInternal(this.context, system.settings.config).onComplete(
                problem ⇒ self ! AlmhirtContextMessages.StreamsNotCreated(problem),
                streams ⇒ self ! AlmhirtContextMessages.StreamsCreated(streams))

            case AlmhirtContextMessages.StreamsCreated(streams) ⇒
              log.info("Created streams. Next: Create context")
              val ccuad = CanCreateUuidsAndDateTimes()
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
                val localActorPaths = ContextActorPaths.local(system)
                def tellHerder(what: almhirt.herder.HerderMessages.HerderNotificicationMessage) { tellTheHerder(what) }

                def stop() {
                  log.info("Stopping.")
                  //streams.stop()
                  context.stop(self)
                }
              }
              self ! AlmhirtContextMessages.ContextCreated(ctx)

            case AlmhirtContextMessages.ContextCreated(ctx) ⇒
              log.info("Context created. Next: Configure herder")
              (for {
                herderProps <- almhirt.herder.Herder.props()(ctx)
              } yield herderProps).fold(
                prob => theReceiver ! AlmhirtContextMessages.FailedInitialization(prob),
                herderProps => {
                  val herder = context.actorOf(herderProps, almhirt.herder.Herder.actorname)
                  this.tellTheHerder = x => herder ! x
                  self ! AlmhirtContextMessages.HerderCreated(ctx)
                })
               theReceiver ! AlmhirtContextMessages.FinishedInitialization(ctx)

            case AlmhirtContextMessages.HerderCreated(ctx) ⇒
              log.info("Context created. Next: Configure components")
              val components = context.actorOf(componentactors.componentsProps(ctx), "components")
              components ! componentactors.UnfoldFromFactories(componentFactories)
              theReceiver ! AlmhirtContextMessages.FinishedInitialization(ctx)

            case AlmhirtContextMessages.StreamsNotCreated(prob) ⇒
              log.error(s"Could not create streams:\n$prob")
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
          case AlmhirtContextMessages.FailedInitialization(prob) ⇒ scalaz.Failure(prob)
        }
      }))
  }

  val actorname = "almhirt"
  val componentsActorname = "components"

  object TestContext {
    def noComponentsDefaultGlobalDispatcher(contextName: String)(implicit system: ActorSystem, maxDur: scala.concurrent.duration.FiniteDuration): AlmFuture[AlmhirtContext with Stoppable] =
      noComponentsDefaultGlobalDispatcher(contextName, CanCreateUuidsAndDateTimes())

    def noComponentsDefaultGlobalDispatcher(contextName: String, ccuad: CanCreateUuidsAndDateTimes)(implicit system: ActorSystem, maxDur: scala.concurrent.duration.FiniteDuration): AlmFuture[AlmhirtContext with Stoppable] = {
      val futuresExecutor = system.dispatchers.defaultGlobalDispatcher
      val crunchersExecutor = system.dispatchers.defaultGlobalDispatcher
      val blockersExecutor = system.dispatchers.defaultGlobalDispatcher

      val almhirtProps = Props(new Actor with ActorLogging {
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
        case AlmhirtContextMessages.FailedInitialization(prob) ⇒ scalaz.Failure(prob)
      }
    }
  }
}