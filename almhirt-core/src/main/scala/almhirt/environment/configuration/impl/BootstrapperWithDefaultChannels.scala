//package almhirt.environment.configuration.impl
//
//import scala.concurrent.duration.Duration
//import scalaz.syntax.validation._
//import akka.event.LoggingAdapter
//import almhirt.common._
//import almhirt.core._
//import almhirt.environment._
//import almhirt.environment.configuration._
//import almhirt.commanding.CommandEnvelope
//import almhirt.util.OperationState
//import almhirt.environment.CommandChannel
//import almhirt.domain.DomainEvent
//
//trait BootstrapperWithDefaultChannels extends AlmhirtBootstrapper{
//  override def createCoreComponents(theAlmhirt: Almhirt, theServiceRegistry: ServiceRegistry, startUpLogger: LoggingAdapter): AlmValidation[CleanUpAction] = {
//    super.createCoreComponents(theAlmhirt, theServiceRegistry, startUpLogger).flatMap { superCleanUp =>
//      implicit val dur = Duration(1, "s")
//      implicit val hasExecContext = theAlmhirt
//      startUpLogger.info("Create CommandChannel, OperationStateChannel, DomainEventsChannel, ProblemsChannel")
//      val channels =
//        (for {
//          commandChannel <- theAlmhirt.messageHub.createMessageChannel[CommandEnvelope]("CommandChannel")
//          operationStateChannel <- theAlmhirt.messageHub.createMessageChannel[OperationState]("OperationStateChannel")
//          domainEventsChannel <- theAlmhirt.messageHub.createMessageChannel[DomainEvent]("DomainEventsChannel")
//          problemsChannel <- theAlmhirt.messageHub.createMessageChannel[Problem]("ProblemsChannel")
//        } yield (
//          new CommandChannelWrapper(commandChannel),
//          new OperationStateChannelWrapper(operationStateChannel),
//          new DomainEventsChannelWrapper(domainEventsChannel),
//          new ProblemChannelWrapper(problemsChannel))).awaitResult
//
//      channels.foreach { x =>
//        startUpLogger.info("Register CommandChannel")
//        theServiceRegistry.registerService[CommandChannel](x._1)
//        startUpLogger.info("Register OperationStateChannel")
//        theServiceRegistry.registerService[OperationStateChannel](x._2)
//        startUpLogger.info("Register DomainEventsChannel")
//        theServiceRegistry.registerService[DomainEventsChannel](x._3)
//        startUpLogger.info("Register ProblemsChannel")
//        theServiceRegistry.registerService[ProblemChannel](x._4)
//      }
//      (superCleanUp).success
//    }
//  }
//
//}