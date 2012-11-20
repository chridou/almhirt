package almhirt.environment.configuration

import akka.actor._
import com.typesafe.config.Config
import almhirt.common.AlmValidation
import almhirt.almvalidation.funs._
import almhirt.eventlog._
import almhirt.environment.AlmhirtContext

object SystemHelper {
  def addDispatcherToProps(config: Config)(path: String, props: Props): Props = {
    ConfigHelper.tryGetDispatcherName(config)(path) match {
      case None => props
      case Some(dn) => props.withDispatcher(dn)
    }
  }

  def createEventLogFromFactory(ctx: AlmhirtContext): AlmValidation[DomainEventLog] = {
    ConfigHelper.getFactoryName(ctx.config)(ConfigPaths.eventlog).bind(factoryName =>
      inTryCatch(Class.forName(factoryName).newInstance().asInstanceOf[{ def createDomainEventLog(ctx: AlmhirtContext): AlmValidation[DomainEventLog] }]).bind(factory =>
        factory.createDomainEventLog(ctx)))
  }
}