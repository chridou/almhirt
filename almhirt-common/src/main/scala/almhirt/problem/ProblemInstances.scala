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
package almhirt.problem

import scala.language.implicitConversions

import almhirt.common._
import scalaz.Show
import scalaz.Monoid

trait ProblemInstances {
  implicit def ToAggregateProblemSemiGroup: Monoid[AggregateProblem] =
    new Monoid[AggregateProblem] {
      override val zero = AggregateProblem.empty
      override def append(a: AggregateProblem, b: => AggregateProblem): AggregateProblem = {
        AggregateProblem(a.problems ++ b.problems)
      }
    }
}
