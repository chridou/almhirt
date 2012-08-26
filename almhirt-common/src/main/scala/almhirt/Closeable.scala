package almhirt

/** A Resource that has to be closed. Usually a resource which is used for a few operations*/
trait Closeable {
 def close()
}