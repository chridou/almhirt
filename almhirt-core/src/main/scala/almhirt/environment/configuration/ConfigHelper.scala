package almhirt.environment.configuration

import scala.concurrent.duration.FiniteDuration
import scala.concurrent.duration._
import scalaz.std._
import scalaz.syntax.validation._
import akka.actor._
import com.typesafe.config.Config
import almhirt.common._

object ConfigHelper {
  def getString(config: Config)(path: String): AlmValidation[String] =
    almhirt.almvalidation.funs.computeSafely {
      config.getString(path) match {
        case null => KeyNotFoundProblem("String not found: %s".format(path), args = Map("key" -> path)).failure
        case "" => KeyNotFoundProblem("String not found: %s".format(path), args = Map("key" -> path)).failure
        case str =>
          if (str.toLowerCase().equals("none"))
            KeyNotFoundProblem("String not found: %s".format(path), args = Map("key" -> path)).failure
          else
            str.success
      }
    }

  def getStringOrDefault(default: => String)(config: Config)(path: String): String = {
    getString(config)(path).toOption.getOrElse(default)
  }

  def getBoolean(config: Config)(path: String): AlmValidation[Boolean] =
    almhirt.almvalidation.funs.inTryCatch(config.getBoolean(path))

  def isBooleanSet(config: Config)(path: String): Boolean =
    getBoolean(config)(path).fold(_ => false, x => x)

  def getInt(config: Config)(path: String): AlmValidation[Int] =
    almhirt.almvalidation.funs.inTryCatch { config.getInt(path) }

  def getIntOrDefault(default: => Int)(config: Config)(path: String): Int = {
    getInt(config)(path).toOption.getOrElse(default)
  }
  
  def getMilliseconds(config: Config)(path: String): AlmValidation[FiniteDuration] = {
    try {
      config.getMilliseconds(path) match {
        case null => KeyNotFoundProblem("Entry for duration not found: %s".format(path), args = Map("key" -> path)).failure
        case nanos => FiniteDuration(nanos, "ms").success
      }
    } catch {
      case exn: Exception => UnspecifiedProblem("Not a duration on path '%s'".format(path), cause = Some(exn), args = Map("key" -> path)).failure
    }
  }

  def getSubConfig(config: Config)(path: String): AlmValidation[Config] =
    almhirt.almvalidation.funs.computeSafely {
      config.getConfig(path) match {
        case null => KeyNotFoundProblem("SubConfig not found: %s".format(path), args = Map("key" -> path)).failure
        case found => found.success
      }
    }

  def getDispatcherNameFromComponentConfigPath(rootConfig: Config)(componentConfigPath: String): AlmValidation[String] =
    getSubConfig(rootConfig)(componentConfigPath).flatMap(getDispatcherNameFromComponentConfig(_))

  def getDispatcherNameFromComponentConfig(componentConfig: Config): AlmValidation[String] =
    getString(componentConfig)(ConfigItems.dispatchername)

  object domainEventLog {
    def getConfig(config: Config): AlmValidation[Config] = getSubConfig(config)(ConfigPaths.domaineventlog)
    def getActorName(domaineventlogConfig: Config): String = ConfigHelper.getStringOrDefault("DomainEventLog")(domaineventlogConfig)(ConfigItems.actorName)
    def factoryName(domaineventlogConfig: Config): AlmValidation[String] = shared.getFactoryNameFromComponentConfig(domaineventlogConfig)
  }

  object eventLog {
    def getConfig(config: Config): AlmValidation[Config] = getSubConfig(config)(ConfigPaths.eventlog)
    def getActorName(eventlogConfig: Config): String = ConfigHelper.getStringOrDefault("EventLog")(eventlogConfig)(ConfigItems.actorName)
    def factoryName(eventlogConfig: Config): AlmValidation[String] = shared.getFactoryNameFromComponentConfig(eventlogConfig)
  }

  object operationState {
    def getConfig(config: Config): AlmValidation[Config] = getSubConfig(config)(ConfigPaths.operationState)
    def getActorName(operationStateConfig: Config): String = ConfigHelper.getStringOrDefault("OperationStateTracker")(operationStateConfig)(ConfigItems.actorName)
    def factoryName(operationStateConfig: Config): AlmValidation[String] = shared.getFactoryNameFromComponentConfig(operationStateConfig)
  }

  object commandEndpoint {
    def getConfig(config: Config): AlmValidation[Config] = getSubConfig(config)(ConfigPaths.commandEndpoint)
    def getActorName(endpointConfig: Config): String = ConfigHelper.getStringOrDefault("CommandEndpoint")(endpointConfig)(ConfigItems.actorName)
    def factoryName(endpointConfig: Config): AlmValidation[String] = shared.getFactoryNameFromComponentConfig(endpointConfig)
  }

  object commandDispatcher {
    def getConfig(config: Config): AlmValidation[Config] = getSubConfig(config)(ConfigPaths.commandDispatcher)
    def factoryName(dispatcherConfig: Config): AlmValidation[String] = shared.getFactoryNameFromComponentConfig(dispatcherConfig)
  }

  object http {
    def getConfig(config: Config): AlmValidation[Config] = getSubConfig(config)(ConfigPaths.http)
    def port(httpConfig: Config): AlmValidation[Int] = getInt(httpConfig)("port")
    def maxContentLength(httpConfig: Config): AlmValidation[Int] = getInt(httpConfig)("maxContentLength")
    def maxSyncCommandDuration(httpConfig: Config): AlmValidation[FiniteDuration] = getMilliseconds(httpConfig)("max-sync-command-duration")
  }

  object problems {
    def getConfig(config: Config): AlmValidation[Config] = getSubConfig(config)(ConfigPaths.problems)
    def minSeverity(problemsConfig: Config): AlmValidation[Severity] = 
      getString(problemsConfig)("minSeverity").flatMap(Severity.fromString(_))
    def getActorName(problemsConfig: Config): String = ConfigHelper.getStringOrDefault("ProblemLogger")(problemsConfig)(ConfigItems.actorName)
  }
  
  object shared {
    def getFactoryNameFromComponentConfig(componentConfig: Config): AlmValidation[String] = ConfigHelper.getString(componentConfig)(ConfigItems.factory)
  }

  def lookUpDispatcher(system: ActorSystem)(name: Option[String]): akka.dispatch.MessageDispatcher = {
    name match {
      case Some(n) => system.dispatchers.lookup(n)
      case None => system.dispatchers.defaultGlobalDispatcher
    }
  }
}


