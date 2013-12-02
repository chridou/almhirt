package almhirt.http

/** This Media type corresponds to Spray's media type */
trait AlmMediaType {
	def mainType : String
    def subType : String
    def compressible: Boolean
    def binary: Boolean
    def fileExtensions: Seq[String]
}