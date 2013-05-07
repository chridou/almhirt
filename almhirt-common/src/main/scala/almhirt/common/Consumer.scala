package almhirt.common

trait Consumer[-T] {
  final def apply(that: T) { consume(that) }
  def consume(that: T)
}