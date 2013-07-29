package almhirt.testkit.testevents

import almhirt.common._

trait TestEvent extends Event {
  override def changeMetadata(newMetaData: Map[String, String]): TestEvent
}

final case class TestEvent1(header: EventHeader) extends TestEvent {
  override def changeMetadata(newMetaData: Map[String, String]): TestEvent1 = copy(header = this.header.changeMetadata(newMetaData))
}

final case class TestEvent2(header: EventHeader, aValue: Int) extends TestEvent {
  override def changeMetadata(newMetaData: Map[String, String]): TestEvent2 = copy(header = this.header.changeMetadata(newMetaData))
}

final case class TestEvent3(header: EventHeader, values: Vector[Int]) extends TestEvent {
  override def changeMetadata(newMetaData: Map[String, String]): TestEvent3 = copy(header = this.header.changeMetadata(newMetaData))
}

object Serialization {
  import scalaz.syntax.validation._
  import riftwarp._
  import riftwarp.std.kit._
  import riftwarp.std.WarpObjectLookUp
  import riftwarp.serialization.common.EventWarpPackagingTemplate

  implicit object TestEvent1WarpPackaging extends EventWarpPackagingTemplate[TestEvent1] with RegisterableWarpPacker {
    override val warpDescriptor = WarpDescriptor(classOf[TestEvent1].getSimpleName())
    override val alternativeWarpDescriptors = WarpDescriptor(classOf[TestEvent1]) :: Nil
    override def addEventParams(what: TestEvent1, into: WarpObject)(implicit packers: WarpPackers): AlmValidation[WarpPackage] =
      into.success

    override def extractEventParams(from: WarpObjectLookUp, header: EventHeader)(implicit unpackers: WarpUnpackers): AlmValidation[TestEvent1] =
      TestEvent1(header).success
  }

  implicit object TestEvent2WarpPackaging extends EventWarpPackagingTemplate[TestEvent2] with RegisterableWarpPacker {
    override val warpDescriptor = WarpDescriptor(classOf[TestEvent2].getSimpleName())
    override val alternativeWarpDescriptors = WarpDescriptor(classOf[TestEvent2]) :: Nil
    override def addEventParams(what: TestEvent2, into: WarpObject)(implicit packers: WarpPackers): AlmValidation[WarpPackage] =
      into ~>
        P("aValue", what.aValue)

    override def extractEventParams(from: WarpObjectLookUp, header: EventHeader)(implicit unpackers: WarpUnpackers): AlmValidation[TestEvent2] =
      for {
        aValue <- from.getAs[Int]("aValue")
      } yield TestEvent2(header, aValue)
  }

  implicit object TestEvent3WarpPackaging extends EventWarpPackagingTemplate[TestEvent3] with RegisterableWarpPacker {
    override val warpDescriptor = WarpDescriptor(classOf[TestEvent3].getSimpleName())
    override val alternativeWarpDescriptors = WarpDescriptor(classOf[TestEvent3]) :: Nil
    override def addEventParams(what: TestEvent3, into: WarpObject)(implicit packers: WarpPackers): AlmValidation[WarpPackage] =
      into ~>
        CP("values", what.values)

    override def extractEventParams(from: WarpObjectLookUp, header: EventHeader)(implicit unpackers: WarpUnpackers): AlmValidation[TestEvent3] =
      for {
        values <- from.getPrimitives[Int]("values")
      } yield TestEvent3(header, values)
  }

  def addTestEventSerializers(to: RiftWarp): RiftWarp = {
    List(TestEvent1WarpPackaging,
      TestEvent2WarpPackaging, TestEvent3WarpPackaging).foreach { packaging =>
        to.packers.add(packaging)
        to.unpackers.add(packaging)
      }
    to
  }
}