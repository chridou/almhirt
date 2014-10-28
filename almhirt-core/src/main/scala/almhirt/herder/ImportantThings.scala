package almhirt.herder

import scala.reflect.ClassTag
import almhirt.common._
import almhirt.collections.CircularBuffer

trait GetsImportance[T] { def get(from: T): Importance }

final case class ImportantThingsHistory[T](occurencesCount: Int, maxImportance: Option[Importance], lastOccurences: Vector[T])

class MutableImportantThingsHistory[T: ClassTag](val maxQueueSize: Int)(implicit getsImportance: GetsImportance[T]) {
  private var occurencesCount: Int = 0
  private var maxImportance: Option[Importance] = None
  private var lastOccurencesQueue = new CircularBuffer[T](maxQueueSize)

  def add(what: T) {
    occurencesCount += 1
    lastOccurencesQueue.push(what)
    val Importance = getsImportance.get(what)
    maxImportance = Some(maxImportance.map(_ and Importance).getOrElse(Importance))
  }

  def clear() {
    occurencesCount = 0
    maxImportance = None
    lastOccurencesQueue.clear
  }

  def resize(newSize: Int) {
    lastOccurencesQueue = lastOccurencesQueue.resize(newSize)
  }

  def immutable: ImportantThingsHistory[T] = ImportantThingsHistory(occurencesCount, maxImportance, lastOccurencesQueue.toVector)
  def immutableReversed: ImportantThingsHistory[T] = ImportantThingsHistory(occurencesCount, maxImportance, lastOccurencesQueue.toVector.reverse)

  def oldestOccurence: Option[T] =
    lastOccurencesQueue.headOption

  def latestOccurence: Option[T] =
    lastOccurencesQueue.lastOption

}

class MutableImportantThingsHistories[K, T: GetsImportance: ClassTag](val maxQueueSize: Int) {
  private val entries = new scala.collection.mutable.HashMap[K, MutableImportantThingsHistory[T]]()

  def add(key: K, what: T) {
    entries get key match {
      case Some(entry) =>
        entry.add(what)
      case None =>
        val newEntry = new MutableImportantThingsHistory[T](maxQueueSize)
        newEntry.add(what)
        entries.put(key, newEntry)
    }
  }

  def get(key: K) = entries get key
  def getImmutable(key: K) = (entries get key).map(_.immutable)
  def getImmutableReversed(key: K) = (entries get key).map(_.immutableReversed)
  def all: Vector[(K, ImportantThingsHistory[T])] = entries.map(x => (x._1, x._2.immutable)).toVector

  def clear(key: K) { get(key).foreach(_.clear()) }
  def resize(key: K, newSize: Int) { get(key).foreach(_.resize(newSize)) }

  def clearAll() { entries.clear() }
  def resizeAll(newSize: Int) { entries.values.foreach(_.resize(newSize)) }

}