package almhirt.ext.core.unfiltered

import almhirt.common._
import almhirt.syntax.almvalidation._
import unfiltered.request._
import unfiltered.netty.ReceivedMessage

object UnfilteredFuns {
  def getContentType(req: HttpRequest[ReceivedMessage]): AlmValidation[String] =
    RequestContentType(req).noneIsBadData("ContentType")
}