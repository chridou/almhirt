package riftwarp.ext.liftjson

import scalaz.syntax.validation._
import almhirt.common._
import riftwarp._

object LiftJson {
  val toolGroup = ToolGroupLiftJson()

  def register(to: RiftWarp, asDefaults: Boolean): AlmValidation[RiftWarp] = {
    to.toolShed.addDematerializerFactory(ToLiftJsonAstDematerializer, asDefaults)

    to.converters.addConverter(DimensionLiftJsonAstToString)
    to.converters.addConverter(DimensionLiftJsonAstToNiceString)
    to.converters.addConverter(DimensionLiftJsonAstToCord)
    to.converters.addConverter(DimensionLiftJsonAstToNiceCord)
    to.converters.addConverter(DimensionStringToLiftJsonAst)
    to.converters.addConverter(DimensionCordToLiftJsonAst)

    to.success
  }

  def registerAsDefaults(to: RiftWarp): AlmValidation[RiftWarp] = register(to, true)
}
