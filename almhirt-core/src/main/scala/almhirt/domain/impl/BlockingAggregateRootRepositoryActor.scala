package almhirt.domain.impl

import scala.reflect.ClassTag
import scalaz._, Scalaz._
import akka.actor._
import akka.pattern._
import akka.util.Timeout._
import almhirt.core._
import almhirt.common._
import almhirt.almvalidation.kit._
import almhirt.almfuture.all._
import almhirt.domain._
import almhirt.eventlog._
import almhirt.util._
import almhirt.common.AlmFuture
import almhirt.core.Almhirt

abstract class BlockingAggregateRootRepositoryActor[AR <: AggregateRoot[AR, Event], Event <: DomainEvent](eventLog: ActorRef, arFactory: CanCreateAggragateRoot[AR, Event])(implicit theAlmhirt: Almhirt, tagAr: ClassTag[AR], tagEvent: ClassTag[Event]) extends Actor {
  private val validator = new CanValidateAggregateRootsAgainstEvents[AR, Event] {}
  implicit private def timeout = theAlmhirt.defaultDuration
  implicit private val executionContext = theAlmhirt.executionContext

  private def getFromEventLog(id: java.util.UUID): AlmFuture[AR] = {
    val future = (eventLog ? GetDomainEventsQry(id, None, None))(timeout).mapToSuccessfulAlmFuture[DomainEventsForAggregateRootRsp]
    future
      .mapV { case DomainEventsForAggregateRootRsp(id, DomainEventsChunk(idx, isLast, events), corrId) => events }
      .map(events => events.map(x => x.asInstanceOf[Event]))
      .mapV(events =>
        if (events.isEmpty) NotFoundProblem("No aggregate root found with id '%s'".format(id)).failure
        else arFactory.rebuildFromHistory(events))
  }

  private def storeToEventLog(ar: AR, uncommittedEvents: IndexedSeq[Event], ticket: Option[TrackingTicket]) = {
    inTryCatchM {
      (for {
        validated <- getFromEventLog(ar.id).foldV(
          fail => fail match {
            case p: NotFoundProblem => 0L.success
            case _ => fail.failure
          },
          succ => succ.version.success).map(reqVersion =>
            validator.validateAggregateRootAgainstEvents(ar, uncommittedEvents, reqVersion))
        committedEventsRsp <- (eventLog ? LogDomainEventsQry(uncommittedEvents, None))(timeout).mapToSuccessfulAlmFuture[LoggedDomainEventsRsp]
      } yield committedEventsRsp.committedEvents).andThen(
        fail =>
          updateFailedOperationState(fail, ticket),
        succ => {
          val action: PerformedAction =
            if (succ.isEmpty) PerformedNoAction("There were no events to store")
            else if (succ.head.isInstanceOf[CreatingNewAggregateRootEvent]) PerformedCreateAction(AggregateRootRef(ar.id, succ.last.aggVersion + 1))
            else PerformedUpdateAction(AggregateRootRef(ar.id, succ.last.aggVersion + 1))
          ticket.foreach(t => theAlmhirt.publishOperationState(Executed(t, action)))
          succ.foreach(event => theAlmhirt.publishDomainEvent(event))
        }).awaitResult
    }(s"Could not store ${uncommittedEvents.size} events for aggregate root ${ar.id}").onFailure(p => updateFailedOperationState(p, ticket))
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
      val res = getFromEventLog(aggId).awaitResult
      sender ! GetAggregateRootRsp(aggId, res)
    case StoreAggregateRootCmd(ar, uncommittedEvents, style) =>
      ar.castTo[AR].fold(
        fail => theAlmhirt.publishProblem(fail),
        typedAr =>
          style match {
            case Tracked(ticket) => storeToEventLog(typedAr, uncommittedEvents.asInstanceOf[IndexedSeq[Event]], Some(ticket))
            case FireAndForget => storeToEventLog(typedAr, uncommittedEvents.asInstanceOf[IndexedSeq[Event]], None)
            case Correlated(corrId) =>
              theAlmhirt.publishProblem(NotSupportedProblem(s"BlockingAggregateRootRepositoryActor can not store an AR with a Correlated($corrId) style."))
          })
  }
}