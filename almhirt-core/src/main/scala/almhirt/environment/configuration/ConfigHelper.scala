package almhirt.environment.configuration

import scala.concurrent.duration.FiniteDuration
import scala.concurrent.duration._
import scalaz.std._
import scalaz.syntax.validation._
import akka.actor._
import com.typesafe.config.Config
import almhirt.common._

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

  def getBoolean(config: Config)(path: String): AlmValidation[Boolean] =
    almhirt.almvalidation.funs.inTryCatch(config.getBoolean(path))

  def isBooleanSet(config: Config)(path: String): Boolean =
    getBoolean(config)(path).fold(_ => false, x => x)

  def getDuration(config: Config)(path: String): AlmValidation[FiniteDuration] = {
    try {
      config.getNanoseconds(path) match {
        case null => KeyNotFoundProblem("Entry for duration not found: %s".format(path), args = Map("key" -> path)).failure
        case nanos => FiniteDuration(nanos, NANOSECONDS).success
      }
    } catch {
      case exn: Throwable => UnspecifiedProblem("Not a duration on path '%s'".format(path), cause = Some(CauseIsThrowable(exn)), args = Map("key" -> path)).failure
    }
  }

  def tryGetDuration(config: Config)(path: String): Option[FiniteDuration] =
    (getDuration(config)(path)).fold(_ => None, succ => Some(succ))

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
    getSubConfig(config)(path).flatMap(getString(_)("factory"))

  def getActorName(config: Config)(path: String): AlmValidation[String] =
    getSubConfig(config)(path).flatMap(getString(_)("name"))

  def getActorNameOrDefault(default: String)(config: Config)(path: String): String =
    getActorName(config: Config)(path: String).getOrElse(default)

  def getEventLogActorName(config: Config): String = ConfigHelper.getActorNameOrDefault("EventLog")(config)(ConfigPaths.eventlog)
    
  def lookUpDispatcher(system: ActorSystem)(name: Option[String]): akka.dispatch.MessageDispatcher = {
    name match {
      case Some(n) => system.dispatchers.lookup(n)
      case None => system.dispatchers.defaultGlobalDispatcher
    }
  }
}


