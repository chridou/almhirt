package test

import java.util.UUID
import org.joda.time.DateTime
import almhirt.domain.CreatingNewAggregateRootEvent
import almhirt.domain.DomainEvent

trait TestPersonEvent extends DomainEvent
case class TestPersonCreated(id: UUID, name: String, timestamp: DateTime = DateTime.now) extends TestPersonEvent with CreatingNewAggregateRootEvent
case class TestPersonNameChanged(id: UUID, version: Long, newName: String, timestamp: DateTime = DateTime.now) extends TestPersonEvent
case class TestPersonAddressAquired(id: UUID, version: Long, aquiredAddress: String, timestamp: DateTime = DateTime.now) extends TestPersonEvent
case class TestPersonMoved(id: UUID, version: Long, newAddress: String, timestamp: DateTime = DateTime.now) extends TestPersonEvent
case class TestPersonUnhandledEvent(id: UUID, version: Long, timestamp: DateTime = DateTime.now) extends TestPersonEvent

