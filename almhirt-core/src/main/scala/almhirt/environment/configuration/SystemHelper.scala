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
    ConfigHelper.getSubConfig(config)(ConfigPaths.bootstrapper).bind(subConf =>
      ConfigHelper.getString(subConf)(ConfigPaths.bootstrapperClassName).bind(className =>
        inTryCatch {
          val constructor = Class.forName(className).getConstructors()(0)
          val instance = constructor.newInstance(Array[AnyRef](config))
          instance.asInstanceOf[AlmhirtBootstrapper]
        }))
  }

  def createEventLogFromFactory(ctx: AlmhirtContext, system: AlmhirtSystem): AlmValidation[DomainEventLog] = {
    ConfigHelper.getFactoryName(system.config)(ConfigPaths.eventlog).bind(factoryName =>
      inTryCatch(Class.forName(factoryName).newInstance().asInstanceOf[{ def createDomainEventLog(ctx: AlmhirtContext): AlmValidation[DomainEventLog] }]).bind(factory =>
        factory.createDomainEventLog(ctx)))
  }

}