package riftwarp.http

import org.scalatest._
import org.scalatest.matchers.ShouldMatchers
import org.joda.time.DateTime
import scalaz.syntax.validation._
import almhirt.common._
import almhirt.almvalidation.kit._
import almhirt.http._
import riftwarp._

class RiftWarpHttpFunsSpecs extends WordSpec with ShouldMatchers {
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

  val settings = RiftHttpFunsSettings(riftWarp, false, almhirt.http.impl.JustForTestingProblemLaundry, _ => (), RiftChannel.Json, new RiftHttpContentTypeWithPrefixOps("testprefix", riftWarp.channels))
  def createBody(bodyType: RiftHttpBodyType) =
    bodyType match {
      case RiftStringBodyType => RiftStringBody("").success
      case RiftBinaryBodyType => RiftBinaryBody(Array.empty).success
    }

  """createHttpDataFromRequest""" should {
    """return Success(RiftHttpDataWithoutContent) for a None content type and an empty body""" in {
      createHttpDataFromRequest(() => None, bodyType => RiftStringBody("").success)(settings.contentTypeOps) should equal(scalaz.Success(RiftHttpDataWithoutContent))
    }
    """return Success(RiftHttpDataWithoutContent) for a None content type and a non empty body""" in {
      createHttpDataFromRequest(() => None, bodyType => RiftStringBody("a").success)(settings.contentTypeOps) should equal(scalaz.Success(RiftHttpDataWithoutContent))
    }
    """fail for a Some("") as the content type and an empty body""" in {
      createHttpDataFromRequest(() => Some(""), bodyType => RiftStringBody("").success)(settings.contentTypeOps).isFailure
    }

    """return Success(RiftHttpDataWithContent(RiftHttpChannelContentType(RiftChannel.Json), RiftStringBody("")))) for a Some("text/json") content type and an empty body""" in {
      createHttpDataFromRequest(() => Some("text/json"), createBody)(settings.contentTypeOps) should equal(scalaz.Success(RiftHttpDataWithContent(RiftHttpChannelContentType(RiftChannel.Json), RiftStringBody(""))))
    }

    """fail for a Some("wrong") content type and an empty body""" in {
      createHttpDataFromRequest(() => Some("wrong"), createBody)(settings.contentTypeOps).isFailure
    }

    """return Success(RiftHttpDataWithContent(RiftHttpQualifiedContentType(RiftDescriptor("xxx", 1), RiftChannel.Json), RiftStringBody("")))) for a Some("application/vnd.testprefix.xxx+json;version=1") content type and an empty body""" in {
      createHttpDataFromRequest(() => Some("application/vnd.testprefix.xxx+json;version=1"), createBody)(settings.contentTypeOps) should equal(scalaz.Success(RiftHttpDataWithContent(RiftHttpQualifiedContentType(RiftDescriptor("xxx", 1), RiftChannel.Json), RiftStringBody(""))))
    }

    """fail for a Some("application/vnd.testprefix.xxx+XXXson;version=1") content type and an empty body""" in {
      createHttpDataFromRequest(() => Some("application/vnd.testprefix.xxx+XXXson;version=1"), createBody)(settings.contentTypeOps).isFailure
    }
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

  val primitiveVectorMAs = PrimitiveVectorMAs(
    vectorString = Vector("alpha", "beta", "gamma", "delta"),
    vectorInt = Vector(1, 2, 3, 4, 5, 6, 7, 8, 9, 10),
    vectorDouble = Vector(1.0, 0.5, 0.2, 0.125),
    vectorBigDecimal = Vector(BigDecimal("1.333333"), BigDecimal("1.33333335"), BigDecimal("1.6666666"), BigDecimal("1.6666667")),
    vectorDateTime = Vector(new DateTime("2013-01-23T06:23:14.421+01:00").plusHours(1), new DateTime("2013-01-23T06:23:14.421+01:00").plusHours(2), new DateTime("2013-01-23T06:23:14.421+01:00").plusHours(3), new DateTime("2013-01-23T06:23:14.421+01:00").plusHours(4)))

  val primitiveVectorMAsJson = """{"riftdesc":"riftwarp.PrimitiveVectorMAs","vectorString":["alpha","beta","gamma","delta"],"vectorInt":[1,2,3,4,5,6,7,8,9,10],"vectorDouble":[1.0,0.5,0.2,0.125],"vectorBigDecimal":["1.333333","1.33333335","1.6666666","1.6666667"],"vectorDateTime":["2013-01-23T07:23:14.421+01:00","2013-01-23T08:23:14.421+01:00","2013-01-23T09:23:14.421+01:00","2013-01-23T10:23:14.421+01:00"]}"""
  val primitiveVectorMAsJsonWithoutTypeDescriptor = """{"vectorString":["alpha","beta","gamma","delta"],"vectorInt":[1,2,3,4,5,6,7,8,9,10],"vectorDouble":[1.0,0.5,0.2,0.125],"vectorBigDecimal":["1.333333","1.33333335","1.6666666","1.6666667"],"vectorDateTime":["2013-01-23T07:23:14.421+01:00","2013-01-23T08:23:14.421+01:00","2013-01-23T09:23:14.421+01:00","2013-01-23T10:23:14.421+01:00"]}"""

  val primitiveVectorRequestDataQualifiedWithDescriptorInBody = RiftHttpDataWithContent(RiftHttpQualifiedContentType("riftwarp.PrimitiveVectorMAs", RiftChannel.Json, Map()), RiftStringBody(primitiveVectorMAsJson))
  val primitiveVectorRequestDataUnqualifiedWithDescriptorInBody = RiftHttpDataWithContent(RiftHttpChannelContentType(RiftChannel.Json, Map()), RiftStringBody(primitiveVectorMAsJson))

  val primitiveVectorRequestDataQualified = RiftHttpDataWithContent(RiftHttpQualifiedContentType("riftwarp.PrimitiveVectorMAs", RiftChannel.Json, Map()), RiftStringBody(primitiveVectorMAsJsonWithoutTypeDescriptor))
  val primitiveVectorRequestDataUnqualified = RiftHttpDataWithContent(RiftHttpChannelContentType(RiftChannel.Json, Map()), RiftStringBody(primitiveVectorMAsJsonWithoutTypeDescriptor))

  def createPrimitiveTypesResponseDataJson(statusCode: almhirt.http.HttpStatusCode) = RiftHttpResponse(statusCode, RiftHttpDataWithContent(RiftHttpQualifiedContentType("riftwarp.PrimitiveTypes", RiftChannel.Json, Map()), RiftStringBody("""{"riftdesc":"riftwarp.PrimitiveTypes","str":"I am Pete","bool":true,"byte":127,"int":-237823,"long":-278234263,"bigInt":"265876257682376587365863876528756875682765252520577305007209857025728132213242","float":1.3674999475479126,"double":1.3672322350005,"bigDec":"23761247614876823746.23846749182408","dateTime":"2013-01-23T06:23:14.421+01:00","uuid":"63d0847f-718d-4d00-8267-09d809477589"}""")))

  "transformFromHttpData(targeting 'PrimitiveTypes' from JSON)" should {
    """recreate PrimitiveTypes from proper request data with RiftHttpChannelContentType(RiftChannel.Json)""" in {
      val recreated =
        transformFromHttpData[PrimitiveTypes](riftWarp)(RiftHttpDataWithContent(
          RiftHttpChannelContentType(RiftChannel.Json),
          RiftStringBody(testdataPrimJson)))
      recreated should equal(scalaz.Success(testdataPrim))
    }
    """recreate PrimitiveTypes from proper request data with RiftHttpQualifiedContentType(RiftDescriptor(classOf[PrimitiveTypes]), RiftChannel.Json)""" in {
      val recreated =
        transformFromHttpData[PrimitiveTypes](riftWarp)(RiftHttpDataWithContent(
          RiftHttpQualifiedContentType(RiftDescriptor(classOf[PrimitiveTypes]), RiftChannel.Json),
          RiftStringBody(testdataPrimJson)))
      recreated should equal(scalaz.Success(testdataPrim))
    }
    """recreate PrimitiveTypes from proper request data with RiftHttpQualifiedContentType(RiftDescriptor(classOf[PrimitiveTypes]), RiftChannel.Json) when the target type is a supertype of the target type""" in {
      val recreated =
        transformFromHttpData[AnyRef](riftWarp)(RiftHttpDataWithContent(
          RiftHttpQualifiedContentType(RiftDescriptor(classOf[PrimitiveTypes]), RiftChannel.Json),
          RiftStringBody(testdataPrimJson)))
      recreated should equal(scalaz.Success(testdataPrim))
    }
    """fail on correct content e.g. RiftHttpQualifiedContentType(RiftDescriptor(classOf[PrimitiveTypes]), RiftChannel.Json) when the target type is a not a supertype of the target type""" in {
      val recreated =
        transformFromHttpData[PrimitiveMaps](riftWarp)(RiftHttpDataWithContent(
          RiftHttpQualifiedContentType(RiftDescriptor(classOf[PrimitiveTypes]), RiftChannel.Json),
          RiftStringBody(testdataPrimJson)))
      recreated.isFailure should be(true)
    }
    """recreate PrimitiveTypes from proper request data with RiftHttpQualifiedContentType(RiftDescriptor(classOf[PrimitiveTypes]), RiftChannel.Json) even if the content has no RiftDescriptor""" in {
      val recreated =
        transformFromHttpData[PrimitiveTypes](riftWarp)(RiftHttpDataWithContent(
          RiftHttpQualifiedContentType(RiftDescriptor(classOf[PrimitiveTypes]), RiftChannel.Json),
          RiftStringBody(testdataPrimJsonWithoutDescriptor)))
      recreated should equal(scalaz.Success(testdataPrim))
    }
    """recreate PrimitiveTypes from proper request data with RiftHttpChannelContentType(RiftChannel.Json) even if the content has no RiftDescriptor when the type parameter is set to the target type""" in {
      val recreated =
        transformFromHttpData[String](riftWarp)(RiftHttpDataWithContent(
          RiftHttpChannelContentType(RiftChannel.Json),
          RiftStringBody(testdataPrimJsonWithoutDescriptor)))
      recreated.isFailure should be(true)
    }
    """fail on RiftHttpChannelContentType(RiftChannel.Json) if the content has no RiftDescriptor and the type parameter is not set to the target type[String]""" in {
      val recreated =
        transformFromHttpData[PrimitiveTypes](riftWarp)(RiftHttpDataWithContent(
          RiftHttpChannelContentType(RiftChannel.Json),
          RiftStringBody(testdataPrimJsonWithoutDescriptor)))
      recreated should equal(scalaz.Success(testdataPrim))
    }
    """fail on wrong channel data with RiftHttpQualifiedContentType(RiftDescriptor(classOf[PrimitiveTypes]), RiftChannel.Xml)""" in {
      val recreated =
        transformFromHttpData[PrimitiveTypes](riftWarp)(RiftHttpDataWithContent(
          RiftHttpQualifiedContentType(RiftDescriptor(classOf[PrimitiveTypes]), RiftChannel.Xml),
          RiftStringBody(testdataPrimJson)))
      recreated.isFailure should be(true)
    }
    """fail on malformed content channel data with RiftHttpQualifiedContentType(RiftDescriptor(classOf[PrimitiveTypes]), RiftChannel.Json)""" in {
      val recreated =
        transformFromHttpData[PrimitiveTypes](riftWarp)(RiftHttpDataWithContent(
          RiftHttpQualifiedContentType(RiftDescriptor(classOf[PrimitiveTypes]), RiftChannel.Json),
          RiftStringBody(testdataPrimJson.drop(10))))
      recreated.isFailure should be(true)
    }
    """recreate PrimitiveTypes with wrong RiftDescriptor in RiftHttpQualifiedContentType(RiftDescriptor(classOf[String]), RiftChannel.Json) because the content has the correct RiftDescriptor which has priority""" in {
      val recreated =
        transformFromHttpData[PrimitiveTypes](riftWarp)(RiftHttpDataWithContent(
          RiftHttpQualifiedContentType(RiftDescriptor(classOf[String]), RiftChannel.Json),
          RiftStringBody(testdataPrimJson)))
      recreated should equal(scalaz.Success(testdataPrim))
    }
    """fail on wrong RiftDescriptor RiftHttpQualifiedContentType(RiftDescriptor(classOf[String]), RiftChannel.Json) when content contains no RiftDescriptor""" in {
      val recreated =
        transformFromHttpData[PrimitiveTypes](riftWarp)(RiftHttpDataWithContent(
          RiftHttpQualifiedContentType(RiftDescriptor(classOf[String]), RiftChannel.Json),
          RiftStringBody(testdataPrimJsonWithoutDescriptor)))
      recreated.isFailure should be(true)
    }
  }

  "transformFromHttpData(targeting 'PrimitiveVectorMAs' from JSON)" should {
    """recreate PrimitiveVectorMAs from proper request data with RiftHttpChannelContentType(RiftChannel.Json)""" in {
      val recreated =
        transformFromHttpData[PrimitiveTypes](riftWarp)(RiftHttpDataWithContent(
          RiftHttpChannelContentType(RiftChannel.Json),
          RiftStringBody(primitiveVectorMAsJson)))
      recreated should equal(scalaz.Success(primitiveVectorMAs))
    }
  }

  "createHttpProblemResponseData" when {
    "no channel is provided and settings specifies JSON as a default channel" should {
      "create a JSON response for an 'UnspecifiedProblem" in {
        val shouldBe = RiftHttpDataWithContent(RiftHttpQualifiedContentType("almhirt.common.UnspecifiedProblem", RiftChannel.Json, Map()), RiftStringBody("""{"riftdesc":"almhirt.common.UnspecifiedProblem","message":"test","severity":"Major","category":"SystemProblem","args":[]}"""))
        val prob = UnspecifiedProblem("test")
        val resp = createHttpProblemResponseData(settings)(prob, None)
        resp should equal(shouldBe)
      }
    }
    "JSON is set as the target channel and settings specifies JSON as a default channel" should {
      "create a JSON response for an 'UnspecifiedProblem" in {
        val shouldBe = RiftHttpDataWithContent(RiftHttpQualifiedContentType("almhirt.common.UnspecifiedProblem", RiftChannel.Json, Map()), RiftStringBody("""{"riftdesc":"almhirt.common.UnspecifiedProblem","message":"test","severity":"Major","category":"SystemProblem","args":[]}"""))
        val prob = UnspecifiedProblem("test")
        val resp = createHttpProblemResponseData(settings)(prob, None)
        resp should equal(shouldBe)
      }
    }
  }

  "createHttpData" when {
    "no channel is provided and settings specifies JSON as a default channel" should {
      "create a JSON response for an 'UnspecifiedProblem' and TResp set to 'UnspecifiedProblem'" in {
        val shouldBe = RiftHttpDataWithContent(RiftHttpQualifiedContentType("almhirt.common.UnspecifiedProblem", RiftChannel.Json, Map()), RiftStringBody("""{"riftdesc":"almhirt.common.UnspecifiedProblem","message":"test","severity":"Major","category":"SystemProblem","args":[]}"""))
        val prob = UnspecifiedProblem("test")
        val resp = createHttpData[UnspecifiedProblem](settings)(prob, None).forceResult
        resp should equal(shouldBe)
      }
      "create a JSON response for an 'UnspecifiedProblem' and TResp set to 'Problem'" in {
        val shouldBe = RiftHttpDataWithContent(RiftHttpQualifiedContentType("almhirt.common.UnspecifiedProblem", RiftChannel.Json, Map()), RiftStringBody("""{"riftdesc":"almhirt.common.UnspecifiedProblem","message":"test","severity":"Major","category":"SystemProblem","args":[]}"""))
        val prob = UnspecifiedProblem("test")
        val resp = createHttpData[Problem](settings)(prob, None).forceResult
        resp should equal(shouldBe)
      }
      "create a JSON response for an 'UnspecifiedProblem' and TResp set to 'AnyRef'" in {
        val shouldBe = RiftHttpDataWithContent(RiftHttpQualifiedContentType("almhirt.common.UnspecifiedProblem", RiftChannel.Json, Map()), RiftStringBody("""{"riftdesc":"almhirt.common.UnspecifiedProblem","message":"test","severity":"Major","category":"SystemProblem","args":[]}"""))
        val prob = UnspecifiedProblem("test")
        val resp = createHttpData[AnyRef](settings)(prob, None).forceResult
        resp should equal(shouldBe)
      }
      "create a JSON response for an 'UnspecifiedProblem' and TResp is not set" in {
        val shouldBe = RiftHttpDataWithContent(RiftHttpQualifiedContentType("almhirt.common.UnspecifiedProblem", RiftChannel.Json, Map()), RiftStringBody("""{"riftdesc":"almhirt.common.UnspecifiedProblem","message":"test","severity":"Major","category":"SystemProblem","args":[]}"""))
        val prob = UnspecifiedProblem("test")
        val resp = createHttpData(settings)(prob, None).forceResult
        resp should equal(shouldBe)
      }
      "create a JSON response for 'PrimitiveTypes'  and TResp set to 'PrimitiveTypes'" in {
        val shouldBe = RiftHttpDataWithContent(RiftHttpQualifiedContentType("riftwarp.PrimitiveTypes", RiftChannel.Json, Map()), RiftStringBody("""{"riftdesc":"riftwarp.PrimitiveTypes","str":"I am Pete","bool":true,"byte":127,"int":-237823,"long":-278234263,"bigInt":"265876257682376587365863876528756875682765252520577305007209857025728132213242","float":1.3674999475479126,"double":1.3672322350005,"bigDec":"23761247614876823746.23846749182408","dateTime":"2013-01-23T06:23:14.421+01:00","uuid":"63d0847f-718d-4d00-8267-09d809477589"}"""))
        val resp = createHttpData[PrimitiveTypes](settings)(testdataPrim, None).forceResult
        resp should equal(shouldBe)
      }
      "create a JSON response for 'PrimitiveTypes'  and TResp set to 'AnyRef'" in {
        val shouldBe = RiftHttpDataWithContent(RiftHttpQualifiedContentType("riftwarp.PrimitiveTypes", RiftChannel.Json, Map()), RiftStringBody("""{"riftdesc":"riftwarp.PrimitiveTypes","str":"I am Pete","bool":true,"byte":127,"int":-237823,"long":-278234263,"bigInt":"265876257682376587365863876528756875682765252520577305007209857025728132213242","float":1.3674999475479126,"double":1.3672322350005,"bigDec":"23761247614876823746.23846749182408","dateTime":"2013-01-23T06:23:14.421+01:00","uuid":"63d0847f-718d-4d00-8267-09d809477589"}"""))
        val resp = createHttpData[PrimitiveTypes](settings)(testdataPrim, None).forceResult
        resp should equal(shouldBe)
      }
      "create a JSON response for 'PrimitiveTypes'  and TResp is not set" in {
        val shouldBe = RiftHttpDataWithContent(RiftHttpQualifiedContentType("riftwarp.PrimitiveTypes", RiftChannel.Json, Map()), RiftStringBody("""{"riftdesc":"riftwarp.PrimitiveTypes","str":"I am Pete","bool":true,"byte":127,"int":-237823,"long":-278234263,"bigInt":"265876257682376587365863876528756875682765252520577305007209857025728132213242","float":1.3674999475479126,"double":1.3672322350005,"bigDec":"23761247614876823746.23846749182408","dateTime":"2013-01-23T06:23:14.421+01:00","uuid":"63d0847f-718d-4d00-8267-09d809477589"}"""))
        val resp = createHttpData(settings)(testdataPrim, None).forceResult
        resp should equal(shouldBe)
      }
    }
    "JSON is set as the target channel and settings specifies JSON as a default channel" should {
      "create a JSON response for an 'UnspecifiedProblem' and TResp set to 'UnspecifiedProblem'" in {
        val shouldBe = RiftHttpDataWithContent(RiftHttpQualifiedContentType("almhirt.common.UnspecifiedProblem", RiftChannel.Json, Map()), RiftStringBody("""{"riftdesc":"almhirt.common.UnspecifiedProblem","message":"test","severity":"Major","category":"SystemProblem","args":[]}"""))
        val prob = UnspecifiedProblem("test")
        val resp = createHttpData[UnspecifiedProblem](settings)(prob, Some(RiftChannel.Json)).forceResult
        resp should equal(shouldBe)
      }
      "create a JSON response for an 'UnspecifiedProblem' and TResp set to 'Problem'" in {
        val shouldBe = RiftHttpDataWithContent(RiftHttpQualifiedContentType("almhirt.common.UnspecifiedProblem", RiftChannel.Json, Map()), RiftStringBody("""{"riftdesc":"almhirt.common.UnspecifiedProblem","message":"test","severity":"Major","category":"SystemProblem","args":[]}"""))
        val prob = UnspecifiedProblem("test")
        val resp = createHttpData[Problem](settings)(prob, Some(RiftChannel.Json)).forceResult
        resp should equal(shouldBe)
      }
      "create a JSON response for an 'UnspecifiedProblem' and TResp set to 'AnyRef'" in {
        val shouldBe = RiftHttpDataWithContent(RiftHttpQualifiedContentType("almhirt.common.UnspecifiedProblem", RiftChannel.Json, Map()), RiftStringBody("""{"riftdesc":"almhirt.common.UnspecifiedProblem","message":"test","severity":"Major","category":"SystemProblem","args":[]}"""))
        val prob = UnspecifiedProblem("test")
        val resp = createHttpData[AnyRef](settings)(prob, Some(RiftChannel.Json)).forceResult
        resp should equal(shouldBe)
      }
      "create a JSON response for an 'UnspecifiedProblem' and TResp is not set" in {
        val shouldBe = RiftHttpDataWithContent(RiftHttpQualifiedContentType("almhirt.common.UnspecifiedProblem", RiftChannel.Json, Map()), RiftStringBody("""{"riftdesc":"almhirt.common.UnspecifiedProblem","message":"test","severity":"Major","category":"SystemProblem","args":[]}"""))
        val prob = UnspecifiedProblem("test")
        val resp = createHttpData(settings)(prob, Some(RiftChannel.Json)).forceResult
        resp should equal(shouldBe)
      }
      "create a JSON response for 'PrimitiveTypes'  and TResp set to 'PrimitiveTypes'" in {
        val shouldBe = RiftHttpDataWithContent(RiftHttpQualifiedContentType("riftwarp.PrimitiveTypes", RiftChannel.Json, Map()), RiftStringBody("""{"riftdesc":"riftwarp.PrimitiveTypes","str":"I am Pete","bool":true,"byte":127,"int":-237823,"long":-278234263,"bigInt":"265876257682376587365863876528756875682765252520577305007209857025728132213242","float":1.3674999475479126,"double":1.3672322350005,"bigDec":"23761247614876823746.23846749182408","dateTime":"2013-01-23T06:23:14.421+01:00","uuid":"63d0847f-718d-4d00-8267-09d809477589"}"""))
        val resp = createHttpData[PrimitiveTypes](settings)(testdataPrim, Some(RiftChannel.Json)).forceResult
        resp should equal(shouldBe)
      }
      "create a JSON response for 'PrimitiveTypes'  and TResp set to 'AnyRef'" in {
        val shouldBe = RiftHttpDataWithContent(RiftHttpQualifiedContentType("riftwarp.PrimitiveTypes", RiftChannel.Json, Map()), RiftStringBody("""{"riftdesc":"riftwarp.PrimitiveTypes","str":"I am Pete","bool":true,"byte":127,"int":-237823,"long":-278234263,"bigInt":"265876257682376587365863876528756875682765252520577305007209857025728132213242","float":1.3674999475479126,"double":1.3672322350005,"bigDec":"23761247614876823746.23846749182408","dateTime":"2013-01-23T06:23:14.421+01:00","uuid":"63d0847f-718d-4d00-8267-09d809477589"}"""))
        val resp = createHttpData[PrimitiveTypes](settings)(testdataPrim, Some(RiftChannel.Json)).forceResult
        resp should equal(shouldBe)
      }
      "create a JSON response for 'PrimitiveTypes'  and TResp is not set" in {
        val shouldBe = RiftHttpDataWithContent(RiftHttpQualifiedContentType("riftwarp.PrimitiveTypes", RiftChannel.Json, Map()), RiftStringBody("""{"riftdesc":"riftwarp.PrimitiveTypes","str":"I am Pete","bool":true,"byte":127,"int":-237823,"long":-278234263,"bigInt":"265876257682376587365863876528756875682765252520577305007209857025728132213242","float":1.3674999475479126,"double":1.3672322350005,"bigDec":"23761247614876823746.23846749182408","dateTime":"2013-01-23T06:23:14.421+01:00","uuid":"63d0847f-718d-4d00-8267-09d809477589"}"""))
        val resp = createHttpData(settings)(testdataPrim, Some(RiftChannel.Json)).forceResult
        resp should equal(shouldBe)
      }
    }
  }

  "The respond function transforming to JSON" should {
    "return an OK-Response when the computations returns a Success(Some(x))" in {
      val response = respond(settings)(Http_200_OK, RiftChannel.Json)(() => Some(testdataPrim).success)
      response should equal(createPrimitiveTypesResponseDataJson(Http_200_OK))
    }
    "return an OK-Response with no content when computations returns a Success(None)" in {
      val response = respond(settings)(Http_200_OK, RiftChannel.Json)(() => None.success)
      response should equal(RiftHttpResponse(Http_200_OK, RiftHttpDataWithoutContent))
    }
    "return a 500-Response with no content when the computation fails" in {
      val response = respond(settings)(Http_200_OK, RiftChannel.Json)(() => UnspecifiedProblem("Failed computation").failure)
      response should equal(RiftHttpResponse(Http_500_Internal_Server_Error, RiftHttpDataWithContent(RiftHttpQualifiedContentType("almhirt.common.UnspecifiedProblem", RiftChannel.Json, Map()), RiftStringBody("""{"riftdesc":"almhirt.common.UnspecifiedProblem","message":"Failed computation","severity":"Major","category":"SystemProblem","args":[]}"""))))
    }
  }

  "withRequestData transforming to 'PrimitiveTypes'" when {
    "getting a JSON-Request of 'PrimitiveVectorMAs'" should {
      "return a JSON-OK response when the request is qualified" in {
        val response = withRequestData[PrimitiveVectorMAs](settings, primitiveVectorRequestDataQualifiedWithDescriptorInBody, pva =>
          respond(settings)(Http_200_OK, RiftChannel.Json)(() => Some(testdataPrim).success))
        response should equal(createPrimitiveTypesResponseDataJson(Http_200_OK))
      }
    }
  }

}