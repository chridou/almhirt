package almhirt.ext.core.slick.shared

import scala.slick.driver.ExtendedProfile
import scala.slick.driver.H2Driver
import almhirt.common._
import almhirt.almvalidation.kit._
import almhirt.almvalidation.constraints._
import scala.slick.session.Database
import almhirt.ext.core.slick.eventlogs._
import almhirt.ext.core.slick.snapshots.TextSnapshotsDataAccess
import scala.slick.driver._

trait Profile {
  val profile: ExtendedProfile
  def getDb: Unit => Database
}

final case class ProfileSettings(driver: String, slickDriver: ExtendedProfile)

object Profiles {
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

  def createTextEventLogAccess(aProfile: String, anEventlogtablename: String, createDataBase: String => Database)(implicit hasExecutionContext: HasExecutionContext): AlmValidation[TextEventLogDataAccess] =
    for {
      profileName <- aProfile.toLowerCase().notEmptyOrWhitespace
      eventlogtablename <- anEventlogtablename.notEmptyOrWhitespace
      profile <- (profiles.lift >! profileName)
    } yield new TextEventLogDataAccess(eventlogtablename, Unit => createDataBase(profile.driver), profile.slickDriver)

  def createTextDomainEventLogAccess(aProfile: String, anEventlogtablename: String, createDataBase: String => Database)(implicit hasExecutionContext: HasExecutionContext): AlmValidation[TextDomainEventLogDataAccess] =
    for {
      profileName <- aProfile.toLowerCase().notEmptyOrWhitespace
      eventlogtablename <- anEventlogtablename.notEmptyOrWhitespace
      profile <- (profiles.lift >! profileName)
    } yield new TextDomainEventLogDataAccess(eventlogtablename, Unit => createDataBase(profile.driver), profile.slickDriver)

  def createTextSnapshotsAccess(aProfile: String, aSnapshotsTablename: String, createDataBase: String => Database)(implicit hasExecutionContext: HasExecutionContext): AlmValidation[TextSnapshotsDataAccess] =
    for {
      profileName <- aProfile.toLowerCase().notEmptyOrWhitespace
      snapshotstablename <- aSnapshotsTablename.notEmptyOrWhitespace
      profile <- (profiles.lift >! profileName)
    } yield new TextSnapshotsDataAccess(snapshotstablename, Unit => createDataBase(profile.driver), profile.slickDriver)
    
}