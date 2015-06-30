package almhirt.domain

import akka.actor.{ ActorRef, Props }
import almhirt.common._
import almhirt.snapshots.SnapshottingPolicyProvider

trait AggregateRootDroneFactory extends Function3[AggregateRootCommand, ActorRef, Option[(ActorRef, SnapshottingPolicyProvider)], AlmValidation[Props]] {
  final def apply(command: AggregateRootCommand, aggregateRootEventLog: ActorRef, snapshotting: Option[(ActorRef, SnapshottingPolicyProvider)]): AlmValidation[Props] =
    propsForCommand(command, aggregateRootEventLog, snapshotting)

  def propsForCommand(command: AggregateRootCommand, aggregateRootEventLog: ActorRef, snapshotting: Option[(ActorRef, SnapshottingPolicyProvider)]): AlmValidation[Props]
}
