package riftwarp.std

import scala.reflect.ClassTag
import scalaz.syntax.validation._
import scalaz.std._
import almhirt.common._
import riftwarp._

trait WarpObjectLookUp {
  def underlying: WarpObject
  def riftDescriptor: Option[RiftDescriptor]
  def getRiftDescriptor: AlmValidation[RiftDescriptor] =
    riftDescriptor match {
      case Some(rd) => rd.success
      case None => NoSuchElementProblem("Object has no RiftDescriptor").failure
    }

  def tryGetWarpPackage(label: String): AlmValidation[Option[WarpPackage]]

  def getWarpPackage(label: String): AlmValidation[WarpPackage] =
    tryGetWarpPackage(label).fold(
      fail => fail.failure,
      optV => optV match {
        case Some(v) => v.success
        case None => NoSuchElementProblem(s"""The WarpObject contains an element with label "$label" but it has no value""").failure
      })

  def tryGetWarpPrimitive(label: String): AlmValidation[Option[WarpPrimitive]] =
    getAndCheck(label) {
      case wp: WarpPrimitive => wp.success
      case x => UnspecifiedApplicationProblem(s""""${x.getClass().getName()}" is not a WarpPrimitive""").failure
    }

  def getWarpPrimitive(label: String): AlmValidation[WarpPrimitive] =
    getMandatory(label, tryGetWarpPrimitive)

  def tryGetWarpObject(label: String): AlmValidation[Option[WarpObject]] =
    getAndCheck(label) {
      case wo: WarpObject => wo.success
      case x => UnspecifiedApplicationProblem(s""""${x.getClass().getName()}" is not a WarpObject""").failure
    }

  def getWarpObject(label: String): AlmValidation[WarpObject] =
    getMandatory(label, tryGetWarpObject)

  def tryGetWarpCollection(label: String): AlmValidation[Option[WarpCollection]] =
    getAndCheck(label) {
      case wc: WarpCollection => wc.success
      case x => UnspecifiedApplicationProblem(s""""${x.getClass().getName()}" is not a WarpCollection""").failure
    }

  def getWarpCollection(label: String): AlmValidation[WarpCollection] =
    getMandatory(label, tryGetWarpCollection)

  def tryGetWarpAssociativeCollection(label: String): AlmValidation[Option[WarpAssociativeCollection]] =
    getAndCheck(label) {
      case wc: WarpAssociativeCollection => wc.success
      case x => UnspecifiedApplicationProblem(s""""${x.getClass().getName()}" is not a WarpAssociativeCollection""").failure
    }

  def getWarpAssociativeCollection(label: String): AlmValidation[WarpAssociativeCollection] =
    getMandatory(label, tryGetWarpAssociativeCollection)

  def tryGetWarpTree(label: String): AlmValidation[Option[WarpTree]] =
    getAndCheck(label) {
      case wt: WarpTree => wt.success
      case x => UnspecifiedApplicationProblem(s""""${x.getClass().getName()}" is not a WarpTree""").failure
    }

  def getWarpTree(label: String): AlmValidation[WarpTree] =
    getMandatory(label, tryGetWarpTree)

  def tryGetBytes(label: String): AlmValidation[Option[Array[Byte]]] =
    getAndCheck(label) {
      case wb: WarpBytes => wb.bytes.success
      case wb: WarpBase64 => wb.bytes.success
      case wb: WarpBlob => wb.bytes.success
      case x => UnspecifiedApplicationProblem(s""""${x.getClass().getName()}" is not a byte array representation""").failure
    }

  def getBytes(label: String): AlmValidation[Array[Byte]] =
    getMandatory(label, tryGetBytes)

  def tryGetAs[T: WarpPrimitiveConverter](label: String): AlmValidation[Option[T]] =
    getAndCheck(label) {
      case wp: WarpPrimitive => wp.as[T]
      case x => UnspecifiedApplicationProblem(s""""${x.getClass().getName()}" is not a WarpPrimitive""").failure
    }

  def getAs[T: WarpPrimitiveConverter](label: String): AlmValidation[T] =
    getMandatory(label, x => tryGetAs[T](x))

  def tryGetObjWith[T](label: String, unpacker: WarpUnpacker[T])(implicit unpackers: WarpUnpackers): AlmValidation[Option[T]] =
    getAndCheck(label) {
      case wo: WarpObject => unpacker(wo)
      case x => UnspecifiedApplicationProblem(s""""${x.getClass().getName()}" is not a WarpObject""").failure
    }

  def getObjWith[T](label: String, unpacker: WarpUnpacker[T])(implicit unpackers: WarpUnpackers): AlmValidation[T] =
    getMandatory(label, x => tryGetObjWith(x, unpacker))

  def tryGetObjByDesc[T](label: String, descriptor: RiftDescriptor)(implicit unpackers: WarpUnpackers, tag: ClassTag[T]): AlmValidation[Option[T]] =
    getAndCheck(label) {
      case wo: WarpObject => unpackers.getTyped(descriptor).flatMap(_(wo))
      case x => UnspecifiedApplicationProblem(s""""${x.getClass().getName()}" is not a WarpObject""").failure
    }

  def getObjByDesc[T](label: String, descriptor: RiftDescriptor)(implicit unpackers: WarpUnpackers, tag: ClassTag[T]): AlmValidation[T] =
    getMandatory(label, x => tryGetObjByDesc(x, descriptor))

  def tryGetObjByTag[T](label: String)(implicit unpackers: WarpUnpackers, tag: ClassTag[T]): AlmValidation[Option[T]] =
    tryGetObjByDesc(label, RiftDescriptor(tag.runtimeClass))

  def getObjByTag[T](label: String)(implicit unpackers: WarpUnpackers, tag: ClassTag[T]): AlmValidation[T] =
    getObjByDesc(label, RiftDescriptor(tag.runtimeClass))

  private def getAndCheck[T](label: String)(checkType: WarpPackage => AlmValidation[T]): AlmValidation[Option[T]] =
    tryGetWarpPackage(label).fold(
      fail => fail.failure,
      succ => option.fold(succ)(
        some => checkType(some).map(Some(_)),
        None.success))

  private def getMandatory[T](label: String, get: String => AlmValidation[Option[T]]): AlmValidation[T] =
    get(label).fold(
      fail => fail.failure,
      optV => optV match {
        case Some(v) => v.success
        case None => NoSuchElementProblem(s"""The WarpObject contains an element with label "$label" but it has no value""").failure
      })

}

private class MapBasedWarpObjectLookUp(override val underlying: WarpObject) extends WarpObjectLookUp {
  private val theMap = underlying.elements.map(e => (e.label, e.value)).toMap
  override val riftDescriptor = underlying.riftDescriptor
  override def tryGetWarpPackage(label: String): AlmValidation[Option[WarpPackage]] =
    theMap.get(label) match {
      case Some(v) => v.success
      case None => NoSuchElementProblem(s"""The WarpObject does not contain an element with label "$label"""").failure
    }

}

trait PackageExtractorFuns {
  def fastLookUp(obj: WarpObject): WarpObjectLookUp = new MapBasedWarpObjectLookUp(obj)
  def withFastLookUp[T](obj: WarpPackage)(f: WarpObjectLookUp => AlmValidation[T]): AlmValidation[T] =
    obj match {
      case wo: WarpObject => f(fastLookUp(wo))
      case x => UnspecifiedApplicationProblem(s""""${x.getClass().getName()}" is not a WarpObject so i cannot create a fast lookup""").failure
    }
}