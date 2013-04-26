import scalaz.syntax.validation._
import almhirt.common._

package object riftwarp {
//  implicit def string2DimensionString(str: String): DimensionString = DimensionString(str)
//  implicit def cord2DimensionCord(cord: scalaz.Cord): DimensionCord = DimensionCord(cord)
//  implicit def arrayByte2DimensionBinary(array: Array[Byte]): DimensionBinary = DimensionBinary(array)
  
  object funs {
    import riftwarp.components._
    object hasRecomposers extends HasRecomposersFuns 
  }
  
  object inst extends CanBuildFroms
}