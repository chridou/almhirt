package almhirt.http

trait MediaTypeVendorProvider {
  def vendor: MediaTypeVendorPart
}

object MediaTypeVendorProvider {
  def apply(theVendor: MediaTypeVendorPart): MediaTypeVendorProvider = new MediaTypeVendorProvider { val vendor = theVendor }
  def apply(theVendor: String): MediaTypeVendorProvider = MediaTypeVendorProvider(theVendor)
  def apply(): MediaTypeVendorProvider = MediaTypeVendorProvider(NoVendor)
}

object AlmhirtMediaTypeVendorProvider extends MediaTypeVendorProvider { val vendor = SpecificVendor("almhirt") }