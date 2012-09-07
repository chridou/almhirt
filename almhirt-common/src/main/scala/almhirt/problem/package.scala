package almhirt

package object problem {
  object inst extends ProblemInstances with ProblemCategoryInstances  with SeverityInstances

  object funs  extends ProblemFunctions  

  object all 
     extends ProblemInstances with ProblemCategoryInstances with SeverityInstances 
     with ProblemFunctions with ToProblemOps  

}