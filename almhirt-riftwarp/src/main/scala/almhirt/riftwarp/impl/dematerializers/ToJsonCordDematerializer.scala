package almhirt.riftwarp.impl.dematerializers

import org.joda.time.DateTime
import scalaz.Cord
import scalaz.Cord._
import scalaz.std._
import scalaz.syntax.validation._
import almhirt.common._
import almhirt.riftwarp._

class ToJsonCordDematerializer(state: Cord)(implicit hasDecomposers: HasDecomposers) extends DematerializesToCord {
  val channelType = RiftJson
  def dematerialize = ('{' -: state :- '}').success

  private def launderString(str: String): String = str

  def addPart(part: Cord): AlmValidation[ToJsonCordDematerializer] =
    if (state.length == 0)
      new ToJsonCordDematerializer(part).success
    else
      new ToJsonCordDematerializer((state :- ',') ++ part).success

  private def addNonePart(ident: String): AlmValidation[ToJsonCordDematerializer] =
    addPart('\"' + ident + ":null")

  private def addStringPart(ident: String, value: String): AlmValidation[ToJsonCordDematerializer] =
    addPart('\"' + ident + ":\"" + launderString(value) + '\"')

  private def addBooleanPart(ident: String, value: Boolean): AlmValidation[ToJsonCordDematerializer] =
    addPart('\"' + ident + ':' + value.toString)

  private def addLongPart(ident: String, value: Long): AlmValidation[ToJsonCordDematerializer] =
    addPart('\"' + ident + ':' + value.toString)

  private def addBigIntPart(ident: String, value: BigInt): AlmValidation[ToJsonCordDematerializer] =
    addPart('\"' + ident + ':' + value.toString)

  private def addFloatingPointPart(ident: String, value: Double): AlmValidation[ToJsonCordDematerializer] =
    addPart('\"' + ident + ':' + value.toString)

  private def addBigDecimalPart(ident: String, value: BigDecimal): AlmValidation[ToJsonCordDematerializer] =
    addPart('\"' + ident + ":\"" + value.toString() + '\"')

  private def addByteArrayPart(ident: String, value: Array[Byte]): AlmValidation[ToJsonCordDematerializer] =
    addPart('\"' + ident + ":\"" + value.toString() + '\"')

  private def addDateTimePart(ident: String, value: DateTime): AlmValidation[ToJsonCordDematerializer] =
    addPart('\"' + ident + ":\"" + value.toString() + '\"')

  private def addUuidPart(ident: String, value: _root_.java.util.UUID): AlmValidation[ToJsonCordDematerializer] =
    addPart('\"' + ident + ":\"" + value.toString() + '\"')

  private def addJsonPart(ident: String, value: String): AlmValidation[ToJsonCordDematerializer] =
    addPart('\"' + ident + ":\"" + value.toString() + '\"')

  private def addXmlPart(ident: String, value: _root_.scala.xml.Node): AlmValidation[ToJsonCordDematerializer] =
    addPart('\"' + ident + ":\"" + value.toString() + '\"')

  private def addComplexPart(ident: String, value: Cord): AlmValidation[ToJsonCordDematerializer] =
    addPart('\"' + ident + ":\"" + value.toString() + '\"')
    
}