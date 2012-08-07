package almhirt.messaging

import almhirt.Scribbler
import almhirt.xtractnduce.NDuceScribe

trait ScribblingMessage[+TPayload <: AnyRef] extends Scribbler { self: Message[TPayload] =>
  def scribble() = {
    val envelope =
      NDuceScribe.scribble("Message")
        .setUUID("id", id)
        .setElement("grouping", grouping.map{ grp => 
          NDuceScribe.scribble("MesssgeGrouping")
            .setUUID("groupId", grp.groupId)
            .setInt("seq", grp.seq)
            .setBoolean("isLast", grp.isLast)})
        .setElements(
            "metaData", 
            metaData.toSeq.map{case (k,v) => 
              NDuceScribe.scribble("KeyValue")
                .setString("key", k)
                .setString("value", v)}: _*)
        .setString("topic", topic)
    payload match {
      case s: Scribbler => envelope.setElement("payload", s.scribble())
      case _ => sys.error("Payload cannot be scribbled!")
    }
  }
}

