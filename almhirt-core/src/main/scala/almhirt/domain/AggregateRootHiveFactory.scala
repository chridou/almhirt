package almhirt.domain

import akka.actor.Props
import almhirt.common._

trait AggregateRootHiveFactory extends (() => Props) {
  final def apply(): Props = props
  def props: Props
}



