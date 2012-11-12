package almhirt.riftwarp

import almhirt.common._

/** atoms -> instance */
trait Recomposer[T] {
  def recompose(from: Rematerializer): AlmValidation[T]
}