package almhirt.domain

import java.util.{ UUID => JUUID }
import akka.actor._
import almhirt.base._
import almhirt.base.SelectorBasedRouter
import almhirt.base.UuidBasedRouter

class AggregateRootRepositoryRouter(numChildren: Int, childProps: Props) extends SelectorBasedRouter(numChildren,childProps) with UuidBasedRouter with Actor {
  import DomainMessages._

//  override def supervisorStrategy = OneForOneStrategy() {
//    case _: CriticalAggregateRootCellSourceException => Escalate
//    case _ => Restart
//  }

  override def receive: Receive = {
    case m: GetAggregateRoot =>
      dispatch(m.arId, m)
    case m: UpdateAggregateRoot =>
      dispatch(m.newState.id, m)
  }
}