package almhirt.httpx.spray

import org.scalatest._
import org.scalatest.matchers.MustMatchers
import almhirt.httpx.spray.marshalling._
import almhirt.http.AlmhirtMediaTypeVendorProvider

class CommonContentTypeProvidersTest extends FunSuite with MustMatchers {
  test("CommonContentTypeProvidersFromMediaTypes must provide a boolean content type provider") {
     val provider = new CommonContentTypeProvidersFromMediaTypes with HasCommonMediaTypesProviders with VendorBasedCommonMediaTypesProviders {
      override val vendorProvider = AlmhirtMediaTypeVendorProvider
    }
     
    provider.booleanContentTypeProvider.marshallingContentTypes must not be('empty)
  }

  test("DelegatingCommonContentTypeProvidersFromMediaTypes must provide a boolean content type provider") {
    val mediaTypesProvider = new HasCommonMediaTypesProviders with VendorBasedCommonMediaTypesProviders  {
      override val vendorProvider = AlmhirtMediaTypeVendorProvider
    }
    
    val provider = new DelegatingCommonContentTypeProvidersFromMediaTypes {
     override val hasCommonMediaTypesProviders = mediaTypesProvider
    }

    provider.booleanContentTypeProvider.marshallingContentTypes must not be('empty)
  }
  
}