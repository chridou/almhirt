package riftwarp.impl.rematerializers

import language.higherKinds

import scala.collection.generic.CanBuildFrom
import scala.reflect.ClassTag
import scalaz._, Scalaz._
import almhirt.common._
import almhirt.almvalidation.kit._
import riftwarp._

abstract class RematerializerTemplate[TDimension <: RiftDimension] extends Rematerializer[TDimension] {

  override def getString(from: TDimension): AlmValidation[String] = stringFromRepr(from.manifestation)
  override def getBoolean(from: TDimension): AlmValidation[Boolean] = booleanFromRepr(from.manifestation)
  override def getByte(from: TDimension): AlmValidation[Byte] = byteFromRepr(from.manifestation)
  override def getInt(from: TDimension): AlmValidation[Int] = intFromRepr(from.manifestation)
  override def getLong(from: TDimension): AlmValidation[Long] = longFromRepr(from.manifestation)
  override def getBigInt(from: TDimension): AlmValidation[BigInt] = bigIntFromRepr(from.manifestation)
  override def getFloat(from: TDimension): AlmValidation[Float] = floatFromRepr(from.manifestation)
  override def getDouble(from: TDimension): AlmValidation[Double] = doubleFromRepr(from.manifestation)
  override def getBigDecimal(from: TDimension): AlmValidation[BigDecimal] = bigDecimalFromRepr(from.manifestation)
  override def getByteArray(from: TDimension): AlmValidation[Array[Byte]] = byteArrayFromRepr(from.manifestation)
  override def getByteArrayFromBase64Encoding(from: TDimension): AlmValidation[Array[Byte]] = byteArrayFromBase64Repr(from.manifestation)
  override def getByteArrayFromBlobEncoding(from: TDimension): AlmValidation[Array[Byte]] = byteArrayFromBlobRepr(from.manifestation)
  override def getDateTime(from: TDimension): AlmValidation[org.joda.time.DateTime] = dateTimeFromRepr(from.manifestation)
  override def getUri(from: TDimension): AlmValidation[_root_.java.net.URI] = uriFromRepr(from.manifestation)
  override def getUuid(from: TDimension): AlmValidation[_root_.java.util.UUID] = uuidFromRepr(from.manifestation)

  override def fromReprWithExtractor[T](value: ValueRepr, f: Extractor => AlmValidation[T], createExtractor: ValueRepr => AlmValidation[Extractor]): AlmValidation[T] =
    for {
      extractor <- createExtractor(value)
      recomposed <- f(extractor)
    } yield recomposed
  
  override def traversable2FromRepr(value: ValueRepr): AlmValidation[Traversable[(ValueRepr, ValueRepr)]] =
    traversableOfReprFromRepr(value).flatMap(items => {
      val tuplesV = items.map(tuple2OfReprFromRepr(_).toAgg).toList.sequence
      tuplesV
    })

  override def collectionOfReprFromRepr[That[_]](value: ValueRepr)(implicit cbf: CanBuildFrom[Traversable[_], ValueRepr, That[ValueRepr]]): AlmValidation[That[ValueRepr]] =
    traversableOfReprFromRepr(value).map { items =>
      val builder = cbf()
      items.foreach(x => builder += x)
      builder.result
    }

  override def resequencedMappedFromRepr[That[_], T](value: ValueRepr, f: ValueRepr => AlmValidation[T])(implicit cbf: CanBuildFrom[Traversable[_], T, That[T]]): AlmValidation[That[T]] =
    collectionOfReprFromRepr(value).flatMap { reprItems =>
      val itemsV = reprItems.toList.map(f(_).toAgg).sequence
      itemsV.map(items => {
        val builder = cbf()
        items.foreach(x => builder += x)
        builder.result
      })
    }

  override def resequencedOfPrimitivesFromRepr[That[_], T](value: ValueRepr)(implicit tag: ClassTag[T], cbf: CanBuildFrom[Traversable[_], T, That[T]]): AlmValidation[That[T]] =
    for {
      mapper <- valueMapperFromTag[T]
      resequenced <- resequencedMappedFromRepr(value, mapper)
    } yield resequenced

  override def retuplelized2MappedFromRepr[A, B](value: ValueRepr, fa: ValueRepr => AlmValidation[A], fb: ValueRepr => AlmValidation[B]): AlmValidation[(A, B)] =
    tuple2OfReprFromRepr(value).flatMap(ab =>
      for {
        r1 <- fa(ab._1)
        r2 <- fb(ab._2)
      } yield (r1, r2))

  override def retuplelized2TraversableMappedFromRepr[A, B](value: ValueRepr, fa: ValueRepr => AlmValidation[A], fb: ValueRepr => AlmValidation[B]): AlmValidation[Traversable[(A, B)]] =
    for {
      traversable <- traversableOfReprFromRepr(value)
      tuples <- traversable.map(retuplelized2MappedFromRepr[A, B](_, fa, fb).toAgg).toList.sequence
    } yield tuples

  override def treeOfRepr(value: ValueRepr): AlmValidation[scalaz.Tree[ValueRepr]] = 
    ???
    
  override def remappedTree[T](value: ValueRepr, f: ValueRepr => AlmValidation[T]): AlmValidation[scalaz.Tree[T]] = 
    ???

}