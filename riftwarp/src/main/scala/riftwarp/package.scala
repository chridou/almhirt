import scalaz.syntax.validation._
import almhirt.common._

package object riftwarp {
  type BlobDivert = (Array[Byte], RiftBlobIdentifier) => AlmValidation[RiftBlob] 
  type BlobFetch = (RiftBlob) => AlmValidation[Array[Byte]] 

  
  val NoDivertBlobDivert: BlobDivert = (arr: Array[Byte], path: RiftBlobIdentifier) => RiftBlobArrayValue(arr).success
  val NoFetchBlobFetch: BlobFetch = {
    case RiftBlobArrayValue(arr) => arr.success 
    case x => UnspecifiedProblem("Could not fetch the blob's byte array. This is a standard function which can only fetch byte arrays from RiftBlobArrayValue. Please specify your own function to retrieve blob data. The unsupprted type was: %s".format(x)).failure
  }
  
  import language.implicitConversions
  
  implicit def string2TypeDescriptor(descriptor: String): TypeDescriptor = TypeDescriptor(descriptor)
  implicit def class2TypeDescriptor(clazz: Class[_]): TypeDescriptor = TypeDescriptor(clazz.getName())
  implicit def uuid2TypeDescriptor(uuid: java.util.UUID): TypeDescriptor = TypeDescriptor(uuid.toString())
  implicit def long2TypeDescriptor(id: Long): TypeDescriptor = TypeDescriptor(id.toString())
  
  implicit def string2DimensionString(str: String): DimensionString = DimensionString(str)
  implicit def cord2DimensionCord(cord: scalaz.Cord): DimensionCord = DimensionCord(cord)
  implicit def arrayByte2DimensionBinary(array: Array[Byte]): DimensionBinary = DimensionBinary(array)
  
  object funs {
    import riftwarp.components._
    object hasRecomposers extends HasRecomposersFuns 
  }
}