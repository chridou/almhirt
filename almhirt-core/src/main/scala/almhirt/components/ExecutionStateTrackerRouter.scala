package almhirt.components

import akka.actor._
import almhirt.commanding.ExecutionStateChanged
import almhirt.base._

class ExecutionStateTrackerRouter(numChildren: Int, childProps: Props) extends SelectorBasedRouter(numChildren,childProps) with StringBasedRouter with Actor {
  import ExecutionStateTracker._

  override def receive: Receive = {
    case m: ExecutionStateChanged => dispatch(m.executionState.trackId , m)
    case m: GetExecutionStateFor => dispatch(m.trackId, m)
    case m: SubscribeForFinishedState => dispatch(m.trackId, m)
    case m: UnsubscribeForFinishedState => dispatch(m.trackId, m)
  }
}