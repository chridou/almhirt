package almhirt.i18n.impl

import scalaz.syntax.validation._
import scalaz.Validation.FlatMap._
import almhirt.common._
import almhirt.almvalidation.kit._
import almhirt.i18n.{ AlmFormatter, BasicValueResourceValue }
import com.ibm.icu.util.ULocale
import java.text.FieldPosition

private[almhirt] final class SelectionOfManyResourceValue(
    override val locale: ULocale,
    selectionSizeParameter: Option[String],
    lowerIndexParameter: Option[String],
    allItemsCountParameter: Option[String],
    upperIndexParameter: Option[String],
    ifAllItemsCountParamIsZero: String,
    ifSelectionSizeEqualsAllItemsCountFormatter: Option[() ⇒ AlmFormatter],
    ifSelectionSizeIsZeroFormatter: Option[() ⇒ AlmFormatter],
    joiner: Option[String],
    rangeSelectionFormatter: Option[() ⇒ AlmFormatter],
    amountSelectionFormatter: Option[() ⇒ AlmFormatter],
    allItemsPartFormatter: Option[() ⇒ AlmFormatter]) extends BasicValueResourceValue with AlmFormatter {
  val selectionSizeParamName = selectionSizeParameter getOrElse "selection_size"
  val lowerIndexParamName = lowerIndexParameter getOrElse "lower_index"
  val allItemsCountParamName = allItemsCountParameter getOrElse "all_items_count"
  val upperIndexParamName = upperIndexParameter getOrElse "upper_index"

  val myParamNames = Set(selectionSizeParamName, lowerIndexParamName, allItemsCountParamName, upperIndexParamName)

  def formatArgsIntoAt(appendTo: StringBuffer, pos: FieldPosition, args: Map[String, Any]): AlmValidation[StringBuffer] = {
    val alienParameters = args.filterKeys { key ⇒ !myParamNames(key) }
    for {
      allItemsCount ← any2Int((args get allItemsCountParamName) getOrElse 0, allItemsCountParamName)
      lowerIndex ← (args get lowerIndexParamName).map(any2Int(_, lowerIndexParamName)).validationOut
      upperIndex ← (args get upperIndexParamName).map(any2Int(_, upperIndexParamName)).validationOut
      selectionSize ← (args get selectionSizeParamName).map(any2Int(_, selectionSizeParamName)).validationOut
      res ← formatInternal(allItemsCount, lowerIndex, upperIndex, selectionSize, appendTo, alienParameters)
    } yield res
  }

  private def formatInternal(allItemsCount: Int, lowerIndex: Option[Int], upperIndex: Option[Int], selectionSize: Option[Int], appendTo: StringBuffer, alienParameters: Map[String, Any]): AlmValidation[StringBuffer] = {
    if (allItemsCount <= 0) formatNoItems(appendTo)
    else {
      val (effSelectionSize, upperAndLowerIndex): (Int, Option[(Int, Int)]) =
        (selectionSize, lowerIndex, upperIndex) match {
          case (Some(sz), _, _) if sz <= 0 ⇒ (0, None)
          case (Some(sz), Some(li), _)     ⇒ (sz, Some((li, li + sz - 1)))
          case (Some(sz), None, Some(ui))  ⇒ (sz, Some((ui - sz + 1, ui)))
          case (Some(sz), None, None)      ⇒ (sz, None)
          case (None, Some(li), Some(ui))  ⇒ (ui - li + 1, Some((li, ui)))
          case (None, Some(li), None)      ⇒ (1, Some((li, li)))
          case (None, None, Some(ui))      ⇒ (1, Some(ui, ui))
          case (None, None, None)          ⇒ (0, None)
        }

      val allParams = createParamsMap(alienParameters, upperAndLowerIndex, selectionSizeParamName -> effSelectionSize, allItemsCountParamName -> allItemsCount)

      val preResV: AlmValidation[StringBuffer] =
        if (effSelectionSize == 0 && ifSelectionSizeIsZeroFormatter.isDefined) {
          ifSelectionSizeIsZeroFormatter.get().formatArgsInto(appendTo, allParams)
        } else if (effSelectionSize == allItemsCount && ifSelectionSizeEqualsAllItemsCountFormatter.isDefined) {
          ifSelectionSizeEqualsAllItemsCountFormatter.get().formatArgsInto(appendTo, allParams)
        } else {
          (upperAndLowerIndex, rangeSelectionFormatter, amountSelectionFormatter) match {
            case (Some(_), Some(rsf), _) ⇒
              rsf().formatArgsInto(appendTo, allParams)
            case (_, _, Some(asf)) ⇒
              asf().formatArgsInto(appendTo, allParams)
            case _ ⇒
              UnspecifiedProblem("I need at least a formatter for an amount selection to render a selection.").failure
          }
        }

      preResV.flatMap(appendTo ⇒
        allItemsPartFormatter match {
          case Some(fmt) ⇒
            joiner.foreach { appendTo.append }
            fmt().formatArgsInto(appendTo, allParams)
          case None ⇒ appendTo.success
        })
    }
  }

  def createParamsMap(alienParameters: Map[String, Any], upperAndLowerIndex: Option[(Int, Int)], others: (String, Any)*): Map[String, Any] =
    upperAndLowerIndex match {
      case None           ⇒ alienParameters ++ others
      case Some((li, ui)) ⇒ alienParameters ++ (others :+ (lowerIndexParamName -> li) :+ (upperIndexParamName -> ui))
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

private[almhirt] final class CustomSelectionOfManyResourceValue(
    override val locale: ULocale,
    selectionSizeParameter: Option[String],
    lowerIndexParameter: Option[String],
    allItemsCountParameter: Option[String],
    upperIndexParameter: Option[String],
    formatter: () ⇒ AlmFormatter) extends BasicValueResourceValue with AlmFormatter {
  val selectionSizeParamName = selectionSizeParameter getOrElse "selection_size"
  val lowerIndexParamName = lowerIndexParameter getOrElse "lower_index"
  val allItemsCountParamName = allItemsCountParameter getOrElse "all_items_count"
  val upperIndexParamName = upperIndexParameter getOrElse "upper_index"

  val myParamNames = Set(selectionSizeParamName, lowerIndexParamName, allItemsCountParamName, upperIndexParamName)

  def formatArgsIntoAt(appendTo: StringBuffer, pos: FieldPosition, args: Map[String, Any]): AlmValidation[StringBuffer] = {
    val alienParameters = args.filterKeys { key ⇒ !myParamNames(key) }
    for {
      allItemsCount ← any2Int((args get allItemsCountParamName) getOrElse 0, allItemsCountParamName)
      lowerIndex ← (args get lowerIndexParamName).map(any2Int(_, lowerIndexParamName)).validationOut
      upperIndex ← (args get upperIndexParamName).map(any2Int(_, upperIndexParamName)).validationOut
      selectionSize ← (args get selectionSizeParamName).map(any2Int(_, selectionSizeParamName)).validationOut
      res ← formatInternal(allItemsCount, lowerIndex, upperIndex, selectionSize, appendTo, alienParameters)
    } yield res
  }

  private def formatInternal(allItemsCount: Int, lowerIndex: Option[Int], upperIndex: Option[Int], selectionSize: Option[Int], appendTo: StringBuffer, alienParameters: Map[String, Any]): AlmValidation[StringBuffer] = {
      val (effSelectionSize, upperAndLowerIndex): (Int, Option[(Int, Int)]) =
        (selectionSize, lowerIndex, upperIndex) match {
          case (Some(sz), _, _) if sz <= 0 ⇒ (0, None)
          case (Some(sz), Some(li), _)     ⇒ (sz, Some((li, li + sz - 1)))
          case (Some(sz), None, Some(ui))  ⇒ (sz, Some((ui - sz + 1, ui)))
          case (Some(sz), None, None)      ⇒ (sz, None)
          case (None, Some(li), Some(ui))  ⇒ (ui - li + 1, Some((li, ui)))
          case (None, Some(li), None)      ⇒ (1, Some((li, li)))
          case (None, None, Some(ui))      ⇒ (1, Some(ui, ui))
          case (None, None, None)          ⇒ (0, None)
        }

      val allParams = createParamsMap(alienParameters, upperAndLowerIndex, selectionSizeParamName -> effSelectionSize, allItemsCountParamName -> allItemsCount)

      formatter().formatArgsInto(appendTo, allParams)
  }

  def createParamsMap(alienParameters: Map[String, Any], upperAndLowerIndex: Option[(Int, Int)], others: (String, Any)*): Map[String, Any] =
    upperAndLowerIndex match {
      case None           ⇒ alienParameters ++ others
      case Some((li, ui)) ⇒ alienParameters ++ (others :+ (lowerIndexParamName -> li) :+ (upperIndexParamName -> ui))
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
