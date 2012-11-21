package almhirt.eventlog.anorm

import almhirt.environment.configuration._
import com.typesafe.config.Config

case class DbTemplate(driverName: String, ddlScriptName: Option[String])

object DbTemplate {
  private val templates = Map("h2" -> DbTemplate("org.h2.Driver", Some("/conf/h2ddl.sql")))
  
  def tryGetTemplate(config: Config): Option[DbTemplate] = 
    ConfigHelper.tryGetString(config)(ConfigPaths.eventlog+".dbtemplate").flatMap(templates.get(_))
}