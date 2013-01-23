package almhirt.messaging

import org.scalatest._
import scala.concurrent.duration._
import scala.reflect.ClassTag
import akka.actor.ActorSystem
import almhirt.syntax.almvalidation._
import almhirt.environment._

class ActorBasedMessageHubTests extends FunSuite with BeforeAndAfterAll with AlmhirtsystemTestkit {
  implicit val system = createTestSystem()
  implicit val atMost = FiniteDuration(1, "s")
  implicit def getUUID = java.util.UUID.randomUUID()

  override def afterAll {
    system.dispose()
  }

  private class A(val propa: Int)
  private class B(propa: Int, val propb: String) extends A(propa)

  private def getHub: MessageHub = MessageHub(java.util.UUID.randomUUID().toString())

  test("A MessageHub must accept a message") {
    val hub = getHub
    hub.broadcast(Message(new B(1, "B")))
    hub.close
  }

  test("A MessageHub must be able to create a global channel") {
    val hub = getHub
    val channel = hub.createMessageChannel[AnyRef]("testChannel")
    hub.close
  }

  test("A MessageHub must be able to create a channel") {
    val hub = getHub
    val channel = hub.createMessageChannel[AnyRef]("testChannel")
    hub.close
  }
}