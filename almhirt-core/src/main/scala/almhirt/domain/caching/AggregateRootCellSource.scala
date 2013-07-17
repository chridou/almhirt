package almhirt.domain.caching

import scala.language.existentials
import java.util.{ UUID => JUUID }
import akka.actor._
import almhirt.common._
import scala.concurrent.ExecutionContext

object AggregateRootCellSource {
  sealed trait AggregateRootCellCacheMessage
  final case class GetCell(arId: JUUID, arType: Class[_]) extends AggregateRootCellCacheMessage

  final case class AggregateRootCellSourceResult(arId: JUUID, cellHandle: CellHandle) extends AggregateRootCellCacheMessage

  final case class DoesNotExistNotification(arId: JUUID)   
 
  trait CellHandle {
    def cell: ActorRef
    def release()
    def onceWithCellSync[T](f: ActorRef => T) = {
      try {
        val result = f(cell)
        release()
        result
      } catch {
        case exn: Exception =>
          release()
          throw exn
      }
    }
    def onceWithCell[T](f: ActorRef => AlmFuture[T])(implicit execContext: ExecutionContext): AlmFuture[T] = {
      import almhirt.almfuture.all._
      f(cell) andThen (_ => release())
    }
  }
  
}

trait AggregateRootCellSource { actor: Actor =>
  def receiveAggregateRootCellSourceMessage: Receive
}
