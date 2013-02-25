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
import almhirt.ext.core.riftwarp.serialization._

class TestPersonCreatedDecomposer extends Decomposer[TestPersonCreated] with HasNoAlternativeRiftDescriptors {
  val riftDescriptor = RiftDescriptor(classOf[TestPersonCreated])
  def decompose[TDimension <: RiftDimension](what: TestPersonCreated)(into: Dematerializer[TDimension]): AlmValidation[Dematerializer[TDimension]] = {
    into.addRiftDescriptor(riftDescriptor)
      .addUuid("id", what.id)
      .addComplexSelective("aggRef", AggregateRootRefDecomposer, what.aggRef).map(
        _.addString("name", what.name)
          .addDateTime("timestamp", what.timestamp))
  }
}

class TestPersonCreatedRecomposer extends Recomposer[TestPersonCreated] with HasNoAlternativeRiftDescriptors {
  val riftDescriptor = RiftDescriptor(classOf[TestPersonCreated])
  def recompose(from: Rematerializer): AlmValidation[TestPersonCreated] = {
    val id = from.getUuid("id").toAgg
    val aggRef = from.getComplexType("aggRef", AggregateRootRefRecomposer).toAgg
    val name = from.getString("name").toAgg
    val timestamp = from.getDateTime("timestamp").toAgg
    (id |@| aggRef |@| name |@| timestamp)(TestPersonCreated.apply)
  }
}

class TestPersonNameChangedDecomposer extends Decomposer[TestPersonNameChanged] with HasNoAlternativeRiftDescriptors {
  val riftDescriptor = RiftDescriptor(classOf[TestPersonNameChanged])
  def decompose[TDimension <: RiftDimension](what: TestPersonNameChanged)(into: Dematerializer[TDimension]): AlmValidation[Dematerializer[TDimension]] = {
    into.addRiftDescriptor(riftDescriptor)
      .addUuid("id", what.id)
      .addComplexSelective("aggRef", AggregateRootRefDecomposer, what.aggRef).map(
        _.addString("newName", what.newName)
          .addDateTime("timestamp", what.timestamp))
  }
}

class TestPersonNameChangedRecomposer extends Recomposer[TestPersonNameChanged] with HasNoAlternativeRiftDescriptors {
  val riftDescriptor = RiftDescriptor(classOf[TestPersonNameChanged])
  def recompose(from: Rematerializer): AlmValidation[TestPersonNameChanged] = {
    val id = from.getUuid("id").toAgg
    val aggRef = from.getComplexType("aggRef", AggregateRootRefRecomposer).toAgg
    val newName = from.getString("newName").toAgg
    val timestamp = from.getDateTime("timestamp").toAgg
    (id |@| aggRef |@| newName |@| timestamp)(TestPersonNameChanged.apply)
  }
}

class TestPersonAddressAquiredDecomposer extends Decomposer[TestPersonAddressAquired] with HasNoAlternativeRiftDescriptors {
  val riftDescriptor = RiftDescriptor(classOf[TestPersonAddressAquired])
  def decompose[TDimension <: RiftDimension](what: TestPersonAddressAquired)(into: Dematerializer[TDimension]): AlmValidation[Dematerializer[TDimension]] = {
    into.addRiftDescriptor(riftDescriptor)
      .addUuid("id", what.id)
      .addComplexSelective("aggRef", AggregateRootRefDecomposer, what.aggRef).map(
        _.addString("aquiredAddress", what.aquiredAddress)
          .addDateTime("timestamp", what.timestamp))
  }
}

class TestPersonAddressAquiredRecomposer extends Recomposer[TestPersonAddressAquired] with HasNoAlternativeRiftDescriptors {
  val riftDescriptor = RiftDescriptor(classOf[TestPersonAddressAquired])
  def recompose(from: Rematerializer): AlmValidation[TestPersonAddressAquired] = {
    val id = from.getUuid("id").toAgg
    val aggRef = from.getComplexType("aggRef", AggregateRootRefRecomposer).toAgg
    val aquiredAddress = from.getString("aquiredAddress").toAgg
    val timestamp = from.getDateTime("timestamp").toAgg
    (id |@| aggRef |@| aquiredAddress |@| timestamp)(TestPersonAddressAquired.apply)
  }
}

class TestPersonMovedDecomposer extends Decomposer[TestPersonMoved] with HasNoAlternativeRiftDescriptors {
  val riftDescriptor = RiftDescriptor(classOf[TestPersonMoved])
  def decompose[TDimension <: RiftDimension](what: TestPersonMoved)(into: Dematerializer[TDimension]): AlmValidation[Dematerializer[TDimension]] = {
    into.addRiftDescriptor(riftDescriptor)
      .addUuid("id", what.id)
      .addComplexSelective("aggRef", AggregateRootRefDecomposer, what.aggRef).map(
        _.addString("newAddress", what.newAddress)
          .addDateTime("timestamp", what.timestamp))
  }
}

class TestPersonMovedRecomposer extends Recomposer[TestPersonMoved] with HasNoAlternativeRiftDescriptors {
  val riftDescriptor = RiftDescriptor(classOf[TestPersonMoved])
  def recompose(from: Rematerializer): AlmValidation[TestPersonMoved] = {
    val id = from.getUuid("id").toAgg
    val aggRef = from.getComplexType("aggRef", AggregateRootRefRecomposer).toAgg
    val newAddress = from.getString("newAddress").toAgg
    val timestamp = from.getDateTime("timestamp").toAgg
    (id |@| aggRef |@| newAddress |@| timestamp)(TestPersonMoved.apply)
  }
}

class TestPersonUnhandledEventDecomposer extends Decomposer[TestPersonUnhandledEvent] with HasNoAlternativeRiftDescriptors {
  val riftDescriptor = RiftDescriptor(classOf[TestPersonUnhandledEvent])
  def decompose[TDimension <: RiftDimension](what: TestPersonUnhandledEvent)(into: Dematerializer[TDimension]): AlmValidation[Dematerializer[TDimension]] = {
    into.addRiftDescriptor(riftDescriptor)
      .addUuid("id", what.id)
      .addComplexSelective("aggRef", AggregateRootRefDecomposer, what.aggRef).map(
        _.addDateTime("timestamp", what.timestamp))
  }
}

class TestPersonUnhandledEventRecomposer extends Recomposer[TestPersonUnhandledEvent] with HasNoAlternativeRiftDescriptors {
  val riftDescriptor = RiftDescriptor(classOf[TestPersonUnhandledEvent])
  def recompose(from: Rematerializer): AlmValidation[TestPersonUnhandledEvent] = {
    val id = from.getUuid("id").toAgg
    val aggRef = from.getComplexType("aggRef", AggregateRootRefRecomposer).toAgg
    val timestamp = from.getDateTime("timestamp").toAgg
    (id |@| aggRef |@| timestamp)(TestPersonUnhandledEvent.apply)
  }
}