package riftwarp.util

//import scala.reflect.ClassTag
//import almhirt.serialization.StringBasedSerializer
//import riftwarp.RiftWarp
//import almhirt.serialization.BinaryBasedSerializer
//
//object Serializers {
//  def createForStrings[TIn, TOut](riftWarp: RiftWarp)(implicit tag: ClassTag[TOut]): StringBasedSerializer[TIn, TOut] = {
//    val serializer = new WarpSerializerToString[TIn](riftWarp)
//    val deserializer = new WarpDeserializerFromString[TOut](riftWarp)
//    new StringBasedSerializer[TIn, TOut] {
//      def serialize(channel: String)(what: TIn, options: Map[String, Any] = Map.empty) = serializer.serialize(channel)(what, options)
//      def deserialize(channel: String)(what: SerializedRepr, options: Map[String, Any] = Map.empty) = deserializer.deserialize(channel)(what, options)
//    }
//  }
//  
//  def createSpecificForStrings[T](riftWarp: RiftWarp)(implicit tag: ClassTag[T]): StringBasedSerializer[T, T] = 
//    createForStrings[T, T](riftWarp)
//    
//    
//  def createForBinary[TIn, TOut](riftWarp: RiftWarp)(implicit tag: ClassTag[TOut]): BinaryBasedSerializer[TIn, TOut] = {
//    val serializer = new BinaryWarpSerializer[TIn](riftWarp)
//    val deserializer = new WarpDeserializerFromBinary[TOut](riftWarp)
//    new BinaryBasedSerializer[TIn, TOut] {
//      def serialize(channel: String)(what: TIn, options: Map[String, Any] = Map.empty) = serializer.serialize(channel)(what, options)
//      def deserialize(channel: String)(what: SerializedRepr, options: Map[String, Any] = Map.empty) = deserializer.deserialize(channel)(what, options)
//    }
//  }
//  
//  def createSpecificForBinary[T](riftWarp: RiftWarp)(implicit tag: ClassTag[T]): BinaryBasedSerializer[T, T] = 
//    createForBinary[T, T](riftWarp)
//    
//}