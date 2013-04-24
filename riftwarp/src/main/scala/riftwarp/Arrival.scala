package riftwarp

import almhirt.common._

trait Arrival[T] {
  def checkOut(traveller: WarpTraveller): AlmValidation[T]
}