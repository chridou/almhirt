package almhirt.domain

import scala.language.postfixOps

import scala.concurrent.duration._
import scalaz._, Scalaz._
import akka.actor._
import almhirt.common._
import almhirt.aggregates._
import almhirt.almvalidation.kit._
import akka.stream.actor._

/**
 * Manages views on an aggregate root, where there is one actor view on a single aggregate root.
 *  The views receive an [[AggregateRootViewMessages.ApplyAggregateEvent]] message on an aggregate event for the aggregate root and must deliver their
 *  view when receiving [[AggregateRootViewMessages.GetAggregateRootProjection]].
 *
 *  On [[AggregateRootViewMessages.ApplyAggregateEvent]] an [[AggregateRootViewMessages.AggregateEventHandled]] is expected.
 *  On [[AggregateRootViewMessages.GetAggregateRootProjection]] the view must send a custom result to the sender of [[GetAggregateRootProjection]].
 *
 *  The Actor is an ActorSubscriber that requests [[AggregateRootViewMessages.AggregateRootEvents]].
 */
private[almhirt] trait AggregateRootViewsSkeleton[T, E <: AggregateRootEvent] extends ActorSubscriber with ActorLogging {
  import AggregateRootViewMessages._

  import akka.actor.OneForOneStrategy
  import akka.actor.SupervisorStrategy._
  override val supervisorStrategy =
    OneForOneStrategy(maxNrOfRetries = 10, withinTimeRange = 1 minute) {
      case _: Exception ⇒ Restart
    }

  def getViewProps: AggregateRootId => Props
  implicit def eventTag: scala.reflect.ClassTag[E]
  def eventBufferSize: Int

  final override val requestStrategy = ZeroRequestStrategy

  private def receiveRunning: Receive = {
    case GetAggregateRootProjectionFor(id) =>
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

    case ActorSubscriberMessage.OnNext(event: AggregateRootEvent) =>
      event.castTo[E].fold(
        fail => {
          // This can happen quite often...
          if (log.isDebugEnabled)
            log.debug(s"Received unproccessable aggregate event:\n$fail")
          request(1)
        },
        aggregateEvent => {
          context.child(aggregateEvent.aggId.value) match {
            case Some(v) ⇒
              v ! ApplyAggregateEvent(aggregateEvent)
            case None ⇒
              request(1)
          }
        })

    case AggregateEventHandled =>
      request(1)

    case ActorSubscriberMessage.OnNext(x) =>
      log.warning(s"""Received unproccessable message from publisher: ${x.getClass.getName()}".""")
      request(1)

    case ActorSubscriberMessage.OnError(ex) =>
      throw new Exception(s"""I("$self.path") received an error via the stream.""", ex)

    case ActorSubscriberMessage.OnComplete =>
      context.stop(self)

  }

  def receive: Receive = receiveRunning

  override def preStart() {
    request(eventBufferSize)
  }
}