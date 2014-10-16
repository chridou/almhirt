package almhirt.herder

import scala.reflect.ClassTag
import almhirt.problem.Severity
import almhirt.collections.CircularBuffer

trait GetsSeverity[T] { def get(from: T): Severity }

class MutableBadThingsStats[T: ClassTag](val maxQueueSize: Int)(implicit getsSeverity: GetsSeverity[T]) {
  private var occurencesCount: Int = 0
  private var maxSeverity: Option[Severity] = None
  private val lastOccurencesQueue = new CircularBuffer[T](maxQueueSize) 
  
  def add(what: T) {
    occurencesCount += 1
    lastOccurencesQueue.push(what)
    val severity = getsSeverity.get(what)
    maxSeverity.map(_ and severity).getOrElse(severity)
  }
  
  def clear() {
    occurencesCount = 0
    maxSeverity = None
    lastOccurencesQueue.clear
  }
}

class MutableBadThingsStatsContainer[K, T : GetsSeverity : ClassTag](val maxQueueSize: Int) {
  private val entries = new scala.collection.mutable.HashMap[K, MutableBadThingsStats[T]]()
  
  def add(key: K, what: T) {
    entries get key match {
      case Some(entry) => 
        entry.add(what)
      case None =>
        val newEntry = new MutableBadThingsStats[T](maxQueueSize)
        newEntry.add(what)
        entries.put(key, newEntry)
    }
  }
  
  def get(key: K) = entries get key
  
  def clear(key: K) { get(key).foreach(_.clear()) }
  
  def clearAll() { entries.clear() }
}