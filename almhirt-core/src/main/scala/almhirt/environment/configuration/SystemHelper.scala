package almhirt.environment.configuration

import akka.actor._
import com.typesafe.config.Config
import almhirt.common.AlmValidation
import almhirt.almvalidation.funs._
import almhirt.eventlog._
import almhirt.environment._
import almhirt.common.AlmValidation
import almhirt.util.CommandEndpoint

object SystemHelper {
  def addDispatcherToProps(config: Config)(props: Props): Props =
    ConfigHelper.getString(config)(ConfigItems.dispatchername).fold(
      _ => props,
      dn => props.withDispatcher(dn))

  def createBootstrapperFromConfig(config: Config): AlmValidation[AlmhirtBootstrapper] = {
    ConfigHelper.getSubConfig(config)(ConfigPaths.bootstrapper).flatMap(subConf =>
      ConfigHelper.getString(subConf)(ConfigItems.className).flatMap(className =>
        inTryCatch {
          val constructor = Class.forName(className).getConstructors()(0)
          val instance = constructor.newInstance(config)
          instance.asInstanceOf[AlmhirtBootstrapper]
        }))
  }

  def createEventLogFromFactory(implicit theAlmhirt: Almhirt): AlmValidation[ActorRef] = {
    import language.reflectiveCalls
    for {
      eventLogConfig <- ConfigHelper.eventLog.getConfig(theAlmhirt.config)
      factoryName <- ConfigHelper.shared.getFactoryName(eventLogConfig)
      factory <- inTryCatch(
        Class.forName(factoryName)
          .newInstance()
          .asInstanceOf[{ def createDomainEventLog(x: Almhirt): AlmValidation[ActorRef] }])
      eventLog <- factory.createDomainEventLog(theAlmhirt)
    } yield eventLog
  }

  def createOperationStateTrackerFromFactory(implicit theAlmhirt: Almhirt): AlmValidation[ActorRef] = {
    import language.reflectiveCalls
    for {
      opStateConfig <- ConfigHelper.operationState.getConfig(theAlmhirt.config)
      factoryName <- ConfigHelper.shared.getFactoryName(opStateConfig)
      factory <- inTryCatch {
        Class.forName(factoryName)
          .newInstance()
          .asInstanceOf[{ def createOperationStateTracker(x: Almhirt): AlmValidation[ActorRef] }]
      }
      tracker <- factory.createOperationStateTracker(theAlmhirt)
    } yield tracker
  }

  def createCommandEndpointFromFactory(implicit theAlmhirt: Almhirt): AlmValidation[CommandEndpoint] = {
    import language.reflectiveCalls
    for {
      endpointConfig <- ConfigHelper.commandEndpoint.getConfig(theAlmhirt.config)
      factoryName <- ConfigHelper.shared.getFactoryName(endpointConfig)
      factory <- inTryCatch {
        Class.forName(factoryName)
          .newInstance()
          .asInstanceOf[{ def createCommandEndpoint(theAlmhirt: Almhirt): AlmValidation[CommandEndpoint] }]
      }
      endpoint <- factory.createCommandEndpoint(theAlmhirt)
    } yield endpoint
  }

  def createCommandDispatcherFromFactory(implicit theAlmhirt: Almhirt): AlmValidation[almhirt.client.CommandDispatcher] = {
    import language.reflectiveCalls
    for {
      dispatcherConfig <- ConfigHelper.commandDispatcher.getConfig(theAlmhirt.config)
      factoryName <- ConfigHelper.shared.getFactoryName(dispatcherConfig)
      factory <- inTryCatch {
        Class.forName(factoryName)
          .newInstance()
          .asInstanceOf[{ def createCommandDispatcher(theAlmhirt: Almhirt): AlmValidation[almhirt.client.CommandDispatcher] }]
      }
      dispatcher <- factory.createCommandDispatcher(theAlmhirt)
    } yield dispatcher
  }

}