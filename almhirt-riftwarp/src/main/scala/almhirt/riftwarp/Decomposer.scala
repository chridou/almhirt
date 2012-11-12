package almhirt.riftwarp

/** instance -> Atoms 
 */
trait Decomposer[T] {
  def decompose(what: T, into: Dematerializer): Unit
}