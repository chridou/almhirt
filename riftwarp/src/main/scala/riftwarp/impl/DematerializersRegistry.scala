package riftwarp.impl

import scalaz.syntax.validation._
import almhirt.common._
import riftwarp._
import riftwarp.std._

class DematerializersRegistry extends Dematerializers {
  private val dematerializers = new _root_.java.util.concurrent.ConcurrentHashMap[(String, String), Dematerializer[Any]](32)

  override def add[T](dematerializer: Dematerializer[T]) {
    dematerializers.put((dematerializer.channel, dematerializer.dimension), dematerializer)
  }
    
  override def get(dimension: String, channel: String): AlmValidation[Dematerializer[Any]] =
    dematerializers.get((channel, dimension)) match {
      case null => NoSuchElementProblem(s"""No Dematerialzer found  found for channel "$channel" and dimension "$dimension"""").failure
      case x => x.success
    }

}

object DematerializersRegistry {
  def apply(): DematerializersRegistry = {
    val reg = new DematerializersRegistry()
    reg.add(ToJsonStringDematerializer)
    reg.add(ToNoisyXmlStringDematerializer)
    reg.add(ToHtmlStringDematerializer)
    reg
  }
  
  def empty: DematerializersRegistry = new DematerializersRegistry()
}