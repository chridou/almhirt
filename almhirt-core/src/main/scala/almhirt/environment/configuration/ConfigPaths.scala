package almhirt.environment.configuration

object ConfigPaths {
  val bootstrapper = "almhirt.bootstrapper"
  val messagehub = "almhirt.messagehub"
  val messagechannels = "almhirt.messagechannels"
  val eventlog = "almhirt.eventlog"
  val domaineventlog = "almhirt.domaineventlog"
  val commandexecutor = "almhirt.commandexecutor"
  val repositories = "almhirt.repositories"
  val operationState = "almhirt.operationstate"
  val commandEndpoint = "almhirt.commandendpoint"
  val commandDispatcher = "almhirt.client.commanddispatcher"
    
  val futures = "almhirt.futures"
  val cruncher = "almhirt.cruncher"
    
  val http = "almhirt.http"
  val problems = "almhirt.problems"

}

object ConfigItems {
  val className = "class"
  val factory = "factory"
  val dispatchername = "dispatchername"
  val actorName = "name"
}