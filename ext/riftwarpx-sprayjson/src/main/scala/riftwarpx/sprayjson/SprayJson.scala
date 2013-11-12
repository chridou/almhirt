package riftwarpx.sprayjson

import riftwarp.RiftWarp
import riftwarp.WarpTags
import spray.json._

object SprayJson {
  def addToRiftWarp(riftWarp: RiftWarp): RiftWarp = {
    val rematerializers = riftWarp.rematerializers
    rematerializers.add(FromJsonStringRematerializer)
    rematerializers.add(FromSprayJsonRematerializer)
    riftWarp
  }
}