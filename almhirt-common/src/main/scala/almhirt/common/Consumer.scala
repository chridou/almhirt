package almhirt.common

trait Consumer[-T] {
  final def apply(that: T) { consume(that) }
  def consume(that: T)
}

object Consumer {
  def apply[T](swallow: T => Unit): Consumer[T] = new Consumer[T] {
    def consume(that: T) { swallow(that) }
  }
}