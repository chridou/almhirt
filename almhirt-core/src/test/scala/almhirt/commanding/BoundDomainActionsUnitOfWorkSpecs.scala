package almhirt.commanding

import org.scalatest._
import org.scalatest.matchers.ShouldMatchers
import scala.concurrent.duration.FiniteDuration
import almhirt.common._
import almhirt.almvalidation.kit._
import almhirt.environment.AlmhirtTestKit

class BoundDomainActionsUnitOfWorkSpecs extends FlatSpec with BeforeAndAfterAll with ShouldMatchers with AlmhirtTestKit {
  private[this] val (theAlmhirt, shutDown) = createTestAlmhirt(createDefaultBootStrapper()).forceResult
  implicit val atMost = FiniteDuration(1, "s")
  implicit val alm = theAlmhirt
 
  override def afterAll {
    shutDown.shutDown
  }

  
}