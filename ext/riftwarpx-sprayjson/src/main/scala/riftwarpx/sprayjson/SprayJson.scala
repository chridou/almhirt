package riftwarpx.sprayjson

import riftwarp.RiftWarp
import riftwarp.WarpTags
import spray.json._

object SprayJson {
  def addToRiftWarp(riftWarp: RiftWarp): RiftWarp = {
    val rematerializers = riftWarp.rematerializers
    rematerializers.addTyped("json", (what: String, options: Map[String, Any]) => FromJsonStringRematerializer.rematerialize(WarpTags.JsonString(what), options))
    rematerializers.addTyped("json", (what: JsValue, options: Map[String, Any]) => FromSprayJsonRematerializer.rematerialize(what, options))
    riftWarp
  }
}