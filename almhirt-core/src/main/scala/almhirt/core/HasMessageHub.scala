package almhirt.core

import almhirt.messaging.MessageHub

trait HasMessageHub {
  def messageHub: MessageHub
}