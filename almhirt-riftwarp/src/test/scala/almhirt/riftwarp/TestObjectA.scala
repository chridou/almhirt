package almhirt.riftwarp

import almhirt.common._
import almhirt.almvalidation.kit._
import scalaz._, Scalaz._
import almhirt.common.AlmValidation
import java.util.UUID
import org.joda.time.DateTime

case class PrimitiveMAs(
  listString: List[String],
  listInt: List[Int],
  listDouble: List[Double],
  listBigDecimal: List[BigDecimal],
  listDateTime: List[DateTime],
  vectorString: Vector[String],
  vectorInt: Vector[Int],
  vectorDouble: Vector[Double],
  vectorBigDecimal: Vector[BigDecimal],
  vectorDateTime: Vector[DateTime],
  setString: Set[String],
  setInt: Set[Int],
  setDouble: Set[Double],
  setBigDecimal: Set[BigDecimal],
  setDateTime: Set[DateTime],
  iterableString: Iterable[String],
  iterableInt: Iterable[Int],
  iterableDouble: Iterable[Double],
  iterableBigDecimal: Iterable[BigDecimal],
  iterableDateTime: Iterable[DateTime])

case class TestObjectA(
  str: String,
  strOpt: Option[String],
  bool: Boolean,
  byte: Byte,
  int: Int,
  long: Long,
  bigInt: BigInt,
  float: Float,
  double: Double,
  bigDec: BigDecimal,
  dateTime: DateTime,
  uuid: UUID,
  arrayByte: Array[Byte],
  blob: Array[Byte],
  primitiveMAs: PrimitiveMAs,
  addressOpt: Option[TestAddress]) extends HasDefaultTypeDescriptor

object TestObjectA {
  val pete: TestObjectA =
    TestObjectA(
      str = "I am Pete",
      strOpt = Some("I am Henry, too"),
      bool = true,
      byte = 127,
      int = -237823,
      long = -278234263,
      bigInt = BigInt("265876257682376587365863876528756875682765252520577305007209857025728132213242"),
      float = 1.367232235F,
      double = 1.3672322350005D,
      bigDec = BigDecimal("23761247614876823746.23846749182408184098140981094809184834082307582375243658732465897259724"),
      dateTime = new DateTime(),
      uuid = UUID.randomUUID(),
      arrayByte = Array(126, -123, 12, -45, -128),
      blob = Array(0, 0, 0, 0, 0, 6, -123, 12, -45, -128, 112, 0, 0, 0),
      primitiveMAs = PrimitiveMAs(
        listString = List("alpha", "beta", "gamma", "delta"),
        listInt = List(1, 2, 3, 4, 5, 6, 7, 8, 9, 10),
        listDouble = List(1.0, 0.5, 0.2, 0.125),
        listBigDecimal = List(BigDecimal("1.333333"), BigDecimal("1.33333335"), BigDecimal("1.6666666"), BigDecimal("1.6666667")),
        listDateTime = List(new DateTime().plusHours(1), new DateTime().plusHours(2), new DateTime().plusHours(3), new DateTime().plusHours(4)),
        vectorString = Vector("alpha", "beta", "gamma", "delta"),
        vectorInt = Vector(1, 2, 3, 4, 5, 6, 7, 8, 9, 10),
        vectorDouble = Vector(1.0, 0.5, 0.2, 0.125),
        vectorBigDecimal = Vector(BigDecimal("1.333333"), BigDecimal("1.33333335"), BigDecimal("1.6666666"), BigDecimal("1.6666667")),
        vectorDateTime = Vector(new DateTime().plusHours(1), new DateTime().plusHours(2), new DateTime().plusHours(3), new DateTime().plusHours(4)),
        setString = Set("alpha", "beta", "gamma", "delta"),
        setInt = Set(1, 2, 3, 4, 5, 6, 7, 8, 9, 10),
        setDouble = Set(1.0, 0.5, 0.2, 0.125),
        setBigDecimal = Set(BigDecimal("1.333333"), BigDecimal("1.33333335"), BigDecimal("1.6666666"), BigDecimal("1.6666667")),
        setDateTime = Set(new DateTime().plusHours(1), new DateTime().plusHours(2), new DateTime().plusHours(3), new DateTime().plusHours(4)),
        iterableString = Iterable("alpha", "beta", "gamma", "delta"),
        iterableInt = Iterable(1, 2, 3, 4, 5, 6, 7, 8, 9, 10),
        iterableDouble = Iterable(1.0, 0.5, 0.2, 0.125),
        iterableBigDecimal = Iterable(BigDecimal("1.333333"), BigDecimal("1.33333335"), BigDecimal("1.6666666"), BigDecimal("1.6666667")),
        iterableDateTime = Iterable(new DateTime().plusHours(1), new DateTime().plusHours(2), new DateTime().plusHours(3), new DateTime().plusHours(4))),
      addressOpt = Some(TestAddress("Berlin", "At the wall 89")))
}

