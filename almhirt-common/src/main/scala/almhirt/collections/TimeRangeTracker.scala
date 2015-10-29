package almhirt.collections

import java.time._;

trait TimeRangeTracker {
  def coveredRange: (Option[LocalDateTime], Option[LocalDateTime])
  def add(occurrence: LocalDateTime): Unit
  def occurences(time: Duration): Long
}

class TimeRangeTrackerImpl(numberOfBuckets: Int, bucketSpan: Duration, getTime: () => LocalDateTime) extends TimeRangeTracker {
  case class TimeRange(begin: LocalDateTime, end: LocalDateTime)
  case class OccurencesInTimeRange(timeRange: TimeRange, var count: Long)

  val buckets = new CircularBuffer[OccurencesInTimeRange](numberOfBuckets)

  def add(occurrence: LocalDateTime): Unit = {
    if (buckets.size == 0)
      buckets.push(OccurencesInTimeRange(TimeRange(occurrence, occurrence.plusNanos(bucketSpan.toNanos)), 1L))
    else {
      val filteredBuckets = buckets.toVector.filter(bucket ⇒
        occurrence.isAfter(bucket.timeRange.begin.minusNanos(1L)) && occurrence.isBefore(bucket.timeRange.end))
      if (filteredBuckets.isEmpty)
        addBucketInTimeInterval(occurrence)
      else
        filteredBuckets foreach (bucket => bucket.count = bucket.count + 1L)
    }
  }

  private def addBucketInTimeInterval(occurrence: LocalDateTime): Unit = {
    val firstTimeRange: TimeRange = buckets.headOption match {
      case Some(bucket) => TimeRange(bucket.timeRange.end, bucket.timeRange.end.plusNanos(bucketSpan.toNanos))
      case None         => TimeRange(occurrence, occurrence.plusNanos(bucketSpan.toNanos))
    }

    var newBucketTimeFound = false
    val occurrenceIsAfterFirstEntry = occurrence.isAfter(firstTimeRange.begin.minusNanos(1L))
    println(occurrenceIsAfterFirstEntry)
    var newTimeRange = firstTimeRange

    while (!newBucketTimeFound) {
      if (occurrence.isAfter(newTimeRange.begin.minusNanos(1L)) && occurrence.isBefore(newTimeRange.end)) {
        buckets.push(OccurencesInTimeRange(TimeRange(newTimeRange.begin, newTimeRange.end), 1L))
        newBucketTimeFound = true
      } else {
        if (occurrenceIsAfterFirstEntry)
          newTimeRange = TimeRange(newTimeRange.end, newTimeRange.end.plusNanos(bucketSpan.toNanos))
        else 
          newTimeRange = TimeRange(newTimeRange.begin.minusNanos(bucketSpan.toNanos), newTimeRange.begin)
      }
    }
  }

  def coveredRange: (Option[LocalDateTime], Option[LocalDateTime]) = (buckets.headOption, buckets.lastOption) match {
    case (Some(headBucket), Some(lastBucket)) => (Some(headBucket.timeRange.begin), Some(lastBucket.timeRange.end))
    case (Some(headBucket), None)             => (Some(headBucket.timeRange.begin), Some(headBucket.timeRange.end))
    case (None, Some(lastBucket))             => (Some(lastBucket.timeRange.begin), Some(lastBucket.timeRange.end))
    case (None, None)                         => (None, None)
  }

  def occurences(time: Duration): Long = {
    val currentTime = getTime()
    val from = currentTime.minusNanos(time.toNanos)
    buckets.toVector.filter(bucket => bucket.timeRange.begin.isAfter(from.minusNanos(1L)) && bucket.timeRange.end.isBefore(currentTime.plusNanos(1L)))
      .map(_.count).sum
  }

}