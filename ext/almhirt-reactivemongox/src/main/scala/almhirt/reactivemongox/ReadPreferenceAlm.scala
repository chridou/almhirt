package almhirt.reactivemongox

import scalaz.syntax.validation._
import scalaz.Validation.FlatMap._
import almhirt.common._
import almhirt.configuration._
import reactivemongo.api.ReadPreference
import com.typesafe.config.Config

sealed trait ReadPreferenceAlm

object ReadPreferenceAlm {
  case object PrimaryOnly extends ReadPreferenceAlm {
    override def toString = s"PrimaryOnly"
  }
  final case class PrimaryPreferred() extends ReadPreferenceAlm {
    override def toString = s"PrimaryPreferred"
  }
  final case class SecondaryPreferred() extends ReadPreferenceAlm {
    override def toString = s"SecondaryPreferred"
  }
  final case class SecondaryOnly() extends ReadPreferenceAlm {
    override def toString = s"SecondaryOnly"
  }
  final case class Nearest() extends ReadPreferenceAlm {
    override def toString = s"Nearest"
  }

  val Default = SecondaryPreferred()

  def toReactiveMongoReadPreference(what: ReadPreferenceAlm): ReadPreference = {
    what match {
      case PrimaryOnly          ⇒ ReadPreference.Primary
      case PrimaryPreferred()   ⇒ ReadPreference.PrimaryPreferred(None)
      case SecondaryPreferred() ⇒ ReadPreference.SecondaryPreferred(None)
      case SecondaryOnly()      ⇒ ReadPreference.Secondary(None)
      case Nearest()            ⇒ ReadPreference.Nearest(None)
    }
  }

  def fromString(str: String): AlmValidation[ReadPreferenceAlm] = {
    str match {
      case "primary-only"        ⇒ PrimaryOnly.success
      case "primary-preferred"   ⇒ PrimaryPreferred().success
      case "secondary-preferred" ⇒ SecondaryPreferred().success
      case "secondary-only"      ⇒ SecondaryOnly().success
      case "nearest"             ⇒ Nearest().success
      case x                     ⇒ ParsingProblem(s""""$x" is not a valid read preference. Allowed are: primary-only, primary-preferred, secondary-preferred, secondary-only, nearest.""").failure
    }
  }

  def fromConfig(config: Config): AlmValidation[ReadPreferenceAlm] = {
    for {
      mode ← config.v[String]("mode")
      res ← fromString(mode).leftMap { p ⇒ ConfigurationProblem(s"Could not read the read preference from config object @{$config.path}", cause = Some(p)) }
    } yield res
  }

}