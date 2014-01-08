package almhirt.components

import scala.language.existentials
import java.util.{UUID => JUUID}
import scala.concurrent.ExecutionContext
import akka.actor._
import almhirt.common._
import almhirt.configuration._
import almhirt.core.Almhirt
import almhirt.core.types._
import almhirt.domain.AggregateRootCellStateSink
import almhirt.domain.AggregateRootCell.AggregateRootCellState
import almhirt.components.impl.AggregateRootCellCacheStats
import com.typesafe.config.Config
import almhirt.components.impl.AggregateRootCellSourceImpl

object AggregateRootCellSource {
  sealed trait AggregateRootCellCacheMessage
  final case class GetCell(arId: JUUID, arType: Class[_ <: AggregateRoot[_,_]]) extends AggregateRootCellCacheMessage

  final case class AggregateRootCellSourceResult(arId: JUUID, cellHandle: CellHandle) extends AggregateRootCellCacheMessage
  final case class CellStateNotification(arId: JUUID, cellState: AggregateRootCellState) extends AggregateRootCellCacheMessage   

  final object GetStats extends AggregateRootCellCacheMessage
  final case class AggregateRootCellSourceStats(cacheStats: AggregateRootCellCacheStats) extends AggregateRootCellCacheMessage
  
  trait CellHandle {
    protected def cell: ActorRef
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
  
  def apply(cellPropsFactories: Class[_] => Option[(JUUID, AggregateRootCellStateSink) => Props], theAlmhirt: Almhirt, actorRefFactory: ActorRefFactory): AlmValidation[(ActorRef, CloseHandle)] =
    for {
      configSection <- theAlmhirt.config.v[Config]("almhirt.aggregate-root-cell-source")
      numActors <- configSection.v[Int]("number-of-actors")
    } yield {
      val theProps = Props(new AggregateRootCellSourceImpl(cellPropsFactories, theAlmhirt))
      theAlmhirt.log.info(s"""Aggregate root cell source: "number-of-actors" is $numActors""")
      if (numActors <= 1) {
        (actorRefFactory.actorOf(theProps, "aggregate-root-cell-source"), CloseHandle.noop)
      } else {
        (actorRefFactory.actorOf(Props(new AggregateRootCellSourceRouter(numActors, theProps)), "aggregate-root-cell-source"), CloseHandle.noop)
      }
    }
    
  def apply(cellPropsFactories: Class[_] => Option[(JUUID, AggregateRootCellStateSink) => Props], theAlmhirt: Almhirt): AlmValidation[(ActorRef, CloseHandle)] =
    apply(cellPropsFactories, theAlmhirt, theAlmhirt.actorSystem)
}

trait AggregateRootCellSource { actor: Actor =>
  def receiveAggregateRootCellSourceMessage: Receive
}