package riftwarp.impl.rematerializers

import language.higherKinds
import scala.collection.generic.CanBuildFrom
import scala.reflect.ClassTag
import scalaz.syntax.validation._
import almhirt.common._
import riftwarp._

trait NoneHandlingExtractor extends Extractor {
  def hasValue(ident: String): Boolean

  override def tryGetString(ident: String): AlmValidation[Option[String]] =
    if (hasValue(ident)) getString(ident).map(Some(_)) else None.success

  override def tryGetBoolean(ident: String): AlmValidation[Option[Boolean]] =
    if (hasValue(ident)) getBoolean(ident).map(Some(_)) else None.success

  override def tryGetByte(ident: String): AlmValidation[Option[Byte]] =
    if (hasValue(ident)) getByte(ident).map(Some(_)) else None.success
  override def tryGetInt(ident: String): AlmValidation[Option[Int]] =
    if (hasValue(ident)) getInt(ident).map(Some(_)) else None.success
  override def tryGetLong(ident: String): AlmValidation[Option[Long]] =
    if (hasValue(ident)) getLong(ident).map(Some(_)) else None.success
  override def tryGetBigInt(ident: String): AlmValidation[Option[BigInt]] =
    if (hasValue(ident)) getBigInt(ident).map(Some(_)) else None.success

  override def tryGetFloat(ident: String): AlmValidation[Option[Float]] =
    if (hasValue(ident)) getFloat(ident).map(Some(_)) else None.success
  override def tryGetDouble(ident: String): AlmValidation[Option[Double]] =
    if (hasValue(ident)) getDouble(ident).map(Some(_)) else None.success
  override def tryGetBigDecimal(ident: String): AlmValidation[Option[BigDecimal]] =
    if (hasValue(ident)) getBigDecimal(ident).map(Some(_)) else None.success

  override def tryGetByteArray(ident: String): AlmValidation[Option[Array[Byte]]] =
    if (hasValue(ident)) getByteArray(ident).map(Some(_)) else None.success
  override def tryGetByteArrayFromBase64Encoding(ident: String): AlmValidation[Option[Array[Byte]]] =
    if (hasValue(ident)) getByteArrayFromBase64Encoding(ident).map(Some(_)) else None.success
  override def tryGetByteArrayFromBlobEncoding(ident: String): AlmValidation[Option[Array[Byte]]] =
    if (hasValue(ident)) getByteArrayFromBlobEncoding(ident).map(Some(_)) else None.success

  override def tryGetDateTime(ident: String): AlmValidation[Option[org.joda.time.DateTime]] =
    if (hasValue(ident)) getDateTime(ident).map(Some(_)) else None.success

  override def tryGetUri(ident: String): AlmValidation[Option[_root_.java.net.URI]] =
    if (hasValue(ident)) getUri(ident).map(Some(_)) else None.success

  override def tryGetUuid(ident: String): AlmValidation[Option[_root_.java.util.UUID]] =
    if (hasValue(ident)) getUuid(ident).map(Some(_)) else None.success

  override def tryGetWith[T](ident: String, recomposes: Extractor => AlmValidation[T]): AlmValidation[Option[T]] =
    if (hasValue(ident)) getWith[T](ident, recomposes).map(Some(_)) else None.success
  override def tryGetWithRecomposes[T](ident: String, recomposes: Recomposes[T]): AlmValidation[Option[T]] =
    if (hasValue(ident)) getWithRecomposes[T](ident, recomposes).map(Some(_)) else None.success
  override def tryGetComplex(ident: String, descriptor: RiftDescriptor): AlmValidation[Option[Any]] =
    if (hasValue(ident)) getComplex(ident, descriptor).map(Some(_)) else None.success
  override def tryGetComplexByTag[T <: AnyRef](ident: String, backupDescriptor: Option[RiftDescriptor])(implicit tag: ClassTag[T]): AlmValidation[Option[T]] =
    if (hasValue(ident)) getComplexByTag[T](ident, backupDescriptor).map(Some(_)) else None.success
  override def tryGetComplexByValueDescriptor(ident: String, backupDescriptor: Option[RiftDescriptor]): AlmValidation[Option[AnyRef]]=
    if (hasValue(ident)) getComplexByValueDescriptor(ident, backupDescriptor).map(Some(_)) else None.success

  override def tryGetManyPrimitives[That[_], T](ident: String)(implicit mA: ClassTag[T], cbf: CanBuildFrom[Traversable[_], T, That[T]]): AlmValidation[Option[That[T]]] =
    if (hasValue(ident)) getManyPrimitives[That, T](ident).map(Some(_)) else None.success
  override def tryGetManyWith[That[_], T](ident: String, recomposes: Extractor => AlmValidation[T])(implicit cbf: CanBuildFrom[Traversable[_], T, That[T]]): AlmValidation[Option[That[T]]] =
    if (hasValue(ident)) getManyWith[That, T](ident, recomposes).map(Some(_)) else None.success
  override def tryGetManyComplex[That[_]](ident: String, descriptor: RiftDescriptor)(implicit cbf: CanBuildFrom[Traversable[_], Any, That[Any]]): AlmValidation[Option[That[Any]]] =
    if (hasValue(ident)) getManyComplex[That](ident, descriptor).map(Some(_)) else None.success
  override def tryGetManyComplexOfType[That[_], T <: AnyRef](ident: String, backupDescriptor: Option[RiftDescriptor])(implicit tag: ClassTag[T], cbf: CanBuildFrom[Traversable[_], T, That[T]]): AlmValidation[Option[That[T]]] =
    if (hasValue(ident)) getManyComplexOfType[That, T](ident, backupDescriptor).map(Some(_)) else None.success
  override def tryGetManyComplexByTag[That[_], T <: AnyRef](ident: String, backupDescriptor: Option[RiftDescriptor])(implicit tag: ClassTag[T], cbf: CanBuildFrom[Traversable[_], T, That[T]]): AlmValidation[Option[That[T]]] =
    if (hasValue(ident)) getManyComplexByTag[That, T](ident, backupDescriptor).map(Some(_)) else None.success
  override def tryGetMany[That[_]](ident: String, backupDescriptor: Option[RiftDescriptor])(implicit cbf: CanBuildFrom[Traversable[_], Any, That[Any]]): AlmValidation[Option[That[Any]]] =
    if (hasValue(ident)) getMany[That](ident, backupDescriptor).map(Some(_)) else None.success

  override def tryGetMapOfPrimitives[A, B](ident: String)(implicit tagA: ClassTag[A], tagB: ClassTag[B]): AlmValidation[Option[Map[A, B]]] =
    if (hasValue(ident)) getMapOfPrimitives[A, B](ident).map(Some(_)) else None.success
  override def tryGetMapWith[A, B](ident: String, recomposes: Extractor => AlmValidation[B])(implicit tag: ClassTag[A]): AlmValidation[Option[Map[A, B]]] =
    if (hasValue(ident)) getMapWith[A, B](ident, recomposes).map(Some(_)) else None.success
  override def tryGetMapComplex[A](ident: String, descriptor: RiftDescriptor)(implicit tag: ClassTag[A]): AlmValidation[Option[Map[A, Any]]] =
    if (hasValue(ident)) getMapComplex[A](ident, descriptor).map(Some(_)) else None.success
  override def tryGetMapComplexByTag[A, B <: AnyRef](ident: String, backupDescriptor: Option[RiftDescriptor])(implicit tagA: ClassTag[A], tagB: ClassTag[B]): AlmValidation[Option[Map[A, B]]] =
    if (hasValue(ident)) getMapComplexByTag[A, B](ident, backupDescriptor).map(Some(_)) else None.success
  override def tryGetMap[A](ident: String, backupDescriptor: Option[RiftDescriptor])(implicit tagA: ClassTag[A]): AlmValidation[Option[Map[A, Any]]] =
    if (hasValue(ident)) getMap[A](ident, backupDescriptor).map(Some(_)) else None.success

  import scalaz.Tree
  override def tryGetTreeOfPrimitives[T](ident: String)(implicit mA: ClassTag[T]): AlmValidation[Option[Tree[T]]] =
    if (hasValue(ident)) getTreeOfPrimitives[T](ident).map(Some(_)) else None.success
  override def tryGetTreeWith[T](ident: String, recomposes: Extractor => AlmValidation[T]): AlmValidation[Option[Tree[T]]] =
    if (hasValue(ident)) getTreeWith[T](ident, recomposes).map(Some(_)) else None.success
  override def tryGetTreeOfComplex(ident: String, descriptor: RiftDescriptor): AlmValidation[Option[Tree[AnyRef]]] =
    if (hasValue(ident)) getTreeOfComplex(ident, descriptor).map(Some(_)) else None.success
  override def tryGetTreeOfComplexOfType[T <: AnyRef](ident: String, backupDescriptor: Option[RiftDescriptor])(implicit tag: ClassTag[T]): AlmValidation[Option[Tree[T]]] =
    if (hasValue(ident)) getTreeOfComplexOfType[T](ident, backupDescriptor).map(Some(_)) else None.success
  override def tryGetTreeOfComplexByTag[T <: AnyRef](ident: String, backupDescriptor: Option[RiftDescriptor])(implicit tag: ClassTag[T]): AlmValidation[Option[Tree[T]]] =
    if (hasValue(ident)) getTreeOfComplexByTag[T](ident, backupDescriptor).map(Some(_)) else None.success
  override def tryGetTree(ident: String, backupDescriptor: Option[RiftDescriptor]): AlmValidation[Option[Tree[Any]]] =
    if (hasValue(ident)) getTree(ident, backupDescriptor).map(Some(_)) else None.success
}