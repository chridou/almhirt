package almhirt.collections

import scala.language.higherKinds

import scala.reflect.ClassTag
import scala.collection.generic.CanBuildFrom

/**
 *  A circular buffer that preallocates the required slots.
 *
 *  Not thread safe
 */
class CircularBuffer[@specialized T](capacity: Int)(implicit tag: ClassTag[T]) {
  require(capacity > 0)

  private val arr = new Array[T](capacity)

  private var _cursor = 0
  private var _size = 0

  def push(value: T) {
    arr(_cursor) = value
    _cursor += 1
    _cursor %= capacity
    if (_size < capacity) _size += 1
  }

  def size = _size

  def clear() {
    _size = 0
  }
  
  def itemAt(idx: Int): T = {
    if (_size == 0 || idx >= size)
      throw new IndexOutOfBoundsException()
    else
      arr((_cursor + capacity - size + idx) % capacity)
    
  }

  def headOption: Option[T] =
    if (_size == 0)
      None
    else
      Some(itemAt(0))

  def lastOption: Option[T] =
    if (_size == 0)
      None
    else
      Some(itemAt(_size-1))

  def resize(newCapacity: Int): CircularBuffer[T] = {
    val newBuffer = new CircularBuffer(newCapacity)
    this.toVector.take(Math.min(_size, newCapacity)).foreach(newBuffer.push)
    newBuffer
  }

  def isEmpty = size == 0

  // needs optimization...
  def toVector: Vector[T] = {
    var pos = (_cursor + capacity - size) % capacity
    var vec = Vector.empty[T]
    var i = 0
    while (i < _size) {
      vec = vec :+ arr(pos)
      pos += 1
      pos %= capacity
      i += 1
    }
    vec
  }
}
