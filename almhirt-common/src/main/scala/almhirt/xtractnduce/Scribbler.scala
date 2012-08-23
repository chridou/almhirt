package almhirt.xtractnduce

/** Use as a type class */
trait Scribbler[A] {
  def scribble(a: A): NDuceScript
}