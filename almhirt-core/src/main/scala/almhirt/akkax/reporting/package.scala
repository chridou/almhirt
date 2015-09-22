package almhirt.akkax

import scala.language.implicitConversions
import almhirt.common._
import ezreps.ast

package object reporting extends ezreps.util.EzFuns {
  type StatusReport = ast.EzReportValue
  type StatusReportOptions = ezreps.EzOptions
  val StatusReportOptions = ezreps.EzOptions
  
  type ProblematicOption[T] = AlmValidation[Option[T]]

  object Implicits extends ezreps.util.EzValueConverters with ezreps.util.EzValueIdentityConverters with ezreps.util.EzValueOptionConverters with AlmEzValueConverters with AlmEzValueOptionConverters

  object StatusReport {
    def apply(): ezreps.ast.EzReportValue = ezreps.ast.EzReportValue()
    def apply(name: String): ezreps.ast.EzReportValue = StatusReport().withReportName(name)
  }

  implicit def almValidation2RValue[T](v: AlmValidation[T])(implicit converter: ezreps.util.EzValueConverter[T], pconv: ezreps.util.EzValueConverter[Problem]): ast.EzValue =
    v.fold(
      fail ⇒ toAST(fail),
      succ ⇒ toAST(succ))

  implicit def almValidationOption2RValue[T](v: ProblematicOption[T])(implicit converter: ezreps.util.EzValueConverter[Option[T]], pconv: ezreps.util.EzValueConverter[Problem]): ast.EzValue =
    v.fold(
      fail ⇒ toAST(fail),
      succ ⇒ toAST(succ))

  implicit def almOptionValidation2RValue[T](v: Option[AlmValidation[T]])(implicit converter: ezreps.util.EzValueConverter[T], pconv: ezreps.util.EzValueConverter[Problem]): ast.EzValue =
    v match {
    case None => ast.EzNotAvailable
    case Some(something) => almValidation2RValue(something)
  }
      
  implicit def toFieldFromValidation[T](v: (String, AlmValidation[T]))(implicit converter: ezreps.util.EzValueConverter[T], pconv: ezreps.util.EzValueConverter[Problem]): ast.EzField =
    ast.EzField(v._1, v._2.fold(
      fail ⇒ toAST(fail),
      succ ⇒ toAST(succ)))

  implicit def toFieldFromProblematicOption[T](v: (String, ProblematicOption[T]))(implicit converter: ezreps.util.EzValueConverter[Option[T]], pconv: ezreps.util.EzValueConverter[Problem]): ast.EzField =
    ast.EzField(v._1, v._2.fold(
      fail ⇒ toAST(fail),
      succ ⇒ toAST(succ)))

  implicit def toFieldFromOptionValidation[T](v: (String, Option[AlmValidation[T]]))(implicit converter: ezreps.util.EzValueConverter[T], pconv: ezreps.util.EzValueConverter[Problem]): ast.EzField =
    ast.EzField(v._1, v._2)
      
  implicit class EzValueOps(val self: ast.EzValue) extends ezreps.util.EzValueOps      
      
  implicit class StatusReportAlmhirtManipulationOps(val self: ast.EzReportValue) extends ezreps.util.EzReportManipulationOps with ezreps.util.EzReportQueryOps {

    def withComponentState(state: ComponentState): ast.EzReportValue =
      ast.EzReportValue(self.fields :+ ast.EzField("component-state", ast.EzString(state.parsableString)))

    def createdNowUtc(implicit ccdt: CanCreateDateTime): ast.EzReportValue =
      self.createdOnUtc(ccdt.getUtcTimestamp)

    def createdNow(implicit ccdt: CanCreateDateTime): ast.EzReportValue =
      self.createdOn(ccdt.getDateTime())

    def actorPath(path: akka.actor.ActorPath): ast.EzReportValue =
      ast.EzReportValue(self.fields :+ ast.EzField("actor-path", ast.EzString(path.toStringWithoutAddress)))

  }
}

