package riftwarp

import scala.reflect.ClassTag
import scalaz.std._
import scalaz.syntax.validation._
import almhirt.common._
import almhirt.almvalidation.kit._
import almhirt.serialization._

trait RiftWarp {
  def packers: WarpPackers
  def unpackers: WarpUnpackers
  
  def depart[T, U](what: U)
}

object RiftWarp {

}