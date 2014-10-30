package almhirt.almvalidation

import scala.reflect.ClassTag
import scalaz.syntax.validation._
import almhirt.common._

object CastHelper {
  /** Taken from scala.concurrent, unfortunately its a private val there
   * 
   */
  private[almvalidation] val toBoxed = Map[Class[_], Class[_]](
    classOf[Boolean] → classOf[java.lang.Boolean],
    classOf[Byte] → classOf[java.lang.Byte],
    classOf[Char] → classOf[java.lang.Character],
    classOf[Short] → classOf[java.lang.Short],
    classOf[Int] → classOf[java.lang.Integer],
    classOf[Long] → classOf[java.lang.Long],
    classOf[Float] → classOf[java.lang.Float],
    classOf[Double] → classOf[java.lang.Double],
    classOf[Unit] → classOf[scala.runtime.BoxedUnit])

}

trait AlmValidationCastFunctions {
  /** A cast with runtime safety
   * 
   * Includes code from scala.concurrent.Future
   */
  def almCast[To](what: Any)(implicit tag: ClassTag[To]): AlmValidation[To] = {
	// Taken from scala.concurrent.Future.mapTo
    def boxedType(c: Class[_]): Class[_] = {
      if (c.isPrimitive) CastHelper.toBoxed(c) else c
    }
    try {
      scalaz.Success(boxedType(tag.runtimeClass).cast(what).asInstanceOf[To])
    } catch {
      case exn: ClassCastException ⇒ InvalidCastProblem(s"""I cannot cast from "${what.getClass.getName()}" to "${tag.runtimeClass.getName()}"""", cause = Some(exn)).failure
    }
  }
}