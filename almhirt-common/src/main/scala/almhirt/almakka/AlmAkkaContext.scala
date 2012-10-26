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

import scalaz.syntax.validation._
import almhirt._
import akka.actor.ActorSystem
import akka.dispatch.MessageDispatcher
import akka.util.Duration
import akka.util.duration._
import com.typesafe.config._

/** Components and values needed to use Akka */
trait AlmAkkaContext extends Disposable{
  def config: Config
  def actorSystem: ActorSystem
  def futureDispatcher: MessageDispatcher
  def messageStreamDispatcherName: Option[String]
  def messageHubDispatcherName: Option[String]
  def shortDuration: Duration
  def mediumDuration: Duration
  def longDuration: Duration
  def generateUuid: java.util.UUID
}

object AlmAkkaContext {
  def apply(config: Config): AlmAkkaContext = {
    val uuidGen = new JavaUtilUuidGenerator()
    val ctx =
	  new AlmAkkaContext {
	    val config = ConfigFactory.load
	    val actorSystem = ActorSystem(config.getString("almhirt.systemname"))
	    val futureDispatcher = actorSystem.dispatchers.lookup("almhirt.future-dispatcher")
	    val messageStreamDispatcherName = Some("almhirt.messagestream-dispatcher")
	    val messageHubDispatcherName = Some("almhirt.messagehub-dispatcher")
	    val shortDuration = config.getDouble("almhirt.durations.short") seconds
	    val mediumDuration = config.getDouble("almhirt.durations.medium") seconds
	    val longDuration = config.getDouble("almhirt.durations.long") seconds
	    def generateUuid = uuidGen.generate
	    def dispose = actorSystem.shutdown}
    ctx
  }
}

