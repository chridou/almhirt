package almhirt.util

import almhirt.domain.AggregateRootRef

sealed trait PerformedAction

sealed trait PerformedDomainAction extends PerformedAction
final case class PerformedCreateAction(aggRef: AggregateRootRef) extends PerformedDomainAction
final case class PerformedUpdateAction(aggRef: AggregateRootRef) extends PerformedDomainAction
final case class PerformedDeleteAction(aggRef: AggregateRootRef) extends PerformedDomainAction
final case class PerformedNoAction(reason: String) extends PerformedDomainAction

