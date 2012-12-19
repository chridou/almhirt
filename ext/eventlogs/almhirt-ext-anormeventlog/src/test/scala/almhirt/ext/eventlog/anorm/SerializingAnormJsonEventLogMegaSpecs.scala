//package almhirt.ext.eventlog.anorm;
//
//import org.specs2.mutable._
//import scalaz.syntax.validation._
//import akka.util.Duration
//import almhirt.common._
//import almhirt.almvalidation.kit._
//import almhirt.environment._
//import almhirt.eventlog._
//import almhirt.domain.DomainEvent
//
//class SerializingAnormJsonEventLogMegaSpecs extends Specification with TestAlmhirtKit {
//  val count = 10000
//
//  private def withEmptyEventLog[T](f: (DomainEventLog, Almhirt) => T) =
//    inTestAlmhirt(almhirt => f(almhirt.eventLog, almhirt))
//
//  "An anorm SerializingAnormEventLog" should {
//    "accept %d events with the same aggId shuffled with %d other events and return the events for a specific aggId in the same order(getEvents(aggId))".format(count, count) in {
//      withEmptyEventLog { (eventLog, almhirt) =>
//        val rnd = new java.util.Random
//        //def createString() = ""
//        def createString() = (for (i <- 1 to 1000) yield 65+(rnd.nextInt() % 20)).foldLeft("")((agg, c) => agg + c)
//        val aggId = almhirt.getUuid
//        val pre1 = almhirt.getDateTime
//        val firstEvent = TestPersonCreated(almhirt.getUuid, aggId, "testEvent0")
//        val events = firstEvent :: (for (i <- 1 until count) yield TestPersonNameChanged(almhirt.getUuid, aggId, i, "testEvent%d_%s".format(i, createString)).asInstanceOf[DomainEvent]).toList
//        val eventsToShuffleIn = (for (i <- 0 until count) yield TestPersonCreated(almhirt.getUuid, almhirt.getUuid, "shuffle%d_%s".format(i, createString)).asInstanceOf[DomainEvent]).toList
//        val pre2 = almhirt.getDateTime
//        println("Events creation took %s".format(new org.joda.time.Period(pre2.getMillis() - pre1.getMillis())))
//
//        val shuffled = events.reverse.zip(eventsToShuffleIn.reverse).foldLeft(List.empty[DomainEvent])((acc, elem) => elem._1 :: elem._2 :: acc)
//        println("Store %d events".format(count * 2))
//        val start = almhirt.getDateTime
//        val resCommit = eventLog.storeEvents(shuffled).awaitResult(Duration.Inf).forceResult
//        val inter = almhirt.getDateTime
//        val diff1 = new org.joda.time.Period(inter.getMillis() - start.getMillis())
//        println("Store took %s".format(diff1))
//        println("Read %d events".format(count))
//        val res = eventLog.getEvents(aggId).awaitResult((Duration.Inf)).forceResult
//        val end = almhirt.getDateTime
//        val diff2 = new org.joda.time.Period(end.getMillis() - inter.getMillis())
//        println("Read took %s".format(diff2))
//        val typedEvents = events.map(_.asInstanceOf[TestPersonEvent])
//        val post1 = almhirt.getDateTime
//        val fromHistory = TestPerson.rebuildFromHistory(typedEvents) 
//        val post2 = almhirt.getDateTime
//        println("Rebuild agregate root took %s".format(new org.joda.time.Period(post2.getMillis() - post1.getMillis())))
//        res === events
//      }
//    }
//  }
//}