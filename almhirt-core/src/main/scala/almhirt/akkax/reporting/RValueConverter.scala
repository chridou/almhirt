package almhirt.akkax.reporting

import java.time.{ LocalDateTime, ZonedDateTime }
import almhirt.problem.{ Problem, ProblemCause }
import almhirt.akkax.ComponentState
import ezreps.util.EzValueConverter
import almhirt.aggregates._
import almhirt.tracking.{ CorrelationId, TrackingTicket }
import almhirt.http.AlmMediaType

trait AlmEzValueConverters {
  implicit val RValueAggregateRootIdInst: EzValueConverter[AggregateRootId] = new EzValueConverter[AggregateRootId] {
    def convert(value: AggregateRootId): ezreps.ast.EzValue = ezreps.ast.EzString(value.value)
  }

  implicit val RValueAggregateRootVersionInst: EzValueConverter[AggregateRootVersion] = new EzValueConverter[AggregateRootVersion] {
    def convert(value: AggregateRootVersion): ezreps.ast.EzValue = ezreps.ast.EzInteger(value.value)
  }

  implicit val RValueCorrelationIdVersionInst: EzValueConverter[CorrelationId] = new EzValueConverter[CorrelationId] {
    def convert(value: CorrelationId): ezreps.ast.EzValue = ezreps.ast.EzString(value.value)
  }

  implicit val RValueTrackingTicketVersionInst: EzValueConverter[TrackingTicket] = new EzValueConverter[TrackingTicket] {
    def convert(value: TrackingTicket): ezreps.ast.EzValue = ezreps.ast.EzString(value.value)
  }

  implicit val RValueAlmMediaTypeInst: EzValueConverter[AlmMediaType] = new EzValueConverter[AlmMediaType] {
    def convert(value: AlmMediaType): ezreps.ast.EzValue = ezreps.ast.EzString(value.value)
  }
  
  implicit val RValueConverterComponentStateInst: EzValueConverter[ComponentState] = new EzValueConverter[ComponentState] {
    def convert(value: ComponentState): ezreps.ast.EzValue = ezreps.ast.EzString(value.parsableString)
  }
  implicit val RValueConverterProblemInst: EzValueConverter[Problem] = new EzValueConverter[Problem] {
    def convert(value: almhirt.common.Problem): ezreps.ast.EzValue = ezreps.ast.EzError(value.message)
  }
  implicit val RValueConverterProblemCauseInst: EzValueConverter[ProblemCause] = new EzValueConverter[ProblemCause] {
    def convert(value: ProblemCause): ezreps.ast.EzValue = ezreps.ast.EzError(value.message)
  }
}

trait AlmEzValueOptionConverters { self: AlmEzValueConverters ⇒
  private def createOptionSomeConverterWrapperInst[T: EzValueConverter]: EzValueConverter[Option[T]] = new EzValueConverter[Option[T]] {
    def convert(value: Option[T]): ezreps.ast.EzValue =
      value match {
        case Some(t) ⇒ ezreps.toAST(t)
        case None    ⇒ ezreps.ast.EzNotAvailable
      }

  }
  implicit val OptionRValueConverterComponentStateInst: EzValueConverter[Option[ComponentState]] = createOptionSomeConverterWrapperInst[ComponentState]
  implicit val OptionRValueConverterAggregateRootIdInst: EzValueConverter[Option[AggregateRootId]] = createOptionSomeConverterWrapperInst[AggregateRootId]
  implicit val OptionRValueConverterAggregateRootVersionInst: EzValueConverter[Option[AggregateRootVersion]] = createOptionSomeConverterWrapperInst[AggregateRootVersion]
  implicit val OptionRValueConverterCorrelationIdInst: EzValueConverter[Option[CorrelationId]] = createOptionSomeConverterWrapperInst[CorrelationId]
  implicit val OptionRValueConverterTrackingTicketInst: EzValueConverter[Option[TrackingTicket]] = createOptionSomeConverterWrapperInst[TrackingTicket]
  implicit val OptionRValueConverterAlmMediaTypeInst: EzValueConverter[Option[AlmMediaType]] = createOptionSomeConverterWrapperInst[AlmMediaType]
  implicit val OptionRValueConverterProblemInst: EzValueConverter[Option[Problem]] = createOptionSomeConverterWrapperInst[Problem]
  implicit val OptionRValueConverterProblemCauseInst: EzValueConverter[Option[ProblemCause]] = createOptionSomeConverterWrapperInst[ProblemCause]
}