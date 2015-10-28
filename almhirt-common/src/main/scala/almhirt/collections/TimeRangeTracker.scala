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
    val currentTime = getTime()
    if (buckets.size == 0)
      buckets.push(OccurencesInTimeRange(TimeRange(currentTime, currentTime.plusNanos(bucketSpan.toNanos)), 1L))
    else {
      val filteredBuckets = buckets.toVector.filter(bucket ⇒
        occurrence.isAfter(bucket.timeRange.begin.minusNanos(1L)) && occurrence.isBefore(bucket.timeRange.end.plusNanos(1L)))
      if (filteredBuckets.isEmpty)
        buckets.push(OccurencesInTimeRange(TimeRange(currentTime, currentTime.plusNanos(bucketSpan.toNanos)), 1L))
      else
        filteredBuckets foreach (bucket => bucket.count = bucket.count + 1L)
    }
  }

  def coveredRange: (Option[LocalDateTime], Option[LocalDateTime]) = {
    val begin = buckets.headOption match {
      case Some(bucket) => Some(bucket.timeRange.begin)
      case None         => None
    }

    val end = buckets.lastOption match {
      case Some(bucket) => Some(bucket.timeRange.end)
      case None         => None
    }

    (begin, end)
  }

  def occurences(time: Duration): Long = {
    val currentTime = getTime()
    val from = currentTime.minusNanos(time.toNanos)
    buckets.toVector.filter(bucket => bucket.timeRange.begin.isAfter(from.minusNanos(1L)) && bucket.timeRange.end.isBefore(currentTime.plusNanos(1L)))
      .map(_.count).sum
  }

}