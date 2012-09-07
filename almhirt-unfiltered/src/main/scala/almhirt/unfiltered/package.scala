package almhirt.ext

package object unfiltered {
  //@Deprecated(message = "Use 'all' instead", since = "0.0.6")
  object unfilteredall extends UnfilteredResponseFunctions with ToUnfilteredResponeOps
  object all extends UnfilteredResponseFunctions with ToUnfilteredResponeOps
}