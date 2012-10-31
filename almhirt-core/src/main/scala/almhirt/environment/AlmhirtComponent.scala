package almhirt.environment

import almhirt._

trait AlmhirtComponent {
  implicit def almhirt: Almhirt
}