package almhirt.util

import language.implicitConversions

import java.util.{ UUID => JUUID }
import almhirt.common._

/**
 * If the receiver accepts this, tell her how she should treat the result.
 */
sealed trait ExecutionStyle

object ExecutionStyle {
  def apply(): ExecutionStyle = FireAndForget
  def apply(uuid: JUUID): ExecutionStyle = Correlated(uuid)
  def apply(ticket: TrackingTicket): ExecutionStyle = Tracked(ticket)
  def correlated()(implicit hasUuids: CanCreateUuid): ExecutionStyle = apply(hasUuids.getUuid)
  def tracked()(implicit hasUuids: CanCreateUuid): ExecutionStyle = apply(TrackingTicket(hasUuids.getUuid))

  implicit def fromTicketOption2ExecutionStyle(maybeATicket: Option[TrackingTicket]): ExecutionStyle =
    maybeATicket match {
      case Some(t) => Tracked(t)
      case None => FireAndForget
    }

  implicit def fromTicket2ExecutionStyle(ticket: TrackingTicket): ExecutionStyle =
    apply(ticket)

  implicit def fromUUID2ExecutionStyle(correlationId: JUUID): ExecutionStyle =
    apply(correlationId)
}


/**
 * The sender himself is not interested in the result but knows that someone else is. 
 * If you are the last one in the chain of operations who can accept [[Tracked]], publish the result as a [[ResultOperationState]]
 */
final case class Tracked(ticket: TrackingTicket) extends ExecutionStyle

/**
 * The receiver can execute your command in a fire and forget manner or send you a result with a correlationId
 */
sealed trait UntrackedExecutionStyle extends ExecutionStyle

/**
 * The sender signals, that he is not interested in any result and knows that no one else is. Just do it! If you fail... well, maybe log it..
 */
case object FireAndForget extends UntrackedExecutionStyle

/**
 * I need a result.
 */
trait NeedResponseExectionStyle extends UntrackedExecutionStyle

object NeedResponseExectionStyle {
  def unapply(style: NeedResponseExectionStyle): Option[Option[JUUID]] =
    style match {
    case Uncorrelated => Some(None)
    case Correlated(id) => Some(Some(id)) 
  }
    
}

/**
 * The sender signals, that he is interested in the result. Send it back.
 */
case object Uncorrelated extends NeedResponseExectionStyle

/**
 * The sender signals, that he is interested in the result. Send it back with the given correlation id.
 */
final case class Correlated(correlationId: JUUID) extends NeedResponseExectionStyle


