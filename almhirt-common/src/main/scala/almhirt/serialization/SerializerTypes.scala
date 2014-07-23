package almhirt.serialization

trait SerializesToString[-TIn] extends Serializes[TIn, String]
trait DeserializesFromString[+TOut] extends Deserializes[String, TOut]

trait SerializesToBytes[-TIn] extends Serializes[TIn, Array[Byte]]
trait DeserializesFromBytes[+TOut] extends Deserializes[Array[Byte], TOut]