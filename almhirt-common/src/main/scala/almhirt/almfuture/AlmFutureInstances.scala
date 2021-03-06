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
package almhirt.almfuture

import scala.language.implicitConversions

import scala.concurrent.Future
import almhirt.common._

trait AlmFutureInstances {
  /** Turn this [[scala.concurrent.Future]] into an [[almhirt.common.AlmFuture]] */
  implicit def akkaFutureToAlmhirtFuture[T](akkaFuture: Future[AlmValidation[T]]): AlmFuture[T] =
    new AlmFuture(akkaFuture)

  implicit val javaTimerSchedulingMagnet = new ActionSchedulingMagnet[java.util.Timer] {
    def schedule(to: java.util.Timer, actionBlock: ⇒ Unit, in: scala.concurrent.duration.FiniteDuration, executor: scala.concurrent.ExecutionContext): Unit = {
      val r = new java.util.TimerTask() { def run() { actionBlock } }
      to.schedule(r, in.toMillis)
    }
  }
}