package almhirt.domain

import scalaz._, Scalaz._
import almhirt.common._
import almhirt.almvalidation.kit._

trait AnotherTestArEvent extends DomainEvent

case class AnotherTestArCreated(header: DomainEventHeader) extends AnotherTestArEvent with CreatesNewAggregateRootEvent with DomainEventTemplate[AnotherTestArCreated] {
  def changeHeader(newHeader: DomainEventHeader) = this.copy(header = newHeader)
}

case class CChanged(header: DomainEventHeader, newC: Option[Int]) extends AnotherTestArEvent with DomainEventTemplate[CChanged] {
  def changeHeader(newHeader: DomainEventHeader) = this.copy(header = newHeader)
}

case class AnotherTestArDeleted(header: DomainEventHeader) extends AnotherTestArEvent with CreatesNewAggregateRootEvent with DomainEventTemplate[AnotherTestArDeleted] {
  def changeHeader(newHeader: DomainEventHeader) = this.copy(header = newHeader)
}

case class UnhandableAnotherTestArEvent(header: DomainEventHeader) extends AnotherTestArEvent with DomainEventTemplate[UnhandableAnotherTestArEvent] {
  def changeHeader(newHeader: DomainEventHeader) = this.copy(header = newHeader)
}

case class AnotherTestAr(ref: AggregateRootRef, theC: Option[Int], isDeleted: Boolean)
  extends AggregateRoot[AnotherTestAr, AnotherTestArEvent]
  with AggregateRootWithHandlers[AnotherTestAr, AnotherTestArEvent]
  with AggregateRootMutationHelpers[AnotherTestAr, AnotherTestArEvent] {

  protected override def handlers = {
    case CChanged(_, newC) => set((ar: AnotherTestAr, v: Option[Int]) => ar.copy(theC = v), newC)
    case AnotherTestArDeleted(_) => markDeleted((ar: AnotherTestAr, b: Boolean) => ar.copy(isDeleted = b))
  }

  def setC(newC: Int)(implicit ccuad: CanCreateUuidsAndDateTimes): UpdateRecorder[AnotherTestAr, AnotherTestArEvent] =
    update(CChanged(ref, Some(newC)))

  def unsetC()(implicit ccuad: CanCreateUuidsAndDateTimes): UpdateRecorder[AnotherTestAr, AnotherTestArEvent] =
    update(CChanged(ref, None))

  def delete()(implicit ccuad: CanCreateUuidsAndDateTimes): UpdateRecorder[AnotherTestAr, AnotherTestArEvent] =
    update(AnotherTestArDeleted(ref))

  protected override def updateRef(newRef: AggregateRootRef): AnotherTestAr = this.copy(ref = newRef)
}

object AnotherTestAr extends CanCreateAggragateRoot[AnotherTestAr, AnotherTestArEvent] {
  protected override def creationHandler: PartialFunction[AnotherTestArEvent, AnotherTestAr] = {
    case AnotherTestArCreated(header) =>
      AnotherTestAr(header.aggRef.inc, None, false)
  }

  def fromScratch(id: java.util.UUID)(implicit ccuad: CanCreateUuidsAndDateTimes): UpdateRecorder[AnotherTestAr, AnotherTestArEvent] =
    create(AnotherTestArCreated(DomainEventHeader(id, 0L)))
  
}

object AnotherTestArLenses {
  val theCL: AnotherTestAr @> Option[Int] = Lens.lensu((a, b) => a.copy(theC = b), _.theC)
}