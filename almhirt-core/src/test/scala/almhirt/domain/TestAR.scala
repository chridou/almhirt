package almhirt.domain

import scalaz._, Scalaz._
import almhirt.common._
import almhirt.almvalidation.kit._

trait TestArEvent extends DomainEvent

case class TestArCreated(header: DomainEventHeader, newA: String) extends TestArEvent with CreatesNewAggregateRootEvent with DomainEventTemplate[TestArCreated] {
  def changeHeader(newHeader: DomainEventHeader) = this.copy(header = newHeader)
}

case class AChanged(header: DomainEventHeader, newA: String) extends TestArEvent with CreatesNewAggregateRootEvent with DomainEventTemplate[AChanged] {
  def changeHeader(newHeader: DomainEventHeader) = this.copy(header = newHeader)
}

case class BChanged(header: DomainEventHeader, newB: Option[String]) extends TestArEvent with CreatesNewAggregateRootEvent with DomainEventTemplate[BChanged] {
  def changeHeader(newHeader: DomainEventHeader) = this.copy(header = newHeader)
}

case class TestArDeleted(header: DomainEventHeader) extends TestArEvent with CreatesNewAggregateRootEvent with DomainEventTemplate[TestArDeleted] {
  def changeHeader(newHeader: DomainEventHeader) = this.copy(header = newHeader)
}

case class TestAr(ref: AggregateRootRef, theA: String, theB: Option[String], isDeleted: Boolean)
  extends AggregateRoot[TestAr, TestArEvent]
  with AggregateRootWithHandlers[TestAr, TestArEvent]
  with AddsUpdatingToAggregateRoot[TestAr, TestArEvent] {

  protected override def handlers = {
    case AChanged(_, newA) => set((ar: TestAr, v: String) => ar.copy(theA = v), newA)
    case BChanged(_, newB) => setL(TestArLenses.theBL, newB)
    case TestArDeleted(_) => markDeleted((ar: TestAr, b: Boolean) => ar.copy(isDeleted = b))
  }

  def changeA(newA: String)(implicit ccuad: CanCreateUuidsAndDateTimes): UpdateRecorder[TestAr, TestArEvent] =
    newA.notEmptyAlm.fold(
      fail => reject(fail),
      succ => update(AChanged(ref, succ)))

  def changeB(newB: Option[String])(implicit ccuad: CanCreateUuidsAndDateTimes): UpdateRecorder[TestAr, TestArEvent] =
    newB.map(_.notEmptyAlm).validationOut.fold(
      fail => reject(fail),
      succ => update(BChanged(ref, succ)))

  def delete()(implicit ccuad: CanCreateUuidsAndDateTimes): UpdateRecorder[TestAr, TestArEvent] =
    update(TestArDeleted(ref))

  protected override def updateRef(newRef: AggregateRootRef): TestAr = this.copy(ref = newRef)
}

object TestAr extends CanCreateAggragateRoot[TestAr, TestArEvent] {
  protected override def creationHandler: PartialFunction[TestArEvent, TestAr] = {
    case TestArCreated(header, newA) =>
      TestAr(header.aggRef, newA, None, false)
  }
}

object TestArLenses {
  val theBL: TestAr @> Option[String] = Lens.lensu((a, b) => a.copy(theB = b), _.theB)
}