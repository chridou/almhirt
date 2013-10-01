package almhirt.domain

import java.util.{ UUID => JUUID }
import akka.actor._

class AggregateRootRepositoryRouter(numChildren: Int, childProps: Props) extends Actor {
  import DomainMessages._
  val children = (for (i <- 0 until (numChildren)) yield context.actorOf(childProps)).toVector

//  override def supervisorStrategy = OneForOneStrategy() {
//    case _: CriticalAggregateRootCellSourceException => Escalate
//    case _ => Restart
//  }

  private def dispatch(aggId: JUUID, message: Any) {
    val target = Math.abs(aggId.hashCode()) % numChildren
    children(target) forward message
  }

  override def receive: Receive = {
    case m: GetAggregateRoot =>
      dispatch(m.arId, m)
    case m: UpdateAggregateRoot =>
      dispatch(m.newState.id, m)
  }
}