package almhirt.components

import java.util.{ UUID => JUUID }
import akka.actor._
import akka.actor.SupervisorStrategy._
import almhirt.core.types._
import almhirt.base._

class AggregateRootCellSourceRouter(numChildren: Int, childProps: Props) extends SelectorBasedRouter(numChildren,childProps) with UuidBasedRouter with Actor {
  import AggregateRootCellSource._

  override def supervisorStrategy = OneForOneStrategy() {
    case _: CriticalAggregateRootCellSourceException => Escalate
    case _ => Restart
  }

  override def receive: Receive = {
    case m: GetCell =>
      dispatch(m.arId, m)
    case GetStats =>
      children.foreach(_ ! GetStats)
  }
}