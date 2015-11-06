package almhirt.akkax.reporting

import java.time.{ LocalDateTime, ZonedDateTime }
import almhirt.problem.ProblemCause
import almhirt.akkax.ComponentState
import ezreps.util.EzValueConverter
import almhirt.aggregates._

trait AlmEzValueConverters {
  implicit val RValueAggregateRootIdInst: EzValueConverter[AggregateRootId] = new EzValueConverter[AggregateRootId] {
    def convert(value: AggregateRootId): ezreps.ast.EzValue = ezreps.ast.EzString(value.value)
  }

  implicit val RValueAggregateRootVersionInst: EzValueConverter[AggregateRootVersion] = new EzValueConverter[AggregateRootVersion] {
    def convert(value: AggregateRootVersion): ezreps.ast.EzValue = ezreps.ast.EzInteger(value.value)
  }

  implicit val RValueConverterComponentStateInst: EzValueConverter[ComponentState] = new EzValueConverter[ComponentState] {
    def convert(value: ComponentState): ezreps.ast.EzValue = ezreps.ast.EzString(value.parsableString)
  }
  implicit val RValueConverterProblemInst: EzValueConverter[almhirt.common.Problem] = new EzValueConverter[almhirt.common.Problem] {
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
  implicit val OptionRVAlueConverterComponentStateInst: EzValueConverter[Option[ComponentState]] = createOptionSomeConverterWrapperInst[ComponentState]
}