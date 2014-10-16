package almhirt.collections

import org.scalatest._

class CircularBufferTests extends FunSuite with Matchers {
  test("must return an empty Vector when empty") {
    val b = new CircularBuffer[Int](5)
    b.toVector should equal(Vector.empty)
  }

  test("must have size 0 when empty") {
    val b = new CircularBuffer[Int](5)
    b.size should equal(0)
  }

  test("must return the item added (size = 1)") {
    val b = new CircularBuffer[Int](5)
    b.push(7)
    b.toVector should equal(Vector(7))
  }
  
  test("must have size 1 when 1 item is added") {
    val b = new CircularBuffer[Int](5)
    b.push(7)
    b.size should equal(1)
  }

  test("must return the correct values when more than 1 items are added (size < capacity)") {
    val b = new CircularBuffer[Int](5)
    (1 to 4).foreach(b.push(_))
    b.toVector should equal(Vector(1 to 4: _*))
  }

  test("must have the correct size when more than 1 items are added (size < capacity)") {
    val b = new CircularBuffer[Int](5)
    (1 to 4).foreach(b.push(_))
    b.size should equal(4)
  }
  
  test("must  return the correct values when capacity are added") {
    val b = new CircularBuffer[Int](5)
    (1 to 5).foreach(b.push(_))
    b.toVector should equal(Vector(1 to 5: _*))
  }
 
  test("must have size capacity when capacity items are added") {
    val b = new CircularBuffer[Int](5)
    (1 to 5).foreach(b.push(_))
    b.size should equal(5)
  }
  
  test("must return the correct values when 1 more than capacity items are added ") {
    val b = new CircularBuffer[Int](5)
    (1 to 6).foreach(b.push(_))
    b.toVector should equal(Vector(2 to 6: _*))
  }
 
  test("must have size capacity when 1 more than capacity items are added") {
    val b = new CircularBuffer[Int](5)
    (1 to 6).foreach(b.push(_))
    b.size should equal(5)
  }
  
  test("must return the correct values when twice the capacity items are added") {
    val b = new CircularBuffer[Int](5)
    (1 to 10).foreach(b.push(_))
    b.toVector should equal(Vector(6 to 10: _*))
  }
 
  test("must have size capacity when twice the capacity items are added") {
    val b = new CircularBuffer[Int](5)
    (1 to 10).foreach(b.push(_))
    b.size should equal(5)
  }
}