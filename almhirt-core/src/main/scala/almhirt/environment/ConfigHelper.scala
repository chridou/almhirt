package almhirt.environment

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

  def tryGetSubConfig(config: Config)(path: String): Option[Config] =
    config.getConfig(path) match {
      case null => None
      case found => Some(found)
    }

  def getSubConfig(config: Config)(path: String): AlmValidation[Config] =
    option.cata(tryGetSubConfig(config)(path))(_.success, KeyNotFoundProblem("SubConfig not found: %s".format(path), args = Map("key" -> path)).failure)

  def tryGetDispatcherName(config: Config)(path: String): Option[String] =
    tryGetSubConfig(config)(path).flatMap(tryGetString(_)("dispatchername"))

  def lookUpDispatcher(system: ActorSystem)(name: Option[String]): akka.dispatch.MessageDispatcher = {
    name match {
      case Some(n) => system.dispatchers.lookup(n)
      case None => system.dispatchers.defaultGlobalDispatcher
    }
  }
}

object ConfigPaths {
  val messagehub = "almhirt.messagehub"
  val messagechannels = "almhirt.messagechannels"
  val eventlog = "almhirt.eventlog"
  val commandexecutor = "almhirt.commandexecutor"
  val repositories = "almhirt.repositories"
  val futures = "almhirt.futures"
}
