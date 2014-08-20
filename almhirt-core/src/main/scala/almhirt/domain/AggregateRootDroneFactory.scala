package almhirt.domain

import akka.actor.Props
import almhirt.common._

trait AggregateRootDroneFactory extends Function1[AggregateCommand, AlmValidation[Props]] {
  final def apply(command: AggregateCommand): AlmValidation[Props] = propsForCommand(command)
  def propsForCommand(command: AggregateCommand): AlmValidation[Props]
}
