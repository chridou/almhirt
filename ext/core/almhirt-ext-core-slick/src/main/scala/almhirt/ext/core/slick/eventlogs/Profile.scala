package almhirt.ext.core.slick.eventlogs

import scala.slick.driver.ExtendedProfile
import scala.slick.driver.H2Driver
import almhirt.common._
import almhirt.almvalidation.kit._

trait Profile {
  val profile: ExtendedProfile
}

final case class ProfileSettings(driver: String, slickDriver: ExtendedProfile)

object Profiles {
  val profiles: Map[String, ProfileSettings] =
    Map(
      "h2" ->  ProfileSettings("org.h2.Driver", H2Driver) )
      
  def createTextDomainEventLogAccess(profile: String, eventlogtablename: String, blobtablename: String): AlmValidation[TextDomainEventLogDataAccess] =
    for {
      profile <- (profiles.lift >? profile)
    } yield TextDomainEventLogDataAccess()
    
}