package almhirt.i18n.text

import almhirt.common._
import almhirt.i18n.LocaleMagnet

object WordIterator {
  def apply[L: LocaleMagnet](text: String, locale: L): AlmIterator[String] = {
    val iter = BreakIterator.wordInstance(text, locale)

    var last: Option[Int] = iter.next()
    val producer: () ⇒ Option[String] = () ⇒ {
      last match {
        case None ⇒
          None
        case Some(begin) ⇒
          last = iter.next()
          last match {
            case Some(end) ⇒
              Some(text.substring(begin, end))
            case None ⇒
              None
          }
      }

    }
    AlmIterator.fromElementsProducer { producer }.filter { _.toIterator.exists { _.isLetterOrDigit } }
  }
}