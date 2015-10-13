package almhirt.i18n.impl

import scalaz.syntax.validation._
import scalaz.Validation.FlatMap._
import almhirt.common._
import almhirt.almvalidation.kit._
import almhirt.i18n.{ AlmFormatter, BasicValueResourceValue }
import com.ibm.icu.util.ULocale
import java.text.FieldPosition

private[almhirt] object SelectionOfManyResourceValue {

}

private[almhirt] final class SelectionOfManyResourceValue(
    override val locale: ULocale,
    selectionSizeParameter: Option[String],
    lowerIndexParameter: Option[String],
    allItemsCountParameter: Option[String],
    upperIndexParameter: Option[String],
    ifAllItemsCountParamIsZero: String,
    ifSelectionSizeIsZero: String,
    separator: Option[String],
    rangeSelectionFormatter: Option[AlmFormatter],
    amountSelectionFormatter: Option[AlmFormatter],
    allItemsPartFormatter: Option[AlmFormatter]) extends BasicValueResourceValue with AlmFormatter {
  val selectionSizeParamName = selectionSizeParameter getOrElse "selection-size"
  val lowerIndexParamName = lowerIndexParameter getOrElse "lower-index"
  val allItemsCountParamName = allItemsCountParameter getOrElse "all-items-count"
  val upperIndexParamName = upperIndexParameter getOrElse "upper-index"

  def formatArgsIntoAt(appendTo: StringBuffer, pos: FieldPosition, args: Map[String, Any]): AlmValidation[StringBuffer] = {
    for {
      allItemsCount ← any2Int((args get allItemsCountParamName) getOrElse 0, allItemsCountParamName)
      lowerIndex ← (args get lowerIndexParamName).map(any2Int(_, lowerIndexParamName)).validationOut
      upperIndex ← (args get upperIndexParamName).map(any2Int(_, upperIndexParamName)).validationOut
      selectionSize ← (args get selectionSizeParamName).map(any2Int(_, selectionSizeParamName)).validationOut
      res ← formatInternal(allItemsCount, lowerIndex, upperIndex, selectionSize, appendTo)
    } yield res
  }

  private def formatInternal(allItemsCount: Int, lowerIndex: Option[Int], upperIndex: Option[Int], selectionSize: Option[Int], appendTo: StringBuffer): AlmValidation[StringBuffer] = {
    if (allItemsCount <= 0) formatNoItems(appendTo)
    else {
      val (effSelectionSize, effLowerIndex): (Int, Option[Int]) =
        (selectionSize, lowerIndex, upperIndex) match {
          case (Some(sz), _, _) if sz <= 0 ⇒ (0, None)
          case (Some(sz), Some(li), _)     ⇒ (sz, Some(li))
          case (Some(sz), None, Some(ui))  ⇒ (sz, Some(ui - sz + 1))
          case (Some(sz), None, None)      ⇒ (sz, None)
          case (None, Some(li), Some(ui))  ⇒ (ui - li + 1, Some(li))
          case (None, Some(li), None)      ⇒ (1, Some(li))
          case (None, None, Some(ui))      ⇒ (1, Some(ui))
          case (None, None, None)          ⇒ (0, None)
        }

      val preResV: AlmValidation[StringBuffer] = (effLowerIndex, effSelectionSize, rangeSelectionFormatter, amountSelectionFormatter) match {
        case (_, 0, _, _) ⇒
          appendTo.append(ifSelectionSizeIsZero).success
        case (Some(li), _, Some(rsf), _) ⇒
          val upperIndex = li + effSelectionSize - 1
          rsf.formatInto(appendTo, selectionSizeParamName -> effSelectionSize, lowerIndexParamName -> li, upperIndexParamName -> upperIndex)
        case (_, _, _, Some(asf)) ⇒
          asf.formatInto(appendTo, selectionSizeParamName -> effSelectionSize)
        case _ ⇒
          UnspecifiedProblem("I need at least an 'amount-selection-part' to display any other selection than zero elements.").failure
      }

      preResV.flatMap(appendTo ⇒
        allItemsPartFormatter match {
          case Some(fmt) ⇒
            separator.foreach { appendTo.append }
            fmt.formatInto(appendTo, allItemsCountParamName -> allItemsCount)
          case None ⇒ appendTo.success
        })
    }
  }

  private def formatNoItems(appendTo: StringBuffer): AlmValidation[StringBuffer] = {
    appendTo.append(ifAllItemsCountParamIsZero).success
  }

  override def formatable = this

  private def any2Int(what: Any, paramName: String): AlmValidation[Int] =
    what match {
      case x: Byte   ⇒ x.toInt.success
      case x: Short  ⇒ x.toInt.success
      case x: Int    ⇒ x.success
      case x: Long   ⇒ inTryCatch { x.toInt }
      case x: Float  ⇒ inTryCatch { x.toInt }
      case x: Double ⇒ inTryCatch { x.toInt }
      case x: String ⇒ x.toIntAlm
      case x         ⇒ ArgumentProblem(s"Parameter '$paramName' of type ${x.getClass.getName} can not be converted to an Int.").failure
    }
}
