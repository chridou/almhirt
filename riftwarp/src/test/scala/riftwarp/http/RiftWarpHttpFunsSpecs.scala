package riftwarp.http

import org.scalatest._
import org.scalatest.matchers.ShouldMatchers
import org.joda.time.DateTime
import scalaz.syntax.validation._
import almhirt.common._
import almhirt.almvalidation.kit._
import riftwarp._
import riftwarp.impl.UnsafeChannelRegistry

class RiftWarpHttpFunsSpecs extends FlatSpec with ShouldMatchers {
  import RiftWarpHttpFuns._
  val riftWarp = {
    val rw = RiftWarp.concurrentWithDefaults()
    rw.barracks.addDecomposer(new TestObjectADecomposer())
    rw.barracks.addRecomposer(new TestObjectARecomposer())
    rw.barracks.addDecomposer(new TestAddressDecomposer())
    rw.barracks.addRecomposer(new TestAddressRecomposer())

    rw.barracks.addDecomposer(new PrimitiveTypesDecomposer())
    rw.barracks.addRecomposer(new PrimitiveTypesRecomposer())
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
    rw.barracks.addDecomposer(new PrimitiveMapsDecomposer())
    rw.barracks.addRecomposer(new PrimitiveMapsRecomposer())
    rw.barracks.addDecomposer(new ComplexMapsDecomposer())
    rw.barracks.addRecomposer(new ComplexMapsRecomposer())
    rw
  }

  val settings = RiftHttpFunsSettings(riftWarp, true, almhirt.http.impl.JustForTestingProblemLaundry, _ => (), RiftChannel.Json, new RiftHttpContentTypeWithPrefixOps("testprefix", riftWarp.channels))
  def createBody(bodyType: RiftHttpBodyType) =
    bodyType match {
      case RiftStringBodyType => RiftStringBody("").success
      case RiftBinaryBodyType => RiftBinaryBody(Array.empty).success
    }

  """createHttpDataFromRequest""" should
    """return Success(RiftHttpDataWithoutContent) for a None content type and an empty body""" in {
      createHttpDataFromRequest(() => None, bodyType => RiftStringBody("").success)(settings.contentTypeOps) should equal(scalaz.Success(RiftHttpDataWithoutContent))
    }
  it should """return Success(RiftHttpDataWithoutContent) for a None content type and a non empty body""" in {
    createHttpDataFromRequest(() => None, bodyType => RiftStringBody("a").success)(settings.contentTypeOps) should equal(scalaz.Success(RiftHttpDataWithoutContent))
  }
  it should """fail for a Some("") as the content type and an empty body""" in {
    createHttpDataFromRequest(() => Some(""), bodyType => RiftStringBody("").success)(settings.contentTypeOps).isFailure
  }

  it should """return Success(RiftHttpDataWithContent(RiftHttpChannelContentType(RiftChannel.Json), RiftStringBody("")))) for a Some("text/json") content type and an empty body""" in {
    createHttpDataFromRequest(() => Some("text/json"), createBody)(settings.contentTypeOps) should equal(scalaz.Success(RiftHttpDataWithContent(RiftHttpChannelContentType(RiftChannel.Json), RiftStringBody(""))))
  }

  it should """fail for a Some("wrong") content type and an empty body""" in {
    createHttpDataFromRequest(() => Some("wrong"), createBody)(settings.contentTypeOps).isFailure
  }

  it should """return Success(RiftHttpDataWithContent(RiftHttpQualifiedContentType(RiftDescriptor("xxx", 1), RiftChannel.Json), RiftStringBody("")))) for a Some("application/vnd.testprefix.xxx+json;version=1") content type and an empty body""" in {
    createHttpDataFromRequest(() => Some("application/vnd.testprefix.xxx+json;version=1"), createBody)(settings.contentTypeOps) should equal(scalaz.Success(RiftHttpDataWithContent(RiftHttpQualifiedContentType(RiftDescriptor("xxx", 1), RiftChannel.Json), RiftStringBody(""))))
  }

  it should """fail for a Some("application/vnd.testprefix.xxx+XXXson;version=1") content type and an empty body""" in {
    createHttpDataFromRequest(() => Some("application/vnd.testprefix.xxx+XXXson;version=1"), createBody)(settings.contentTypeOps).isFailure
  }

  val testdataPrim = PrimitiveTypes(
    str = """I am Pete""",
    bool = true,
    byte = 127,
    int = -237823,
    long = -278234263,
    bigInt = BigInt("265876257682376587365863876528756875682765252520577305007209857025728132213242"),
    float = 1.3675F,
    double = 1.3672322350005D,
    bigDec = BigDecimal("23761247614876823746.23846749182408184098140981094809184834082307582375243658732465897259724"),
    dateTime = new DateTime("2013-01-23T06:23:14.421+01:00"),
    uuid = java.util.UUID.fromString("63d0847f-718d-4d00-8267-09d809477589"))

  val testdataPrimJson =
    """	|{"riftdesc":"riftwarp.PrimitiveTypes",
      		| "str":"I am Pete",
      		| "bool":true,
      		| "byte":127,
      		| "int":-237823,
      		| "long":-278234263,
      		| "bigInt":"265876257682376587365863876528756875682765252520577305007209857025728132213242",
      		| "float":1.3675,
      		| "double":1.3672322350005,
      		| "bigDec":"23761247614876823746.23846749182408",
      		| "dateTime":"2013-01-23T06:23:14.421+01:00",
      		| "uuid":"63d0847f-718d-4d00-8267-09d809477589"}""".stripMargin

  val testdataPrimJsonWithoutDescriptor =
    """	|{"str":"I am Pete",
      		| "bool":true,
      		| "byte":127,
      		| "int":-237823,
      		| "long":-278234263,
      		| "bigInt":"265876257682376587365863876528756875682765252520577305007209857025728132213242",
      		| "float":1.3675,
      		| "double":1.3672322350005,
      		| "bigDec":"23761247614876823746.23846749182408",
      		| "dateTime":"2013-01-23T06:23:14.421+01:00",
      		| "uuid":"63d0847f-718d-4d00-8267-09d809477589"}""".stripMargin

  "transformFromHttpData(targeting 'PrimitiveTypes' from JSON)" should
    """be able to recreate PrimitiveTypes from proper request data with RiftHttpChannelContentType(RiftChannel.Json)""" in {
      val recreated =
        transformFromHttpData[PrimitiveTypes](riftWarp)(RiftHttpDataWithContent(
          RiftHttpChannelContentType(RiftChannel.Json),
          RiftStringBody(testdataPrimJson)))
      recreated should equal(scalaz.Success(testdataPrim))
    }
  it should """be able to recreate PrimitiveTypes from proper request data with RiftHttpQualifiedContentType(RiftDescriptor(classOf[PrimitiveTypes]), RiftChannel.Json)""" in {
    val recreated =
      transformFromHttpData[PrimitiveTypes](riftWarp)(RiftHttpDataWithContent(
        RiftHttpQualifiedContentType(RiftDescriptor(classOf[PrimitiveTypes]), RiftChannel.Json),
        RiftStringBody(testdataPrimJson)))
    recreated should equal(scalaz.Success(testdataPrim))
  }
  it should """be able to recreate PrimitiveTypes from proper request data with RiftHttpQualifiedContentType(RiftDescriptor(classOf[PrimitiveTypes]), RiftChannel.Json) when the target type is a supertype of the target type""" in {
    val recreated =
      transformFromHttpData[AnyRef](riftWarp)(RiftHttpDataWithContent(
        RiftHttpQualifiedContentType(RiftDescriptor(classOf[PrimitiveTypes]), RiftChannel.Json),
        RiftStringBody(testdataPrimJson)))
    recreated should equal(scalaz.Success(testdataPrim))
  }
  it should """fail on correct content e.g. RiftHttpQualifiedContentType(RiftDescriptor(classOf[PrimitiveTypes]), RiftChannel.Json) when the target type is a not a supertype of the target type""" in {
    val recreated =
      transformFromHttpData[PrimitiveMaps](riftWarp)(RiftHttpDataWithContent(
        RiftHttpQualifiedContentType(RiftDescriptor(classOf[PrimitiveTypes]), RiftChannel.Json),
        RiftStringBody(testdataPrimJson)))
    recreated.isFailure
  }
  it should """be able to recreate PrimitiveTypes from proper request data with RiftHttpQualifiedContentType(RiftDescriptor(classOf[PrimitiveTypes]), RiftChannel.Json) even if the content has no RiftDescriptor""" in {
    val recreated =
      transformFromHttpData[PrimitiveTypes](riftWarp)(RiftHttpDataWithContent(
        RiftHttpQualifiedContentType(RiftDescriptor(classOf[PrimitiveTypes]), RiftChannel.Json),
        RiftStringBody(testdataPrimJsonWithoutDescriptor)))
    recreated should equal(scalaz.Success(testdataPrim))
  }
  it should """be able to recreate PrimitiveTypes from proper request data with RiftHttpChannelContentType(RiftChannel.Json) even if the content has no RiftDescriptor when the type parameter is set to the target type""" in {
    val recreated =
      transformFromHttpData[String](riftWarp)(RiftHttpDataWithContent(
        RiftHttpChannelContentType(RiftChannel.Json),
        RiftStringBody(testdataPrimJsonWithoutDescriptor)))
    recreated.isFailure
  }
  it should """fail on RiftHttpChannelContentType(RiftChannel.Json) if the content has no RiftDescriptor and the type parameter is not set to the target type[String]""" in {
    val recreated =
      transformFromHttpData[PrimitiveTypes](riftWarp)(RiftHttpDataWithContent(
        RiftHttpChannelContentType(RiftChannel.Json),
        RiftStringBody(testdataPrimJsonWithoutDescriptor)))
    recreated should equal(scalaz.Success(testdataPrim))
  }
  it should """fail on wrong channel data with RiftHttpQualifiedContentType(RiftDescriptor(classOf[PrimitiveTypes]), RiftChannel.Xml)""" in {
    val recreated =
      transformFromHttpData[PrimitiveTypes](riftWarp)(RiftHttpDataWithContent(
        RiftHttpQualifiedContentType(RiftDescriptor(classOf[PrimitiveTypes]), RiftChannel.Xml),
        RiftStringBody(testdataPrimJson)))
    recreated.isFailure
  }
  it should """fail on malformed content channel data with RiftHttpQualifiedContentType(RiftDescriptor(classOf[PrimitiveTypes]), RiftChannel.Json)""" in {
    val recreated =
      transformFromHttpData[PrimitiveTypes](riftWarp)(RiftHttpDataWithContent(
        RiftHttpQualifiedContentType(RiftDescriptor(classOf[PrimitiveTypes]), RiftChannel.Json),
        RiftStringBody(testdataPrimJson.drop(10))))
    recreated.isFailure
  }
  it should """be able to recreate PrimitiveTypes with wrong RiftDescriptor in RiftHttpQualifiedContentType(RiftDescriptor(classOf[String]), RiftChannel.Json) because the content has the correct RiftDescriptor which has priority""" in {
    val recreated =
      transformFromHttpData[PrimitiveTypes](riftWarp)(RiftHttpDataWithContent(
        RiftHttpQualifiedContentType(RiftDescriptor(classOf[String]), RiftChannel.Json),
        RiftStringBody(testdataPrimJson)))
    recreated should equal(scalaz.Success(testdataPrim))
  }
  it should """fail on wrong RiftDescriptor RiftHttpQualifiedContentType(RiftDescriptor(classOf[String]), RiftChannel.Json) when content contains no RiftDescriptor""" in {
    val recreated =
      transformFromHttpData[PrimitiveTypes](riftWarp)(RiftHttpDataWithContent(
        RiftHttpQualifiedContentType(RiftDescriptor(classOf[String]), RiftChannel.Json),
        RiftStringBody(testdataPrimJsonWithoutDescriptor)))
    recreated.isFailure
  }
}