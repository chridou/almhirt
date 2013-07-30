package almhirt.components.impl

import akka.actor._
import almhirt.components.{ ExecutionStateTracker, ExecutionStateStore}

trait InMemoryExecutionStateTracker extends ExecutionStateTracker { actor: ExecutionTrackerTemplate with Actor with ActorLogging =>
  import ExecutionStateTracker._
  import ExecutionStateStore._
  
  protected class SecondLevelStoreActor extends Actor {
    var stored: Map[String, TrackingEntry] = Map.empty
    
    def receive: Receive = {
      case StoreEntry(entry) => 
        stored = stored + (entry.currentState.trackId -> entry)
        sender ! StoreEntryState(None)
      case GetEntry(trackId) => 
        sender ! GetEntryResult(stored.get(trackId))
    }
  }
  
  private val secondLeveStoreActor: ActorRef = 
    context.actorOf(Props[SecondLevelStoreActor], "in_memory_second_level_store")
  
  override val secondLevelStore = ExecutionStateStore.secondLevelStoreWrapper(secondLeveStoreActor)

}