package almhirt.util

import almhirt.domain.AggregateRootRef

sealed trait PerformedAction
final case class PerformedCreateAction(aggRef: AggregateRootRef) extends PerformedAction
final case class PerformedUpdateAction(aggRef: AggregateRootRef) extends PerformedAction
final case class PerformedDeleteAction(aggRef: AggregateRootRef) extends PerformedAction
final case class PerformedNoAction(reason: String) extends PerformedAction

