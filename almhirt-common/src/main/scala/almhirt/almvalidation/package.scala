package almhirt

package object almvalidation {
  object inst extends AlmValidationInstances

  object funs extends AlmValidationFunctions with AlmValidationParseFunctions

  object all 
    extends AlmValidationFunctions with AlmValidationParseFunctions
    with AlmValidationInstances
    with almvalidation.ToAlmValidationOps

  object kit 
    extends almhirt.problem.ProblemInstances with almhirt.problem.ProblemCategoryInstances with almhirt.problem.SeverityInstances 
    with almhirt.problem.ProblemFunctions with almhirt.problem.ToProblemOps  
    with AlmValidationFunctions with AlmValidationParseFunctions
    with AlmValidationInstances
    with almvalidation.ToAlmValidationOps
    
  object flatmap extends ValidationFlatMapEnabler
}