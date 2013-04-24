package riftwarp

import almhirt.common._

trait Departures extends Function1[RiftDescriptor, AlmValidation[BlindDeparture]]

trait BlindDeparture {
  def blindCheckIn(what: Any)(implicit lookup: Departures): AlmValidation[WarpTraveller]
}

trait Departure[T] extends BlindDeparture {
  def checkIn(what: T)(implicit lookup: Departures): AlmValidation[WarpTraveller]
  override final def blindCheckIn(what: Any)(implicit lookup: Departures): AlmValidation[WarpTraveller] = checkIn(what.asInstanceOf[T])
}