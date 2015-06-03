package almhirt.configuration

import scalaz.syntax.validation._
import scalaz.Validation.FlatMap._
import almhirt.common._
import almhirt.almvalidation.kit._

trait ParamsConverter[T] extends Function1[Any, AlmValidation[T]] {
  final def apply(v: Any): AlmValidation[T] = convertValue(v)
  def convertValue(v: Any): AlmValidation[T]
}

trait DefaultParamsConverters {
  implicit val ParamsParamsConverter = new ParamsConverter[Params] {
    override def convertValue(v: Any) =
      v match {
        case x: Params ⇒ x.success
        case x         ⇒ InvalidCastProblem(s""""${x.getClass().getName}"" can not be a "Params".""").failure
      }
  }

  implicit val StringParamsConverter = new ParamsConverter[String] {
    override def convertValue(v: Any) = v.toString().success
  }

  implicit val BooleanParamsConverter = new ParamsConverter[Boolean] {
    override def convertValue(v: Any) =
      v match {
        case x: Boolean ⇒ x.success
        case x: String  ⇒ x.toBooleanAlm
        case x          ⇒ InvalidCastProblem(s""""${x.getClass().getName}"" can not be a "Boolean".""").failure
      }
  }

  implicit val IntParamsConverter = new ParamsConverter[Int] {
    override def convertValue(v: Any) =
      v match {
        case x: Int    ⇒ x.success
        case x: String ⇒ x.toIntAlm
        case x         ⇒ InvalidCastProblem(s""""${x.getClass().getName}"" can not be an "Int".""").failure
      }
  }

  implicit val LongParamsConverter = new ParamsConverter[Long] {
    override def convertValue(v: Any) =
      v match {
        case x: Int    ⇒ x.toLong.success
        case x: Long   ⇒ x.success
        case x: String ⇒ x.toLongAlm
        case x         ⇒ InvalidCastProblem(s""""${x.getClass().getName}"" can not be a "Long".""").failure
      }
  }

  implicit val FloatParamsConverter = new ParamsConverter[Float] {
    override def convertValue(v: Any) =
      v match {
        case x: Float  ⇒ x.success
        case x: String ⇒ x.toFloatAlm
        case x         ⇒ InvalidCastProblem(s""""${x.getClass().getName}"" can not be a "Float".""").failure
      }
  }

  implicit val DoubleParamsConverter = new ParamsConverter[Double] {
    override def convertValue(v: Any) =
      v match {
        case x: Float  ⇒ x.toDouble.success
        case x: Double ⇒ x.success
        case x: Int    ⇒ x.toDouble.success
        case x: String ⇒ x.toDoubleAlm
        case x         ⇒ InvalidCastProblem(s""""${x.getClass().getName}"" can not be a "Float".""").failure
      }
  }
}
