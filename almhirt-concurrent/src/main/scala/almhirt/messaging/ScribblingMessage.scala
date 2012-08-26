package almhirt.messaging

import almhirt.xtractnduce.{Scribbler, NDuceScribe}

//trait ScribblingMessage[+TPayload <: AnyRef] extends Scribbles { self: Message[TPayload] =>
//  def scribble(implicit payloadScribbler: Scribbler[TPayload]) = {
//    val envelope =
//      NDuceScribe.scribble("Message")
//        .setUUID("id", id)
//        .setElement("grouping", grouping.map{ grp => 
//          NDuceScribe.scribble("MesssgeGrouping")
//            .setUUID("groupId", grp.groupId)
//            .setInt("seq", grp.seq)
//            .setBoolean("isLast", grp.isLast)})
//        .setElements(
//            "metaData", 
//            metaData.toSeq.map{case (k,v) => 
//              NDuceScribe.scribble("KeyValue")
//                .setString("key", k)
//                .setString("value", v)}: _*)
//        .setString("topic", topic)
//    }
//  }
//}
//
