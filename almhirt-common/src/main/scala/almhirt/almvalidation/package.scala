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
package almhirt

package object almvalidation {
  object inst extends AlmValidationInstances

  object funs extends AlmValidationFunctions with AlmValidationParseFunctions with AlmValidationCastFunctions with AlmConstraintsFuns

  object all 
    extends AlmValidationFunctions with AlmValidationParseFunctions with AlmValidationCastFunctions
    with AlmValidationInstances
    with almvalidation.ToAlmValidationOps

  object kit 
    extends almhirt.problem.ProblemInstances with almhirt.problem.ProblemCategoryInstances with almhirt.problem.SeverityInstances 
    with almhirt.problem.ProblemFunctions with almhirt.problem.ToProblemOps  
    with AlmValidationFunctions with AlmValidationParseFunctions with AlmValidationCastFunctions
    with AlmValidationInstances
    with almvalidation.ToAlmValidationOps
    
  object constraints extends ToAlmValidationContraintsOps
}