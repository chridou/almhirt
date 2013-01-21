package riftwarp

import java.util.UUID
import org.joda.time.DateTime

object TypeHelpers {
  def isPrimitiveType(toCheck: Class[_]): Boolean = {
    classOf[String].isAssignableFrom(toCheck) ||
      classOf[Boolean].isAssignableFrom(toCheck) ||
      classOf[Byte].isAssignableFrom(toCheck) ||
      classOf[Int].isAssignableFrom(toCheck) ||
      classOf[Long].isAssignableFrom(toCheck) ||
      classOf[BigInt].isAssignableFrom(toCheck) ||
      classOf[Float].isAssignableFrom(toCheck) ||
      classOf[Double].isAssignableFrom(toCheck) ||
      classOf[BigDecimal].isAssignableFrom(toCheck) ||
      classOf[org.joda.time.DateTime].isAssignableFrom(toCheck) ||
      classOf[_root_.java.util.UUID].isAssignableFrom(toCheck)
  }

  def isPrimitiveValue(what: Any): Boolean = {
    what.isInstanceOf[String] ||
      what.isInstanceOf[Boolean] ||
      what.isInstanceOf[Byte] ||
      what.isInstanceOf[Int] ||
      what.isInstanceOf[Long] ||
      what.isInstanceOf[BigInt] ||
      what.isInstanceOf[Float] ||
      what.isInstanceOf[Double] ||
      what.isInstanceOf[BigDecimal] ||
      what.isInstanceOf[org.joda.time.DateTime] ||
      what.isInstanceOf[_root_.java.util.UUID]
  }
}