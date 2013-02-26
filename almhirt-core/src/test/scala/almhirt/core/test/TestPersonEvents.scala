package almhirt.core.test

import java.util.UUID
import org.joda.time.DateTime
import scalaz._, Scalaz._
import almhirt.common._
import almhirt.almvalidation.kit._
import almhirt.domain._

trait TestPersonEvent extends DomainEvent
case class TestPersonCreated(header: DomainEventHeader, name: String) extends TestPersonEvent with CreatingNewAggregateRootEvent
case class TestPersonNameChanged(header: DomainEventHeader, newName: String) extends TestPersonEvent
case class TestPersonAddressAquired(header: DomainEventHeader, aquiredAddress: String) extends TestPersonEvent
case class TestPersonMoved(header: DomainEventHeader, newAddress: String) extends TestPersonEvent
case class TestPersonUnhandledEvent(header: DomainEventHeader) extends TestPersonEvent
