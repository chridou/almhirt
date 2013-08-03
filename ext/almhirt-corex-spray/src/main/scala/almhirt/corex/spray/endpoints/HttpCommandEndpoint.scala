package almhirt.corex.spray.endpoints

import scala.language.postfixOps
import almhirt.common._
import almhirt.components.CommandEndpointWrapper
import spray.routing._
import spray.http._
import spray.routing.directives._

trait HttpCommandEndpoint extends HttpService  {
  def endpoint: CommandEndpointWrapper
  def maxSyncDuration: scala.concurrent.duration.FiniteDuration
  implicit def executionContext: scala.concurrent.ExecutionContext
  
  val execute = pathPrefix("execute") & (get & parameters('tracked ?, 'sync ?))
  
  val route = execute {(tracked, sync) => ctx =>
  	val cmd: Command = null
  	(tracked, sync) match {
  	  case (None, None) => 
  	    endpoint.execute(cmd)
  	    complete{(StatusCodes.Accepted, "")}
  	  case (Some(_), _) => 
  	    val trackId = endpoint.executeTracked(cmd)
  	    complete{(StatusCodes.Accepted, trackId)}
  	  case (_, Some(_)) => 
  	    endpoint.executeSync(cmd, maxSyncDuration).fold(
  	      prob => 
  	        ctx.complete(StatusCodes.InternalServerError, prob.message),
  	      res => 
  	        ctx.complete(StatusCodes.OK) )
  	}
  }
}