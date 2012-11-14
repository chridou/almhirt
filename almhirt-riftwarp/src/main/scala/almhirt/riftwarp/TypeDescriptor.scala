package almhirt.riftwarp

import scalaz.std._
import scalaz.syntax.validation._
import almhirt.common._
import almhirt.almvalidation.funs

class TypeDescriptor private (val name: String, val version: Option[Int]) extends Equals {
  private lazy val hash = {
    val prime = 41
    prime * (prime + name.hashCode) + version.hashCode
  }

  def unqualifiedName: String = name.split('.').last

  def canEqual(other: Any) = {
    other.isInstanceOf[almhirt.riftwarp.TypeDescriptor]
  }

  override def equals(other: Any) = {
    other match {
      case that: almhirt.riftwarp.TypeDescriptor => that.canEqual(TypeDescriptor.this) && name == that.name && version == that.version
      case _ => false
    }
  }

  override def hashCode() = hash

  override def toString() = toString(";")
  def toString(versionDelim: String) = {
    option.cata(version)(v => "%s%sv%d".format(name, versionDelim, v), name)
  }

}

object TypeDescriptor {
  def apply(name: String): TypeDescriptor = new TypeDescriptor(name, None)
  def apply(name: String, version: Int): TypeDescriptor = new TypeDescriptor(name, Some(version))
  def apply(clazz: Class[_]): TypeDescriptor = new TypeDescriptor(clazz.getName(), None)
  def apply(clazz: Class[_], version: Int): TypeDescriptor = new TypeDescriptor(clazz.getName(), Some(version))

  def parse(toParse: String): AlmValidation[TypeDescriptor] = parse(toParse, ";")
  def parse(toParse: String, versionDelim: String): AlmValidation[TypeDescriptor] = {
    val parts = toParse.split(versionDelim)
    parts match {
      case Array(name) =>
        TypeDescriptor(name).success
      case Array(name, version) =>
        val v = version.drop(1)
        almhirt.almvalidation.funs.parseIntAlm(v, "version").map(TypeDescriptor(name, _))
      case _ =>
        ParsingProblem("Not a valid type descriptor format. The provided delimeter for name and version was '%s'".format(versionDelim), Some(toParse)).failure
    }
  }

}