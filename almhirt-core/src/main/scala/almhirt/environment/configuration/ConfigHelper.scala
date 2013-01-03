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
    try {
      config.getString(path) match {
        case null => None
        case "" => None
        case str =>
          if (str.toLowerCase().equals("none"))
            None
          else
            Some(str)
      }
    } catch {
      case exn: Throwable => None
    }
  }

  def getString(config: Config)(path: String): AlmValidation[String] = {
    option.cata(tryGetString(config)(path))(_.success, KeyNotFoundProblem("String not found: %s".format(path), args = Map("key" -> path)).failure)
  }

  def getStringOrDefault(default: => String)(config: Config)(path: String): String = {
    tryGetString(config)(path).getOrElse(default)
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

  def ifSubConfigExists(config: Config)(path: String)(action: Config => Unit) {
    option.cata(tryGetSubConfig(config)(path))(
      subConfig => action(subConfig),
      ())
  }

  def tryGetDispatcherNameFromRootConfig(config: Config)(path: String): Option[String] =
    tryGetSubConfig(config)(path).flatMap(tryGetString(_)("dispatchername"))

  object eventLog {
    def tryGetConfig(config: Config): Option[Config] = tryGetSubConfig(config)(ConfigPaths.eventlog)
    def getConfig(config: Config): AlmValidation[Config] = getSubConfig(config)(ConfigPaths.eventlog)
    def getActorName(eventlogConfig: Config): String = ConfigHelper.getStringOrDefault("EventLog")(eventlogConfig)(ConfigItems.actorName)
  }

  object operationState {
    def tryGetConfig(config: Config): Option[Config] = tryGetSubConfig(config)(ConfigPaths.operationState)
    def getConfig(config: Config): AlmValidation[Config] = getSubConfig(config)(ConfigPaths.operationState)
    def getActorName(operationStateConfig: Config): String = ConfigHelper.getStringOrDefault("OperationStateTracker")(operationStateConfig)(ConfigItems.actorName)
  }

  object shared {
    def tryGetFactoryName(config: Config): Option[String] = ConfigHelper.tryGetString(config)(ConfigItems.factory)
    def getFactoryName(config: Config): AlmValidation[String] = ConfigHelper.getString(config)(ConfigItems.factory)
  }

  def lookUpDispatcher(system: ActorSystem)(name: Option[String]): akka.dispatch.MessageDispatcher = {
    name match {
      case Some(n) => system.dispatchers.lookup(n)
      case None => system.dispatchers.defaultGlobalDispatcher
    }
  }
}


