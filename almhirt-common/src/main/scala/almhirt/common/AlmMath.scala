package almhirt.common

object AlmMath {
  /** Calculates the next power of 2 from n unless n is a power of 2 */
  def nextPowerOf2(n: Int): Int = {
    var k = 1
    while (k < n) {
      k = k * 2
    }
    k
  }

  /** Calculates the next power of 2 from n unless n is a power of 2 */
  def nextPowerOf2(n: Long): Long = {
    var k = 1L
    while (k < n) {
      k = k * 2L
    }
    k
  }
}