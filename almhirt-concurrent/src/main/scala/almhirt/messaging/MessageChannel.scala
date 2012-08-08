package almhirt.messaging

trait MessageChannel extends MessageStream with CanDeliverMessages with CanCreateSubChannels {

}