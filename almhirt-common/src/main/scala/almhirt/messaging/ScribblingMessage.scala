/* Copyright 2012 Christian Douven

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
*/
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
