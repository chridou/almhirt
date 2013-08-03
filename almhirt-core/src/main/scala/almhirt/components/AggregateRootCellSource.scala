package almhirt.components

import scala.language.existentials
import java.util.{ UUID => JUUID }
import akka.actor._
import almhirt.common._
import scala.concurrent.ExecutionContext
import java.util.{UUID => JUUID}
import almhirt.domain.AggregateRoot

object AggregateRootCellSource {
  sealed trait AggregateRootCellCacheMessage
  final case class GetCell(arId: JUUID, arType: Class[_ <: AggregateRoot[_,_]]) extends AggregateRootCellCacheMessage

  final case class AggregateRootCellSourceResult(arId: JUUID, cellHandle: CellHandle) extends AggregateRootCellCacheMessage
  final case class DoesNotExistNotification(arId: JUUID) extends AggregateRootCellCacheMessage   

  final object GetStats extends AggregateRootCellCacheMessage
  final case class AggregateRootCellSourceStats(
      cachedCells: Int, 
      numberOfBookedCells: Int,
      numberOfBookings: Int) extends AggregateRootCellCacheMessage
  
  trait CellHandle {
    def cell: ActorRef
    def release()
    /**
     * Execute the function f with the contained cell and the release the cell. 
     * DO NOT RETURN A FUTURE! THE CELL MIGHT BE RELEASED BEFORE THE FUTURES CONTENT GETS EXECUTED!
     * 
     * If you want to go async, which should be the preferred way, use "onceWithCell"(the one with the same name like this one except without the "Sync")
     * 
     * A Loan-Pattern
     */
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
    
    /**
     * Execute the function f which returns a future and release the cell when the returned future has been executed. 
     * 
     * A Loan-Pattern
     */
    def onceWithCell[T](f: ActorRef => AlmFuture[T])(implicit execContext: ExecutionContext): AlmFuture[T] = {
      import almhirt.almfuture.all._
      f(cell) andThen (_ => release())
    }
  }
}

trait AggregateRootCellSource { actor: Actor =>
  def receiveAggregateRootCellSourceMessage: Receive
}
