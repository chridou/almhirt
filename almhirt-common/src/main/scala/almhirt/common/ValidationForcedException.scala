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
package almhirt.common

/** Wow, let's hope you'll never trigger that one in production code ... 
 */
case class ResultForcedFromValidationException(val problem: Problem) extends Exception("A value has been forced from a failure: %s".format(problem.message))

/** Wow, let's hope you'll never trigger that one in production code ... 
 */
case class ProblemForcedFromValidationException() extends Exception("A problem has been forced from a success.")
