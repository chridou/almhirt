package almhirt.parts

import almhirt.common._
import almhirt.environment._
import almhirt.core.Almhirt

trait CommandExecutorFactory {
  def createCommandExecutor(almhirt: Almhirt): AlmValidation[CommandExecutor]
}