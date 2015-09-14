package almhirt.akkax

import scala.reflect.ClassTag
import scala.concurrent.ExecutionContext
import scala.concurrent.duration._
import akka.pattern._
import almhirt.common._
import almhirt.almfuture.all._
import almhirt.herder.HerderMessages.StatusReportMessages
import almhirt.herder.HerderMessages

trait StatusReportingActor { me: AlmActor ⇒
  final def registerStatusReporter(reporter: almhirt.herder.StatusReporter)(implicit cnp: ActorComponentIdProvider): Unit =
    almhirtContext.tellHerder(StatusReportMessages.RegisterStatusReporter(cnp.componentId, reporter))

  final def registerStatusReporterPF[T: ClassTag](requestReportMsg: Any, extractReportPF: PartialFunction[T, AlmValidation[almhirt.herder.StatusReport]])(implicit cnp: ActorComponentIdProvider, executor: ExecutionContext): Unit = {
    val reporter = almhirt.herder.StatusReporter(getReport = () ⇒ {
      (self ? requestReportMsg)(5.seconds).mapCastTo[T].mapV(extractReportPF)
    })
    this.registerStatusReporter(reporter)(cnp)
  }

  final def deregisterStatusReporter()(implicit cnp: ActorComponentIdProvider): Unit =
    almhirtContext.tellHerder(StatusReportMessages.DeregisterStatusReporter(cnp.componentId))
}