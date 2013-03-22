package almhirt.ext.core.slick.eventlogs

import scala.slick.driver.ExtendedProfile
import scala.slick.driver.H2Driver
import almhirt.common._
import almhirt.almvalidation.kit._
import almhirt.almvalidation.constraints._
import scala.slick.session.Database

trait Profile {
  val profile: ExtendedProfile
  def getDb: Unit => Database
}

final case class ProfileSettings(driver: String, slickDriver: ExtendedProfile)

object Profiles {
  val profiles: Map[String, ProfileSettings] =
    Map(
      "h2" -> ProfileSettings("org.h2.Driver", H2Driver))

  def createTextEventLogAccess(aProfile: String, anEventlogtablename: String, aBlobtablename: String, createDataBase: String => Database)(implicit hasExecutionContext: HasExecutionContext): AlmValidation[TextEventLogDataAccess] =
    for {
      profileName <- aProfile.toLowerCase().notEmptyOrWhitespace
      eventlogtablename <- anEventlogtablename.notEmptyOrWhitespace
      blobtablename <- aBlobtablename.notEmptyOrWhitespace
      profile <- (profiles.lift >! profileName)
    } yield new TextEventLogDataAccess(eventlogtablename, blobtablename, Unit => createDataBase(profile.driver), profile.slickDriver, hasExecutionContext)

  def createTextDomainEventLogAccess(aProfile: String, anEventlogtablename: String, aBlobtablename: String, createDataBase: String => Database)(implicit hasExecutionContext: HasExecutionContext): AlmValidation[TextDomainEventLogDataAccess] =
    for {
      profileName <- aProfile.toLowerCase().notEmptyOrWhitespace
      eventlogtablename <- anEventlogtablename.notEmptyOrWhitespace
      blobtablename <- aBlobtablename.notEmptyOrWhitespace
      profile <- (profiles.lift >! profileName)
    } yield new TextDomainEventLogDataAccess(eventlogtablename, blobtablename, Unit => createDataBase(profile.driver), profile.slickDriver, hasExecutionContext)

}