package almhirt.i18n.impl

import scala.collection.mutable.HashMap
import almhirt.common._
import almhirt.i18n._
import com.ibm.icu.util.ULocale
import java.text.FieldPosition

final class SingleArgFormatable(formatter: BasicValueFormatter, private var _args: HashMap[String, Any]) extends Formatable {
  def this(formatter: BasicValueFormatter) = this(formatter, HashMap[String, Any]())

  def withArg(arg: (String, Any)): Formatable = {
    _args += arg
    this
  }

  def withArgs(args: (String, Any)*): Formatable = {
    _args = _args ++ args
    this
  }

  override def withUnnamedArg(arg: Any): Formatable = {
    _args += (formatter.argname -> arg)
    this
  }

  override def withUnnamedArgs(args: Any*): Formatable = {
    if (args.isEmpty)
      this
    else
      withUnnamedArg(args.head)
  }

  def withRenderedArg(argname: String)(f: ULocale ⇒ String): Formatable = {
    withArg(argname -> f(formatter.locale))
  }

  override def renderIntoBuffer(appendTo: StringBuffer, pos: FieldPosition): AlmValidation[StringBuffer] = {
    _args get (formatter.argname) match {
      case Some(v) ⇒
        formatter.renderIntoBuffer(v, appendTo, pos)
      case None ⇒
        scalaz.Failure(NoSuchElementProblem(s"""An argument named "${formatter.argname}" was not found."""))
    }
  }

  override def snapshot: Formatable = {
    val newArgs = HashMap[String, Any]()
    newArgs ++= _args
    new SingleArgFormatable(formatter, newArgs)
  }
}