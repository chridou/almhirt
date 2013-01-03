package almhirt.environment.configuration

import akka.actor._
import com.typesafe.config.Config
import almhirt.common.AlmValidation
import almhirt.almvalidation.funs._
import almhirt.eventlog._
import almhirt.environment._
import almhirt.common.AlmValidation

object SystemHelper {
  def addDispatcherToProps(config: Config)(props: Props): Props = {
    ConfigHelper.tryGetString(config)(ConfigItems.dispatchername) match {
      case None => props
      case Some(dn) => props.withDispatcher(dn)
    }
  }

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
      eventLogConfig <- ConfigHelper.eventLog.getConfig(theAlmhirt.system.config)
      factoryName <- ConfigHelper.shared.getFactoryName(eventLogConfig)
      factory <- inTryCatch(
        Class.forName(factoryName)
          .newInstance()
          .asInstanceOf[{ def createDomainEventLog(x: Almhirt): AlmValidation[ActorRef] }])
      eventLog <- factory.createDomainEventLog(theAlmhirt)
    } yield eventLog
  }

}