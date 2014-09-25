package almhirt.domain

import akka.actor.{ActorRef ,Props }
import almhirt.common._

trait AggregateRootDroneFactory extends Function3[AggregateRootCommand, ActorRef, Option[ActorRef], AlmValidation[Props]] {
  final def apply(command: AggregateRootCommand, aggregateRootEventLog: ActorRef, snapShotStorage: Option[ActorRef]): AlmValidation[Props] = 
    propsForCommand(command, aggregateRootEventLog,snapShotStorage)
    
  def propsForCommand(command: AggregateRootCommand, aggregateRootEventLog: ActorRef, snapShotStorage: Option[ActorRef]): AlmValidation[Props]
}
