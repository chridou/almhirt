package almhirt.domain.impl

import scala.reflect.ClassTag
import scalaz._, Scalaz._
import akka.actor._
import akka.pattern._
import akka.util.Timeout._
import almhirt.common._
import almhirt.core._
import almhirt.syntax.almfuture._
import almhirt.almvalidation.kit._
import almhirt.domain._
import almhirt.eventlog._
import almhirt.util._

abstract class UnsafeAggregateRootRepositoryActor[AR <: AggregateRoot[AR, Event], Event <: DomainEvent](eventLog: ActorRef, arFactory: CanCreateAggragateRoot[AR, Event])(implicit theAlmhirt: Almhirt, tagAr: ClassTag[AR], tagEvent: ClassTag[Event]) extends Actor {
  private val validator = new CanValidateAggregateRootsAgainstEvents[AR, Event] {}
  implicit private def timeout = theAlmhirt.durations.longDuration

  private def getFromEventLog(id: java.util.UUID): AlmFuture[AR] = {
    val future = (eventLog ? GetDomainEventsQry(id, None, None))(timeout).mapToSuccessfulAlmFuture[DomainEventsForAggregateRootRsp]
    future
      .mapV { case DomainEventsForAggregateRootRsp(id, DomainEventsChunk(idx, isLast, events), corrId) => events }
      .map(events => events.map(x => x.asInstanceOf[Event]))
      .mapV(events =>
        if (events.isEmpty) NotFoundProblem("No aggregate root found with id '%s'".format(id)).failure
        else arFactory.rebuildFromHistory(events))
  }

  private def storeToEventLog(ar: AR, uncommittedEvents: IndexedSeq[Event], ticket: Option[TrackingTicket]) {
    (for {
      validated <- getFromEventLog(ar.id).foldV(
        fail => fail match {
          case p: NotFoundProblem => 0L.success
          case _ => fail.failure
        },
        succ => succ.version.success).map(reqVersion =>
          validator.validateAggregateRootAgainstEvents(ar, uncommittedEvents, reqVersion))
      committedEventsRsp <- (eventLog ? LogDomainEventsQry(uncommittedEvents, None))(timeout).mapToSuccessfulAlmFuture[LoggedDomainEventsRsp]
    } yield committedEventsRsp).onComplete(
      fail =>
        updateFailedOperationState(fail, ticket),
      {
        case LoggedDomainEventsRsp(committedEvents, Some((cause, failedEvents)), _) =>
          val prob = PersistenceProblem(s"${failedEvents.length} domain events out of ${uncommittedEvents.length} couldn't be stored. ${committedEvents.length} domain events have been stored.", cause = Some(cause))
          updateFailedOperationState(prob, ticket)
        case LoggedDomainEventsRsp(committedEvents, None, _) =>
          val action: PerformedAction =
            if (committedEvents.isEmpty) PerformedNoAction("No events have been stored")
            else if (committedEvents.head.isInstanceOf[CreatingNewAggregateRootEvent]) PerformedCreateAction(AggregateRootRef(ar.id, committedEvents.last.aggVersion + 1))
            else PerformedUpdateAction(AggregateRootRef(ar.id, committedEvents.last.aggVersion + 1))
          ticket.foreach(t => theAlmhirt.publishOperationState(Executed(t, action)))
          committedEvents.foreach(event => theAlmhirt.publishEvent(event))
      })
  }

  private def updateFailedOperationState(p: Problem, ticket: Option[TrackingTicket]) {
    theAlmhirt.publishProblem(p)
    ticket match {
      case Some(t) => theAlmhirt.publishOperationState(NotExecuted(t, p))
      case None => ()
    }
  }

  def receive: Receive = {
    case GetAggregateRootQry(aggId) =>
      val pinnedSender = sender
      getFromEventLog(aggId).onComplete(pinnedSender ! GetAggregateRootRsp(aggId, _))
    case StoreAggregateRootCmd(ar, uncommittedEvents, style) =>
      ar.castTo[AR].fold(
        fail => theAlmhirt.publishProblem(fail),
        typedAr =>
          style match {
            case Tracked(ticket) => storeToEventLog(typedAr, uncommittedEvents.asInstanceOf[IndexedSeq[Event]], Some(ticket))
            case FireAndForget => storeToEventLog(typedAr, uncommittedEvents.asInstanceOf[IndexedSeq[Event]], None)
            case NeedResponseExectionStyle(a) =>
              theAlmhirt.publishProblem(NotSupportedProblem(s"UnsafeAggregateRootRepositoryActor can not store an AR with a Correlated(${a.toString}) style."))
          })

  }
}