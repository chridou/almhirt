package riftwarp.std

import scala.reflect.ClassTag
import scalaz._, Scalaz._
import almhirt.common._
import almhirt.almvalidation.kit._
import riftwarp._

trait PackageBuilderFuns {

  def toWarpPrimitive(what: Any): AlmValidation[WarpPrimitive] = {
    val clazz = what.getClass
    if (clazz == classOf[String])
      what.castTo[String].map(WarpString(_))
    else if (clazz == classOf[_root_.java.lang.String])
      what.castTo[String].map(WarpString(_))
    else if (clazz == classOf[Boolean])
      what.castTo[Boolean].map(WarpBoolean(_))
    else if (clazz == classOf[_root_.java.lang.Boolean])
      what.castTo[Boolean].map(WarpBoolean(_))
    else if (clazz == classOf[Byte])
      what.castTo[Byte].map(WarpByte(_))
    else if (clazz == classOf[_root_.java.lang.Byte])
      what.castTo[Byte].map(WarpByte(_))
    else if (clazz == classOf[Int])
      what.castTo[Int].map(WarpInt(_))
    else if (clazz == classOf[_root_.java.lang.Integer])
      what.castTo[Int].map(WarpInt(_))
    else if (clazz == classOf[Long])
      what.castTo[Long].map(WarpLong(_))
    else if (clazz == classOf[_root_.java.lang.Long])
      what.castTo[Long].map(WarpLong(_))
    else if (clazz == classOf[BigInt])
      what.castTo[BigInt].map(WarpBigInt(_))
    else if (clazz == classOf[Float])
      what.castTo[Float].map(WarpFloat(_))
    else if (clazz == classOf[_root_.java.lang.Float])
      what.castTo[Float].map(WarpFloat(_))
    else if (clazz == classOf[Double])
      what.castTo[Double].map(WarpDouble(_))
    else if (clazz == classOf[_root_.java.lang.Double])
      what.castTo[Double].map(WarpDouble(_))
    else if (clazz == classOf[BigDecimal])
      what.castTo[BigDecimal].map(WarpBigDecimal(_))
    else if (clazz == classOf[org.joda.time.DateTime])
      what.castTo[org.joda.time.DateTime].map(WarpDateTime(_))
    else if (clazz == classOf[_root_.java.util.UUID])
      what.castTo[_root_.java.util.UUID].map(WarpUuid(_))
    else if (clazz == classOf[_root_.java.net.URI])
      what.castTo[_root_.java.net.URI].map(WarpUri(_))
    else
      UnspecifiedProblem(s""""${clazz.getName()}" is not a primitive value""").failure
  }

  def getMapToPrimitive[T](implicit tag: ClassTag[T]): AlmValidation[T => WarpPrimitive] = {
    val clazz = tag.runtimeClass
    if (clazz == classOf[String])
      ((x: String) => WarpString(x)).asInstanceOf[T => WarpPrimitive].success
    else if (clazz == classOf[_root_.java.lang.String])
      ((x: String) => WarpString(x)).asInstanceOf[T => WarpPrimitive].success
    else if (clazz == classOf[Boolean])
      ((x: Boolean) => WarpBoolean(x)).asInstanceOf[T => WarpPrimitive].success
    else if (clazz == classOf[_root_.java.lang.Boolean])
      ((x: Boolean) => WarpBoolean(x)).asInstanceOf[T => WarpPrimitive].success
    else if (clazz == classOf[Byte])
      ((x: Byte) => WarpByte(x)).asInstanceOf[T => WarpPrimitive].success
    else if (clazz == classOf[_root_.java.lang.Byte])
      ((x: Byte) => WarpByte(x)).asInstanceOf[T => WarpPrimitive].success
    else if (clazz == classOf[Int])
      ((x: Int) => WarpInt(x)).asInstanceOf[T => WarpPrimitive].success
    else if (clazz == classOf[_root_.java.lang.Integer])
      ((x: Int) => WarpInt(x)).asInstanceOf[T => WarpPrimitive].success
    else if (clazz == classOf[Long])
      ((x: Long) => WarpLong(x)).asInstanceOf[T => WarpPrimitive].success
    else if (clazz == classOf[_root_.java.lang.Long])
      ((x: Long) => WarpLong(x)).asInstanceOf[T => WarpPrimitive].success
    else if (clazz == classOf[BigInt])
      ((x: BigInt) => WarpBigInt(x)).asInstanceOf[T => WarpPrimitive].success
    else if (clazz == classOf[Float])
      ((x: Float) => WarpFloat(x)).asInstanceOf[T => WarpPrimitive].success
    else if (clazz == classOf[_root_.java.lang.Float])
      ((x: Float) => WarpFloat(x)).asInstanceOf[T => WarpPrimitive].success
    else if (clazz == classOf[Double])
      ((x: Double) => WarpDouble(x)).asInstanceOf[T => WarpPrimitive].success
    else if (clazz == classOf[_root_.java.lang.Double])
      ((x: Double) => WarpDouble(x)).asInstanceOf[T => WarpPrimitive].success
    else if (clazz == classOf[BigDecimal])
      ((x: BigDecimal) => WarpBigDecimal(x)).asInstanceOf[T => WarpPrimitive].success
    else if (clazz == classOf[org.joda.time.DateTime])
      ((x: org.joda.time.DateTime) => WarpDateTime(x)).asInstanceOf[T => WarpPrimitive].success
    else if (clazz == classOf[_root_.java.util.UUID])
      ((x: _root_.java.util.UUID) => WarpUuid(x)).asInstanceOf[T => WarpPrimitive].success
    else if (clazz == classOf[_root_.java.net.URI])
      ((x: _root_.java.net.URI) => WarpUri(x)).asInstanceOf[T => WarpPrimitive].success
    else
      UnspecifiedProblem(s""""${clazz.getName()}" is not a primitive value""").failure
  }

  def toWarpPrimitivesCollection[A](what: Traversable[A])(implicit tag: ClassTag[A]): AlmValidation[WarpCollection] =
    getMapToPrimitive[A].map(m => WarpCollection(what.map(m).toVector))

  def ++(a: WarpElement, b: WarpElement): Unit = {}

  // def xxx() = WarpElement("", None) ++ WarpElement("", None)

  def E(label: String, what: WarpPackage): AlmValidation[WarpElement] =
    WarpElement(label, what).success  
    
  def P(label: String, what: Any): AlmValidation[WarpElement] =
    toWarpPrimitive(what).map(WarpElement(label, _))

  def POpt(label: String, what: Option[Any]): AlmValidation[WarpElement] =
    what match {
    case Some(v) => toWarpPrimitive(v).map(WarpElement(label, _))
    case None => WarpElement(label).success
  }
    
  def PC[A](label: String, what: Traversable[A])(implicit tag: ClassTag[A]): AlmValidation[WarpElement] =
    toWarpPrimitivesCollection(what).map(WarpElement(label, _))

  def PCOpt[A](label: String, what: Option[Traversable[A]])(implicit tag: ClassTag[A]): AlmValidation[WarpElement] =
    what match {
    case Some(v) => toWarpPrimitivesCollection(v).map(WarpElement(label, _))
    case None => WarpElement(label).success
  }
 
  def With[T](label: String, what: T, packer: WarpPacker[T])(implicit packers: WarpPackers): AlmValidation[WarpElement] =
    packer(what).map(WarpElement(label, _))

  def WithOpt[T](label: String, what: Option[T], packer: WarpPacker[T])(implicit packers: WarpPackers): AlmValidation[WarpElement] =
    what match {
    case Some(v) => packer(v).map(WarpElement(label, _))
    case None => WarpElement(label).success
  }
    
}

trait PackageBuilderOps {
  implicit class WarpElementOps(self: WarpElement) {
    def ~+(next: => AlmValidation[WarpElement]): AlmValidation[WarpObject] =
      next.fold(
        fail => fail.failure,
        succ => WarpObject(None, Vector(self, succ)).success)
  }

  implicit class WarpObjectOps(self: WarpObject) {
    def ~+(next: => AlmValidation[WarpElement]): AlmValidation[WarpObject] =
      next.fold(
        fail => fail.failure,
        succ => WarpObject(self.riftDescriptor, self.elements :+ succ).success)
  }

  implicit class RiftDescriptorOps(self: RiftDescriptor) {
    def ~+(next: => AlmValidation[WarpElement]): AlmValidation[WarpObject] =
      next.fold(
        fail => fail.failure,
        succ => WarpObject(Some(self), Vector(succ)).success)
  }

  implicit class WarpObjectVOps(self: AlmValidation[WarpObject]) {
    def ~+(next: => AlmValidation[WarpElement]): AlmValidation[WarpObject] =
      self.fold(
        fail => fail.failure,
        succObj =>
          next.fold(
            fail => fail.failure,
            succ => WarpObject(succObj.riftDescriptor, succObj.elements :+ succ).success))
  }

}