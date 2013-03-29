package almhirt.environment.configuration

import scalaz.std._
import akka.actor._
import almhirt.common.AlmValidation
import almhirt.almvalidation.funs._
import almhirt.eventlog._
import almhirt.environment._
import almhirt.common.AlmValidation
import almhirt.util.CommandEndpoint
import almhirt.core.Almhirt
import almhirt.common.UnspecifiedProblem
import almhirt.common.CauseIsProblem
import com.typesafe.config.Config

object SystemHelper {
  def addDispatcherToPropsFromComponentPath(rootConfig: Config)(pathToComponentConfig: String)(props: Props): AlmValidation[Props] =
    ConfigHelper.getSubConfig(rootConfig)(pathToComponentConfig).flatMap(componentConfig =>
      addDispatcherToPropsFromComponentConfig(componentConfig)(props))

  def addDispatcherToPropsFromComponentConfig(componentConfig: Config)(props: Props): AlmValidation[Props] =
    ConfigHelper.getString(componentConfig)(ConfigItems.dispatchername).map(props.withDispatcher(_))

  def addDispatcherByNameToProps(dispatcherName: Option[String])(props: Props): Props =
    option.cata(dispatcherName)(
      dn => props.withDispatcher(dn),
      props)

  def createBootstrapperFromConfig(config: Config): AlmValidation[Bootstrapper] = {
    ConfigHelper.getSubConfig(config)(ConfigPaths.bootstrapper).flatMap(subConf =>
      ConfigHelper.getString(subConf)(ConfigItems.className).flatMap(className =>
        inTryCatch {
          val constructor = Class.forName(className).getConstructors()(0)
          val instance = constructor.newInstance(config)
          instance.asInstanceOf[Bootstrapper]
        }))
  }

  def createEventLogFromFactory(implicit theAlmhirt: Almhirt): AlmValidation[ActorRef] = {
    import language.reflectiveCalls
    for {
      theConfig <- theAlmhirt.getConfig
      eventLogConfig <- ConfigHelper.eventLog.getConfig(theConfig)
      factoryName <- ConfigHelper.shared.getFactoryNameFromComponentConfig(eventLogConfig)
      factory <- inTryCatch {
        theAlmhirt.log.info(s"Creating EventLog using factory '$factoryName'")
        Class.forName(factoryName)
          .newInstance()
          .asInstanceOf[{ def createEventLog(x: Almhirt): AlmValidation[ActorRef] }]
      }
      eventLog <- factory.createEventLog(theAlmhirt)
    } yield eventLog
  }
  
  def createDomainEventLogFromFactory(implicit theAlmhirt: Almhirt): AlmValidation[ActorRef] = {
    import language.reflectiveCalls
    for {
      theConfig <- theAlmhirt.getConfig
      eventLogConfig <- ConfigHelper.domainEventLog.getConfig(theConfig)
      factoryName <- ConfigHelper.shared.getFactoryNameFromComponentConfig(eventLogConfig)
      factory <- inTryCatch {
        theAlmhirt.log.info(s"Creating DomainEventLog using factory '$factoryName'")
        Class.forName(factoryName)
          .newInstance()
          .asInstanceOf[{ def createDomainEventLog(x: Almhirt): AlmValidation[ActorRef] }]
      }
      eventLog <- factory.createDomainEventLog(theAlmhirt)
    } yield eventLog
  }

  def createAggregateRootCacheFromFactory(implicit theAlmhirt: Almhirt): AlmValidation[ActorRef] = {
    import language.reflectiveCalls
    for {
      theConfig <- theAlmhirt.getConfig
      configSection <- ConfigHelper.aggregateRootCache.getConfig(theConfig)
      factoryName <- ConfigHelper.shared.getFactoryNameFromComponentConfig(configSection)
      factory <- inTryCatch {
        theAlmhirt.log.info(s"Creating AggregateRootCache using factory '$factoryName'")
        Class.forName(factoryName)
          .newInstance()
          .asInstanceOf[{ def createAggregateRootCache(x: Almhirt): AlmValidation[ActorRef] }]
      }
      eventLog <- factory.createAggregateRootCache(theAlmhirt)
    } yield eventLog
  }

  def createSnapshotStorageFromFactory(implicit theAlmhirt: Almhirt): AlmValidation[ActorRef] = {
    import language.reflectiveCalls
    for {
      theConfig <- theAlmhirt.getConfig
      configSection <- ConfigHelper.snapshotStorage.getConfig(theConfig)
      factoryName <- ConfigHelper.shared.getFactoryNameFromComponentConfig(configSection)
      factory <- inTryCatch {
        theAlmhirt.log.info(s"Creating SnapshotStorage using factory '$factoryName'")
        Class.forName(factoryName)
          .newInstance()
          .asInstanceOf[{ def createSnapshotStorage(x: Almhirt): AlmValidation[ActorRef] }]
      }
      eventLog <- factory.createSnapshotStorage(theAlmhirt)
    } yield eventLog
  }
   
  def createOperationStateTrackerFromFactory(implicit theAlmhirt: Almhirt): AlmValidation[ActorRef] = {
    import language.reflectiveCalls
    for {
      theConfig <- theAlmhirt.getConfig
      opStateConfig <- ConfigHelper.operationState.getConfig(theConfig)
      factoryName <- ConfigHelper.shared.getFactoryNameFromComponentConfig(opStateConfig)
      factory <- inTryCatch {
        theAlmhirt.log.info(s"Creating OperationStateTracker using factory '$factoryName'")
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
      theConfig <- theAlmhirt.getConfig
      endpointConfig <- ConfigHelper.commandEndpoint.getConfig(theConfig)
      factoryName <- ConfigHelper.shared.getFactoryNameFromComponentConfig(endpointConfig)
      factory <- inTryCatch {
        theAlmhirt.log.info(s"Creating CommandEndpoint using factory '$factoryName'")
        Class.forName(factoryName)
          .newInstance()
          .asInstanceOf[{ def createCommandEndpoint(theAlmhirt: Almhirt): AlmValidation[CommandEndpoint] }]
      }
      endpoint <- factory.createCommandEndpoint(theAlmhirt)
    } yield endpoint
  }

  def createCommandDispatcherFromFactory(implicit theAlmhirt: Almhirt): AlmValidation[almhirt.client.CommandDispatcher] = {
    import language.reflectiveCalls
    val res =
      for {
        theConfig <- theAlmhirt.getConfig
        dispatcherConfig <- ConfigHelper.commandDispatcher.getConfig(theConfig)
        factoryName <- ConfigHelper.shared.getFactoryNameFromComponentConfig(dispatcherConfig)
        factory <- inTryCatch {
          theAlmhirt.log.info(s"Creating CommandDispatcher using factory '$factoryName'")
          Class.forName(factoryName)
            .newInstance()
            .asInstanceOf[{ def createCommandDispatcher(theAlmhirt: Almhirt): AlmValidation[almhirt.client.CommandDispatcher] }]
        }
        dispatcher <- factory.createCommandDispatcher(theAlmhirt)
      } yield dispatcher
    res.bimap(prob => new UnspecifiedProblem("Could not create a command endpoint from factory", cause = Some(CauseIsProblem(prob))), g => g)
  }

}