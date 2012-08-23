package almhirt.xtractnduce

trait Scribbler[A] {
  def scribble(a: A): NDuceScript
}