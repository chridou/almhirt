package almhirt.i18n

import almhirt.common._
import almhirt.almvalidation.kit._
import java.text.FieldPosition

trait CanRenderToString {
  def render: AlmValidation[String] = renderIntoBuffer(new StringBuffer(), null).map(_.toString)
  def renderIntoBuffer(into: StringBuffer): AlmValidation[StringBuffer] = renderIntoBuffer(into, null)
  def renderIntoBuffer(into: StringBuffer, pos: FieldPosition): AlmValidation[StringBuffer]

  def forceRender: String = forceRenderIntoBuffer(new StringBuffer(), null).toString
  def forceRenderIntoBuffer(into: StringBuffer): StringBuffer = forceRenderIntoBuffer(into, null)
  def forceRenderIntoBuffer(into: StringBuffer, pos: FieldPosition): StringBuffer = renderIntoBuffer(into, pos).resultOrEscalate

}

object CanRenderToString {
  def apply(text: String): CanRenderToString = new CanRenderToString {
    override def render: AlmValidation[String] = scalaz.Success(text)
    override def renderIntoBuffer(into: StringBuffer): AlmValidation[StringBuffer] = scalaz.Success(into.append(text))
    override def renderIntoBuffer(into: StringBuffer, pos: FieldPosition) = scalaz.Success(into.append(text))

    override def forceRender: String = text
    override def forceRenderIntoBuffer(into: StringBuffer): StringBuffer = into.append(text)
    override def forceRenderIntoBuffer(into: StringBuffer, pos: FieldPosition): StringBuffer = into.append(text)
  }
}