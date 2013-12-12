package almhirt.httpx.spray

import org.scalatest._
import org.scalatest.matchers.MustMatchers
import almhirt.httpx.spray.marshalling._
import almhirt.http.AlmhirtMediaTypeVendorProvider
import almhirt.http.HasCommonAlmMediaTypesProviders
import almhirt.http.VendorBasedCommonAlmMediaTypesProviders

class CommonContentTypeProvidersTest extends FunSuite with MustMatchers {
  test("CommonContentTypeProvidersFromMediaTypes must provide a boolean content type provider") {
     val provider = new CommonContentTypeProvidersFromMediaTypes with HasCommonAlmMediaTypesProviders with VendorBasedCommonAlmMediaTypesProviders {
      override val vendorProvider = AlmhirtMediaTypeVendorProvider
    }
     
    provider.booleanContentTypeProvider.marshallingContentTypes must not be('empty)
  }

  test("DelegatingCommonContentTypeProvidersFromMediaTypes must provide a boolean content type provider") {
    val mediaTypesProvider = new HasCommonAlmMediaTypesProviders with VendorBasedCommonAlmMediaTypesProviders  {
      override val vendorProvider = AlmhirtMediaTypeVendorProvider
    }
    
    val provider = new DelegatingCommonContentTypeProvidersFromMediaTypes {
     override val hasCommonAlmMediaTypesProviders = mediaTypesProvider
    }

    provider.booleanContentTypeProvider.marshallingContentTypes must not be('empty)
  }
  
}