package almhirt.corex.mongo.aggregaterooteventlog

import scala.language.reflectiveCalls
import scala.concurrent._
import scala.concurrent.duration._
import scalaz._, Scalaz._
import scalaz.Validation.FlatMap._
import akka.actor._
import almhirt.common._
import almhirt.aggregates.AggregateRootId
import almhirt.almfuture.all._
import almhirt.almvalidation.kit._
import almhirt.converters.BinaryConverter
import almhirt.configuration._
import almhirt.context._
import almhirt.akkax._
import almhirt.akkax.reporting._
import almhirt.akkax.reporting.Implicits._
import reactivemongo.bson._
import reactivemongo.api._
import reactivemongo.api.indexes.{ Index ⇒ MIndex }
import reactivemongo.api.indexes.IndexType
import reactivemongo.core.commands.GetLastError
import com.typesafe.config.Config
import almhirt.reactivemongox._
import play.api.libs.iteratee._

object MongoAggregateRootEventLog {
  def propsRaw(
    db: DB with DBMetaCommands,
    collectionName: String,
    serializeAggregateRootEvent: AggregateRootEvent ⇒ AlmValidation[BSONDocument],
    deserializeAggregateRootEvent: BSONDocument ⇒ AlmValidation[AggregateRootEvent],
    writeWarnThreshold: FiniteDuration,
    readWarnThreshold: FiniteDuration,
    retrySettings: RetrySettings,
    rwMode: ReadWriteMode.SupportsReading)(implicit ctx: AlmhirtContext): Props =
    Props(new MongoAggregateRootEventLogImpl(
      db,
      collectionName,
      serializeAggregateRootEvent,
      deserializeAggregateRootEvent,
      writeWarnThreshold,
      readWarnThreshold,
      retrySettings,
      rwMode))

  def propsWithDb(
    db: DB with DBMetaCommands,
    serializeAggregateRootEvent: AggregateRootEvent ⇒ AlmValidation[BSONDocument],
    deserializeAggregateRootEvent: BSONDocument ⇒ AlmValidation[AggregateRootEvent],
    configName: Option[String] = None)(implicit ctx: AlmhirtContext): AlmValidation[Props] = {
    import almhirt.configuration._
    import almhirt.almvalidation.kit._
    val path = "almhirt.components.event-logs.aggregate-root-event-log" + configName.map("." + _).getOrElse("")
    (for {
      section ← ctx.config.v[com.typesafe.config.Config](path)
      collectionName ← section.v[String]("collection-name")
      writeWarnThreshold ← section.v[FiniteDuration]("write-warn-threshold")
      readWarnThreshold ← section.v[FiniteDuration]("read-warn-threshold")
      retrySettings ← section.v[RetrySettings]("retry-settings")
      readOnly ← section.v[Boolean]("read-only")
      rwMode ← section.v[ReadWriteMode.SupportsReading]("read-write-mode")
    } yield propsRaw(
      db,
      collectionName,
      serializeAggregateRootEvent,
      deserializeAggregateRootEvent,
      writeWarnThreshold,
      readWarnThreshold,
      retrySettings,
      rwMode)).leftMap(p ⇒ ConfigurationProblem(s"""Failed to configure MongoAggregateRootEventLog @$path.""", cause = Some(p)))
  }

  def propsWithConnection(
    connection: MongoConnection,
    serializeAggregateRootEvent: AggregateRootEvent ⇒ AlmValidation[BSONDocument],
    deserializeAggregateRootEvent: BSONDocument ⇒ AlmValidation[AggregateRootEvent],
    configName: Option[String] = None)(implicit ctx: AlmhirtContext): AlmValidation[Props] = {
    import almhirt.configuration._
    import almhirt.almvalidation.kit._
    val path = "almhirt.components.event-logs.aggregate-root-event-log" + configName.map("." + _).getOrElse("")
    for {
      section ← ctx.config.v[com.typesafe.config.Config](path)
      dbName ← section.v[String]("db-name")
      db ← inTryCatch { Await.result(connection.database(dbName)(ctx.futuresContext), 10.seconds) }
      props ← propsWithDb(
        db,
        serializeAggregateRootEvent,
        deserializeAggregateRootEvent,
        configName)
    } yield props
  }

}

private[almhirt] class MongoAggregateRootEventLogImpl(
    db: DB with DBMetaCommands,
    collectionName: String,
    serializeAggregateRootEvent: AggregateRootEvent ⇒ AlmValidation[BSONDocument],
    deserializeAggregateRootEvent: BSONDocument ⇒ AlmValidation[AggregateRootEvent],
    writeWarnThreshold: FiniteDuration,
    readWarnThreshold: FiniteDuration,
    retrySettings: RetrySettings,
    rwMode: ReadWriteMode.SupportsReading)(implicit override val almhirtContext: AlmhirtContext) extends AlmActor with AlmActorLogging with ControllableActor with StatusReportingActor {

  import almhirt.eventlog.AggregateRootEventLog._

  override val componentControl = LocalComponentControl(self, ComponentControlActions.none, Some(logWarning))

  logInfo(s"""|collectionName: $collectionName
              |read-write-mode: $rwMode""".stripMargin)

  implicit val defaultExecutor = almhirtContext.futuresContext
  val serializationExecutor = almhirtContext.futuresContext

  val projectionFilter = BSONDocument("event" → 1)

  val noSorting = BSONDocument()
  val sortByVersion = BSONDocument("version" → 1)

  private var numEventsReceived = 0L
  private var numEventsReceivedWhileUninitialized = 0L
  private val numDuplicateEventsReceived = new java.util.concurrent.atomic.AtomicLong(0L)
  private val numEventsStored = new java.util.concurrent.atomic.AtomicLong(0L)
  private val numEventsNotStored = new java.util.concurrent.atomic.AtomicLong(0L)

  private val fromBsonDocToAggregateRootEvent: Enumeratee[BSONDocument, AggregateRootEvent] =
    Enumeratee.mapM[BSONDocument] { doc ⇒ Future { documentToAggregateRootEvent(doc).resultOrEscalate }(serializationExecutor) }

  def aggregateRootEventToDocument(aggregateRootEvent: AggregateRootEvent): AlmValidation[BSONDocument] = {
    (for {
      serialized ← serializeAggregateRootEvent(aggregateRootEvent)
    } yield {
      BSONDocument(
        ("_id" → BSONString(aggregateRootEvent.eventId.value)),
        ("aggid" → BSONString(aggregateRootEvent.aggId.value)),
        ("version" → BSONLong(aggregateRootEvent.aggVersion.value)),
        ("type" → BSONString(aggregateRootEvent.getClass().getSimpleName())),
        ("event" → serialized))
    }).leftMap(p ⇒ SerializationProblem(s"""Could not serialize a "${aggregateRootEvent.getClass().getName()}".""", cause = Some(p)))
  }

  def documentToAggregateRootEvent(document: BSONDocument): AlmValidation[AggregateRootEvent] = {
    (document.get("event") match {
      case Some(d: BSONDocument) ⇒
        deserializeAggregateRootEvent(d)
      case Some(x) ⇒
        val msg = s"""Event must be contained as a BSONDocument. It is a "${x.getClass().getName()}"."""
        MappingProblem(msg).failure
      case None ⇒
        NoSuchElementProblem("""BSONDocument for payload(field name = "event") not found""").failure
    }).leftMap { p ⇒
      val docStr = BSONDocument.pretty(document)
      val msg = s"""Could not deserialize BSONDocument with field "_id"=${document.getAs[BSONValue]("_id")} as a domain event."""
      val completeMsg = "$msg\n$docStr"
      val prob = SerializationProblem(completeMsg, cause = Some(p))
      logError(prob.toString)
      prob
    }
  }

  def storeEvent(document: BSONDocument): AlmFuture[Deadline] = {
    rwMode.useForWriteOp { writeConcern ⇒
      val collection = db(collectionName)
      val start = Deadline.now
      for {
        writeResult ← collection.insert(document, writeConcern = writeConcern).toAlmFuture
        _ ← if (writeResult.ok)
          AlmFuture.successful(())
        else {
          val msg = writeResult.errmsg.getOrElse("unknown error")
          AlmFuture.failed(PersistenceProblem(msg))
        }
      } yield start
    }
  }

  def commitEvent(event: AggregateRootEvent, respondTo: ActorRef) {
    import almhirt.problem._
    (for {
      serialized ← AlmFuture { aggregateRootEventToDocument(event: AggregateRootEvent) }(serializationExecutor)
      storeRes ← storeEvent(serialized)
    } yield storeRes) onComplete (
      fail ⇒ {
        fail match {
          case ExceptionCaughtProblem(p) ⇒
            p.cause match {
              case Some(CauseIsThrowable(HasAThrowable(dbexn: reactivemongo.core.errors.DatabaseException))) ⇒
                if (dbexn.code == Some(11000)) {
                  logWarning(s"Event(${event.eventId.value}) for aggregate root ${event.aggId.value} already exists")
                  val prob = AlreadyExistsProblem(s"Event(${event.eventId.value}) for aggregate root ${event.aggId.value} already exists", cause = Some(p))
                  respondTo ! AggregateRootEventNotCommitted(event.eventId, prob)
                  numDuplicateEventsReceived.incrementAndGet()
                } else {
                  logError(s"Could not commit aggregate root event:\n$fail")
                  reportMissedEvent(event, CriticalSeverity, fail)
                  reportMajorFailure(fail)
                  respondTo ! AggregateRootEventNotCommitted(event.eventId, fail)
                  numEventsNotStored.incrementAndGet()
                }
              case _ ⇒
                logError(s"Could not commit aggregate root event:\n$fail")
                reportMissedEvent(event, CriticalSeverity, fail)
                reportMajorFailure(fail)
                respondTo ! AggregateRootEventNotCommitted(event.eventId, fail)
                numEventsNotStored.incrementAndGet()
            }
        }
      },
      start ⇒ {
        respondTo ! AggregateRootEventCommitted(event.eventId)
        numEventsStored.incrementAndGet()
        val lap = start.lap
        if (lap > writeWarnThreshold)
          logWarning(s"""Storing aggregate root event "${event.getClass().getSimpleName()}(${event.eventId})" took longer than ${writeWarnThreshold.defaultUnitString}(${lap.defaultUnitString}).""")
      })
  }

  def getAggregateRootEventsDocs(query: BSONDocument, sort: BSONDocument, traverse: TraverseWindow): Enumerator[BSONDocument] = {
    val collection = db(collectionName)
    val enumerator = collection.queryAlm(query, projectionFilter, sort, rwMode.readPreference).traverse(traverse).enumerate[BSONDocument]
    enumerator
  }

  def getAggregateRootEvents(query: BSONDocument, sort: BSONDocument, traverse: TraverseWindow): Enumerator[AggregateRootEvent] = {
    val docsEnumerator = getAggregateRootEventsDocs(query, sort, traverse)
    docsEnumerator.through(fromBsonDocToAggregateRootEvent)
  }

  def createQueryAndSort(m: AggregateRootEventLogQueryManyMessage): (BSONDocument, BSONDocument) = {
    m match {
      case m: GetAllAggregateRootEvents ⇒
        (BSONDocument.empty, BSONDocument.empty)
      case GetAggregateRootEventsFor(aggId, start, end, _) ⇒
        (start, end) match {
          case (FromStart, ToEnd) ⇒
            (BSONDocument("aggid" → BSONString(aggId.value)), sortByVersion)
          case (FromVersion(fromVersion), ToEnd) ⇒
            (BSONDocument(
              "aggid" → BSONString(aggId.value),
              "version" → BSONDocument("$gte" → BSONLong(fromVersion.value))), sortByVersion)
          case (FromStart, ToVersion(toVersion)) ⇒
            (BSONDocument(
              "aggid" → BSONString(aggId.value),
              "version" → BSONDocument("$lte" → BSONLong(toVersion.value))), sortByVersion)
          case (FromVersion(fromVersion), ToVersion(toVersion)) ⇒
            (BSONDocument(
              "aggid" → BSONString(aggId.value),
              "$and" → BSONArray(
                BSONDocument("version" → BSONDocument("$gte" → BSONLong(fromVersion.value))),
                BSONDocument("version" → BSONDocument("$lte" → BSONLong(toVersion.value))))), sortByVersion)
        }
    }
  }

  def fetchAndDispatchAggregateRootEvents(m: AggregateRootEventLogQueryManyMessage, respondTo: ActorRef) {
    val start = Deadline.now
    val (query, sort) = createQueryAndSort(m)
    val eventsEnumerator = getAggregateRootEvents(query, sort, m.traverse)
    val enumeratorWithCallback = eventsEnumerator.onDoneEnumerating({
      val lap = start.lap
      if (lap > readWarnThreshold)
        logWarning(s"""Fetching aggregate root events took longer than ${readWarnThreshold.defaultUnitString}(${lap.defaultUnitString}).""")
    })
    respondTo ! FetchedAggregateRootEvents(enumeratorWithCallback)
  }

  private case object Initialize
  private case object Initialized
  private case class InitializeFailed(prob: Problem)

  def uninitializedReadWrite: Receive = startup() {
    reportsStatusF(onReportRequested = createStatusReport(None)) {
      case Initialize ⇒
        logInfo("Initializing(read/write)")
        val toTry = () ⇒ (for {
          collectinNames ← db.collectionNames
          createonRes ← if (collectinNames.contains(collectionName)) {
            logInfo(s"""Collection "$collectionName" already exists.""")
            Future.successful(false)
          } else {
            logInfo(s"""Collection "$collectionName" does not yet exist.""")
            val collection = db(collectionName)
            collection.indexesManager.ensure(MIndex(List("aggid" → IndexType.Ascending, "version" → IndexType.Ascending), name = Some("idx_aggid_version"), unique = false))
          }
        } yield createonRes).toAlmFuture

        context.retryWithLogging[Boolean](
          retryContext = s"Initialize collection $collectionName",
          toTry = toTry,
          onSuccess = createRes ⇒ {
            logInfo(s"""Index on "aggid, version" created: $createRes""")
            self ! Initialized
          },
          onFinalFailure = (t, n, p) ⇒ {
            val prob = MandatoryDataProblem(s"Initialize collection '$collectionName' finally failed after $n attempts and ${t.defaultUnitString}.", cause = Some(p))
            self ! InitializeFailed(PersistenceProblem("Failed to initialize", cause = Some(prob)))
          },
          log = this.log,
          settings = retrySettings,
          actorName = Some("initializes-collection"))

      case Initialized ⇒
        logInfo("Initialized")
        context.become(receiveAggregateRootEventLogMsg)

      case InitializeFailed(prob) ⇒
        logError(s"Initialize failed:\n$prob")
        reportCriticalFailure(prob)
        sys.error(prob.message)

      case m: AggregateRootEventLogMessage ⇒
        val msg = s"""Received domain event log message ${m.getClass().getSimpleName()} while uninitialized."""
        logWarning(msg)
        val problem = ServiceNotReadyProblem(msg)
        m match {
          case CommitAggregateRootEvent(event) ⇒
            numEventsReceivedWhileUninitialized = numEventsReceivedWhileUninitialized + 1L
            sender ! AggregateRootEventNotCommitted(event.eventId, problem)
            reportMissedEvent(event, MajorSeverity, problem)
          case m: GetAllAggregateRootEvents ⇒
            sender ! GetAggregateRootEventsFailed(problem)
          case GetAggregateRootEvent(eventId) ⇒
            sender ! GetAggregateRootEventFailed(eventId, problem)
          case m: GetAggregateRootEventsFor ⇒
            sender ! GetAggregateRootEventsFailed(problem)
        }
    }
  }

  def uninitializedReadOnly: Receive = startup() {
    reportsStatusF(onReportRequested = createStatusReport(None)) {
      case Initialize ⇒
        logInfo("Initializing(read-only)")
        context.retryWithLogging[Unit](
          retryContext = s"Find collection $collectionName",
          toTry = () ⇒ db.collectionNames.toAlmFuture.foldV(
            fail ⇒ fail.failure,
            collectionNames ⇒ {
              if (collectionNames.contains(collectionName))
                ().success
              else
                MandatoryDataProblem(s"""Collection "$collectionName" is not among [${collectionNames.mkString(", ")}] in database "${db.name}".""").failure
            }),
          onSuccess = _ ⇒ { self ! Initialized },
          onFinalFailure = (t, n, p) ⇒ {
            val prob = MandatoryDataProblem(s"Look up collection '$collectionName' finally failed after $n attempts and ${t.defaultUnitString}.", cause = Some(p))
            self ! InitializeFailed(PersistenceProblem("Failed to initialize", cause = Some(prob)))
          },
          log = this.log,
          settings = retrySettings,
          actorName = Some("looks-for-collection"))

      case Initialized ⇒
        logInfo("Initialized")
        context.become(receiveAggregateRootEventLogMsg)

      case InitializeFailed(prob) ⇒
        logError(s"Initialize failed:\n$prob")
        reportCriticalFailure(prob)
        sys.error(prob.message)

      case m: AggregateRootEventLogMessage ⇒
        val msg = s"""Received domain event log message ${m.getClass().getSimpleName()} while uninitialized in read only mode."""
        logWarning(msg)
        val problem = ServiceNotReadyProblem(msg)
        m match {
          case CommitAggregateRootEvent(event) ⇒
            numEventsReceivedWhileUninitialized = numEventsReceivedWhileUninitialized + 1L
            sender ! AggregateRootEventNotCommitted(event.eventId, problem)
          case m: GetAllAggregateRootEvents ⇒
            sender ! GetAggregateRootEventsFailed(problem)
          case GetAggregateRootEvent(eventId) ⇒
            sender ! GetAggregateRootEventFailed(eventId, problem)
          case m: GetAggregateRootEventsFor ⇒
            sender ! GetAggregateRootEventsFailed(problem)
        }
    }
  }

  def receiveAggregateRootEventLogMsg: Receive = running() {
    reportsStatusF(onReportRequested = options ⇒ createStatusReport(Some(db.collection(collectionName).count().map(_.toLong)))(options)) {
      case CommitAggregateRootEvent(event) ⇒
        commitEvent(event, sender())

      case m: AggregateRootEventLogQueryManyMessage ⇒
        fetchAndDispatchAggregateRootEvents(m, sender())

      case GetAggregateRootEvent(eventId) ⇒
        val collection = db(collectionName)
        val query = BSONDocument("_id" → BSONString(eventId.value))
        (for {
          doc ← collection.find(query).cursor(readPreference = rwMode.readPreference).headOption.toAlmFuture
          aggregateRootEvent ← AlmFuture {
            doc match {
              case None    ⇒ None.success
              case Some(d) ⇒ documentToAggregateRootEvent(d).map(Some(_))
            }
          }(serializationExecutor)
        } yield aggregateRootEvent).mapOrRecoverThenPipeTo(
          eventOpt ⇒ FetchedAggregateRootEvent(eventId, eventOpt),
          problem ⇒ {
            logError(problem.toString())
            reportMajorFailure(problem)
            GetAggregateRootEventFailed(eventId, problem)
          })(sender())
    }
  }

  private def createStatusReport(numEvents: Option[AlmFuture[Long]])(options: StatusReportOptions): AlmFuture[StatusReport] = {
    val baseRep = StatusReport("AggregateEventLog").withComponentState(componentState) addMany (
      "number-of-events-received" -> numEventsReceived,
      "number-of-events-received-while-uninitialized" -> numEventsReceivedWhileUninitialized,
      "number-of-duplicate-events-received" -> numDuplicateEventsReceived.get,
      "number-of-events-stored" -> numEventsStored.get,
      "number-of-events-not-stored" -> numEventsNotStored.get,
      "collection-name" -> collectionName)

    for {
      totalEvents ← numEvents match {
        case None      ⇒ AlmFuture.successful(None)
        case Some(fut) ⇒ fut.materializedValidation.map(Some(_))
      }
    } yield baseRep ~ ("events-persisted" -> totalEvents)
  }

  override def receive =
    if (rwMode.supportsWriting)
      uninitializedReadWrite
    else
      uninitializedReadOnly

  override def preStart() {
    super.preStart()
    registerComponentControl()
    registerStatusReporter(description = Some("AggregateEventLog based on MongoDB"))
    context.parent ! ActorMessages.ConsiderMeForReporting
    self ! Initialize
  }

  override def postStop() {
    super.postStop()
    deregisterComponentControl()
    deregisterStatusReporter()
  }

}
