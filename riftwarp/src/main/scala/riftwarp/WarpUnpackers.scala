package riftwarp

import scala.reflect.ClassTag
import scalaz.syntax.validation._
import almhirt.common._
import almhirt.almvalidation.kit._
import riftwarp.impl.WarpUnpackerRegistry
import riftwarp.std._
import riftwarp.serialization.common._

trait WarpUnpackers {
  final def apply(descriptor: WarpDescriptor): AlmValidation[WarpUnpacker[Any]] = get(descriptor)
  def get(descriptor: WarpDescriptor): AlmValidation[WarpUnpacker[Any]]
  def getTyped[T](descriptor: WarpDescriptor)(implicit tag: ClassTag[T]): AlmValidation[WarpUnpacker[T]] =
    apply(descriptor).map(unpacker =>
      new WarpUnpacker[T] {
        def unpack(from: WarpPackage)(implicit unpackers: WarpUnpackers): AlmValidation[T] =
          computeSafely {
            unpacker.unpack(from).flatMap(_.castTo[T])
          }
      })

  def getByTag[T](implicit tag: ClassTag[T]): AlmValidation[WarpUnpacker[Any]] =
    apply(WarpDescriptor(tag.runtimeClass))

  def getByTagTyped[T](implicit tag: ClassTag[T]): AlmValidation[WarpUnpacker[T]] =
    apply(WarpDescriptor(tag.runtimeClass)).map(untyped => untypedToTyped[T](untyped))

  def add(unpacker: RegisterableWarpUnpacker[Any])
  def addTyped[T](unpacker: RegisterableWarpUnpacker[T])

  protected def untypedToTyped[T](unpacker: WarpUnpacker[Any])(implicit tag: ClassTag[T]) =
    new WarpUnpacker[T] {
      def unpack(from: WarpPackage)(implicit unpackers: WarpUnpackers): AlmValidation[T] =
        computeSafely {
          unpacker.unpack(from).flatMap(_.castTo[T])
        }
    }
}

object WarpUnpackers {
  def apply(): WarpUnpackers = {
    val unpackers = new WarpUnpackerRegistry()
    unpackers.addTyped(BooleanWarpUnpacker)
    unpackers.addTyped(StringWarpUnpacker)
    unpackers.addTyped(ByteWarpUnpacker)
    unpackers.addTyped(IntWarpUnpacker)
    unpackers.addTyped(LongWarpUnpacker)
    unpackers.addTyped(BigIntWarpUnpacker)
    unpackers.addTyped(FloatWarpUnpacker)
    unpackers.addTyped(DoubleWarpUnpacker)
    unpackers.addTyped(BigDecimalWarpUnpacker)
    unpackers.addTyped(UuidWarpUnpacker)
    unpackers.addTyped(UriWarpUnpacker)
    unpackers.addTyped(DateTimeWarpUnpacker)
    unpackers.addTyped(ByteArrayWarpUnpacker)
    unpackers.addTyped(Base64BlobWarpUnpacker)
 
    unpackers.addTyped(HasAThrowableDescribedUnpacker)
    unpackers.addTyped(CauseIsThrowableUnpacker)
    unpackers.addTyped(CauseIsProblemUnpacker)
    unpackers.addTyped(ProblemCauseUnpacker)

    unpackers.addTyped(WarpDescriptorUnpacker)
    
    serialization.common.Problems.registerAllCommonProblems(WarpPackers.NoWarpPackers, unpackers)
    
    unpackers
  }
  
  def empty: WarpUnpackers = new WarpUnpackerRegistry()
  
  val NoWarpUnpackers = new WarpUnpackers {
    override def get(descriptor: WarpDescriptor) = UnspecifiedSystemProblem("NoWarpUnpackers has no unpackers").failure
    override def add(unpacker: RegisterableWarpUnpacker[Any]) {}
    override def addTyped[T](unpacker: RegisterableWarpUnpacker[T]) {}
  }
}