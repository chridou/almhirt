package almhirt

import scala.concurrent.ExecutionContext
import almhirt.common._
import almhirt.akkax.ComponentFactory
import almhirt.problem.Severity
import almhirt.context.ComponentFactoryBuilderEntry

package object context {
  implicit class ComponentFactoryBuilderFunOps1(self: AlmhirtContext => ComponentFactory) {
    def toEntry(severity: Severity): ComponentFactoryBuilderEntry =
      ComponentFactoryBuilderEntry(
        ctx => AlmFuture { scalaz.Success(self(ctx)) }(ctx.futuresContext),
        severity)

    def toCriticalEntry: ComponentFactoryBuilderEntry =
      ComponentFactoryBuilderEntry(
        ctx => AlmFuture { scalaz.Success(self(ctx)) }(ctx.futuresContext),
        CriticalSeverity)

    def toMajorEntry: ComponentFactoryBuilderEntry =
      ComponentFactoryBuilderEntry(
        ctx => AlmFuture { scalaz.Success(self(ctx)) }(ctx.futuresContext),
        MajorSeverity)

    def toMinorEntry: ComponentFactoryBuilderEntry =
      ComponentFactoryBuilderEntry(
        ctx => AlmFuture { scalaz.Success(self(ctx)) }(ctx.futuresContext),
        MinorSeverity)
  }

  implicit class ComponentFactoryBuilderFunOps2(self: AlmhirtContext => AlmValidation[ComponentFactory]) {
    def toEntry(severity: Severity): ComponentFactoryBuilderEntry =
      ComponentFactoryBuilderEntry(
        ctx => AlmFuture { self(ctx) }(ctx.futuresContext),
        severity)

    def toCriticalEntry: ComponentFactoryBuilderEntry =
      ComponentFactoryBuilderEntry(
        ctx => AlmFuture { self(ctx) }(ctx.futuresContext),
        CriticalSeverity)

    def toMajorEntry: ComponentFactoryBuilderEntry =
      ComponentFactoryBuilderEntry(
        ctx => AlmFuture { self(ctx) }(ctx.futuresContext),
        MajorSeverity)

    def toMinorEntry: ComponentFactoryBuilderEntry =
      ComponentFactoryBuilderEntry(
        ctx => AlmFuture { self(ctx) }(ctx.futuresContext),
        MinorSeverity)
  }
  
  implicit class ComponentFactoryBuilderFunOps3(self: AlmhirtContext => AlmFuture[ComponentFactory]) {
    def toEntry(severity: Severity): ComponentFactoryBuilderEntry =
      ComponentFactoryBuilderEntry(
        self,
        severity)

    def toCriticalEntry: ComponentFactoryBuilderEntry =
      ComponentFactoryBuilderEntry(
        self,
        CriticalSeverity)

    def toMajorEntry: ComponentFactoryBuilderEntry =
      ComponentFactoryBuilderEntry(
        self,
        MajorSeverity)

    def toMinorEntry: ComponentFactoryBuilderEntry =
      ComponentFactoryBuilderEntry(
        self,
        MinorSeverity)
  }

}