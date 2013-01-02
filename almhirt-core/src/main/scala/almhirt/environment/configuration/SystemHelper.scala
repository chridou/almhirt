package almhirt.environment.configuration

import akka.actor._
import com.typesafe.config.Config
import almhirt.common.AlmValidation
import almhirt.almvalidation.funs._
import almhirt.eventlog._
import almhirt.environment._
import almhirt.common.AlmValidation

object SystemHelper {
  def addDispatcherToProps(config: Config)(path: String, props: Props): Props = {
    ConfigHelper.tryGetDispatcherName(config)(path) match {
      case None => props
      case Some(dn) => props.withDispatcher(dn)
    }
  }

  def createBootstrapperFromConfig(config: Config): AlmValidation[AlmhirtBootstrapper] = {
    ConfigHelper.getSubConfig(config)(ConfigPaths.bootstrapper).flatMap(subConf =>
      ConfigHelper.getString(subConf)(ConfigPaths.bootstrapperClassName).flatMap(className =>
        inTryCatch {
          val constructor = Class.forName(className).getConstructors()(0)
          val instance = constructor.newInstance(config)
          instance.asInstanceOf[AlmhirtBootstrapper]
        }))
  }

  def createEventLogFromFactory(implicit theAlmhirt: Almhirt): AlmValidation[ActorRef] = {
    import language.reflectiveCalls
    ConfigHelper.getFactoryName(theAlmhirt.system.config)(ConfigPaths.eventlog).flatMap(factoryName =>
      inTryCatch(
        Class.forName(factoryName)
          .newInstance()
          .asInstanceOf[{ def createDomainEventLog(x: Almhirt): AlmValidation[ActorRef] }]).flatMap(factory =>
          factory.createDomainEventLog(theAlmhirt)))
  }
  
}