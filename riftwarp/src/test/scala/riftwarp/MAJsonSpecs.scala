package riftwarp

import org.scalatest._
import org.scalatest.matchers.ShouldMatchers
import org.joda.time.DateTime
import scalaz.syntax.validation._
import almhirt.common._
import almhirt.almvalidation.kit._
import riftwarp._

class MAJsonSpecs extends FlatSpec with ShouldMatchers {
  val riftWarp = {
    val rw = RiftWarp.concurrentWithDefaults()

    rw.barracks.addDecomposer(new PrimitiveListMAsDecomposer())
    rw.barracks.addRecomposer(new PrimitiveListMAsRecomposer())
    rw.barracks.addDecomposer(new PrimitiveVectorMAsDecomposer())
    rw.barracks.addRecomposer(new PrimitiveVectorMAsRecomposer())
    rw.barracks.addDecomposer(new PrimitiveSetMAsDecomposer())
    rw.barracks.addRecomposer(new PrimitiveSetMAsRecomposer())
    rw.barracks.addDecomposer(new PrimitiveIterableMAsDecomposer())
    rw.barracks.addRecomposer(new PrimitiveIterableMAsRecomposer())
    rw.barracks.addDecomposer(new ComplexMAsDecomposer())
    rw.barracks.addRecomposer(new ComplexMAsRecomposer())
    rw
  }

  val primitiveVectorMAs = PrimitiveVectorMAs(
    vectorString = Vector("alpha", "beta", "gamma", "delta"),
    vectorInt = Vector(1, 2, 3, 4, 5, 6, 7, 8, 9, 10),
    vectorDouble = Vector(1.0, 0.5, 0.2, 0.125),
    vectorBigDecimal = Vector(BigDecimal("1.333333"), BigDecimal("1.33333335"), BigDecimal("1.6666666"), BigDecimal("1.6666667")),
    vectorDateTime = Vector(new DateTime("2013-01-23T06:23:14.421+01:00").plusHours(1), new DateTime("2013-01-23T06:23:14.421+01:00").plusHours(2), new DateTime("2013-01-23T06:23:14.421+01:00").plusHours(3), new DateTime("2013-01-23T06:23:14.421+01:00").plusHours(4)))
  val primitiveVectorMAsJson = """{"riftdesc":"riftwarp.PrimitiveVectorMAs","vectorString":["alpha","beta","gamma","delta"],"vectorInt":[1,2,3,4,5,6,7,8,9,10],"vectorDouble":[1.0,0.5,0.2,0.125],"vectorBigDecimal":["1.333333","1.33333335","1.6666666","1.6666667"],"vectorDateTime":["2013-01-23T07:23:14.421+01:00","2013-01-23T08:23:14.421+01:00","2013-01-23T09:23:14.421+01:00","2013-01-23T10:23:14.421+01:00"]}"""
  val primitiveVectorMAsJsonWithoutTypeDescriptor = """{"vectorString":["alpha","beta","gamma","delta"],"vectorInt":[1,2,3,4,5,6,7,8,9,10],"vectorDouble":[1.0,0.5,0.2,0.125],"vectorBigDecimal":["1.333333","1.33333335","1.6666666","1.6666667"],"vectorDateTime":["2013-01-23T07:23:14.421+01:00","2013-01-23T08:23:14.421+01:00","2013-01-23T09:23:14.421+01:00","2013-01-23T10:23:14.421+01:00"]}"""

  "RiftWarp" should
    "succeed in dematerialzing PrimitiveVectorMAs" in {
      riftWarp.prepareForWarp[DimensionString](RiftChannel.Json, None)(primitiveVectorMAs).isSuccess should be(true)
    }
    it should "succeed in rematerialzing PrimitiveVectorMAs" in {
      riftWarp.receiveFromWarp[DimensionString, PrimitiveVectorMAs](RiftChannel.Json, None)(DimensionString(primitiveVectorMAsJson)).isSuccess should be(true)
    }
    it should "succeed in dematerialzing PrimitiveVectorMAs correctly" in {
      val res = riftWarp.prepareForWarp[DimensionString](RiftChannel.Json, None)(primitiveVectorMAs)
      res should equal(scalaz.Success(DimensionString(primitiveVectorMAsJson)))
    }
    it should "rematerialze PrimitiveVectorMAs correctly" in {
     val res = riftWarp.receiveFromWarp[DimensionString, PrimitiveVectorMAs](RiftChannel.Json, None)(DimensionString(primitiveVectorMAsJson))
     res should equal(scalaz.Success(primitiveVectorMAs))
  }
}