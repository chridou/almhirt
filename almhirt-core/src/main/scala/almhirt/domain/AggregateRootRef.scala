package almhirt.domain

import language.implicitConversions

final case class AggregateRootRef(id: java.util.UUID, version: Long) {
  def inc: AggregateRootRef = AggregateRootRef(id, version + 1L)
}

object AggregateRootRef {
  def apply(id: java.util.UUID): AggregateRootRef = AggregateRootRef(id, 0L)
  
  implicit def tuple2ToAggRef(idAndVersion: (java.util.UUID, Long)): AggregateRootRef = AggregateRootRef(idAndVersion._1, idAndVersion._2)
}
