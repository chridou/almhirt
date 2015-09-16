package almhirt.akkax.reporting

import java.time.{ LocalDateTime, ZonedDateTime }
import almhirt.problem.ProblemCause
import almhirt.akkax.ComponentState

trait RValueConverter[T] {
  def convert(value: T): AST.RValue
}

trait RValueConverters {
  implicit val RValueConverterIdentityInst: RValueConverter[AST.RValue] = new RValueConverter[AST.RValue] {
    def convert(value: AST.RValue): AST.RValue = value
  }
  implicit val RValueConverterComponentStateInst: RValueConverter[ComponentState] = new RValueConverter[ComponentState] {
    def convert(value: ComponentState): AST.RValue = AST.RComponentState(value)
  }
  implicit val RValueConverterStringInst: RValueConverter[String] = new RValueConverter[String] {
    def convert(value: String): AST.RValue = AST.RString(value)
  }
  implicit val RValueConverterIntInst: RValueConverter[Int] = new RValueConverter[Int] {
    def convert(value: Int): AST.RValue = AST.RInteger(value.toLong)
  }
  implicit val RValueConverterLongInst: RValueConverter[Long] = new RValueConverter[Long] {
    def convert(value: Long): AST.RValue = AST.RInteger(value)
  }
  implicit val RValueConverterFloatInst: RValueConverter[Float] = new RValueConverter[Float] {
    def convert(value: Float): AST.RValue = AST.RFloat(value.toDouble)
  }
  implicit val RValueConverterDoubleInst: RValueConverter[Double] = new RValueConverter[Double] {
    def convert(value: Double): AST.RValue = AST.RFloat(value)
  }
  implicit val RValueConverterBooleanInst: RValueConverter[Boolean] = new RValueConverter[Boolean] {
    def convert(value: Boolean): AST.RValue = AST.RBool(value)
  }
  implicit val RValueConverterLocalDateTimeInst: RValueConverter[LocalDateTime] = new RValueConverter[LocalDateTime] {
    def convert(value: LocalDateTime): AST.RValue = AST.RLocalDateTime(value)
  }
  implicit val RValueConverterZonedDateTimeInst: RValueConverter[ZonedDateTime] = new RValueConverter[ZonedDateTime] {
    def convert(value: ZonedDateTime): AST.RValue = AST.RZonedDateTime(value)
  }
  implicit val RValueConverterDurationInst: RValueConverter[scala.concurrent.duration.FiniteDuration] = new RValueConverter[scala.concurrent.duration.FiniteDuration] {
    def convert(value: scala.concurrent.duration.FiniteDuration): AST.RValue = AST.RDuration(value)
  }
  implicit val RValueConverterProblemInst: RValueConverter[almhirt.common.Problem] = new RValueConverter[almhirt.common.Problem] {
    def convert(value: almhirt.common.Problem): AST.RValue = AST.RError(value.message)
  }
  implicit val RValueConverterProblemCauseInst: RValueConverter[ProblemCause] = new RValueConverter[ProblemCause] {
    def convert(value: ProblemCause): AST.RValue = AST.RError(value.message)
  }

}

trait RValueOptionConverters { self: RValueConverters ⇒
  private def createOptionSomeConverterWrapperInst[T](implicit converter: RValueConverter[T]): RValueConverter[Option[T]] = new RValueConverter[Option[T]] {
    def convert(value: Option[T]): AST.RValue = 
      value match {
      case Some(t) => converter.convert(t)
       case None => AST.RNotAvailable
    }
      
  }

  implicit val SomeRVAlueConverterIdentityInst: RValueConverter[Option[AST.RValue]] = createOptionSomeConverterWrapperInst[AST.RValue]
  implicit val SomeRVAlueConverterComponentStateInst: RValueConverter[Option[ComponentState]] = createOptionSomeConverterWrapperInst[ComponentState]
  implicit val SomeRVAlueConverterStringInst: RValueConverter[Option[String]] = createOptionSomeConverterWrapperInst[String]
  implicit val SomeRVAlueConverterIntInst: RValueConverter[Option[Int]] = createOptionSomeConverterWrapperInst[Int]
  implicit val SomeRVAlueConverterLongInst: RValueConverter[Option[Long]] = createOptionSomeConverterWrapperInst[Long]
  implicit val SomeRVAlueConverterFloatInst: RValueConverter[Option[Float]] = createOptionSomeConverterWrapperInst[Float]
  implicit val SomeRVAlueConverterDoubleInst: RValueConverter[Option[Double]] = createOptionSomeConverterWrapperInst[Double]
  implicit val SomeRVAlueConverterrBooleanInst: RValueConverter[Option[Boolean]] = createOptionSomeConverterWrapperInst[Boolean]
  implicit val SomeRVAlueConverterLocalDateTimeInst: RValueConverter[Option[LocalDateTime]] = createOptionSomeConverterWrapperInst[LocalDateTime]
  implicit val SomeRVAlueConverterZonedDateTimeInst: RValueConverter[Option[ZonedDateTime]] = createOptionSomeConverterWrapperInst[ZonedDateTime]
  implicit val SomeRVAlueConverterDurationInst: RValueConverter[Option[scala.concurrent.duration.FiniteDuration]] = createOptionSomeConverterWrapperInst[scala.concurrent.duration.FiniteDuration]
  implicit val SomeRVAlueConverterProblemCauseInst: RValueConverter[Option[ProblemCause]] = createOptionSomeConverterWrapperInst[ProblemCause]
}