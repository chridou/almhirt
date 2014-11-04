package riftwarp.std

import scala.reflect.ClassTag
import scalaz._, Scalaz._
import scalaz.Validation.FlatMap._
import almhirt.common._
import almhirt.almvalidation.kit._
import riftwarp._

trait PackageBuilderFuns {

  def toWarpPrimitive[A](what: A)(implicit conv: WarpPrimitiveConverter[A]): WarpPrimitive = conv.convertBack(what)

  def toWarpPrimitivesCollection[A](what: Traversable[A])(implicit conv: WarpPrimitiveConverter[A]): WarpCollection =
    WarpCollection(what.map(conv.convertBack(_)).toVector)

  def toWarpCollectionWith[A](what: Traversable[A], packer: WarpPacker[A])(implicit packers: WarpPackers): AlmValidation[WarpCollection] =
    what.map(item ⇒ packer(item).toAgg).toVector.sequence.map(WarpCollection.apply)

  def toWarpCollectionLookUp(what: Traversable[Any])(implicit packers: WarpPackers): AlmValidation[WarpCollection] =
    what.map(item ⇒
      packers(WarpDescriptor(item.getClass)).flatMap(_.packBlind(item)).toAgg)
      .toVector
      .sequence
      .map(WarpCollection.apply)

  def E(label: String, what: WarpPackage): AlmValidation[WarpElement] =
    WarpElement(label, Some(what)).success

  def TypeClass[A](label: String, what: A)(implicit packer: WarpPacker[A], packers: WarpPackers): AlmValidation[WarpElement] =
    With[A](label, what, packer)

  def TypeClassOpt[A](label: String, what: Option[A])(implicit packer: WarpPacker[A], packers: WarpPackers): AlmValidation[WarpElement] =
    WithOpt[A](label, what, packer)

  def TypeClassFlatOpt[A](label: String, what: Option[A])(implicit packer: WarpPacker[A]): AlmValidation[WarpElement] =
    WithOpt[A](label, what, packer)(WarpPackers.NoWarpPackers)

  def P[A: WarpPrimitiveConverter](label: String, what: A): AlmValidation[WarpElement] =
    WarpElement(label, Some(toWarpPrimitive(what))).success

  def POpt[A: WarpPrimitiveConverter](label: String, what: Option[A]): AlmValidation[WarpElement] =
    what match {
      case Some(v) ⇒ P(label, v)
      case None ⇒ WarpElement(label).success
    }

  def With[T](label: String, what: T, packer: WarpPacker[T])(implicit packers: WarpPackers): AlmValidation[WarpElement] =
    packer(what).map(x ⇒ WarpElement(label, Some(x)))

  def WithOpt[T](label: String, what: Option[T], packer: WarpPacker[T])(implicit packers: WarpPackers): AlmValidation[WarpElement] =
    what match {
      case Some(v) ⇒ packer(v).map(x ⇒ WarpElement(label, Some(x)))
      case None ⇒ WarpElement(label).success
    }

  def LookUp(label: String, what: Any)(implicit packers: WarpPackers): AlmValidation[WarpElement] =
    funs.pack(what).map(x ⇒ WarpElement(label, Some(x)))

  def LookUpOpt[T](label: String, what: Option[Any])(implicit packers: WarpPackers): AlmValidation[WarpElement] =
    what match {
      case Some(v) ⇒ funs.pack(v).map(x ⇒ WarpElement(label, Some(x)))
      case None ⇒ WarpElement(label).success
    }

  def PTup2[A: WarpPrimitiveConverter, B: WarpPrimitiveConverter](label: String, what: (A, B)): AlmValidation[WarpElement] =
    WarpElement(label, Some(WarpTuple2(toWarpPrimitive(what._1), toWarpPrimitive(what._2)))).success

  def PTup2Opt[A: WarpPrimitiveConverter, B: WarpPrimitiveConverter](label: String, what: Option[(A, B)]): AlmValidation[WarpElement] =
    what match {
      case Some(v) ⇒ PTup2(label, v)
      case None ⇒ WarpElement(label).success
    }

  def PTup3[A: WarpPrimitiveConverter, B: WarpPrimitiveConverter, C: WarpPrimitiveConverter](label: String, what: (A, B, C)): AlmValidation[WarpElement] =
    WarpElement(label, Some(WarpTuple3(toWarpPrimitive(what._1), toWarpPrimitive(what._2), toWarpPrimitive(what._3)))).success

  def PTup3Opt[A: WarpPrimitiveConverter, B: WarpPrimitiveConverter, C: WarpPrimitiveConverter](label: String, what: Option[(A, B, C)]): AlmValidation[WarpElement] =
    what match {
      case Some(v) ⇒ PTup3(label, v)
      case None ⇒ WarpElement(label).success
    }

  def CP[A: WarpPrimitiveConverter](label: String, what: Traversable[A]): AlmValidation[WarpElement] =
    WarpElement(label, Some(toWarpPrimitivesCollection(what))).success

  def CPOpt[A: WarpPrimitiveConverter](label: String, what: Option[Traversable[A]]): AlmValidation[WarpElement] =
    what match {
      case Some(v) ⇒ WarpElement(label, Some(toWarpPrimitivesCollection(v))).success
      case None ⇒ WarpElement(label).success
    }

  def CWith[A](label: String, what: Traversable[A], packer: WarpPacker[A])(implicit packers: WarpPackers): AlmValidation[WarpElement] =
    toWarpCollectionWith(what, packer).map(x ⇒ WarpElement(label, Some(x)))

  def CWithOpt[A](label: String, what: Option[Traversable[A]], packer: WarpPacker[A])(implicit packers: WarpPackers): AlmValidation[WarpElement] =
    what match {
      case Some(v) ⇒ toWarpCollectionWith(v, packer).map(x ⇒ WarpElement(label, Some(x)))
      case None ⇒ WarpElement(label).success
    }

  def CWith2[A](label: String, what: Traversable[A])(implicit  packer: WarpPacker[A], packers: WarpPackers): AlmValidation[WarpElement] =
    toWarpCollectionWith(what, packer).map(x ⇒ WarpElement(label, Some(x)))

  def CWithOpt2[A](label: String, what: Option[Traversable[A]])(implicit  packer: WarpPacker[A], packers: WarpPackers): AlmValidation[WarpElement] =
    what match {
      case Some(v) ⇒ toWarpCollectionWith(v, packer).map(x ⇒ WarpElement(label, Some(x)))
      case None ⇒ WarpElement(label).success
    }
  
  def CLookUp[A](label: String, what: Traversable[A])(implicit packers: WarpPackers): AlmValidation[WarpElement] =
    toWarpCollectionLookUp(what).map(x ⇒ WarpElement(label, Some(x)))

  def CLookUpOpt[A](label: String, what: Option[Traversable[A]])(implicit packers: WarpPackers): AlmValidation[WarpElement] =
    what match {
      case Some(v) ⇒ toWarpCollectionLookUp(v).map(x ⇒ WarpElement(label, Some(x)))
      case None ⇒ WarpElement(label).success
    }

  def MP[A, B](label: String, what: Map[A, B])(implicit convA: WarpPrimitiveConverter[A], convB: WarpPrimitiveConverter[B]): AlmValidation[WarpElement] =
    WarpElement(label, Some(WarpAssociativeCollection(what.map { case (a, b) ⇒ (convA.convertBack(a), convB.convertBack(b)) }.toVector))).success

  def MPOpt[A: WarpPrimitiveConverter, B: WarpPrimitiveConverter](label: String, what: Option[Map[A, B]]): AlmValidation[WarpElement] =
    what match {
      case Some(v) ⇒ MP[A, B](label, v)
      case None ⇒ WarpElement(label).success
    }

  def MWith[A, B](label: String, what: Map[A, B], packerB: WarpPacker[B])(implicit convA: WarpPrimitiveConverter[A], packers: WarpPackers): AlmValidation[WarpElement] =
    what.map { case (a, b) ⇒ packerB(b).map(wb ⇒ (convA.convertBack(a), wb)).toAgg }.toVector.sequence.map(items ⇒
      WarpElement(label, Some(WarpAssociativeCollection(items))))

  def MWithOpt[A: WarpPrimitiveConverter, B](label: String, what: Option[Map[A, B]], packerB: WarpPacker[B])(implicit packers: WarpPackers): AlmValidation[WarpElement] =
    what match {
      case Some(v) ⇒ MWith[A, B](label, v, packerB)
      case None ⇒ WarpElement(label).success
    }

  def MLookUp[A, B](label: String, what: Map[A, B])(implicit convA: WarpPrimitiveConverter[A], packers: WarpPackers): AlmValidation[WarpElement] =
    what.map {
      case (a, b) ⇒
        packers(WarpDescriptor(b.getClass)).flatMap(packer ⇒
          packer.packBlind(b).map(wb ⇒
            (convA.convertBack(a), wb))).toAgg
    }.toVector.sequence.map(items ⇒
      WarpElement(label, Some(WarpAssociativeCollection(items))))

  def MLookUpOpt[A: WarpPrimitiveConverter, B](label: String, what: Option[Map[A, B]])(implicit packers: WarpPackers): AlmValidation[WarpElement] =
    what match {
      case Some(v) ⇒ MLookUp(label, v)
      case None ⇒ WarpElement(label).success
    }

  def MLookUpForgiving[A, B](label: String, what: Map[A, B])(implicit convA: WarpPrimitiveConverter[A], packers: WarpPackers): AlmValidation[WarpElement] =
    what.map {
      case (a, b) ⇒
        packers(WarpDescriptor(b.getClass)).fold(
          fail ⇒
            None,
          packer ⇒
            packer.packBlind(b).map(wb ⇒
              (convA.convertBack(a), wb)).toAgg.some)
    }.flatten.toVector.sequence.map(items ⇒
      WarpElement(label, Some(WarpAssociativeCollection(items))))

  def MLookUpForgivingOpt[A: WarpPrimitiveConverter, B](label: String, what: Option[Map[A, B]])(implicit packers: WarpPackers): AlmValidation[WarpElement] =
    what match {
      case Some(v) ⇒ MLookUpForgiving(label, v)
      case None ⇒ WarpElement(label).success
    }

  def TP[A](label: String, what: Tree[A])(implicit conv: WarpPrimitiveConverter[A]): AlmValidation[WarpElement] =
    WarpElement(label, Some(WarpTree(what.map(conv.convertBack(_))))).success

  def TPOpt[A: WarpPrimitiveConverter](label: String, what: Option[Tree[A]]): AlmValidation[WarpElement] =
    what match {
      case Some(v) ⇒ TP(label, v)
      case None ⇒ WarpElement(label).success
    }

  def TWith[A](label: String, what: Tree[A], packer: WarpPacker[A])(implicit packers: WarpPackers): AlmValidation[WarpElement] =
    what.map(x ⇒ packer(x).toAgg).sequence.map(x ⇒ WarpElement(label, Some(WarpTree(x))))

  def TWithOpt[A](label: String, what: Option[Tree[A]], packer: WarpPacker[A])(implicit packers: WarpPackers): AlmValidation[WarpElement] =
    what match {
      case Some(v) ⇒ TWith(label, v, packer)
      case None ⇒ WarpElement(label).success
    }

  def TLookUp[A](label: String, what: Tree[A])(implicit packers: WarpPackers): AlmValidation[WarpElement] =
    what.map(x ⇒
      packers(WarpDescriptor(x.getClass)).flatMap(packer ⇒
        packer.packBlind(x)).toAgg).sequence.map(x ⇒
      WarpElement(label, Some(WarpTree(x))))

  def TLookUpOpt[A](label: String, what: Option[Tree[A]])(implicit packers: WarpPackers): AlmValidation[WarpElement] =
    what match {
      case Some(v) ⇒ TLookUp(label, v)
      case None ⇒ WarpElement(label).success
    }

  def Bytes(label: String, bytes: IndexedSeq[Byte]): AlmValidation[WarpElement] = BytesOpt(label, Some(bytes))
  def BytesOpt(label: String, bytes: Option[IndexedSeq[Byte]]): AlmValidation[WarpElement] = WarpElement(label, bytes.map(WarpBytes(_))).success

  def Blob(label: String, bytes: IndexedSeq[Byte]): AlmValidation[WarpElement] = BlobOpt(label, Some(bytes))
  def BlobOpt(label: String, bytes: Option[IndexedSeq[Byte]]): AlmValidation[WarpElement] = WarpElement(label, bytes.map(WarpBlob(_))).success
}

trait PackageBuilderOps {
  import language.implicitConversions

  implicit def tuple2WarpElement[T](tuple: (String, Option[T]))(implicit packer: WarpPacker[T], packers: WarpPackers): AlmValidation[WarpElement] =
    tuple._2 match {
      case None ⇒ WarpElement(tuple._1, None).success
      case Some(x) ⇒ packer(x).map(x ⇒ WarpElement(tuple._1, Some(x)))
    }

  implicit class Tuple2Ops[T](self: Tuple2[String, T]) {
    def ~>(next: ⇒ AlmValidation[WarpElement])(implicit packer: WarpPacker[T], packers: WarpPackers): AlmValidation[WarpObject] =
      next.fold(
        fail ⇒ fail.failure,
        succ ⇒ packer(self._2).map(x ⇒ WarpObject(None, Vector(WarpElement(self._1, Some(x)), succ))))

    def ⟿(next: ⇒ AlmValidation[WarpElement])(implicit packer: WarpPacker[T], packers: WarpPackers): AlmValidation[WarpObject] = ~>(next)
  }

  implicit class WarpElementOps(self: WarpElement) {
    def ~>(next: ⇒ AlmValidation[WarpElement]): AlmValidation[WarpObject] =
      next.fold(
        fail ⇒ fail.failure,
        succ ⇒ WarpObject(None, Vector(self, succ)).success)

    def ~+>(next: ⇒ WarpElement): AlmValidation[WarpObject] =
      WarpObject(None, Vector(self, next)).success

    def ~~>(next: ⇒ AlmValidation[Seq[WarpElement]]): AlmValidation[WarpObject] =
      next.fold(
        fail ⇒ fail.failure,
        succ ⇒ WarpObject(None, (self +: succ.toVector)).success)

    def ~~+>(next: ⇒ Seq[WarpElement]): AlmValidation[WarpObject] =
      WarpObject(None, (self +: next.toVector)).success

    def ~?>[T](next: ⇒ (String, Option[T]))(implicit packer: WarpPacker[T], packers: WarpPackers): AlmValidation[WarpObject] =
      ~>(tuple2WarpElement(next))

    def ~>[T](next: ⇒ (String, T))(implicit packer: WarpPacker[T], packers: WarpPackers): AlmValidation[WarpObject] =
      ~?>((next._1, Some(next._2)))

    def ⟿(next: ⇒ AlmValidation[WarpElement]): AlmValidation[WarpObject] = ~>(next)
    def ⟿?[T](next: ⇒ (String, Option[T]))(implicit packer: WarpPacker[T], packers: WarpPackers): AlmValidation[WarpObject] = ~?>(next)
    def ⟿[T](next: ⇒ (String, T))(implicit packer: WarpPacker[T], packers: WarpPackers): AlmValidation[WarpObject] = ~>[T](next)
  }

  implicit class WarpObjectOps(self: WarpObject) {
    def ~>(next: ⇒ AlmValidation[WarpElement]): AlmValidation[WarpObject] =
      next.fold(
        fail ⇒ fail.failure,
        succ ⇒ WarpObject(self.warpDescriptor, self.elements :+ succ).success)

    def ~+>(next: ⇒ WarpElement): AlmValidation[WarpObject] =
      WarpObject(self.warpDescriptor, self.elements :+ next).success

    def ~~>(next: ⇒ AlmValidation[Seq[WarpElement]]): AlmValidation[WarpObject] =
      next.fold(
        fail ⇒ fail.failure,
        succ ⇒ WarpObject(self.warpDescriptor, self.elements ++ succ).success)

    def ~~+>(next: ⇒ Seq[WarpElement]): AlmValidation[WarpObject] =
      WarpObject(self.warpDescriptor, self.elements ++ next).success

    def ~?>[T](next: ⇒ (String, Option[T]))(implicit packer: WarpPacker[T], packers: WarpPackers): AlmValidation[WarpObject] =
      ~>(tuple2WarpElement(next))

    def ~>[T](next: ⇒ (String, T))(implicit packer: WarpPacker[T], packers: WarpPackers): AlmValidation[WarpObject] =
      ~?>((next._1, Some(next._2)))

    def ⟿(next: ⇒ AlmValidation[WarpElement]): AlmValidation[WarpObject] = ~>(next)
    def ⟿?[T](next: ⇒ (String, Option[T]))(implicit packer: WarpPacker[T], packers: WarpPackers): AlmValidation[WarpObject] = ~?>[T](next)
    def ⟿[T](next: ⇒ (String, T))(implicit packer: WarpPacker[T], packers: WarpPackers): AlmValidation[WarpObject] = ~>[T](next)
  }

  implicit class WarpDescriptorOps(self: WarpDescriptor) {
    def ~>(next: ⇒ AlmValidation[WarpElement]): AlmValidation[WarpObject] =
      next.fold(
        fail ⇒ fail.failure,
        succ ⇒ WarpObject(Some(self), Vector(succ)).success)

    def ~~>(next: ⇒ AlmValidation[Seq[WarpElement]]): AlmValidation[WarpObject] =
      next.fold(
        fail ⇒ fail.failure,
        succ ⇒ WarpObject(Some(self), succ.toVector).success)

    def ~?>[T](next: ⇒ (String, Option[T]))(implicit packer: WarpPacker[T], packers: WarpPackers): AlmValidation[WarpObject] =
      ~>(tuple2WarpElement(next))

    def ~>[T](next: ⇒ (String, T))(implicit packer: WarpPacker[T], packers: WarpPackers): AlmValidation[WarpObject] =
      ~?>((next._1, Some(next._2)))

    def ⟿(next: ⇒ AlmValidation[WarpElement]): AlmValidation[WarpObject] = ~>(next)
    def ⟿?[T](next: ⇒ (String, Option[T]))(implicit packer: WarpPacker[T], packers: WarpPackers): AlmValidation[WarpObject] = ~?>[T](next)
    def ⟿[T](next: ⇒ (String, T))(implicit packer: WarpPacker[T], packers: WarpPackers): AlmValidation[WarpObject] = ~>[T](next)
  }

  implicit class WarpObjectVOps(self: AlmValidation[WarpObject]) {
    def ~>(next: ⇒ AlmValidation[WarpElement]): AlmValidation[WarpObject] =
      self.fold(
        fail ⇒ fail.failure,
        succObj ⇒
          next.fold(
            fail ⇒ fail.failure,
            succ ⇒ WarpObject(succObj.warpDescriptor, succObj.elements :+ succ).success))

    def ~+>(next: ⇒ WarpElement): AlmValidation[WarpObject] =
      self.fold(
        fail ⇒ fail.failure,
        succObj ⇒ WarpObject(succObj.warpDescriptor, succObj.elements :+ next).success)

    def ~~>(next: ⇒ AlmValidation[Seq[WarpElement]]): AlmValidation[WarpObject] =
      self.fold(
        fail ⇒ fail.failure,
        succObj ⇒
          next.fold(
            fail ⇒ fail.failure,
            succ ⇒ WarpObject(succObj.warpDescriptor, succObj.elements ++ succ).success))

    def ~~+>(next: ⇒ Seq[WarpElement]): AlmValidation[WarpObject] =
      self.fold(
        fail ⇒ fail.failure,
        succObj ⇒ WarpObject(succObj.warpDescriptor, succObj.elements ++ next).success)

    def ~?>[T](next: ⇒ (String, Option[T]))(implicit packer: WarpPacker[T], packers: WarpPackers): AlmValidation[WarpObject] =
      ~>(tuple2WarpElement(next))

    def ~>[T](next: ⇒ (String, T))(implicit packer: WarpPacker[T], packers: WarpPackers): AlmValidation[WarpObject] =
      ~?>((next._1, Some(next._2)))

    def ⟿(next: ⇒ AlmValidation[WarpElement]): AlmValidation[WarpObject] = ~>(next)
    def ⟿?[T](next: ⇒ (String, Option[T]))(implicit packer: WarpPacker[T], packers: WarpPackers): AlmValidation[WarpObject] = ~?>[T](next)
    def ⟿[T](next: ⇒ (String, T))(implicit packer: WarpPacker[T], packers: WarpPackers): AlmValidation[WarpObject] = ~>[T](next)

  }

}