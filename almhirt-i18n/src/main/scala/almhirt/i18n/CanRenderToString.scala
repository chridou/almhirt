package almhirt.i18n

import almhirt.common._
import almhirt.almvalidation.kit._
import java.text.FieldPosition

trait CanRenderToString {
  def render: AlmValidation[String] = renderIntoBuffer(new StringBuffer(), util.DontCareFieldPosition).map(_.toString)
  def renderIntoBuffer(appendTo: StringBuffer): AlmValidation[StringBuffer] = renderIntoBuffer(appendTo, null)
  def renderIntoBuffer(appendTo: StringBuffer, pos: FieldPosition): AlmValidation[StringBuffer]

  def forceRender: String = forceRenderIntoBuffer(new StringBuffer(), util.DontCareFieldPosition).toString
  def forceRenderIntoBuffer(appendTo: StringBuffer): StringBuffer = forceRenderIntoBuffer(appendTo, util.DontCareFieldPosition)
  def forceRenderIntoBuffer(appendTo: StringBuffer, pos: FieldPosition): StringBuffer = renderIntoBuffer(appendTo, pos).resultOrEscalate

  def tryRender: Option[String] = render.toOption
  def tryRenderIntoBuffer(appendTo: StringBuffer): Option[StringBuffer] = renderIntoBuffer(appendTo).toOption
  def tryRenderIntoBuffer(appendTo: StringBuffer, pos: FieldPosition): Option[StringBuffer] = renderIntoBuffer(appendTo, pos).toOption
}

object CanRenderToString {
  def apply(text: String): CanRenderToString = new CanRenderToString {
    override def render: AlmValidation[String] = scalaz.Success(text)
    override def renderIntoBuffer(appendTo: StringBuffer): AlmValidation[StringBuffer] = scalaz.Success(appendTo.append(text))
    override def renderIntoBuffer(appendTo: StringBuffer, pos: FieldPosition) = scalaz.Success(appendTo.append(text))

    override def forceRender: String = text
    override def forceRenderIntoBuffer(appendTo: StringBuffer): StringBuffer = appendTo.append(text)
    override def forceRenderIntoBuffer(appendTo: StringBuffer, pos: FieldPosition): StringBuffer = appendTo.append(text)
  }
}