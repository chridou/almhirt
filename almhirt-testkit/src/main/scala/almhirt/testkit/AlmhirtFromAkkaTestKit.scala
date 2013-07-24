package almhirt.testkit

import almhirt.core._
import almhirt.almvalidation.kit._

trait AlmhirtFromAkkaTestKitWithoutConfiguration { self: AlmhirtTestKit =>
  private val almhirtAndCloseHandleF = Almhirt.notFromConfig(self.system).awaitResult(scala.concurrent.duration.Duration(5, "s"))
  private val almhirtAndCloseHandle = almhirtAndCloseHandleF.resultOrEscalate

  override val theAlmhirt = almhirtAndCloseHandle._1
}