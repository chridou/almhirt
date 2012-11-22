package almhirt.core.serialization

import scalaz._, Scalaz._
import scalaz.syntax.validation._
import almhirt.common._
import almhirt.almvalidation.kit._
import almhirt.riftwarp._
import almhirt.messaging._

class MessageGroupingDecomposer extends Decomposer[MessageGrouping] {
  val typeDescriptor = TypeDescriptor(classOf[MessageGrouping], 1)
  def decompose[TDimension <: RiftTypedDimension[_], TChannel <: RiftChannelDescriptor](what: MessageGrouping)(implicit into: Dematerializer[TDimension, TChannel]): AlmValidation[Dematerializer[TDimension, TChannel]] = {
    into
      .addTypeDescriptor(this.typeDescriptor)
      .bind(_.addUuid("groupId", what.groupId))
      .bind(_.addInt("seq", what.seq))
      .bind(_.addBoolean("isLast", what.isLast))
  }
}

class MessageGroupingRecomposer extends Recomposer[MessageGrouping] {
  val typeDescriptor = TypeDescriptor(classOf[MessageGrouping], 1)
  def recompose(from: RematerializationArray): AlmValidation[MessageGrouping] = {
    (from.getUuid("groupId").toAgg |@|
      from.getInt("seq").toAgg |@|
      from.getBoolean("isLast").toAgg)(MessageGrouping.apply)
  }
}

class MessageHeaderDecomposer extends Decomposer[MessageHeader] {
  val typeDescriptor = TypeDescriptor(classOf[MessageHeader], 1)
  def decompose[TDimension <: RiftTypedDimension[_], TChannel <: RiftChannelDescriptor](what: MessageHeader)(implicit into: Dematerializer[TDimension, TChannel]): AlmValidation[Dematerializer[TDimension, TChannel]] = {
    into
      .addTypeDescriptor(this.typeDescriptor)
      .bind(_.addUuid("id", what.id))
      .bind(_.addOptionalComplexType("grouping", what.grouping))
      //.bind(_.addOptionalComplexType("metaData", header.metaData))
      .bind(_.addDateTime("timestamp", what.timestamp))
  }
}

class MessageHeaderRecomposer extends Recomposer[MessageHeader] {
  val typeDescriptor = TypeDescriptor(classOf[MessageHeader], 1)
  def recompose(from: RematerializationArray): AlmValidation[MessageHeader] = {
    (from.getUuid("id").toAgg |@|
      from.tryGetComplexType("grouping").toAgg |@|
      //from.getOptionalComplexType("metaData").toAgg |@| 
      Map.empty[String, String].success[Problem].toAgg |@|
      from.getDateTime("timestamp").toAgg)(MessageHeader.apply)
  }
}

class MessageDecomposer extends Decomposer[Message[AnyRef]] {
  val typeDescriptor = TypeDescriptor(classOf[Message[AnyRef]], 1)
  def decompose[TDimension <: RiftTypedDimension[_], TChannel <: RiftChannelDescriptor](what: Message[AnyRef])(implicit into: Dematerializer[TDimension, TChannel]): AlmValidation[Dematerializer[TDimension, TChannel]] = {
    into
      .addTypeDescriptor(this.typeDescriptor)
      .bind(_.addComplexType("header", what.header))
      .bind(_.addComplexType("payload", what.payload))
  }
}

class MessageRecomposer extends Recomposer[Message[AnyRef]] {
  val typeDescriptor = TypeDescriptor(classOf[Message[AnyRef]], 1)
  def recompose(from: RematerializationArray): AlmValidation[Message[AnyRef]] = {
    val header = from.getComplexType[MessageHeader]("header").toAgg
    val payload = from.getComplexType[AnyRef]("payload").toAgg
    (header |@| payload)(Message(_,_))
  }
}