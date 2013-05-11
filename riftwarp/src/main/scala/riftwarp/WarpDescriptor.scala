package riftwarp

import scalaz.std._
import scalaz.syntax.validation._
import almhirt.common._
import almhirt.almvalidation.kit._

/**
 * A tag used for looking up a Decomposer or WarpUnpacker.
 */
sealed class WarpDescriptor private (val identifier: String, val version: Option[Int]) extends Equals {
  private lazy val hash = {{
      val prime = 41
      prime * (prime + identifier.hashCode) + version.hashCode
    }
  }

  def unqualifiedName: String = identifier.split('.').last

  def canEqual(other: Any) = {
    other.isInstanceOf[WarpDescriptor]
  }

  override def equals(other: Any) = {
    other match {
      case that: WarpDescriptor => that.canEqual(WarpDescriptor.this) && identifier == that.identifier && version == that.version
      case _ => false
    }
  }

  override def hashCode() = hash

  override def toString() = toString(";")
  def toString(versionDelim: String) = {
    option.cata(version)(v => s"WarpDescriptor($identifier;$v)", s"WarpDescriptor($identifier;no version)")
  }

  def toParsableString(versionDelim: String = ";") =
    version match {
      case Some(v) => s"$identifier$versionDelim$v"
      case None => s"$identifier"
    }
}

object WarpDescriptor {
  val defaultKey = "warpdesc"

  def apply(name: String, version: Option[Int]): WarpDescriptor = new WarpDescriptor(name, version)
  def apply(name: String): WarpDescriptor = new WarpDescriptor(name, None)
  def apply(name: String, version: Int): WarpDescriptor = new WarpDescriptor(name, Some(version))
  def apply(clazz: Class[_]) = new WarpDescriptor(clazz.getName(), None)
  def apply(clazz: Class[_], version: Int): WarpDescriptor = new WarpDescriptor(clazz.getName(), Some(version))

  def unapply(td: WarpDescriptor): Option[String] = Some(td.identifier)

  def parse(toParse: String): AlmValidation[WarpDescriptor] = parse(toParse, ";")
  def parse(toParse: String, versionDelim: String): AlmValidation[WarpDescriptor] = {
    val parts = toParse.split(versionDelim)
    parts match {
      case Array(name) =>
        WarpDescriptor(name, None).success
      case Array(name, version) =>
        val v = version.drop(1)
        parseIntAlm(v).withIdentifierOnFailure("version").map(v => WarpDescriptor(name, Some(v)))
      case _ =>
        ParsingProblem("Not a valid WarpDescriptor format. The provided delimeter for name and version was '%s'".format(versionDelim)).withInput(toParse).failure
    }
  }
  
  import language.implicitConversions

  implicit def string2WarpDescriptor(descriptor: String): WarpDescriptor = WarpDescriptor(descriptor)
  implicit def class2WarpDescriptor(clazz: Class[_]): WarpDescriptor = WarpDescriptor(clazz.getName())

}