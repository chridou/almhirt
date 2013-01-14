package riftwarp.components

import almhirt.common._
import riftwarp._

trait KnowsChannels {
  def getChannel(ident: String): AlmValidation[RiftChannel]
  def lookUpFromHttpContentType(contentType: String): AlmValidation[RiftChannel with RiftHttpChannel] 
}