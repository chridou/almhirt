package almhirt.context

trait HasAlmhirtContext {
  implicit def almhirtContext: AlmhirtContext
}