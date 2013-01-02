package almhirt.parts

import almhirt.common._
import almhirt.environment._

trait CommandExecutorFactory {
  def createCommandExecutor(almhirt: Almhirt): AlmValidation[CommandExecutor]
}