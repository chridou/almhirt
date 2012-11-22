package test

import java.util.UUID
import org.joda.time.DateTime
import scalaz._, Scalaz._
import almhirt.common._
import almhirt.almvalidation.kit._
import almhirt.domain._
import almhirt.riftwarp._

trait TestPersonEvent extends DomainEvent
case class TestPersonCreated(id: UUID, aggId: UUID, name: String, timestamp: DateTime = DateTime.now) extends TestPersonEvent with CreatingNewAggregateRootEvent
case class TestPersonNameChanged(id: UUID,aggId: UUID, aggVersion: Long, newName: String, timestamp: DateTime = DateTime.now) extends TestPersonEvent
case class TestPersonAddressAquired(id: UUID,aggId: UUID, aggVersion: Long, aquiredAddress: String, timestamp: DateTime = DateTime.now) extends TestPersonEvent
case class TestPersonMoved(id: UUID,aggId: UUID, aggVersion: Long, newAddress: String, timestamp: DateTime = DateTime.now) extends TestPersonEvent
case class TestPersonUnhandledEvent(id: UUID,aggId: UUID, aggVersion: Long, timestamp: DateTime = DateTime.now) extends TestPersonEvent

class TestPersonCreatedDecomposer extends Decomposer[TestPersonCreated] {
  val typeDescriptor = TypeDescriptor(classOf[TestPersonCreated])
  def decompose[TDimension <: RiftTypedDimension[_], TChannel <: RiftChannelDescriptor](what: TestPersonCreated)(implicit into: Dematerializer[TDimension, TChannel]): AlmValidation[Dematerializer[TDimension, TChannel]] = {
    into.addTypeDescriptor(typeDescriptor)
      .bind(_.addUuid("id", what.id))
      .bind(_.addUuid("aggId", what.aggId))
      .bind(_.addString("name", what.name))
      .bind(_.addDateTime("timestamp", what.timestamp))
  }
}

class TestPersonCreatedRecomposer extends Recomposer[TestPersonCreated] {
  val typeDescriptor = TypeDescriptor(classOf[TestPersonCreated])
  def recompose(from: RematerializationArray): AlmValidation[TestPersonCreated] = {
    val id = from.getUuid("id").toAgg
    val aggId = from.getUuid("aggId").toAgg
    val name = from.getString("name").toAgg
    val timestamp = from.getDateTime("timestamp").toAgg
    (id |@| aggId |@| name |@| timestamp)(TestPersonCreated.apply)
  }
}

class TestPersonNameChangedDecomposer extends Decomposer[TestPersonNameChanged] {
  val typeDescriptor = TypeDescriptor(classOf[TestPersonNameChanged])
  def decompose[TDimension <: RiftTypedDimension[_], TChannel <: RiftChannelDescriptor](what: TestPersonNameChanged)(implicit into: Dematerializer[TDimension, TChannel]): AlmValidation[Dematerializer[TDimension, TChannel]] = {
    into.addTypeDescriptor(typeDescriptor)
      .bind(_.addUuid("id", what.id))
      .bind(_.addUuid("aggId", what.aggId))
      .bind(_.addLong("aggVersion", what.aggVersion))
      .bind(_.addString("newName", what.newName))
      .bind(_.addDateTime("timestamp", what.timestamp))
  }
}

class TestPersonNameChangedRecomposer extends Recomposer[TestPersonNameChanged] {
  val typeDescriptor = TypeDescriptor(classOf[TestPersonNameChanged])
  def recompose(from: RematerializationArray): AlmValidation[TestPersonNameChanged] = {
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
  def decompose[TDimension <: RiftTypedDimension[_], TChannel <: RiftChannelDescriptor](what: TestPersonAddressAquired)(implicit into: Dematerializer[TDimension, TChannel]): AlmValidation[Dematerializer[TDimension, TChannel]] = {
    into.addTypeDescriptor(typeDescriptor)
      .bind(_.addUuid("id", what.id))
      .bind(_.addUuid("aggId", what.aggId))
      .bind(_.addLong("aggVersion", what.aggVersion))
      .bind(_.addString("aquiredAddress", what.aquiredAddress))
      .bind(_.addDateTime("timestamp", what.timestamp))
  }
}

class TestPersonAddressAquiredRecomposer extends Recomposer[TestPersonAddressAquired] {
  val typeDescriptor = TypeDescriptor(classOf[TestPersonAddressAquired])
  def recompose(from: RematerializationArray): AlmValidation[TestPersonAddressAquired] = {
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
  def decompose[TDimension <: RiftTypedDimension[_], TChannel <: RiftChannelDescriptor](what: TestPersonMoved)(implicit into: Dematerializer[TDimension, TChannel]): AlmValidation[Dematerializer[TDimension, TChannel]] = {
    into.addTypeDescriptor(typeDescriptor)
      .bind(_.addUuid("id", what.id))
      .bind(_.addUuid("aggId", what.aggId))
      .bind(_.addLong("aggVersion", what.aggVersion))
      .bind(_.addString("newAddress", what.newAddress))
      .bind(_.addDateTime("timestamp", what.timestamp))
  }
}

class TestPersonMovedRecomposer extends Recomposer[TestPersonMoved] {
  val typeDescriptor = TypeDescriptor(classOf[TestPersonMoved])
  def recompose(from: RematerializationArray): AlmValidation[TestPersonMoved] = {
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
  def decompose[TDimension <: RiftTypedDimension[_], TChannel <: RiftChannelDescriptor](what: TestPersonUnhandledEvent)(implicit into: Dematerializer[TDimension, TChannel]): AlmValidation[Dematerializer[TDimension, TChannel]] = {
    into.addTypeDescriptor(typeDescriptor)
      .bind(_.addUuid("id", what.id))
      .bind(_.addUuid("aggId", what.aggId))
      .bind(_.addLong("aggVersion", what.aggVersion))
      .bind(_.addDateTime("timestamp", what.timestamp))
  }
}

class TestPersonUnhandledEventRecomposer extends Recomposer[TestPersonUnhandledEvent] {
  val typeDescriptor = TypeDescriptor(classOf[TestPersonUnhandledEvent])
  def recompose(from: RematerializationArray): AlmValidation[TestPersonUnhandledEvent] = {
    val id = from.getUuid("id").toAgg
    val aggId = from.getUuid("aggId").toAgg
    val aggVersion = from.getLong("aggVersion").toAgg
    val timestamp = from.getDateTime("timestamp").toAgg
    (id |@| aggId|@| aggVersion |@| timestamp)(TestPersonUnhandledEvent.apply)
  }
}