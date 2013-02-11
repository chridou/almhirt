package almhirt.core.riftwarp.test

import java.util.UUID
import org.joda.time.DateTime
import scalaz._
import scalaz.Scalaz._
import almhirt.common._
import almhirt.almvalidation.kit._
import almhirt.domain._
import riftwarp._
import almhirt.core.test._
import riftwarp.components.HasNoAlternativeRiftDescriptors

class TestPersonCreatedDecomposer extends Decomposer[TestPersonCreated] with HasNoAlternativeRiftDescriptors {
  val riftDescriptor = RiftDescriptor(classOf[TestPersonCreated])
  def decompose[TDimension <: RiftDimension](what: TestPersonCreated)(into: Dematerializer[TDimension]): AlmValidation[Dematerializer[TDimension]] = {
    into.addRiftDescriptor(riftDescriptor)
      .flatMap(_.addUuid("id", what.id))
      .flatMap(_.addUuid("aggId", what.aggId))
      .flatMap(_.addString("name", what.name))
      .flatMap(_.addDateTime("timestamp", what.timestamp))
  }
}

class TestPersonCreatedRecomposer extends Recomposer[TestPersonCreated] with HasNoAlternativeRiftDescriptors {
  val riftDescriptor = RiftDescriptor(classOf[TestPersonCreated])
  def recompose(from: Rematerializer): AlmValidation[TestPersonCreated] = {
    val id = from.getUuid("id").toAgg
    val aggId = from.getUuid("aggId").toAgg
    val name = from.getString("name").toAgg
    val timestamp = from.getDateTime("timestamp").toAgg
    (id |@| aggId |@| name |@| timestamp)(TestPersonCreated.apply)
  }
}

class TestPersonNameChangedDecomposer extends Decomposer[TestPersonNameChanged] with HasNoAlternativeRiftDescriptors {
  val riftDescriptor = RiftDescriptor(classOf[TestPersonNameChanged])
  def decompose[TDimension <: RiftDimension](what: TestPersonNameChanged)(into: Dematerializer[TDimension]): AlmValidation[Dematerializer[TDimension]] = {
    into.addRiftDescriptor(riftDescriptor)
      .flatMap(_.addUuid("id", what.id))
      .flatMap(_.addUuid("aggId", what.aggId))
      .flatMap(_.addLong("aggVersion", what.aggVersion))
      .flatMap(_.addString("newName", what.newName))
      .flatMap(_.addDateTime("timestamp", what.timestamp))
  }
}

class TestPersonNameChangedRecomposer extends Recomposer[TestPersonNameChanged] with HasNoAlternativeRiftDescriptors {
  val riftDescriptor = RiftDescriptor(classOf[TestPersonNameChanged])
  def recompose(from: Rematerializer): AlmValidation[TestPersonNameChanged] = {
    val id = from.getUuid("id").toAgg
    val aggId = from.getUuid("aggId").toAgg
    val aggVersion = from.getLong("aggVersion").toAgg
    val newName = from.getString("newName").toAgg
    val timestamp = from.getDateTime("timestamp").toAgg
    (id |@| aggId |@| aggVersion |@| newName |@| timestamp)(TestPersonNameChanged.apply)
  }
}

class TestPersonAddressAquiredDecomposer extends Decomposer[TestPersonAddressAquired] with HasNoAlternativeRiftDescriptors {
  val riftDescriptor = RiftDescriptor(classOf[TestPersonAddressAquired])
  def decompose[TDimension <: RiftDimension](what: TestPersonAddressAquired)(into: Dematerializer[TDimension]): AlmValidation[Dematerializer[TDimension]] = {
    into.addRiftDescriptor(riftDescriptor)
      .flatMap(_.addUuid("id", what.id))
      .flatMap(_.addUuid("aggId", what.aggId))
      .flatMap(_.addLong("aggVersion", what.aggVersion))
      .flatMap(_.addString("aquiredAddress", what.aquiredAddress))
      .flatMap(_.addDateTime("timestamp", what.timestamp))
  }
}

class TestPersonAddressAquiredRecomposer extends Recomposer[TestPersonAddressAquired] with HasNoAlternativeRiftDescriptors {
  val riftDescriptor = RiftDescriptor(classOf[TestPersonAddressAquired])
  def recompose(from: Rematerializer): AlmValidation[TestPersonAddressAquired] = {
    val id = from.getUuid("id").toAgg
    val aggId = from.getUuid("aggId").toAgg
    val aggVersion = from.getLong("aggVersion").toAgg
    val aquiredAddress = from.getString("aquiredAddress").toAgg
    val timestamp = from.getDateTime("timestamp").toAgg
    (id |@| aggId |@| aggVersion |@| aquiredAddress |@| timestamp)(TestPersonAddressAquired.apply)
  }
}

class TestPersonMovedDecomposer extends Decomposer[TestPersonMoved] with HasNoAlternativeRiftDescriptors {
  val riftDescriptor = RiftDescriptor(classOf[TestPersonMoved])
  def decompose[TDimension <: RiftDimension](what: TestPersonMoved)(into: Dematerializer[TDimension]): AlmValidation[Dematerializer[TDimension]] = {
    into.addRiftDescriptor(riftDescriptor)
      .flatMap(_.addUuid("id", what.id))
      .flatMap(_.addUuid("aggId", what.aggId))
      .flatMap(_.addLong("aggVersion", what.aggVersion))
      .flatMap(_.addString("newAddress", what.newAddress))
      .flatMap(_.addDateTime("timestamp", what.timestamp))
  }
}

class TestPersonMovedRecomposer extends Recomposer[TestPersonMoved] with HasNoAlternativeRiftDescriptors {
  val riftDescriptor = RiftDescriptor(classOf[TestPersonMoved])
  def recompose(from: Rematerializer): AlmValidation[TestPersonMoved] = {
    val id = from.getUuid("id").toAgg
    val aggId = from.getUuid("aggId").toAgg
    val aggVersion = from.getLong("aggVersion").toAgg
    val newAddress = from.getString("newAddress").toAgg
    val timestamp = from.getDateTime("timestamp").toAgg
    (id |@| aggId |@| aggVersion |@| newAddress |@| timestamp)(TestPersonMoved.apply)
  }
}

class TestPersonUnhandledEventDecomposer extends Decomposer[TestPersonUnhandledEvent] with HasNoAlternativeRiftDescriptors {
  val riftDescriptor = RiftDescriptor(classOf[TestPersonUnhandledEvent])
  def decompose[TDimension <: RiftDimension](what: TestPersonUnhandledEvent)(into: Dematerializer[TDimension]): AlmValidation[Dematerializer[TDimension]] = {
    into.addRiftDescriptor(riftDescriptor)
      .flatMap(_.addUuid("id", what.id))
      .flatMap(_.addUuid("aggId", what.aggId))
      .flatMap(_.addLong("aggVersion", what.aggVersion))
      .flatMap(_.addDateTime("timestamp", what.timestamp))
  }
}

class TestPersonUnhandledEventRecomposer extends Recomposer[TestPersonUnhandledEvent] with HasNoAlternativeRiftDescriptors {
  val riftDescriptor = RiftDescriptor(classOf[TestPersonUnhandledEvent])
  def recompose(from: Rematerializer): AlmValidation[TestPersonUnhandledEvent] = {
    val id = from.getUuid("id").toAgg
    val aggId = from.getUuid("aggId").toAgg
    val aggVersion = from.getLong("aggVersion").toAgg
    val timestamp = from.getDateTime("timestamp").toAgg
    (id |@| aggId |@| aggVersion |@| timestamp)(TestPersonUnhandledEvent.apply)
  }
}