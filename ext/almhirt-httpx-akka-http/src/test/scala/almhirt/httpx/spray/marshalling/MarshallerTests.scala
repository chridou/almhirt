//package almhirt.httpx.akkahttp.marshalling
//
//import org.scalatest._
//import scalaz._, Scalaz._
//import almhirt.common._
//import almhirt.http.HasCommonAlmMediaTypesProviders
//import almhirt.http.VendorBasedCommonAlmMediaTypesProviders
//import almhirt.http.AlmhirtMediaTypeVendorProvider
//import almhirt.http.HttpSerializer
//import almhirt.common.Event
//import almhirt.serialization._
//import almhirt.http.AlmHttpBody
//import almhirt.http.BinaryBody
//import scala.concurrent.ExecutionContext.Implicits.global
//import almhirt.http.AlmMediaType
//
//class MarshallerTests extends FunSuite with Matchers {
//  val contentTypeProviders: HasCommonContentTypeProviders = new HasCommonAlmMediaTypesProviders with CommonContentTypeProvidersFromMediaTypes with VendorBasedCommonAlmMediaTypesProviders {
//    override val vendorProvider = AlmhirtMediaTypeVendorProvider
//  }
//
//  import contentTypeProviders._
//
//  val binaryBodySerializer = new HttpSerializer[Int] {
//    def serialize(what: Int, mediaType: AlmMediaType)(implicit: AlmValidation[AlmHttpBody] =
//     BinaryBody(Array.empty).success
//
//    def deserialize(what: AlmHttpBody, options: Map[String, Any]): AlmValidation[Int] =
//      ???
//  }
//
//  val marshaller = ContentTypeBoundMarshallerFactory.create[Int].marshaller(binaryBodySerializer)
//
//  test("The marshaller must marshal an Int to msgpack(binary)") {
//    val res = marshaller(1)
//  }
//}