package almhirt.messaging

// Request creation of a message stream Filter = None means: Subscribe only to those messages that don't have a topic att all
case class CreateMessageStreamCommand(topicFilter: Option[String])