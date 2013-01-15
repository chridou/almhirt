package riftwarp.http

sealed trait RiftHttpBodyType
object RiftBinaryBodyType extends RiftHttpBodyType
object RiftStringBodyType extends RiftHttpBodyType
