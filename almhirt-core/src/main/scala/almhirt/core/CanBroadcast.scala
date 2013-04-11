package almhirt.core

import almhirt.domain.DomainEvent
import almhirt.messaging.Message
import almhirt.util.OperationState
import almhirt.common._
import almhirt.commanding.CommandEnvelope

trait CanPublishMessages {
  def publish(msg: Message[AnyRef]): Unit
}

trait PostsOnMessageHub{ self: CanPublishMessages with HasMessageHub =>
  override def publish(msg: Message[AnyRef]) { messageHub.post(msg) }}

trait BroadcastsOnMessageHub{ self: CanPublishMessages with HasMessageHub =>
  override def publish(msg: Message[AnyRef]) { messageHub.broadcast(msg) }}

trait CanPublishItems{ self: CanPublishMessages with CanCreateUuidsAndDateTimes =>
  def publishEvent(event: Event, metaData: Map[String, String] = Map.empty) { 
    self.publish(self.createMessage(event, metaData)) }  
  def publishOperationState(opState: OperationState, metaData: Map[String, String] = Map.empty) { 
    self.publish(self.createMessage(opState, metaData)) }  
  def publishProblem(problem: Problem, metaData: Map[String, String] = Map.empty) { 
    self.publish(self.createMessage(ProblemEvent(problem)(self), metaData)) }  
  def publishCommandEnvelope(cmdEnv: CommandEnvelope, metaData: Map[String, String] = Map.empty) { 
    self.publish(self.createMessage(cmdEnv, metaData)) }  

}