package almhirt.i18n

import almhirt.common._
import almhirt.almvalidation.kit._
import com.ibm.icu.text._
import java.text.FieldPosition
import com.ibm.icu.util.ULocale

/**
 * Implementations of this trait must be considered non thread safe.
 * With* methods usually do not return a new instance.
 */
trait Formatable {
  def locale: ULocale

  def formatArgsIntoBufferAt(appendTo: StringBuffer, pos: FieldPosition, args: Map[String, Any]): AlmValidation[StringBuffer]

  def formatIntoBufferAt(appendTo: StringBuffer, pos: FieldPosition, args: (String, Any)*): AlmValidation[StringBuffer] =
    formatArgsIntoBufferAt(appendTo, pos, Map(args: _*))

  def formatIntoBuffer(appendTo: StringBuffer, args: (String, Any)*): AlmValidation[StringBuffer] =
    formatIntoBufferAt(appendTo, util.DontCareFieldPosition, args: _*)

  def formatArgsIntoBuffer(appendTo: StringBuffer, args: Map[String, Any]): AlmValidation[StringBuffer] =
    formatArgsIntoBufferAt(appendTo, util.DontCareFieldPosition, args)

  def format(args: (String, Any)*): AlmValidation[String] =
    formatIntoBufferAt(new StringBuffer(), util.DontCareFieldPosition, args: _*).map(_.toString)

  def formatArgs(args: Map[String, Any]): AlmValidation[String] =
    formatArgsIntoBufferAt(new StringBuffer(), util.DontCareFieldPosition, args).map(_.toString)

  def formatValuesIntoBufferAt(appendTo: StringBuffer, pos: FieldPosition, values: Any*): AlmValidation[StringBuffer] = {
    var i = 0
    val theMap = values.map { x ⇒ i = i + 1; (i.toString, x) }.toMap
    formatArgsIntoBufferAt(appendTo, pos, theMap)
  }

  def formatValuesIntoBuffer(appendTo: StringBuffer, values: Any*): AlmValidation[StringBuffer] =
    formatValuesIntoBufferAt(appendTo, util.DontCareFieldPosition, values: _*)

  def formatValues(values: Any*): AlmValidation[String] =
    formatValuesIntoBufferAt(new StringBuffer(), util.DontCareFieldPosition, values: _*).map(_.toString)
}

object Formatable {
  def apply(theLocale: ULocale, text: String): Formatable = new Formatable {
    override val locale = theLocale
    
    override def formatArgsIntoBufferAt(appendTo: StringBuffer, pos: FieldPosition, args: Map[String, Any]): AlmValidation[StringBuffer] =
      scalaz.Success(appendTo.append(text))

    override def formatIntoBufferAt(appendTo: StringBuffer, pos: FieldPosition, args: (String, Any)*): AlmValidation[StringBuffer] =
      scalaz.Success(appendTo.append(text))

    override def formatIntoBuffer(appendTo: StringBuffer, args: (String, Any)*): AlmValidation[StringBuffer] =
      scalaz.Success(appendTo.append(text))

    override def formatArgsIntoBuffer(appendTo: StringBuffer, args: Map[String, Any]): AlmValidation[StringBuffer] =
      scalaz.Success(appendTo.append(text))

    override def format(args: (String, Any)*): AlmValidation[String] =
      scalaz.Success(text)

    override def formatArgs(args: Map[String, Any]): AlmValidation[String] =
      scalaz.Success(text)
  }

  implicit class FormatableOps(val self: Formatable) extends AnyVal {
    def forceFormatArgsIntoBufferAt(appendTo: StringBuffer, pos: FieldPosition, args: Map[String, Any]): StringBuffer =
      self.formatArgsIntoBufferAt(appendTo, pos, args).resultOrEscalate
    def forceFormatIntoBufferAt(appendTo: StringBuffer, pos: FieldPosition, args: (String, Any)*): StringBuffer =
      self.formatIntoBufferAt(appendTo, pos, args: _*).resultOrEscalate
    def forceFormatIntoBuffer(appendTo: StringBuffer, args: (String, Any)*): StringBuffer =
      self.formatIntoBuffer(appendTo, args: _*).resultOrEscalate
    def forceFormatArgsIntoBuffer(appendTo: StringBuffer, args: Map[String, Any]): StringBuffer =
      self.formatArgsIntoBuffer(appendTo, args).resultOrEscalate
    def forceFormat(args: (String, Any)*): String =
      self.format(args: _*).resultOrEscalate
    def forceFormatArgs(args: Map[String, Any]): String =
      self.formatArgs(args).resultOrEscalate

    def forceFormatValuesIntoBufferAt(appendTo: StringBuffer, pos: FieldPosition, values: Any*): StringBuffer = {
      self.formatValuesIntoBufferAt(appendTo, pos, values: _*).resultOrEscalate
    }

    def forceFormatValuesIntoBuffer(appendTo: StringBuffer, values: Any*): StringBuffer =
      self.formatValuesIntoBuffer(appendTo, values: _*).resultOrEscalate

    def forceFormatValues(values: Any*): String =
      self.formatValues(values: _*).resultOrEscalate

  }
}

final class IcuFormatable(msgFormat: MessageFormat) extends Formatable {
  override val locale = msgFormat.getULocale
  
  override def formatArgsIntoBufferAt(appendTo: StringBuffer, pos: FieldPosition, args: Map[String, Any]): AlmValidation[StringBuffer] =
    inTryCatch {
      val map = new java.util.HashMap[String, Any]
      args.foreach({ case (k, v) ⇒ map.put(k, v) })
      msgFormat.format(map, appendTo, pos)
    }

  val underlying = msgFormat
}


