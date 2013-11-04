//package riftwarp
//
//import almhirt.common._
//import almhirt.almvalidation.kit._
//import scala.reflect.ClassTag
//
//trait WarpDimension[T] {
//  def channel: WarpChannel
//  def cast(what: Any): AlmValidation[T] = inTryCatch { what.asInstanceOf[T] }
//}
//
//object WarpDimension {
//  def apply[T: ClassTag](theChannel: WarpChannel) = new WarpDimension[T] {
//    val channel = theChannel
//    def cast(what: Any): AlmValidation[T] = what.castTo[T]
//  }
//}
//
//object WarpDimensions {
////  def forChannel(channel: WarpChannel): AlmValidation
//}
