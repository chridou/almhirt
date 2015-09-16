package almhirt.akkax.reporting

import java.time.{ LocalDateTime, ZonedDateTime }
import almhirt.problem.ProblemCause
import almhirt.akkax.ComponentState

trait RValueConverter[T] {
  def convert(value: T): AST.RValue
}

trait RValueIdentityConverters {
  implicit val RValueIdentityConverterComponentStateInst: RValueConverter[AST.RComponentState] = new RValueConverter[AST.RComponentState] {
    def convert(value: AST.RComponentState): AST.RValue = value
  }
  implicit val RValueIdentityConverterStringInst: RValueConverter[AST.RString] = new RValueConverter[AST.RString] {
    def convert(value: AST.RString): AST.RValue = value
  }
  implicit val RValueIdentityConverterIntInst: RValueConverter[AST.RInteger] = new RValueConverter[AST.RInteger] {
    def convert(value: AST.RInteger): AST.RValue = value
  }
  implicit val RValueIdentityConverterFloatInst: RValueConverter[AST.RFloat] = new RValueConverter[AST.RFloat] {
    def convert(value: AST.RFloat): AST.RValue = value
  }
  implicit val RValueIdentityConverterBooleanInst: RValueConverter[AST.RBool] = new RValueConverter[AST.RBool] {
    def convert(value: AST.RBool): AST.RValue = value
  }
  implicit val RValueIdentityConverterLocalDateTimeInst: RValueConverter[AST.RLocalDateTime] = new RValueConverter[AST.RLocalDateTime] {
    def convert(value: AST.RLocalDateTime): AST.RValue = value
  }
  implicit val RValueIdentityConverterZonedDateTimeInst: RValueConverter[AST.RZonedDateTime] = new RValueConverter[AST.RZonedDateTime] {
    def convert(value: AST.RZonedDateTime): AST.RValue = value
  }
  implicit val RValueIdentityConverterDurationInst: RValueConverter[AST.RDuration] = new RValueConverter[AST.RDuration] {
    def convert(value: AST.RDuration): AST.RValue = value
  }
  implicit val RValueIdentityConverterErrorInst: RValueConverter[AST.RError] = new RValueConverter[AST.RError] {
    def convert(value: AST.RError): AST.RValue = value
  }

  implicit val RValueIdentityConverterReportInst: RValueConverter[AST.RReport] = new RValueConverter[AST.RReport] {
    def convert(value: AST.RReport): AST.RValue = value
  }
}

trait RValueConverters {
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

trait RValueOptionConverters { self: RValueConverters with RValueIdentityConverters ⇒
  private def createOptionSomeConverterWrapperInst[T: RValueConverter]: RValueConverter[Option[T]] = new RValueConverter[Option[T]] {
    def convert(value: Option[T]): AST.RValue =
      value match {
        case Some(t) ⇒ toAST(t)
        case None    ⇒ AST.RNotAvailable
      }

  }

  implicit val OptionRValueIdentityConverterComponentStateInst: RValueConverter[Option[AST.RComponentState]] = createOptionSomeConverterWrapperInst[AST.RComponentState]
  implicit val OptionRValueIdentityConverterStringInst: RValueConverter[Option[AST.RString]] = createOptionSomeConverterWrapperInst[AST.RString]
  implicit val OptionRValueIdentityConverterIntInst: RValueConverter[Option[AST.RInteger]] = createOptionSomeConverterWrapperInst[AST.RInteger]
  implicit val OptionRValueIdentityConverterFloatInst: RValueConverter[Option[AST.RFloat]] = createOptionSomeConverterWrapperInst[AST.RFloat]
  implicit val OptionRValueIdentityConverterBooleanInst: RValueConverter[Option[AST.RBool]] = createOptionSomeConverterWrapperInst[AST.RBool]
  implicit val OptionRValueIdentityConverterLocalDateTimeInst: RValueConverter[Option[AST.RLocalDateTime]] = createOptionSomeConverterWrapperInst[AST.RLocalDateTime]
  implicit val OptionRValueIdentityConverterZonedDateTimeInst: RValueConverter[Option[AST.RZonedDateTime]] = createOptionSomeConverterWrapperInst[AST.RZonedDateTime]
  implicit val OptionRValueIdentityConverterDurationInst: RValueConverter[Option[AST.RDuration]] = createOptionSomeConverterWrapperInst[AST.RDuration]
  implicit val OptionRValueIdentityConverterErrorInst: RValueConverter[Option[AST.RError]] = createOptionSomeConverterWrapperInst[AST.RError]
  implicit val OptionRValueIdentityConverterReportInst: RValueConverter[Option[AST.RReport]] = createOptionSomeConverterWrapperInst[AST.RReport]


  implicit val OptionRVAlueConverterComponentStateInst: RValueConverter[Option[ComponentState]] = createOptionSomeConverterWrapperInst[ComponentState]
  implicit val OptionRVAlueConverterStringInst: RValueConverter[Option[String]] = createOptionSomeConverterWrapperInst[String]
  implicit val OptionRVAlueConverterIntInst: RValueConverter[Option[Int]] = createOptionSomeConverterWrapperInst[Int]
  implicit val OptionRVAlueConverterLongInst: RValueConverter[Option[Long]] = createOptionSomeConverterWrapperInst[Long]
  implicit val OptionRVAlueConverterFloatInst: RValueConverter[Option[Float]] = createOptionSomeConverterWrapperInst[Float]
  implicit val OptionRVAlueConverterDoubleInst: RValueConverter[Option[Double]] = createOptionSomeConverterWrapperInst[Double]
  implicit val OptionRVAlueConverterrBooleanInst: RValueConverter[Option[Boolean]] = createOptionSomeConverterWrapperInst[Boolean]
  implicit val OptionRVAlueConverterLocalDateTimeInst: RValueConverter[Option[LocalDateTime]] = createOptionSomeConverterWrapperInst[LocalDateTime]
  implicit val OptionRVAlueConverterZonedDateTimeInst: RValueConverter[Option[ZonedDateTime]] = createOptionSomeConverterWrapperInst[ZonedDateTime]
  implicit val OptionRVAlueConverterDurationInst: RValueConverter[Option[scala.concurrent.duration.FiniteDuration]] = createOptionSomeConverterWrapperInst[scala.concurrent.duration.FiniteDuration]
}