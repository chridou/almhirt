package riftwarp.std

object WarpPackageHelpers {
 def isPrimitive(clazz: Class[_]): Boolean = {
    if (clazz == classOf[String])
      true
    else if (clazz == classOf[_root_.java.lang.String])
      true
    else if (clazz == classOf[Boolean])
      true
    else if (clazz == classOf[_root_.java.lang.Boolean])
     true
    else if (clazz == classOf[Byte])
      true
    else if (clazz == classOf[_root_.java.lang.Byte])
      true
    else if (clazz == classOf[Int])
      true
    else if (clazz == classOf[_root_.java.lang.Integer])
      true
    else if (clazz == classOf[Long])
      true
    else if (clazz == classOf[_root_.java.lang.Long])
      true
    else if (clazz == classOf[BigInt])
      true
    else if (clazz == classOf[Float])
      true
    else if (clazz == classOf[_root_.java.lang.Float])
      true
    else if (clazz == classOf[Double])
      true
    else if (clazz == classOf[_root_.java.lang.Double])
      true
    else if (clazz == classOf[BigDecimal])
      true
    else if (clazz == classOf[_root_.java.time.ZonedDateTime])
      true
    else if (clazz == classOf[_root_.java.time.LocalDateTime])
      true
    else if (clazz == classOf[_root_.java.util.UUID])
      true
    else if (clazz == classOf[_root_.java.net.URI])
      true
    else
      false
  }
}