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

    val futuresExecutor = system.dispatchers.lookup("almhirt.context.dispatchers.futures-dispatcher")
    val crunchersExecutor = system.dispatchers.lookup("almhirt.context.dispatchers.cruncher-dispatcher")
    val blockersExecutor = system.dispatchers.lookup("almhirt.context.dispatchers.blockers-dispatcher")

    val almhirtProps = Props(new Actor with ActorLogging {
      implicit val execCtx = futuresExecutor
      var theReceiver: ActorRef = null
      def receive: Receive = {
        case AlmhirtContextMessages.Start =>
          theReceiver = sender()
          log.info("Creating streams.")
          AlmhirtStreams.createInternal(this.context).onComplete(
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
            def parseUuid(str: String) = ccuad.parseUuid(str)
            val eventBroker = streams.eventBroker
            val eventStream = streams.eventStream
            val systemEventStream = streams.systemEventStream
            val domainEventStream = streams.domainEventStream
            val aggregateEventStream = streams.aggregateEventStream
            val commandBroker = streams.commandBroker
            val commandStream = streams.commandStream
            val systemCommandStream = streams.systemCommandStream
            val domainCommandStream = streams.domainCommandStream
            val aggregateCommandStream = streams.aggregateCommandStream
            def stop() {
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
    val sn = actorName getOrElse (this.actorname)
    val theAlmhirt = system.actorOf(almhirtProps, sn)

    import scala.concurrent.duration._
    import almhirt.almvalidation.kit._
    import almhirt.almfuture.all._
    import almhirt.configuration._
    val maxInitDur =
      system.settings.config.opt[FiniteDuration]("almhirt.context.max-init-duration").fold(
        fail => {
          5.seconds
        },
        d => {
          d.getOrElse(5.seconds)
        })
    import akka.pattern._
    implicit val execCtx = futuresExecutor
    (theAlmhirt ? AlmhirtContextMessages.Start)(maxInitDur).successfulAlmFuture[AlmhirtContextMessages.FinishedResponse].mapV {
      case AlmhirtContextMessages.FinishedInitialization(ctx) => scalaz.Success(ctx)
      case AlmhirtContextMessages.FailedInitialization(prob) => scalaz.Failure(prob)
    }
  }

  val actorname = "almhirt"
  val componentsActorname = "components"
}