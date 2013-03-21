package almhirt.serialization

import almhirt.common.AlmValidation

trait SerializerToStringFactory {
  def createSerializer: AlmValidation[CanSerializeAndDeserialize[AnyRef, AnyRef] with WorksWithStringRepresentation]
}

trait SerializerToBinaryFactory {
  def createSerializer: AlmValidation[CanSerializeAndDeserialize[AnyRef, AnyRef] with WorksWithBinaryRepresentation]
}