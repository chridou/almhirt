package almhirt.context

import scala.language.postfixOps
import scala.concurrent.duration._
import akka.actor._
import almhirt.common._
import akka.testkit._
import org.scalatest._

class AlmhirtContextTests(_system: ActorSystem)  extends TestKit(_system) with FunSuiteLike with Matchers with BeforeAndAfterAll {
  def this() = this(ActorSystem("AlmhirtContextTests", almhirt.TestConfigs.logInfoConfig))

  private val currentTestId = new java.util.concurrent.atomic.AtomicInteger(1)
  def nextTestId = currentTestId.getAndIncrement()

  test("AlmhirtContext should be constructable") {
    val ctxF = AlmhirtContext(system, Some(s"ctx-${nextTestId}"), None)
    val ctx = ctxF.awaitResultOrEscalate(5 seconds)
    ctx.stop()
  }

  
  override def beforeAll() {

  }

  override def afterAll() {
    TestKit.shutdownActorSystem(system)
  }

}