package riftwarp.impl.rematerializers

import language.higherKinds
import scala.reflect.ClassTag
import scala.collection.generic.CanBuildFrom
import scalaz._, Scalaz._
import almhirt.common._
import almhirt.almvalidation.kit._
import almhirt.serialization._
import riftwarp._
import riftwarp.components.HasRecomposers
import riftwarp.serialization.common.BlobRepresentationRecomposer

abstract class ExtractorTemplate[TDimension <: RiftDimension](val path: List[String])(implicit hasRecomposers: HasRecomposers) extends Extractor {
  type Remat = Rematerializer[TDimension]
  def rematerializer: Remat

  def blobPolicy: BlobDeserializationPolicy

  def getValue(ident: String): AlmValidation[Remat#ValueRepr]
  def spawnNew(ident: String)(value: Remat#ValueRepr): AlmValidation[Extractor]

  override def getString(ident: String): AlmValidation[String] = getValue(ident).flatMap(value => rematerializer.stringFromRepr(value))
  override def getBoolean(ident: String): AlmValidation[Boolean] = getValue(ident).flatMap(value => rematerializer.booleanFromRepr(value))
  override def getByte(ident: String): AlmValidation[Byte] = getValue(ident).flatMap(value => rematerializer.byteFromRepr(value))
  override def getInt(ident: String): AlmValidation[Int] = getValue(ident).flatMap(value => rematerializer.intFromRepr(value))
  override def getLong(ident: String): AlmValidation[Long] = getValue(ident).flatMap(value => rematerializer.longFromRepr(value))
  override def getBigInt(ident: String): AlmValidation[BigInt] = getValue(ident).flatMap(value => rematerializer.bigIntFromRepr(value))
  override def getFloat(ident: String): AlmValidation[Float] = getValue(ident).flatMap(value => rematerializer.floatFromRepr(value))
  override def getDouble(ident: String): AlmValidation[Double] = getValue(ident).flatMap(value => rematerializer.doubleFromRepr(value))
  override def getBigDecimal(ident: String): AlmValidation[BigDecimal] = getValue(ident).flatMap(value => rematerializer.bigDecimalFromRepr(value))
  override def getByteArray(ident: String): AlmValidation[Array[Byte]] = getValue(ident).flatMap(value => rematerializer.byteArrayFromRepr(value))
  override def getByteArrayFromBase64Encoding(ident: String): AlmValidation[Array[Byte]] = getValue(ident).flatMap(value => rematerializer.byteArrayFromBase64Repr(value))
  override def getByteArrayFromBlobEncoding(ident: String): AlmValidation[Array[Byte]] = getValue(ident).flatMap(value => rematerializer.byteArrayFromBlobRepr(value))
  override def getDateTime(ident: String): AlmValidation[org.joda.time.DateTime] = getValue(ident).flatMap(value => rematerializer.dateTimeFromRepr(value))
  override def getUri(ident: String): AlmValidation[_root_.java.net.URI] = getValue(ident).flatMap(value => rematerializer.uriFromRepr(value))
  override def getUuid(ident: String): AlmValidation[_root_.java.util.UUID] = getValue(ident).flatMap(value => rematerializer.uuidFromRepr(value))

  override def getWith[T](ident: String, recomposes: Extractor => AlmValidation[T]): AlmValidation[T] =
    for {
      value <- getValue(ident)
      recomposed <- rematerializer.fromReprWithExtractor(value, recomposes, spawnNew(ident))
    } yield recomposed

  override def getWithRecomposes[T](ident: String, recomposes: Recomposes[T]): AlmValidation[T] =
    getWith[T](ident, recomposes.recompose)

  override def getComplex(ident: String, descriptor: RiftDescriptor): AlmValidation[Any] =
    for {
      value <- getValue(ident)
      recomposer <- hasRecomposers.getRawRecomposer(descriptor)
      recomposed <- rematerializer.fromReprWithExtractor(value, recomposer.recomposeRaw, spawnNew(ident))
    } yield recomposed

  override def getComplexByTag[T <: AnyRef](ident: String, backupDescriptor: Option[RiftDescriptor])(implicit tag: ClassTag[T]): AlmValidation[T] =
    for {
      value <- getValue(ident)
      extractor <- spawnNew(ident)(value)
      recomposer <- hasRecomposers.lookUpFromRematerializer(extractor, backupDescriptor)
      recomposed <- recomposer.recompose(extractor)
    } yield recomposed

  override def getComplexByValueDescriptor(ident: String, backupDescriptor: Option[RiftDescriptor]): AlmValidation[AnyRef] =
    for {
      value <- getValue(ident)
      extractor <- spawnNew(ident)(value)
      recomposer <- hasRecomposers.lookUpRawFromRematerializer(extractor, backupDescriptor)
      recomposed <- recomposer.recomposeRaw(extractor)
    } yield recomposed

  override def getManyPrimitives[That[_], T](ident: String)(implicit mA: ClassTag[T], cbf: CanBuildFrom[Traversable[_], T, That[T]]): AlmValidation[That[T]] =
    for {
      value <- getValue(ident)
      resequenced <- rematerializer.resequencedOfPrimitivesFromRepr(value)
    } yield resequenced

  override def getManyWith[That[_], T](ident: String, recomposes: Extractor => AlmValidation[T])(implicit cbf: CanBuildFrom[Traversable[_], T, That[T]]): AlmValidation[That[T]] =
    for {
      value <- getValue(ident)
      resequenced <- rematerializer.resequencedMappedFromRepr(value, v => spawnNew(ident)(v).flatMap(recomposes))
    } yield resequenced

  override def getManyComplex[That[_]](ident: String, descriptor: RiftDescriptor)(implicit cbf: CanBuildFrom[Traversable[_], Any, That[Any]]): AlmValidation[That[Any]] =
    for {
      recomposer <- hasRecomposers.getRawRecomposer(descriptor)
      recomposed <- getManyWith[That, Any](ident, recomposer.recomposeRaw)
    } yield recomposed

  override def getManyComplexOfType[That[_], T <: AnyRef](ident: String, backupDescriptor: Option[RiftDescriptor])(implicit tag: ClassTag[T], cbf: CanBuildFrom[Traversable[_], T, That[T]]): AlmValidation[That[T]] =
    getManyWith[That, T](ident, extractor => extractComplexWithLookupAndCast(extractor, backupDescriptor))

  override def getManyComplexByTag[That[_], T <: AnyRef](ident: String, backupDescriptor: Option[RiftDescriptor])(implicit tag: ClassTag[T], cbf: CanBuildFrom[Traversable[_], T, That[T]]): AlmValidation[That[T]] =
    getManyWith[That, T](ident, extractor => extractComplexWithLookup(extractor, backupDescriptor))

  override def getMany[That[_]](ident: String, backupDescriptor: Option[RiftDescriptor])(implicit cbf: CanBuildFrom[Traversable[_], Any, That[Any]]): AlmValidation[That[Any]] =
    for {
      value <- getValue(ident)
      resequenced <- rematerializer.resequencedMappedFromRepr(value, v => extractPrimitiveOrComplexWithLookup(ident, v, backupDescriptor))
    } yield resequenced

  override def getMapOfPrimitives[A, B](ident: String)(implicit tagA: ClassTag[A], tagB: ClassTag[B]): AlmValidation[Map[A, B]] =
    for {
      value <- getValue(ident)
      keyMapper <- rematerializer.valueMapperFromTag[A]
      valueMapper <- rematerializer.valueMapperFromTag[B]
      traversableItems <- rematerializer.retuplelized2TraversableMappedFromRepr(value, keyMapper, valueMapper)
    } yield traversableItems.toMap

  override def getMapWith[A, B](ident: String, recomposes: Extractor => AlmValidation[B])(implicit tag: ClassTag[A]): AlmValidation[Map[A, B]] =
    for {
      value <- getValue(ident)
      keyMapper <- rematerializer.valueMapperFromTag[A]
      tupleValues <- rematerializer.traversable2FromRepr(value)
      mappedTuples <- {
        val mappedTuplesV = tupleValues.map {
          case (va, vb) =>
            (for {
              a <- keyMapper(va)
              extractorB <- spawnNew(ident)(vb)
              b <- recomposes(extractorB)
            } yield (a, b)).toAgg
        }
        mappedTuplesV.toList.sequence
      }
    } yield mappedTuples.toMap

  override def getMapComplex[A](ident: String, descriptor: RiftDescriptor)(implicit tag: ClassTag[A]): AlmValidation[Map[A, Any]] =
    for {
      recomposer <- hasRecomposers.getRawRecomposer(descriptor)
      map <- getMapWith[A, Any](ident, recomposer.recomposeRaw)
    } yield map

  override def getMapComplexByTag[A, B <: AnyRef](ident: String, backupDescriptor: Option[RiftDescriptor])(implicit tagA: ClassTag[A], tagB: ClassTag[B]): AlmValidation[Map[A, B]] =
    getMapWith[A, B](ident, extractor => extractComplexWithLookup(extractor, backupDescriptor))

  override def getMap[A](ident: String, backupDescriptor: Option[RiftDescriptor])(implicit tagA: ClassTag[A]): AlmValidation[Map[A, Any]] =
    for {
      value <- getValue(ident)
      keyMapper <- rematerializer.valueMapperFromTag[A]
      tupleValues <- rematerializer.traversable2FromRepr(value)
      mappedTuples <- {
        val mappedTuplesV = tupleValues.map {
          case (va, vb) =>
            (for {
              a <- keyMapper(va)
              b <- extractPrimitiveOrComplexWithLookup(ident, vb, backupDescriptor)
            } yield (a, b)).toAgg
        }
        mappedTuplesV.toList.sequence
      }
    } yield mappedTuples.toMap

  def getTreeOfPrimitives[T](ident: String)(implicit mA: ClassTag[T]): AlmValidation[Tree[T]] =
    for {
      value <- getValue(ident)
      mapper <- rematerializer.valueMapperFromTag[T]
      tree <- rematerializer.remappedTree(value, mapper)
    } yield tree

  def getTreeWith[T](ident: String, recomposes: Extractor => AlmValidation[T]): AlmValidation[Tree[T]] =
    for {
      value <- getValue(ident)
      tree <- rematerializer.remappedTree(value, v => spawnNew(ident)(v).flatMap(recomposes))
    } yield tree

  def getTreeOfComplex(ident: String, descriptor: RiftDescriptor): AlmValidation[Tree[AnyRef]] =
    for {
      recomposer <- hasRecomposers.getRawRecomposer(descriptor)
      tree <- getTreeWith(ident, recomposer.recomposeRaw)
    } yield tree

  def getTreeOfComplexOfType[T <: AnyRef](ident: String, backupDescriptor: Option[RiftDescriptor])(implicit tag: ClassTag[T]): AlmValidation[Tree[T]] =
    getTreeWith[T](ident, extractor => extractComplexWithLookupAndCast(extractor, backupDescriptor))

  def getTreeOfComplexByTag[T <: AnyRef](ident: String, backupDescriptor: Option[RiftDescriptor])(implicit tag: ClassTag[T]): AlmValidation[Tree[T]] =
    getTreeWith[T](ident, extractor => extractComplexWithLookup(extractor, backupDescriptor))

  def getTree(ident: String, backupDescriptor: Option[RiftDescriptor]): AlmValidation[Tree[Any]] =
    for {
      value <- getValue(ident)
      tree <- rematerializer.remappedTree(value, v => extractPrimitiveOrComplexWithLookup(ident, v, backupDescriptor))
    } yield tree

  override def getBlob(ident: String): AlmValidation[Array[Byte]] =
    blobPolicy match {
      case BlobIntegrationDisabled => getByteArrayFromBlobEncoding(ident)
      case BlobIntegrationEnabled(unpacker) =>
        for {
          value <- getValue(ident)
          blobExtractor <- spawnNew(ident)(value)
          blob <- BlobRepresentationRecomposer.recompose(blobExtractor)
          data <- blob match {
            case blobVal: BlobValue => blobVal.dataAsArray.success
            case ref: BlobReference => unpacker(ref)
          }  
        } yield data
    }

  private def extractComplexWithLookup[T <: AnyRef](extractor: Extractor, backupDescriptor: Option[RiftDescriptor])(implicit tag: ClassTag[T]): AlmValidation[T] =
    for {
      recomposer <- hasRecomposers.lookUpFromRematerializer(extractor, backupDescriptor)
      recomposed <- recomposer.recompose(extractor)
    } yield recomposed

  private def extractComplexWithLookupAndCast[T <: AnyRef](extractor: Extractor, backupDescriptor: Option[RiftDescriptor])(implicit tag: ClassTag[T]): AlmValidation[T] =
    for {
      recomposer <- hasRecomposers.lookUpRawFromRematerializer(extractor, backupDescriptor)
      recomposed <- recomposer.recomposeRaw(extractor)
      casted <- almCast[T](recomposed)
    } yield casted

  private def extractPrimitiveOrComplexWithLookup(ident: String, value: Remat#ValueRepr, backupDescriptor: Option[RiftDescriptor]): AlmValidation[Any] =
    if (rematerializer.isPrimitive(value))
      rematerializer.primitiveFromValue(value)
    else
      for {
        extractor <- spawnNew(ident)(value)
        recomposer <- hasRecomposers.lookUpRawFromRematerializer(extractor, backupDescriptor)
        recomposed <- recomposer.recomposeRaw(extractor)
      } yield recomposed

}