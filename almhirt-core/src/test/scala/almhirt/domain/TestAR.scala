package almhirt.domain

trait TestArEvent extends DomainEvent

case class TestArCreated(header: DomainEventHeader, a: String) extends TestArEvent with CreatesNewAggregateRootEvent with DomainEventTemplate[TestArCreated] {
  def changeHeader(newHeader: DomainEventHeader) = this.copy(header = newHeader)
}

case class AChanged(header: DomainEventHeader, a: String) extends TestArEvent with CreatesNewAggregateRootEvent with DomainEventTemplate[AChanged] {
  def changeHeader(newHeader: DomainEventHeader) = this.copy(header = newHeader)
}

case class BChanged(header: DomainEventHeader, b: Option[String]) extends TestArEvent with CreatesNewAggregateRootEvent with DomainEventTemplate[BChanged] {
  def changeHeader(newHeader: DomainEventHeader) = this.copy(header = newHeader)
}

case class TestAr(ref: AggregateRootRef, a: String, b: Option[String]) extends AggregateRoot[TestAr, TestArEvent] with AggregateRootWithHandlers[TestAr, TestArEvent] {
  protected override def handlers = {
    case _ => ???
  }

  protected override def  updateRef(newRef: AggregateRootRef): TestAr = this.copy(ref = newRef)

}