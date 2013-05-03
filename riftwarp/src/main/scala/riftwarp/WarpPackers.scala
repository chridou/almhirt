package riftwarp

import scala.reflect.ClassTag
import scalaz.syntax.validation._
import almhirt.common._
import almhirt.almvalidation.kit._
import riftwarp.impl.WarpPackerRegistry
import riftwarp.serialization.common._
import riftwarp.std._

trait WarpPackers extends Function1[WarpDescriptor, AlmValidation[BlindWarpPacker]] {
  final def apply(descriptor: WarpDescriptor): AlmValidation[BlindWarpPacker] = get(descriptor)
  def get(descriptor: WarpDescriptor): AlmValidation[BlindWarpPacker]
  def getByTag[T](implicit tag: ClassTag[T]): AlmValidation[BlindWarpPacker] =
    apply(WarpDescriptor(tag.runtimeClass))
  def getTyped[T](descriptor: WarpDescriptor): AlmValidation[WarpPacker[T]] =
    get(descriptor).map(blindPacker => blindToTyped[T](blindPacker))

  def getByTagTyped[T](implicit tag: ClassTag[T]): AlmValidation[WarpPacker[T]] =
    getTyped[T](WarpDescriptor(tag.runtimeClass))

  def add(blindPacker: BlindWarpPacker with RegisterableWarpPacker)
  def addTyped[T](packer: WarpPacker[T] with RegisterableWarpPacker)

  protected def blindToTyped[T](blindPacker: BlindWarpPacker) =
    new WarpPacker[T] {
      override def pack(what: T)(implicit packers: WarpPackers): AlmValidation[WarpPackage] =
        computeSafely {
          blindPacker.packBlind(what)
        }
    }
}

object WarpPackers {
  def apply(): WarpPackers = {
    val packers = new WarpPackerRegistry()
    packers.addTyped(BooleanWarpPacker)
    packers.addTyped(StringWarpPacker)
    packers.addTyped(ByteWarpPacker)
    packers.addTyped(IntWarpPacker)
    packers.addTyped(LongWarpPacker)
    packers.addTyped(BigIntWarpPacker)
    packers.addTyped(FloatWarpPacker)
    packers.addTyped(DoubleWarpPacker)
    packers.addTyped(BigDecimalWarpPacker)
    packers.addTyped(UuidWarpPacker)
    packers.addTyped(UriWarpPacker)
    packers.addTyped(DateTimeWarpPacker)
    packers.addTyped(ByteArrayWarpPacker)
    packers.addTyped(Base64BlobWarpPacker)
 
    packers.addTyped(HasAThrowableDescribedPacker)
    packers.addTyped(HasAThrowablePacker)
    packers.addTyped(ThrowableRepresentationPacker)
    packers.addTyped(CauseIsThrowablePacker)
    packers.addTyped(CauseIsProblemPacker)
    packers.addTyped(ProblemCausePacker)

    packers.addTyped(WarpDescriptorPacker)

    serialization.common.Problems.registerAllCommonProblems(packers, WarpUnpackers.NoWarpUnpackers)
    packers
  }
  
  
  val NoWarpPackers: WarpPackers = new WarpPackers {
    override def get(descriptor: WarpDescriptor) = UnspecifiedSystemProblem("NoWarpPackers has no packers").failure
    override def add(blindPacker: BlindWarpPacker with RegisterableWarpPacker) {}
    override def addTyped[T](packer: WarpPacker[T] with RegisterableWarpPacker) {}
  }
}