package almhirt

import java.util.UUID

trait Registration[T] extends Disposable {
  def ticket: T
}