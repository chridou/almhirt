package almhirt.collections

import org.scalatest._
import java.time.Duration
import java.time.LocalDateTime

class TimeRangeTrackerTest extends FunSuite with Matchers {

  test("covered range begin and end must be none") {
    val duration = Duration.ofMinutes(1L)
    val numberOfBuckets = 2
    val startTime = LocalDateTime.now
    val (begin, end) = new TimeRangeTrackerImpl(numberOfBuckets, duration, () => startTime).coveredRange
    begin should equal (None)
    end should equal (None)
  }

//  test("must have 10 occurences in 60 seconds") {
//    val duration = Duration.ofMinutes(1L)
//    val numberOfBuckets = 2
//    val startTime = LocalDateTime.now
//    val tracker = new TimeRangeTrackerImpl(numberOfBuckets, duration, startTime)
//    (1 to 10) foreach(index => tracker.add(startTime.plusSeconds(index)))
//    val count = tracker.occurences(startTime, startTime.plusSeconds(60))
//    count should equal(10)
//  }
//  
//  test("must have 6 occurences") {
//    val duration = Duration.ofMinutes(1L)
//    val numberOfBuckets = 2
//    val startTime = LocalDateTime.now
//    val tracker = new TimeRangeTrackerImpl(numberOfBuckets, duration, startTime)
//    (1 to 10) foreach(index => tracker.add(startTime.plusSeconds(index*10)))
//    val count = tracker.occurences(startTime, startTime.plusSeconds(60))
//    count should equal(6)
//  }
//  
//  test("must have 10 occurences in 120 seconds") {
//    val duration = Duration.ofMinutes(1L)
//    val numberOfBuckets = 2
//    val startTime = LocalDateTime.now
//    val tracker = new TimeRangeTrackerImpl(numberOfBuckets, duration, startTime)
//    (1 to 10) foreach(index => tracker.add(startTime.plusSeconds(index*10)))
//    val count = tracker.occurences(startTime, startTime.plusSeconds(120))
//    count should equal(10)
//  }

}