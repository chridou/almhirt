package riftwarp.http

import org.specs2.mutable._
import scalaz.syntax.validation._
import almhirt.common._
import riftwarp._
import riftwarp.impl.UnsafeChannelRegistry

class RiftWarpHttpFunsSpecs extends Specification {
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

  """createHttpDataFromRequest""" should {
    """return Success(RiftHttpNoContentData) for a None content type and an empty body""" in {
      createHttpDataFromRequest(() => None, bodyType => RiftStringBody("").success)(settings.contentTypeOps) must beEqualTo(scalaz.Success(RiftHttpNoContentData))
    }
    """return Success(RiftHttpNoContentData) for a None content type and a non empty body""" in {
      createHttpDataFromRequest(() => None, bodyType => RiftStringBody("a").success)(settings.contentTypeOps) must beEqualTo(scalaz.Success(RiftHttpNoContentData))
    }
    """fail for a Some("") as the content type and an empty body""" in {
      createHttpDataFromRequest(() => Some(""), bodyType => RiftStringBody("").success)(settings.contentTypeOps).isFailure
    }

    """return Success(RiftHttpDataWithContent(RiftHttpChannelContentType(RiftChannel.Json), RiftStringBody("")))) for a Some("text/json") content type and an empty body""" in {
      createHttpDataFromRequest(() => Some("text/json"), createBody)(settings.contentTypeOps) must beEqualTo(scalaz.Success(RiftHttpDataWithContent(RiftHttpChannelContentType(RiftChannel.Json), RiftStringBody(""))))
    }

    """fail for a Some("wrong") content type and an empty body""" in {
      createHttpDataFromRequest(() => Some("wrong"), createBody)(settings.contentTypeOps).isFailure
    }

    """return Success(RiftHttpDataWithContent(RiftHttpQualifiedContentType(RiftDescriptor("xxx", 1), RiftChannel.Json), RiftStringBody("")))) for a Some("application/vnd.testprefix.xxx+json;version=1") content type and an empty body""" in {
      createHttpDataFromRequest(() => Some("application/vnd.testprefix.xxx+json;version=1"), createBody)(settings.contentTypeOps) must beEqualTo(scalaz.Success(RiftHttpDataWithContent(RiftHttpQualifiedContentType(RiftDescriptor("xxx", 1), RiftChannel.Json), RiftStringBody(""))))
    }

    """fail for a Some("application/vnd.testprefix.xxx+XXXson;version=1") content type and an empty body""" in {
      createHttpDataFromRequest(() => Some("application/vnd.testprefix.xxx+XXXson;version=1"), createBody)(settings.contentTypeOps).isFailure
    }
    
  }
}