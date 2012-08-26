package almhirt

/** Ask the implementor to release its resources */
trait Disposable {
  def dispose(): Unit
}