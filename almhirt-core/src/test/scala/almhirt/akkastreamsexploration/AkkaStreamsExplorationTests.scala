package almhirt.akkastreamsexploration

import org.scalatest._
import akka.testkit._
import akka.stream.FlowMaterializer
import akka.actor.ActorSystem
import akka.stream.scaladsl._
import akka.stream.OverflowStrategy
import org.reactivestreams.{ Subscriber, Publisher }

class AkkaStreamsExplorationTests(_system: ActorSystem) extends TestKit(_system) with FunSuiteLike with Matchers with BeforeAndAfterAll {
  def this() = this(ActorSystem("AkkaStreamsExplorationTests"))
  implicit val mat = FlowMaterializer()

  //- Connect via faked flow *** FAILED ***
  //  java.lang.IllegalArgumentException: requirement failed: Graph must be connected
  //  at scala.Predef$.require(Predef.scala:219)
  //  at akka.stream.scaladsl.FlowGraphBuilder.checkBuildPreconditions(FlowGraph.scala:1175)
  //  at akka.stream.scaladsl.FlowGraphBuilder.build(FlowGraph.scala:1107)
  //  at akka.stream.scaladsl.FlowGraph$.apply(FlowGraph.scala:1203)
  //  at akka.stream.scaladsl.FlowGraph$.apply(FlowGraph.scala:1199)
  //  at akka.stream.scaladsl.GraphSource.to(GraphFlow.scala:153)
  //  at akka.stream.scaladsl.GraphSource.to(GraphFlow.scala:162)
  //  at almhirt.akkastreamsexploration.AkkaStreamsExplorationTests$$anonfun$1.apply$mcV$sp(AkkaStreamsExplorationTests.scala:17)
  //  at almhirt.akkastreamsexploration.AkkaStreamsExplorationTests$$anonfun$1.apply(AkkaStreamsExplorationTests.scala:14)
  //  at almhirt.akkastreamsexploration.AkkaStreamsExplorationTests$$anonfun$1.apply(AkkaStreamsExplorationTests.scala:14)  
  test("Connect via faked flow throws IllegalArgumentException") {
    val faked = Flow(BlackholeSink, Source(List(1, 2, 3, 4, 5)))
    val exn = intercept[java.lang.IllegalArgumentException] {
      val runnableFlow = Source(List(11, 22, 33, 44, 55)).via(faked).to(BlackholeSink)
    }
    assert(true)
  }

  override def beforeAll() {

  }

  override def afterAll() {
    TestKit.shutdownActorSystem(system)
  }

}