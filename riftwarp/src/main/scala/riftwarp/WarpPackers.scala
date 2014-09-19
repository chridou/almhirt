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
  def getByPredicate(what: Any): AlmValidation[BlindWarpPacker]

  def getByTag[T](implicit tag: ClassTag[T]): AlmValidation[BlindWarpPacker] =
    apply(WarpDescriptor(tag.runtimeClass))

  def getTyped[T](descriptor: WarpDescriptor): AlmValidation[WarpPacker[T]] =
    get(descriptor).map(blindPacker ⇒ blindToTyped[T](blindPacker))

  def getByTagTyped[T](implicit tag: ClassTag[T]): AlmValidation[WarpPacker[T]] =
    getTyped[T](WarpDescriptor(tag.runtimeClass))

  def getFor(what: Any, overrideDescriptor: Option[WarpDescriptor], backupDescriptor: Option[WarpDescriptor]): AlmValidation[BlindWarpPacker] =
    overrideDescriptor match {
      case Some(ord) ⇒
        get(ord)
      case None ⇒
        get(WarpDescriptor(what.getClass)).fold(
          fail ⇒
            backupDescriptor match {
              case Some(bd) ⇒
                get(bd).fold(
                  fail ⇒ getByPredicate(what),
                  succ ⇒ succ.success)
              case None ⇒
                getByPredicate(what)
            },
          succ ⇒
            succ.success)
    }

  def add(blindPacker: BlindWarpPacker with RegisterableWarpPacker)
  def addTyped[T](packer: WarpPacker[T] with RegisterableWarpPacker)
  def addPredicated(pred: Any ⇒ Boolean, packer: BlindWarpPacker)

  protected def blindToTyped[T](blindPacker: BlindWarpPacker) =
    new WarpPacker[T] {
      override val warpDescriptor = blindPacker.warpDescriptor
      override def pack(what: T)(implicit packers: WarpPackers): AlmValidation[WarpPackage] =
        unsafe {
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
    packers.addTyped(LocalDateTimeWarpPacker)
    packers.addTyped(DurationWarpPacker)
    packers.addTyped(ByteArrayWarpPacker)
    packers.addTyped(Base64BlobWarpPacker)

    packers.addTyped(HasAThrowableDescribedPacker)
    packers.addTyped(HasAThrowablePacker)
    packers.addTyped(ThrowableRepresentationPacker)
    packers.addTyped(CauseIsThrowablePacker)
    packers.addTyped(CauseIsProblemPacker)
    packers.addTyped(ProblemCausePacker)

    packers.addTyped(WarpDescriptorPacker)
 
    packers.addTyped(SingleProblemPackaging)
    packers.addTyped(AggregatedProblemPackaging)
    packers.addTyped(ProblemPackaging)

    packers.addTyped(CommandResponseWarpPackaging)
    packers.addTyped(CommandStatusChangedWarpPackaging)
 
    packers.addPredicated(x ⇒ x.isInstanceOf[SingleProblem], SingleProblemPackaging)
    packers.addPredicated(x ⇒ x.isInstanceOf[AggregatedProblem], AggregatedProblemPackaging)
    
    serialization.common.ProblemTypes.registerPackers(packers)
    packers
  }

  def empty: WarpPackers = new WarpPackerRegistry()

  val NoWarpPackers: WarpPackers = new WarpPackers {
    override def get(descriptor: WarpDescriptor) = NoSuchElementProblem("NoWarpPackers has no packers").failure
    override def getByPredicate(what: Any) = NoSuchElementProblem("NoWarpPackers has no packers").failure
    override def add(blindPacker: BlindWarpPacker with RegisterableWarpPacker) {}
    override def addTyped[T](packer: WarpPacker[T] with RegisterableWarpPacker) {}
    override def addPredicated(pred: Any ⇒ Boolean, packer: BlindWarpPacker) {}
  }
}