package almhirt.environment

import almhirt.core.Almhirt

trait AlmhirtComponent {
  implicit def almhirt: Almhirt
}