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
}

object Formatable {
  def apply(text: String): Formatable = new Formatable {
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

    def tryFormatArgsIntoBufferAt(appendTo: StringBuffer, pos: FieldPosition, args: Map[String, Any]): Option[StringBuffer] =
      self.formatArgsIntoBufferAt(appendTo, pos, args).toOption
    def tryFormatIntoBufferAt(appendTo: StringBuffer, pos: FieldPosition, args: (String, Any)*): Option[StringBuffer] =
      self.formatIntoBufferAt(appendTo, pos, args: _*).toOption
    def tryFormatIntoBuffer(appendTo: StringBuffer, args: (String, Any)*): Option[StringBuffer] =
      self.formatIntoBuffer(appendTo, args: _*).toOption
    def tryFormatArgsIntoBuffer(appendTo: StringBuffer, args: Map[String, Any]): Option[StringBuffer] =
      self.formatArgsIntoBuffer(appendTo, args).toOption
    def tryFormat(args: (String, Any)*): Option[String] =
      self.format(args: _*).toOption
    def tryFormatArgs(args: Map[String, Any]): Option[String] =
      self.formatArgs(args).toOption

  }
}

final class IcuFormatable (msgFormat: MessageFormat) extends Formatable {
  override def (appendTo: StringBuffer, pos: FieldPosition, args: Map[String, Any]): AlmValidation[StringBuffer] =
    inTryCatch {
      val map = new java.util.HashMap[String, Any]
      msgFormat.format(_args, appendTo, pos)
    }

  val underlying = msgFormat
}


