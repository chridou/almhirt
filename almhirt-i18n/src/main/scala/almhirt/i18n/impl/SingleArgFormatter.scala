package almhirt.i18n.impl

import scala.collection.mutable.HashMap
import almhirt.common._
import almhirt.i18n._
import com.ibm.icu.util.ULocale
import java.text.FieldPosition

private[almhirt] final class SingleArgFormatter(formatter: SingleArgFormattingModule) extends AlmFormatter {
  override def locale = formatter.locale
  override def formatArgsIntoBufferAt(appendTo: StringBuffer, pos: FieldPosition, args: Map[String, Any]): AlmValidation[StringBuffer] =
    args get (formatter.argname) match {
      case Some(v) ⇒
        formatter.formatIntoBuffer(appendTo, pos, v)
      case None ⇒
        scalaz.Failure(NoSuchElementProblem(s"""An argument named "${formatter.argname}" was not found."""))
    }

  override def formatValuesIntoBufferAt(appendTo: StringBuffer, pos: FieldPosition, values: Any*): AlmValidation[StringBuffer] =
    if (values.isEmpty) {
      scalaz.Failure(NoSuchElementProblem(s"""An argument is required."""))
    } else {
      formatter.formatIntoBuffer(appendTo, pos, values.head)
    }
}