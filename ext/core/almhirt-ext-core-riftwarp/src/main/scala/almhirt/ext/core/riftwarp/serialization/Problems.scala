package almhirt.ext.core.riftwarp.serialization

import almhirt.common._
import riftwarp._

object Problems {
  def createDefaultRawDecomposer(aTypeDescriptor: TypeDescriptor): RawDecomposer = {
    new RawDecomposer {
      val typeDescriptor = aTypeDescriptor
      def decomposeRaw[TDimension <: RiftDimension](what: AnyRef)(into: Dematerializer[TDimension]): AlmValidation[Dematerializer[TDimension]] =
        for {
          problem <- almhirt.almvalidation.funs.almCast[Problem](what)
          next <- into.addTypeDescriptor(typeDescriptor)
          next <- into.addString("message", problem.message)
          next <- into.addComplexTypeFixed("severity", problem.severity)
          next <- into.addComplexTypeFixed("category", problem.category)
          next <- into.addMap[String, Any]("args", problem.args)
          next <- into.addOptionalComplexType[ProblemCause]("cause", problem.cause)
        } yield next
    }
  }

  def createAggregateProblemDecomposer(withTypeDescriptor: Option[TypeDescriptor] = None): RawDecomposer = {
    val td = withTypeDescriptor.getOrElse(TypeDescriptor("AggregateProblem"))
    val inner = createDefaultRawDecomposer(td)
    new RawDecomposer {
      val typeDescriptor = td
      def decomposeRaw[TDimension <: RiftDimension](what: AnyRef)(into: Dematerializer[TDimension]): AlmValidation[Dematerializer[TDimension]] =

        for {
          problem <- almhirt.almvalidation.funs.almCast[AggregateProblem](what)
          defaults <- inner.decomposeRaw(what)(into)
          additional <- defaults.addComplexMALoose("problems", problem.problems)
        } yield additional
    }
  }
}