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
import akka.util.Duration


trait AlmAkkaComponent {
  implicit def almAkkaContext: AlmAkkaContext
  
  def startAlmAkka() = almAkkaContext.asInstanceOf[AlmAkkaContextImpl].start
  def stopAlmAkka() = almAkkaContext.asInstanceOf[AlmAkkaContextImpl].stop
  
  trait AlmAkkaContextImpl extends AlmAkkaContext {
    def start() { }
    def stop(){ actorSystem.shutdown }
  }
}
