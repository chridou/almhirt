package riftwarp

import scalaz.std._
import scalaz.syntax.validation._
import almhirt.common._
import almhirt.almvalidation.funs._

sealed class TypeDescriptor private (val identifier: String, val version: Option[Int]) extends Equals {
  private lazy val hash = {
    val prime = 41
    prime * (prime + identifier.hashCode) + version.hashCode
  }

  def unqualifiedName: String = identifier.split('.').last

  def canEqual(other: Any) = {
    other.isInstanceOf[TypeDescriptor]
  }

  override def equals(other: Any) = {
    other match {
      case that: TypeDescriptor => that.canEqual(TypeDescriptor.this) && identifier == that.identifier && version == that.version
      case _ => false
    }
  }

  override def hashCode() = hash

  override def toString() = toString(";")
  def toString(versionDelim: String) = {
    option.cata(version)(v => "%s%sv%d".format(identifier, versionDelim, v), identifier)
  }
}

object TypeDescriptor {
  val defaultKey = "riftwarptd"

  def apply(name: String, version: Option[Int]): TypeDescriptor = new TypeDescriptor(name, version)
  def apply(name: String): TypeDescriptor = new TypeDescriptor(name, None)
  def apply(name: String, version: Int): TypeDescriptor = new TypeDescriptor(name, Some(version))
  def apply(clazz: Class[_]): TypeDescriptor = new TypeDescriptor(clazz.getName(), None)
  def apply(clazz: Class[_], version: Int): TypeDescriptor = new TypeDescriptor(clazz.getName(), Some(version))

  def unapply(td: TypeDescriptor): Option[String] = Some(td.identifier)
    
  
  def parse(toParse: String): AlmValidation[TypeDescriptor] = parse(toParse, ";")
  def parse(toParse: String, versionDelim: String): AlmValidation[TypeDescriptor] = {
    val parts = toParse.split(versionDelim)
    parts match {
      case Array(name) =>
        TypeDescriptor(name, None).success
      case Array(name, version) =>
        val v = version.drop(1)
        parseIntAlm(v, "version").map(v => TypeDescriptor(name, Some(v)))
      case _ =>
        ParsingProblem("Not a valid type descriptor format. The provided delimeter for name and version was '%s'".format(versionDelim)).withInput(toParse).failure
    }
  }

}