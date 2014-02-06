package riftwarp.std

import scala.reflect.ClassTag
import scalaz._, Scalaz._
import scalaz.std._
import almhirt.common._
import almhirt.almvalidation.kit._
import riftwarp._
import riftwarp.std._

trait WarpObjectLookUp {
  def underlying: WarpObject
  def warpDescriptor: Option[WarpDescriptor]
  def getWarpDescriptor: AlmValidation[WarpDescriptor] =
    warpDescriptor match {
      case Some(rd) => rd.success
      case None => NoSuchElementProblem("Object has no WarpDescriptor").failure
    }

  def tryGetWarpPackage(label: String): Option[WarpPackage]

  def getWarpPackage(label: String): AlmValidation[WarpPackage] =
    tryGetWarpPackage(label) match {
      case Some(wp) => wp.success
      case None =>
        val wdString = underlying.warpDescriptor.map(_.toString).getOrElse("no WarpDescriptor")
        val labelsStr = underlying.elements.map(_.label).mkString("[", ",", "]")
        NoSuchElementProblem(s"""The WarpObject($wdString) does not contain an element with label "$label". The following labels were found: $labelsStr.""").failure
    }

  def tryGetWarpPrimitive(label: String): AlmValidation[Option[WarpPrimitive]] =
    getAndCheck(label) {
      case wp: WarpPrimitive => wp.success
      case x => ArgumentProblem(s""""${x.getClass().getName()}" is not a WarpPrimitive""").failure
    }

  def getWarpPrimitive(label: String): AlmValidation[WarpPrimitive] =
    getMandatory(label, tryGetWarpPrimitive).leftMap(p => SerializationProblem(s"""The WarpObject with descriptor "${warpDescriptor.toString}" encountered a problem.""", cause = Some(p)))

  def tryGetWarpObject(label: String): AlmValidation[Option[WarpObject]] =
    getAndCheck(label) {
      case wo: WarpObject => wo.success
      case x => ArgumentProblem(s""""${x.getClass().getName()}" is not a WarpObject""").failure
    }

  def getWarpObject(label: String): AlmValidation[WarpObject] =
    getMandatory(label, tryGetWarpObject)

  def tryGetWarpCollection(label: String): AlmValidation[Option[WarpCollection]] =
    getAndCheck(label) {
      case wc: WarpCollection => wc.success
      case x => ArgumentProblem(s""""${x.getClass().getName()}" is not a WarpCollection""").failure
    }

  def getWarpCollection(label: String): AlmValidation[WarpCollection] =
    getMandatory(label, tryGetWarpCollection)

  def tryGetWarpAssociativeCollection(label: String): AlmValidation[Option[WarpAssociativeCollection]] =
    getAndCheck(label) {
      case wc: WarpAssociativeCollection => wc.success
      case x => ArgumentProblem(s""""${x.getClass().getName()}" is not a WarpAssociativeCollection""").failure
    }

  def getWarpAssociativeCollection(label: String): AlmValidation[WarpAssociativeCollection] =
    getMandatory(label, tryGetWarpAssociativeCollection)

  def tryGetWarpTree(label: String): AlmValidation[Option[WarpTree]] =
    getAndCheck(label) {
      case wt: WarpTree => wt.success
      case x => ArgumentProblem(s""""${x.getClass().getName()}" is not a WarpTree""").failure
    }

  def getWarpTree(label: String): AlmValidation[WarpTree] =
    getMandatory(label, tryGetWarpTree)

  def tryGetBytes(label: String): AlmValidation[Option[IndexedSeq[Byte]]] =
    getAndCheck(label) {
      case wb: WarpBytes => wb.bytes.success
      case wb: WarpBlob => wb.bytes.success
      case wc: WarpCollection => wc.items.map(WarpPrimitiveToByteConverterInst.convert(_).toAgg).sequence.map(_.toArray)
      case x => ArgumentProblem(s""""${x.getClass().getName()}" is not a byte array representation""").failure
    }

  def getBytes(label: String): AlmValidation[IndexedSeq[Byte]] =
    getMandatory(label, tryGetBytes)

  def tryGetAs[T: WarpPrimitiveConverter](label: String): AlmValidation[Option[T]] =
    getAndCheck(label) {
      case wp: WarpPrimitive => wp.as[T]
      case x => ArgumentProblem(s""""${x.getClass().getName()}" is not a WarpPrimitive""").failure
    }

  def getAs[T: WarpPrimitiveConverter](label: String): AlmValidation[T] =
    getMandatory(label, x => tryGetAs[T](x))

  def tryGet2As[A: WarpPrimitiveConverter, B: WarpPrimitiveConverter](label: String): AlmValidation[Option[(A, B)]] =
    getAndCheck(label) {
      case WarpTuple2(a: WarpPrimitive, b: WarpPrimitive) => a.as[A].flatMap(a => b.as[B].map((a, _)))
      case WarpCollection(Vector(a: WarpPrimitive, b: WarpPrimitive)) => a.as[A].flatMap(a => b.as[B].map((a, _)))
      case x => ArgumentProblem(s""""${x.getClass().getName()}" is not a WarpTuple2 of primitives""").failure
    }

  def get2As[A: WarpPrimitiveConverter, B: WarpPrimitiveConverter](label: String): AlmValidation[(A, B)] =
    getMandatory(label, x => tryGet2As[A, B](x))

  def tryGet3As[A: WarpPrimitiveConverter, B: WarpPrimitiveConverter, C: WarpPrimitiveConverter](label: String): AlmValidation[Option[(A, B, C)]] =
    getAndCheck(label) {
      case WarpTuple3(a: WarpPrimitive, b: WarpPrimitive, c: WarpPrimitive) => a.as[A].flatMap(a => b.as[B].flatMap(b => c.as[C].map((a, b, _))))
      case WarpCollection(Vector(a: WarpPrimitive, b: WarpPrimitive, c: WarpPrimitive)) => a.as[A].flatMap(a => b.as[B].flatMap(b => c.as[C].map((a, b, _))))
      case x => ArgumentProblem(s""""${x.getClass().getName()}" is not a WarpTuple3 of primitives""").failure
    }

  def get3As[A: WarpPrimitiveConverter, B: WarpPrimitiveConverter, C: WarpPrimitiveConverter](label: String): AlmValidation[(A, B, C)] =
    getMandatory(label, x => tryGet3As[A, B, C](x))

  def tryGetWith[T](label: String, unpacker: WarpUnpacker[T])(implicit unpackers: WarpUnpackers): AlmValidation[Option[T]] =
    getAndCheck(label) { what => unpacker(what) }

  def getWith[T](label: String, unpacker: WarpUnpacker[T])(implicit unpackers: WarpUnpackers): AlmValidation[T] =
    getMandatory(label, x => tryGetWith(x, unpacker))

  def tryGet(label: String, overrideDescriptor: Option[WarpDescriptor] = None, backUpDescriptor: Option[WarpDescriptor] = None)(implicit unpackers: WarpUnpackers): AlmValidation[Option[Any]] =
    getAndCheck(label) { what => unpack(what, overrideDescriptor, backUpDescriptor) }

  def get(label: String, overrideDescriptor: Option[WarpDescriptor] = None, backUpDescriptor: Option[WarpDescriptor] = None)(implicit unpackers: WarpUnpackers): AlmValidation[Any] =
    getMandatory(label, x => tryGet(x, overrideDescriptor, backUpDescriptor))

  def tryGetTyped[T](label: String, overrideDescriptor: Option[WarpDescriptor] = None)(implicit unpackers: WarpUnpackers, tag: ClassTag[T]): AlmValidation[Option[T]] =
    getAndCheck(label) { what => unpack(what, overrideDescriptor, Some(WarpDescriptor(tag.runtimeClass))).flatMap(_.castTo[T]) }

  def getTyped[T](label: String, overrideDescriptor: Option[WarpDescriptor] = None)(implicit unpackers: WarpUnpackers, tag: ClassTag[T]): AlmValidation[T] =
    getMandatory(label, x => tryGetTyped(x, overrideDescriptor))

  def tryGetPrimitives[T](label: String)(implicit conv: WarpPrimitiveConverter[T]): AlmValidation[Option[Vector[T]]] =
    getAndCheck(label) {
      case wc: WarpCollection => wc.items.map(item => conv.convert(item).toAgg).sequence
      case x => ArgumentProblem(s"""[tryGetPrimitives("$label")]: "${x.getClass().getName()}" is not a WarpCollection""").failure
    }

  def getPrimitives[T: WarpPrimitiveConverter](label: String): AlmValidation[Vector[T]] =
    getMandatory(label, x => tryGetPrimitives[T](x))

  def tryGetManyWith[T](label: String, unpacker: WarpUnpacker[T])(implicit unpackers: WarpUnpackers): AlmValidation[Option[Vector[T]]] =
    getAndCheck(label) {
      case wc: WarpCollection => wc.items.map(item => unpacker(item).toAgg).sequence
      case x => ArgumentProblem(s"""[tryGetManyWith("$label")]: "${x.getClass().getName()}" is not a WarpCollection""").failure
    }

  def getManyWith[T](label: String, unpacker: WarpUnpacker[T])(implicit unpackers: WarpUnpackers): AlmValidation[Vector[T]] =
    getMandatory(label, x => tryGetManyWith[T](x, unpacker))

  def tryGetMany(label: String, overrideDescriptor: Option[WarpDescriptor] = None, backUpDescriptor: Option[WarpDescriptor] = None)(implicit unpackers: WarpUnpackers): AlmValidation[Option[Vector[Any]]] =
    getAndCheck(label) {
      case wc: WarpCollection => wc.items.map(item => unpack(item, overrideDescriptor, backUpDescriptor).toAgg).sequence
      case x => ArgumentProblem(s"""[tryGetMany("$label")]: "${x.getClass().getName()}" is not a WarpCollection""").failure
    }

  def getMany(label: String, overrideDescriptor: Option[WarpDescriptor] = None, backUpDescriptor: Option[WarpDescriptor] = None)(implicit unpackers: WarpUnpackers): AlmValidation[Vector[Any]] =
    getMandatory(label, x => tryGetMany(x, overrideDescriptor, backUpDescriptor))

  def tryGetManyTyped[T](label: String, overrideDescriptor: Option[WarpDescriptor] = None)(implicit unpackers: WarpUnpackers, tag: ClassTag[T]): AlmValidation[Option[Vector[T]]] =
    getAndCheck(label) {
      case wc: WarpCollection => wc.items.map(item => unpack(item, overrideDescriptor, Some(WarpDescriptor(tag.runtimeClass))).flatMap(_.castTo[T]).toAgg).sequence
      case x => ArgumentProblem(s"""[tryGetManyTyped("$label")]: "${x.getClass().getName()}" is not a WarpCollection""").failure
    }

  def getManyTyped[T](label: String, overrideDescriptor: Option[WarpDescriptor] = None)(implicit unpackers: WarpUnpackers, tag: ClassTag[T]): AlmValidation[Vector[T]] =
    getMandatory(label, x => tryGetManyTyped[T](x, overrideDescriptor))

  def tryGetPrimitiveAssocs[A, B](label: String)(implicit convA: WarpPrimitiveConverter[A], convB: WarpPrimitiveConverter[B]): AlmValidation[Option[Vector[(A, B)]]] = {
    def mapThem(wa: WarpAssociativeCollection) = wa.items.map(item => convA.convert(item._1).flatMap(a => convB.convert(item._2).map(b => (a, b))).toAgg).sequence
    getAndCheck(label) {
      case wa: WarpAssociativeCollection => mapThem(wa)
      case wc: WarpCollection => wc.associative.flatMap(mapThem(_))
      case x => ArgumentProblem(s"""[tryGetPrimitiveAssocs]("$label")]: "${x.getClass().getName()}" is not a WarpAssociativeCollection nor can it be transformed to a WarpAssociativeCollection""").failure
    }
  }

  def getPrimitiveAssocs[A: WarpPrimitiveConverter, B: WarpPrimitiveConverter](label: String): AlmValidation[Vector[(A, B)]] =
    getMandatory(label, x => tryGetPrimitiveAssocs[A, B](x))

  def tryGetAssocsEachWith[A, B](label: String, unpackerA: WarpUnpacker[A], unpackerB: WarpUnpacker[B])(implicit unpackers: WarpUnpackers): AlmValidation[Option[Vector[(A, B)]]] = {
    def mapThem(wa: WarpAssociativeCollection) = wa.items.map(item => unpackerA(item._1).flatMap(a => unpackerB(item._2).map(b => (a, b))).toAgg).sequence
    getAndCheck(label) {
      case wa: WarpAssociativeCollection => mapThem(wa)
      case wc: WarpCollection => wc.associative.flatMap(mapThem(_))
      case x => ArgumentProblem(s"""[tryGetAssocsEachWith("$label")]: "${x.getClass().getName()}" is not a WarpAssociativeCollection nor can it be transformed to a WarpAssociativeCollection""").failure
    }
  }

  def getAssocsEachWith[A, B](label: String, unpackerA: WarpUnpacker[A], unpackerB: WarpUnpacker[B])(implicit unpackers: WarpUnpackers): AlmValidation[Vector[(A, B)]] =
    getMandatory(label, x => tryGetAssocsEachWith(x, unpackerA, unpackerB))

  def tryGetAssocsWith[A, B](label: String, unpackerB: WarpUnpacker[B])(implicit convA: WarpPrimitiveConverter[A], unpackers: WarpUnpackers): AlmValidation[Option[Vector[(A, B)]]] = {
    def mapThem(wa: WarpAssociativeCollection) = wa.items.map(item => convA.convert(item._1).flatMap(a => unpackerB(item._2).map(b => (a, b))).toAgg).sequence
    getAndCheck(label) {
      case wa: WarpAssociativeCollection => mapThem(wa)
      case wc: WarpCollection => wc.associative.flatMap(mapThem(_))
      case x => ArgumentProblem(s"""[tryGetAssocsWith("$label")]: "${x.getClass().getName()}" is not a WarpAssociativeCollection nor can it be transformed to a WarpAssociativeCollection""").failure
    }
  }

  def getAssocsWith[A: WarpPrimitiveConverter, B](label: String, unpackerB: WarpUnpacker[B])(implicit unpackers: WarpUnpackers): AlmValidation[Vector[(A, B)]] =
    getMandatory(label, x => tryGetAssocsWith[A, B](x, unpackerB))

  def tryGetAssocs[A](label: String, overrideDescriptor: Option[WarpDescriptor] = None, backUpDescriptor: Option[WarpDescriptor] = None)(implicit unpackers: WarpUnpackers, conv: WarpPrimitiveConverter[A]): AlmValidation[Option[Vector[(A, Any)]]] = {
    def mapThem(wa: WarpAssociativeCollection) = wa.items.map(item => conv.convert(item._1).flatMap(a => unpack(item._2, overrideDescriptor, backUpDescriptor).map(b => (a, b))).toAgg).sequence
    getAndCheck(label) {
      case wa: WarpAssociativeCollection => mapThem(wa)
      case wc: WarpCollection => wc.associative.flatMap(mapThem(_))
      case x => ArgumentProblem(s"""[tryGetAssocs("$label")]: "${x.getClass().getName()}" is not a WarpAssociativeCollection nor can it be transformed to a WarpAssociativeCollection""").failure
    }
  }

  def getAssocs[A](label: String, overrideDescriptor: Option[WarpDescriptor] = None, backUpDescriptor: Option[WarpDescriptor] = None)(implicit unpackers: WarpUnpackers, conv: WarpPrimitiveConverter[A]): AlmValidation[Vector[(A, Any)]] =
    getMandatory(label, x => tryGetAssocs[A](x, overrideDescriptor, backUpDescriptor))

  def tryGetAssocsTyped[A, B](label: String, overrideDescriptor: Option[WarpDescriptor] = None)(implicit unpackers: WarpUnpackers, tag: ClassTag[B], conv: WarpPrimitiveConverter[A]): AlmValidation[Option[Vector[(A, B)]]] = {
    def mapThem(wa: WarpAssociativeCollection) = wa.items.map(item =>
      conv.convert(item._1).flatMap(a =>
        unpack(item._2, overrideDescriptor, Some(WarpDescriptor(tag.runtimeClass))).flatMap(_.castTo[B]).map(b =>
          (a, b))).toAgg).sequence

    getAndCheck(label) {
      case wa: WarpAssociativeCollection => mapThem(wa)
      case wc: WarpCollection => wc.associative.flatMap(mapThem(_))
      case x => ArgumentProblem(s"""[tryGetAssocsTyped("$label")]: "${x.getClass().getName()}" is not a WarpAssociativeCollection nor can it be transformed to a WarpAssociativeCollection""").failure
    }
  }

  def getAssocsTyped[A, B](label: String, overrideDescriptor: Option[WarpDescriptor] = None)(implicit unpackers: WarpUnpackers, tag: ClassTag[B], conv: WarpPrimitiveConverter[A]): AlmValidation[Vector[(A, B)]] =
    getMandatory(label, x => tryGetAssocsTyped[A, B](x, overrideDescriptor))

  def tryGetPrimitivesTree[T](label: String)(implicit conv: WarpPrimitiveConverter[T]): AlmValidation[Option[Tree[T]]] = {
    def mapThem(wt: WarpTree) = wt.tree.map(item => conv.convert(item).toAgg).sequence
    getAndCheck(label) {
      case wt: WarpTree => mapThem(wt)
      case wc: WarpCollection => wc.warpTree.flatMap(mapThem(_))
      case x => ArgumentProblem(s"""[tryGetPrimitivesTree("$label")]: "${x.getClass().getName()}" is not a WarpTree nor can it be transformed to a WarpTree""").failure
    }
  }

  def getPrimitivesTree[T: WarpPrimitiveConverter](label: String): AlmValidation[Tree[T]] =
    getMandatory(label, x => tryGetPrimitivesTree[T](x))

  def tryGetTreeWith[T](label: String, unpacker: WarpUnpacker[T])(implicit unpackers: WarpUnpackers): AlmValidation[Option[Tree[T]]] = {
    def mapThem(wt: WarpTree) = wt.tree.map(item => unpacker(item).toAgg).sequence
    getAndCheck(label) {
      case wt: WarpTree => mapThem(wt)
      case wc: WarpCollection => wc.warpTree.flatMap(mapThem(_))
      case x => ArgumentProblem(s"""[tryGetTreeWith("$label")]: "${x.getClass().getName()}" is not a WarpTree nor can it be transformed to a WarpTree""").failure
    }
  }

  def getTreeWith[T](label: String, unpacker: WarpUnpacker[T])(implicit unpackers: WarpUnpackers): AlmValidation[Tree[T]] =
    getMandatory(label, x => tryGetTreeWith[T](x, unpacker))

  def tryGetTree(label: String, overrideDescriptor: Option[WarpDescriptor] = None, backUpDescriptor: Option[WarpDescriptor] = None)(implicit unpackers: WarpUnpackers): AlmValidation[Option[Tree[Any]]] = {
    def mapThem(wt: WarpTree) = wt.tree.map(item => unpack(item, overrideDescriptor, backUpDescriptor).toAgg).sequence
    getAndCheck(label) {
      case wt: WarpTree => mapThem(wt)
      case wc: WarpCollection => wc.warpTree.flatMap(mapThem(_))
      case x => ArgumentProblem(s"""[tryGetTree("$label")]: "${x.getClass().getName()}" is not a WarpTree nor can it be transformed to a WarpTree""").failure
    }
  }

  def getTree[T](label: String, overrideDescriptor: Option[WarpDescriptor] = None, backUpDescriptor: Option[WarpDescriptor] = None)(implicit unpackers: WarpUnpackers): AlmValidation[Tree[Any]] =
    getMandatory(label, x => tryGetTree(x, overrideDescriptor, backUpDescriptor))

  def tryGetTreeTyped[T](label: String, overrideDescriptor: Option[WarpDescriptor] = None)(implicit unpackers: WarpUnpackers, tag: ClassTag[T]): AlmValidation[Option[Tree[T]]] = {
    def mapThem(wt: WarpTree) = wt.tree.map(item => unpack(item, overrideDescriptor, Some(WarpDescriptor(tag.runtimeClass))).flatMap(_.castTo[T]).toAgg).sequence
    getAndCheck(label) {
      case wt: WarpTree => mapThem(wt)
      case wc: WarpCollection => wc.warpTree.flatMap(mapThem(_))
      case x => ArgumentProblem(s"""[tryGetTreeTyped("$label")]: "${x.getClass().getName()}" is not a WarpTree nor can it be transformed to a WarpTree""").failure
    }
  }

  def getTreeTyped[T](label: String, overrideDescriptor: Option[WarpDescriptor] = None)(implicit unpackers: WarpUnpackers, tag: ClassTag[T]): AlmValidation[Tree[T]] =
    getMandatory(label, x => tryGetTreeTyped[T](x, overrideDescriptor))

  private def getAndCheck[T](label: String)(checkType: WarpPackage => AlmValidation[T]): AlmValidation[Option[T]] =
    tryGetWarpPackage(label) match {
      case Some(wp) => checkType(wp).map(Some(_))
      case None => None.success
    }

  private def getMandatory[T](label: String, get: String => AlmValidation[Option[T]]): AlmValidation[T] =
    get(label).fold(
      fail => fail.failure,
      optV => optV match {
        case Some(v) => v.success
        case None => NoSuchElementProblem(s"""The WarpObject(${warpDescriptor.toString()}) contains an element with label "$label" but it has no value.""").failure
      })

  private def unpack(what: WarpPackage, overrideDescriptor: Option[WarpDescriptor], backUpDescriptor: Option[WarpDescriptor])(implicit unpackers: WarpUnpackers): AlmValidation[Any] = {
    overrideDescriptor match {
      case Some(pd) =>
        unpackers.get(pd).leftMap(old => NoSuchElementProblem(s"WarpDescriptor has been overriden", cause = Some(old))).flatMap(_(what))
      case None =>
        what match {
          case wp: WarpPrimitive => wp.value.success
          case WarpObject(td, _) =>
            td match {
              case Some(d) =>
                unpackers.get(d).fold(
                  fail => backUpDescriptor match {
                    case Some(bud) => unpackers.get(bud).flatMap(_(what))
                    case None => SerializationProblem(s"""No Unpacker found for WarpObject. Hint: The WarpObject had a ${d.toString} but no unpacker was found. There was neither a backup WarpDescriptor nor an override WarpDescriptor.""").failure
                  },
                  unpacker => unpacker(what))
              case None =>
                backUpDescriptor match {
                  case Some(bud) => unpackers.get(bud).flatMap(_(what))
                  case None => SerializationProblem("No Unpacker found for WarpObject. Hint: Neither the WarpObject contained a WarpDescriptor nor a backup WarpDescriptor or override WarpDescriptor were supplied.").failure
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
          case WarpTuple2(a, b) =>
            for {
              va <- unpack(a, None, None)
              vb <- unpack(b, None, None)
            } yield (va, vb)
          case WarpTuple3(a, b, c) =>
            for {
              va <- unpack(a, None, None)
              vb <- unpack(b, None, None)
              vc <- unpack(c, None, None)
            } yield (va, vb, vc)
        }
    }
  }

}

private class MapBasedWarpObjectLookUp(override val underlying: WarpObject) extends WarpObjectLookUp {
  private val theMap = underlying.elements.map(e => (e.label, e.value)).toMap
  override val warpDescriptor = underlying.warpDescriptor
  override def tryGetWarpPackage(label: String): Option[WarpPackage] =
    theMap.get(label) match {
      case Some(x) => x
      case None => None
    }

}

trait PackageExtractorFuns {
  def fastLookUp(obj: WarpObject): WarpObjectLookUp = new MapBasedWarpObjectLookUp(obj)
  def withFastLookUp[T](from: WarpPackage)(f: WarpObjectLookUp => AlmValidation[T]): AlmValidation[T] =
    from match {
      case wo: WarpObject => f(fastLookUp(wo))
      case x @ WarpPrimitive(v) => ArgumentProblem(s""""${x.getClass().getName()}" is not a WarpObject but a WarpPrimitive($v) so I cannot create a fast lookup""").failure
      case x @ WarpCollection(v) => {
        val valuesStrPrefix = v.mkString("[", ", ", "").ellipse(100) + "]"
          ArgumentProblem(s""""${x.getClass().getName()}" is not a WarpObject so I cannot create a fast lookup. The values in the collection are $valuesStrPrefix.""").failure
      }
      case x =>
        ArgumentProblem(s""""${x.getClass().getName()}" is not a WarpObject so I cannot create a fast lookup""").failure
    }
}