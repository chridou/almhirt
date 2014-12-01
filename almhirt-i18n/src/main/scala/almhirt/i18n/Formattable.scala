package almhirt.i18n

import almhirt.common._
import com.ibm.icu.text.MessageFormat
import java.text.FieldPosition

trait Formattable {
  def withRawArg(arg: (String, Any)): Formattable
  def withRawArgs(args: (String, Any)*): Formattable
  def withMeasuredValue(mv: (String, Measured)): Formattable

  def render: AlmValidation[String] = renderIntoBuffer(new StringBuffer(), null).map(_.toString)
  def renderIntoBuffer(into: StringBuffer): AlmValidation[StringBuffer] = renderIntoBuffer(into, null)
  def renderIntoBuffer(into: StringBuffer, pos: FieldPosition): AlmValidation[StringBuffer]
}

class IcuFormattable(msgFormat: MessageFormat) extends Formattable {
  import java.util.HashMap
  private val _args = new HashMap[String, Any]()

  
  def withRawArg(arg: (String, Any)): IcuFormattable = {
    _args.put(arg._1, arg._2)
    this
  }
  
  def withRawArgs(args: (String, Any)*): IcuFormattable = {
    args.foreach(arg => _args.put(arg._1, arg._2))
    this
  }
  
  def withMeasuredValue(mv: (String, Measured)): IcuFormattable = {
    withRawArg(mv._1 -> mv._2.icu)
  }
  
  def renderIntoBuffer(into: StringBuffer, pos: FieldPosition): AlmValidation[StringBuffer] =
    inTryCatch {
      msgFormat.format(_args, into, pos)
    }
}