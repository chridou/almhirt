package test

import almhirt.domain.impl.BasicAggregateRootRepository
import almhirt.environment.AlmhirtContext
import almhirt.eventsourcing.DomainEventLog

class TestPersonRepository(eventLog: DomainEventLog)(implicit almhirtContext: AlmhirtContext) extends BasicAggregateRootRepository[TestPerson, TestPersonEvent](eventLog, TestPerson, almhirtContext)
