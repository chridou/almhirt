package almhirt.ext.eventlog.anorm

import java.util.UUID
import org.joda.time.DateTime
import scalaz._
import scalaz.Scalaz._
import almhirt.common._
import almhirt.almvalidation.kit._
import almhirt.domain._
import riftwarp._

trait TestPersonEvent extends DomainEvent
case class TestPersonCreated(id: UUID, aggId: UUID, name: String, timestamp: DateTime = DateTime.now) extends TestPersonEvent with CreatingNewAggregateRootEvent
case class TestPersonNameChanged(id: UUID,aggId: UUID, aggVersion: Long, newName: String, timestamp: DateTime = DateTime.now) extends TestPersonEvent
case class TestPersonAddressAquired(id: UUID,aggId: UUID, aggVersion: Long, aquiredAddress: String, timestamp: DateTime = DateTime.now) extends TestPersonEvent
case class TestPersonMoved(id: UUID,aggId: UUID, aggVersion: Long, newAddress: String, timestamp: DateTime = DateTime.now) extends TestPersonEvent
case class TestPersonUnhandledEvent(id: UUID,aggId: UUID, aggVersion: Long, timestamp: DateTime = DateTime.now) extends TestPersonEvent

class TestPersonCreatedDecomposer extends Decomposer[TestPersonCreated] {
  val typeDescriptor = TypeDescriptor(classOf[TestPersonCreated])
  def decompose[TDimension <: RiftDimension](what: TestPersonCreated)(implicit into: Dematerializer[TDimension]): AlmValidation[Dematerializer[TDimension]] = {
    into.addTypeDescriptor(typeDescriptor)
      .flatMap(_.addUuid("id", what.id))
      .flatMap(_.addUuid("aggId", what.aggId))
      .flatMap(_.addString("name", what.name))
      .flatMap(_.addDateTime("timestamp", what.timestamp))
  }
}

class TestPersonCreatedRecomposer extends Recomposer[TestPersonCreated] {
  val typeDescriptor = TypeDescriptor(classOf[TestPersonCreated])
  def recompose(from: Rematerializer): AlmValidation[TestPersonCreated] = {
    val id = from.getUuid("id").toAgg
    val aggId = from.getUuid("aggId").toAgg
    val name = from.getString("name").toAgg
    val timestamp = from.getDateTime("timestamp").toAgg
    (id |@| aggId |@| name |@| timestamp)(TestPersonCreated.apply)
  }
}

class TestPersonNameChangedDecomposer extends Decomposer[TestPersonNameChanged] {
  val typeDescriptor = TypeDescriptor(classOf[TestPersonNameChanged])
  def decompose[TDimension <: RiftDimension](what: TestPersonNameChanged)(implicit into: Dematerializer[TDimension]): AlmValidation[Dematerializer[TDimension]] = {
    into.addTypeDescriptor(typeDescriptor)
      .flatMap(_.addUuid("id", what.id))
      .flatMap(_.addUuid("aggId", what.aggId))
      .flatMap(_.addLong("aggVersion", what.aggVersion))
      .flatMap(_.addString("newName", what.newName))
      .flatMap(_.addDateTime("timestamp", what.timestamp))
  }
}

class TestPersonNameChangedRecomposer extends Recomposer[TestPersonNameChanged] {
  val typeDescriptor = TypeDescriptor(classOf[TestPersonNameChanged])
  def recompose(from: Rematerializer): AlmValidation[TestPersonNameChanged] = {
    val id = from.getUuid("id").toAgg
    val aggId = from.getUuid("aggId").toAgg
    val aggVersion = from.getLong("aggVersion").toAgg
    val newName = from.getString("newName").toAgg
    val timestamp = from.getDateTime("timestamp").toAgg
    (id |@| aggId|@| aggVersion |@| newName |@| timestamp)(TestPersonNameChanged.apply)
  }
}

class TestPersonAddressAquiredDecomposer extends Decomposer[TestPersonAddressAquired] {
  val typeDescriptor = TypeDescriptor(classOf[TestPersonAddressAquired])
  def decompose[TDimension <: RiftDimension](what: TestPersonAddressAquired)(implicit into: Dematerializer[TDimension]): AlmValidation[Dematerializer[TDimension]] = {
    into.addTypeDescriptor(typeDescriptor)
      .flatMap(_.addUuid("id", what.id))
      .flatMap(_.addUuid("aggId", what.aggId))
      .flatMap(_.addLong("aggVersion", what.aggVersion))
      .flatMap(_.addString("aquiredAddress", what.aquiredAddress))
      .flatMap(_.addDateTime("timestamp", what.timestamp))
  }
}

class TestPersonAddressAquiredRecomposer extends Recomposer[TestPersonAddressAquired] {
  val typeDescriptor = TypeDescriptor(classOf[TestPersonAddressAquired])
  def recompose(from: Rematerializer): AlmValidation[TestPersonAddressAquired] = {
    val id = from.getUuid("id").toAgg
    val aggId = from.getUuid("aggId").toAgg
    val aggVersion = from.getLong("aggVersion").toAgg
    val aquiredAddress = from.getString("aquiredAddress").toAgg
    val timestamp = from.getDateTime("timestamp").toAgg
    (id |@| aggId|@| aggVersion |@| aquiredAddress |@| timestamp)(TestPersonAddressAquired.apply)
  }
}

class TestPersonMovedDecomposer extends Decomposer[TestPersonMoved] {
  val typeDescriptor = TypeDescriptor(classOf[TestPersonMoved])
  def decompose[TDimension <: RiftDimension](what: TestPersonMoved)(implicit into: Dematerializer[TDimension]): AlmValidation[Dematerializer[TDimension]] = {
    into.addTypeDescriptor(typeDescriptor)
      .flatMap(_.addUuid("id", what.id))
      .flatMap(_.addUuid("aggId", what.aggId))
      .flatMap(_.addLong("aggVersion", what.aggVersion))
      .flatMap(_.addString("newAddress", what.newAddress))
      .flatMap(_.addDateTime("timestamp", what.timestamp))
  }
}

class TestPersonMovedRecomposer extends Recomposer[TestPersonMoved] {
  val typeDescriptor = TypeDescriptor(classOf[TestPersonMoved])
  def recompose(from: Rematerializer): AlmValidation[TestPersonMoved] = {
    val id = from.getUuid("id").toAgg
    val aggId = from.getUuid("aggId").toAgg
    val aggVersion = from.getLong("aggVersion").toAgg
    val newAddress = from.getString("newAddress").toAgg
    val timestamp = from.getDateTime("timestamp").toAgg
    (id |@| aggId|@| aggVersion |@| newAddress |@| timestamp)(TestPersonMoved.apply)
  }
}

class TestPersonUnhandledEventDecomposer extends Decomposer[TestPersonUnhandledEvent] {
  val typeDescriptor = TypeDescriptor(classOf[TestPersonUnhandledEvent])
  def decompose[TDimension <: RiftDimension](what: TestPersonUnhandledEvent)(implicit into: Dematerializer[TDimension]): AlmValidation[Dematerializer[TDimension]] = {
    into.addTypeDescriptor(typeDescriptor)
      .flatMap(_.addUuid("id", what.id))
      .flatMap(_.addUuid("aggId", what.aggId))
      .flatMap(_.addLong("aggVersion", what.aggVersion))
      .flatMap(_.addDateTime("timestamp", what.timestamp))
  }
}

class TestPersonUnhandledEventRecomposer extends Recomposer[TestPersonUnhandledEvent] {
  val typeDescriptor = TypeDescriptor(classOf[TestPersonUnhandledEvent])
  def recompose(from: Rematerializer): AlmValidation[TestPersonUnhandledEvent] = {
    val id = from.getUuid("id").toAgg
    val aggId = from.getUuid("aggId").toAgg
    val aggVersion = from.getLong("aggVersion").toAgg
    val timestamp = from.getDateTime("timestamp").toAgg
    (id |@| aggId|@| aggVersion |@| timestamp)(TestPersonUnhandledEvent.apply)
  }
}