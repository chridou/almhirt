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
package almhirt.xtractnduce

import scalaz.syntax.validation._
import almhirt._
import almhirt.syntax.almvalidation._

/** Use as a type class or mix it into something*/
trait CanXTract[T] {
  def xtract(xtractor: XTractor): AlmValidationMBD[T]
  def tryXtractFrom(xtractor: XTractor, key: String): AlmValidationMBD[Option[T]] =
    xtractor.tryGetXTractor(key).toMBD.bind(opt => opt.map(x => xtract(x)).validationOut)
  def xtractFrom(xtractor: XTractor, key: String): AlmValidationMBD[T] = 
    tryXtractFrom(xtractor, key) bind {
      case Some(x) => x.success 
      case None => SingleBadDataProblem("Key not found.", key = key).toMBD.failure } 
}