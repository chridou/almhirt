package almhirt.collections

import java.time._;

class TimeRangeTracker(numberOfBuckets: Int, bucketSpan: Duration) {
  final case class TimeRange(begin: LocalDateTime, end: LocalDateTime)
  final case class OccurencesInTimeRange(timeRange: TimeRange, count: Long)

  private val _buckets = new Array[Int](numberOfBuckets)

  def add(occurrence: LocalDateTime): Unit = ???
  def adjust(currentTime: LocalDateTime): Unit = ???

  def latestBucket(currentTime: LocalDateTime): OccurencesInTimeRange = ???

  // muss hier aktualisiert werden? vielleicht eine Methode die aktualisiert und eine die es niocht tut?
  def bucketAt(targetTime: LocalDateTime): Option[OccurencesInTimeRange] = ???

  // muss hier aktualisiert werden? vielleicht eine Methode die aktualisiert und eine die es nicht tut? bucketByIndex(0) == latestBucket 
  def bucketByIndex(index: Int): Option[OccurencesInTimeRange] = ???

  def occurences(currentTime: LocalDateTime): OccurencesInTimeRange = ???

  def buckets(currentTime: LocalDateTime): Seq[OccurencesInTimeRange] = ???

  def coveredTime(currentTime: LocalDateTime): TimeRange = ???

  def coveredDuration(currentTime: LocalDateTime): Duration = ???

}