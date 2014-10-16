package almhirt.herder

import scala.reflect.ClassTag
import almhirt.problem.Severity
import almhirt.collections.CircularBuffer

trait GetsSeverity[T] { def get(from: T): Severity }

final case class BadThings[T](occurencesCount: Int, maxSeverity: Option[Severity], lastOccurences: Vector[T])

class MutableBadThings[T: ClassTag](val maxQueueSize: Int)(implicit getsSeverity: GetsSeverity[T]) {
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
  
  def immutable: BadThings[T] = BadThings(occurencesCount, maxSeverity, lastOccurencesQueue.toVector)
}

class MutableBadThingsContainer[K, T : GetsSeverity : ClassTag](val maxQueueSize: Int) {
  private val entries = new scala.collection.mutable.HashMap[K, MutableBadThings[T]]()
  
  def add(key: K, what: T) {
    entries get key match {
      case Some(entry) => 
        entry.add(what)
      case None =>
        val newEntry = new MutableBadThings[T](maxQueueSize)
        newEntry.add(what)
        entries.put(key, newEntry)
    }
  }
  
  def get(key: K) = entries get key
  
  def all: Vector[(K, BadThings[T])] = entries.map(x => (x._1, x._2.immutable)).toVector
  
  def clear(key: K) { get(key).foreach(_.clear()) }
  
  def clearAll() { entries.clear() }
}