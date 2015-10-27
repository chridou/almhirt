package almhirt.collections

import java.time._;

trait TimeRangeTracker {
  def coveredRange: (LocalDateTime, LocalDateTime)
  def add(occurrence: LocalDateTime): Unit
  def occurences(from: LocalDateTime, to: LocalDateTime): Long
}

class TimeRangeTrackerImpl(numberOfBuckets: Int, bucketSpan: Duration, startTime: LocalDateTime = LocalDateTime.now) extends TimeRangeTracker {
  case class TimeRange(begin: LocalDateTime, end: LocalDateTime)
  case class OccurencesInTimeRange(timeRange: TimeRange, var count: Long)

  val buckets = ((1 to numberOfBuckets) map (index ⇒ {
    val beginOffset = (index - 1) * bucketSpan.toNanos
    val endOffset = index * bucketSpan.toNanos
    val begin = index match {
      case 1 ⇒ startTime.plusNanos(beginOffset.toLong)
      case _ ⇒ startTime.plusNanos(beginOffset.toLong + 1L)
    }
    val end =  startTime.plusNanos(endOffset.toLong)
    OccurencesInTimeRange(TimeRange(begin, end), 0L)
  })).toSeq

  def add(occurrence: LocalDateTime): Unit = buckets foreach (bucket ⇒ {
    if (occurrence.isAfter(bucket.timeRange.begin.minusNanos(1L)) && occurrence.isBefore(bucket.timeRange.end.plusNanos(1L)))
      bucket.count = bucket.count + 1L
  })

  def coveredRange: (LocalDateTime, LocalDateTime) = (buckets.head.timeRange.begin, buckets.last.timeRange.end)

  def occurences(from: LocalDateTime, to: LocalDateTime): Long = buckets
    .filter(bucket =>
      bucket.timeRange.begin.isAfter(from.minusNanos(1L)) && bucket.timeRange.end.isBefore(to.plusNanos(1L)))
    .map(_.count).sum

}