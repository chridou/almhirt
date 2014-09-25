package almhirt.domain

import scala.language.postfixOps
import scala.reflect.ClassTag
import scala.concurrent.duration._
import scalaz._, Scalaz._
import akka.actor._
import almhirt.common._
import almhirt.aggregates._
import almhirt.almvalidation.kit._
import almhirt.akkax._
import akka.stream.actor._
import org.reactivestreams.Publisher

object AggregateRootViews {
  def propsRaw[E <: AggregateRootEvent: ClassTag](
    getViewProps: (AggregateRootId, ActorRef, Option[ActorRef]) ⇒ Props,
    aggregateEventLogToResolve: ToResolve,
    snapShotStorageToResolve: Option[ToResolve],
    resolveSettings: ResolveSettings,
    eventBufferSize: Int): Props =
    Props(new AggregateRootViews[E](getViewProps, aggregateEventLogToResolve, snapShotStorageToResolve, resolveSettings, eventBufferSize))

  def props[E <: AggregateRootEvent: ClassTag](
    config: com.typesafe.config.Config,
    getViewProps: (AggregateRootId, ActorRef, Option[ActorRef]) ⇒ Props,
    viewsConfigName: Option[String] = None): AlmValidation[Props] = {
    import almhirt.configuration._
    import almhirt.almvalidation.kit._
    val path = "almhirt.components.aggregate-root-views" + viewsConfigName.map("." + _).getOrElse("")
    for {
      section <- config.v[com.typesafe.config.Config](path)
      eventBufferSize <- section.v[Int]("event-buffer-size")
    } yield ??? //propsRaw(getViewProps, eventBufferSize)

  }

  import akka.stream.scaladsl2._
  def subscribeTo[E <: Event](
    publisher: Publisher[Event],
    views: ActorRef)(implicit mat: FlowMaterializer, tag: scala.reflect.ClassTag[E]) {
    FlowFrom(publisher).filter(p ⇒ tag.runtimeClass.isInstance(p)).map(_.asInstanceOf[E]).publishTo(ActorSubscriber[E](views))
  }

  def connectedActor[E <: Event](publisher: Publisher[Event])(
    getViewProps: (AggregateRootId, ActorRef, Option[ActorRef]) ⇒ Props,
    aggregateEventLogToResolve: ToResolve,
    snapShotStorageToResolve: Option[ToResolve],
    resolveSettings: ResolveSettings,
    eventBufferSize: Int,
    name: String)(
      implicit actorRefFactory: ActorRefFactory,
      mat: FlowMaterializer,
      tag: scala.reflect.ClassTag[E]): ActorRef = {
    val props = AggregateRootViews.propsRaw(getViewProps, aggregateEventLogToResolve, snapShotStorageToResolve, resolveSettings, eventBufferSize)
    val views = actorRefFactory.actorOf(props, name)
    subscribeTo[E](publisher, views)
    views
  }
}

/**
 * Manages views on an aggregate root, where there is one actor view on a single aggregate root.
 *  The views receive an [[AggregateRootViewMessages.ApplyAggregateRootEvent]] message on an aggregate event for the aggregate root and must deliver their
 *  view when receiving [[AggregateRootViewMessages.GetAggregateRootProjection]].
 *
 *  On [[AggregateRootViewMessages.ApplyAggregateRootEvent]] an [[AggregateRootViewMessages.AggregateRootEventHandled]] is expected.
 *  On [[AggregateRootViewMessages.GetAggregateRootProjection]] the view must send a custom result to the sender of [[GetAggregateRootProjection]].
 *
 *  The Actor is an ActorSubscriber that requests [[AggregateRootEvents]].
 */
class AggregateRootViews[E <: AggregateRootEvent](
  override val getViewProps: (AggregateRootId, ActorRef, Option[ActorRef]) ⇒ Props,
  override val aggregateEventLogToResolve: ToResolve,
  override val snapShotStorageToResolve: Option[ToResolve],
  override val resolveSettings: ResolveSettings,
  override val eventBufferSize: Int)(implicit override val eventTag: scala.reflect.ClassTag[E]) extends AggregateRootViewsSkeleton[E]

private[almhirt] trait AggregateRootViewsSkeleton[E <: AggregateRootEvent] extends ActorSubscriber with ActorLogging {
  import AggregateRootViewMessages._

  import akka.actor.OneForOneStrategy
  import akka.actor.SupervisorStrategy._
  override val supervisorStrategy =
    OneForOneStrategy(maxNrOfRetries = 10, withinTimeRange = 1 minute) {
      case _: Exception ⇒ Restart
    }

  def getViewProps: (AggregateRootId, ActorRef, Option[ActorRef]) ⇒ Props

  def aggregateEventLogToResolve: ToResolve
  def snapShotStorageToResolve: Option[ToResolve]
  def resolveSettings: ResolveSettings

  implicit def eventTag: scala.reflect.ClassTag[E]
  def eventBufferSize: Int

  final override val requestStrategy = ZeroRequestStrategy

  private case object Resolve
  def receiveResolve: Receive = {
    case Resolve =>
      val actorsToResolve =
        Map("aggregateeventlog" -> aggregateEventLogToResolve) ++
          snapShotStorageToResolve.map(r => Map("snapshotstorage" -> r)).getOrElse(Map.empty)
      context.resolveMany(actorsToResolve, resolveSettings, None, Some("resolver"))

    case ActorMessages.ManyResolved(dependencies, _) =>
      log.info("Found dependencies.")

      request(eventBufferSize)
      context.become(receiveRunning(dependencies("aggregateeventlog"), dependencies.get("snapshotstorage")))

    case ActorMessages.ManyNotResolved(problem, _) =>
      log.error(s"Failed to resolve dependencies:\n$problem")
      sys.error(s"Failed to resolve dependencies.")
  }

  private def receiveRunning(aggregateEventLog: ActorRef, snapshotStorage: Option[ActorRef]): Receive = {
    case GetAggregateRootProjectionFor(id) ⇒
      val view = context.child(id.value) match {
        case Some(v) ⇒
          v
        case None ⇒
          val props = getViewProps(id, aggregateEventLog, snapshotStorage)
          val actor = context.actorOf(props, id.value)
          //context watch actor
          actor
      }
      view forward GetAggregateRootProjection

    case ActorSubscriberMessage.OnNext(event: AggregateRootEvent) ⇒
      event.castTo[E].fold(
        fail ⇒ {
          // This can happen quite often depending on the producer ...
          if (log.isWarningEnabled)
            log.warning(s"Received unproccessable aggregate event:\n$fail")
          request(1)
        },
        aggregateEvent ⇒ {
          context.child(aggregateEvent.aggId.value) match {
            case Some(v) ⇒
              v ! ApplyAggregateRootEvent(aggregateEvent)
            case None ⇒
              request(1)
          }
        })

    case AggregateRootEventHandled ⇒
      request(1)

    case ActorSubscriberMessage.OnNext(x) ⇒
      log.warning(s"""Received unproccessable message from publisher: ${x.getClass.getName()}".""")
      request(1)

    case ActorSubscriberMessage.OnError(ex) ⇒
      throw new Exception(s"""I("$self.path") received an error via the stream.""", ex)

    case ActorSubscriberMessage.OnComplete ⇒
      context.stop(self)

  }

  def receive: Receive = receiveResolve

  override def preStart() {
    self ! Resolve
  }
}