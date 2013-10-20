package riftwarp.impl


import scalaz._, Scalaz._
import almhirt.common._
import riftwarp._
import riftwarp.std._

class RematerializersRegistry extends Rematerializers {
  private val rematerializers = new _root_.java.util.concurrent.ConcurrentHashMap[(String, String), (Any, Map[String, Any]) => AlmValidation[WarpPackage]](32)

  override def add(dimension: String, channel: String, rematerialize: (Any, Map[String, Any]) => AlmValidation[WarpPackage]) {
    rematerializers.put((channel, dimension), rematerialize)
  }
    
  override def get(dimension: String, channel: String): AlmValidation[(Any, Map[String, Any]) => AlmValidation[WarpPackage]] =
    rematerializers.get((channel, dimension)) match {
      case null => NoSuchElementProblem(s"""No Rematerialzer found  found for channel "$channel" and dimension "$dimension"""").failure
      case x => x.success
    }

}

object RematerializersRegistry {
  def apply(): RematerializersRegistry = {
    val messagePackRematerializer = new messagepack.FromMessagePackByteArrayRematerializer{}
    val reg = new RematerializersRegistry()
    reg.addTyped("json", (what: String, options: Map[String, Any]) => FromJsonStringRematerializer.rematerialize(WarpTags.JsonString(what), options))
    reg.addTyped("json", (what: scalaz.Cord, options: Map[String, Any]) => FromJsonCordRematerializer.rematerialize(WarpTags.JsonCord(what), options))
    reg.addTyped("xml", (what: String, options: Map[String, Any]) => FromXmlStringRematerializer.rematerialize(WarpTags.XmlString(what), options))
    reg.addTyped("warppackage", (what: WarpPackage, options: Map[String, Any]) => FromWarpPackageRematerializer.rematerialize(what, options))
    reg.addTyped("msgpack", (what: Array[Byte], options: Map[String, Any]) => messagePackRematerializer.rematerialize(WarpTags.MessagePack(what), options))
    reg
  }
  
  def empty: RematerializersRegistry = new RematerializersRegistry()
}