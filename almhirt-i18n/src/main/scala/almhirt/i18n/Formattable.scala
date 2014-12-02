package almhirt.i18n

import almhirt.common._
import almhirt.almvalidation.kit._
import com.ibm.icu.text._
import java.text.FieldPosition
import com.ibm.icu.util.ULocale

/**
 * Implementations of this trait must be considered mutable and non thread safe.
 * With* methods usually do not return a new instance.
 */
trait Formatable {
  /**
   * Set a value of arbitrary type. If the value is not supported, this may throw an exception.
   *
   * @param arg The argument name and value
   * @return Usually this
   * @provisional This API might change or be removed in a future release.
   */
  def withRawArg(arg: (String, Any)): Formatable

  /**
   * Set many values of arbitrary types. If one ore more of the values is not supported, this may throw an exception.
   *
   * @param args The arguments names and values
   * @return Usually this
   * @provisional This API might change or be removed in a future release.
   */
  def withRawArgs(args: (String, Any)*): Formatable

  /**
   * Add a measured value as pre-rendered String argument.
   *
   * @param arg The argument name and value
   * @param formatwidth the style to render the measured value
   * @return Usually this
   * @provisional This API might change or be removed in a future release.
   */
  def withRenderedMeasuredValue(arg: (String, Measured), formatwidth: MeasureRenderWidth): Formatable

  /**
   * Add a measured value as pre-rendered String argument.
   * How the value is rendered is pre-defined
   * @param arg The argument name and value
   * @return Usually this
   * @provisional This API might change or be removed in a future release.
   */
  def withRenderedMeasuredValue(arg: (String, Measured)): Formatable

  /**
   * Add an argument as a pre-rendered String
   *
   * @param argname the arguments name
   * @param f a function that renders the value which should not throw an exception
   *
   * @return Usually this
   * @provisional This API might change or be removed in a future release.
   */
  def withRenderedArg(argname: String)(f: ULocale ⇒ String): Formatable

  def render: AlmValidation[String] = renderIntoBuffer(new StringBuffer(), null).map(_.toString)
  def renderIntoBuffer(into: StringBuffer): AlmValidation[StringBuffer] = renderIntoBuffer(into, null)
  def renderIntoBuffer(into: StringBuffer, pos: FieldPosition): AlmValidation[StringBuffer]

  final def forceRender: String = forceRenderIntoBuffer(new StringBuffer(), null).toString
  final def forceRenderIntoBuffer(into: StringBuffer): StringBuffer = forceRenderIntoBuffer(into, null)
  final def forceRenderIntoBuffer(into: StringBuffer, pos: FieldPosition): StringBuffer = renderIntoBuffer(into, pos).resultOrEscalate

  def snapshot: Formatable
}

import java.util.HashMap
class IcuFormattable private (msgFormat: MessageFormat, private val _args: HashMap[String, Any]) extends Formatable {
  def this(msgFormat: MessageFormat) = this(msgFormat, new HashMap[String, Any]())

  def withRawArg(arg: (String, Any)): IcuFormattable = {
    _args.put(arg._1, arg._2)
    this
  }

  def withRawArgs(args: (String, Any)*): IcuFormattable = {
    args.foreach(arg ⇒ _args.put(arg._1, arg._2))
    this
  }

  def withRenderedMeasuredValue(arg: (String, Measured), formatwidth: MeasureRenderWidth): IcuFormattable = {
    val fmt = MeasureFormat.getInstance(msgFormat.getULocale, mapRenderWidth(formatwidth))
    withRawArg(arg._1 -> fmt.format(arg._2.icu))
  }

  def withRenderedMeasuredValue(arg: (String, Measured)): IcuFormattable = {
    val fmt = MeasureFormat.getInstance(msgFormat.getULocale, mapRenderWidth(MeasureRenderWidth.Short))
    withRawArg(arg._1 -> fmt.format(arg._2.icu))
  }

  def withRenderedArg(argname: String)(f: ULocale ⇒ String): IcuFormattable = {
    withRawArg(argname -> f(msgFormat.getULocale))
  }

  def renderIntoBuffer(into: StringBuffer, pos: FieldPosition): AlmValidation[StringBuffer] =
    inTryCatch {
      msgFormat.format(_args, into, pos)
    }

  def modify(f: MessageFormat ⇒ Unit): IcuFormattable = {
    f(msgFormat)
    this
  }

  override def snapshot: IcuFormattable = {
    val newArgs = new HashMap[String, Any]()
    newArgs.putAll(_args)
    new IcuFormattable(msgFormat.clone.asInstanceOf[MessageFormat], newArgs)
  }

  val underlying = msgFormat

  private def mapRenderWidth(mrw: MeasureRenderWidth): MeasureFormat.FormatWidth = {
    mrw match {
      case MeasureRenderWidth.Wide    ⇒ MeasureFormat.FormatWidth.WIDE
      case MeasureRenderWidth.Narrow  ⇒ MeasureFormat.FormatWidth.NARROW
      case MeasureRenderWidth.Numeric ⇒ MeasureFormat.FormatWidth.NUMERIC
      case MeasureRenderWidth.Short   ⇒ MeasureFormat.FormatWidth.SHORT
    }
  }
}