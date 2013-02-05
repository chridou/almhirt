package riftwarp

import scalaz.std._
import scalaz.syntax.validation._
import almhirt.common._
import almhirt.almvalidation.kit._

/**
 * A tag used for looking up a Decomposer or Recomposer.
 */
sealed class RiftDescriptor private (val identifier: String, val version: Option[Int]) extends Equals {
  private lazy val hash = {
    val prime = 41
    prime * (prime + identifier.hashCode) + version.hashCode
  }

  def unqualifiedName: String = identifier.split('.').last

  def canEqual(other: Any) = {
    other.isInstanceOf[RiftDescriptor]
  }

  override def equals(other: Any) = {
    other match {
      case that: RiftDescriptor => that.canEqual(RiftDescriptor.this) && identifier == that.identifier && version == that.version
      case _ => false
    }
  }

  override def hashCode() = hash

  override def toString() = toString(";")
  def toString(versionDelim: String) = {
    option.cata(version)(v => "%s%sv%d".format(identifier, versionDelim, v), identifier)
  }
}

object RiftDescriptor {
  val defaultKey = "riftdesc"

  def apply(name: String, version: Option[Int]): RiftDescriptor = new RiftDescriptor(name, version)
  def apply(name: String): RiftDescriptor = new RiftDescriptor(name, None)
  def apply(name: String, version: Int): RiftDescriptor = new RiftDescriptor(name, Some(version))
  def apply(clazz: Class[_]): RiftDescriptor = new RiftDescriptor(clazz.getName(), None)
  def apply(clazz: Class[_], version: Int): RiftDescriptor = new RiftDescriptor(clazz.getName(), Some(version))

  def unapply(td: RiftDescriptor): Option[String] = Some(td.identifier)
    
  
  def parse(toParse: String): AlmValidation[RiftDescriptor] = parse(toParse, ";")
  def parse(toParse: String, versionDelim: String): AlmValidation[RiftDescriptor] = {
    val parts = toParse.split(versionDelim)
    parts match {
      case Array(name) =>
        RiftDescriptor(name, None).success
      case Array(name, version) =>
        val v = version.drop(1)
        parseIntAlm(v).withIdentifierOnFailure("version").map(v => RiftDescriptor(name, Some(v)))
      case _ =>
        ParsingProblem("Not a valid RiftDescriptor format. The provided delimeter for name and version was '%s'".format(versionDelim)).withInput(toParse).failure
    }
  }

}