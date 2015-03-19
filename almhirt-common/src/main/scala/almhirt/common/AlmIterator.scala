package almhirt.common

import scala.collection.GenTraversableOnce

/**
 * An iterator is mutable and maintains internal state!
 *
 */
trait AlmIterator[+A] extends TraversableOnce[A] with Function0[Option[A]] {
  final def apply(): Option[A] = next()
  def next(): Option[A]

  def take(n: Int): AlmIterator[A]

  def drop(n: Int): AlmIterator[A]

  def slice(from: Int, until: Int): AlmIterator[A]

  def map[B](f: A ⇒ B): AlmIterator[B]

  def flatMap[B](f: A ⇒ GenTraversableOnce[B]): AlmIterator[B]

  def ++[B >: A](that: ⇒ GenTraversableOnce[B]): AlmIterator[B]

  def filter(p: A ⇒ Boolean): AlmIterator[A]

  def withFilter(p: A ⇒ Boolean): AlmIterator[A] = filter(p)

  def filterNot(p: A ⇒ Boolean): AlmIterator[A] = filter(!p(_))

  def collect[B](pf: PartialFunction[A, B]): AlmIterator[B]

  def takeWhile(p: A ⇒ Boolean): AlmIterator[A]

  def dropWhile(p: A ⇒ Boolean): AlmIterator[A]
}

object AlmIterator {
  def apply[A](elems: A*): AlmIterator[A] = new AlmIteratorWrapper(elems.iterator)

  def fromElementsProducer[A](elementsProducer: () ⇒ Option[A]): AlmIterator[A] = {
    var _next = elementsProducer()
    new AlmIteratorWrapper(new scala.collection.AbstractIterator[A] {
      def hasNext = _next.isDefined
      def next = {
        val cur = _next
        _next = elementsProducer()
        // We can do this, since the behavior of next is undefined when hasNext returns false which is the case when cur is None
        cur.get
      }
    })
  }
}

private[almhirt] final class AlmIteratorWrapper[A](underlying: Iterator[A]) extends AlmIterator[A] {
  override def next(): Option[A] = if (underlying.hasNext) Some(underlying.next()) else None

  override def copyToArray[B >: A](xs: Array[B], start: Int, len: Int): Unit = underlying.copyToArray(xs, start, len)
  override def exists(p: A ⇒ Boolean): Boolean = underlying.exists { p }
  override def find(p: A ⇒ Boolean): Option[A] = underlying.find { p }
  override def forall(p: A ⇒ Boolean): Boolean = underlying.forall { p }
  override def foreach[U](f: A ⇒ U): Unit = underlying.foreach { f }
  override def hasDefiniteSize: Boolean = underlying.hasDefiniteSize
  override def isEmpty: Boolean = underlying.isEmpty
  override def isTraversableAgain: Boolean = underlying.isTraversableAgain
  override def seq: TraversableOnce[A] = underlying
  override def toIterator: Iterator[A] = underlying
  override def toStream: Stream[A] = underlying.toStream
  override def toTraversable: Traversable[A] = underlying.toTraversable

  override def take(n: Int): AlmIterator[A] = slice(0, n)

  override def drop(n: Int): AlmIterator[A] = {
    var j = 0
    while (j < n && next().isDefined) {
      j += 1
    }
    this
  }

  override def slice(from: Int, until: Int): AlmIterator[A] = new AlmIteratorWrapper(underlying.slice(from, until))

  override def map[B](f: A ⇒ B): AlmIterator[B] = new AlmIteratorWrapper(underlying.map(f))

  override def ++[B >: A](that: ⇒ GenTraversableOnce[B]): AlmIterator[B] = new AlmIteratorWrapper(underlying ++ that)

  override def flatMap[B](f: A ⇒ GenTraversableOnce[B]): AlmIterator[B] = new AlmIteratorWrapper(underlying.flatMap(f))

  override def filter(p: A ⇒ Boolean): AlmIterator[A] = new AlmIteratorWrapper(underlying.filter(p))

  override def collect[B](pf: PartialFunction[A, B]): AlmIterator[B] = new AlmIteratorWrapper(underlying.collect(pf))

  override def takeWhile(p: A ⇒ Boolean): AlmIterator[A] = new AlmIteratorWrapper(underlying.takeWhile(p))

  override def dropWhile(p: A ⇒ Boolean): AlmIterator[A] = new AlmIteratorWrapper(underlying.dropWhile(p))

}