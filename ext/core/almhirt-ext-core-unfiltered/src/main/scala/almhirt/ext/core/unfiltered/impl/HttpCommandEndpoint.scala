package almhirt.ext.core.unfiltered.impl

import almhirt.common._
import almhirt.environment.Almhirt
import almhirt.util.CommandEndpoint
import almhirt.ext.core.unfiltered._
import unfiltered.request.HttpRequest
import unfiltered.netty.ReceivedMessage

abstract class HttpCommandEndpoint(getEndpoint: () => AlmValidation[CommandEndpoint], theAlmhirt: Almhirt) extends ForwardsCommandsFromHttpRequest {
  
  
  def forward(req: HttpRequest[ReceivedMessage]) {
  }
  
  def forwardTracked(req: HttpRequest[ReceivedMessage]) {
    
  }
  
  def forwardWithResultResponse(req: HttpRequest[ReceivedMessage]) {
    
  }

}