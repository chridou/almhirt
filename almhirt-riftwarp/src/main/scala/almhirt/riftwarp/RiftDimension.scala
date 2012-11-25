package almhirt.riftwarp

trait RiftUntypedDimension {
  def ident: String
  def canEqual(other: Any) = {
    other.isInstanceOf[almhirt.riftwarp.RiftUntypedDimension]
  }

  override def equals(other: Any) = {
    other match {
      case that: almhirt.riftwarp.RiftUntypedDimension => that.canEqual(RiftUntypedDimension.this) && that.ident == this.ident
      case _ => false
    }
  }

  override def hashCode() = {
    val prime = 41
    prime + ident.hashCode()
  }
}

trait RiftDimension[T] extends RiftUntypedDimension

class DimensionString() extends RiftDimension[String] { val ident = "StringDimension" }
object DimensionString {
  private val theInstance = new DimensionString()
  def apply() = theInstance
}
class DimensionCord() extends RiftDimension[scalaz.Cord] { val ident = "CordDimension" }
object DimensionCord {
  private val theInstance = new DimensionCord()
  def apply() = theInstance
}
class DimensionBinary() extends RiftDimension[Array[Byte]] { val ident = "BinaryDimension" }
object DimensionBinary {
  private val theInstance = new DimensionBinary()
  def apply() = theInstance
}
class DimensionRawMap() extends RiftDimension[Map[String, Any]] { val ident = "RawMapDimension" }
object DimensionRawMap {
  private val theInstance = new DimensionRawMap()
  def apply() = theInstance
}
class DimensionStdLibJsonMap() extends RiftDimension[Map[String, Any]] { val ident = "StdLibJsonMapDimension" }
object DimensionStdLibJsonMap {
  private val theInstance = new DimensionStdLibJsonMap()
  def apply() = theInstance
}
class DimensionListAny() extends RiftDimension[List[Any]] { val ident = "ListAnyDimension" }
object DimensionListAny {
  private val theInstance = new DimensionListAny()
  def apply() = theInstance
}

object RiftDimension {
  val dimString = DimensionStdLibJsonMap()
  val dimCord = DimensionCord()
  val dimBinary = DimensionBinary()
  val dimRawMap = DimensionRawMap()
  val dimStdLibJsonMap = DimensionStdLibJsonMap()
  val dimListAny = DimensionListAny()
}

trait RawManifestation

trait Manifestation[T] extends RawManifestation {
  def dimension: RiftDimension[_]
  def appearance: T
}

case class ManifestationString(appearance: String) extends Manifestation[String] {
  val dimension = RiftDimension.dimString
}
case class ManifestationCord(appearance: scalaz.Cord) extends Manifestation[scalaz.Cord] {
  val dimension = RiftDimension.dimCord
}
case class ManifestationBinary(appearance: Array[Byte]) extends Manifestation[Array[Byte]] {
  val dimension = RiftDimension.dimBinary
}
case class ManifestationRawMap(appearance: Map[String, Any]) extends Manifestation[Map[String, Any]] {
  val dimension = RiftDimension.dimRawMap
}
case class ManifestationStdLibJsonMap(appearance: Map[String, Any]) extends Manifestation[Map[String, Any]] {
  val dimension = RiftDimension.dimStdLibJsonMap
}
case class ManifestationListAny(appearance: List[Any]) extends Manifestation[List[Any]] {
  val dimension = RiftDimension.dimListAny
}

