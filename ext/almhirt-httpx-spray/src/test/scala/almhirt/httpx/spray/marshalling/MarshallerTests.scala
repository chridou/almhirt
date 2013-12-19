package almhirt.httpx.spray.marshalling

import org.scalatest._
import scalaz._, Scalaz._
import almhirt.common._
import almhirt.http.HasCommonAlmMediaTypesProviders
import almhirt.http.VendorBasedCommonAlmMediaTypesProviders
import almhirt.http.AlmhirtMediaTypeVendorProvider
import almhirt.serialization.WireSerializer
import almhirt.common.Event
import almhirt.serialization._

//class MarshallerTests extends FunSuite with Matchers {
//  val contentTypeProviders: HasCommonContentTypeProviders = new HasCommonAlmMediaTypesProviders with CommonContentTypeProvidersFromMediaTypes with VendorBasedCommonAlmMediaTypesProviders {
//    override val vendorProvider = AlmhirtMediaTypeVendorProvider
//  }
//
//  import contentTypeProviders._
//
//  val binaryWireSerializer = new WireSerializer[Int] {
//    def serialize(channel: String)(what: Int, options: Map[String, Any]): AlmValidation[(WireRepresentation, Option[String])] =
//      (BinaryWire(Array.empty), None).success
//
//    def deserialize(channel: String)(what: WireRepresentation, options: Map[String, Any]): AlmValidation[Int] =
//      ???
//  }
//
//  val marshaller = ContentTypeBoundMarshallerFactory.create[Int].marshaller(binaryWireSerializer)
//
//  test("The marshaller must marshal an Int to msgpack(binary)") {
//    val res = marshaller(1, null)
//  }
//}