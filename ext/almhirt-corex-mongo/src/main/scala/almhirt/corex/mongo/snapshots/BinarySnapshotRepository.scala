package almhirt.corex.mongo.snapshots

import scala.concurrent.Future
import scala.concurrent.duration.FiniteDuration
import akka.actor._
import almhirt.common._
import almhirt.context.AlmhirtContext
import almhirt.akkax._
import almhirt.snapshots.SnapshotMarshaller
import reactivemongo.bson._
import reactivemongo.api._
import reactivemongo.api.indexes.{ Index ⇒ MIndex }
import reactivemongo.api.indexes.IndexType

object BinarySnapshotRepository {

}

private[snapshots] class BinarySnapshotRepositoryActor(
    db: DB with DBMetaCommands,
    collectionName: String,
    marshaller: SnapshotMarshaller[Array[Byte]],
    readWarningThreshold: FiniteDuration,
    writeWarningThreshold: FiniteDuration,
    readOnly: Boolean,
    initializeRetryPolicy: RetryPolicyExt,
    storageRetryPolicy: RetryPolicyExt,
    circuitControlSettings: CircuitControlSettings,
    futuresExecutionContextSelector: ExtendedExecutionContextSelector,
    marshallingExecutionContextSelector: ExtendedExecutionContextSelector)(implicit override val almhirtContext: AlmhirtContext) extends AlmActor with AlmActorLogging {
  import almhirt.snapshots.SnapshotRepository

  implicit val futuresContext = selectExecutionContext(futuresExecutionContextSelector)
  val marshallingContext = selectExecutionContext(marshallingExecutionContextSelector)

  val circuitBreaker = AlmCircuitBreaker(circuitControlSettings, futuresContext, context.system.scheduler)

  private case object Initialize
  private case object Initialized
  private case class InitializeFailed(prob: Problem)

  def receiveInitializeReadWrite: Receive = {
    case Initialize ⇒
      logInfo("Initializing")
      retryFuture(initializeRetryPolicy) {
        for {
          collectionNames ← db.collectionNames
          creationRes ← if (collectionNames.contains(collectionName)) {
            logInfo(s"""Collection "$collectionName" already exists.""")
            Future.successful(false)
          } else {
            logInfo(s"""Collection "$collectionName" does not yet exist. Create.""")
            val collection = db(collectionName)
            collection.create(true)
          }
        } yield creationRes
      }.onComplete(
        prob ⇒ self ! InitializeFailed(PersistenceProblem("Failed to initialize", cause = Some(prob))),
        succ ⇒ { self ! Initialized })

    case Initialized ⇒
      logInfo(s"Initialized.")
      registerCircuitControl(circuitBreaker)
      context.become(receiveRunning)

    case InitializeFailed(cause) ⇒
      logError("Initialization failed.")
      reportCriticalFailure(cause)
    //throw cause.toThrowable

    case SnapshotRepository.StoreSnapshot(ar) ⇒
      logWarning("Received storage message StoreSnapshot while initializing")
      sender() ! SnapshotRepository.StoreSnapshotFailed(ar.id, ServiceNotReadyProblem("The storage is not yet initialized."))

    case SnapshotRepository.MarkAggregateRootAsDeleted(id) ⇒
      logWarning("Received storage message MarkAggregateRootAsDeleted while initializing")
      sender() ! SnapshotRepository.MarkAggregateRootAsDeletedFailed(id, ServiceNotReadyProblem("The storage is not yet initialized."))

    case SnapshotRepository.DeleteSnapshot(id) ⇒
      logWarning("Received storage message DeleteSnapshot while initializing")
      sender() ! SnapshotRepository.DeleteSnapshotFailed(id, ServiceNotReadyProblem("The storage is not yet initialized."))

    case SnapshotRepository.FindSnapshot(id) ⇒
      logWarning("Received storage message FindSnapshot while initializing")
      sender() ! SnapshotRepository.FindSnapshotFailed(id, ServiceNotReadyProblem("The storage is not yet initialized."))
  }

  def receiveInitializeReadOnly: Receive = {
    case Initialize ⇒
      logInfo("Initializing")
      retryFuture(initializeRetryPolicy) {
        for {
          collectionNames ← db.collectionNames
          creationRes ← if (collectionNames.contains(collectionName)) {
            logInfo(s"""Collection "$collectionName" already exists.""")
            AlmFuture.successful(())
          } else {
            AlmFuture.failed(UnspecifiedProblem(s"""Collection "$collectionName" does not exists."""))
          }
        } yield creationRes
      }.onComplete(
        prob ⇒ self ! InitializeFailed(PersistenceProblem("Failed to initialize", cause = Some(prob))),
        succ ⇒ { self ! Initialized })

    case Initialized ⇒
      logInfo(s"Initialized.")
      registerCircuitControl(circuitBreaker)
      context.become(receiveRunning)

    case InitializeFailed(cause) ⇒
      logError("Initialization failed.")
      reportCriticalFailure(cause)
    //throw cause.toThrowable

    case SnapshotRepository.StoreSnapshot(ar) ⇒
      logWarning("Received storage message StoreSnapshot while initializing")
      sender() ! SnapshotRepository.StoreSnapshotFailed(ar.id, ServiceNotReadyProblem("The storage is not yet initialized."))

    case SnapshotRepository.MarkAggregateRootAsDeleted(id) ⇒
      logWarning("Received storage message MarkAggregateRootAsDeleted while initializing")
      sender() ! SnapshotRepository.MarkAggregateRootAsDeletedFailed(id, ServiceNotReadyProblem("The storage is not yet initialized."))

    case SnapshotRepository.DeleteSnapshot(id) ⇒
      logWarning("Received storage message DeleteSnapshot while initializing")
      sender() ! SnapshotRepository.DeleteSnapshotFailed(id, ServiceNotReadyProblem("The storage is not yet initialized."))

    case SnapshotRepository.FindSnapshot(id) ⇒
      logWarning("Received storage message FindSnapshot while initializing")
      sender() ! SnapshotRepository.FindSnapshotFailed(id, ServiceNotReadyProblem("The storage is not yet initialized."))
  }

  def receiveRunning: Receive = {
    case SnapshotRepository.StoreSnapshot(ar) ⇒
      logWarning("Received storage message StoreSnapshot while initializing")
      sender() ! SnapshotRepository.StoreSnapshotFailed(ar.id, ServiceNotReadyProblem("The storage is not yet initialized."))

    case SnapshotRepository.MarkAggregateRootAsDeleted(id) ⇒
      logWarning("Received storage message MarkAggregateRootAsDeleted while initializing")
      sender() ! SnapshotRepository.MarkAggregateRootAsDeletedFailed(id, ServiceNotReadyProblem("The storage is not yet initialized."))

    case SnapshotRepository.DeleteSnapshot(id) ⇒
      logWarning("Received storage message DeleteSnapshot while initializing")
      sender() ! SnapshotRepository.DeleteSnapshotFailed(id, ServiceNotReadyProblem("The storage is not yet initialized."))

    case SnapshotRepository.FindSnapshot(id) ⇒
      logWarning("Received storage message FindSnapshot while initializing")
      sender() ! SnapshotRepository.FindSnapshotFailed(id, ServiceNotReadyProblem("The storage is not yet initialized."))
  }

  override def receive: Receive = Actor.emptyBehavior

  override def preStart() {
    if (readOnly) {
      logInfo("Starting(r/w)...")
      context.become(receiveInitializeReadWrite)
    } else {
      logInfo("Starting(ro)...")
      context.become(receiveInitializeReadOnly)
    }
    self ! Initialize
  }

}