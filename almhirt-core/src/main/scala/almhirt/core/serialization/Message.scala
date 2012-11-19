package almhirt.core.serialization

import scalaz._, Scalaz._
import scalaz.syntax.validation._
import almhirt.common._
import almhirt.almvalidation.kit._
import almhirt.riftwarp._
import almhirt.messaging._

class MessageGroupingDecomposer extends Decomposer[MessageGrouping] {
  val typeDescriptor = TypeDescriptor(classOf[MessageGrouping], 1)
  def decompose(grouping: MessageGrouping)(implicit into: DematerializationFunnel): AlmValidation[DematerializationFunnel] = {
    into
      .addTypeDescriptor(this.typeDescriptor)
      .bind(_.addUuid("groupId", grouping.groupId))
      .bind(_.addInt("seq", grouping.seq))
      .bind(_.addBoolean("isLast", grouping.isLast))
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
  def decompose(header: MessageHeader)(implicit into: DematerializationFunnel): AlmValidation[DematerializationFunnel] = {
    into
      .addTypeDescriptor(this.typeDescriptor)
      .bind(_.addUuid("id", header.id))
      .bind(_.addOptionalComplexType("grouping", header.grouping))
      //.bind(_.addOptionalComplexType("metaData", header.metaData))
      .bind(_.addDateTime("timestamp", header.timestamp))
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
  def decompose(message: Message[AnyRef])(implicit into: DematerializationFunnel): AlmValidation[DematerializationFunnel] = {
    into
      .addTypeDescriptor(this.typeDescriptor)
      .bind(_.addComplexType("header", message.header))
      .bind(_.addComplexType("payload", message.payload))
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