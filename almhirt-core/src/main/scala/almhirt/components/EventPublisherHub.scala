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
      if (ctor.getParameterCount != 0)
        throw new IllegalArgumentException(s"Expected a no arg constructor. The constructor required ${ctor.getParameterCount} arguments. The class name was $className.")
      val instance = ctor.newInstance().asInstanceOf[EventPublisherFactory]
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
  override val componentControl = LocalComponentControl(self, ComponentControlActions.none, Some(logWarning))
  override val statusReportsCollector = Some(StatusReportsCollector(this.context))

  private case class QueueEntry(event: Event, receiver: Option[ActorRef])
  private case object DispatchOne
  private case object DispatchNext
  private case class DispatchTimedOut(id: EventId)

  private var publishers: Set[ActorRef] = Set.empty
  private val queue = scala.collection.mutable.Queue[QueueEntry]()

  var numEventsReceived = 0L
  var numEventsNotDispatched = 0L
  var numEventsDispatched = 0L

  val eventsByType = new scala.collection.mutable.HashMap[Class[_], Long]()
  val eventsByComponent = new scala.collection.mutable.HashMap[String, Long]()

  private def countReceivedEvent(event: Event): Unit = {
    numEventsReceived = numEventsReceived + 1L

    val eventType = event.getClass()
    eventsByType get eventType match {
      case None        ⇒ eventsByType += (eventType -> 1L)
      case Some(count) ⇒ eventsByType.update(eventType, count + 1L)
    }

    event match {
      case e: almhirt.akkax.events.ComponentEvent ⇒
        val cidString = e.origin.toPathString
        eventsByComponent get cidString match {
          case None        ⇒ eventsByComponent += (cidString -> 1L)
          case Some(count) ⇒ eventsByComponent.update(cidString, count + 1L)
        }
      case _ ⇒
        ()
    }
  }

  def receiveIdle(): Receive = running() {
    reportsStatusF(onReportRequested = createStatusReport) {
      case EventPublisher.FireEvent(event) ⇒
        countReceivedEvent(event)
        if (queue.size == buffersize) {
          numEventsNotDispatched = numEventsNotDispatched + 1L
          logWarning(s"Buffer is full($buffersize). Skipping event.")
        } else {
          queue.enqueue(QueueEntry(event, None))
          context.become(receiveDispatching())
          self ! DispatchNext
        }

      case EventPublisher.PublishEvent(event) ⇒
        countReceivedEvent(event)
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
    reportsStatusF(onReportRequested = createStatusReport) {
      case DispatchNext ⇒
        if (queue.isEmpty) {
          context.become(receiveIdle())
        } else {
          val next = queue.dequeue
          context.become(receiveDispatchOne(next))
          self ! DispatchOne
        }

      case EventPublisher.FireEvent(event) ⇒
        countReceivedEvent(event)
        if (queue.size == buffersize) {
          numEventsNotDispatched = numEventsNotDispatched + 1L
          logWarning(s"Buffer is full($buffersize). Skipping event.")
        } else {
          queue.enqueue(QueueEntry(event, None))
          context.become(receiveDispatching())
        }

      case EventPublisher.PublishEvent(event) ⇒
        countReceivedEvent(event)
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
    reportsStatusF(onReportRequested = createStatusReport) {
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
        logWarning(s"Event ${event.getClass.getName}(id=${event.eventId.value}) was not published by ${sender().path.name}:\n$cause")
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
        countReceivedEvent(event)
        if (queue.size == buffersize) {
          numEventsNotDispatched = numEventsNotDispatched + 1L
          logWarning(s"Buffer is full($buffersize). Skipping event.")
        } else {
          queue.enqueue(QueueEntry(event, None))
          context.become(receiveDispatchOne(entry))
        }

      case EventPublisher.PublishEvent(event) ⇒
        countReceivedEvent(event)
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

  private def createStatusReport(options: StatusReportOptions): AlmFuture[StatusReport] = {

    val byTypeReport = StatusReport() ~~ (
      eventsByType.toStream.map { case (clazz, count) ⇒ ezreps.toField(clazz.getName, count) }.toIterable)

    val byComponentReport = StatusReport() ~~ (
      eventsByComponent.toStream.map { ezreps.toField(_) }.toIterable)

    val eventStatsReport = StatusReport() addMany (
      "number-of-events-received" -> numEventsReceived,
      "number-of-events-received-by-type" -> byTypeReport,
      "number-of-events-received-by-component" -> byComponentReport)

    val configRep: StatusReport =
      StatusReport() addMany (
        "max-dispatch-time" -> maxDispatchTime,
        "buffer-size" -> buffersize)
        
    val baseReport = StatusReport("EventPublisherHub-Report").withComponentState(componentState) addMany (
      "number-of-publishers" -> publishers.size,
      "number-of-events-dispatched" -> numEventsDispatched,
      "number-of-events-not-dispatched" -> numEventsNotDispatched,
      "queue-size" -> queue.size,
      "received-events-stats" -> eventStatsReport,
      "config" -> configRep)

    appendToReportFromCollector(baseReport)(options)
  }

  override def preStart() {
    super.preStart()
    logInfo(s"Start with ${initialPublisherFactories.size} publishers to create.")
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