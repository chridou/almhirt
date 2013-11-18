package almhirt.corex.spray

import akka.actor._
import almhirt.core.Almhirt


abstract class SingleTypeHttpPublisher[T]()(implicit myAlmhirt: Almhirt) extends almhirt.httpx.spray.connectors.SingleTypeHttpPublisher[T] {
  override val executionContext = myAlmhirt.futuresExecutor
  override val serializationExecutionContext = myAlmhirt.numberCruncher

}

abstract class SingleTypeHttpQuery[U]()(implicit myAlmhirt: Almhirt) extends almhirt.httpx.spray.connectors.SingleTypeHttpQuery[U] {
  override val executionContext = myAlmhirt.futuresExecutor
  override val serializationExecutionContext = myAlmhirt.numberCruncher
}

abstract class SingleTypeHttpConversation[T, U]()(implicit myAlmhirt: Almhirt) extends almhirt.httpx.spray.connectors.SingleTypeHttpConversation[T, U] {
  override val executionContext = myAlmhirt.futuresExecutor
  override val serializationExecutionContext = myAlmhirt.numberCruncher
}