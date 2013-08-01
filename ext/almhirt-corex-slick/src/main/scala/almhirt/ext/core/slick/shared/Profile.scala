package almhirt.ext.core.slick.shared

import scala.slick.driver.ExtendedProfile
import scala.slick.driver.H2Driver
import almhirt.common._
import almhirt.almvalidation.kit._
import almhirt.configuration._
import scala.slick.session.Database
import scala.slick.driver._
import com.typesafe.config.Config

trait Profile {
  val profile: ExtendedProfile
  def db: Database
}

final case class ProfileSettings(driver: String, slickDriver: ExtendedProfile)

object ProfileSettings {
  val profiles: Map[String, ProfileSettings] =
    Map(
      "h2" -> ProfileSettings("org.h2.Driver", H2Driver),
      "postgres" -> ProfileSettings("org.postgresql.Driver", PostgresDriver),
      "derby" -> ProfileSettings("org.apache.derby.jdbc.ClientDriver", DerbyDriver),
      "derbyembedded" -> ProfileSettings("org.apache.derby.jdbc.EmbeddedDriver", DerbyDriver),
      "mssql" -> ProfileSettings("com.microsoft.sqlserver.jdbc.SQLServerDriver", SQLServerDriver),
      "hsqldb" -> ProfileSettings("hSql.hDriver ", HsqldbDriver),
      "mysql" -> ProfileSettings("com.mysql.jdbc.Driver", MySQLDriver),
      "sqlite" -> ProfileSettings("org.sqlite.JDBC", SQLiteDriver))

  def fromConfig(config: Config): AlmValidation[ProfileSettings] =
    for {
      profileName <- config.value[String]("profile").flatMap(_.notEmptyOrWhitespace)
      profile <- (profiles.lift >! profileName)
    } yield profile

    
}