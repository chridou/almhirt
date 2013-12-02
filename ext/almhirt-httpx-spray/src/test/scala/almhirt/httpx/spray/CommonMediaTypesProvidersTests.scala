package almhirt.httpx.spray

import org.scalatest._
import org.scalatest.matchers.MustMatchers
import almhirt.http.AlmhirtMediaTypeVendorProvider

class CommonMediaTypesProvidersTests extends FunSuite with MustMatchers {

  test("The providers instance must supply a boolean media types provider") {
    val provider = new VendorBasedCommonMediaTypesProviders {
      override val vendorProvider = AlmhirtMediaTypeVendorProvider
    }
    val booleanMediaTypesProvider = provider.booleanMediaTypesProvider
    booleanMediaTypesProvider.marshallableMediaTypes must not be ('empty)
  }

  test("The delegating providers must supply a int media types provider") {
    val innrerProvider = new VendorBasedCommonMediaTypesProviders {
      override val vendorProvider = AlmhirtMediaTypeVendorProvider
    }
    val delagatingProvider = new DelegatingCommonMediaTypesProviders {
      val commmonMediaTypesProviders = innrerProvider
    }

    val booleanMediaTypesProvider = delagatingProvider.intMediaTypesProvider
    booleanMediaTypesProvider.marshallableMediaTypes must not be ('empty)
  }

}