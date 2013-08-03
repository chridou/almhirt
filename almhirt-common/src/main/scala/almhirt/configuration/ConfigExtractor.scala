package almhirt.configuration

import scala.reflect.ClassTag
import scalaz.syntax.validation._
import almhirt.common._
import com.typesafe.config.Config
import com.typesafe.config.ConfigException

trait ConfigExtractor[T] {
  def getValue(config: Config, path: String): AlmValidation[T]
  def tryGetValue(config: Config, path: String): AlmValidation[Option[T]]
}

object ConfigHelper {
  def getFromConfigSafely[T](path: String, f: String => T)(implicit tag: ClassTag[T]): AlmValidation[T] =
    try {
      f(path).success
    } catch {
      case exn: ConfigException.Missing =>
        NoSuchElementProblem(s"""No value found at "$path".""", args = Map("key" -> path)).failure
      case exn: ConfigException.WrongType =>
        BadDataProblem(s"""Value at "$path" can not be converted to a "${tag.runtimeClass.getName()}".""", args = Map("key" -> path)).failure
    }
  def tryGetFromConfigSafely[T](path: String, f: String => T)(implicit tag: ClassTag[T]): AlmValidation[Option[T]] =
    try {
      Some(f(path)).success
    } catch {
      case exn: ConfigException.Missing =>
        None.success
      case exn: ConfigException.WrongType =>
        BadDataProblem(s"""Value at "$path" can not be converted to a "${tag.runtimeClass.getName()}".""", args = Map("key" -> path)).failure
    }
}

trait ConfigStringExtractor extends ConfigExtractor[String] {
  def getValue(config: Config, path: String): AlmValidation[String] =
    ConfigHelper.getFromConfigSafely(path, config.getString)
  def tryGetValue(config: Config, path: String): AlmValidation[Option[String]] =
    ConfigHelper.tryGetFromConfigSafely(path, config.getString)
}

trait ConfigBooleanExtractor extends ConfigExtractor[Boolean] {
  def getValue(config: Config, path: String): AlmValidation[Boolean] =
    ConfigHelper.getFromConfigSafely(path, config.getBoolean)
  def tryGetValue(config: Config, path: String): AlmValidation[Option[Boolean]] =
    ConfigHelper.tryGetFromConfigSafely(path, config.getBoolean)
}

trait ConfigIntExtractor extends ConfigExtractor[Int] {
  def getValue(config: Config, path: String): AlmValidation[Int] =
    ConfigHelper.getFromConfigSafely(path, config.getInt)
  def tryGetValue(config: Config, path: String): AlmValidation[Option[Int]] =
    ConfigHelper.tryGetFromConfigSafely(path, config.getInt)
}

trait ConfigConfigExtractor extends ConfigExtractor[Config] {
  def getValue(config: Config, path: String): AlmValidation[Config] =
    ConfigHelper.getFromConfigSafely(path, config.getConfig)
  def tryGetValue(config: Config, path: String): AlmValidation[Option[Config]] =
    ConfigHelper.tryGetFromConfigSafely(path, config.getConfig)
}

trait ConfigJavaPropertiesExtractor extends ConfigExtractor[java.util.Properties] {
  import java.util.Properties
  import collection.JavaConversions._

  def getValue(config: Config, path: String): AlmValidation[Properties] =
    ConfigHelper.getFromConfigSafely(path, config.getConfig).map(subConfig =>
      subConfig.entrySet()
        .map(x => (x.getKey(), x.getValue().unwrapped().toString()))
        .foldLeft(new Properties)((acc, x) => { acc.setProperty(x._1, x._2); acc }))
  def tryGetValue(config: Config, path: String): AlmValidation[Option[Properties]] =
    ConfigHelper.tryGetFromConfigSafely(path, config.getConfig).map(_.map((subConfig =>
      subConfig.entrySet()
        .map(x => (x.getKey(), x.getValue().unwrapped().toString()))
        .foldLeft(new Properties)((acc, x) => { acc.setProperty(x._1, x._2); acc }))))
}
