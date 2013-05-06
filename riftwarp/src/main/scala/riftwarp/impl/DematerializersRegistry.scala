package riftwarp.impl

import scalaz.syntax.validation._
import almhirt.common._
import riftwarp._
import riftwarp.std._

class DematerializersRegistry extends Dematerializers {
  private val dematerializers = new _root_.java.util.concurrent.ConcurrentHashMap[(String, String), (WarpPackage, Map[String, Any]) => Any](32)

  override def add(dimension: String, channel: String, dematerialize: (WarpPackage, Map[String, Any]) => Any) {
    dematerializers.put((channel, dimension), dematerialize)
  }
    
  override def get(dimension: String, channel: String): AlmValidation[(WarpPackage, Map[String, Any]) => Any] =
    dematerializers.get((channel, dimension)) match {
      case null => KeyNotFoundProblem(s"""No Dematerialzer found  found for channel "$channel" and dimension "$dimension"""").failure
      case x => x.success
    }

}

object DematerializersRegistry {
  def apply(): DematerializersRegistry = {
    val reg = new DematerializersRegistry()
    reg.addTyped("json", (what: WarpPackage, options: Map[String, Any]) => ToJsonStringDematerializer.dematerialize(what, options))
    reg.addTyped("xml", (what: WarpPackage, options: Map[String, Any]) => ToNoisyXmlStringDematerializer.dematerialize(what, options))
    reg
  }
  
  def empty: DematerializersRegistry = new DematerializersRegistry()
}