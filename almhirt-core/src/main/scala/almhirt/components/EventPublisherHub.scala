package almhirt.components

import scala.collection.immutable.Queue
import scala.concurrent.duration._
import scalaz.Validation.FlatMap._
import scalaz._, Scalaz._
import akka.actor._
import almhirt.common._
import almhirt.context.AlmhirtContext
import almhirt.akkax._
import almhirt.akkax.reporting._
import almhirt.akkax.reporting.Implicits._

object EventPublisherHub {
  def propsRaw(initialPublisherFactories: List[ComponentFactory], maxDispatchTime: FiniteDuration, buffersize: Int)(implicit almhirtContext: AlmhirtContext): Props = {
    Props(new EventPublisherHubActor(initialPublisherFactories, maxDispatchTime, buffersize))
  }

  def props(initialPublisherFactories: List[ComponentFactory])(implicit almhirtContext: AlmhirtContext): AlmValidation[Props] = {
    import almhirt.configuration._
    import almhirt.almvalidation.kit._
    for {
      section ← almhirtContext.config.v[com.typesafe.config.Config]("almhirt.components.misc.event-publisher-hub")
      buffersize ← section.v[Int]("buffer-size").constrained(_ >= 0, n ⇒ s""""buffer-size" must be greater or equal than 0, not $n.""")
      maxDispatchTime ← section.v[FiniteDuration]("max-dispatch-time")
      factoryNamesOpt ← section.magicOption[List[String]]("publisher-factories")
      additionalFactories ← {
        val effList: List[String] = (factoryNamesOpt getOrElse Nil)
        val allFactoriesClassesV: AlmValidation[List[ComponentFactory]] = effList.map(className ⇒ createFactory(className).toAgg).sequence
        allFactoriesClassesV
      }
    } yield propsRaw(initialPublisherFactories ++ additionalFactories, maxDispatchTime, buffersize)
  }

  private def createFactory(className: String)(implicit almhirtContext: AlmhirtContext): AlmValidation[ComponentFactory] = {
    unsafe {
      val clazz = Class.forName(className)
      val ctor = clazz.getConstructors()(0)
      val instance = ctor.newInstance(Array.empty).asInstanceOf[EventPublisherFactory]
      instance.create
    }
  }

  def componentFactory(initialPublisherFactories: List[ComponentFactory])(implicit almhirtContext: AlmhirtContext): AlmValidation[ComponentFactory] = {
    props(initialPublisherFactories).map(ComponentFactory(_, actorname))
  }

  final case class CreatePublisher(factory: ComponentFactory)
  final case class AddPublisher(publisher: ActorRef)

  val actorname = "event-publisher-hub"
}

private[components] class EventPublisherHubActor(
    initialPublisherFactories: List[ComponentFactory],
    maxDispatchTime: FiniteDuration,
    buffersize: Int)(implicit override val almhirtContext: AlmhirtContext) extends AlmActor with AlmActorLogging with ActorLogging with ControllableActor with StatusReportingActor {
  implicit val executor = almhirtContext.futuresContext
  override val componentControl = LocalComponentControl(self, ActorMessages.ComponentControlActions.none, Some(logWarning))
  override val statusReportsCollector = Some(StatusReportsCollector(this.context))

  private case class QueueEntry(event: Event, receiver: Option[ActorRef])
  private case object DispatchOne
  private case object DispatchNext
  private case class DispatchTimedOut(id: EventId)

  private var publishers: Set[ActorRef] = Set.empty
  private val queue = scala.collection.mutable.Queue[QueueEntry]()

  var numEventsNotDispatched = 0L
  var numEventsDispatched = 0L

  def receiveIdle(): Receive = running() {
    reportsStatus(onReportRequested = createStatusReport) {
      case EventPublisher.FireEvent(event) ⇒
        if (queue.size == buffersize) {
          numEventsNotDispatched = numEventsNotDispatched + 1L
          logWarning(s"Buffer is full($buffersize). Skipping event.")
        } else {
          queue.enqueue(QueueEntry(event, None))
          context.become(receiveDispatching())
          self ! DispatchNext
        }

      case EventPublisher.PublishEvent(event) ⇒
        if (queue.size == buffersize) {
          numEventsNotDispatched = numEventsNotDispatched + 1L
          logWarning(s"Buffer is full($buffersize). Skipping event.")
          sender() ! EventPublisher.EventNotPublished(event, ServiceBusyProblem((s"Buffer is full($buffersize)")))
        } else {
          queue.enqueue(QueueEntry(event, Some(sender())))
          context.become(receiveDispatching())
          self ! DispatchNext
        }

      case EventPublisherHub.CreatePublisher(factory) ⇒
        val effectiveName = factory.name match {
          case None       ⇒ s"no-name-publisher-${almhirtContext.getUniqueString()}"
          case Some(name) ⇒ name
        }
        val effectiveFactory = factory.copy(name = Some(effectiveName))
        context.childFrom(effectiveFactory).fold(
          fail ⇒ {
            reportMajorFailure(fail)
          },
          actor ⇒ {
            logInfo(s"Add publisher $effectiveName")
            self ! EventPublisherHub.AddPublisher(actor)
          })

      case EventPublisherHub.AddPublisher(publisher) ⇒
        if (publishers(publisher)) {
          logWarning(s"A publisher with name ${publisher.path.toStringWithoutAddress} already exists")
        } else {
          publishers = publishers + publisher
        }
    }
  }

  private def receiveDispatching(): Receive = running() {
    reportsStatus(onReportRequested = createStatusReport) {
      case DispatchNext ⇒
        if (queue.isEmpty) {
          context.become(receiveIdle())
        } else {
          val next = queue.dequeue
          context.become(receiveDispatchOne(next))
          self ! DispatchOne
        }

      case EventPublisher.FireEvent(event) ⇒
        if (queue.size == buffersize) {
          numEventsNotDispatched = numEventsNotDispatched + 1L
          logWarning(s"Buffer is full($buffersize). Skipping event.")
        } else {
          queue.enqueue(QueueEntry(event, None))
          context.become(receiveDispatching())
        }

      case EventPublisher.PublishEvent(event) ⇒
        if (queue.size == buffersize) {
          numEventsNotDispatched = numEventsNotDispatched + 1L
          logWarning(s"Buffer is full($buffersize). Skipping event.")
          sender() ! EventPublisher.EventNotPublished(event, ServiceBusyProblem((s"Buffer is full($buffersize)")))
        } else {
          queue.enqueue(QueueEntry(event, Some(sender())))
          context.become(receiveDispatching())
        }

      case EventPublisherHub.CreatePublisher(factory) ⇒
        val effectiveName = factory.name match {
          case None       ⇒ s"no-name-publisher-${almhirtContext.getUniqueString()}"
          case Some(name) ⇒ name
        }
        val effectiveFactory = factory.copy(name = Some(effectiveName))
        context.childFrom(effectiveFactory).fold(
          fail ⇒ {
            reportMajorFailure(fail)
          },
          actor ⇒ {
            logInfo(s"Add publisher $effectiveName")
            self ! EventPublisherHub.AddPublisher(actor)
          })

      case EventPublisherHub.AddPublisher(publisher) ⇒
        if (publishers(publisher)) {
          logWarning(s"A publisher with name ${publisher.path.toStringWithoutAddress} already exists")
        } else {
          publishers = publishers + publisher
        }
    }
  }

  private var waitingFor: Set[ActorRef] = Set.empty
  private var cancelCurrent: Cancellable = null
  private var errorOccured = false
  private def receiveDispatchOne(entry: QueueEntry): Receive = running() {
    reportsStatus(onReportRequested = createStatusReport) {
      case DispatchOne ⇒
        waitingFor = publishers
        if (waitingFor.nonEmpty) {
          errorOccured = false
          waitingFor.foreach { _ ! EventPublisher.PublishEvent(entry.event) }
          cancelCurrent = context.system.scheduler.scheduleOnce(maxDispatchTime, self, DispatchTimedOut(entry.event.eventId))
        } else {
          entry.receiver.foreach { receiver ⇒ receiver ! EventPublisher.EventPublished(entry.event) }
          context.become(receiveDispatching())
          self ! DispatchNext
        }

      case EventPublisher.EventPublished(event) ⇒
        numEventsDispatched = numEventsDispatched + 1L
        if (event.eventId == entry.event.eventId) {
          waitingFor = waitingFor - sender()
          if (waitingFor.isEmpty) {
            cancelCurrent.cancel()
            context.become(receiveDispatching())
            self ! DispatchNext
            if (errorOccured) {
              entry.receiver.foreach { receiver ⇒ receiver ! EventPublisher.EventNotPublished(entry.event, UnspecifiedProblem(s"The event was not published to all publishers.")) }
            } else {
              entry.receiver.foreach { receiver ⇒ receiver ! EventPublisher.EventPublished(entry.event) }
            }
          }
        } else {
          logWarning(s"Received 'EventPublished' for event ${event.eventId.value} which is not the current event.")
        }

      case EventPublisher.EventNotPublished(event, cause) ⇒
        numEventsNotDispatched = numEventsNotDispatched + 1L
        if (event.eventId == entry.event.eventId) {
          waitingFor = waitingFor - sender()
          errorOccured = true
          if (waitingFor.isEmpty) {
            cancelCurrent.cancel()
            context.become(receiveDispatching())
            self ! DispatchNext
            entry.receiver.foreach { receiver ⇒ receiver ! EventPublisher.EventNotPublished(entry.event, UnspecifiedProblem(s"The event was not published to all publishers.")) }
          }
        } else {
          logWarning(s"Received 'EventNotPublished' for event ${event.eventId.value} which is not the current event.")
        }

      case DispatchTimedOut(eventId) ⇒
        if (eventId == entry.event.eventId) {
          cancelCurrent.cancel()
          entry.receiver.foreach { receiver ⇒ receiver ! EventPublisher.EventNotPublished(entry.event, OperationTimedOutProblem(s"The event was not published to all publishers due to a timeout.")) }
          context.become(receiveDispatching())
          self ! DispatchNext
        } else {
          logWarning(s"Received 'DispatchTimedOut' for event ${eventId.value} which is not the current event.")
        }

      case EventPublisher.FireEvent(event) ⇒
        if (queue.size == buffersize) {
          numEventsNotDispatched = numEventsNotDispatched + 1L
          logWarning(s"Buffer is full($buffersize). Skipping event.")
        } else {
          queue.enqueue(QueueEntry(event, None))
          context.become(receiveDispatchOne(entry))
        }

      case EventPublisher.PublishEvent(event) ⇒
        if (queue.size == buffersize) {
          numEventsNotDispatched = numEventsNotDispatched + 1L
          logWarning(s"Buffer is full($buffersize). Skipping event.")
          sender() ! EventPublisher.EventNotPublished(event, ServiceBusyProblem((s"Buffer is full($buffersize)")))
        } else {
          queue.enqueue(QueueEntry(event, Some(sender())))
          context.become(receiveDispatchOne(entry))
        }

      case EventPublisherHub.CreatePublisher(factory) ⇒
        val effectiveName = factory.name match {
          case None       ⇒ s"no-name-publisher-${almhirtContext.getUniqueString()}"
          case Some(name) ⇒ name
        }
        val effectiveFactory = factory.copy(name = Some(effectiveName))
        context.childFrom(effectiveFactory).fold(
          fail ⇒ {
            reportMajorFailure(fail)
          },
          actor ⇒ {
            logInfo(s"Add publisher $effectiveName")
            self ! EventPublisherHub.AddPublisher(actor)
          })

      case EventPublisherHub.AddPublisher(publisher) ⇒
        if (publishers(publisher)) {
          logWarning(s"A publisher with name ${publisher.path.toStringWithoutAddress} already exists")
        } else {
          publishers = publishers + publisher
        }

    }
  }

  override val receive: Receive = receiveIdle

  private def createStatusReport(options: StatusReportOptions): AlmValidation[StatusReport] = {
    val configRep: StatusReport =
      StatusReport() addMany (
        "max-dispatch-time" -> maxDispatchTime,
        "buffer-size" -> buffersize)
    val baseReport = StatusReport("EventPublisherHub-Report").withComponentState(componentState) addMany (
      "number-of-publishers" -> publishers.size,
      "number-of-events-dispatched" -> numEventsDispatched,
      "number-of-events-not-dispatched" -> numEventsNotDispatched,
      "queue-size" -> queue.size,
      "config" -> configRep)

    scalaz.Success(baseReport)
  }

  override def preStart() {
    super.preStart()
    registerComponentControl()
    registerStatusReporter(description = Some("Things that just publish events(not from the stream)..."))
    context.parent ! ActorMessages.ConsiderMeForReporting
    initialPublisherFactories.foreach { factory ⇒ self ! EventPublisherHub.CreatePublisher(factory) }
  }

  override def postStop() {
    super.postStop()
    deregisterComponentControl()
    deregisterStatusReporter()
  }

}