package almhirt.domain

import scalaz._, Scalaz._
import almhirt.common._
import almhirt.almvalidation.kit._

trait TestArEvent extends DomainEvent

case class TestArCreated(header: DomainEventHeader, newA: String) extends TestArEvent with CreatesNewAggregateRootEvent {
  override def changeMetadata(newMetaData: Map[String, String]): TestArCreated = copy(header = this.header.changeMetadata(newMetaData))
}

case class AChanged(header: DomainEventHeader, newA: String) extends TestArEvent {
  override def changeMetadata(newMetaData: Map[String, String]): AChanged = copy(header = this.header.changeMetadata(newMetaData))
}

case class BChanged(header: DomainEventHeader, newB: Option[String]) extends TestArEvent {
  override def changeMetadata(newMetaData: Map[String, String]): BChanged = copy(header = this.header.changeMetadata(newMetaData))
}

case class TestArDeleted(header: DomainEventHeader) extends TestArEvent with CreatesNewAggregateRootEvent {
  override def changeMetadata(newMetaData: Map[String, String]): TestArDeleted = copy(header = this.header.changeMetadata(newMetaData))
}

case class UnhandableTestArEvent(header: DomainEventHeader) extends TestArEvent {
  override def changeMetadata(newMetaData: Map[String, String]): UnhandableTestArEvent = copy(header = this.header.changeMetadata(newMetaData))
}

case class TestAr(ref: AggregateRootRef, theA: String, theB: Option[String], isDeleted: Boolean)
  extends AggregateRoot[TestAr, TestArEvent]
  with AggregateRootWithHandlers[TestAr, TestArEvent]
  with AggregateRootMutationHelpers[TestAr, TestArEvent] {

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
      TestAr(header.aggRef.inc, newA, None, false)
  }

  def fromScratch(id: java.util.UUID, a: String)(implicit ccuad: CanCreateUuidsAndDateTimes): UpdateRecorder[TestAr, TestArEvent] =
    create(TestArCreated(DomainEventHeader(id, 0L), a))

}

object TestArLenses {
  val theBL: TestAr @> Option[String] = Lens.lensu((a, b) => a.copy(theB = b), _.theB)
}