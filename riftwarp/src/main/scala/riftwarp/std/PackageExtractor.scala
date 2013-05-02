package riftwarp.std

import scala.reflect.ClassTag
import scalaz._, Scalaz._
import scalaz.std._
import almhirt.common._
import almhirt.almvalidation.kit._
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

  def tryGetWith[T](label: String, unpacker: WarpUnpacker[T])(implicit unpackers: WarpUnpackers): AlmValidation[Option[T]] =
    getAndCheck(label) { what => unpacker(what) }

  def getWith[T](label: String, unpacker: WarpUnpacker[T])(implicit unpackers: WarpUnpackers): AlmValidation[T] =
    getMandatory(label, x => tryGetWith(x, unpacker))

  def tryGet(label: String, overrideDescriptor: Option[RiftDescriptor] = None, backUpDescriptor: Option[RiftDescriptor] = None)(implicit unpackers: WarpUnpackers): AlmValidation[Option[Any]] =
    getAndCheck(label) { what => unpack(what, overrideDescriptor, backUpDescriptor) }

  def get(label: String, overrideDescriptor: Option[RiftDescriptor] = None, backUpDescriptor: Option[RiftDescriptor] = None)(implicit unpackers: WarpUnpackers): AlmValidation[Any] =
    getMandatory(label, x => tryGet(x, overrideDescriptor, backUpDescriptor))

  def tryGetTyped[T](label: String, overrideDescriptor: Option[RiftDescriptor] = None)(implicit unpackers: WarpUnpackers, tag: ClassTag[T]): AlmValidation[Option[T]] =
    getAndCheck(label) { what => unpack(what, overrideDescriptor, Some(RiftDescriptor(tag.runtimeClass))).flatMap(_.castTo[T]) }

  def getTyped[T](label: String, overrideDescriptor: Option[RiftDescriptor] = None)(implicit unpackers: WarpUnpackers, tag: ClassTag[T]): AlmValidation[T] =
    getMandatory(label, x => tryGetTyped(x, overrideDescriptor))

  def tryGetPrimitives[T](label: String)(implicit conv: WarpPrimitiveConverter[T]): AlmValidation[Option[Vector[T]]] =
    getAndCheck(label) {
      case wc: WarpCollection => wc.items.map(item => conv.convert(item).toAgg).sequence
      case x => UnspecifiedApplicationProblem(s""""${x.getClass().getName()}" is not a WarpCollection""").failure
    }

  def getPrimitives[T: WarpPrimitiveConverter](label: String): AlmValidation[Vector[T]] =
    getMandatory(label, x => tryGetPrimitives[T](x))

  def tryGetManyWith[T](label: String, unpacker: WarpUnpacker[T])(implicit unpackers: WarpUnpackers): AlmValidation[Option[Vector[T]]] =
    getAndCheck(label) {
      case wc: WarpCollection => wc.items.map(item => unpacker(item).toAgg).sequence
      case x => UnspecifiedApplicationProblem(s""""${x.getClass().getName()}" is not a WarpCollection""").failure
    }

  def getManyWith[T](label: String, unpacker: WarpUnpacker[T])(implicit unpackers: WarpUnpackers): AlmValidation[Vector[T]] =
    getMandatory(label, x => tryGetManyWith[T](x, unpacker))

  def tryGetMany(label: String, overrideDescriptor: Option[RiftDescriptor] = None, backUpDescriptor: Option[RiftDescriptor] = None)(implicit unpackers: WarpUnpackers): AlmValidation[Option[Vector[Any]]] =
    getAndCheck(label) {
      case wc: WarpCollection => wc.items.map(item => unpack(item, overrideDescriptor, backUpDescriptor).toAgg).sequence
      case x => UnspecifiedApplicationProblem(s""""${x.getClass().getName()}" is not a WarpCollection""").failure
    }

  def getMany[T](label: String, overrideDescriptor: Option[RiftDescriptor] = None, backUpDescriptor: Option[RiftDescriptor] = None)(implicit unpackers: WarpUnpackers): AlmValidation[Vector[Any]] =
    getMandatory(label, x => tryGetMany(x, overrideDescriptor, backUpDescriptor))

  def tryGetManyTyped[T](label: String, overrideDescriptor: Option[RiftDescriptor] = None)(implicit unpackers: WarpUnpackers, tag: ClassTag[T]): AlmValidation[Option[Vector[T]]] =
    getAndCheck(label) {
      case wc: WarpCollection => wc.items.map(item => unpack(item, overrideDescriptor, Some(RiftDescriptor(tag.runtimeClass))).flatMap(_.castTo[T]).toAgg).sequence
      case x => UnspecifiedApplicationProblem(s""""${x.getClass().getName()}" is not a WarpCollection""").failure
    }

  def getManyTyped[T](label: String, overrideDescriptor: Option[RiftDescriptor] = None)(implicit unpackers: WarpUnpackers, tag: ClassTag[T]): AlmValidation[Vector[T]] =
    getMandatory(label, x => tryGetManyTyped[T](x, overrideDescriptor))

  def tryGetPrimitiveAssocs[A, B](label: String)(implicit convA: WarpPrimitiveConverter[A], convB: WarpPrimitiveConverter[B]): AlmValidation[Option[Vector[(A, B)]]] =
    getAndCheck(label) {
      case wa: WarpAssociativeCollection => wa.items.map(item => convA.convert(item._1).flatMap(a => convB.convert(item._2).map(b => (a, b))).toAgg).sequence
      case x => UnspecifiedApplicationProblem(s""""${x.getClass().getName()}" is not a WarpAssociativeCollection""").failure
    }

  def getPrimitiveAssocs[A: WarpPrimitiveConverter, B: WarpPrimitiveConverter](label: String): AlmValidation[Vector[(A, B)]] =
    getMandatory(label, x => tryGetPrimitiveAssocs[A, B](x))
    
  def tryGetAssocsEachWith[A, B](label: String, unpackerA: WarpUnpacker[A], unpackerB: WarpUnpacker[B])(implicit unpackers: WarpUnpackers): AlmValidation[Option[Vector[(A, B)]]] =
    getAndCheck(label) {
      case wa: WarpAssociativeCollection => wa.items.map(item => unpackerA(item._1).flatMap(a => unpackerB(item._2).map(b => (a, b))).toAgg).sequence
      case x => UnspecifiedApplicationProblem(s""""${x.getClass().getName()}" is not a WarpAssociativeCollection""").failure
    }

  def getAssocsEachWith[A, B](label: String, unpackerA: WarpUnpacker[A], unpackerB: WarpUnpacker[B])(implicit unpackers: WarpUnpackers): AlmValidation[Vector[(A, B)]] =
    getMandatory(label, x => tryGetAssocsEachWith(x, unpackerA, unpackerB))

  def tryGetAssocsWith[A, B](label: String, unpackerB: WarpUnpacker[B])(implicit convA: WarpPrimitiveConverter[A], unpackers: WarpUnpackers): AlmValidation[Option[Vector[(A, B)]]] =
    getAndCheck(label) {
      case wa: WarpAssociativeCollection => wa.items.map(item => convA.convert(item._1).flatMap(a => unpackerB(item._2).map(b => (a, b))).toAgg).sequence
      case x => UnspecifiedApplicationProblem(s""""${x.getClass().getName()}" is not a WarpAssociativeCollection""").failure
    }

  def getAssocsWith[A: WarpPrimitiveConverter, B](label: String, unpackerB: WarpUnpacker[B])(implicit unpackers: WarpUnpackers): AlmValidation[Vector[(A, B)]] =
    getMandatory(label, x => tryGetAssocsWith[A, B](x, unpackerB))
    
  def tryGetAssocs[A](label: String, overrideDescriptor: Option[RiftDescriptor] = None, backUpDescriptor: Option[RiftDescriptor] = None)(implicit unpackers: WarpUnpackers, conv: WarpPrimitiveConverter[A]): AlmValidation[Option[Vector[(A, Any)]]] =
    getAndCheck(label) {
      case wa: WarpAssociativeCollection => wa.items.map(item => conv.convert(item._1).flatMap(a => unpack(item._2, overrideDescriptor, backUpDescriptor).map(b => (a, b))).toAgg).sequence
      case x => UnspecifiedApplicationProblem(s""""${x.getClass().getName()}" is not a WarpAssociativeCollection""").failure
    }

  def getAssocs[A](label: String, overrideDescriptor: Option[RiftDescriptor] = None, backUpDescriptor: Option[RiftDescriptor] = None)(implicit unpackers: WarpUnpackers, conv: WarpPrimitiveConverter[A]): AlmValidation[Vector[(A, Any)]] =
    getMandatory(label, x => tryGetAssocs[A](x, overrideDescriptor, backUpDescriptor))

  def tryGetAssocsTyped[A, B](label: String, overrideDescriptor: Option[RiftDescriptor] = None)(implicit unpackers: WarpUnpackers, tag: ClassTag[B], conv: WarpPrimitiveConverter[A]): AlmValidation[Option[Vector[(A, B)]]] =
    getAndCheck(label) {
      case wa: WarpAssociativeCollection => wa.items.map(item =>
        conv.convert(item._1).flatMap(a =>
          unpack(item._2, overrideDescriptor, Some(RiftDescriptor(tag.runtimeClass))).flatMap(_.castTo[B]).map(b =>
            (a, b))).toAgg).sequence
      case x => UnspecifiedApplicationProblem(s""""${x.getClass().getName()}" is not a WarpAssociativeCollection""").failure
    }

  def getAssocsTyped[A, B](label: String, overrideDescriptor: Option[RiftDescriptor] = None)(implicit unpackers: WarpUnpackers, tag: ClassTag[B], conv: WarpPrimitiveConverter[A]): AlmValidation[Vector[(A, B)]] =
    getMandatory(label, x => tryGetAssocsTyped[A, B](x, overrideDescriptor))

  def tryGetPrimitivesTree[T](label: String)(implicit conv: WarpPrimitiveConverter[T]): AlmValidation[Option[Tree[T]]] =
    getAndCheck(label) {
      case wt: WarpTree => wt.tree.map(item => conv.convert(item).toAgg).sequence
      case x => UnspecifiedApplicationProblem(s""""${x.getClass().getName()}" is not a WarpCollection""").failure
    }

  def getPrimitivesTree[T: WarpPrimitiveConverter](label: String): AlmValidation[Tree[T]] =
    getMandatory(label, x => tryGetPrimitivesTree[T](x))
    
  def tryGetTreeWith[T](label: String, unpacker: WarpUnpacker[T])(implicit unpackers: WarpUnpackers): AlmValidation[Option[Tree[T]]] =
    getAndCheck(label) {
      case wt: WarpTree => wt.tree.map(item => unpacker(item).toAgg).sequence
      case x => UnspecifiedApplicationProblem(s""""${x.getClass().getName()}" is not a WarpTree""").failure
    }

  def getTreeWith[T](label: String, unpacker: WarpUnpacker[T])(implicit unpackers: WarpUnpackers): AlmValidation[Tree[T]] =
    getMandatory(label, x => tryGetTreeWith[T](x, unpacker))

  def tryGetTree(label: String, overrideDescriptor: Option[RiftDescriptor] = None, backUpDescriptor: Option[RiftDescriptor] = None)(implicit unpackers: WarpUnpackers): AlmValidation[Option[Tree[Any]]] =
    getAndCheck(label) {
      case wt: WarpTree => wt.tree.map(item => unpack(item, overrideDescriptor, backUpDescriptor).toAgg).sequence
      case x => UnspecifiedApplicationProblem(s""""${x.getClass().getName()}" is not a WarpTree""").failure
    }

  def getTree[T](label: String, overrideDescriptor: Option[RiftDescriptor] = None, backUpDescriptor: Option[RiftDescriptor] = None)(implicit unpackers: WarpUnpackers): AlmValidation[Tree[Any]] =
    getMandatory(label, x => tryGetTree(x, overrideDescriptor, backUpDescriptor))

  def tryGetTreeTyped[T](label: String, overrideDescriptor: Option[RiftDescriptor] = None)(implicit unpackers: WarpUnpackers, tag: ClassTag[T]): AlmValidation[Option[Tree[T]]] =
    getAndCheck(label) {
      case wt: WarpTree => wt.tree.map(item => unpack(item, overrideDescriptor, Some(RiftDescriptor(tag.runtimeClass))).flatMap(_.castTo[T]).toAgg).sequence
      case x => UnspecifiedApplicationProblem(s""""${x.getClass().getName()}" is not a WarpTree""").failure
    }

  def getTreeTyped[T](label: String, overrideDescriptor: Option[RiftDescriptor] = None)(implicit unpackers: WarpUnpackers, tag: ClassTag[T]): AlmValidation[Tree[T]] =
    getMandatory(label, x => tryGetTreeTyped[T](x, overrideDescriptor))

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

  private def unpack(what: WarpPackage, overrideDescriptor: Option[RiftDescriptor], backUpDescriptor: Option[RiftDescriptor])(implicit unpackers: WarpUnpackers): AlmValidation[Any] = {
    overrideDescriptor match {
      case Some(pd) =>
        unpackers.get(pd).leftMap(_.mapMessage(old => s"RiftDescriptor has been overriden: $old")).flatMap(_(what))
      case None =>
        what match {
          case wp: WarpPrimitive => wp.value.success
          case WarpObject(td, _) =>
            td match {
              case Some(d) =>
                unpackers.get(d).fold(
                  fail => backUpDescriptor match {
                    case Some(bud) => unpackers.get(bud).flatMap(_(what))
                    case None => SerializationProblem("No Unpacker found for WarpObject. Hint: The WarpObject had a RiftDescriptor but no unpacker was found. There was neither a backup RiftDescriptor nor an override RiftDescriptor.").failure
                  },
                  succ => succ.success)
              case None =>
                backUpDescriptor match {
                  case Some(bud) => unpackers.get(bud).flatMap(_(what))
                  case None => SerializationProblem("No Unpacker found for WarpObject. Hint: Neither the WarpObject contained a RiftDescriptor nor a backup RiftDescriptor or override RiftDescriptor were supplied.").failure
                }
            }
          case bp: BinaryWarpPackage => bp.bytes.success
          case WarpCollection(items) =>
            val x = items.map(item => unpack(item, None, None).toAgg).sequence
            x
          case WarpAssociativeCollection(items) =>
            val x = items.map(item =>
              unpack(item._1, None, None).flatMap(k =>
                unpack(item._2, None, None).map(v =>
                  (k, v))).toAgg).sequence
            x
          case WarpTree(tree) =>
            val x = tree.map(item => unpack(item, None, None).toAgg).sequence
            x
        }
    }
  }

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
  def withFastLookUp[T](from: WarpPackage)(f: WarpObjectLookUp => AlmValidation[T]): AlmValidation[T] =
    from match {
      case wo: WarpObject => f(fastLookUp(wo))
      case x => UnspecifiedApplicationProblem(s""""${x.getClass().getName()}" is not a WarpObject so i cannot create a fast lookup""").failure
    }
}