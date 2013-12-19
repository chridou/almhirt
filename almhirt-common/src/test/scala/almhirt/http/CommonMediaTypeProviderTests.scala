package almhirt.http

import org.scalatest._

class CommonMediaTypeProviderTests extends FunSuite with Matchers {

  test("The providers instance should supply a boolean media types provider") {
    val provider = new HasCommonAlmMediaTypesProviders with VendorBasedCommonAlmMediaTypesProviders {
      override val vendorProvider = AlmhirtMediaTypeVendorProvider
    }
    val booleanMediaTypesProvider = provider.booleanAlmMediaTypesProvider
    booleanMediaTypesProvider.marshallableMediaTypes should not be ('empty)
  }

  test("The delegating providers should supply a int media types provider") {
    val innerProvider = new HasCommonAlmMediaTypesProviders with VendorBasedCommonAlmMediaTypesProviders {
      override val vendorProvider = AlmhirtMediaTypeVendorProvider
    }
    val delagatingProvider = new HasCommonAlmMediaTypesProviders with DelegatingCommonAlmMediaTypesProviders {
      val commmonAlmMediaTypesProviders = innerProvider
    }

    val mediaTypesProvider = delagatingProvider.intAlmMediaTypesProvider
    mediaTypesProvider.marshallableMediaTypes should not be ('empty)
  }

  test("The int media types should return a msgpack media type for marshalling which is binary") {
    val innerProvider = new HasCommonAlmMediaTypesProviders with VendorBasedCommonAlmMediaTypesProviders {
      override val vendorProvider = AlmhirtMediaTypeVendorProvider
    }
    val delagatingProvider = new HasCommonAlmMediaTypesProviders with DelegatingCommonAlmMediaTypesProviders {
      val commmonAlmMediaTypesProviders = innerProvider
    }

    val mediaTypesProvider = delagatingProvider.intAlmMediaTypesProvider
    val msgPackMediaType = mediaTypesProvider.findForMarshalling("msgpack").get
    msgPackMediaType.binary should equal(true)
  }

  test("The int media types should return a msgpack media type for marshalling content 'application' which is binary") {
    val innerProvider = new HasCommonAlmMediaTypesProviders with VendorBasedCommonAlmMediaTypesProviders {
      override val vendorProvider = AlmhirtMediaTypeVendorProvider
    }
    val delagatingProvider = new HasCommonAlmMediaTypesProviders with DelegatingCommonAlmMediaTypesProviders {
      val commmonAlmMediaTypesProviders = innerProvider
    }

    val mediaTypesProvider = delagatingProvider.intAlmMediaTypesProvider
    val msgPackMediaType = mediaTypesProvider.findForMarshalling("application", "msgpack").get
    msgPackMediaType.binary should equal(true)
  }
}