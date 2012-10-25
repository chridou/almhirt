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
package almhirt.almakka

import akka.actor.ActorSystem
import akka.dispatch.MessageDispatcher
import akka.util.duration._
import com.typesafe.config._

trait StandardAlmAkkaComponent extends AlmAkkaComponent {
  val almAkkaContext: AlmAkkaContext = new AlmAkkaByConfig()
  
  private class AlmAkkaByConfig() extends AlmAkkaContext {
    val config = ConfigFactory.load
    val actorSystem = ActorSystem(config.getString("almhirt.systemname"))
    val futureDispatcher = actorSystem.dispatchers.lookup("almhirt.future-dispatcher")
    val messageStreamDispatcherName = Some("almhirt.messagestream-dispatcher")
    val messageHubDispatcherName = Some("almhirt.messagehub-dispatcher")
    val shortDuration = config.getDouble("almhirt.durations.short") seconds
    val mediumDuration = config.getDouble("almhirt.durations.medium") seconds
    val longDuration = config.getDouble("almhirt.durations.long") seconds
    def dispose = actorSystem.shutdown
  }
}

