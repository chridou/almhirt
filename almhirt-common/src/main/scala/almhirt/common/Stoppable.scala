package almhirt.common

trait Stoppable {
  final def apply() { stop() }
  def stop()
}