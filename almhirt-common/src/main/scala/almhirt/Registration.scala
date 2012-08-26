package almhirt

import java.util.UUID

/** An object that when disposed cancels the assigned registration */
trait RegistrationHolder extends Disposable

/** An registration with its ticket that when disposed cancels the assigned registration 
 * 
 * @tparam T The type of the ticket
 */
trait Registration[T] extends RegistrationHolder {
  /** The registration's ticket */
  def ticket: T
}