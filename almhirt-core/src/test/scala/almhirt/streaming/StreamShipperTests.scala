package almhirt.streaming

import scala.language.postfixOps
import scala.concurrent.duration._
import akka.actor._
import akka.stream.scaladsl.{ Flow }
import akka.stream.{ FlowMaterializer, MaterializerSettings }
import almhirt.common._
import akka.testkit._
import org.scalatest._
import org.reactivestreams.{ Publisher }

class StreamShipperTests(_system: ActorSystem) extends TestKit(_system) with fixture.WordSpecLike with Matchers with BeforeAndAfterAll {
  def this() = this(ActorSystem("StreamShipperTests", almhirt.TestConfigs.logWarningConfig))

  implicit val executionContext = system.dispatchers.defaultGlobalDispatcher

  val nMsgBig = 150000L
  val nMsgBigPublishers = 100L
  val nMsgSome = 1000L
  val nMsgSomePublishers = 10L

  implicit val mat = FlowMaterializer(MaterializerSettings())

  "The StreamShipper" when {
    import akka.stream.actor.ActorSubscriber
    "accessed via consumers" should {
      "dispatch an element on the stream from a single producer" in { fixture ⇒
        val FixtureParam(testId, broker, streamOutput) = fixture
        val consumerProbeEvent = TestProbe()
        val actorDelegatingSubscriber = system.actorOf(ActorDelegatingSubscriber.props(consumerProbeEvent.ref, 32), s"actorconsumer_$testId")
        val streamSubscriber = ActorSubscriber[Long](actorDelegatingSubscriber)
        streamOutput.subscribe(streamSubscriber)
        within(1 second) {
          Flow(List(1L)).produceTo(broker.newSubscriber)
          consumerProbeEvent.expectMsg(1L)
        }
      }

      "dispatch 2 elements on the stream from a single producer" in { fixture ⇒
        val FixtureParam(testId, broker, streamOutput) = fixture
        val consumerProbeEvent = TestProbe()
        val actorDelegatingSubscriber = system.actorOf(ActorDelegatingSubscriber.props(consumerProbeEvent.ref, 32), s"actorconsumer_$testId")
        val streamSubscriber = ActorSubscriber[Long](actorDelegatingSubscriber)
        streamOutput.subscribe(streamSubscriber)
        within(1 second) {
          Flow(List(1L, 2L)).produceTo(broker.newSubscriber)
          val res = consumerProbeEvent.receiveN(2)
          res should equal(List(1L, 2L))
        }
      }

      s"dispatch some($nMsgSome) elements on the stream from a single producer" in { fixture ⇒
        val n = nMsgSome
        val FixtureParam(testId, broker, streamOutput) = fixture
        val consumerProbeEvent = TestProbe()
        val actorDelegatingSubscriber = system.actorOf(ActorDelegatingSubscriber.props(consumerProbeEvent.ref, 32), s"actorconsumer_$testId")
        val streamSubscriber = ActorSubscriber[Long](actorDelegatingSubscriber)
        streamOutput.subscribe(streamSubscriber)
        val start = Deadline.now
        within(1 second) {
          Flow(1L to n).produceTo(broker.newSubscriber)
          val res = consumerProbeEvent.receiveN(n.toInt)
          val time = start.lap
          info(s"Dispatched $n in ${start.lap.defaultUnitString}((${(n * 1000).toDouble / time.toMillis}/s)).")
          res should equal(1L to n)
        }
      }

      s"dispatch many($nMsgBig) elements on the stream from a single producer" in { fixture ⇒
        val n = nMsgBig
        val FixtureParam(testId, broker, streamOutput) = fixture
        val consumerProbeEvent = TestProbe()
        val actorDelegatingSubscriber = system.actorOf(ActorDelegatingSubscriber.props(consumerProbeEvent.ref, 32), s"actorconsumer_$testId")
        val streamSubscriber = ActorSubscriber[Long](actorDelegatingSubscriber)
        streamOutput.subscribe(streamSubscriber)
        val start = Deadline.now
        within(10 second) {
          Flow(1L to n).produceTo(broker.newSubscriber)
          val res = consumerProbeEvent.receiveN(n.toInt, 10.seconds.dilated)
          val time = start.lap
          info(s"Dispatched $n in ${start.lap.defaultUnitString}((${(n * 1000).toDouble / time.toMillis}/s)).")
          res should equal(1L to n)
        }
      }

      "dispatch 2 elements on the stream from two producers" in { fixture ⇒
        val FixtureParam(testId, broker, streamOutput) = fixture
        val consumerProbeEvent = TestProbe()
        val actorDelegatingSubscriber = system.actorOf(ActorDelegatingSubscriber.props(consumerProbeEvent.ref, 32), s"actorconsumer_$testId")
        val streamSubscriber = ActorSubscriber[Long](actorDelegatingSubscriber)
        streamOutput.subscribe(streamSubscriber)
        within(1 second) {
          Flow(List(1L)).produceTo(broker.newSubscriber)
          Flow(List(2L)).produceTo(broker.newSubscriber)
          val res = consumerProbeEvent.receiveN(2)
          res.toSet should equal(Set(1L, 2L))
        }
      }

      s"dispatch some(${2 * nMsgSome}) elements on the stream from two producers" in { fixture ⇒
        val FixtureParam(testId, broker, streamOutput) = fixture
        val consumerProbeEvent = TestProbe()
        val actorDelegatingSubscriber = system.actorOf(ActorDelegatingSubscriber.props(consumerProbeEvent.ref, 32), s"actorconsumer_$testId")
        val streamSubscriber = ActorSubscriber[Long](actorDelegatingSubscriber)
        streamOutput.subscribe(streamSubscriber)
        val start = Deadline.now
        within(1 second) {
          Flow(1L to nMsgSome).produceTo(broker.newSubscriber)
          Flow((nMsgSome + 1L) to (2L * nMsgSome)).produceTo(broker.newSubscriber)
          val res = consumerProbeEvent.receiveN((2 * nMsgSome).toInt, 4.seconds.dilated)
          val time = start.lap
          info(s"Dispatched ${2 * nMsgSome} in ${start.lap.defaultUnitString}((${(2 * nMsgSome * 1000).toDouble / time.toMillis}/s)).")
          res.toSet should equal(Set(1L to (2 * nMsgSome): _*))
        }
      }

      s"dispatch many(${2 * nMsgBig}) elements on the stream from two producers" in { fixture ⇒
        val FixtureParam(testId, broker, streamOutput) = fixture
        val consumerProbeEvent = TestProbe()
        val actorDelegatingSubscriber = system.actorOf(ActorDelegatingSubscriber.props(consumerProbeEvent.ref, 32), s"actorconsumer_$testId")
        val streamSubscriber = ActorSubscriber[Long](actorDelegatingSubscriber)
        streamOutput.subscribe(streamSubscriber)
        val start = Deadline.now
        within(10 seconds) {
          Flow(1L to nMsgBig).produceTo(broker.newSubscriber)
          Flow((nMsgBig + 1L) to (2L * nMsgBig)).produceTo(broker.newSubscriber)
          val res = consumerProbeEvent.receiveN((2 * nMsgBig).toInt, 10.seconds.dilated)
          val time = start.lap
          info(s"Dispatched ${2 * nMsgBig} in ${start.lap.defaultUnitString}((${(2 * nMsgBig * 1000).toDouble / time.toMillis}/s)).")
          res.toSet should equal(Set(1L to (2 * nMsgBig): _*))
        }
      }

      s"dispatch some($nMsgSome) elements on the stream from $nMsgSomePublishers producers" in { fixture ⇒
        val n = nMsgSome
        val FixtureParam(testId, broker, streamOutput) = fixture
        val consumerProbeEvent = TestProbe()
        val actorDelegatingSubscriber = system.actorOf(ActorDelegatingSubscriber.props(consumerProbeEvent.ref, 32), s"actorconsumer_$testId")
        val streamSubscriber = ActorSubscriber[Long](actorDelegatingSubscriber)
        streamOutput.subscribe(streamSubscriber)
        val items = (1L to n)
        val groups = items.toSeq.groupBy(x ⇒ x % nMsgSomePublishers)
        val start = Deadline.now
        within(1 second) {
          groups.foreach(x ⇒ Flow(x._2).produceTo(broker.newSubscriber))
          val res = consumerProbeEvent.receiveN((n).toInt)
          val time = start.lap
          info(s"Dispatched ${n} in ${start.lap.defaultUnitString}((${(n * 1000).toDouble / time.toMillis}/s)).")
          res.toSet should equal(Set(items: _*))
        }
      }

      s"dispatch many($nMsgBig) elements on the stream from $nMsgSomePublishers producers" in { fixture ⇒
        val n = nMsgBig
        val FixtureParam(testId, broker, streamOutput) = fixture
        val consumerProbeEvent = TestProbe()
        val actorDelegatingSubscriber = system.actorOf(ActorDelegatingSubscriber.props(consumerProbeEvent.ref, 32), s"actorconsumer_$testId")
        val streamSubscriber = ActorSubscriber[Long](actorDelegatingSubscriber)
        streamOutput.subscribe(streamSubscriber)
        val items = (1L to n)
        val groups = items.toSeq.groupBy(x ⇒ x % nMsgSomePublishers)
        val start = Deadline.now
        within(10 second) {
          groups.foreach(x ⇒ Flow(x._2).produceTo(broker.newSubscriber))
          val res = consumerProbeEvent.receiveN((n).toInt, 10.seconds.dilated)
          val time = start.lap
          info(s"Dispatched ${n} in ${start.lap.defaultUnitString}((${(n * 1000).toDouble / time.toMillis}/s)).")
          res.toSet should equal(Set(items: _*))
        }
      }

      s"dispatch many($nMsgBig) elements on the stream from $nMsgBigPublishers producers" in { fixture ⇒
        val n = nMsgBig
        val FixtureParam(testId, broker, streamOutput) = fixture
        val consumerProbeEvent = TestProbe()
        val actorDelegatingSubscriber = system.actorOf(ActorDelegatingSubscriber.props(consumerProbeEvent.ref, 32), s"actorconsumer_$testId")
        val streamSubscriber = ActorSubscriber[Long](actorDelegatingSubscriber)
        streamOutput.subscribe(streamSubscriber)
        val items = (1L to n)
        val groups = items.toSeq.groupBy(x ⇒ x % nMsgBigPublishers)
        val start = Deadline.now
        within(10 second) {
          groups.foreach(x ⇒ Flow(x._2).produceTo(broker.newSubscriber))
          val res = consumerProbeEvent.receiveN((n).toInt, 10.seconds.dilated)
          val time = start.lap
          info(s"Dispatched ${n} in ${start.lap.defaultUnitString}((${(n * 1000).toDouble / time.toMillis}/s)).")
          res.toSet should equal(Set(items: _*))
        }
      }
    }

    "accessed via contractors" should {
      "dispatch an element on the stream from a single contractor" in { fixture ⇒
        val FixtureParam(testId, broker, streamOutput) = fixture
        val consumerProbeEvent = TestProbe()
        val actorDelegatingSubscriber = system.actorOf(ActorDelegatingSubscriber.props(consumerProbeEvent.ref, 32), s"actorconsumer_$testId")
        val streamSubscriber = ActorSubscriber[Long](actorDelegatingSubscriber)
        streamOutput.subscribe(streamSubscriber)
        within(1 second) {
          ActorStillage.create(List(1L), s"stillage-$testId").signContract(broker)
          consumerProbeEvent.expectMsg(100 millis, 1L)
        }
      }

      "dispatch 2 elements on the stream from a single contractor" in { fixture ⇒
        val FixtureParam(testId, broker, streamOutput) = fixture
        val consumerProbeEvent = TestProbe()
        val actorDelegatingSubscriber = system.actorOf(ActorDelegatingSubscriber.props(consumerProbeEvent.ref, 32), s"actorconsumer_$testId")
        val streamSubscriber = ActorSubscriber[Long](actorDelegatingSubscriber)
        streamOutput.subscribe(streamSubscriber)
        within(1 second) {
          ActorStillage.create(List(1L, 2L), s"stillage-$testId").signContract(broker)
          val res = consumerProbeEvent.receiveN(2)
          res should equal(List(1L, 2L))
        }
      }

      s"dispatch some($nMsgSome) elements on the stream from a single contractor" in { fixture ⇒
        val n = nMsgSome
        val FixtureParam(testId, broker, streamOutput) = fixture
        val consumerProbeEvent = TestProbe()
        val actorDelegatingSubscriber = system.actorOf(ActorDelegatingSubscriber.props(consumerProbeEvent.ref, 32), s"actorconsumer_$testId")
        val streamSubscriber = ActorSubscriber[Long](actorDelegatingSubscriber)
        streamOutput.subscribe(streamSubscriber)
        val start = Deadline.now
        within(1 second) {
          ActorStillage.create(1L to n, s"stillage-$testId").signContract(broker)
          val res = consumerProbeEvent.receiveN(n.toInt)
          val time = start.lap
          info(s"Dispatched $n in ${start.lap.defaultUnitString}((${(n * 1000).toDouble / time.toMillis}/s)).")
          res should equal(1L to n)
        }
      }

      s"dispatch many($nMsgBig) elements on the stream from a single contractor" in { fixture ⇒
        val n = nMsgBig
        val FixtureParam(testId, broker, streamOutput) = fixture
        val consumerProbeEvent = TestProbe()
        val actorDelegatingSubscriber = system.actorOf(ActorDelegatingSubscriber.props(consumerProbeEvent.ref, 32), s"actorconsumer_$testId")
        val streamSubscriber = ActorSubscriber[Long](actorDelegatingSubscriber)
        streamOutput.subscribe(streamSubscriber)
        val start = Deadline.now
        within(10 second) {
          ActorStillage.create(1L to n, s"stillage-$testId").signContract(broker)
          val res = consumerProbeEvent.receiveN(n.toInt, 10.second.dilated)
          val time = start.lap
          info(s"Dispatched $n in ${start.lap.defaultUnitString}((${(n * 1000).toDouble / time.toMillis}/s)).")
          res should equal(1L to n)
        }
      }

      "dispatch 2 elements on the stream from two contractors" in { fixture ⇒
        val FixtureParam(testId, broker, streamOutput) = fixture
        val consumerProbeEvent = TestProbe()
        val actorDelegatingSubscriber = system.actorOf(ActorDelegatingSubscriber.props(consumerProbeEvent.ref, 32), s"actorconsumer_$testId")
        val streamSubscriber = ActorSubscriber[Long](actorDelegatingSubscriber)
        streamOutput.subscribe(streamSubscriber)
        within(1 second) {
          ActorStillage.create(List(1L), s"stillage-$testId-1").signContract(broker)
          ActorStillage.create(List(2L), s"stillage-$testId-2").signContract(broker)
          val res = consumerProbeEvent.receiveN(2)
          res.toSet should equal(Set(1L, 2L))
        }
      }

      s"dispatch some(${2 * nMsgSome}) elements on the stream from two contractors" in { fixture ⇒
        val FixtureParam(testId, broker, streamOutput) = fixture
        val consumerProbeEvent = TestProbe()
        val actorDelegatingSubscriber = system.actorOf(ActorDelegatingSubscriber.props(consumerProbeEvent.ref, 32), s"actorconsumer_$testId")
        val streamSubscriber = ActorSubscriber[Long](actorDelegatingSubscriber)
        streamOutput.subscribe(streamSubscriber)
        val start = Deadline.now
        within(1 second) {
          ActorStillage.create(1L to nMsgSome, s"stillage-$testId-1").signContract(broker)
          ActorStillage.create((nMsgSome + 1L) to (2L * nMsgSome), s"stillage-$testId-2").signContract(broker)
          val res = consumerProbeEvent.receiveN((2 * nMsgSome).toInt)
          val time = start.lap
          info(s"Dispatched ${2 * nMsgSome} in ${start.lap.defaultUnitString}((${(2 * nMsgSome * 1000).toDouble / time.toMillis}/s)).")
          res.toSet should equal(Set(1L to (2 * nMsgSome): _*))
        }
      }

      s"dispatch many(${2 * nMsgBig}) elements on the stream from two contractors" in { fixture ⇒
        val FixtureParam(testId, broker, streamOutput) = fixture
        val consumerProbeEvent = TestProbe()
        val actorDelegatingSubscriber = system.actorOf(ActorDelegatingSubscriber.props(consumerProbeEvent.ref, 32), s"actorconsumer_$testId")
        val streamSubscriber = ActorSubscriber[Long](actorDelegatingSubscriber)
        streamOutput.subscribe(streamSubscriber)
        val start = Deadline.now
        within(10 seconds) {
          ActorStillage.create(1L to nMsgBig, s"stillage-$testId-1").signContract(broker)
          ActorStillage.create((nMsgBig + 1L) to (2L * nMsgBig), s"stillage-$testId-2").signContract(broker)
          val res = consumerProbeEvent.receiveN((2 * nMsgBig).toInt, 10.second.dilated)
          val time = start.lap
          info(s"Dispatched ${2 * nMsgBig} in ${start.lap.defaultUnitString}((${(2 * nMsgBig * 1000).toDouble / time.toMillis}/s)).")
          res.toSet should equal(Set(1L to (2 * nMsgBig): _*))
        }
      }

      s"dispatch some($nMsgSome) elements on the stream from $nMsgSomePublishers contractors" in { fixture ⇒
        val n = nMsgSome
        val FixtureParam(testId, broker, streamOutput) = fixture
        val consumerProbeEvent = TestProbe()
        val actorDelegatingSubscriber = system.actorOf(ActorDelegatingSubscriber.props(consumerProbeEvent.ref, 32), s"actorconsumer_$testId")
        val streamSubscriber = ActorSubscriber[Long](actorDelegatingSubscriber)
        streamOutput.subscribe(streamSubscriber)
        val items = (1L to n)
        val groups = items.toSeq.groupBy(x ⇒ x % nMsgSomePublishers)
        val start = Deadline.now
        within(1 second) {
          groups.foreach(x ⇒ ActorStillage.create(x._2, s"stillage-$testId-${x._1}").signContract(broker))
          val res = consumerProbeEvent.receiveN((n).toInt)
          val time = start.lap
          info(s"Dispatched ${n} in ${start.lap.defaultUnitString}((${(n * 1000).toDouble / time.toMillis}/s)).")
          res.toSet should equal(Set(items: _*))
        }
      }

      s"dispatch many($nMsgBig) elements on the stream from $nMsgSomePublishers contractors" in { fixture ⇒
        val n = nMsgBig
        val FixtureParam(testId, broker, streamOutput) = fixture
        val consumerProbeEvent = TestProbe()
        val actorDelegatingSubscriber = system.actorOf(ActorDelegatingSubscriber.props(consumerProbeEvent.ref, 32), s"actorconsumer_$testId")
        val streamSubscriber = ActorSubscriber[Long](actorDelegatingSubscriber)
        streamOutput.subscribe(streamSubscriber)
        val items = (1L to n)
        val groups = items.toSeq.groupBy(x ⇒ x % nMsgSomePublishers)
        val start = Deadline.now
        within(10 second) {
          groups.foreach(x ⇒ ActorStillage.create(x._2, s"stillage-$testId-${x._1}").signContract(broker))
          val res = consumerProbeEvent.receiveN((n).toInt, 10.second.dilated)
          val time = start.lap
          info(s"Dispatched ${n} in ${start.lap.defaultUnitString}((${(n * 1000).toDouble / time.toMillis}/s)).")
          res.toSet should equal(Set(items: _*))
        }
      }

      s"dispatch many($nMsgBig) elements on the stream from $nMsgBigPublishers contractors" in { fixture ⇒
        val n = nMsgBig
        val FixtureParam(testId, broker, streamOutput) = fixture
        val consumerProbeEvent = TestProbe()
        val actorDelegatingSubscriber = system.actorOf(ActorDelegatingSubscriber.props(consumerProbeEvent.ref, 32), s"actorconsumer_$testId")
        val streamSubscriber = ActorSubscriber[Long](actorDelegatingSubscriber)
        streamOutput.subscribe(streamSubscriber)
        val items = (1L to n)
        val groups = items.toSeq.groupBy(x ⇒ x % nMsgBigPublishers)
        val start = Deadline.now
        within(10 second) {
          groups.foreach(x ⇒ Stillage(x._2).signContract(broker))
          val res = consumerProbeEvent.receiveN((n).toInt, 10.second.dilated)
          val time = start.lap
          info(s"Dispatched ${n} in ${start.lap.defaultUnitString}((${(n * 1000).toDouble / time.toMillis}/s)).")
          res.toSet should equal(Set(items: _*))
        }
      }
    }
    "accessed via consumers and contractors" should {
      "dispatch 2 elements on the stream from a producer and a contractor" in { fixture ⇒
        val FixtureParam(testId, broker, streamOutput) = fixture
        val consumerProbeEvent = TestProbe()
        val actorDelegatingSubscriber = system.actorOf(ActorDelegatingSubscriber.props(consumerProbeEvent.ref, 32), s"actorconsumer_$testId")
        val streamSubscriber = ActorSubscriber[Long](actorDelegatingSubscriber)
        streamOutput.subscribe(streamSubscriber)
        within(1 second) {
          ActorStillage.create(List(1L), s"stillage-$testId").signContract(broker)
          Flow(List(2L)).produceTo(broker.newSubscriber)
          val res = consumerProbeEvent.receiveN(2)
          res.toSet should equal(Set(1L, 2L))
        }
      }

      s"dispatch some(${2 * nMsgSome}) elements on the stream from a producer and a contractor" in { fixture ⇒
        val FixtureParam(testId, broker, streamOutput) = fixture
        val consumerProbeEvent = TestProbe()
        val actorDelegatingSubscriber = system.actorOf(ActorDelegatingSubscriber.props(consumerProbeEvent.ref, 32), s"actorconsumer_$testId")
        val streamSubscriber = ActorSubscriber[Long](actorDelegatingSubscriber)
        streamOutput.subscribe(streamSubscriber)
        val start = Deadline.now
        within(10 seconds) {
          ActorStillage.create(1L to nMsgSome, s"stillage-$testId").signContract(broker)
          Flow((nMsgSome + 1L) to (2L * nMsgSome)).produceTo(broker.newSubscriber)
          val res = consumerProbeEvent.receiveN((2 * nMsgSome).toInt, 10.seconds.dilated)
          val time = start.lap
          info(s"Dispatched ${2 * nMsgSome} in ${start.lap.defaultUnitString}((${(2 * nMsgSome * 1000).toDouble / time.toMillis}/s)).")
          res.toSet should equal(Set(1L to (2 * nMsgSome): _*))
        }
      }

      s"dispatch many(${2 * nMsgBig}) elements on the stream from two contractors" in { fixture ⇒
        val FixtureParam(testId, broker, streamOutput) = fixture
        val consumerProbeEvent = TestProbe()
        val actorDelegatingSubscriber = system.actorOf(ActorDelegatingSubscriber.props(consumerProbeEvent.ref, 32), s"actorconsumer_$testId")
        val streamSubscriber = ActorSubscriber[Long](actorDelegatingSubscriber)
        streamOutput.subscribe(streamSubscriber)
        val start = Deadline.now
        within(10 seconds) {
          ActorStillage.create(1L to nMsgBig, s"stillage-$testId").signContract(broker)
          Flow((nMsgBig + 1L) to (2L * nMsgBig)).produceTo(broker.newSubscriber)
          val res = consumerProbeEvent.receiveN((2 * nMsgBig).toInt, 10.second.dilated)
          val time = start.lap
          info(s"Dispatched ${2 * nMsgBig} in ${start.lap.defaultUnitString}((${(2 * nMsgBig * 1000).toDouble / time.toMillis}/s)).")
          res.toSet should equal(Set(1L to (2 * nMsgBig): _*))
        }
      }

      s"dispatch some($nMsgSome) elements on the stream from ${nMsgSomePublishers / 2} producers and ${nMsgSomePublishers / 2} contractors" in { fixture ⇒
        val n = nMsgSome
        val FixtureParam(testId, broker, streamOutput) = fixture
        val consumerProbeEvent = TestProbe()
        val actorDelegatingSubscriber = system.actorOf(ActorDelegatingSubscriber.props(consumerProbeEvent.ref, 32), s"actorconsumer_$testId")
        val streamSubscriber = ActorSubscriber[Long](actorDelegatingSubscriber)
        streamOutput.subscribe(streamSubscriber)
        val items = (1L to n)
        val groups = items.toSeq.groupBy(x ⇒ x % nMsgSomePublishers)
        val start = Deadline.now
        within(1 second) {
          groups.foreach(x ⇒
            if (x._1 % 2 == 0) {
              ActorStillage.create(x._2, s"stillage-$testId-${x._1}").signContract(broker)
            } else {
              Flow(x._2).produceTo(broker.newSubscriber)
            })
          val res = consumerProbeEvent.receiveN((n).toInt)
          val time = start.lap
          info(s"Dispatched ${n} in ${start.lap.defaultUnitString}((${(n * 1000).toDouble / time.toMillis}/s)).")
          res.toSet should equal(Set(items: _*))
        }
      }

      s"dispatch many($nMsgBig) elements on the stream from ${nMsgSomePublishers / 2} producers and ${nMsgSomePublishers / 2} contractors" in { fixture ⇒
        val n = nMsgBig
        val FixtureParam(testId, broker, streamOutput) = fixture
        val consumerProbeEvent = TestProbe()
        val actorDelegatingSubscriber = system.actorOf(ActorDelegatingSubscriber.props(consumerProbeEvent.ref, 32), s"actorconsumer_$testId")
        val streamSubscriber = ActorSubscriber[Long](actorDelegatingSubscriber)
        streamOutput.subscribe(streamSubscriber)
        val items = (1L to n)
        val groups = items.toSeq.groupBy(x ⇒ x % nMsgSomePublishers)
        val start = Deadline.now
        within(10 seconds) {
          groups.foreach(x ⇒
            if (x._1 % 2 == 0) ActorStillage.create(x._2, s"stillage-$testId-${x._1}").signContract(broker)
            else Flow(x._2).produceTo(broker.newSubscriber))
          val res = consumerProbeEvent.receiveN((n).toInt, 10.second.dilated)
          val time = start.lap
          info(s"Dispatched ${n} in ${start.lap.defaultUnitString}((${(n * 1000).toDouble / time.toMillis}/s)).")
          res.toSet should equal(Set(items: _*))
        }
      }

      s"dispatch many($nMsgBig) elements on the stream from ${nMsgBigPublishers / 2} producers and ${nMsgBigPublishers / 2} contractors" in { fixture ⇒
        val n = nMsgBig
        val FixtureParam(testId, broker, streamOutput) = fixture
        val consumerProbeEvent = TestProbe()
        val actorDelegatingSubscriber = system.actorOf(ActorDelegatingSubscriber.props(consumerProbeEvent.ref, 32), s"actorconsumer_$testId")
        val streamSubscriber = ActorSubscriber[Long](actorDelegatingSubscriber)
        streamOutput.subscribe(streamSubscriber)
        val items = (1L to n)
        val groups = items.toSeq.groupBy(x ⇒ x % nMsgBigPublishers)
        val start = Deadline.now
        within(10.seconds) {
          groups.foreach(x ⇒
            if (x._1 % 2 == 0) ActorStillage.create(x._2, s"stillage-$testId-${x._1}").signContract(broker)
            else Flow(x._2).produceTo(broker.newSubscriber))
          val res = consumerProbeEvent.receiveN((n).toInt, 10.second.dilated)
          val time = start.lap
          info(s"Dispatched ${n} in ${start.lap.defaultUnitString}((${(n * 1000).toDouble / time.toMillis}/s)).")
          res.toSet should equal(Set(items: _*))
        }
      }
      s"dispatch really many(${nMsgBig * 10}) elements on the stream from ${nMsgBigPublishers * 10 / 2} producers and ${nMsgBigPublishers * 10 / 2} contractors" in { fixture ⇒
        val n = nMsgBig * 10
        val FixtureParam(testId, broker, streamOutput) = fixture
        val consumerProbeEvent = TestProbe()
        val actorDelegatingSubscriber = system.actorOf(ActorDelegatingSubscriber.props(consumerProbeEvent.ref, 32), s"actorconsumer_$testId")
        val streamSubscriber = ActorSubscriber[Long](actorDelegatingSubscriber)
        streamOutput.subscribe(streamSubscriber)
        val items = (1L to n)
        val groups = items.toSeq.groupBy(x ⇒ x % (nMsgBigPublishers * 10))
        val start = Deadline.now
        within(10.seconds) {
          groups.foreach(x ⇒
            if (x._1 % 2 == 0) ActorStillage.create(x._2, s"stillage-$testId-${x._1}").signContract(broker)
            else Flow(x._2).produceTo(broker.newSubscriber))
          val res = consumerProbeEvent.receiveN((n).toInt, 10.second.dilated)
          val time = start.lap
          info(s"Dispatched ${n} in ${start.lap.defaultUnitString}((${(n * 1000).toDouble / time.toMillis}/s)).")
          res.toSet should equal(Set(items: _*))
        }
      }
      s"dispatch many(${nMsgBig}) elements on the stream from ${nMsgBigPublishers * 10 / 2} producers and ${nMsgBigPublishers * 10 / 2} contractors starting at a random time" in { fixture ⇒
        val n = nMsgBig
        val tmax = 1.seconds.dilated
        val rnd = scala.util.Random
        val FixtureParam(testId, broker, streamOutput) = fixture
        val consumerProbeEvent = TestProbe()
        val actorDelegatingSubscriber = system.actorOf(ActorDelegatingSubscriber.props(consumerProbeEvent.ref, 32), s"actorconsumer_$testId")
        val streamSubscriber = ActorSubscriber[Long](actorDelegatingSubscriber)
        streamOutput.subscribe(streamSubscriber)
        val items = (1L to n)
        val groups = items.toSeq.groupBy(x ⇒ x % (nMsgBigPublishers * 10))
        val start = Deadline.now
        within(10.seconds) {
          groups.foreach { x ⇒
            val scale = (tmax.toMillis * rnd.nextDouble).millis
            if (x._1 % 2 == 0)
              system.scheduler.scheduleOnce(scale)(ActorStillage.create(x._2, s"stillage-$testId-${x._1}").signContract(broker))
            else
              system.scheduler.scheduleOnce(scale)(Flow(x._2).produceTo(broker.newSubscriber))
          }
          val res = consumerProbeEvent.receiveN((n).toInt, 10.second.dilated)
          val time = start.lap
          info(s"Took ${start.lap.defaultUnitString}")
          res.toSet should equal(Set(items: _*))
        }
      }
    }
  }

  private val currentTestId = new java.util.concurrent.atomic.AtomicInteger(1)
  def nextTestId = currentTestId.getAndIncrement()

  case class FixtureParam(testId: Int, broker: StreamBroker[Long], streamOutput: Publisher[Long])

  def withFixture(test: OneArgTest) = {
    val testId = nextTestId
    info(s"Test $testId")
    val shipperProps = StreamShipper.props[Long]
    val shipperActor = system.actorOf(shipperProps, s"shipper_$testId")
    val (broker, streamOutput, stopper) = StreamShipper[Long](shipperActor)
    val fixture = FixtureParam(testId, broker, streamOutput)
    try {
      withFixture(test.toNoArgTest(fixture))
    } finally {
      stopper.stop()
    }
  }

  override def beforeAll() {

  }

  override def afterAll() {
    TestKit.shutdownActorSystem(system)
  }

}