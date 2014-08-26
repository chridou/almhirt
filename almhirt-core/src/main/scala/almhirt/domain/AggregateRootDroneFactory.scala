package almhirt.domain

import akka.actor.Props
import almhirt.common._

trait AggregateRootDroneFactory extends Function1[AggregateRootCommand, AlmValidation[Props]] {
  final def apply(command: AggregateRootCommand): AlmValidation[Props] = propsForCommand(command)
  def propsForCommand(command: AggregateRootCommand): AlmValidation[Props]
}
