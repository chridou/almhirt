package almhirt.context

import almhirt._

trait AlmhirtComponent {
  implicit def almhirtContext: AlmhirtContext
  
  def startAlmhirt() = almhirtContext.asInstanceOf[AlmhirtContextImpl].start
  def stopAlmhirt() = almhirtContext.asInstanceOf[AlmhirtContextImpl].stop

  trait AlmhirtContextImpl extends AlmhirtContext {
    def start() { }
    def stop(){ actorSystem.shutdown }
  }
}