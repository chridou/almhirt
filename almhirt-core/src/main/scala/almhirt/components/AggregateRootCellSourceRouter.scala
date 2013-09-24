package almhirt.components

import java.util.{ UUID => JUUID }
import akka.actor._
import akka.actor._
import akka.actor.SupervisorStrategy._
import almhirt.commanding.ExecutionStateChanged

class AggregateRootCellSourceRouter(numChildren: Int, childProps: Props) extends Actor {
  import AggregateRootCellSource._
  val children = (for (i <- 0 until (numChildren)) yield context.actorOf(childProps)).toVector

  override def supervisorStrategy = OneForOneStrategy() {
    case _: CriticalAggregateRootCellSourceException => Escalate
    case _ => Restart
  }

  private def dispatch(aggId: JUUID, message: Any) {
    val target = Math.abs(aggId.hashCode()) % numChildren
    children(target) forward message
  }

  override def receive: Receive = {
    case m: GetCell =>
      dispatch(m.arId, m)
    case GetStats =>
      children.foreach(_ ! GetStats)
  }
}