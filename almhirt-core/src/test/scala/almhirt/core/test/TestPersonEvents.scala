package almhirt.core.test

import java.util.UUID
import org.joda.time.DateTime
import scalaz._, Scalaz._
import almhirt.common._
import almhirt.almvalidation.kit._
import almhirt.domain._

trait TestPersonEvent extends DomainEvent
case class TestPersonCreated(id: UUID, aggRef: AggregateRootRef, name: String, timestamp: DateTime = DateTime.now) extends TestPersonEvent with CreatingNewAggregateRootEvent
case class TestPersonNameChanged(id: UUID, aggRef: AggregateRootRef, newName: String, timestamp: DateTime = DateTime.now) extends TestPersonEvent
case class TestPersonAddressAquired(id: UUID, aggRef: AggregateRootRef, aquiredAddress: String, timestamp: DateTime = DateTime.now) extends TestPersonEvent
case class TestPersonMoved(id: UUID, aggRef: AggregateRootRef, newAddress: String, timestamp: DateTime = DateTime.now) extends TestPersonEvent
case class TestPersonUnhandledEvent(id: UUID, aggRef: AggregateRootRef, timestamp: DateTime = DateTime.now) extends TestPersonEvent
