package almhirt.collections

import java.time._;

class TimeRangeTracker(numberOfBuckets: Int, bucketSpan: Duration) {
  final case class TimeRange(begin: LocalDateTime, end: LocalDateTime)
  final case class OccurencesInTimeRange(timeRange: TimeRange, var count: Long)

  private var _buckets = List[OccurencesInTimeRange]()
  private val _initTime = LocalDateTime.now
  initializeBuckets()

  private def initializeBuckets() = {
    (1 to numberOfBuckets) foreach (index ⇒ {
      val beginOffset = (index - 1) * bucketSpan.getNano
      val endOffset = index * bucketSpan.getNano
      val begin = _initTime.plusNanos(beginOffset.toLong)
      val end = _initTime.plusNanos(endOffset.toLong)
      val timeRange = new TimeRange(begin, end)
      _buckets :+ new OccurencesInTimeRange(timeRange, 0L)
    })
  }

  //
  def add(occurrence: LocalDateTime): Unit = _buckets foreach (bucket ⇒ {
    if (occurrence.isAfter(bucket.timeRange.begin) && occurrence.isBefore(bucket.timeRange.end))
      bucket.count = bucket.count + 1L
  })

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