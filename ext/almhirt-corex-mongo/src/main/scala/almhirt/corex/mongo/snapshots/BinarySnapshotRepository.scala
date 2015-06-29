package almhirt.corex.mongo.snapshots

import scala.concurrent.Future
import scala.concurrent.duration.FiniteDuration
import akka.actor._
import almhirt.common._
import almhirt.aggregates.AggregateRootId
import almhirt.almfuture.all._
import almhirt.context.AlmhirtContext
import almhirt.akkax._
import almhirt.snapshots.SnapshotMarshaller
import reactivemongo.bson._
import reactivemongo.api._
import reactivemongo.api.indexes.{ Index ⇒ MIndex }
import reactivemongo.api.indexes.IndexType
import reactivemongo.core.commands.GetLastError

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

    case SnapshotRepository.MarkAggregateRootMortuus(id, version) ⇒
      logWarning("Received storage message MarkAggregateRootMortuus while initializing")
      sender() ! SnapshotRepository.MarkAggregateRootMortuusFailed(id, ServiceNotReadyProblem("The storage is not yet initialized."))

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

    case SnapshotRepository.MarkAggregateRootMortuus(id, version) ⇒
      logWarning("Received storage message MarkAggregateRootMortuus while initializing")
      sender() ! SnapshotRepository.MarkAggregateRootMortuusFailed(id, ServiceNotReadyProblem("The storage is not yet initialized."))

    case SnapshotRepository.DeleteSnapshot(id) ⇒
      logWarning("Received storage message DeleteSnapshot while initializing")
      sender() ! SnapshotRepository.DeleteSnapshotFailed(id, ServiceNotReadyProblem("The storage is not yet initialized."))

    case SnapshotRepository.FindSnapshot(id) ⇒
      logWarning("Received storage message FindSnapshot while initializing")
      sender() ! SnapshotRepository.FindSnapshotFailed(id, ServiceNotReadyProblem("The storage is not yet initialized."))
  }

  def receiveRunning: Receive = {

    case SnapshotRepository.StoreSnapshot(ar) ⇒
      val f = for {
        arBytes ← AlmFuture(marshaller.marshal(ar))(marshallingContext)
        storedAggId ← circuitBreaker.fused(storeBinarySnapshot(StoredBinaryVivusSnapshot(ar.id, ar.version, arBytes)))
      } yield SnapshotRepository.SnapshotStored(storedAggId)
      f.recoverThenPipeTo(fail ⇒ SnapshotRepository.StoreSnapshotFailed(ar.id, fail))(sender())

    case SnapshotRepository.MarkAggregateRootMortuus(id, version) ⇒
      val f = circuitBreaker.fused(markSnapshotMortuus(StoredMortuusSnapshot(id, version))).map(SnapshotRepository.AggregateRootMarkedMortuus(_))
      f.recoverThenPipeTo(fail ⇒ SnapshotRepository.MarkAggregateRootMortuusFailed(id, fail))(sender())

    case SnapshotRepository.DeleteSnapshot(id) ⇒
      val f = circuitBreaker.fused(deleteSnapshot(id)).map(SnapshotRepository.SnapshotDeleted(_))
      f.recoverThenPipeTo(fail ⇒ SnapshotRepository.DeleteSnapshotFailed(id, fail))(sender())

    case SnapshotRepository.FindSnapshot(id) ⇒
      val f = circuitBreaker.fused(findSnapshot(id))
      f.recoverThenPipeTo(fail ⇒ SnapshotRepository.FindSnapshotFailed(id, fail))(sender())
  }

  override def receive: Receive = Actor.emptyBehavior

  private def storeBinarySnapshot(snapshot: StoredBinaryVivusSnapshot): AlmFuture[AggregateRootId] = {
    val collection = db(collectionName)
    retryFuture(storageRetryPolicy)(
      collection.update(BSONDocument("_id" -> snapshot.aggId.value), snapshot: StoredSnapshot, writeConcern = GetLastError(), upsert = true, multi = false).toAlmFuture.mapV(res ⇒
        if (res.ok) {
          scalaz.Success(snapshot.aggId)
        } else {
          scalaz.Failure(PersistenceProblem(s"""Failed to upsert snapshot for ${snapshot.aggId.value} with version ${snapshot.version.value}: ${res.message}"""))
        }))
  }

  private def markSnapshotMortuus(snapshot: StoredMortuusSnapshot): AlmFuture[AggregateRootId] = {
    val collection = db(collectionName)
    retryFuture(storageRetryPolicy)(
      collection.update(BSONDocument("_id" -> snapshot.aggId.value), snapshot: StoredSnapshot, writeConcern = GetLastError(), upsert = true, multi = false).toAlmFuture.mapV(res ⇒
        if (res.ok) {
          scalaz.Success(snapshot.aggId)
        } else {
          scalaz.Failure(PersistenceProblem(s"""Failed to mark snapshot for ${snapshot.aggId.value} with version ${snapshot.version.value} as mortuus: ${res.message}"""))
        }))
  }

  private def deleteSnapshot(id: AggregateRootId): AlmFuture[AggregateRootId] = {
    val collection = db(collectionName)
    retryFuture(storageRetryPolicy)(
      collection.remove(BSONDocument("_id" -> id.value), writeConcern = GetLastError(), firstMatchOnly = true).toAlmFuture.mapV(res ⇒
        if (res.ok) {
          scalaz.Success(id)
        } else {
          scalaz.Failure(PersistenceProblem(s"""Failed to delete snapshot for ${id.value}: ${res.message}"""))
        }))
  }

  private def findSnapshot(id: AggregateRootId): AlmFuture[SnapshotRepository.FindSnapshotResponse] = {
    val collection = db(collectionName)
    retryFuture(storageRetryPolicy)(
      (for {
        docs ← collection.find(BSONDocument("_id" -> id.value)).cursor[StoredSnapshot].collect[List](2, true).toAlmFuture
        snapshotFromStorage ← AlmFuture {
          docs match {
            case Nil                        ⇒ scalaz.Success(None)
            case (x: StoredSnapshot) :: Nil ⇒ scalaz.Success(Some(x))
            case x                          ⇒ scalaz.Failure(PersistenceProblem(s"""Expected 0..1 snapshots of type StoredSnapshot with id "${id.value}". Found ${x.size}."""))
          }
        }
        rsp ← snapshotFromStorage match {
          case Some(StoredBinaryVivusSnapshot(_, _, bin)) ⇒ AlmFuture(marshaller.unmarshal(bin).map(SnapshotRepository.FoundSnapshot(_)))(marshallingContext)
          case Some(StoredBsonVivusSnapshot(_, _, _))     ⇒ AlmFuture.successful(SnapshotRepository.FindSnapshotFailed(id, UnspecifiedProblem("This storage does not support a BSON representation of an aggregate root.")))
          case Some(StoredMortuusSnapshot(id, _))         ⇒ AlmFuture.successful(SnapshotRepository.AggregateRootWasDeleted(id))
          case None                                       ⇒ AlmFuture.successful(SnapshotRepository.SnapshotNotFound(id))
        }

      } yield rsp))
  }

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