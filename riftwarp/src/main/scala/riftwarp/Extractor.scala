package riftwarp

import language.higherKinds

import scala.reflect.ClassTag
import scala.collection.generic.CanBuildFrom
import almhirt.common._

trait Extractor {
  def path: List[String]
  
  def getString(ident: String): AlmValidation[String]
  def tryGetString(ident: String): AlmValidation[Option[String]]

  def getBoolean(ident: String): AlmValidation[Boolean]
  def tryGetBoolean(ident: String): AlmValidation[Option[Boolean]]

  def getByte(ident: String): AlmValidation[Byte]
  def tryGetByte(ident: String): AlmValidation[Option[Byte]]
  def getInt(ident: String): AlmValidation[Int]
  def tryGetInt(ident: String): AlmValidation[Option[Int]]
  def getLong(ident: String): AlmValidation[Long]
  def tryGetLong(ident: String): AlmValidation[Option[Long]]
  def getBigInt(ident: String): AlmValidation[BigInt]
  def tryGetBigInt(ident: String): AlmValidation[Option[BigInt]]

  def getFloat(ident: String): AlmValidation[Float]
  def tryGetFloat(ident: String): AlmValidation[Option[Float]]
  def getDouble(ident: String): AlmValidation[Double]
  def tryGetDouble(ident: String): AlmValidation[Option[Double]]
  def getBigDecimal(ident: String): AlmValidation[BigDecimal]
  def tryGetBigDecimal(ident: String): AlmValidation[Option[BigDecimal]]

  def getByteArray(ident: String): AlmValidation[Array[Byte]]
  def tryGetByteArray(ident: String): AlmValidation[Option[Array[Byte]]]
  def getByteArrayFromBase64Encoding(ident: String): AlmValidation[Array[Byte]]
  def tryGetByteArrayFromBase64Encoding(ident: String): AlmValidation[Option[Array[Byte]]]
  def getByteArrayFromBlobEncoding(ident: String): AlmValidation[Array[Byte]]
  def tryGetByteArrayFromBlobEncoding(ident: String): AlmValidation[Option[Array[Byte]]]

  def getDateTime(ident: String): AlmValidation[org.joda.time.DateTime]
  def tryGetDateTime(ident: String): AlmValidation[Option[org.joda.time.DateTime]]

  def getUri(ident: String): AlmValidation[_root_.java.net.URI]
  def tryGetUri(ident: String): AlmValidation[Option[_root_.java.net.URI]]

  def getUuid(ident: String): AlmValidation[_root_.java.util.UUID]
  def tryGetUuid(ident: String): AlmValidation[Option[_root_.java.util.UUID]]

  def getWith[T](ident: String, recomposes: Extractor => AlmValidation[T]): AlmValidation[T]
  def tryGetWith[T](ident: String, recomposes: Extractor => AlmValidation[T]): AlmValidation[Option[T]]
  def getWithRecomposes[T](ident: String, recomposes: Recomposes[T]): AlmValidation[T]
  def tryGetWithRecomposes[T](ident: String, recomposes: Recomposes[T]): AlmValidation[Option[T]]
  def getComplex(ident: String, descriptor: RiftDescriptor): AlmValidation[Any]
  def tryGetComplex(ident: String, descriptor: RiftDescriptor): AlmValidation[Option[Any]]
  def getComplexByTag[T <: AnyRef](ident: String, backupDescriptor: Option[RiftDescriptor])(implicit tag: ClassTag[T]): AlmValidation[T]
  def tryGetComplexByTag[T <: AnyRef](ident: String, backupDescriptor: Option[RiftDescriptor])(implicit tag: ClassTag[T]): AlmValidation[Option[T]]
  def getComplexByValueDescriptor(ident: String, backupDescriptor: Option[RiftDescriptor]): AlmValidation[AnyRef]
  def tryGetComplexByValueDescriptor(ident: String, backupDescriptor: Option[RiftDescriptor]): AlmValidation[Option[AnyRef]]
  
  def getManyPrimitives[That[_], T](ident: String)(implicit mA: ClassTag[T], cbf: CanBuildFrom[Traversable[_], T, That[T]]): AlmValidation[That[T]]
  def tryGetManyPrimitives[That[_], T](ident: String)(implicit mA: ClassTag[T], cbf: CanBuildFrom[Traversable[_], T, That[T]]): AlmValidation[Option[That[T]]]
  def getManyWith[That[_], T](ident: String, recomposes: Extractor => AlmValidation[T])(implicit cbf: CanBuildFrom[Traversable[_], T, That[T]]): AlmValidation[That[T]]
  def tryGetManyWith[That[_], T](ident: String, recomposes: Extractor => AlmValidation[T])(implicit cbf: CanBuildFrom[Traversable[_], T, That[T]]): AlmValidation[Option[That[T]]]
  def getManyComplex[That[_]](ident: String, descriptor: RiftDescriptor)(implicit cbf: CanBuildFrom[Traversable[_], Any, That[Any]]): AlmValidation[That[Any]]
  def tryGetManyComplex[That[_]](ident: String, descriptor: RiftDescriptor)(implicit cbf: CanBuildFrom[Traversable[_], Any, That[Any]]): AlmValidation[Option[That[Any]]]
  def getManyComplexOfType[That[_], T <: AnyRef](ident: String, backupDescriptor: Option[RiftDescriptor])(implicit tag: ClassTag[T], cbf: CanBuildFrom[Traversable[_], T, That[T]]): AlmValidation[That[T]]
  def tryGetManyComplexOfType[That[_], T <: AnyRef](ident: String, backupDescriptor: Option[RiftDescriptor])(implicit tag: ClassTag[T], cbf: CanBuildFrom[Traversable[_], T, That[T]]): AlmValidation[Option[That[T]]]
  def getManyComplexByTag[That[_], T <: AnyRef](ident: String, backupDescriptor: Option[RiftDescriptor])(implicit tag: ClassTag[T], cbf: CanBuildFrom[Traversable[_], T, That[T]]): AlmValidation[That[T]]
  def tryGetManyComplexByTag[That[_], T <: AnyRef](ident: String, backupDescriptor: Option[RiftDescriptor])(implicit tag: ClassTag[T], cbf: CanBuildFrom[Traversable[_], T, That[T]]): AlmValidation[Option[That[T]]]
  def getMany[That[_]](ident: String, backupDescriptor: Option[RiftDescriptor])(implicit cbf: CanBuildFrom[Traversable[_], Any, That[Any]]): AlmValidation[That[Any]]
  def tryGetMany[That[_]](ident: String, backupDescriptor: Option[RiftDescriptor])(implicit cbf: CanBuildFrom[Traversable[_], Any, That[Any]]): AlmValidation[Option[That[Any]]]

  def getMapOfPrimitives[A, B](ident: String)(implicit tagA: ClassTag[A], tagB: ClassTag[B]): AlmValidation[Map[A, B]]
  def tryGetMapOfPrimitives[A, B](ident: String)(implicit tagA: ClassTag[A], tagB: ClassTag[B]): AlmValidation[Option[Map[A, B]]]
  def getMapWith[A, B](ident: String, recomposes: Extractor => AlmValidation[B])(implicit tag: ClassTag[A]): AlmValidation[Map[A, B]]
  def tryGetMapWith[A, B](ident: String, recomposes: Extractor => AlmValidation[B])(implicit tag: ClassTag[A]): AlmValidation[Option[Map[A, B]]]
  def getMapComplex[A](ident: String, descriptor: RiftDescriptor)(implicit tag: ClassTag[A]): AlmValidation[Map[A, Any]]
  def tryGetMapComplex[A](ident: String, descriptor: RiftDescriptor)(implicit tag: ClassTag[A]): AlmValidation[Option[Map[A, Any]]]
  def getMapComplexByTag[A, B <: AnyRef](ident: String, backupDescriptor: Option[RiftDescriptor])(implicit tagA: ClassTag[A], tagB: ClassTag[B]): AlmValidation[Map[A, B]]
  def tryGetMapComplexByTag[A, B <: AnyRef](ident: String, backupDescriptor: Option[RiftDescriptor])(implicit tagA: ClassTag[A], tagB: ClassTag[B]): AlmValidation[Option[Map[A, B]]]
  def getMap[A](ident: String, backupDescriptor: Option[RiftDescriptor])(implicit tagA: ClassTag[A]): AlmValidation[Map[A, Any]]
  def tryGetMap[A](ident: String, backupDescriptor: Option[RiftDescriptor])(implicit tagA: ClassTag[A]): AlmValidation[Option[Map[A, Any]]]

  import scalaz.Tree
  def getTreeOfPrimitives[T](ident: String)(implicit mA: ClassTag[T]): AlmValidation[Tree[T]]
  def tryGetTreeOfPrimitives[T](ident: String)(implicit mA: ClassTag[T]): AlmValidation[Option[Tree[T]]]
  def getTreeWith[T](ident: String, recomposes: Extractor => AlmValidation[T]): AlmValidation[Tree[T]]
  def tryGetTreeWith[T](ident: String, recomposes: Extractor => AlmValidation[T]): AlmValidation[Option[Tree[T]]]
  def getTreeOfComplex(ident: String, descriptor: RiftDescriptor): AlmValidation[Tree[AnyRef]]
  def tryGetTreeOfComplex(ident: String, descriptor: RiftDescriptor): AlmValidation[Option[Tree[AnyRef]]]
  def getTreeOfComplexOfType[T <: AnyRef](ident: String, backupDescriptor: Option[RiftDescriptor])(implicit tag: ClassTag[T]): AlmValidation[Tree[T]]
  def tryGetTreeOfComplexOfType[T <: AnyRef](ident: String, backupDescriptor: Option[RiftDescriptor])(implicit tag: ClassTag[T]): AlmValidation[Option[Tree[T]]]
  def getTreeOfComplexByTag[T <: AnyRef](ident: String, backupDescriptor: Option[RiftDescriptor])(implicit tag: ClassTag[T]): AlmValidation[Tree[T]]
  def tryGetTreeOfComplexByTag[T <: AnyRef](ident: String, backupDescriptor: Option[RiftDescriptor])(implicit tag: ClassTag[T]): AlmValidation[Option[Tree[T]]]
  def getTree(ident: String, backupDescriptor: Option[RiftDescriptor]): AlmValidation[Tree[Any]]
  def tryGetTree(ident: String, backupDescriptor: Option[RiftDescriptor]): AlmValidation[Option[Tree[Any]]]
  
  
  def getBlob(ident: String): AlmValidation[Array[Byte]]
  def tryGetBlob(ident: String): AlmValidation[Option[Array[Byte]]]
  
  def getRiftDescriptor: AlmValidation[RiftDescriptor]
  def tryGetRiftDescriptor: AlmValidation[Option[RiftDescriptor]]
}

object Extractor {
  implicit class ExtractorOps(self: Extractor) {
    def pathFromRoot: List[String] = self.path.reverse
    def showPathFromRoot(sep: String = "."): String = pathFromRoot.mkString(sep)
  }
}