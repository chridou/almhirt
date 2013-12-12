package almhirt.httpx.spray

import org.scalatest._
import org.scalatest.matchers.MustMatchers
import almhirt.http.AlmhirtMediaTypeVendorProvider
import almhirt.http.HasCommonAlmMediaTypesProviders
import almhirt.http.VendorBasedCommonAlmMediaTypesProviders
import almhirt.http.DelegatingCommonAlmMediaTypesProviders

class CommonMediaTypesProvidersTests extends FunSuite with MustMatchers {

  test("The providers instance must supply a boolean media types provider") {
    val provider = new HasCommonAlmMediaTypesProviders with VendorBasedCommonAlmMediaTypesProviders {
      override val vendorProvider = AlmhirtMediaTypeVendorProvider
    }
    val booleanMediaTypesProvider = provider.booleanAlmMediaTypesProvider
    booleanMediaTypesProvider.marshallableMediaTypes must not be ('empty)
  }

  test("The delegating providers must supply a int media types provider") {
    val innrerProvider = new HasCommonAlmMediaTypesProviders with VendorBasedCommonAlmMediaTypesProviders {
      override val vendorProvider = AlmhirtMediaTypeVendorProvider
    }
    val delagatingProvider = new HasCommonAlmMediaTypesProviders with DelegatingCommonAlmMediaTypesProviders {
      val commmonAlmMediaTypesProviders = innrerProvider
    }

    val booleanMediaTypesProvider = delagatingProvider.intAlmMediaTypesProvider
    booleanMediaTypesProvider.marshallableMediaTypes must not be ('empty)
  }

}