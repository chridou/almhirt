package test

import java.util.UUID
import org.joda.time.DateTime
import scalaz._, Scalaz._
import almhirt.common._
import almhirt.almvalidation.kit._
import almhirt.domain._
import almhirt.riftwarp._

trait TestPersonEvent extends DomainEvent
case class TestPersonCreated(id: UUID, name: String, timestamp: DateTime = DateTime.now) extends TestPersonEvent with CreatingNewAggregateRootEvent
case class TestPersonNameChanged(id: UUID, version: Long, newName: String, timestamp: DateTime = DateTime.now) extends TestPersonEvent
case class TestPersonAddressAquired(id: UUID, version: Long, aquiredAddress: String, timestamp: DateTime = DateTime.now) extends TestPersonEvent
case class TestPersonMoved(id: UUID, version: Long, newAddress: String, timestamp: DateTime = DateTime.now) extends TestPersonEvent
case class TestPersonUnhandledEvent(id: UUID, version: Long, timestamp: DateTime = DateTime.now) extends TestPersonEvent

class TestPersonCreatedDecomposer extends Decomposer[TestPersonCreated] {
  val typeDescriptor = TypeDescriptor(classOf[TestPersonCreated])
  def decompose(what: TestPersonCreated)(implicit into: DematerializationFunnel): AlmValidation[DematerializationFunnel] = {
    into.addTypeDescriptor(typeDescriptor)
      .bind(_.addUuid("id", what.id))
      .bind(_.addString("name", what.name))
      .bind(_.addDateTime("timestamp", what.timestamp))
  }
}

class TestPersonCreatedRecomposer extends Recomposer[TestPersonCreated] {
  val typeDescriptor = TypeDescriptor(classOf[TestPersonCreated])
  def recompose(from: RematerializationArray): AlmValidation[TestPersonCreated] = {
    val id = from.getUuid("id").toAgg
    val name = from.getString("name").toAgg
    val timestamp = from.getDateTime("timestamp").toAgg
    (id |@| name |@| timestamp)(TestPersonCreated.apply)
  }
}

class TestPersonNameChangedDecomposer extends Decomposer[TestPersonNameChanged] {
  val typeDescriptor = TypeDescriptor(classOf[TestPersonNameChanged])
  def decompose(what: TestPersonNameChanged)(implicit into: DematerializationFunnel): AlmValidation[DematerializationFunnel] = {
    into.addTypeDescriptor(typeDescriptor)
      .bind(_.addUuid("id", what.id))
      .bind(_.addLong("version", what.version))
      .bind(_.addString("name", what.newName))
      .bind(_.addDateTime("timestamp", what.timestamp))
  }
}

class TestPersonNameChangedRecomposer extends Recomposer[TestPersonNameChanged] {
  val typeDescriptor = TypeDescriptor(classOf[TestPersonNameChanged])
  def recompose(from: RematerializationArray): AlmValidation[TestPersonNameChanged] = {
    val id = from.getUuid("id").toAgg
    val version = from.getLong("version").toAgg
    val newName = from.getString("newName").toAgg
    val timestamp = from.getDateTime("timestamp").toAgg
    (id |@| version |@| newName |@| timestamp)(TestPersonNameChanged.apply)
  }
}

class TestPersonAddressAquiredDecomposer extends Decomposer[TestPersonAddressAquired] {
  val typeDescriptor = TypeDescriptor(classOf[TestPersonAddressAquired])
  def decompose(what: TestPersonAddressAquired)(implicit into: DematerializationFunnel): AlmValidation[DematerializationFunnel] = {
    into.addTypeDescriptor(typeDescriptor)
      .bind(_.addUuid("id", what.id))
      .bind(_.addLong("version", what.version))
      .bind(_.addString("aquiredAddress", what.aquiredAddress))
      .bind(_.addDateTime("timestamp", what.timestamp))
  }
}

class TestPersonAddressAquiredRecomposer extends Recomposer[TestPersonAddressAquired] {
  val typeDescriptor = TypeDescriptor(classOf[TestPersonAddressAquired])
  def recompose(from: RematerializationArray): AlmValidation[TestPersonAddressAquired] = {
    val id = from.getUuid("id").toAgg
    val version = from.getLong("version").toAgg
    val aquiredAddress = from.getString("aquiredAddress").toAgg
    val timestamp = from.getDateTime("timestamp").toAgg
    (id |@| version |@| aquiredAddress |@| timestamp)(TestPersonAddressAquired.apply)
  }
}

class TestPersonMovedDecomposer extends Decomposer[TestPersonMoved] {
  val typeDescriptor = TypeDescriptor(classOf[TestPersonMoved])
  def decompose(what: TestPersonMoved)(implicit into: DematerializationFunnel): AlmValidation[DematerializationFunnel] = {
    into.addTypeDescriptor(typeDescriptor)
      .bind(_.addUuid("id", what.id))
      .bind(_.addLong("version", what.version))
      .bind(_.addString("newAddress", what.newAddress))
      .bind(_.addDateTime("timestamp", what.timestamp))
  }
}

class TestPersonMovedRecomposer extends Recomposer[TestPersonMoved] {
  val typeDescriptor = TypeDescriptor(classOf[TestPersonMoved])
  def recompose(from: RematerializationArray): AlmValidation[TestPersonMoved] = {
    val id = from.getUuid("id").toAgg
    val version = from.getLong("version").toAgg
    val newAddress = from.getString("newAddress").toAgg
    val timestamp = from.getDateTime("timestamp").toAgg
    (id |@| version |@| newAddress |@| timestamp)(TestPersonMoved.apply)
  }
}

class TestPersonUnhandledEventDecomposer extends Decomposer[TestPersonUnhandledEvent] {
  val typeDescriptor = TypeDescriptor(classOf[TestPersonUnhandledEvent])
  def decompose(what: TestPersonUnhandledEvent)(implicit into: DematerializationFunnel): AlmValidation[DematerializationFunnel] = {
    into.addTypeDescriptor(typeDescriptor)
      .bind(_.addUuid("id", what.id))
      .bind(_.addLong("version", what.version))
      .bind(_.addDateTime("timestamp", what.timestamp))
  }
}

class TestPersonUnhandledEventRecomposer extends Recomposer[TestPersonUnhandledEvent] {
  val typeDescriptor = TypeDescriptor(classOf[TestPersonUnhandledEvent])
  def recompose(from: RematerializationArray): AlmValidation[TestPersonUnhandledEvent] = {
    val id = from.getUuid("id").toAgg
    val version = from.getLong("version").toAgg
    val timestamp = from.getDateTime("timestamp").toAgg
    (id |@| version |@| timestamp)(TestPersonUnhandledEvent.apply)
  }
}