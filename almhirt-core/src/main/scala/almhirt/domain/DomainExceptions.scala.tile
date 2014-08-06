package almhirt.domain

import almhirt.common._

class PotentiallyInvalidStatePersistedException(arId: java.util.UUID, problem: Problem) extends RuntimeException(s"""Aggregate root with id "${arId.toString()}" could be in an invalid state!""")

class NewAggregateRootWasRequiredException(arId: java.util.UUID) extends RuntimeException(s"""Aggregate root with id "${arId.toString()}" does not exist nor one was created""")

class CouldNotRebuildAggregateRootException(arId: java.util.UUID, problem: Problem) extends RuntimeException(s"""Aggregate root with id "${arId.toString()}" could not be rebuild""")

class FetchDomainEventsFailed(arId: java.util.UUID, eventlogName: String, proplem: Option[Problem]) extends RuntimeException(s"""The domnain event log "$eventlogName" failed when fetching events for Aggregate root with id "${arId.toString()}".""")

class CommitDomainEventsFailed(arId: java.util.UUID, eventlogName: String, proplem: Option[Problem]) extends RuntimeException(s"""The domnain event log "$eventlogName" failed when storing events for Aggregate root with id "${arId.toString()}".""")
