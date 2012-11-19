package almhirt.environment.configuration

import scalaz.std._
import scalaz.syntax.validation._
import akka.actor._
import com.typesafe.config.Config
import almhirt.common.AlmValidation
import almhirt.common.KeyNotFoundProblem

object ConfigHelper {
  def tryGetString(config: Config)(path: String): Option[String] = {
    config.getString(path) match {
      case null => None
      case "" => None
      case str =>
        if (str.toLowerCase().equals("none"))
          None
        else
          Some(str)
    }
  }

  def getString(config: Config)(path: String): AlmValidation[String] = {
    option.cata(tryGetString(config)(path))(_.success, KeyNotFoundProblem("String not found: %s".format(path), args = Map("key" -> path)).failure)
  }

  def tryGetSubConfig(config: Config)(path: String): Option[Config] =
    config.getConfig(path) match {
      case null => None
      case found => Some(found)
    }

  def getSubConfig(config: Config)(path: String): AlmValidation[Config] =
    option.cata(tryGetSubConfig(config)(path))(_.success, KeyNotFoundProblem("SubConfig not found: %s".format(path), args = Map("key" -> path)).failure)

  def tryGetDispatcherName(config: Config)(path: String): Option[String] =
    tryGetSubConfig(config)(path).flatMap(tryGetString(_)("dispatchername"))

  def getFactoryName(config: Config)(path: String): AlmValidation[String] =
    getSubConfig(config)(path).bind(getString(_)("factory"))
    
  def lookUpDispatcher(system: ActorSystem)(name: Option[String]): akka.dispatch.MessageDispatcher = {
    name match {
      case Some(n) => system.dispatchers.lookup(n)
      case None => system.dispatchers.defaultGlobalDispatcher
    }
  }
}


