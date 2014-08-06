package almhirt.messaging

import akka.actor._
import akka.stream.actor.ActorProducer
import almhirt.common._

trait DispatchCommandMessage
final case class DispatchCommand(event: Command) extends DispatchCommandMessage
sealed trait DispatchCommandResponse extends DispatchCommandMessage
final case class CommandDispatched(event: Command) extends DispatchCommandResponse
final case class CommandNotDispatched(event: Command) extends DispatchCommandResponse

object CommandStreamDispatcher {
  import scalaz._, Scalaz._
  import scala.concurrent.ExecutionContext
  import scala.concurrent.duration._
  import akka.pattern._
  import almhirt.almfuture.all._
}

private[messaging] class CommandStreamDispatcher(initialBufferSize: Int) extends ActorProducer[Command] with ActorLogging {
  import ActorProducer._
  
  var bufferSize: Int = initialBufferSize
  var buffered: Vector[Command] = Vector.empty
  
  override def receive: Receive = {
    case Request(amount) =>
      if(!buffered.isEmpty) {
        val toSendNow = buffered.take(totalDemand)
        toSendNow.foreach(onNext)
        buffered = buffered.drop(toSendNow.size)
      }

    case DispatchCommand(command) =>
      if (totalDemand > 0) {
        onNext(command)
        sender() ! CommandDispatched(command)
      } else if(buffered.size < bufferSize){
        buffered = buffered :+ command
        sender() ! CommandDispatched(command)
      } else {
        sender() ! CommandNotDispatched(command)
      }
      
    case SetBufferSize(newBufferSize) =>
      bufferSize = newBufferSize
  }
}