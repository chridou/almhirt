package almhirt.eventlog.anorm

import almhirt.environment.configuration._
import com.typesafe.config.Config

case class DbTemplate(driverName: String, ddlScriptName: Option[String])

object DbTemplate {
  private val templates = Map(
    "h2-json" -> DbTemplate("org.h2.Driver", Some("/conf/h2jsonddl.sql")),
    "postgres-json" -> DbTemplate("org.postgresql.Driver", Some("/conf/postgresjsonddl.sql")))

  def tryGetTemplate(config: Config): Option[DbTemplate] =
    ConfigHelper.tryGetString(config)(ConfigPaths.eventlog + ".dbtemplate").flatMap(templates.get(_))
}