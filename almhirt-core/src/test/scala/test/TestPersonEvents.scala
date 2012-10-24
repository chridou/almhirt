package test

import java.util.UUID
import org.joda.time.DateTime
import almhirt.domain.CreatingNewAggregateRootEvent
import almhirt.domain.DomainEvent

trait TestPersonEvent extends DomainEvent
case class TestPersonCreated(aggRootId: UUID, name: String, timestamp: DateTime = DateTime.now) extends TestPersonEvent with CreatingNewAggregateRootEvent
case class TestPersonNameChanged(aggRootId: UUID, aggRootVersion: Long, newName: String, timestamp: DateTime = DateTime.now) extends TestPersonEvent
case class TestPersonAddressAquired(aggRootId: UUID, aggRootVersion: Long, aquiredAddress: String, timestamp: DateTime = DateTime.now) extends TestPersonEvent
case class TestPersonMoved(aggRootId: UUID, aggRootVersion: Long, newAddress: String, timestamp: DateTime = DateTime.now) extends TestPersonEvent
case class TestPersonUnhandledEvent(aggRootId: UUID, aggRootVersion: Long, timestamp: DateTime = DateTime.now) extends TestPersonEvent

