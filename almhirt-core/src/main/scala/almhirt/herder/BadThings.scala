package almhirt.herder

import scala.reflect.ClassTag
import almhirt.problem.Severity
import almhirt.collections.CircularBuffer

trait GetsSeverity[T] { def get(from: T): Severity }

final case class BadThingsHistory[T](occurencesCount: Int, maxSeverity: Option[Severity], lastOccurences: Vector[T])

class MutableBadThingsHistory[T: ClassTag](val maxQueueSize: Int)(implicit getsSeverity: GetsSeverity[T]) {
  private var occurencesCount: Int = 0
  private var maxSeverity: Option[Severity] = None
  private var lastOccurencesQueue = new CircularBuffer[T](maxQueueSize)

  def add(what: T) {
    occurencesCount += 1
    lastOccurencesQueue.push(what)
    val severity = getsSeverity.get(what)
    maxSeverity = Some(maxSeverity.map(_ and severity).getOrElse(severity))
  }

  def clear() {
    occurencesCount = 0
    maxSeverity = None
    lastOccurencesQueue.clear
  }

  def resize(newSize: Int) {
    lastOccurencesQueue = lastOccurencesQueue.resize(newSize)
  }

  def immutable: BadThingsHistory[T] = BadThingsHistory(occurencesCount, maxSeverity, lastOccurencesQueue.toVector)
  def immutableReversed: BadThingsHistory[T] = BadThingsHistory(occurencesCount, maxSeverity, lastOccurencesQueue.toVector.reverse)

  def oldestOccurence: Option[T] =
    lastOccurencesQueue.headOption

  def latestOccurence: Option[T] =
    lastOccurencesQueue.lastOption

}

class MutableBadThingsHistories[K, T: GetsSeverity: ClassTag](val maxQueueSize: Int) {
  private val entries = new scala.collection.mutable.HashMap[K, MutableBadThingsHistory[T]]()

  def add(key: K, what: T) {
    entries get key match {
      case Some(entry) =>
        entry.add(what)
      case None =>
        val newEntry = new MutableBadThingsHistory[T](maxQueueSize)
        newEntry.add(what)
        entries.put(key, newEntry)
    }
  }

  def get(key: K) = entries get key
  def getImmutable(key: K) = (entries get key).map(_.immutable)
  def getImmutableReversed(key: K) = (entries get key).map(_.immutableReversed)
  def all: Vector[(K, BadThingsHistory[T])] = entries.map(x => (x._1, x._2.immutable)).toVector
  def allReversed: Vector[(K, BadThingsHistory[T])] = entries.map(x => (x._1, x._2.immutableReversed)).toVector

  def clear(key: K) { get(key).foreach(_.clear()) }
  def resize(key: K, newSize: Int) { get(key).foreach(_.resize(newSize)) }

  def clearAll() { entries.clear() }
  def resizeAll(newSize: Int) { entries.values.foreach(_.resize(newSize)) }

}