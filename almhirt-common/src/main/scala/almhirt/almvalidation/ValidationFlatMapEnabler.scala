package almhirt.almvalidation

import almhirt._

trait ValidationFlatMapEnabler {
  implicit def almValidation2FlatMapW[T](validation: AlmValidation[T]) = new AlmValidationFlatMapW(validation)
  final class AlmValidationFlatMapW[T](validation: AlmValidation[T]) {
    def flatMap[U](f: T => AlmValidation[U]) =
      validation.bind(f)
  }
}
