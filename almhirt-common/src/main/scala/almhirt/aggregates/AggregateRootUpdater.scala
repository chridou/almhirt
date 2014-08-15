package almhirt.aggregates

import almhirt.common._

trait AggregateRootUpdater[T <: AggregateRoot, E <: AggregateEvent] { self : AggregateRootEventHandler[T, E] =>
  def update(agg: T, event: E): (AggregateRootState[T], E) =
    (this.applyEvent(agg, event), event)
    
  protected implicit class ArOps(self: T) {
    def update(event: E) = AggregateRootUpdater.this.update(self, event)
    def accept(event: E) = UpdateRecorder.accept(AggregateRootUpdater.this.update(self, event))
  }

  protected implicit class Lifter(self: T => UpdateRecorder[T, E]) {
    def lift: AggregateRootState[T] => UpdateRecorder[T, E] = 
      UpdateRecorder.ifAlive(self)
      
    def liftWith(state: AggregateRootState[T]): UpdateRecorder[T, E] = 
      UpdateRecorder.ifAlive(self)(state)
  }
  
//  def recorder(agg: T, event: E): (AggregateRootState[T], E) =
//    (this.applyEvent(agg, event), event)

  object updaterimplicits {
    import scala.language.implicitConversions
    
//    implicit def lifter(f: T => UpdateRecorder[T, E]): AggregateRootState[T] => UpdateRecorder[T, E] =
//      ???
  }  
    
    
}