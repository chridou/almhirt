package almhirt.xtractnduce

/** Implementors know how to create an [[almhirt.xtractnduce.NDuceScript]] themselves */
trait Scribbles {
  /** Creates an [almhirt.xtractnduce.NDuceScript]] from this instance */
  def scribble(): NDuceScript
}