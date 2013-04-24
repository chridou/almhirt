package riftwarp

import almhirt.common._

trait Departures extends Function1[RiftDescriptor, AlmValidation[BlindDeparture]]

trait BlindDeparture {
  def blindCheckIn(what: Any)(implicit lookup: Departures): AlmValidation[WarpPackage]
}

trait Departure[T] extends BlindDeparture {
  def checkIn(what: T)(implicit lookup: Departures): AlmValidation[WarpPackage]
  override final def blindCheckIn(what: Any)(implicit lookup: Departures): AlmValidation[WarpPackage] = checkIn(what.asInstanceOf[T])
}