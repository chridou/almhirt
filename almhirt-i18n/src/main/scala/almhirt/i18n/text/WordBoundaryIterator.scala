package almhirt.i18n.text

import com.ibm.icu.util.ULocale
import almhirt.i18n.LocaleMagnet
import com.ibm.icu.text.BreakIterator

object WordBoundaryIterator {
  /** Returned instances are not thread safe
   */
  def apply[L: LocaleMagnet](locale: L): WordBoundaryIterator = {
    val uloc = implicitly[LocaleMagnet[L]].toULocale(locale)
    val iter = BreakIterator.getWordInstance(uloc)
    //new WordBoundaryIteratorImp(uloc, None, iter)
    ???
  }
}

/**
 * Instances are not thread safe nor anything lazy that is returned
 */
trait WordBoundaryIterator {
  def locale: ULocale
  def setText(newText: String): Unit
  def wordsIterator: Iterator[String]
  def breaksIterator: Iterator[Int]
}

//private[almhirt] class WordBoundaryIteratorImp(override val locale: ULocale, initialText: Option[String], iteratorInstance: BreakIterator) extends WordBoundaryIterator {
//  initialText.foreach { iteratorInstance.setText(_) }
//  
//  override def setText(newText: String): Unit = iteratorInstance.setText(newText)
//  
//  override def wordsIterator: Iterator[String] =
//    
//
//}