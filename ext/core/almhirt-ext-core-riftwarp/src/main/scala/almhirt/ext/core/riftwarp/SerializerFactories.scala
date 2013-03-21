package almhirt.ext.core.riftwarp

import almhirt.common.AlmValidation
import almhirt.core.Almhirt
import almhirt.serializing._


class RiftWarpEventToStringSerializerFactory(theAlmhirt: Almhirt) extends EventToStringSerializerFactory{
  override def createSerializer: AlmValidation[EventToStringSerializer] =
    ???
}

class RiftWarpEventToBinarySerializerFactory(theAlmhirt: Almhirt) extends EventToBinarySerializerFactory {
  override def createSerializer: AlmValidation[EventToBinarySerializer] =
    ???
}

class RiftWarpDomainEventToStringSerializerFactory(theAlmhirt: Almhirt) extends DomainEventToStringSerializerFactory {
  override def createSerializer: AlmValidation[DomainEventToStringSerializer] =
    ???
}

class RiftWarpDomainEventToBinarySerializerFactory(theAlmhirt: Almhirt) extends DomainEventToBinarySerializerFactory {
  override def createSerializer: AlmValidation[DomainEventToBinarySerializer] =
    ???
}