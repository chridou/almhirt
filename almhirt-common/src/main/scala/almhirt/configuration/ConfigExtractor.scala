package almhirt.configuration

import scala.reflect.ClassTag
import scalaz._, Scalaz._
import scalaz.Validation.FlatMap._
import almhirt.common._
import com.typesafe.config.Config
import com.typesafe.config.ConfigException

trait ConfigExtractor[T] {
  def getValue(config: Config, path: String): AlmValidation[T]
  def tryGetValue(config: Config, path: String): AlmValidation[Option[T]]
}

object ConfigHelper {
  def getFromConfigSafely[T](path: String, f: String ⇒ T)(implicit tag: ClassTag[T]): AlmValidation[T] =
    try {
      f(path).success
    } catch {
      case exn: ConfigException.Missing ⇒
        ConfigurationProblem(s"""No value found at "$path".""", args = Map("key" → path), cause = Some(exn)).failure
      case exn: ConfigException.WrongType ⇒
        ConfigurationProblem(s"""Value at "$path" can not be converted to a "${tag.runtimeClass.getName()}".""", args = Map("key" → path), cause = Some(exn)).failure
    }
  def tryGetFromConfigSafely[T](path: String, f: String ⇒ T)(implicit tag: ClassTag[T]): AlmValidation[Option[T]] =
    try {
      Some(f(path)).success
    } catch {
      case exn: ConfigException.Missing ⇒
        None.success
      case exn: ConfigException.WrongType ⇒
        ConfigurationProblem(s"""Value at "$path" can not be converted to a "${tag.runtimeClass.getName()}".""", args = Map("key" → path), cause = Some(exn)).failure
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

trait ConfigLongExtractor extends ConfigExtractor[Long] {
  def getValue(config: Config, path: String): AlmValidation[Long] =
    ConfigHelper.getFromConfigSafely(path, config.getLong)
  def tryGetValue(config: Config, path: String): AlmValidation[Option[Long]] =
    ConfigHelper.tryGetFromConfigSafely(path, config.getLong)
}

trait ConfigDoubleExtractor extends ConfigExtractor[Double] {
  def getValue(config: Config, path: String): AlmValidation[Double] =
    ConfigHelper.getFromConfigSafely(path, config.getDouble)
  def tryGetValue(config: Config, path: String): AlmValidation[Option[Double]] =
    ConfigHelper.tryGetFromConfigSafely(path, config.getDouble)
}

trait ConfigFiniteDurationMsExtractor extends ConfigExtractor[scala.concurrent.duration.FiniteDuration] {
  def getValue(config: Config, path: String): AlmValidation[scala.concurrent.duration.FiniteDuration] =
    ConfigHelper.getFromConfigSafely(path, str ⇒ config.getDuration(str, scala.concurrent.duration.MILLISECONDS)).map(ms ⇒ scala.concurrent.duration.FiniteDuration.apply(ms, scala.concurrent.duration.MILLISECONDS))
  def tryGetValue(config: Config, path: String): AlmValidation[Option[scala.concurrent.duration.FiniteDuration]] =
    ConfigHelper.tryGetFromConfigSafely(path, str ⇒ config.getDuration(str, scala.concurrent.duration.MILLISECONDS)).map(msOpt ⇒ msOpt.map(scala.concurrent.duration.FiniteDuration.apply(_, scala.concurrent.duration.MILLISECONDS)))
}

trait ConfigJavaDurationMsExtractor extends ConfigExtractor[java.time.Duration] {
  def getValue(config: Config, path: String): AlmValidation[java.time.Duration] =
    ConfigHelper.getFromConfigSafely(path, str ⇒ config.getDuration(str, scala.concurrent.duration.MILLISECONDS)).map(ms ⇒ java.time.Duration.ofMillis((ms)))
  def tryGetValue(config: Config, path: String): AlmValidation[Option[java.time.Duration]] =
    ConfigHelper.tryGetFromConfigSafely(path, str ⇒ config.getDuration(str, scala.concurrent.duration.MILLISECONDS)).map(msOpt ⇒ msOpt.map(ms ⇒ java.time.Duration.ofMillis(ms)))
}

trait ConfigUuidExtractor extends ConfigExtractor[java.util.UUID] {
  import almhirt.almvalidation.kit._
  def getValue(config: Config, path: String): AlmValidation[java.util.UUID] =
    ConfigHelper.getFromConfigSafely(path, config.getString).flatMap(str ⇒ str.toUuidAlm)
  def tryGetValue(config: Config, path: String): AlmValidation[Option[java.util.UUID]] =
    ConfigHelper.tryGetFromConfigSafely(path, config.getString).flatMap(strOpt ⇒ strOpt.map(str ⇒ str.toUuidAlm).validationOut)
}

trait ConfigUriExtractor extends ConfigExtractor[java.net.URI] {
  import almhirt.almvalidation.kit._
  def getValue(config: Config, path: String): AlmValidation[java.net.URI] =
    ConfigHelper.getFromConfigSafely(path, config.getString).flatMap(str ⇒ str.toUriAlm)
  def tryGetValue(config: Config, path: String): AlmValidation[Option[java.net.URI]] =
    ConfigHelper.tryGetFromConfigSafely(path, config.getString).flatMap(strOpt ⇒ strOpt.map(str ⇒ str.toUriAlm).validationOut)
}

trait ConfigStringListExtractor extends ConfigExtractor[List[String]] {
  import scala.collection.JavaConversions._
  def getValue(config: Config, path: String): AlmValidation[List[String]] =
    ConfigHelper.getFromConfigSafely(path, config.getStringList).map(l ⇒ l.toList)
  def tryGetValue(config: Config, path: String): AlmValidation[Option[List[String]]] =
    ConfigHelper.tryGetFromConfigSafely(path, config.getStringList).map(lOpt ⇒ lOpt.map(l ⇒ l.toList))
}

trait ConfigBooleanListExtractor extends ConfigExtractor[List[Boolean]] {
  import scala.collection.JavaConversions._
  def getValue(config: Config, path: String): AlmValidation[List[Boolean]] =
    ConfigHelper.getFromConfigSafely(path, config.getBooleanList).map(l ⇒ l.map(_.booleanValue).toList)
  def tryGetValue(config: Config, path: String): AlmValidation[Option[List[Boolean]]] =
    ConfigHelper.tryGetFromConfigSafely(path, config.getBooleanList).map(lOpt ⇒ lOpt.map(l ⇒ l.map(_.booleanValue).toList))
}

trait ConfigIntListExtractor extends ConfigExtractor[List[Int]] {
  import scala.collection.JavaConversions._
  def getValue(config: Config, path: String): AlmValidation[List[Int]] =
    ConfigHelper.getFromConfigSafely(path, config.getIntList).map(l ⇒ l.map(_.toInt).toList)
  def tryGetValue(config: Config, path: String): AlmValidation[Option[List[Int]]] =
    ConfigHelper.tryGetFromConfigSafely(path, config.getIntList).map(lOpt ⇒ lOpt.map(l ⇒ l.map(_.toInt).toList))
}

trait ConfigLongListExtractor extends ConfigExtractor[List[Long]] {
  import scala.collection.JavaConversions._
  def getValue(config: Config, path: String): AlmValidation[List[Long]] =
    ConfigHelper.getFromConfigSafely(path, config.getLongList).map(l ⇒ l.map(_.toLong).toList)
  def tryGetValue(config: Config, path: String): AlmValidation[Option[List[Long]]] =
    ConfigHelper.tryGetFromConfigSafely(path, config.getLongList).map(lOpt ⇒ lOpt.map(l ⇒ l.map(_.toLong).toList))
}

trait ConfigDoubleListExtractor extends ConfigExtractor[List[Double]] {
  import scala.collection.JavaConversions._
  def getValue(config: Config, path: String): AlmValidation[List[Double]] =
    ConfigHelper.getFromConfigSafely(path, config.getDoubleList).map(l ⇒ l.map(_.toDouble).toList)
  def tryGetValue(config: Config, path: String): AlmValidation[Option[List[Double]]] =
    ConfigHelper.tryGetFromConfigSafely(path, config.getDoubleList).map(lOpt ⇒ lOpt.map(l ⇒ l.map(_.toDouble).toList))
}

trait ConfigFiniteDurationListMsExtractor extends ConfigExtractor[List[scala.concurrent.duration.FiniteDuration]] {
  import scala.collection.JavaConversions._
  def getValue(config: Config, path: String): AlmValidation[List[scala.concurrent.duration.FiniteDuration]] =
    ConfigHelper.getFromConfigSafely(path, str ⇒ config.getDurationList(str, scala.concurrent.duration.MILLISECONDS)).map(l ⇒ l.map(ms ⇒ scala.concurrent.duration.FiniteDuration.apply(ms, scala.concurrent.duration.MILLISECONDS)).toList)
  def tryGetValue(config: Config, path: String): AlmValidation[Option[List[scala.concurrent.duration.FiniteDuration]]] =
    ConfigHelper.tryGetFromConfigSafely(path, str ⇒ config.getDurationList(str, scala.concurrent.duration.MILLISECONDS)).map(lOpt ⇒ lOpt.map(l ⇒ l.map(ms ⇒ scala.concurrent.duration.FiniteDuration.apply(ms, scala.concurrent.duration.MILLISECONDS)).toList))
}

trait ConfigJavaDurationListMsExtractor extends ConfigExtractor[List[java.time.Duration]] {
  import scala.collection.JavaConversions._
  def getValue(config: Config, path: String): AlmValidation[List[java.time.Duration]] =
    ConfigHelper.getFromConfigSafely(path, str ⇒ config.getDurationList(str, scala.concurrent.duration.MILLISECONDS)).map(l ⇒ l.map(ms ⇒ java.time.Duration.ofMillis(ms)).toList)
  def tryGetValue(config: Config, path: String): AlmValidation[Option[List[java.time.Duration]]] =
    ConfigHelper.tryGetFromConfigSafely(path, str ⇒ config.getDurationList(str, scala.concurrent.duration.MILLISECONDS)).map(lOpt ⇒ lOpt.map(l ⇒ l.map(ms ⇒ java.time.Duration.ofMillis(ms)).toList))
}

trait ConfigUuidListExtractor extends ConfigExtractor[List[java.util.UUID]] {
  import almhirt.almvalidation.kit._
  import scala.collection.JavaConversions._
  def getValue(config: Config, path: String): AlmValidation[List[java.util.UUID]] =
    ConfigHelper.getFromConfigSafely(path, config.getStringList).flatMap(l ⇒
      l.toList.map(str ⇒
        str.toUuidAlm.toAgg).sequence[AlmValidationAP, java.util.UUID])
  def tryGetValue(config: Config, path: String): AlmValidation[Option[List[java.util.UUID]]] =
    ConfigHelper.tryGetFromConfigSafely(path, config.getStringList).flatMap(strLOpt ⇒ strLOpt.map(l ⇒ l.toList.map(str ⇒
      str.toUuidAlm.toAgg).sequence[AlmValidationAP, java.util.UUID]).validationOut)
}

trait ConfigUriListExtractor extends ConfigExtractor[List[java.net.URI]] {
  import almhirt.almvalidation.kit._
  import scala.collection.JavaConversions._
  def getValue(config: Config, path: String): AlmValidation[List[java.net.URI]] =
    ConfigHelper.getFromConfigSafely(path, config.getStringList).flatMap(l ⇒
      l.toList.map(str ⇒
        str.toUriAlm.toAgg).sequence[AlmValidationAP, java.net.URI])
  def tryGetValue(config: Config, path: String): AlmValidation[Option[List[java.net.URI]]] =
    ConfigHelper.tryGetFromConfigSafely(path, config.getStringList).flatMap(strLOpt ⇒ strLOpt.map(l ⇒ l.toList.map(str ⇒
      str.toUriAlm.toAgg).sequence[AlmValidationAP, java.net.URI]).validationOut)
}

trait ConfigConfigExtractor extends ConfigExtractor[Config] {
  def getValue(config: Config, path: String): AlmValidation[Config] =
    ConfigHelper.getFromConfigSafely(path, config.getConfig)
  def tryGetValue(config: Config, path: String): AlmValidation[Option[Config]] =
    ConfigHelper.tryGetFromConfigSafely(path, config.getConfig)
}

trait ConfigConfigListExtractor extends ConfigExtractor[List[Config]] {
  import almhirt.almvalidation.kit._
  import scala.collection.JavaConversions._
  def getValue(config: Config, path: String): AlmValidation[List[Config]] =
    for {
      items ← ConfigHelper.getFromConfigSafely(path, config.getConfigList)
      typed ← items.map(c ⇒ inTryCatch { c.asInstanceOf[Config] }.toAgg).toList.sequence
    } yield typed

  def tryGetValue(config: Config, path: String): AlmValidation[Option[List[Config]]] =
    unsafe {
      ConfigHelper.tryGetFromConfigSafely(path, config.getConfigList).map(lOpt ⇒
        lOpt.map(l ⇒ l.toList.map(cfg ⇒
          cfg.asInstanceOf[Config])))
    }
}

trait ConfigJavaPropertiesExtractor extends ConfigExtractor[java.util.Properties] {
  import java.util.Properties
  import collection.JavaConversions._

  def getValue(config: Config, path: String): AlmValidation[Properties] =
    ConfigHelper.getFromConfigSafely(path, config.getConfig).map(subConfig ⇒
      subConfig.entrySet()
        .map(x ⇒ (x.getKey(), x.getValue().unwrapped().toString()))
        .foldLeft(new Properties)((acc, x) ⇒ { acc.setProperty(x._1, x._2); acc }))
  def tryGetValue(config: Config, path: String): AlmValidation[Option[Properties]] =
    ConfigHelper.tryGetFromConfigSafely(path, config.getConfig).map(_.map((subConfig ⇒
      subConfig.entrySet()
        .map(x ⇒ (x.getKey(), x.getValue().unwrapped().toString()))
        .foldLeft(new Properties)((acc, x) ⇒ { acc.setProperty(x._1, x._2); acc }))))
}
