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
trait Formatable extends CanRenderToString {
  /**
   * Set a value of arbitrary type. If the value is not supported, this may throw an exception.
   *
   * @param arg The argument name and value
   * @return Usually this
   * @provisional This API might change or be removed in a future release.
   */
  def withArg(arg: (String, Any)): Formatable

  /**
   * Set many values of arbitrary types. If one ore more of the values is not supported, this may throw an exception.
   *
   * @param args The arguments names and values
   * @return Usually this
   * @provisional This API might change or be removed in a future release.
   */
  def withArgs(args: (String, Any)*): Formatable

  /**
   * Set many values of arbitrary types. If one ore more of the values is not supported, this may throw an exception.
   *
   * @param arg The arguments value
   * @return Usually this
   * @provisional This API might change or be removed in a future release.
   */
  def withUnnamedArg(arg: Any): Formatable

  /**
   * Set many values of arbitrary types. If one ore more of the values is not supported, this may throw an exception.
   *
   * @param args The argument values
   * @return Usually this
   * @provisional This API might change or be removed in a future release.
   */
  def withUnnamedArgs(args: Any*): Formatable

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

  def snapshot: Formatable
}

final class IcuFormatable private (msgFormat: MessageFormat, private val _args: java.util.HashMap[String, Any], private var _argIndex: Int) extends Formatable {
  def this(msgFormat: MessageFormat) = this(msgFormat, new java.util.HashMap[String, Any](), 1)

  override def withArg(arg: (String, Any)): IcuFormatable = {
    _args.put(arg._1, arg._2)
    this
  }

  override def withArgs(args: (String, Any)*): IcuFormatable = {
    args.foreach(arg ⇒ _args.put(arg._1, arg._2))
    this
  }

  override def withRenderedArg(argname: String)(f: ULocale ⇒ String): IcuFormatable = {
    withArg(argname -> f(msgFormat.getULocale))
  }

  override def withUnnamedArg(arg: Any): Formatable = {
    _args.put(_argIndex.toString(), arg)
    _argIndex += 1
    this
  }

  override def withUnnamedArgs(args: Any*): Formatable = {
    args.foreach { arg ⇒
      _args.put(_argIndex.toString(), arg)
      _argIndex += 1
    }
    this
  }

  override def renderIntoBuffer(appendTo: StringBuffer, pos: FieldPosition): AlmValidation[StringBuffer] =
    inTryCatch {
      msgFormat.format(_args, appendTo, pos)
    }

  def modify(f: MessageFormat ⇒ Unit): IcuFormatable = {
    f(msgFormat)
    this
  }

  override def snapshot: IcuFormatable = {
    val newArgs = new java.util.HashMap[String, Any]()
    newArgs.putAll(_args)
    new IcuFormatable(msgFormat.clone.asInstanceOf[MessageFormat], newArgs, _argIndex)
  }

  val underlying = msgFormat
}


