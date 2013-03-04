package riftwarp.impl.dematerializers

import scala.reflect.ClassTag
import scala.collection.IterableLike
import scalaz._, Scalaz._
import almhirt.common._
import almhirt.almvalidation.kit._
import riftwarp._
import riftwarp.components.HasDecomposers

trait DematerializerTemplate[TDimension <: RiftDimension] extends Dematerializer[TDimension] {

  protected def valueReprToDim(repr: ValueRepr): TDimension
  protected def dimToReprValue(dim: TDimension): ValueRepr
  protected def foldReprs(elems: Iterable[ValueRepr]): ValueRepr
  protected def getPrimitiveToRepr[A](implicit tag: ClassTag[A]): AlmValidation[(A => ValueRepr)]
  protected def getAnyPrimitiveToRepr(what: Any): AlmValidation[(Any => ValueRepr)]
  
  override def getWithRepr[A](what: A, decomposes: Decomposes[A], spawnNewSequencer: () => WarpSequencer[TDimension]): AlmValidation[ValueRepr] =
    decomposes.decompose(what, spawnNewSequencer()).map(dematerializedComplex =>
      dimToReprValue(dematerializedComplex.dematerialize))

  override def getComplexRepr[A <: AnyRef](what: A, backupRiftDescriptor: Option[RiftDescriptor], spawnNewSequencer: () => WarpSequencer[TDimension])(implicit hasDecomposers: HasDecomposers): AlmValidation[ValueRepr] =
    hasDecomposers.getDecomposerFor(what, backupRiftDescriptor).flatMap(decomposer => getWithRepr[A](what, decomposer, spawnNewSequencer))

  override def getComplexByTagRepr[A <: AnyRef](what: A, spawnNewSequencer: () => WarpSequencer[TDimension])(implicit tag: ClassTag[A], hasDecomposers: HasDecomposers): AlmValidation[ValueRepr] =
    hasDecomposers.getDecomposer[A]().flatMap(decomposer => getWithRepr[A](what, decomposer, spawnNewSequencer))

  override def getIterableAllWithRepr[A, Coll](what: IterableLike[A, Coll], decomposes: Decomposes[A], spawnNewSequencer: () => WarpSequencer[TDimension]): AlmValidation[ValueRepr] = {
    val mappedV = what.toList.map(x => decomposes.decompose(x, spawnNewSequencer()).toAgg)
    mappedV.sequence.map(dematerializedItems =>
      foldReprs(dematerializedItems.map(demat => dimToReprValue(demat.dematerialize))))
  }

  override def getIterableStrictRepr[A <: AnyRef, Coll](what: IterableLike[A, Coll], riftDesc: Option[RiftDescriptor], spawnNewSequencer: () => WarpSequencer[TDimension])(implicit tag: ClassTag[A], hasDecomposers: HasDecomposers): AlmValidation[ValueRepr] =
    hasDecomposers.getDecomposerByDescriptorAndThenByTag(riftDesc).flatMap(decomposer => getIterableAllWithRepr(what, decomposer, spawnNewSequencer))
    
  override def getIterableOfComplexRepr[A <: AnyRef, Coll](what: IterableLike[A, Coll], backupRiftDescriptor: Option[RiftDescriptor], spawnNewSequencer: () => WarpSequencer[TDimension])(implicit hasDecomposers: HasDecomposers): AlmValidation[ValueRepr] = {
    val decomposes = new Decomposes[A] {
      override def decompose[TTDimension <: RiftDimension](elem: A, into: WarpSequencer[TTDimension]): AlmValidation[WarpSequencer[TTDimension]] =
        hasDecomposers.getDecomposerFor(elem, backupRiftDescriptor).flatMap(_.decompose(elem, into))
    }
    getIterableAllWithRepr(what, decomposes, spawnNewSequencer)
  }
    
  override def getIterableOfPrimitivesRepr[A, Coll](what: IterableLike[A, Coll])(implicit tag: ClassTag[A]): AlmValidation[ValueRepr] =
    getPrimitiveToRepr[A](tag).map(primToRepr =>
      foldReprs(what.toList.map(elem => primToRepr(elem))))
      
  override def getIterableRepr[A, Coll](what: IterableLike[A, Coll], backupRiftDescriptor: Option[RiftDescriptor], spawnNewSequencer: () => WarpSequencer[TDimension])(implicit hasDecomposers: HasDecomposers): AlmValidation[ValueRepr] = {
    val mappedV = what.toList.map(x => getReprForPrimitiveOrComplex(x, backupRiftDescriptor, spawnNewSequencer).toAgg)
    val sequenced = mappedV.sequence
    sequenced.map(elems => foldReprs(elems))
  }

  override def getMapAllWithRepr[A, B](what: scala.collection.Map[A,B], decomposes: Decomposes[B], spawnNewSequencer: () => WarpSequencer[TDimension])(implicit tag: ClassTag[A]): AlmValidation[ValueRepr] =
    getPrimitiveToRepr[A](tag).flatMap { primMapper =>
      val itemsV =
        what.toSeq.map {
          case (a, b) =>
            decomposes.decompose[TDimension](b, spawnNewSequencer()).map(demat =>
              (primMapper(a), dimToReprValue(demat.dematerialize))).toAgg
        }
      val items = itemsV.toList.sequence.map(_.map { case (reprA, reprB) => foldReprs(reprA :: reprB :: Nil) })
      items.map(tuples => foldReprs(tuples))
    }
 
  override def getMapStrictRepr[A, B <: AnyRef](what: scala.collection.Map[A,B], riftDesc: Option[RiftDescriptor], spawnNewSequencer: () => WarpSequencer[TDimension])(implicit tagA: ClassTag[A], tagB: ClassTag[B], hasDecomposers: HasDecomposers): AlmValidation[ValueRepr] =
    hasDecomposers.getDecomposerByDescriptorAndThenByTag(riftDesc).flatMap(decomposer =>
      getMapAllWithRepr(what, decomposer, spawnNewSequencer))

  override def getMapOfComplexRepr[A, B <: AnyRef](what: scala.collection.Map[A,B], backupRiftDescriptor: Option[RiftDescriptor], spawnNewSequencer: () => WarpSequencer[TDimension])(implicit tag: ClassTag[A], hasDecomposers: HasDecomposers): AlmValidation[ValueRepr] = {
    val decomposes = new Decomposes[B] {
      override def decompose[TTDimension <: RiftDimension](elem: B, into: WarpSequencer[TTDimension]): AlmValidation[WarpSequencer[TTDimension]] =
        hasDecomposers.getDecomposerFor(elem, backupRiftDescriptor).flatMap(_.decompose(elem, into))
    }
    getMapAllWithRepr[A, B](what, decomposes, spawnNewSequencer)
  }
  
  override def getMapOfPrimitivesRepr[A, B](what: scala.collection.Map[A,B])(implicit tagA: ClassTag[A], tagB: ClassTag[B]): AlmValidation[ValueRepr] =
    for {
      primMapperA <- getPrimitiveToRepr[A](tagA)
      primMapperB <- getPrimitiveToRepr[B](tagB)
    } yield foldReprs(what.map { case (a, b) => foldReprs(primMapperA(a) :: primMapperB(b) :: Nil) })
  
  override def getMapRepr[A, B](what: scala.collection.Map[A,B], backupRiftDescriptor: Option[RiftDescriptor], spawnNewSequencer: () => WarpSequencer[TDimension])(implicit tag: ClassTag[A], hasDecomposers: HasDecomposers): AlmValidation[ValueRepr] =
    getPrimitiveToRepr[A](tag).flatMap { keyMapper =>
      val itemsV = what.map {
        case (a, b) => getReprForPrimitiveOrComplex(b, backupRiftDescriptor, spawnNewSequencer).map(valueRepr =>
          foldReprs(keyMapper(a) :: valueRepr :: Nil)).toAgg
      }
      itemsV.toList.sequence.map(items => foldReprs(items))
    }

  override def getMapLiberateRepr[A, B](what: scala.collection.Map[A, B], backupRiftDescriptor: Option[RiftDescriptor], spawnNewSequencer: () => WarpSequencer[TDimension])(implicit tag: ClassTag[A], hasDecomposers: HasDecomposers): AlmValidation[ValueRepr] =
    getPrimitiveToRepr[A](tag).map { keyMapper =>
      val items = what.map {
        case (a, b) => getReprForPrimitiveOrComplex(b, backupRiftDescriptor, spawnNewSequencer).map(valueRepr =>
          foldReprs(keyMapper(a) :: valueRepr :: Nil)).toOption
      }.flatten
      foldReprs(items)
    }
    
  private def getReprForPrimitiveOrComplex(what: Any, backupRiftDescriptor: Option[RiftDescriptor], spawnNewSequencer: () => WarpSequencer[TDimension])(implicit hasDecomposers: HasDecomposers): AlmValidation[ValueRepr] =
    getAnyPrimitiveToRepr(what).fold(
      fail =>
        what match {
          case ar: AnyRef => hasDecomposers.getRawDecomposerFor(ar, backupRiftDescriptor).flatMap(decomposer =>
            decomposer.decomposeRaw[TDimension](ar, spawnNewSequencer())).map(demat => dimToReprValue(demat.dematerialize))
          case x => UnspecifiedProblem(s"'${x.getClass.getName()}' is not a primitive type nor an AnyRef").failure
        },
      succ => succ(what).success)
  
  
  override def getString(aValue: String) = valueReprToDim(getStringRepr(aValue))
  override def getBoolean(aValue: Boolean) = valueReprToDim(getBooleanRepr(aValue))
  override def getByte(aValue: Byte) = valueReprToDim(getByteRepr(aValue))
  override def getInt(aValue: Int) = valueReprToDim(getIntRepr(aValue))
  override def getLong(aValue: Long) = valueReprToDim(getLongRepr(aValue))
  override def getBigInt(aValue: BigInt) = valueReprToDim(getBigIntRepr(aValue))
  override def getFloat(aValue: Float) = valueReprToDim(getFloatRepr(aValue))
  override def getDouble(aValue: Double) = valueReprToDim(getDoubleRepr(aValue))
  override def getBigDecimal(aValue: BigDecimal) = valueReprToDim(getBigDecimalRepr(aValue))
  override def getByteArray(aValue: Array[Byte]) = valueReprToDim(getByteArrayRepr(aValue))
  override def getBase64EncodedByteArray(aValue: Array[Byte]) = valueReprToDim(getBase64EncodedByteArrayRepr(aValue))
  override def getByteArrayBlobEncoded(aValue: Array[Byte]) = valueReprToDim(getByteArrayBlobEncodedRepr(aValue))
  override def getDateTime(aValue: org.joda.time.DateTime) = valueReprToDim(getDateTimeRepr(aValue))
  override def getUri(aValue: _root_.java.net.URI) = valueReprToDim(getUriRepr(aValue))
  override def getUuid(aValue: _root_.java.util.UUID) = valueReprToDim(getUuidRepr(aValue))
  override def getWith[A](what: A, decomposes: Decomposes[A], spawnNewSequencer: () => WarpSequencer[TDimension]): AlmValidation[TDimension] =
    getWithRepr(what: A, decomposes: Decomposes[A], spawnNewSequencer).map(valueReprToDim)
  override def getComplex[A <: AnyRef](what: A, backupRiftDescriptor: Option[RiftDescriptor], spawnNewSequencer: () => WarpSequencer[TDimension])(implicit hasDecomposers: HasDecomposers): AlmValidation[TDimension] =
    getComplexRepr(what: A, backupRiftDescriptor: Option[RiftDescriptor], spawnNewSequencer).map(valueReprToDim)
  override def getComplexByTag[A <: AnyRef](what: A, spawnNewSequencer: () => WarpSequencer[TDimension])(implicit tag: ClassTag[A], hasDecomposers: HasDecomposers): AlmValidation[TDimension] =
    getComplexByTagRepr(what: A, spawnNewSequencer).map(valueReprToDim)
  override def getIterableAllWith[A, Coll](what: IterableLike[A, Coll], decomposes: Decomposes[A], spawnNewSequencer: () => WarpSequencer[TDimension]): AlmValidation[TDimension] =
    getIterableAllWithRepr(what: IterableLike[A, Coll], decomposes: Decomposes[A], spawnNewSequencer).map(valueReprToDim)
  override def getIterableStrict[A <: AnyRef, Coll](what: IterableLike[A, Coll], riftDesc: Option[RiftDescriptor], spawnNewSequencer: () => WarpSequencer[TDimension])(implicit tag: ClassTag[A], hasDecomposers: HasDecomposers): AlmValidation[TDimension] =
    getIterableStrictRepr(what: IterableLike[A, Coll], riftDesc: Option[RiftDescriptor], spawnNewSequencer).map(valueReprToDim)
  override def getIterableOfComplex[A <: AnyRef, Coll](what: IterableLike[A, Coll], backupRiftDescriptor: Option[RiftDescriptor], spawnNewSequencer: () => WarpSequencer[TDimension])(implicit hasDecomposers: HasDecomposers): AlmValidation[TDimension] =
    getIterableOfComplexRepr(what: IterableLike[A, Coll], backupRiftDescriptor: Option[RiftDescriptor], spawnNewSequencer).map(valueReprToDim)
  override def getIterableOfPrimitives[A, Coll](what: IterableLike[A, Coll])(implicit tag: ClassTag[A]): AlmValidation[TDimension] =
    getIterableOfPrimitivesRepr(what: IterableLike[A, Coll]).map(valueReprToDim)
  override def getIterable[A, Coll]( what: IterableLike[A, Coll], backupRiftDescriptor: Option[RiftDescriptor], spawnNewSequencer: () => WarpSequencer[TDimension])(implicit hasDecomposers: HasDecomposers): AlmValidation[TDimension] =
    getIterableRepr(what: IterableLike[A, Coll], backupRiftDescriptor: Option[RiftDescriptor], spawnNewSequencer).map(valueReprToDim)
  override def getMapAllWith[A, B](what: scala.collection.Map[A, B], decomposes: Decomposes[B], spawnNewSequencer: () => WarpSequencer[TDimension])(implicit tag: ClassTag[A]): AlmValidation[TDimension] =
    getMapAllWithRepr(what: scala.collection.Map[A, B], decomposes: Decomposes[B], spawnNewSequencer).map(valueReprToDim)
  override def getMapStrict[A, B <: AnyRef](what: scala.collection.Map[A, B], riftDesc: Option[RiftDescriptor], spawnNewSequencer: () => WarpSequencer[TDimension])(implicit tagA: ClassTag[A], tagB: ClassTag[B], hasDecomposers: HasDecomposers): AlmValidation[TDimension] =
    getMapStrictRepr(what: scala.collection.Map[A, B], riftDesc: Option[RiftDescriptor], spawnNewSequencer).map(valueReprToDim)
  override def getMapOfComplex[A, B <: AnyRef](what: scala.collection.Map[A, B], backupRiftDescriptor: Option[RiftDescriptor], spawnNewSequencer: () => WarpSequencer[TDimension])(implicit tag: ClassTag[A], hasDecomposers: HasDecomposers): AlmValidation[TDimension] =
    getMapOfComplexRepr(what: scala.collection.Map[A, B], backupRiftDescriptor: Option[RiftDescriptor], spawnNewSequencer).map(valueReprToDim)
  override def getMapOfPrimitives[A, B](what: scala.collection.Map[A, B])(implicit tagA: ClassTag[A], tagB: ClassTag[B]): AlmValidation[TDimension] =
    getMapOfPrimitivesRepr(what: scala.collection.Map[A, B]).map(valueReprToDim)
  override def getMap[A, B](what: scala.collection.Map[A, B], backupRiftDescriptor: Option[RiftDescriptor], spawnNewSequencer: () => WarpSequencer[TDimension])(implicit tag: ClassTag[A], hasDecomposers: HasDecomposers): AlmValidation[TDimension] =
    getMapRepr(what: scala.collection.Map[A, B], backupRiftDescriptor: Option[RiftDescriptor], spawnNewSequencer).map(valueReprToDim)
  override def getMapLiberate[A, B](what: scala.collection.Map[A, B], backupRiftDescriptor: Option[RiftDescriptor], spawnNewSequencer: () => WarpSequencer[TDimension])(implicit tag: ClassTag[A], hasDecomposers: HasDecomposers): AlmValidation[TDimension] =
    getMapLiberateRepr(what: scala.collection.Map[A, B], backupRiftDescriptor: Option[RiftDescriptor], spawnNewSequencer).map(valueReprToDim)
}