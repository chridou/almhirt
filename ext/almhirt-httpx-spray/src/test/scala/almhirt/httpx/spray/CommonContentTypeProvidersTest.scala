package almhirt.httpx.spray

import org.scalatest._
import almhirt.httpx.spray.marshalling._
import almhirt.http.AlmhirtMediaTypeVendorProvider
import almhirt.http.HasCommonAlmMediaTypesProviders
import almhirt.http.VendorBasedCommonAlmMediaTypesProviders
import almhirt.http.AlmCharacterEncoding

class CommonContentTypeProvidersTest extends FunSuite with Matchers {
  test("CommonContentTypeProvidersFromMediaTypes should provide a boolean content type provider") {
     val provider = new CommonContentTypeProvidersFromMediaTypes with HasCommonAlmMediaTypesProviders with VendorBasedCommonAlmMediaTypesProviders {
      override val vendorProvider = AlmhirtMediaTypeVendorProvider
    }
     
    provider.booleanContentTypeProvider.marshallingContentTypes should not be('empty)
  }

  test("DelegatingCommonContentTypeProvidersFromMediaTypes should provide a boolean content type provider") {
    val mediaTypesProvider = new HasCommonAlmMediaTypesProviders with VendorBasedCommonAlmMediaTypesProviders  {
      override val vendorProvider = AlmhirtMediaTypeVendorProvider
    }
    
    val provider = new DelegatingCommonContentTypeProvidersFromMediaTypes {
     override val defaultEncoding = implicitly[AlmCharacterEncoding]
     override val hasCommonAlmMediaTypesProviders = mediaTypesProvider
    }

    provider.booleanContentTypeProvider.marshallingContentTypes should not be('empty)
  }
}