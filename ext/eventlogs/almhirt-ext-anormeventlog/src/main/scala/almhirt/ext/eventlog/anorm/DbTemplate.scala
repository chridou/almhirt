package almhirt.ext.eventlog.anorm

import scalaz.std._
import scalaz.syntax.validation._
import almhirt.common._
import almhirt.environment.configuration._
import com.typesafe.config.Config

case class DbTemplate(driverName: String, ddlScriptName: Option[String])

object DbTemplate {
  private val templates = Map(
    "h2-json" -> DbTemplate("org.h2.Driver", Some("/conf/h2jsonddl.sql")),
    "postgres-json" -> DbTemplate("org.postgresql.Driver", Some("/conf/postgresjsonddl.sql")))

  def getTemplate(config: Config): AlmValidation[DbTemplate] =
    ConfigHelper.getString(config)("dbtemplate").flatMap(templateName =>
      option.cata(templates.get(templateName))(
        template => template.success,
        KeyNotFoundProblem(s"No db template found for $templateName").failure))
}