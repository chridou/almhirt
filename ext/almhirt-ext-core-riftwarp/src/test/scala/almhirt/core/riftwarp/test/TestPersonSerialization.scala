package almhirt.core.riftwarp.test

import java.util.UUID
import org.joda.time.DateTime
import scalaz._
import scalaz.Scalaz._
import almhirt.common._
import almhirt.almvalidation.kit._
import almhirt.domain._
import riftwarp._
import riftwarp.std._
import riftwarp.std.kit._
import almhirt.core.test._
import almhirt.ext.core.riftwarp.serialization._

object TestPersonCreatedPacker extends DomainEventWarpPacker[TestPersonCreated] with RegisterableWarpPacker {
  val warpDescriptor = WarpDescriptor(classOf[TestPersonCreated])
  val alternativeWarpDescriptors = Nil
  override def addEventParams(what: TestPersonCreated, into: WarpObject)(implicit packers: WarpPackers): AlmValidation[WarpPackage] =
  	into ~> ("name" -> what.name)
}

object TestPersonCreatedWarpUnpacker extends DomainEventWarpUnpacker[TestPersonCreated] {
  val warpDescriptor = WarpDescriptor(classOf[TestPersonCreated])
  val alternativeWarpDescriptors = Nil
  def extractEventParams(from: WarpObjectLookUp, header: DomainEventHeader)(implicit unpackers: WarpUnpackers): AlmValidation[TestPersonCreated] = {
    from.getAs[String]("name").map(TestPersonCreated(header, _))
  }
}

object TestPersonNameChangedPacker extends DomainEventWarpPacker[TestPersonNameChanged] with RegisterableWarpPacker {
  val warpDescriptor = WarpDescriptor(classOf[TestPersonNameChanged])
  val alternativeWarpDescriptors = Nil
  override def addEventParams(what: TestPersonNameChanged, into: WarpObject)(implicit packers: WarpPackers): AlmValidation[WarpPackage] =
    into ~> ("newName", what.newName)
}

object TestPersonNameChangedWarpUnpacker extends DomainEventWarpUnpacker[TestPersonNameChanged] {
  val warpDescriptor = WarpDescriptor(classOf[TestPersonNameChanged])
  val alternativeWarpDescriptors = Nil
  def extractEventParams(from: WarpObjectLookUp, header: DomainEventHeader)(implicit unpackers: WarpUnpackers): AlmValidation[TestPersonNameChanged] = {
    from.getAs[String]("newName").map(TestPersonNameChanged(header, _))
  }
}

object TestPersonAddressAquiredPacker extends DomainEventWarpPacker[TestPersonAddressAquired] with RegisterableWarpPacker {
  val warpDescriptor = WarpDescriptor(classOf[TestPersonAddressAquired])
  val alternativeWarpDescriptors = Nil
  override def addEventParams(what: TestPersonAddressAquired, into: WarpObject)(implicit packers: WarpPackers): AlmValidation[WarpPackage] =
    into ~> ("aquiredAddress", what.aquiredAddress)
}

object TestPersonAddressAquiredWarpUnpacker extends DomainEventWarpUnpacker[TestPersonAddressAquired] {
  val warpDescriptor = WarpDescriptor(classOf[TestPersonAddressAquired])
  val alternativeWarpDescriptors = Nil
  def extractEventParams(from: WarpObjectLookUp, header: DomainEventHeader)(implicit unpackers: WarpUnpackers): AlmValidation[TestPersonAddressAquired] = {
    from.getAs[String]("aquiredAddress").map(TestPersonAddressAquired(header, _))
  }
}

object TestPersonMovedPacker extends DomainEventWarpPacker[TestPersonMoved] with RegisterableWarpPacker {
  val warpDescriptor = WarpDescriptor(classOf[TestPersonMoved])
  val alternativeWarpDescriptors = Nil
  override def addEventParams(what: TestPersonMoved, into: WarpObject)(implicit packers: WarpPackers): AlmValidation[WarpPackage] =
    into ~> ("newAddress", what.newAddress) ~> ("timestamp", what.timestamp)
}

object TestPersonMovedWarpUnpacker extends DomainEventWarpUnpacker[TestPersonMoved] {
  val warpDescriptor = WarpDescriptor(classOf[TestPersonMoved])
  val alternativeWarpDescriptors = Nil
  def extractEventParams(from: WarpObjectLookUp, header: DomainEventHeader)(implicit unpackers: WarpUnpackers): AlmValidation[TestPersonMoved] = {
    from.getAs[String]("newAddress").map(str => TestPersonMoved(header, str))
  }
}

object TestPersonUnhandledEventPacker extends DomainEventWarpPacker[TestPersonUnhandledEvent] with RegisterableWarpPacker {
  val warpDescriptor = WarpDescriptor(classOf[TestPersonUnhandledEvent])
  val alternativeWarpDescriptors = Nil
  override def addEventParams(what: TestPersonUnhandledEvent, into: WarpObject)(implicit packers: WarpPackers): AlmValidation[WarpPackage] =
    into.success
}

object TestPersonUnhandledEventWarpUnpacker extends DomainEventWarpUnpacker[TestPersonUnhandledEvent] {
  val warpDescriptor = WarpDescriptor(classOf[TestPersonUnhandledEvent])
  val alternativeWarpDescriptors = Nil
  def extractEventParams(from: WarpObjectLookUp, header: DomainEventHeader)(implicit unpackers: WarpUnpackers): AlmValidation[TestPersonUnhandledEvent] = {
    TestPersonUnhandledEvent(header).success
  }
}