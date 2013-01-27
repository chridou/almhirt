package almhirt.environment.configuration

import scala.concurrent.duration.FiniteDuration
import scala.concurrent.duration._
import scalaz.std._
import scalaz.syntax.validation._
import akka.actor._
import com.typesafe.config.Config
import almhirt.common._

object ConfigHelper {
  def getString(config: Config)(path: String): AlmValidation[String] = {
    try {
      config.getString(path) match {
        case null => KeyNotFoundProblem("String not found: %s".format(path), args = Map("key" -> path)).failure
        case "" => KeyNotFoundProblem("String not found: %s".format(path), args = Map("key" -> path)).failure
        case str =>
          if (str.toLowerCase().equals("none"))
            KeyNotFoundProblem("String not found: %s".format(path), args = Map("key" -> path)).failure
          else
            str.success
      }
    } catch {
      case exn: Throwable => ExceptionCaughtProblem("").withCause(CauseIsThrowable(exn)).failure
    }
  }

  def getStringOrDefault(default: => String)(config: Config)(path: String): String = {
    getString(config)(path).toOption.getOrElse(default)
  }

  def getBoolean(config: Config)(path: String): AlmValidation[Boolean] =
    almhirt.almvalidation.funs.inTryCatch(config.getBoolean(path))

  def isBooleanSet(config: Config)(path: String): Boolean =
    getBoolean(config)(path).fold(_ => false, x => x)

  def getMilliseconds(config: Config)(path: String): AlmValidation[FiniteDuration] = {
    try {
      config.getMilliseconds(path) match {
        case null => KeyNotFoundProblem("Entry for duration not found: %s".format(path), args = Map("key" -> path)).failure
        case nanos => FiniteDuration(nanos, "ms").success
      }
    } catch {
      case exn: Throwable => UnspecifiedProblem("Not a duration on path '%s'".format(path), cause = Some(CauseIsThrowable(exn)), args = Map("key" -> path)).failure
    }
  }

  def getSubConfig(config: Config)(path: String): AlmValidation[Config] =
    config.getConfig(path) match {
      case null => KeyNotFoundProblem("SubConfig not found: %s".format(path), args = Map("key" -> path)).failure
      case found => found.success
    }

  def getDispatcherNameFromRootConfig(config: Config)(path: String): AlmValidation[String] =
    getSubConfig(config)(path).flatMap(getString(_)("dispatchername"))

  def lookupDispatcherConfigPath(config: Config)(path: String): AlmValidation[String] =
    getDispatcherNameFromRootConfig(config)(path).map(dispatcherLookupName =>
      s"almhirt.dispatchers.$dispatcherLookupName")
      
  object eventLog {
    def getConfig(config: Config): AlmValidation[Config] = getSubConfig(config)(ConfigPaths.eventlog)
    def getActorName(eventlogConfig: Config): String = ConfigHelper.getStringOrDefault("EventLog")(eventlogConfig)(ConfigItems.actorName)
  }

  object operationState {
    def getConfig(config: Config): AlmValidation[Config] = getSubConfig(config)(ConfigPaths.operationState)
    def getActorName(operationStateConfig: Config): String = ConfigHelper.getStringOrDefault("OperationStateTracker")(operationStateConfig)(ConfigItems.actorName)
  }

  object commandEndpoint {
    def getConfig(config: Config): AlmValidation[Config] = getSubConfig(config)(ConfigPaths.commandEndpoint)
    def getActorName(endpointConfig: Config): String = ConfigHelper.getStringOrDefault("CommandEnpoint")(endpointConfig)(ConfigItems.actorName)
  }

  object commandDispatcher {
    def getConfig(config: Config): AlmValidation[Config] = getSubConfig(config)(ConfigPaths.commandDispatcher)
  }

  object shared {
    def getFactoryName(config: Config): AlmValidation[String] = ConfigHelper.getString(config)(ConfigItems.factory)
  }

  def lookUpDispatcher(system: ActorSystem)(name: Option[String]): akka.dispatch.MessageDispatcher = {
    name match {
      case Some(n) => system.dispatchers.lookup(n)
      case None => system.dispatchers.defaultGlobalDispatcher
    }
  }
}


