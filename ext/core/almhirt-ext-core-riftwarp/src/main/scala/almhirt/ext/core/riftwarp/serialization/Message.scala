package almhirt.ext.core.riftwarp.serialization

import scalaz._
import scalaz.Scalaz._
import scalaz.syntax.validation
import almhirt.common._
import almhirt.almvalidation.kit._
import riftwarp._
import almhirt.messaging._
import almhirt.messaging.MessageGrouping

class MessageGroupingDecomposer extends Decomposer[MessageGrouping] {
  val typeDescriptor = TypeDescriptor(classOf[MessageGrouping], 1)
  def decompose[TDimension <: RiftDimension](what: MessageGrouping)(implicit into: Dematerializer[TDimension]): AlmValidation[Dematerializer[TDimension]] = {
    into
      .addTypeDescriptor(this.typeDescriptor)
      .flatMap(_.addUuid("groupId", what.groupId))
      .flatMap(_.addInt("seq", what.seq))
      .flatMap(_.addBoolean("isLast", what.isLast))
  }
}

class MessageGroupingRecomposer extends Recomposer[MessageGrouping] {
  val typeDescriptor = TypeDescriptor(classOf[MessageGrouping], 1)
  def recompose(from: Rematerializer): AlmValidation[MessageGrouping] = {
    (from.getUuid("groupId").toAgg |@|
      from.getInt("seq").toAgg |@|
      from.getBoolean("isLast").toAgg)(MessageGrouping.apply)
  }
}

class MessageHeaderDecomposer extends Decomposer[MessageHeader] {
  val typeDescriptor = TypeDescriptor(classOf[MessageHeader], 1)
  def decompose[TDimension <: RiftDimension](what: MessageHeader)(implicit into: Dematerializer[TDimension]): AlmValidation[Dematerializer[TDimension]] = {
    into
      .addTypeDescriptor(this.typeDescriptor)
      .flatMap(_.addUuid("id", what.id))
      .flatMap(_.addOptionalComplexType("grouping", what.grouping))
      //.bind(_.addOptionalComplexType("metaData", header.metaData))
      .flatMap(_.addDateTime("timestamp", what.timestamp))
  }
}

class MessageHeaderRecomposer extends Recomposer[MessageHeader] {
  val typeDescriptor = TypeDescriptor(classOf[MessageHeader], 1)
  def recompose(from: Rematerializer): AlmValidation[MessageHeader] = {
    (from.getUuid("id").toAgg |@|
      from.tryGetComplexType("grouping").toAgg |@|
      //from.getOptionalComplexType("metaData").toAgg |@| 
      Map.empty[String, String].success[Problem].toAgg |@|
      from.getDateTime("timestamp").toAgg)(MessageHeader.apply)
  }
}

class MessageDecomposer extends Decomposer[Message[AnyRef]] {
  val typeDescriptor = TypeDescriptor(classOf[Message[AnyRef]], 1)
  def decompose[TDimension <: RiftDimension](what: Message[AnyRef])(implicit into: Dematerializer[TDimension]): AlmValidation[Dematerializer[TDimension]] = {
    into
      .addTypeDescriptor(this.typeDescriptor)
      .flatMap(_.addComplexType("header", what.header))
      .flatMap(_.addComplexType("payload", what.payload))
  }
}

class MessageRecomposer extends Recomposer[Message[AnyRef]] {
  val typeDescriptor = TypeDescriptor(classOf[Message[AnyRef]], 1)
  def recompose(from: Rematerializer): AlmValidation[Message[AnyRef]] = {
    val header = from.getComplexType[MessageHeader]("header").toAgg
    val payload = from.getComplexType[AnyRef]("payload").toAgg
    (header |@| payload)(Message(_,_))
  }
}