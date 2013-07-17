package almhirt.domain

import scalaz._, Scalaz._
import almhirt.common._
import almhirt.almvalidation.kit._

trait TestAr2Event extends DomainEvent

case class TestAr2Created(header: DomainEventHeader, newA: String) extends TestAr2Event with CreatesNewAggregateRootEvent with DomainEventTemplate[TestAr2Created] {
  def changeHeader(newHeader: DomainEventHeader) = this.copy(header = newHeader)
}

case class CChanged(header: DomainEventHeader, newC: Option[Int]) extends TestAr2Event with DomainEventTemplate[CChanged] {
  def changeHeader(newHeader: DomainEventHeader) = this.copy(header = newHeader)
}

case class TestAr2Deleted(header: DomainEventHeader) extends TestAr2Event with CreatesNewAggregateRootEvent with DomainEventTemplate[TestAr2Deleted] {
  def changeHeader(newHeader: DomainEventHeader) = this.copy(header = newHeader)
}

case class UnhandableTestAr2Event(header: DomainEventHeader) extends TestAr2Event with DomainEventTemplate[UnhandableTestAr2Event] {
  def changeHeader(newHeader: DomainEventHeader) = this.copy(header = newHeader)
}

case class TestAr2(ref: AggregateRootRef, theC: Option[Int], isDeleted: Boolean)
  extends AggregateRoot[TestAr2, TestAr2Event]
  with AggregateRootWithHandlers[TestAr2, TestAr2Event]
  with AggregateRootMutationHelpers[TestAr2, TestAr2Event] {

  protected override def handlers = {
    case CChanged(_, newC) => set((ar: TestAr2, v: Option[Int]) => ar.copy(theC = v), newC)
    case TestAr2Deleted(_) => markDeleted((ar: TestAr2, b: Boolean) => ar.copy(isDeleted = b))
  }

  def setC(newC: Int)(implicit ccuad: CanCreateUuidsAndDateTimes): UpdateRecorder[TestAr2, TestAr2Event] =
    update(CChanged(ref, Some(newC)))

  def unsetC()(implicit ccuad: CanCreateUuidsAndDateTimes): UpdateRecorder[TestAr2, TestAr2Event] =
    update(CChanged(ref, None))

  def delete()(implicit ccuad: CanCreateUuidsAndDateTimes): UpdateRecorder[TestAr2, TestAr2Event] =
    update(TestAr2Deleted(ref))

  protected override def updateRef(newRef: AggregateRootRef): TestAr2 = this.copy(ref = newRef)
}

object TestAr2 extends CanCreateAggragateRoot[TestAr2, TestAr2Event] {
  protected override def creationHandler: PartialFunction[TestAr2Event, TestAr2] = {
    case TestAr2Created(header, newA) =>
      TestAr2(header.aggRef.inc, None, false)
  }

  def fromScratch(id: java.util.UUID, a : String)(implicit ccuad: CanCreateUuidsAndDateTimes): UpdateRecorder[TestAr2, TestAr2Event] =
    create(TestAr2Created(DomainEventHeader(id, 0L), a))
  
}

object TestAr2Lenses {
  val theCL: TestAr2 @> Option[Int] = Lens.lensu((a, b) => a.copy(theC = b), _.theC)
}