package almhirt.components

import akka.actor._
import almhirt.commanding.ExecutionStateChanged

class ExecutionStateTrackerRouter(numChildren: Int, childProps: Props) extends Actor {
  import ExecutionStateTracker._
  val children = (for (i <- 0 until (numChildren)) yield context.actorOf(childProps)).toVector

  private def dispatch(trackId: String, message: Any) {
    val target = Math.abs(trackId.hashCode()) % numChildren
    children(target) forward message
  }

  override def receive: Receive = {
    case m: ExecutionStateChanged => dispatch(m.executionState.trackId , m)
    case m: GetExecutionStateFor => dispatch(m.trackId, m)
    case m: SubscribeForFinishedState => dispatch(m.trackId, m)
    case m: UnsubscribeForFinishedState => dispatch(m.trackId, m)
  }
}