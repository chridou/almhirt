package almhirt

import java.util.UUID

trait RegistrationHolder extends Disposable

trait Registration[T] extends RegistrationHolder {
  def ticket: T
}