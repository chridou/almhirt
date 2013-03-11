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

class TestPersonCreatedDecomposer extends DomainEventDecomposer[TestPersonCreated] with HasNoAlternativeRiftDescriptors {
  val riftDescriptor = RiftDescriptor(classOf[TestPersonCreated])
  override def addEventParams[TDimension <: RiftDimension](what: TestPersonCreated, into: WarpSequencer[TDimension]): AlmValidation[WarpSequencer[TDimension]] = {
    into.addString("name", what.name).ok
  }
}

class TestPersonCreatedRecomposer extends DomainEventRecomposer[TestPersonCreated] with HasNoAlternativeRiftDescriptors {
  val riftDescriptor = RiftDescriptor(classOf[TestPersonCreated])
  override def extractEventParams(from: Extractor, header: DomainEventHeader): AlmValidation[TestPersonCreated] = {
    from.getString("name").map(TestPersonCreated(header, _))
  }
}

class TestPersonNameChangedDecomposer extends DomainEventDecomposer[TestPersonNameChanged] with HasNoAlternativeRiftDescriptors {
  val riftDescriptor = RiftDescriptor(classOf[TestPersonNameChanged])
  override def addEventParams[TDimension <: RiftDimension](what: TestPersonNameChanged, into: WarpSequencer[TDimension]): AlmValidation[WarpSequencer[TDimension]] = {
    into
      .addString("newName", what.newName)
      .addDateTime("timestamp", what.timestamp).ok
  }
}

class TestPersonNameChangedRecomposer extends DomainEventRecomposer[TestPersonNameChanged] with HasNoAlternativeRiftDescriptors {
  val riftDescriptor = RiftDescriptor(classOf[TestPersonNameChanged])
  override def extractEventParams(from: Extractor, header: DomainEventHeader): AlmValidation[TestPersonNameChanged] = {
    from.getString("newName").map(TestPersonNameChanged(header, _))
  }
}

class TestPersonAddressAquiredDecomposer extends DomainEventDecomposer[TestPersonAddressAquired] with HasNoAlternativeRiftDescriptors {
  val riftDescriptor = RiftDescriptor(classOf[TestPersonAddressAquired])
  override def addEventParams[TDimension <: RiftDimension](what: TestPersonAddressAquired, into: WarpSequencer[TDimension]): AlmValidation[WarpSequencer[TDimension]] = {
    into
      .addString("aquiredAddress", what.aquiredAddress)
      .addDateTime("timestamp", what.timestamp).ok
  }
}

class TestPersonAddressAquiredRecomposer extends DomainEventRecomposer[TestPersonAddressAquired] with HasNoAlternativeRiftDescriptors {
  val riftDescriptor = RiftDescriptor(classOf[TestPersonAddressAquired])
  override def extractEventParams(from: Extractor, header: DomainEventHeader): AlmValidation[TestPersonAddressAquired] = {
    from.getString("aquiredAddress").map(TestPersonAddressAquired(header, _))
  }
}

class TestPersonMovedDecomposer extends DomainEventDecomposer[TestPersonMoved] with HasNoAlternativeRiftDescriptors {
  val riftDescriptor = RiftDescriptor(classOf[TestPersonMoved])
  override def addEventParams[TDimension <: RiftDimension](what: TestPersonMoved, into: WarpSequencer[TDimension]): AlmValidation[WarpSequencer[TDimension]] = {
    into
      .addString("newAddress", what.newAddress)
      .addDateTime("timestamp", what.timestamp).ok
  }
}

class TestPersonMovedRecomposer extends DomainEventRecomposer[TestPersonMoved] with HasNoAlternativeRiftDescriptors {
  val riftDescriptor = RiftDescriptor(classOf[TestPersonMoved])
  override def extractEventParams(from: Extractor, header: DomainEventHeader): AlmValidation[TestPersonMoved] = {
    from.getString("newAddress").map(str => TestPersonMoved(header, str))
  }
}

class TestPersonUnhandledEventDecomposer extends DomainEventDecomposer[TestPersonUnhandledEvent] with HasNoAlternativeRiftDescriptors {
  val riftDescriptor = RiftDescriptor(classOf[TestPersonUnhandledEvent])
  override def addEventParams[TDimension <: RiftDimension](what: TestPersonUnhandledEvent, into: WarpSequencer[TDimension]): AlmValidation[WarpSequencer[TDimension]] = {
    into.ok
  }
}

class TestPersonUnhandledEventRecomposer extends DomainEventRecomposer[TestPersonUnhandledEvent] with HasNoAlternativeRiftDescriptors {
  val riftDescriptor = RiftDescriptor(classOf[TestPersonUnhandledEvent])
  override def extractEventParams(from: Extractor, header: DomainEventHeader): AlmValidation[TestPersonUnhandledEvent] = {
    TestPersonUnhandledEvent(header).success
  }
}