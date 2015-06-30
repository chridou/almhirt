package almhirt.snapshots

import akka.actor._
import almhirt.common._
import almhirt.aggregates.{ AggregateRootId, AggregateRootVersion, AggregateRoot }
import almhirt.context.AlmhirtContext
import almhirt.akkax._

object InMemorySnapshotRepository {
  def propsRaw(readonly: Boolean)(implicit almhirtContext: AlmhirtContext): Props = Props(new InMemorySnapshotRepositoryActor(readonly))

  def componentFactory(readonly: Boolean)(implicit almhirtContext: AlmhirtContext): ComponentFactory = ComponentFactory(propsRaw(readonly), SnapshotRepository.actorname)
}

private[snapshots] class InMemorySnapshotRepositoryActor(readOnly: Boolean)(implicit override val almhirtContext: AlmhirtContext) extends AlmActor with AlmActorLogging {
  import almhirt.snapshots.SnapshotRepository

  private sealed trait AggState
  private case class AggStateAlive(ar: AggregateRoot) extends AggState
  private case class AggStateDead(id: AggregateRootId, version: AggregateRootVersion) extends AggState

  private val storedSnapshots = scala.collection.mutable.HashMap[AggregateRootId, AggState]()

  def receiveRunning: Receive = {
    case SnapshotRepository.StoreSnapshot(ar) ⇒
      if (!readOnly) {
        storedSnapshots += (ar.id -> AggStateAlive(ar))
        sender() ! SnapshotRepository.SnapshotStored(ar.id)
      } else {
        sender() ! SnapshotRepository.StoreSnapshotFailed(ar.id, IllegalOperationProblem("No write operation in read only mode!"))
      }

    case SnapshotRepository.MarkAggregateRootMortuus(id, version) ⇒
      if (!readOnly) {
        storedSnapshots += (id -> AggStateDead(id, version))
        sender() ! SnapshotRepository.AggregateRootMarkedMortuus(id)
      } else {
        sender() ! SnapshotRepository.MarkAggregateRootMortuusFailed(id, IllegalOperationProblem("No write operation in read only mode!"))
      }

    case SnapshotRepository.DeleteSnapshot(id) ⇒
      if (!readOnly) {
        storedSnapshots -= id
        sender() ! SnapshotRepository.SnapshotDeleted(id)
      } else {
        sender() ! SnapshotRepository.DeleteSnapshotFailed(id, IllegalOperationProblem("No write operation in read only mode!"))
      }

    case SnapshotRepository.FindSnapshot(id) ⇒
      storedSnapshots get id match {
        case None ⇒
          sender() ! SnapshotRepository.SnapshotNotFound(id)
        case Some(AggStateAlive(ar)) ⇒
          sender() ! SnapshotRepository.FoundSnapshot(ar)
        case Some(AggStateDead(id, version)) ⇒
          sender ! SnapshotRepository.AggregateRootWasDeleted(id, version)
      }
  }

  override def receive: Receive = receiveRunning

  override def preStart() {
    if (readOnly) {
      logInfo("Starting(r/w)...")
    } else {
      logInfo("Starting(ro)...")
    }
  }

}