package almhirt.components

import akka.actor.ActorRef
import almhirt.common._

object ExecutionStateStore {
  import scala.concurrent.ExecutionContext
  import ExecutionStateTracker._

  sealed trait ExecutionStateStoreMessage
  sealed trait StoreEntryResponse
  final case class StoreEntry(entry: ExecutionStateEntry) extends ExecutionStateStoreMessage
  final case class StoreEntryState(problem: Option[Problem]) extends StoreEntryResponse
  sealed trait GetEntryResponse extends ExecutionStateStoreMessage
  final case class GetEntry(trackId: String) extends ExecutionStateStoreMessage
  final case class GetEntryResult(entry: Option[ExecutionStateEntry]) extends GetEntryResponse
  final case class GetEntryFailure(problem: Problem) extends GetEntryResponse

}

