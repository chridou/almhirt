package almhirt.collections

import org.scalatest._
import java.time.Duration
import java.time.LocalDateTime

class TimeRangeTrackerTest extends FunSuite with Matchers {

  test("covered range must be 2 minutes") {
    val duration = Duration.ofMinutes(1L)
    val numberOfBuckets = 2
    val coveredRange = new TimeRangeTrackerImpl(numberOfBuckets, duration).coveredRange
    val coveredDuration = Duration.between(coveredRange._1, coveredRange._2)
    coveredDuration.toMinutes should equal(2)
  }

  test("must have 10 occurences") {
    val duration = Duration.ofMinutes(1L)
    val numberOfBuckets = 2
    val startTime = LocalDateTime.now
    val tracker = new TimeRangeTrackerImpl(numberOfBuckets, duration, startTime)
    (1 to 10) foreach(index => tracker.add(startTime.plusSeconds(index)))
    val count = tracker.occurences(startTime, startTime.plusSeconds(10))
    count should equal(10)
  }

}