package test

import java.util.UUID
import org.joda.time.DateTime
import scalaz._, Scalaz._
import almhirt.common._
import almhirt.almvalidation.kit._
import almhirt.domain._

trait TestPersonEvent extends DomainEvent
case class TestPersonCreated(id: UUID, aggId: UUID, name: String, timestamp: DateTime = DateTime.now) extends TestPersonEvent with CreatingNewAggregateRootEvent
case class TestPersonNameChanged(id: UUID,aggId: UUID, aggVersion: Long, newName: String, timestamp: DateTime = DateTime.now) extends TestPersonEvent
case class TestPersonAddressAquired(id: UUID,aggId: UUID, aggVersion: Long, aquiredAddress: String, timestamp: DateTime = DateTime.now) extends TestPersonEvent
case class TestPersonMoved(id: UUID,aggId: UUID, aggVersion: Long, newAddress: String, timestamp: DateTime = DateTime.now) extends TestPersonEvent
case class TestPersonUnhandledEvent(id: UUID,aggId: UUID, aggVersion: Long, timestamp: DateTime = DateTime.now) extends TestPersonEvent
