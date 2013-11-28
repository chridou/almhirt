package almhirt.httpx.spray

trait MediaTypeVendorProvider {
  def vendor: String
}

object MediaTypeVendorProvider {
  def apply(theVendor: String): MediaTypeVendorProvider = new MediaTypeVendorProvider { val vendor = theVendor }
}

object AlmhirtMediaTypeVendorProvider extends MediaTypeVendorProvider { val vendor = "almhirt" }
