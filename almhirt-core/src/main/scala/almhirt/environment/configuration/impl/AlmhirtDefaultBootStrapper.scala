//package almhirt.environment.configuration.impl
//
//import scala.concurrent.duration.FiniteDuration
//import scalaz.syntax.validation._
//import akka.event.LoggingAdapter
//import akka.actor._
//import akka.util.Timeout._
//import almhirt.common._
//import almhirt.almvalidation.kit._
//import almhirt.almfuture.all._
//import almhirt.environment._
//import almhirt.environment.configuration.AlmhirtBootstrapper
//import almhirt.util._
//import almhirt.messaging._
//import almhirt.parts._
//import almhirt.eventlog.DomainEventLog
//import almhirt.commanding.CommandEnvelope
//import almhirt.environment.configuration.SystemHelper
//import com.typesafe.config.Config
//import almhirt.environment.configuration.CleanUpAction
//import almhirt.eventlog.impl.DomainEventLogActorHull
//import almhirt.environment.configuration.ConfigHelper
//import almhirt.environment.configuration.ConfigPaths
//import almhirt.util.impl.OperationStateTrackerActorHull
//import almhirt.core.ServiceRegistry
//import almhirt.core.Almhirt
//import almhirt.core.HasConfig
//
//trait BootstrapperDefaultCoreComponents extends AlmhirtBootstrapper { self: HasConfig =>
//  private var trackerRegistration: RegistrationHolder = null
//  private var repos: HasRepositories = null
//  private var cmdHandlerRegistry: HasCommandHandlers = null
//  private var cmdExecutor: CommandExecutor = null
//  private var cmdExecutorRegistration: RegistrationHolder = null
//
//  implicit val atMost = FiniteDuration(5, "s")
//
//  private var problemLogger: ActorRef = null
//
//  override def createCoreComponents(theAlmhirt: Almhirt, theServiceRegistry: ServiceRegistry, startUpLogger: LoggingAdapter): AlmValidation[CleanUpAction] = {
//    super.createCoreComponents(theAlmhirt, theServiceRegistry, startUpLogger).flatMap { superCleanUp =>
//      startUpLogger.info("Creating default core components...")
//      import akka.pattern._
//      implicit val anAlmhirt = theAlmhirt
//      implicit val executionContext = theAlmhirt.executionContext
//
//      inTryCatch {
//        ConfigHelper.getSubConfig(config)(ConfigPaths.operationState).foreach { subConf =>
//          startUpLogger.info(s"Create operation state tracker ...")
//          val tracker = SystemHelper.createOperationStateTrackerFromFactory.forceResult
//          trackerRegistration =
//            theAlmhirt.getService[OperationStateChannel].flatMap(channel =>
//              (channel.actor ? SubscribeQry(MessagingSubscription.forActor[OperationState](tracker)))(atMost)
//                .mapTo[SubscriptionRsp]
//                .map(_.registration)
//                .toAlmFuture
//                .awaitResult)
//              .forceResult
//          startUpLogger.info(s"Register operation state tracker")
//          theServiceRegistry.registerService[OperationStateTracker](almhirt.util.impl.OperationStateTrackerActorHull(tracker))
//        }
//
//        startUpLogger.info(s"Create HasRepositories")
//        repos = HasRepositories()
//        startUpLogger.info(s"Register HasRepositories")
//        theServiceRegistry.registerService[HasRepositories](repos)
//
//        startUpLogger.info(s"Create HasCommandHandlers")
//        cmdHandlerRegistry = HasCommandHandlers()
//        startUpLogger.info(s"Register HasCommandHandlers")
//        theServiceRegistry.registerService[HasCommandHandlers](cmdHandlerRegistry)
//
//        startUpLogger.info(s"Create CommandExecutor")
//        cmdExecutor = CommandExecutor(cmdHandlerRegistry, repos)
//        startUpLogger.info(s"Register CommandExecutor as listener to CommandChannel")
//        cmdExecutorRegistration =
//          theAlmhirt.getService[CommandChannel].flatMap(channel =>
//            (channel.actor ? SubscribeQry(MessagingSubscription.forActor[CommandEnvelope](cmdExecutor.actor)))(atMost)
//              .mapTo[SubscriptionRsp]
//              .map(_.registration)
//              .toAlmFuture
//              .awaitResult)
//            .forceResult
//        startUpLogger.info(s"Register CommandExecutor")
//        theServiceRegistry.registerService[CommandExecutor](cmdExecutor)
//        ConfigHelper.getSubConfig(config)(ConfigPaths.domaineventlog).foreach { _ =>
//          startUpLogger.info(s"Create DomainEventLog")
//          val eventLogActor = SystemHelper.createEventLogFromFactory.forceResult
//          startUpLogger.info(s"Register DomainEventLog")
//          theServiceRegistry.registerService[DomainEventLog](DomainEventLogActorHull(eventLogActor, config))
//        }
//        ConfigHelper.getSubConfig(config)(ConfigPaths.commandEndpoint).foreach { _ =>
//          startUpLogger.info(s"Create CommandEndpoint")
//          val endpoint = SystemHelper.createCommandEndpointFromFactory.forceResult
//          startUpLogger.info(s"Register CommandEndpoint")
//          theServiceRegistry.registerService[CommandEndpoint](endpoint)
//        }
//        ConfigHelper.getSubConfig(config)(ConfigPaths.problems).foreach { subConf =>
//          startUpLogger.info("Create ProblemLogger")
//          val minSeverity =
//            ConfigHelper.problems.minSeverity(subConf).fold(
//              fail => {
//                startUpLogger.warning(s"Could not determine minSeverity: ${fail.message}. Using Minor as minSeverity")
//                Minor
//              },
//              succ => succ)
//          val actorName = ConfigHelper.problems.getActorName(subConf)
//          problemLogger = theAlmhirt.actorSystem.actorOf(Props(new almhirt.util.impl.ProblemLogger(minSeverity)), actorName)
//          startUpLogger.info(s"ProblemLogger has path ${problemLogger.path.toString()}")
//        }
//
//        (() => {
//          startUpLogger.info(s"Dispose CommandExecutorRegistration")
//          cmdExecutorRegistration.dispose
//          startUpLogger.info(s"Dispose operation state tracker")
//          trackerRegistration.dispose
//          superCleanUp();
//        })
//      }
//    }
//  }
//
//  override def initializeCoreComponents(theAlmhirt: Almhirt, theServiceRegistry: ServiceRegistry, startUpLogger: LoggingAdapter): AlmValidation[CleanUpAction] =
//    super.initializeCoreComponents(theAlmhirt, theServiceRegistry, startUpLogger).flatMap { superCleanUp =>
//      if (problemLogger != null) {
//        theServiceRegistry.getService[ProblemChannel].fold(
//          fail => {
//            startUpLogger.warning("We have a ProblemLogger but no ProblemChannel. You will have to deliver your problems directly to the ProblemLogger if you want your problems to be logged.")
//            superCleanUp.success
//            },
//          probChannel => {
//            startUpLogger.info("Found a problem channel. ProblemLogger will be a listener to ProblemChannel")
//            val registration = (probChannel <-<# ((prob: Problem) => problemLogger ! prob)).awaitResult.forceResult
//            (() => { superCleanUp(); registration.dispose() }).success
//          })
//      } else
//        superCleanUp.success
//    }
//}