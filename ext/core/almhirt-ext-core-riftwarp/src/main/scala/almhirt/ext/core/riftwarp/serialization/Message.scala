package almhirt.ext.core.riftwarp.serialization

import scalaz._
import scalaz.Scalaz._
import scalaz.syntax.validation
import almhirt.common._
import almhirt.almvalidation.kit._
import riftwarp._
import almhirt.messaging._
import almhirt.messaging.MessageGrouping

object MessageGroupingDecomposer extends Decomposer[MessageGrouping] {
  val riftDescriptor = RiftDescriptor(classOf[MessageGrouping])
  val alternativeRiftDescriptors = Nil
  def decompose[TDimension <: RiftDimension](what: MessageGrouping, into: Dematerializer[TDimension]): AlmValidation[Dematerializer[TDimension]] = {
    into
      .addRiftDescriptor(this.riftDescriptor)
      .addUuid("groupId", what.groupId)
      .addInt("seq", what.seq)
      .addBoolean("isLast", what.isLast).ok
  }
}

object MessageGroupingRecomposer extends Recomposer[MessageGrouping] {
  val riftDescriptor = RiftDescriptor(classOf[MessageGrouping])
  val alternativeRiftDescriptors = Nil
  def recompose(from: Rematerializer): AlmValidation[MessageGrouping] = {
    (from.getUuid("groupId").toAgg |@|
      from.getInt("seq").toAgg |@|
      from.getBoolean("isLast").toAgg)(MessageGrouping.apply)
  }
}

object MessageHeaderDecomposer extends Decomposer[MessageHeader] {
  val riftDescriptor = RiftDescriptor(classOf[MessageHeader])
  val alternativeRiftDescriptors = Nil
  def decompose[TDimension <: RiftDimension](what: MessageHeader, into: Dematerializer[TDimension]): AlmValidation[Dematerializer[TDimension]] = {
    into
      .addRiftDescriptor(this.riftDescriptor)
      .addUuid("id", what.id)
      .addOptionalComplex("grouping", what.grouping, None).flatMap(
        _.addMapLiberate("metaData", what.metaData, None).map(
          _.addDateTime("timestamp", what.timestamp)))
  }
}

object MessageHeaderRecomposer extends Recomposer[MessageHeader] {
  val riftDescriptor = RiftDescriptor(classOf[MessageHeader])
  val alternativeRiftDescriptors = Nil
  def recompose(from: Rematerializer): AlmValidation[MessageHeader] = {
    (from.getUuid("id").toAgg |@|
      from.tryGetComplexType("grouping").toAgg |@|
      from.getMap[String, Object]("metaData").toAgg |@|
      from.getDateTime("timestamp").toAgg)(MessageHeader.apply)
  }
}

object MessageDecomposer extends Decomposer[Message[AnyRef]] {
  val riftDescriptor = RiftDescriptor(classOf[Message[AnyRef]])
  val alternativeRiftDescriptors = Nil
  def decompose[TDimension <: RiftDimension](what: Message[AnyRef], into: Dematerializer[TDimension]): AlmValidation[Dematerializer[TDimension]] = {
    into
      .addRiftDescriptor(this.riftDescriptor)
      .addComplex("header", what.header, None).flatMap(
        _.addComplex[AnyRef]("payload", what.payload, None))
  }
}

object MessageRecomposer extends Recomposer[Message[AnyRef]] {
  val riftDescriptor = RiftDescriptor(classOf[Message[AnyRef]])
  val alternativeRiftDescriptors = Nil
  def recompose(from: Rematerializer): AlmValidation[Message[AnyRef]] = {
    val header = from.getComplexType[MessageHeader]("header").toAgg
    val payload = from.getComplexType[AnyRef]("payload").toAgg
    (header |@| payload)(Message(_, _))
  }
}