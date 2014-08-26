package almhirt.domain

import akka.actor.Props
import almhirt.common._

trait AggregateRootHiveFactory extends (HiveDescriptor ⇒ AlmValidation[Props]) {
  final def apply(descriptor: HiveDescriptor): AlmValidation[Props] = props(descriptor)
  def props(descriptor: HiveDescriptor): AlmValidation[Props]
}



