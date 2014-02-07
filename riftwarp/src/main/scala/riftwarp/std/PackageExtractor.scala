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

  def getWarpPackage(label: String): AlmValidation[WarpPackage]

  def tryGetWarpPrimitive(label: String): AlmValidation[Option[WarpPrimitive]] =
    tryGetWarpPackage(label).map(wp => getWarpPrimitiveMapping(label, wp)).validationOut

  def getWarpPrimitive(label: String): AlmValidation[WarpPrimitive] =
    getWarpPackage(label).flatMap(wp => getWarpPrimitiveMapping(label, wp))

  @inline
  private def getWarpPrimitiveMapping(label: String, warpPackage: WarpPackage): AlmValidation[WarpPrimitive] =
    warpPackage match {
      case wp: WarpPrimitive => wp.success
      case x => ArgumentProblem(s""""${x.getClass().getName()}" is not a WarpPrimitive""").failure
    }

  def tryGetWarpObject(label: String): AlmValidation[Option[WarpObject]] =
    tryGetWarpPackage(label).map(wp => getWarpObjectMapping(label, wp)).validationOut

  def getWarpObject(label: String): AlmValidation[WarpObject] =
    getWarpPackage(label).flatMap(wp => getWarpObjectMapping(label, wp))

  @inline
  private def getWarpObjectMapping(label: String, warpPackage: WarpPackage): AlmValidation[WarpObject] =
    warpPackage match {
      case wo: WarpObject => wo.success
      case x => ArgumentProblem(s""""${x.getClass().getName()}" is not a WarpObject""").failure
    }

  def tryGetWarpCollection(label: String): AlmValidation[Option[WarpCollection]] =
    tryGetWarpPackage(label).map(wp => getWarpCollectionMapping(label, wp)).validationOut

  def getWarpCollection(label: String): AlmValidation[WarpCollection] =
    getWarpPackage(label).flatMap(wp => getWarpCollectionMapping(label, wp))

  @inline
  private def getWarpCollectionMapping(label: String, warpPackage: WarpPackage): AlmValidation[WarpCollection] =
    warpPackage match {
      case wc: WarpCollection => wc.success
      case x => ArgumentProblem(s""""${x.getClass().getName()}" is not a WarpCollection""").failure
    }

  def tryGetWarpAssociativeCollection(label: String): AlmValidation[Option[WarpAssociativeCollection]] =
    tryGetWarpPackage(label).map(wp => getWarpAssociativeCollectionMapping(label, wp)).validationOut

  def getWarpAssociativeCollection(label: String): AlmValidation[WarpAssociativeCollection] =
    getWarpPackage(label).flatMap(wp => getWarpAssociativeCollectionMapping(label, wp))

  @inline
  private def getWarpAssociativeCollectionMapping(label: String, warpPackage: WarpPackage): AlmValidation[WarpAssociativeCollection] =
    warpPackage match {
      case wc: WarpAssociativeCollection => wc.success
      case x => ArgumentProblem(s""""${x.getClass().getName()}" is not a WarpAssociativeCollection""").failure
    }

  def tryGetWarpTree(label: String): AlmValidation[Option[WarpTree]] =
    tryGetWarpPackage(label).map(wp => getWarpTreeMapping(label, wp)).validationOut

  def getWarpTree(label: String): AlmValidation[WarpTree] =
    getWarpPackage(label).flatMap(wp => getWarpTreeMapping(label, wp))

  @inline
  private def getWarpTreeMapping(label: String, warpPackage: WarpPackage): AlmValidation[WarpTree] =
    warpPackage match {
      case wt: WarpTree => wt.success
      case x => ArgumentProblem(s""""${x.getClass().getName()}" is not a WarpTree""").failure
    }

  def tryGetBytes(label: String): AlmValidation[Option[IndexedSeq[Byte]]] =
    tryGetWarpPackage(label).map(wp => getBytesMapping(label, wp)).validationOut

  def getBytes(label: String): AlmValidation[IndexedSeq[Byte]] =
    getWarpPackage(label).flatMap(wp => getBytesMapping(label, wp))

  @inline
  private def getBytesMapping(label: String, warpPackage: WarpPackage): AlmValidation[IndexedSeq[Byte]] =
    warpPackage match {
      case wb: WarpBytes => wb.bytes.success
      case wb: WarpBlob => wb.bytes.success
      case wc: WarpCollection => wc.items.map(WarpPrimitiveToByteConverterInst.convert(_).toAgg).sequence.map(_.toArray)
      case x => ArgumentProblem(s""""${x.getClass().getName()}" is not a byte array representation""").failure
    }

  def tryGetAs[T: WarpPrimitiveConverter](label: String): AlmValidation[Option[T]] =
    tryGetWarpPackage(label).map(wp => getAsMapping[T](label, wp)).validationOut

  def getAs[T: WarpPrimitiveConverter](label: String): AlmValidation[T] =
    getWarpPackage(label).flatMap(wp => getAsMapping[T](label, wp))

  @inline
  private def getAsMapping[T: WarpPrimitiveConverter](label: String, warpPackage: WarpPackage): AlmValidation[T] =
    warpPackage match {
      case wp: WarpPrimitive => wp.as[T]
      case x => ArgumentProblem(s""""${x.getClass().getName()}" is not a WarpPrimitive""").failure
    }

  def tryGet2As[A: WarpPrimitiveConverter, B: WarpPrimitiveConverter](label: String): AlmValidation[Option[(A, B)]] =
    tryGetWarpPackage(label).map(wp => get2AsMapping[A, B](label, wp)).validationOut

  def get2As[A: WarpPrimitiveConverter, B: WarpPrimitiveConverter](label: String): AlmValidation[(A, B)] =
    getWarpPackage(label).flatMap(wp => get2AsMapping[A, B](label, wp))

  @inline
  private def get2AsMapping[A: WarpPrimitiveConverter, B: WarpPrimitiveConverter](label: String, warpPackage: WarpPackage): AlmValidation[(A, B)] =
    warpPackage match {
      case WarpTuple2(a: WarpPrimitive, b: WarpPrimitive) => a.as[A].flatMap(a => b.as[B].map((a, _)))
      case WarpCollection(Vector(a: WarpPrimitive, b: WarpPrimitive)) => a.as[A].flatMap(a => b.as[B].map((a, _)))
      case x => ArgumentProblem(s""""${x.getClass().getName()}" is not a WarpTuple2 of primitives""").failure
    }

  def tryGet3As[A: WarpPrimitiveConverter, B: WarpPrimitiveConverter, C: WarpPrimitiveConverter](label: String): AlmValidation[Option[(A, B, C)]] =
    tryGetWarpPackage(label).map(wp => get3AsMapping[A, B, C](label, wp)).validationOut

  def get3As[A: WarpPrimitiveConverter, B: WarpPrimitiveConverter, C: WarpPrimitiveConverter](label: String): AlmValidation[(A, B, C)] =
    getWarpPackage(label).flatMap(wp => get3AsMapping[A, B, C](label, wp))

  @inline
  private def get3AsMapping[A: WarpPrimitiveConverter, B: WarpPrimitiveConverter, C: WarpPrimitiveConverter](label: String, warpPackage: WarpPackage): AlmValidation[(A, B, C)] =
    warpPackage match {
      case WarpTuple3(a: WarpPrimitive, b: WarpPrimitive, c: WarpPrimitive) => a.as[A].flatMap(a => b.as[B].flatMap(b => c.as[C].map((a, b, _))))
      case WarpCollection(Vector(a: WarpPrimitive, b: WarpPrimitive, c: WarpPrimitive)) => a.as[A].flatMap(a => b.as[B].flatMap(b => c.as[C].map((a, b, _))))
      case x => ArgumentProblem(s""""${x.getClass().getName()}" is not a WarpTuple3 of primitives""").failure
    }

  def tryGetWith[T](label: String, unpacker: WarpUnpacker[T])(implicit unpackers: WarpUnpackers): AlmValidation[Option[T]] =
    tryGetWarpPackage(label).map(unpacker(_)).validationOut

  def getWith[T](label: String, unpacker: WarpUnpacker[T])(implicit unpackers: WarpUnpackers): AlmValidation[T] =
    getWarpPackage(label).flatMap(unpacker(_))

  def tryGet(label: String, overrideDescriptor: Option[WarpDescriptor] = None, backUpDescriptor: Option[WarpDescriptor] = None)(implicit unpackers: WarpUnpackers): AlmValidation[Option[Any]] =
    tryGetWarpPackage(label).map(wp => getMapping(label, wp, overrideDescriptor, backUpDescriptor)).validationOut

  def get(label: String, overrideDescriptor: Option[WarpDescriptor] = None, backUpDescriptor: Option[WarpDescriptor] = None)(implicit unpackers: WarpUnpackers): AlmValidation[Any] =
    getWarpPackage(label).flatMap(wp => getMapping(label, wp, overrideDescriptor, backUpDescriptor))

  @inline
  private def getMapping(label: String, warpPackage: WarpPackage, overrideDescriptor: Option[WarpDescriptor], backUpDescriptor: Option[WarpDescriptor])(implicit unpackers: WarpUnpackers): AlmValidation[Any] =
    unpack(warpPackage, overrideDescriptor, backUpDescriptor)

  def tryGetTyped[T](label: String, overrideDescriptor: Option[WarpDescriptor] = None)(implicit unpackers: WarpUnpackers, tag: ClassTag[T]): AlmValidation[Option[T]] =
    tryGetWarpPackage(label).map(wp => getTypedMapping[T](label, wp, overrideDescriptor)).validationOut

  def getTyped[T](label: String, overrideDescriptor: Option[WarpDescriptor] = None)(implicit unpackers: WarpUnpackers, tag: ClassTag[T]): AlmValidation[T] =
    getWarpPackage(label).flatMap(wp => getTypedMapping[T](label, wp, overrideDescriptor))

  @inline
  private def getTypedMapping[T](label: String, warpPackage: WarpPackage, overrideDescriptor: Option[WarpDescriptor])(implicit unpackers: WarpUnpackers, tag: ClassTag[T]): AlmValidation[T] =
    unpack(warpPackage, overrideDescriptor, Some(WarpDescriptor(tag.runtimeClass))).flatMap(_.castTo[T])

  def tryGetPrimitives[T](label: String)(implicit conv: WarpPrimitiveConverter[T]): AlmValidation[Option[Vector[T]]] =
    tryGetWarpPackage(label).map(wp => getPrimitivesMapping[T](label, wp)).validationOut

  def getPrimitives[T: WarpPrimitiveConverter](label: String): AlmValidation[Vector[T]] =
    getWarpPackage(label).flatMap(wp => getPrimitivesMapping[T](label, wp))

  @inline
  private def getPrimitivesMapping[T](label: String, warpPackage: WarpPackage)(implicit conv: WarpPrimitiveConverter[T]): AlmValidation[Vector[T]] =
    warpPackage match {
      case wc: WarpCollection => wc.items.map(item => conv.convert(item).toAgg).sequence
      case x => ArgumentProblem(s"""[tryGetPrimitives("$label")]: "${x.getClass().getName()}" is not a WarpCollection""").failure
    }

  def tryGetManyWith[T](label: String, unpacker: WarpUnpacker[T])(implicit unpackers: WarpUnpackers): AlmValidation[Option[Vector[T]]] =
    tryGetWarpPackage(label).map(wp => getManyWithMapping[T](label, wp, unpacker)).validationOut

  def getManyWith[T](label: String, unpacker: WarpUnpacker[T])(implicit unpackers: WarpUnpackers): AlmValidation[Vector[T]] =
    getWarpPackage(label).flatMap(wp => getManyWithMapping[T](label, wp, unpacker))

  @inline
  private def getManyWithMapping[T](label: String, warpPackage: WarpPackage, unpacker: WarpUnpacker[T])(implicit unpackers: WarpUnpackers): AlmValidation[Vector[T]] =
    warpPackage match {
      case wc: WarpCollection => wc.items.map(item => unpacker(item).toAgg).sequence
      case x => ArgumentProblem(s"""[tryGetManyWith("$label")]: "${x.getClass().getName()}" is not a WarpCollection""").failure
    }

  def tryGetMany(label: String, overrideDescriptor: Option[WarpDescriptor] = None, backUpDescriptor: Option[WarpDescriptor] = None)(implicit unpackers: WarpUnpackers): AlmValidation[Option[Vector[Any]]] =
    tryGetWarpPackage(label).map(wp => getManyMapping(label, wp, overrideDescriptor, backUpDescriptor)).validationOut

  def getMany(label: String, overrideDescriptor: Option[WarpDescriptor] = None, backUpDescriptor: Option[WarpDescriptor] = None)(implicit unpackers: WarpUnpackers): AlmValidation[Vector[Any]] =
    getWarpPackage(label).flatMap(wp => getManyMapping(label, wp, overrideDescriptor, backUpDescriptor))

  @inline
  private def getManyMapping(label: String, warpPackage: WarpPackage, overrideDescriptor: Option[WarpDescriptor], backUpDescriptor: Option[WarpDescriptor])(implicit unpackers: WarpUnpackers): AlmValidation[Vector[Any]] =
    warpPackage match {
      case wc: WarpCollection => wc.items.map(item => unpack(item, overrideDescriptor, backUpDescriptor).toAgg).sequence
      case x => ArgumentProblem(s"""[tryGetMany("$label")]: "${x.getClass().getName()}" is not a WarpCollection""").failure
    }

  def tryGetManyTyped[T](label: String, overrideDescriptor: Option[WarpDescriptor] = None)(implicit unpackers: WarpUnpackers, tag: ClassTag[T]): AlmValidation[Option[Vector[T]]] =
    tryGetWarpPackage(label).map(wp => getManyTypedMapping[T](label, wp, overrideDescriptor)).validationOut

  def getManyTyped[T](label: String, overrideDescriptor: Option[WarpDescriptor] = None)(implicit unpackers: WarpUnpackers, tag: ClassTag[T]): AlmValidation[Vector[T]] =
    getWarpPackage(label).flatMap(wp => getManyTypedMapping[T](label, wp, overrideDescriptor))

  @inline
  private def getManyTypedMapping[T](label: String, warpPackage: WarpPackage, overrideDescriptor: Option[WarpDescriptor])(implicit unpackers: WarpUnpackers, tag: ClassTag[T]): AlmValidation[Vector[T]] =
    warpPackage match {
      case wc: WarpCollection => wc.items.map(item => unpack(item, overrideDescriptor, Some(WarpDescriptor(tag.runtimeClass))).flatMap(_.castTo[T]).toAgg).sequence
      case x => ArgumentProblem(s"""[tryGetManyTyped("$label")]: "${x.getClass().getName()}" is not a WarpCollection""").failure
    }

  def tryGetPrimitiveAssocs[A, B](label: String)(implicit convA: WarpPrimitiveConverter[A], convB: WarpPrimitiveConverter[B]): AlmValidation[Option[Vector[(A, B)]]] =
    tryGetWarpPackage(label).map(wp => getPrimitiveAssocsMapping[A, B](label, wp)).validationOut

  def getPrimitiveAssocs[A: WarpPrimitiveConverter, B: WarpPrimitiveConverter](label: String): AlmValidation[Vector[(A, B)]] =
    getWarpPackage(label).flatMap(wp => getPrimitiveAssocsMapping[A, B](label, wp))

  @inline
  private def getPrimitiveAssocsMapping[A, B](label: String, warpPackage: WarpPackage)(implicit convA: WarpPrimitiveConverter[A], convB: WarpPrimitiveConverter[B]): AlmValidation[Vector[(A, B)]] = {
    @inline
    def mapThem(wa: WarpAssociativeCollection) = wa.items.map(item => convA.convert(item._1).flatMap(a => convB.convert(item._2).map(b => (a, b))).toAgg).sequence
    warpPackage match {
      case wa: WarpAssociativeCollection => mapThem(wa)
      case wc: WarpCollection => wc.associative.flatMap(mapThem(_))
      case x => ArgumentProblem(s"""[tryGetPrimitiveAssocs]("$label")]: "${x.getClass().getName()}" is not a WarpAssociativeCollection nor can it be transformed to a WarpAssociativeCollection""").failure
    }
  }

  def tryGetAssocsEachWith[A, B](label: String, unpackerA: WarpUnpacker[A], unpackerB: WarpUnpacker[B])(implicit unpackers: WarpUnpackers): AlmValidation[Option[Vector[(A, B)]]] =
    tryGetWarpPackage(label).map(wp => getAssocsEachWithMapping[A, B](label, wp, unpackerA, unpackerB)).validationOut

  def getAssocsEachWith[A, B](label: String, unpackerA: WarpUnpacker[A], unpackerB: WarpUnpacker[B])(implicit unpackers: WarpUnpackers): AlmValidation[Vector[(A, B)]] =
    getWarpPackage(label).flatMap(wp => getAssocsEachWithMapping[A, B](label, wp, unpackerA, unpackerB))

  @inline
  private def getAssocsEachWithMapping[A, B](label: String, warpPackage: WarpPackage, unpackerA: WarpUnpacker[A], unpackerB: WarpUnpacker[B])(implicit unpackers: WarpUnpackers): AlmValidation[Vector[(A, B)]] = {
    @inline
    def mapThem(wa: WarpAssociativeCollection) = wa.items.map(item => unpackerA(item._1).flatMap(a => unpackerB(item._2).map(b => (a, b))).toAgg).sequence
    warpPackage match {
      case wa: WarpAssociativeCollection => mapThem(wa)
      case wc: WarpCollection => wc.associative.flatMap(mapThem(_))
      case x => ArgumentProblem(s"""[tryGetAssocsEachWith("$label")]: "${x.getClass().getName()}" is not a WarpAssociativeCollection nor can it be transformed to a WarpAssociativeCollection""").failure
    }
  }

  def tryGetAssocsWith[A, B](label: String, unpackerB: WarpUnpacker[B])(implicit convA: WarpPrimitiveConverter[A], unpackers: WarpUnpackers): AlmValidation[Option[Vector[(A, B)]]] =
    tryGetWarpPackage(label).map(wp => getAssocsWithMapping[A, B](label, wp, unpackerB)).validationOut

  def getAssocsWith[A: WarpPrimitiveConverter, B](label: String, unpackerB: WarpUnpacker[B])(implicit unpackers: WarpUnpackers): AlmValidation[Vector[(A, B)]] =
    getWarpPackage(label).flatMap(wp => getAssocsWithMapping[A, B](label, wp, unpackerB))

  @inline
  private def getAssocsWithMapping[A, B](label: String, warpPackage: WarpPackage, unpackerB: WarpUnpacker[B])(implicit convA: WarpPrimitiveConverter[A], unpackers: WarpUnpackers): AlmValidation[Vector[(A, B)]] = {
    @inline
    def mapThem(wa: WarpAssociativeCollection) = wa.items.map(item => convA.convert(item._1).flatMap(a => unpackerB(item._2).map(b => (a, b))).toAgg).sequence
    warpPackage match {
      case wa: WarpAssociativeCollection => mapThem(wa)
      case wc: WarpCollection => wc.associative.flatMap(mapThem(_))
      case x => ArgumentProblem(s"""[tryGetAssocsWith("$label")]: "${x.getClass().getName()}" is not a WarpAssociativeCollection nor can it be transformed to a WarpAssociativeCollection""").failure
    }
  }

  def tryGetAssocs[A](label: String, overrideDescriptor: Option[WarpDescriptor] = None, backUpDescriptor: Option[WarpDescriptor] = None)(implicit unpackers: WarpUnpackers, conv: WarpPrimitiveConverter[A]): AlmValidation[Option[Vector[(A, Any)]]] =
    tryGetWarpPackage(label).map(wp => getAssocsMapping[A](label, wp, overrideDescriptor)).validationOut

  def getAssocs[A](label: String, overrideDescriptor: Option[WarpDescriptor] = None, backUpDescriptor: Option[WarpDescriptor] = None)(implicit unpackers: WarpUnpackers, conv: WarpPrimitiveConverter[A]): AlmValidation[Vector[(A, Any)]] =
    getWarpPackage(label).flatMap(wp => getAssocsMapping[A](label, wp, overrideDescriptor))

  @inline
  private def getAssocsMapping[A](label: String, warpPackage: WarpPackage, overrideDescriptor: Option[WarpDescriptor] = None, backUpDescriptor: Option[WarpDescriptor] = None)(implicit unpackers: WarpUnpackers, conv: WarpPrimitiveConverter[A]): AlmValidation[Vector[(A, Any)]] = {
    @inline
    def mapThem(wa: WarpAssociativeCollection) = wa.items.map(item => conv.convert(item._1).flatMap(a => unpack(item._2, overrideDescriptor, backUpDescriptor).map(b => (a, b))).toAgg).sequence
    warpPackage match {
      case wa: WarpAssociativeCollection => mapThem(wa)
      case wc: WarpCollection => wc.associative.flatMap(mapThem(_))
      case x => ArgumentProblem(s"""[tryGetAssocs("$label")]: "${x.getClass().getName()}" is not a WarpAssociativeCollection nor can it be transformed to a WarpAssociativeCollection""").failure
    }
  }

  def tryGetAssocsTyped[A, B](label: String, overrideDescriptor: Option[WarpDescriptor] = None)(implicit unpackers: WarpUnpackers, tag: ClassTag[B], conv: WarpPrimitiveConverter[A]): AlmValidation[Option[Vector[(A, B)]]] =
    tryGetWarpPackage(label).map(wp => getAssocsTypedMapping[A, B](label, wp, overrideDescriptor)).validationOut

  def getAssocsTyped[A, B](label: String, overrideDescriptor: Option[WarpDescriptor] = None)(implicit unpackers: WarpUnpackers, tag: ClassTag[B], conv: WarpPrimitiveConverter[A]): AlmValidation[Vector[(A, B)]] =
    getWarpPackage(label).flatMap(wp => getAssocsTypedMapping[A, B](label, wp, overrideDescriptor))

  @inline
  private def getAssocsTypedMapping[A, B](label: String, warpPackage: WarpPackage, overrideDescriptor: Option[WarpDescriptor])(implicit unpackers: WarpUnpackers, tag: ClassTag[B], conv: WarpPrimitiveConverter[A]): AlmValidation[Vector[(A, B)]] = {
    @inline
    def mapThem(wa: WarpAssociativeCollection) = wa.items.map(item =>
      conv.convert(item._1).flatMap(a =>
        unpack(item._2, overrideDescriptor, Some(WarpDescriptor(tag.runtimeClass))).flatMap(_.castTo[B]).map(b =>
          (a, b))).toAgg).sequence

    warpPackage match {
      case wa: WarpAssociativeCollection => mapThem(wa)
      case wc: WarpCollection => wc.associative.flatMap(mapThem(_))
      case x => ArgumentProblem(s"""[tryGetAssocsTyped("$label")]: "${x.getClass().getName()}" is not a WarpAssociativeCollection nor can it be transformed to a WarpAssociativeCollection""").failure
    }
  }

  def tryGetPrimitivesTree[T: WarpPrimitiveConverter](label: String): AlmValidation[Option[Tree[T]]] =
    tryGetWarpPackage(label).map(wp => getPrimitivesTreeMapping[T](label, wp)).validationOut

  def getPrimitivesTree[T: WarpPrimitiveConverter](label: String): AlmValidation[Tree[T]] =
    getWarpPackage(label).flatMap(wp => getPrimitivesTreeMapping[T](label, wp))

  @inline
  private def getPrimitivesTreeMapping[T: WarpPrimitiveConverter](label: String, warpPackage: WarpPackage)(implicit conv: WarpPrimitiveConverter[T]): AlmValidation[Tree[T]] = {
    @inline
    def mapThem(wt: WarpTree) = wt.tree.map(item => conv.convert(item).toAgg).sequence
    warpPackage match {
      case wt: WarpTree => mapThem(wt)
      case wc: WarpCollection => wc.warpTree.flatMap(mapThem(_))
      case x => ArgumentProblem(s"""[tryGetPrimitivesTree("$label")]: "${x.getClass().getName()}" is not a WarpTree nor can it be transformed to a WarpTree""").failure
    }
  }

  def tryGetTreeWith[T](label: String, unpacker: WarpUnpacker[T])(implicit unpackers: WarpUnpackers): AlmValidation[Option[Tree[T]]] =
    tryGetWarpPackage(label).map(wp => getTreeWithMapping(label, wp, unpacker)).validationOut

  def getTreeWith[T](label: String, unpacker: WarpUnpacker[T])(implicit unpackers: WarpUnpackers): AlmValidation[Tree[T]] =
    getWarpPackage(label).flatMap(wp => getTreeWithMapping(label, wp, unpacker))

  @inline
  private def getTreeWithMapping[T](label: String, warpPackage: WarpPackage, unpacker: WarpUnpacker[T])(implicit unpackers: WarpUnpackers): AlmValidation[Tree[T]] = {
    def mapThem(wt: WarpTree) = wt.tree.map(item => unpacker(item).toAgg).sequence
    warpPackage match {
      case wt: WarpTree => mapThem(wt)
      case wc: WarpCollection => wc.warpTree.flatMap(mapThem(_))
      case x => ArgumentProblem(s"""[tryGetTreeWith("$label")]: "${x.getClass().getName()}" is not a WarpTree nor can it be transformed to a WarpTree""").failure
    }
  }

  def tryGetTree(label: String, overrideDescriptor: Option[WarpDescriptor] = None, backUpDescriptor: Option[WarpDescriptor] = None)(implicit unpackers: WarpUnpackers): AlmValidation[Option[Tree[Any]]] =
    tryGetWarpPackage(label).map(wp => getTreeMapping(label, wp, overrideDescriptor, backUpDescriptor)).validationOut

  def getTree[T](label: String, overrideDescriptor: Option[WarpDescriptor] = None, backUpDescriptor: Option[WarpDescriptor] = None)(implicit unpackers: WarpUnpackers): AlmValidation[Tree[Any]] =
    getWarpPackage(label).flatMap(wp => getTreeMapping(label, wp, overrideDescriptor, backUpDescriptor))

  @inline
  private def getTreeMapping[T](label: String, warpPackage: WarpPackage, overrideDescriptor: Option[WarpDescriptor], backUpDescriptor: Option[WarpDescriptor])(implicit unpackers: WarpUnpackers) = {
    def mapThem(wt: WarpTree) = wt.tree.map(item => unpack(item, overrideDescriptor, backUpDescriptor).toAgg).sequence
    warpPackage match {
      case wt: WarpTree => mapThem(wt)
      case wc: WarpCollection => wc.warpTree.flatMap(mapThem(_))
      case x => ArgumentProblem(s"""[tryGetTree("$label")]: "${x.getClass().getName()}" is not a WarpTree nor can it be transformed to a WarpTree""").failure
    }
  }

  def tryGetTreeTyped[T](label: String, overrideDescriptor: Option[WarpDescriptor] = None)(implicit unpackers: WarpUnpackers, tag: ClassTag[T]): AlmValidation[Option[Tree[T]]] = {
    tryGetWarpPackage(label).map(wp => getTreeTypedMapping(label, wp, overrideDescriptor)).validationOut
  }

  def getTreeTyped[T](label: String, overrideDescriptor: Option[WarpDescriptor] = None)(implicit unpackers: WarpUnpackers, tag: ClassTag[T]): AlmValidation[Tree[T]] =
    getWarpPackage(label).flatMap(wp => getTreeTypedMapping(label, wp, overrideDescriptor))

  @inline
  private def getTreeTypedMapping[T](label: String, warpPackage: WarpPackage, overrideDescriptor: Option[WarpDescriptor])(implicit unpackers: WarpUnpackers, tag: ClassTag[T]): AlmValidation[Tree[T]] = {
    def mapThem(wt: WarpTree) = wt.tree.map(item => unpack(item, overrideDescriptor, Some(WarpDescriptor(tag.runtimeClass))).flatMap(_.castTo[T]).toAgg).sequence
    warpPackage match {
      case wt: WarpTree => mapThem(wt)
      case wc: WarpCollection => wc.warpTree.flatMap(mapThem(_))
      case x => ArgumentProblem(s"""[tryGetTreeTyped("$label")]: "${x.getClass().getName()}" is not a WarpTree nor can it be transformed to a WarpTree""").failure
    }
  }

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

  def getWarpPackage(label: String): AlmValidation[WarpPackage] =
    theMap.get(label) match {
      case Some(Some(wp)) =>
        wp.success
      case Some(None) =>
        val wdString = underlying.warpDescriptor.map(_.toString).getOrElse("no WarpDescriptor")
        MandatoryDataProblem(s"""The WarpObject($wdString) does contain an element with label "$label" but it is set to None.""").failure
      case None =>
        val wdString = underlying.warpDescriptor.map(_.toString).getOrElse("no WarpDescriptor")
        val labelsStr = underlying.elements.map(_.label).mkString("[", ",", "]")
        MandatoryDataProblem(s"""The WarpObject($wdString) does not contain an element with label "$label". The following labels were found: $labelsStr.""").failure
    }
}

trait PackageExtractorFuns {
  def fastLookUp(obj: WarpObject): WarpObjectLookUp = new MapBasedWarpObjectLookUp(obj)

  def withFastLookUp[T](from: WarpPackage)(f: WarpObjectLookUp => AlmValidation[T]): AlmValidation[T] = withFastLookUpAndCallerOpt(from, None)(f)

  def withFastLookUp[T](from: WarpPackage, caller: HasWarpDescriptor)(f: WarpObjectLookUp => AlmValidation[T]): AlmValidation[T] = withFastLookUpAndCallerOpt(from, Some(caller))(f)

  def withFastLookUpAndCallerOpt[T](from: WarpPackage, caller: Option[HasWarpDescriptor])(f: WarpObjectLookUp => AlmValidation[T]): AlmValidation[T] =
    from match {
      case wo: WarpObject => f(fastLookUp(wo)).leftMap(p => SerializationProblem(s"""A problem was encountered on unpacking a WarpObject. I was called from someone with the following warp descriptor: ${caller.map(_.warpDescriptor)}""", cause = Some(p)))
      case x @ WarpPrimitive(v) => ArgumentProblem(s""""${x.getClass().getName()}" is not a WarpObject but a WarpPrimitive($v) so I cannot create a fast lookup. I was called from someone with the following warp descriptor: ${caller.map(_.warpDescriptor)}""").failure
      case x @ WarpCollection(v) => {
        val valuesStrPrefix = v.mkString("[", ", ", "").ellipse(100) + "]"
        ArgumentProblem(s""""${x.getClass().getName()}" is not a WarpObject so I cannot create a fast lookup. The values in the collection are $valuesStrPrefix. I was called from someone with the following warp descriptor: ${caller.map(_.warpDescriptor)}""").failure
      }
      case x =>
        ArgumentProblem(s""""${x.getClass().getName()}" is not a WarpObject so I cannot create a fast lookup. I was called from someone with the following warp descriptor: ${caller.map(_.warpDescriptor)}""").failure
    }

}