package almhirt.util

import almhirt.domain.AggregateRootRef

sealed trait PerformedAction
final case class PerformedCreateAction(aggRef: AggregateRootRef) extends PerformedAction
final case class PerformedUpdateAction(aggRef: AggregateRootRef) extends PerformedAction
final case object PerformedUnspecifiedAction extends PerformedAction

