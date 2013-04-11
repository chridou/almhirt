package almhirt.core

import almhirt.common._
import almhirt.domain.DomainEvent
import almhirt.messaging.Message
import almhirt.util.OperationState
import almhirt.util.OperationStateEvent
import almhirt.commanding.CommandEnvelope

trait CanPublishMessages {
  def publish(msg: Message[AnyRef]): Unit
}

trait PostsOnMessageHub { self: CanPublishMessages with HasMessageHub =>
  override def publish(msg: Message[AnyRef]) { messageHub.post(msg) }
}

trait BroadcastsOnMessageHub { self: CanPublishMessages with HasMessageHub =>
  override def publish(msg: Message[AnyRef]) { messageHub.broadcast(msg) }
}

trait CanPublishEvents{ self: CanPublishMessages with CanCreateUuidsAndDateTimes =>
  def publishEvent(event: Event, metaData: Map[String, String] = Map.empty) {
    self.publish(self.createMessage(event, metaData))
  }
}

trait CanPublishProblems{ self: CanPublishEvents with CanPublishMessages with CanCreateUuidsAndDateTimes =>
  def publishProblem(problem: Problem, sender: Option[String] = None, metaData: Map[String, String] = Map.empty) {
    self.publishEvent(ProblemEvent(problem, sender)(self), metaData)
  }

  def publishProblemWithSender(problem: Problem, sender: String, metaData: Map[String, String] = Map.empty) {
    publishProblem(problem, Some(sender), metaData)
  }
}

trait CanPublishItems extends CanPublishEvents with CanPublishProblems { self: CanPublishMessages with CanCreateUuidsAndDateTimes =>

  def publishOperationState(opState: OperationState, sender: Option[String] = None, metaData: Map[String, String] = Map.empty) {
    self.publishEvent(OperationStateEvent(opState, sender)(self), metaData)
  }

  def publishOperationStateWithSender(opState: OperationState, sender: String, metaData: Map[String, String] = Map.empty) {
    self.publishEvent(OperationStateEvent(opState, Some(sender))(self), metaData)
  }

  def publishCommandEnvelope(cmdEnv: CommandEnvelope, metaData: Map[String, String] = Map.empty) {
    self.publish(self.createMessage(cmdEnv, metaData))
  }

}