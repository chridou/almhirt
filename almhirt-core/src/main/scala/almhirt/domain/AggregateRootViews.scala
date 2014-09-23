package almhirt.domain

import scala.language.postfixOps
import scala.concurrent.duration._
import scalaz._, Scalaz._
import akka.actor._
import almhirt.common._
import almhirt.aggregates._
import almhirt.almvalidation.kit._
import akka.stream.actor._
import org.reactivestreams.Publisher

object AggregateRootViews {
  def props[E <: AggregateRootEvent](getViewProps: AggregateRootId ⇒ Props, eventBufferSize: Int)(implicit eventTag: scala.reflect.ClassTag[E]): Props =
    Props(new AggregateRootViews[E](getViewProps, eventBufferSize))

  import akka.stream.scaladsl2._
  def subscribeTo[E <: Event](
    publisher: Publisher[Event],
    views: ActorRef)(implicit mat: FlowMaterializer, tag: scala.reflect.ClassTag[E]) {
    FlowFrom(publisher).filter(p ⇒ tag.runtimeClass.isInstance(p)).map(_.asInstanceOf[E]).publishTo(ActorSubscriber[E](views))
  }

  def connectedActor[E <: Event](publisher: Publisher[Event])(getViewProps: AggregateRootId ⇒ Props, eventBufferSize: Int, name: String)(
    implicit actorRefFactory: ActorRefFactory,
    mat: FlowMaterializer,
    tag: scala.reflect.ClassTag[E]): ActorRef = {
    val props = AggregateRootViews.props(getViewProps, eventBufferSize)
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
  override val getViewProps: AggregateRootId ⇒ Props,
  override val eventBufferSize: Int)(implicit override val eventTag: scala.reflect.ClassTag[E]) extends AggregateRootViewsSkeleton[E]

private[almhirt] trait AggregateRootViewsSkeleton[E <: AggregateRootEvent] extends ActorSubscriber with ActorLogging {
  import AggregateRootViewMessages._

  import akka.actor.OneForOneStrategy
  import akka.actor.SupervisorStrategy._
  override val supervisorStrategy =
    OneForOneStrategy(maxNrOfRetries = 10, withinTimeRange = 1 minute) {
      case _: Exception ⇒ Restart
    }

  def getViewProps: AggregateRootId ⇒ Props
  implicit def eventTag: scala.reflect.ClassTag[E]
  def eventBufferSize: Int

  log.error(s"${eventTag.runtimeClass}")

  final override val requestStrategy = ZeroRequestStrategy

  private def receiveRunning: Receive = {
    case GetAggregateRootProjectionFor(id) ⇒
      val view = context.child(id.value) match {
        case Some(v) ⇒
          v
        case None ⇒
          val props = getViewProps(id)
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

  def receive: Receive = receiveRunning

  override def preStart() {
    request(eventBufferSize)
  }
}