package almhirt.context

import akka.actor._
import almhirt.common._
import almhirt.streaming.AlmhirtStreams
import com.typesafe.config._

trait AlmhirtContext extends CanCreateUuidsAndDateTimes with AlmhirtStreams with HasExecutionContexts {
  def config: Config
}

object AlmhirtContextMessages {
  private[almhirt] case object Start
  private[almhirt] case class StreamsCreated(streams: AlmhirtStreams with Stoppable)
  private[almhirt] case class StreamsNotCreated(problem: Problem)
  private[almhirt] case class ContextCreated(ctx: AlmhirtContext with Stoppable)
  private[almhirt] case class ComponentsPropsCreated(props: Props, ctx: AlmhirtContext with Stoppable)
  private[almhirt] case class ComponentsPropsNotCreated(problem: Problem)

  private[almhirt] sealed trait FinishedResponse
  private[almhirt] case class FinishedInitialization(ctx: AlmhirtContext with Stoppable) extends FinishedResponse
  private[almhirt] case class FailedInitialization(problem: Problem) extends FinishedResponse

}

object AlmhirtContext {
  type ComponentFactory = AlmhirtContext => AlmFuture[Props]

  def apply(system: ActorSystem, actorName: Option[String], createComponents: Option[ComponentFactory]): AlmFuture[AlmhirtContext with Stoppable] = {
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
          def receive: Receive = {
            case AlmhirtContextMessages.Start =>
              theReceiver = sender()
              log.info("Creating streams.")
              AlmhirtStreams.createInternal(this.context, system.settings.config).onComplete(
                problem => self ! AlmhirtContextMessages.StreamsNotCreated(problem),
                streams => self ! AlmhirtContextMessages.StreamsCreated(streams))

            case AlmhirtContextMessages.StreamsCreated(streams) =>
              log.info("Creating context.")
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
                def stop() {
                  log.info("Stopping.")
                  streams.stop()
                  context.stop(self)
                }
              }
              self ! AlmhirtContextMessages.ContextCreated(ctx)

            case AlmhirtContextMessages.ContextCreated(ctx) =>
              createComponents match {
                case None =>
                  log.info("No components to create. Finished initialization.")
                  theReceiver ! AlmhirtContextMessages.FinishedInitialization(ctx)
                case Some(f) =>
                  log.info("Create components.")
                  f(ctx).onComplete(
                    problem => self ! AlmhirtContextMessages.ComponentsPropsNotCreated(problem),
                    props => self ! AlmhirtContextMessages.ComponentsPropsCreated(props, ctx))
              }

            case AlmhirtContextMessages.ComponentsPropsCreated(props, ctx) =>
              context.actorOf(props, componentsActorname)
              log.info("Components created")
              theReceiver ! AlmhirtContextMessages.FinishedInitialization(ctx)
              log.info("Finished initialization.")

            case AlmhirtContextMessages.StreamsNotCreated(prob) =>
              log.error(s"Could not create streams:\n$prob")
              theReceiver ! AlmhirtContextMessages.FailedInitialization(prob)

            case AlmhirtContextMessages.ComponentsPropsNotCreated(prob) =>
              log.error(s"Could not create components:\n$prob")
              theReceiver ! AlmhirtContextMessages.FailedInitialization(prob)
          }
        })
      }

    propsV.fold(
      fail => AlmFuture.failed(fail),
      almhirtProps => {
        val sn = actorName getOrElse (this.actorname)
        val theAlmhirt = system.actorOf(almhirtProps, sn)

        import scala.concurrent.duration._
        import almhirt.almvalidation.kit._
        import almhirt.almfuture.all._
        val maxInitDur =
          system.settings.config.opt[FiniteDuration]("almhirt.context.max-init-duration").fold(
            fail => {
              5.seconds
            },
            d => {
              d.getOrElse(5.seconds)
            })

        import akka.pattern._
        implicit val execCtx = system.dispatcher
        (theAlmhirt ? AlmhirtContextMessages.Start)(maxInitDur).successfulAlmFuture[AlmhirtContextMessages.FinishedResponse].mapV {
          case AlmhirtContextMessages.FinishedInitialization(ctx) => scalaz.Success(ctx)
          case AlmhirtContextMessages.FailedInitialization(prob) => scalaz.Failure(prob)
        }
      })
  }

  val actorname = "almhirt"
  val componentsActorname = "components"
}