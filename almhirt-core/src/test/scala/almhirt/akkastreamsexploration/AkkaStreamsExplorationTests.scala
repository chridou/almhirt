package almhirt.akkastreamsexploration

import org.scalatest._
import akka.testkit._
import akka.actor.ActorSystem
import akka.stream.scaladsl._
import akka.stream.OverflowStrategy
import org.reactivestreams.{ Subscriber, Publisher }

class AkkaStreamsExplorationTests(_system: ActorSystem) extends TestKit(_system) with FunSuiteLike with Matchers with BeforeAndAfterAll {
  def this() = this(ActorSystem("AkkaStreamsExplorationTests"))
  implicit def implicitFlowMaterializer = akka.stream.ActorMaterializer()(_system)

  //  test("Connect via faked flow throws IllegalArgumentException") {
  //    val faked = Flow(Sink.ignore(), Source(List(1, 2, 3, 4, 5)))
  //    val exn = intercept[java.lang.IllegalArgumentException] {
  //      val runnableFlow = Source(List(11, 22, 33, 44, 55)).via(faked).to(BlackholeSink)
  //    }
  //    assert(true)
  //  }

  override def beforeAll() {

  }

  override def afterAll() {
    TestKit.shutdownActorSystem(system)
  }

}