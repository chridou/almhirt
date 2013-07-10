package almhirt.common

trait EventConsumer extends Consumer[Event]
trait CommandConsumer extends Consumer[Command]
trait ProblemConsumer extends Consumer[Problem]